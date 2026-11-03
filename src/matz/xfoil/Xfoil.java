package matz.xfoil;

import java.time.Duration;
import java.io.IOException;
import java.util.regex.Pattern;

public class Xfoil implements AutoCloseable {
    private static final Pattern NACA_PATTERN = Pattern.compile("(?i)^NACA\\s*([0-9]{4,5})$");
    
    // maybe make this setable?
    private long maxWatchDog = 1500; // [ms]
    
    private String newline = "\n\r";

    private final Subprocess process;

    private boolean viscOn = false;

    public Xfoil(String... args) throws IOException {
        this.process = new Subprocess(args);
        readToInput(" XFOIL   c>\u001B[2C", Duration.ofMillis(this.maxWatchDog));
        toggleGui();
    }

    private static String escapeForDiagnostics(String s) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '\\') {
                result.append("\\\\");
            } else if (Character.isISOControl(c)) {
                result.append(String.format("\\u%04X", (int) c));
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    private String readToInput(String prompt, Duration timeout) {
        StringBuilder output = new StringBuilder();
        long deadline = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            String chunk = process.poll();

            if (chunk == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(
                        "Interrupted while waiting for prompt", e);
                }
                continue;
            }

            output.append(chunk);
            // check whether the output ends with the expected prompt
            if (output.toString().endsWith(prompt)) {
                output.setLength(output.length() - prompt.length());
                return output.toString();
            }
        }

        throw new IllegalStateException(
            "Watchdog timeout waiting for: " + prompt +
            "\nCaptured output:\n" + "\"\"\"" + escapeForDiagnostics(output.toString()) + "\"\"\""
        );
    }

    private void toggleGui() throws IOException {
        this.process.write("PLOP"+newline);
        readToInput("      Option, Value   (or <Return>)    c>\u001B[2C", Duration.ofMillis(this.maxWatchDog));
        this.process.write("G"+newline);
        readToInput("      Option, Value   (or <Return>)    c>\u001B[2C", Duration.ofMillis(this.maxWatchDog));
        this.process.write(newline);
        readToInput(" XFOIL   c>\u001B[2C", Duration.ofMillis(this.maxWatchDog));
    }

    public XfoilOut calc(XfoilInput input) throws IOException {
        if (!input.coord.matches("(?i)^NACA\\s*[0-9]{4,5}$")) {
            throw new IllegalArgumentException("Invalid NACA airfoil designation. Please enter a valid 4 or 5 digit NACA airfoil (e.g., NACA 2412).");
        }
        String waitingForCommand = " XFOIL   c>\u001B[2C";
        // setup
        // input NACA
        this.process.write(input.coord+newline);
        readToInput(waitingForCommand, Duration.ofMillis(this.maxWatchDog));
        
        this.process.write("OPER"+newline);
        waitingForCommand = (viscOn) ? ".OPERv   c>\u001B[2C" : ".OPERi   c>\u001B[2C";
        readToInput(waitingForCommand, Duration.ofMillis(this.maxWatchDog));
        this.process.write(String.format("RE %f", input.Re)+newline);
        readToInput(waitingForCommand, Duration.ofMillis(this.maxWatchDog));
        this.process.write(String.format("MACH %f", input.Mach)+newline);
        readToInput(waitingForCommand, Duration.ofMillis(this.maxWatchDog));
        
        // need to disable after to make function resusable
        if ((input.Re > 0 && !viscOn) || (input.Re <= 0 && viscOn)) {
            this.process.write("VISC"+newline);
            waitingForCommand = (!viscOn) ? ".OPERv   c>\u001B[2C" : ".OPERi   c>\u001B[2C";
            readToInput(waitingForCommand, Duration.ofMillis(this.maxWatchDog));
            viscOn = !viscOn;
        }
        
        // Do the calc
        this.process.write(String.format("ALFA %f", input.alpha)+newline);
        String stringData = readToInput(waitingForCommand, Duration.ofMillis(this.maxWatchDog));
        if (stringData.toLowerCase().contains("failed")) {
            throw new IllegalStateException("Something said it failed: " + stringData);
        }

        // exit for the next loop
        this.process.write(newline);
        waitingForCommand = " XFOIL   c>\u001B[2C";
        readToInput(waitingForCommand, Duration.ofMillis(this.maxWatchDog));
        return XfoilParse.parseLastResult(stringData);
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    @Override
    public void close() {
        process.close();
    }
}

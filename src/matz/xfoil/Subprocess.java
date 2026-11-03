package matz.xfoil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

public class Subprocess implements AutoCloseable{
    
    private final PtyProcess process;
    private final BufferedWriter stdin;
    private final BlockingQueue<String> stdout = new LinkedBlockingQueue<>();
    
    private volatile boolean closing;
    
    private final Thread stdoutThread;
    private final Thread stderrThread;

    private static final String EOF = new String("EOF");

    public Subprocess(String... args) throws IOException {
        this.process = new PtyProcessBuilder(args)
            .start();

        this.stdin = new BufferedWriter(
            new OutputStreamWriter(
                process.getOutputStream(), StandardCharsets.UTF_8
            )
        );

        // continuously consume stdout
        this.stdoutThread = new Thread(() -> {
            try (InputStreamReader reader = new InputStreamReader(
                    this.process.getInputStream(), StandardCharsets.UTF_8)) {

                char[] buffer = new char[1024];
                int count;

                while ((count = reader.read(buffer)) != -1) {
                    String output = new String(buffer, 0, count);

                    this.stdout.put(output);
                }
            } catch (Exception e) {
                if (!closing) {
                    e.printStackTrace();
                }
            } finally {
                this.stdout.offer(EOF);
            }
        });

        // continuously consume stderr
        stderrThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                        this.process.getErrorStream(), StandardCharsets.UTF_8))) {

                reader.lines().forEach(System.err::println);

            } catch (Exception e) {
                if (!closing) {
                    e.printStackTrace();
                }
            }
        });

        this.stdoutThread.setDaemon(true);
        this.stderrThread.setDaemon(true);

        this.stdoutThread.start();
        this.stderrThread.start();
    }
    
    // write a line to the process
    public void write(String input) throws IOException {
        stdin.write(input);
        stdin.flush(); // important send data immediately
    }

    // read the next line from stdout (blocks until a line arrives)
    public String read() throws InterruptedException {
        String line = stdout.take();

        if (line.equals(EOF)) {
            return null;
        }

        return line;
    }

    // check if output is already available (non-blocking)
    public String poll() {
        String line = stdout.poll();

        if (line == null || line.equals(EOF)) {
            return null;
        }

        return line;
    }

    boolean isAlive() {
        return this.process.isAlive();
    }

    @Override
    public void close() {
        closing = true;
        
        try {
            this.stdin.close();
        } catch (IOException ignored) {}

        this.process.destroy();

        try {
            if (!this.process.waitFor(2, TimeUnit.SECONDS)) {
                this.process.destroyForcibly();
                this.process.waitFor();
            }

            this.stdoutThread.join(1000);
            this.stderrThread.join(1000);
        } catch (InterruptedException e) {
            this.process.destroyForcibly();
            Thread.currentThread().interrupt();
        }
    }
}

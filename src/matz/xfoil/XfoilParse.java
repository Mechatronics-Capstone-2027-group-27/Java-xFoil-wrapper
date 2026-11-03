package matz.xfoil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class XfoilParse {
    private static final String NUM =
        "[-+]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[Ee][-+]?\\d+)?";

    private static final Pattern ANSI =
        Pattern.compile("\\u001B\\[[;?0-9]*[ -/]*[@-~]");

    private static final Pattern RESULT = Pattern.compile(
        "a\\s*=\\s*(" + NUM + ")\\s+CL\\s*=\\s*(" + NUM + ")" +
        "\\s*\\R\\s*Cm\\s*=\\s*(" + NUM + ")\\s+CD\\s*=\\s*(" + NUM + ")" +
        "\\s*=>\\s*CDf\\s*=\\s*(" + NUM + ")\\s+CDp\\s*=\\s*(" + NUM + ")"
    );

    static XfoilOut parseLastResult(String output) {
        output = ANSI.matcher(output).replaceAll("");
        output = output.replace("\r", "");
        
        Matcher m = RESULT.matcher(output);
        XfoilOut last = null;

        while (m.find()) {
            last = new XfoilOut(
                Double.parseDouble(m.group(1)), // alpha
                Double.parseDouble(m.group(2)), // CL
                Double.parseDouble(m.group(3)), // Cm
                Double.parseDouble(m.group(4)), // CD
                Double.parseDouble(m.group(5)), // CDf
                Double.parseDouble(m.group(6))  // CDp
            );
        }

        if (last == null) {
            throw new IllegalArgumentException("No XFOIL result found in output");
        }
        return last;
    }
}

package com.mieze.httpserver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * A simple utility class for the Webserver's {@link Page} class
 *
 * */
abstract interface Utils {
    /**
     * Takes a {@link String} and returns the HTML represantation of it.<br>
     * 
     * Special characters like '&lt;', '&gt;' or '&quot;' are replaced with HTML &#xx; represantation.
     * new lines are replaced with &lt; br &gt; and tabs with four spaces
     * @param s the {@link String}
     * @return the escaped {@link String}
    */

    public default String escapeHTML(final String s) {
        if (s==null) return "";
        final StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '\'' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else if (c == '\n') {
                out.append("<br>");
            } else if (c == '\t') {
                out.append("    ");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
    
    /**
     * Returns the stack trace of an {@link Exception} as a {@link String}
     *
     * @param e the {@link Exception}
     * @return the Stack Trace
    */
    public default String stackTraceToString(final Throwable e) {
        final StringBuilder sb = new StringBuilder();
        for (final StackTraceElement element : e.getStackTrace()) {
            sb.append("at ");
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    /** 
     * Encodes a given {@link String} URL ecoded, using {@link URLEncoder}
     *
     * @param str the {@link String}
     * @return the encoded {@link String}
    */
    public default String urlEncode(final String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** 
     * Decodes a given {@link String} URL decoded, using {@link URLEncoder}
     *
     * @param str the {@link String}
     * @return the decoded {@link String}
    */
    public default String urlDecode(final String str) {
        if (str == null || str.length() == 0) return "";
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }

    /**
     * Prints a given {@link String}.
     *
     * @param str the {@link String}
    */
    public default void echo(String str) {
        System.out.print(str);
    }

    /**
     * Formats a given {@link String} and then prints it.
     *
     * @param str
     * @param args
    */
    public default void echof(String str, Object ...args) {
        System.out.printf(str, args);
    }

    /**
     * Reads a given {@link java.io.File} from the file system and outputs its content as a {@link String}.
     *
     * @param fileName the path to the {@link java.io.File}
     * @return the content
    */
    public default String readFile(String fileName) {
        try {
            return Files.readString(Path.of(fileName));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Reads a given {@link java.io.File} from the file system and outputs its contents as a {@link Byte} array.
     *
     * @param fileName the path to the {@link java.io.File}
     * @return the content
    */
    public default byte[] readBinaryFile(String fileName) {
        try {
            return Files.readAllBytes(Path.of(fileName));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Writes a given {@link String} to a given {@link java.io.File} from the file system.
     *
     * @param fileName the path to the {@link java.io.File}
     * @param str the content
     * @param append if the file should be overwritten
    */
    public default void writeFile(String fileName, String str, boolean overwrite) {
        try {
            Files.writeString(Path.of(fileName), str, (!overwrite)?StandardOpenOption.APPEND:StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Writes a given byte array to a given {@link java.io.File} from the file system.
     *
     * @param fileName the path to the {@link java.io.File}
     * @param arr the content
     * @param overwrite if the file should be overwritten
    */
    public default void writeBinaryFile(String fileName, byte[] arr, boolean overwrite) {
        try {
            Files.write(Path.of(fileName), arr, (!overwrite)?StandardOpenOption.APPEND:StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    /**
     * Encodes a given {@link String} as {@link Base64}
     *
     * @param str the {@link String}
     * @return the encoded {@link String}
    */
    public default String toBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    /**
     * Decodes a given {@link String} from {@link Base64}
     *
     * @param str the {@link Base64} {@link String}
     * @return the decoded {@link String}
    */
    public default String fromBase64(String str) {
        return new String(Base64.getDecoder().decode(str));
    }

    /**
     * A record representing the output of a process invoked by Utils.exec
     *
     * @param exitCode the exit code.
     * @param stdout the outptut of the process.
     * @param stderr the error messages of the process
     * */
    public record ProcessOutput(int exitCode, String stdout, String stderr) {};

    /**
     * Executes a given command and returns an instance of {@link ProcessOutput} to get the output.
     *
     * @param cmd the command
     * @return the {@link ProcessOutput} instance.
    */
    public default ProcessOutput exec(String cmd) {
        try {
            var proc = new ProcessBuilder(cmd.split(" ")).start();
            var out = proc.getInputStream();
            var err = proc.getErrorStream();
            var stdout = new String(out.readAllBytes());
            var stderr = new String(err.readAllBytes());
            return new ProcessOutput(proc.exitValue(), stdout, stderr);
        } catch (IOException e) {
            return null;
        }
    }

    public default String formatDate(String format, Date date) {
        return new SimpleDateFormat(format).format(date);
    }
    /**
     * Returns the current time as a string in hh:mm:ss format.
     *
     * @return the time
    */
    public default String getTimeString() {
        return formatDate("HH:mm:ss", new Date());
    }
}

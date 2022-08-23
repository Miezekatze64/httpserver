package com.mieze.httpserver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.stream.Stream;

public class Server implements Runnable {
    public static File WEB_ROOT = new File(".");
    public static String DEFAULT_FILE = "index.html";
    public static final String FILE_NOT_FOUND = "404.html";
    public static final String METHOD_NOT_SUPPORTED = "not_supported.html";

    public static int PORT = 8080;
    public static boolean VERBOSE = false;
    
    private Socket socket;

    public Server(Socket s) {
        this.socket = s;
    }

    public static void main(String[] args) {
        parseArgs(args);
        try (ServerSocket connection = new ServerSocket(PORT)) {
            System.out.println("Server started.\nListening on port: " + PORT + "\n");

            while (true) {
                Server ws = new Server(connection.accept());
                if (VERBOSE) System.out.println("Connection opened at " + new Date() + ".");

                Thread thread = new Thread(ws);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Server connection error.\nStack trace:\n");
            e.printStackTrace();
        }
    }

    public static void parseArgs(String[] args) {
        HashMap<String, String> arguments = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                String key = args[i];
                String val = (i+1<args.length)?args[i+1]:"";
                arguments.put(key, val);
            }
        }

        String port = arguments.get("--port");
        if (port == null) port = arguments.get("-p");
        if (port != null && port.length() > 0) {
            try {
                PORT = Integer.parseInt(port);
            } catch (NumberFormatException e) {
                System.err.println("Could not parse port " + port);
            }
        }

        String verbose = arguments.get("--verbose");
        if (verbose != null) VERBOSE = true;

        String web_root = arguments.get("--web-root");
        if (web_root == null) web_root = arguments.get("-w");
        if (web_root != null && web_root.length() > 0) WEB_ROOT = new File(web_root);

        String default_file = arguments.get("--default-file");
        if (default_file == null) default_file = arguments.get("-d");
        if (default_file != null && default_file.length() > 0) DEFAULT_FILE = default_file;

        String configureDir = arguments.get("--configure");
        if (configureDir == null) configureDir = arguments.get("-c");
        if (configureDir != null) {
            if (configureDir.length() == 0) configureDir = ".";
            File dir = new File(configureDir);

            String compileScript = """
                #!/bin/bash

                shopt -s globstar

                FILES="$*"
                MAIN=$1
                MAIN_NAME=${MAIN%.java}

                function toJarCase() {
                    echo $1 |
                        sed -r 's/\\/([A-Z])/\\/\\L\\1/g' |
                        sed -r 's/^([A-Z])/\\L\\1/g'  |
                        sed -r 's/([A-Z])/-\\L\\1/g'  |
                        sed 's/^_//'
                }
                JAR_FILE="`toJarCase $MAIN_NAME.jar`"
                
                mkdir --parents tmp
                mkdir --parents "%WEB_ROOT%/`dirname $JAR_FILE`"
                
                javac $FILES -d ./tmp/
                
                if [ $? != 0 ]; then
                    exit 1;
                fi

                MAIN_NAME="`basename $MAIN_NAME`"

                cd tmp
                CLASS_FILES="`ls ./**/*.class`"
                echo $CLASS_FILES
                jar --create --main-class=$MAIN_NAME --file=../%WEB_ROOT%/$JAR_FILE $CLASS_FILES
                chmod +x ../%WEB_ROOT%/$JAR_FILE

                rm $CLASS_FILES
                cd ..
                rm -rf ./tmp/
                """.
                replace("%WEB_ROOT%", dir.toPath().toAbsolutePath().relativize(WEB_ROOT.toPath().toAbsolutePath()).toString());

                String page404 = """
                    <!DOCTYPE html>
                    <html>
                        <head>
                            <title>404 - Not found</title>
                        </head>
                        <body>
                            <h1>404 - Not found</h1>
                            <p>The requested page could not be found on the server</p>
                            <hr>
                            <em>Java HTTPServer by Miezekatze on %s.</em>
                        </body>
                    </html>
                    """.formatted(System.getProperty("os.name"));

                try {
                    File scriptFile = new File(dir, "compile");
                    Files.writeString(scriptFile.toPath(), compileScript);
                    scriptFile.setExecutable(true);

                    File file404 = new File(WEB_ROOT, "404.html");
                    Files.writeString(file404.toPath(), page404);
                    
                    File classPath = new File(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
                    File folder = new File(classPath, "page-src/com/mieze/httpserver");
                    if (!folder.exists()) return;
                    File dest = new File(dir, "com/mieze/httpserver");
                    dest.mkdirs();
                    for (String s : folder.list()) {
                        File destFile = new File(dest, s);
                        if (destFile.exists()) Files.delete(destFile.toPath());
                        Files.copy(new File(folder, s).toPath(), destFile.toPath());
                    }
                    System.out.println("Successfull configured directory.");
                    System.exit(0);

                } catch (IOException|URISyntaxException e) {
                    System.err.println("Unable to write file: ");
                    e.printStackTrace();
                    System.exit(1);
                }
        }

        if (arguments.get("--help") != null || arguments.get("-h") != null) {
            System.out.println("""
                    A simple http 1.1 web server with server-side java (jar) implementation.
                    
                    To start a simple http server in the current directory, just run \033[2;3;33m`httpserver`\033[0m

                    To create a .jar file, wich produces an HTML page, you could just place it into your web root, \033[3mbut\033[0m
                    it is recommended to use the \033[2;3;33m`httpserver --configure [DIR]`\033[0m option to
                    create a shell script used for compiling your java files with the included \033[2;3;33m`Page`\033[0m class.

                    Example of a java class:
                    \033[2;3;33m
                    import java.util.HexBattle;
                    import com.mieze.httpserver.Page;

                    public class Main extends Page {
                        public static void main(String[] args) {
                            new Main().init(args);
                        }

                        @Override
                        public void request(HashMap<String, String> args) {
                            echof("<html><head></head><body>Time: %s</body>", System.currentTimeMillis());
                            String method = getMethod();
                            // GET code here
                        }
                    }
                    
                    \033[0m

                    Options:
                    -p/--port           set port
                    -w/--web-root       set web root
                    -c/--configure      configure java source folder at given location. (the generated compile script will link to web root.)
                    -d/--default-file   set the default file show on the webserver (GET /)
                    --verbose           outputs more debug info
                    """);
            System.exit(0);
        }
    }

	@Override
	public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream outStream = null;
        String requestFile = "";
        String httpMethod = "UNKNOWN";

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            outStream = new BufferedOutputStream(socket.getOutputStream());

            String input = in.readLine();
            if (input == null || input.length() < 1) return;
            StringTokenizer tokenizer = new StringTokenizer(input);
            httpMethod = tokenizer.nextToken().toUpperCase();
            requestFile = tokenizer.nextToken();
            String httpVersion = tokenizer.nextToken().toLowerCase();
            if (!httpVersion.equalsIgnoreCase("HTTP/1.1")) {
                if (VERBOSE) System.out.println("Unsupported http version: '" + httpVersion + "', exiting...");
                return;
            }
            String hostString = in.readLine();
            StringTokenizer tokenizer2 = new StringTokenizer(hostString);
            tokenizer2.nextToken(); // "Host: "
            String host = tokenizer2.nextToken().toLowerCase();
            
            if (!httpMethod.equals("GET") && !httpMethod.equals("POST")) {
                // method not implemented
                if (VERBOSE) System.err.println("Method " + httpMethod + " not implemented yet.");
                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
                int len = (int)file.length();

                byte[] data = readFileData(file, len);
                sendHeader(out, "text/html", len, 501);
                sendData(outStream, data);
            } else {
                // method implemented
                if (requestFile.endsWith("/")) requestFile += DEFAULT_FILE;
 
                String[] args = new String[0];
                if (httpMethod.equals("GET")) {
                    // get file
                    if (requestFile.contains("?")) {
                        String[] split = requestFile.split("\\?");
                        requestFile = split[0];
                        args = split[1].split("&");
                    }
                }
                requestFile = requestFile.toLowerCase();

                File file = new File(WEB_ROOT, requestFile);
                int len = (int)file.length();
                String contentType = getContentType(requestFile);

                String next = "<>";
                String referer = "";
                String userAgent = "";
                while (in.ready()) {
                    next = in.readLine();
                    if (next == null || next.length() < 0) break;
                    int firstColon = next.indexOf(':');
                    if (firstColon < 0) break;
                    String name = next.substring(0, firstColon);
                    String content = next.substring(firstColon+2);
                    switch (name) {
                    case "Content-Type":
                        break;
                    case "Referer":
                        referer = content;
                        break;
                    case "User-Agent":
                        userAgent = content;
                        break;
                    }
                }

                if (httpMethod.equals("POST")) {
                    // post to file
                    String argString = "";
                    while (in.ready()) {
                        argString += (char)in.read();
                    }
                    args = argString.split("&");
                }

                byte[] data;
                if (!contentType.equals("java/jar")) {
                    data = readFileData(file, len);
                } else {
                    data = runJava(file, concat(new String[]{httpMethod, host, PORT+"", referer, userAgent}, args), host);
                    len = data.length;
                    contentType = "text/html";
                }

                sendHeader(out, contentType, len, 200);
                sendData(outStream, data);

                System.out.println(((httpMethod.equals("GET"))?"GET ":"POST to ") + requestFile + " of type " + contentType + " (200 OK)");
            }
        } catch (FileNotFoundException e) {
            try {
                File file = new File(WEB_ROOT, FILE_NOT_FOUND);
                int len = (int)file.length();
                byte[] data = readFileData(file, len);
                if (out != null) sendHeader(out, "text/html", len, 404);
                System.out.println(((httpMethod.equals("GET"))?"GET ":"POST to ") + requestFile + " (404 Not Found)");
                sendData(outStream, data);
            } catch (IOException e1) {
                System.err.println("An error ocuured during sending of the 404 page:");
                e1.printStackTrace();
                System.err.println();
                
                byte[] data = "Could not load 404 page.".getBytes();
                if (out != null) sendHeader(out, "text/plain", data.length, 404);
                try {
                    sendData(outStream, data);
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Server IO Error:");
            e.printStackTrace();
            System.err.println();
        } catch (Exception e) {
            System.err.println("Server ERROR:");
            e.printStackTrace();
            System.err.println();
        } finally {
           try {
                in.close();
                out.close();
                outStream.close();
                socket.close();
           } catch (SocketException e) {
               if (VERBOSE) System.out.println("socket already closed...");
           } catch (IOException e) {
                System.err.println("ERROR closing streams:");
                e.printStackTrace();
                System.err.println();
           }

           if (VERBOSE) System.out.println("Connection closed\n");
        }
	}

    private String[] concat(String[] a, String[] b) {
        return Stream.concat(Arrays.stream(a), Arrays.stream(b)).toArray(String[]::new);
    }

    private byte[] runJava(File f, String[] args, String host) throws FileNotFoundException {
        if (!f.exists()) throw new FileNotFoundException(f.toString());
        try {
            ProcessBuilder builder = new ProcessBuilder(concat(new String[]{"java", "-jar", f.getAbsolutePath()}, args)).directory(WEB_ROOT);
            Process process = builder.start();
            
            while (process.isAlive()) {
                // waiting
            }
            byte[] data = process.getInputStream().readAllBytes();
            byte[] data2 = process.getErrorStream().readAllBytes();
            if (VERBOSE) System.err.println(new String(data2));
            return data;
        } catch (IOException e) {
            System.err.println("Error during proces running:");
            e.printStackTrace();
            System.err.println();
            return null;
        }
    }

    private String getContentType(String file) {
        if (file.endsWith(".html") || file.endsWith(".html"))
            return "text/html";
        if (file.endsWith(".png")) return "image/png";
        if (file.endsWith(".jar")) return "java/jar";
        if (file.endsWith(".jpg") || file.endsWith(".jpeg")) return "image/jpeg";
        if (file.endsWith(".wasm")) return "application/wasm";
        if (file.endsWith(".pdf")) return "application/pdf";
        return "text/plain";
    }

    private byte[] readFileData(File file, int len) throws IOException {
        byte[] data = new byte[len];

        try (FileInputStream in = new FileInputStream(file)) {
            in.read(data);
        }
        return data;
    }

    private void sendHeader(PrintWriter out, String mimeType, int len, int code) {
        String message;
        switch (code) {
        case 200:
            message = "OK";
            break;
        case 501:
            message = "Not Impleneted";
            break;
        case 404:
            message = "Not Found";
            break;
        default:
            message = "Unknown";
            break;
        }
        out.println(String.format("HTTP/1.1 %d %s", code, message));
        out.println("Server: Java HTTP 1.1 WebServer by Miezekatze");
        out.println("Date: " + new Date());
        out.println("Content-type: " + mimeType);
        out.println("Content-length: " + len);
        out.println();
        out.flush();
    }

    private void sendData(BufferedOutputStream out, byte[] data) throws IOException {
        try {
            out.write(data, 0, data.length);
            out.flush();
        } catch (SocketException e) {
            if (VERBOSE) System.err.println("Error sending files: " + e.getMessage());
        }
    }
}

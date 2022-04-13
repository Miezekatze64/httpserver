# A simple HTTP-Server 

## Features
- HTTP/1.1 Server
- Static HTML/CSS/JS Pages can be placed into the web root.
- Java classes can be compiled to java archives (.jar files) and also be placed into the web root:
    - For this purpose, the [Page](page-src/com/mieze/httpserver) class can be used, which automatically parses the arguments from the server.
    - Example program:
    ```java
    import com.mieze.httpserver.Page;
    import java.util.HashMap;

    public class Example extends Page {
        public static void main(String[] args) {
            // create an instance and call the
            // init method to parse the args
            new Example().init(args);
        }

        // will be called after init, with the arguments of
        // the request (GET: URL params, POST: POST params).
        @Override
        public void request(HashMap<String, String> args) {
            // Java TextBlock is not renderd correctly by Github's markdown engine.
            // Is just a string...
            echof("""
                <!DOCTYPE html>
                <html>
                    <head>
                        <title>Example page</title>
                    </head>
                    <body>
                        %s
                    </body>
                </html>
                """, 
                // usage of the Page methods.
                "You visited " + getHost() + " at port " + getPort() + 
                ", with the " + getMethod() + "method."
            );
        }
    }
    ```

## Getting started
> Follow the below steps to install and configure the http server.

- clone the repository:
    ```sh
    $ git clone https://github.com/miezekatze64/httpserver
    ```

- compile/install the server:
    ```sh
    $ ./compile.sh
    ```
    OR
    ```shell
    $ ./install.sh
    ```

- configure a folder for your java classes:
    ```sh
    $ httpserver --configure JAVA_SOURCE_DIR --web-root WEB_ROOT
    ```

- start the server:
    ```sh
    $ httpserver
    ```
    OR
    ```sh
    $ httpserver --web-root WEB_ROOT --port PORT
    ```

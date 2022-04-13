package com.mieze.httpserver;

import java.util.HashMap;

/**
 * Represents a java page that can be run on the {@link com.mieze.httpserver.Server}.
 *
 */
public abstract class Page implements Utils {
    private String host = "";
    private String referer = "";
    private String userAgent = "";
    private String method = "";
    private int port = 0;

    /**
     * Initialized the page and parses tha arguments from the server.
     *
     * @param args the arguments from the server {method, host, port, referer, userAgent, ARGS}
    */
    public void init(String[] args) {
        System.setErr(System.out);
        if (args.length == 0) {
            System.out.println("No method (GET/POST) found!");
            System.exit(1);
        }
        if (args.length < 2) System.err.println("No host argument (ID 1) found [can be replaced with empty string]");
        if (args.length < 3) System.err.println("No port argument (ID 2) found [can be replaced with empty string]");
        if (args.length < 4) System.err.println("No referer argument (ID 3) found [can be replaced with empty string]");
        if (args.length < 5) System.err.println("No userAgent argument (ID 4) found [can be replaced with empty string]");
        this.host = args[1];
        try {
            this.port = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            this.port = -1;
        }
        this.referer = args[3];
        this.userAgent = args[4];
        HashMap<String, String> map = new HashMap<>();

        for (int i = 5; i < args.length; i++) {
            String[] split = new String[2];
            String[] sp = args[i].split("=");
            split[0] = sp[0];
            if (sp.length < 2 || sp[1] == null) split[1] = "";
            else split[1] = sp[1];
            map.put(split[0], split[1]);
        }

        this.method = args[0];
        try {
            this.request(map);
        } catch (Exception e) {
            showException(e);
        }
    }

    /**
     * Shows an {@link Exception} as HTML.
     *
     * only used by the Page class, if an exception occures in the overwritten code.
     * @param e the {@link Exception}
    */
    private void showException(Exception e) {
        echof("""
            <!DOCTYPE html>
            <html>
                <head>
                    <title>Server Error</title>
                    <style>
                    * {
                        font-family: sans-serif;
                    }
                    </style>
                </head>
                <body>
                    <h1>An unexpected error occured while running Java coode:</h1>
                    <details>
                        <summary><strong>%s</strong></summary>
                        <pre>%s</pre>
                    </details>
                </body>
            """,
            escapeHTML(e.getClass().getName() + ": " + ((e.getMessage()==null)?"":e.getMessage())),
            escapeHTML(stackTraceToString(e))
        );
    }

    /**
     * Return the host adress of the server running this page.
     *
     * @return the host adress.
    */
    public String getHost() {
        return host;
    }

    /**
     * Returns the adress of the site, the user visited before this site.
     *
     * @return the site
    */
    public String getReferer() {
        return referer;
    }

    /**
     * Return the port on which the server is currently running.
     *
     * @return the port
    */
    public int getPort() {
        return port;
    }

    /**
     * Returns the user agent of the user visitin the page.
     *
     * @return the user agent.
    */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Returns the HTTP method (GET/POST) with which the site was requested.
     *
     * @return the method
    */
    public String getMethod() {
        return method;
    }

    /**
     * This method will be called if the initialization was sucessful.
     *
     * It additionally provides a {@link HashMap} with the GET/POST arguments from the user.
     *
     * @param args the {@link HashMap} with the arguments.
    */
    public abstract void request(HashMap<String, String> args);

}

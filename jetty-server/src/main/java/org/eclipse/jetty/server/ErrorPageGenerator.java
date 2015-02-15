package org.eclipse.jetty.server;


import java.io.IOException;
import java.io.Writer;

/**
 * Created by josekalladanthyil on 15/02/15.
 */
public class ErrorPageGenerator {

    public static void jettyPoweredHTML(Writer writer) throws IOException {
        writer.write("<a href=\"http://eclipse.org/jetty\">Powered by Jetty:// Java Web Server</a><hr/>\n");
    }
}

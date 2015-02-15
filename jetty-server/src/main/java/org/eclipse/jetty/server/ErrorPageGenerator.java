package org.eclipse.jetty.server;


import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by josekalladanthyil on 15/02/15.
 */
public class ErrorPageGenerator {

    public static void jettyPoweredHTML(Writer writer) throws IOException {
        writer.write("<a href=\"http://eclipse.org/jetty\">Powered by Jetty:// Java Web Server</a><hr/>\n");
    }

    // Response !!!1
    public static void generateError(Writer writer, int code, String uri, String message) throws IOException {
        writer.write("</title>\n</head>\n<body>\n<h2>HTTP ERROR: ");
        writer.write(Integer.toString(code));
        writer.write("</h2>\n<p>Problem accessing ");
        writer.write(uri);
        writer.write(". Reason:\n<pre>    ");
        writer.write(message);
        writer.write("</pre>");
        ErrorPageGenerator.jettyPoweredHTML(writer);
        writer.write("\n</body>\n</html>\n");
    }

    // for default handler
    public static void generateError(Writer writer, HttpServletRequest request, Handler[] handlers) throws IOException {
        writer.write("<HTML>\n<HEAD>\n<TITLE>Error 404 - Not Found");
        writer.write("</TITLE>\n<BODY>\n<H2>Error 404 - Not Found.</H2>\n");
        writer.write("No context on this server matched or handled this request.<BR>");
        writer.write("Contexts known to this server are: <ul>");

        for (int i=0;handlers!=null && i<handlers.length;i++)
        {
            ContextHandler context = (ContextHandler)handlers[i];
            if (context.isRunning())
            {
                writer.write("<li><a href=\"");
                if (context.getVirtualHosts()!=null && context.getVirtualHosts().length>0)
                    writer.write("http://"+context.getVirtualHosts()[0]+":"+request.getLocalPort());
                writer.write(context.getContextPath());
                if (context.getContextPath().length()>1 && context.getContextPath().endsWith("/"))
                    writer.write("/");
                writer.write("\">");
                writer.write(context.getContextPath());
                if (context.getVirtualHosts()!=null && context.getVirtualHosts().length>0)
                    writer.write("&nbsp;@&nbsp;"+context.getVirtualHosts()[0]+":"+request.getLocalPort());
                writer.write("&nbsp;--->&nbsp;");
                writer.write(context.toString());
                writer.write("</a></li>\n");
            }
            else
            {
                writer.write("<li>");
                writer.write(context.getContextPath());
                if (context.getVirtualHosts()!=null && context.getVirtualHosts().length>0)
                    writer.write("&nbsp;@&nbsp;"+context.getVirtualHosts()[0]+":"+request.getLocalPort());
                writer.write("&nbsp;--->&nbsp;");
                writer.write(context.toString());
                if (context.isFailed())
                    writer.write(" [failed]");
                if (context.isStopped())
                    writer.write(" [stopped]");
                writer.write("</li>\n");
            }
        }

        writer.write("</ul><hr>");
        writer.write("<a href=\"http://eclipse.org/jetty\"><img border=0 src=\"/favicon.ico\"/></a>&nbsp;");
        ErrorPageGenerator.jettyPoweredHTML(writer);

        writer.write("\n</BODY>\n</HTML>\n");
    }

    //for Error Handler
    public static void generateError(Writer writer, HttpServletRequest request, int code, String message, boolean showStacks, boolean _showMessageInTitle) throws IOException {
        writer.write("<html>\n<head>\n");
        writer.write("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"/>\n");
        writer.write("<title>Error ");
        writer.write(Integer.toString(code));

        if (_showMessageInTitle)
        {
            writer.write(' ');
            write(writer, message);
        }
        writer.write("</title>\n");
        writer.write("</head>\n<body>");
        String uri= request.getRequestURI();

        writer.write("<h2>HTTP ERROR ");
        writer.write(Integer.toString(code));
        writer.write("</h2>\n<p>Problem accessing ");
        write(writer, uri);
        writer.write(". Reason:\n<pre>    ");
        write(writer, message);
        writer.write("</pre></p>");
        if (showStacks) {
            Throwable th = (Throwable) request.getAttribute("javax.servlet.error.exception");
            while(th!=null)
            {
                writer.write("<h3>Caused by:</h3><pre>");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                th.printStackTrace(pw);
                pw.flush();
                write(writer,sw.getBuffer().toString());
                writer.write("</pre>\n");

                th =th.getCause();
            }
        }

        ErrorPageGenerator.jettyPoweredHTML(writer);


    }
    private static void write(Writer writer, String string)
            throws IOException
    {
        if (string==null)
            return;

        for (int i=0;i<string.length();i++)
        {
            char c=string.charAt(i);

            switch(c)
            {
                case '&' :
                    writer.write("&amp;");
                    break;
                case '<' :
                    writer.write("&lt;");
                    break;
                case '>' :
                    writer.write("&gt;");
                    break;

                default:
                    if (Character.isISOControl(c) && !Character.isWhitespace(c))
                        writer.write('?');
                    else
                        writer.write(c);
            }
        }
    }
}

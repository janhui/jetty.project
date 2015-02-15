//
//  ========================================================================
//  Copyright (c) 1995-2015 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.server.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.ErrorPageGenerator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;


/* ------------------------------------------------------------ */
/** Default Handler.
 *
 * This handle will deal with unhandled requests in the server.
 * For requests for favicon.ico, the Jetty icon is served.
 * For reqests to '/' a 404 with a list of known contexts is served.
 * For all other requests a normal 404 is served.
 *
 *
 * @org.apache.xbean.XBean
 */
public class DefaultHandler extends AbstractHandler
{
    private static final Logger LOG = Log.getLogger(DefaultHandler.class);

    final long _faviconModified=(System.currentTimeMillis()/1000)*1000L;
    byte[] _favicon;
    boolean _serveIcon=true;
    boolean _showContexts=true;

    public DefaultHandler()
    {
        try
        {
            URL fav = this.getClass().getClassLoader().getResource("org/eclipse/jetty/favicon.ico");
            if (fav!=null)
            {
                Resource r = Resource.newResource(fav);
                _favicon=IO.readBytes(r.getInputStream());
            }
        }
        catch(Exception e)
        {
            LOG.warn(e);
        }
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.eclipse.jetty.server.server.Handler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
     */
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        if (response.isCommitted() || baseRequest.isHandled())
            return;

        baseRequest.setHandled(true);

        String method=request.getMethod();

        // little cheat for common request
        if (_serveIcon && _favicon!=null && HttpMethod.GET.is(method) && request.getRequestURI().equals("/favicon.ico"))
        {
            if (request.getDateHeader(HttpHeader.IF_MODIFIED_SINCE.toString())==_faviconModified)
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            else
            {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("image/x-icon");
                response.setContentLength(_favicon.length);
                response.setDateHeader(HttpHeader.LAST_MODIFIED.toString(), _faviconModified);
                response.setHeader(HttpHeader.CACHE_CONTROL.toString(),"max-age=360000,public");
                response.getOutputStream().write(_favicon);
            }
            return;
        }


        if (!_showContexts || !HttpMethod.GET.is(method) || !request.getRequestURI().equals("/"))
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType(MimeTypes.Type.TEXT_HTML.toString());

        try (ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(1500);)
        {
            Server server = getServer();
            Handler[] handlers = server==null?null:server.getChildHandlersByClass(ContextHandler.class);

            ErrorPageGenerator.generateError(writer, request, handlers);

            writer.flush();
            response.setContentLength(writer.size());
            try (OutputStream out=response.getOutputStream())
            {
                writer.writeTo(out);
            }
        } 
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns true if the handle can server the jetty favicon.ico
     */
    public boolean getServeIcon()
    {
        return _serveIcon;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param serveIcon true if the handle can server the jetty favicon.ico
     */
    public void setServeIcon(boolean serveIcon)
    {
        _serveIcon = serveIcon;
    }

    public boolean getShowContexts()
    {
        return _showContexts;
    }

    public void setShowContexts(boolean show)
    {
        _showContexts = show;
    }

}

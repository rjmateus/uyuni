/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.domain.auth.WebEndpoint;
import com.redhat.rhn.domain.auth.WebEndpointFactory;
import com.redhat.rhn.frontend.xmlrpc.serializer.BigDecimalSerializer;
import com.redhat.rhn.frontend.xmlrpc.serializer.ObjectSerializer;
import com.redhat.rhn.frontend.xmlrpc.serializer.SerializerFactory;

import com.suse.manager.api.ReadOnly;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import redstone.xmlrpc.XmlRpcSerializer;

/**
 * A basic servlet class that registers handlers for xmlrpc calls
 */
public class XmlRpcServlet extends HttpServlet {

    /** Comment for <code>serialVersionUID</code> */
    private static final long serialVersionUID = -9173485623604749521L;

    private static final Logger LOG = LogManager.getLogger(XmlRpcServlet.class);

    private RhnXmlRpcServer server;
    private final HandlerFactory handlerFactory;
    private final SerializerFactory serializerFactory;

    /**
     * Constructor which takes in HandlerFactory and SerializerFactory. The
     * HandlerFactory determines what methods are exposed and which handlers
     * "handle" those calls.  The SerializerFactory adds custom serializers
     * to the mix, extending the capabilities of the XMLRPC library.
     * @param hf HandlerFactory to use.
     * @param sf SerializerFactory to use.
     */
    public XmlRpcServlet(HandlerFactory hf, SerializerFactory sf) {
        handlerFactory = hf;
        serializerFactory = sf;
    }

    /**
     * default constructor
     */
    public XmlRpcServlet() {
        this(HandlerFactory.getDefaultHandlerFactory(), new SerializerFactory());
    }

    private void passControl(HttpServletResponse response) {
        try {
            response.sendRedirect("/rhn/apidoc/index.jsp");
        }
        catch (IOException e) {
            LOG.error("Error redirecting to apidoc index", e);
        }
    }

    /**
     * initialize the servlet
     */
    @Override
    public void init() {
        server = new RhnXmlRpcServer();

        registerInvocationHandlers(server);
        registerCustomSerializers(server);
        try {
            populateEndpointAuth();
        }
        catch (Exception e) {
            LOG.error("error loading XML_RPC API endpoints for authorization", e);
        }

        // enhancement: if we ever need more than one InvocationProcessor
        // we should use the ManifestFactory like we did above for the
        // handlers.
        server.addInvocationInterceptor(new XmlRpcLoggingInvocationProcessor());
    }

    public static final String HTTP_API_ROOT = "/manager/api/";

    private void populateEndpointAuth() {
        LOG.error("populate Endpoint Auth");
        if (handlerFactory != null) {
            Set<WebEndpoint> newEndpoints = new HashSet<>();
            for (String namespace : handlerFactory.getKeys()) {
                BaseHandler handler = handlerFactory.getHandler(namespace).get();
                Method[] methods = new Method[]{};
                Class clazz = handler.getClass();
                try {
                    methods = clazz.getDeclaredMethods();
                }
                catch (SecurityException e) {
                    // This should _never_ happen, because the Handler classes must
                    // have public classes if they're expected to work.method.getDeclaringClass().equals(clazz)
                    LOG.warn("no public methods in class " + clazz.getName());
                }

                // FIXME it's adding all the puublic methods. but this is not correct, since it implementa some filtering, the same way we are doing at the HttpApiRegistry class
                for (Method method: methods) {
                    if (method.getModifiers() == Modifier.PUBLIC) {
                        String classImplemenmtation = clazz.getSimpleName() + "." + method.getName();
                        String endpoint = HTTP_API_ROOT + namespace.replace('.', '/') + '/' + method.getName();
                        String httpMethod = "POST";
                        if (method.isAnnotationPresent(ReadOnly.class)) {
                            httpMethod = "GET";
                        }

                        newEndpoints.add(new WebEndpoint(classImplemenmtation, endpoint, httpMethod, WebEndpoint.Scope.A, true, true));
                    }
                }
            }
            List<WebEndpoint> existingEndpoints = WebEndpointFactory.lookupAll();

            newEndpoints.stream()
                    .filter(e -> !existingEndpoints.contains(e))
                    .forEach(WebEndpointFactory::saveWebEndpoint);

            existingEndpoints.stream()
                    .filter(e -> !newEndpoints.contains(e))
                    .forEach(WebEndpointFactory::deleteWebEndpoint);
            WebEndpointFactory.commitTransaction();
        }
    }

    private void registerCustomSerializers(RhnXmlRpcServer srvr) {
        XmlRpcSerializer serializer = srvr.getSerializer();
        serializer.addCustomSerializer(new ObjectSerializer());
        serializer.addCustomSerializer(new BigDecimalSerializer());

        // find the configured serializers...
        serializerFactory.getSerializers().forEach(serializer::addCustomSerializer);
    }

    private void registerInvocationHandlers(RhnXmlRpcServer srvr) {
        // find the configured handlers...
        for (String namespace : handlerFactory.getKeys()) {
            handlerFactory.getHandler(namespace).ifPresent(handler -> {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("registerInvocationHandler: namespace [{}] handler [{}]", namespace, handler);
                }
                srvr.addInvocationHandler(namespace, handler);
            });
        }
    }

    /**
     * executed when a get request happens
     *
     * @param request the request object
     * @param response the response object
     */
    @Override
    public void doGet(HttpServletRequest request,
                       HttpServletResponse response) {
        passControl(response);
    }

    /**
     * executed when a post request happens
     *
     * @param request the request object
     * @param response the response object
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Entered doPost");
        }

        if (request.getHeader("SOAPAction") != null) {
            passControl(response);
            return;
        }

        response.setContentType("text/xml");
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Passing control to XmlRpcServer.execute");
            }

            server.execute(request.getInputStream(),
                           response.getWriter(),
                           request.getRemoteAddr(),
                           request.getLocalName(),
                           request.getProtocol(),
                           request);

            /*
             * jesusr - 2007.09.14
             * this is still the case
             *
             * mbowman - 2005.10.06
             * Like we were raised in a barn, we are going to leave here without
             * flushing ;)
             * -- The current thinking is that Tocmat handles the outputStream
             * -- flushing and closing for us. This make sense since after this
             * -- method runs, the response still needs to go back up through
             * -- the filters and out. If things start breaking in the future,
             * -- this is a  good place to start looking.
             */
        }
        // As bad as this is, we have no choice, Marquee-xmlrpc throws
        // Throwable, so we have to catch it.
        catch (Throwable t) {
            // By the time we get here, it can't be a FaultException, so just log it
            LOG.error("Unexpected XMLRPC error", t);
        }
    }
}

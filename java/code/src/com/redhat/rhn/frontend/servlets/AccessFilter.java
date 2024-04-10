/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

package com.redhat.rhn.frontend.servlets;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.auth.WebEndpoint;
import com.redhat.rhn.domain.auth.WebEndpointFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import spark.route.HttpMethod;
import spark.route.ServletRoutes;
import spark.routematch.RouteMatch;

/**
 * AuthFilter - a servlet filter to ensure authenticated user info is put at
 * request scope properly
 */
public class AccessFilter implements Filter {

    private static Logger log = LogManager.getLogger(AccessFilter.class);
    protected static final Set<String> noAuthMethods = new HashSet<String>();
    static {
        noAuthMethods.add("/manager/login");
        noAuthMethods.add("/manager/api/login");
        noAuthMethods.add("/errors/404.jsp");
        noAuthMethods.add("/Logout.do");
    }

    /** {@inheritDoc} */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        if (log.isDebugEnabled()) {
            log.debug("ENTER AccessFilter.doFilter: {} [{}] ({})", request.getRemoteAddr(), new Date(),
                    ((HttpServletRequest) (request)).getRequestURI());
        }

        HttpServletRequest hreq = new
                RhnHttpServletRequest((HttpServletRequest)request);

        if (hreq.getServletPath().endsWith(".do") || hreq.getServletPath().endsWith(".jsp")){
            handleStructsAccess(hreq);
        } else {
            handleSparkAccess(hreq);
        }



        // Pass control on to the next filter
        chain.doFilter(request, response);
    }

    private void handleStructsAccess(HttpServletRequest hreq) {
        if (noAuthMethods.contains(hreq.getServletPath()) ){
            return ;
        }

        RequestContext requestContext = new RequestContext(hreq);
        User user = requestContext.getCurrentUser();
        if (user == null ) {
            if (!noAuthMethods.contains(hreq.getServletPath())){
                throw new PermissionException("The " + hreq.getServletPath() +
                        " is not available for unauthenticated users.");
            }
        } else {
            if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
                Optional<WebEndpoint> endpoinOpts = WebEndpointFactory.lookupByUserIdEndpointScope(user.getId(),
                        hreq.getServletPath(),
                        hreq.getMethod(),
                        WebEndpoint.Scope.W);
                if (endpoinOpts.isEmpty()) {
                    throw new PermissionException("The " + hreq.getServletPath() +
                            " API is not available to user " + user.getLogin());
                }
            }
        }
    }

    private void handleSparkAccess(HttpServletRequest hreq) {
        RouteMatch route = ServletRoutes.get().find(
                HttpMethod.get(hreq.getMethod().toLowerCase()),
                hreq.getServletPath(), hreq.getContentType());
        if (route == null)  {
            throw new PermissionException("Route not found to verify authorization for: " + hreq.getServletPath());
        }

        if (noAuthMethods.contains(route.getMatchUri()) ){
            return ;
        }

        //Log.error(route.getMatchUri());
        //ServletRoutes.get().find(context.httpMethod(), context.uri(), context.acceptType());
        RequestContext requestContext = new RequestContext(hreq);
        User user = requestContext.getCurrentUser();
        if (user == null ) {
            if (!noAuthMethods.contains(route.getMatchUri())){
                throw new PermissionException("The " + route.getRequestURI() +
                        " is not available for unauthenticated users.");
            }
        } else {
            if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
                Optional<WebEndpoint> endpoinOpts = WebEndpointFactory.lookupByUserIdEndpointScope(user.getId(),
                        route.getMatchUri(),
                        hreq.getMethod(),
                        WebEndpoint.Scope.W);
                if (endpoinOpts.isEmpty()) {
                    throw new PermissionException("The " + route.getRequestURI() +
                            " API is not available to user " + user.getLogin());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }
}

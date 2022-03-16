/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.domain.auth;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.log4j.Logger;
import org.hibernate.query.NativeQuery;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class WebEndpointFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(WebEndpointFactory.class);
    private static final WebEndpointFactory SINGLETON = new WebEndpointFactory();

    private WebEndpointFactory() {
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * @return payg ssh data object
     */
    public static WebEndpoint createWebEndpoint() {
        return new WebEndpoint();
    }

    /**
     * @param endpoint payg ssh data object
     */
    public static void saveWebEndpoint(WebEndpoint endpoint) {
        SINGLETON.saveObject(endpoint);
    }

    public static void deleteWebEndpoint(WebEndpoint endpoint) {
        getSession().delete(endpoint);
    }

    public static List<WebEndpoint> lookupByUserId(Long userId) {
        if (userId == null) {
            return new LinkedList<>();
        }
        NativeQuery<WebEndpoint> query = getSession().getNamedNativeQuery("WebEndpoint_userEndpoints");
        query.setParameter("user_id", userId);
        return query.getResultList();
    }
    public static Optional<WebEndpoint> lookupByUserIdEndpointScope(Long userId, String endpoint, WebEndpoint.Scope scope) {
        NativeQuery<WebEndpoint> query = getSession().getNamedNativeQuery("WebEndpoint_user_access");
        query.setParameter("user_id", userId);
        query.setParameter("endpoint", endpoint);
        query.setParameter("scope", scope.name());
        return query.uniqueResultOptional();
    }

    public static List<WebEndpoint> lookupAll() {
        return getSession().createQuery("FROM WebEndpoint").list();
    }

    public static Optional<WebEndpoint> lookupByEndpoint(String endpoint) {
        if (endpoint == null) {
            return Optional.empty();
        }
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<WebEndpoint> select = builder.createQuery(WebEndpoint.class);
        Root<WebEndpoint> root = select.from(WebEndpoint.class);
        select.where(builder.equal(root.get("endpoint"), endpoint));

        return getSession().createQuery(select).uniqueResultOptional();
    }

    public static List<WebEndpoint> lookupByScope(WebEndpoint.Scope scope) {
        if (scope == null) {
            return new LinkedList<>();
        }
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<WebEndpoint> select = builder.createQuery(WebEndpoint.class);
        Root<WebEndpoint> root = select.from(WebEndpoint.class);
        select.where(builder.equal(root.get("scope"), scope));

        return getSession().createQuery(select).getResultList();
    }
}

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

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "suseWebEndpoint")
@NamedNativeQueries({
        @NamedNativeQuery(name = "WebEndpoint_userEndpoints",
                query = "select e.* from suseWebEndpoint e join suseUserWebEndpoint ue on e.id=ue.web_endpoint_id " +
                " where ue.user_id=:user_id"
        ),
        @NamedNativeQuery(name = "WebEndpoint_user_access",
                query = "select e.* from suseWebEndpoint e join suseUserWebEndpoint ue on e.id=ue.web_endpoint_id " +
                " where ue.user_id=:user_id and e.endpoint=:endpoint and e.scope=:scope"
        )
})
public class WebEndpoint extends BaseDomainHelper {
    private Long id;

    private String namespace;
    private String className;
    private String endpoint;
    private Scope scope;

    /**
     * Status of the {@link PaygSshData}
     */
    public enum Scope {
        A("API"),
        W("Web application");

        private final String label;

        Scope(String labelIn) {
            this.label = labelIn;
        }

        /**
         * Return the label
         * @return the label
         */
        public String getLabel() {
            return label;
        }
    }

    /**
     * Standard constructor.
     */
    public WebEndpoint() {
    }

    /**
     * full constructor
     * @param namespaceIn
     * @param classNameIn
     * @param methodNameIn
     * @param scopeIn
     */
    public WebEndpoint(String namespaceIn, String classNameIn, String methodNameIn, Scope scopeIn) {
        namespace = namespaceIn;
        className = classNameIn;
        endpoint = methodNameIn;
        scope = scopeIn;
    }

    /**
     * Gets the id.
     * @return the id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suseWebEndpoint_seq")
    @SequenceGenerator(name = "suseWebEndpoint_seq", sequenceName = "suseWebEndpoint_id_seq",
            allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn the new id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    @Column(name = "namespace")
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespaceIn) {
        this.namespace = namespaceIn;
    }


    @Column(name = "class_name")
    public String getClassName() {
        return className;
    }

    public void setClassName(String classNameIn) {
        this.className = className;
    }


    @Column
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpointIn) {
        this.endpoint = endpoint;
    }


    @Column(name = "scope")
    @Enumerated(EnumType.STRING)
    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scopeIn) {
        this.scope = scope;
    }


    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }
        WebEndpoint that = (WebEndpoint) oIn;
        return Objects.equals(namespace, that.namespace) &&
                Objects.equals(className, that.className) &&
                Objects.equals(endpoint, that.endpoint) &&
                scope == that.scope;
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, className, endpoint, scope);
    }
}

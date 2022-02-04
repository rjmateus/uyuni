/*
 * Copyright (c) 2017 SUSE LLC
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

package com.suse.manager.webui.websocket;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * A WebSocket Session configuration manager
 *
 * WebsocketSessionConfigurator
 */
public class WebsocketSessionConfigurator extends ServerEndpointConfig.Configurator {

    private static final Logger LOG = Logger.getLogger(WebsocketSessionConfigurator.class);

    @Override
    public void modifyHandshake(ServerEndpointConfig config,
            HandshakeRequest request,
            HandshakeResponse response) {
        HttpSession httpSession = (HttpSession)request.getHttpSession();
        if (httpSession.getAttribute("webUserID") != null) {
            config.getUserProperties().put("webUserID", httpSession.getAttribute("webUserID"));
        }
        else {
            LOG.info("session field 'webUserID' was null during websocket handshake");
            config.getUserProperties().remove("webUserID");
        }
    }
}

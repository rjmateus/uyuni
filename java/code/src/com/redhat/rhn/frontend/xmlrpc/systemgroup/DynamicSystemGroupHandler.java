/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.systemgroup;

import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.webui.services.iface.SaltApi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AdminConfigurationHandler
 * @apidoc.namespace admin.configuration
 * @apidoc.doc Provides methods to configure the #product() server.
 */
public class DynamicSystemGroupHandler extends BaseHandler {
    private static final Logger LOG = LogManager.getLogger(DynamicSystemGroupHandler.class);
    private final ServerGroupHandler serverGroupHandler;
    private final SystemHandler systemHandler;
    private final SaltApi saltApi;

    /**
     * @param serverGroupHandlerIn ServerGroupHandler
     * @param systemHandlerIn SystemHandler
     * @param saltApiIn SaltApi
     */
    public DynamicSystemGroupHandler(ServerGroupHandler serverGroupHandlerIn,
                                     SystemHandler systemHandlerIn,
                                     SaltApi saltApiIn) {
         serverGroupHandler = serverGroupHandlerIn;
         systemHandler = systemHandlerIn;
         saltApi = saltApiIn;
    }

    private void updateGroup(User loggedInUser,  Integer groupID,
                             String target, String targetType) {
        ServerGroup group = serverGroupHandler.getDetails(loggedInUser, groupID);

        if (target == null || targetType == null || target.isEmpty() || targetType.isEmpty()) {
            // do not call salt to get system list
            return;
        }

        List<Long> currentSystems = SystemManager.systemsInGroupShort(group.getId()).stream()
                .map(system -> system.getId())
                .toList();

        Map<String, Long> minionIdMap = systemHandler.getMinionIdMap(loggedInUser);
        LOG.debug("Minion map {}", minionIdMap);
        List<String> minions = saltApi.selectMinions(target, targetType);
        LOG.debug("Selected minions {}", minions);
        List<Long> selectedMinionIds = minions.stream()
                .map(minionIdMap::get)
                .filter(id -> id != null)
                .toList();

        List<Long> toAdd = new ArrayList<>();
        for (Long id: selectedMinionIds) {
            if (!currentSystems.contains(id)) {
                toAdd.add(id);
            }
        }
        LOG.debug("System IDs to add: {}", toAdd);
        if (!toAdd.isEmpty()) {
            serverGroupHandler.addOrRemoveSystems(loggedInUser, group.getName(), toAdd, true);
        }

        List<Long> toRemove = new ArrayList<>();
        for (Long id: currentSystems) {
            if (!selectedMinionIds.contains(id)) {
                toRemove.add(id);
            }
        }
        LOG.debug("System IDs to remove: {}", toRemove);
        if (!toRemove.isEmpty()) {
            serverGroupHandler.addOrRemoveSystems(loggedInUser, group.getName(), toRemove, false);
        }
    }

    /**
     * Configure the server.
     * @param loggedInUser the current user
     * @param groupID if of the group we want to compute the list of minions
     * @return 1 on success
     *
     * @apidoc.doc Configure server.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("integer", "group id", "Group id for which we want to compute the name")
     * @apidoc.returntype #return_int_success()
     */
    public int computeGroup(User loggedInUser, Integer groupID) {
        ensureSystemGroupAdmin(loggedInUser);
        // https://docs.saltproject.io/en/3006/topics/targeting/compound.html#targeting-compound
        updateGroup(loggedInUser, groupID, "*", "glob");

        return 1;
    }
}

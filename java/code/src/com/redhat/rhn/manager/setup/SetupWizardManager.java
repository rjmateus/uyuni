/**
 * Copyright (c) 2014 SUSE
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

package com.redhat.rhn.manager.setup;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.BaseManager;
import com.redhat.rhn.manager.satellite.ConfigureSatelliteCommand;
import com.suse.manager.ncc.ListSubscriptions;

public class SetupWizardManager extends BaseManager {

    // Logger for this class
    private static Logger logger = Logger.getLogger(SetupWizardManager.class);

    // Config keys
    public final static String KEY_MIRRCREDS_USER = "server.susemanager.mirrcred_user";
    public final static String KEY_MIRRCREDS_PASS = "server.susemanager.mirrcred_pass";
    public final static String KEY_MIRRCREDS_EMAIL = "server.susemanager.mirrcred_email";

    // NCC URL for listing subscriptions
    public final static String NCC_URL = "https://secure-www.novell.com/center/regsvc/?command=listsubscriptions";

    /**
     * Find all valid mirror credentials and return them.
     * @return List of all available mirror credentials
     */
    public static List<MirrorCredentials> getMirrorCredentials() {
        List<MirrorCredentials> credsList = new ArrayList<MirrorCredentials>();

        // Get the main pair of credentials
        String user = Config.get().getString(KEY_MIRRCREDS_USER);
        String password = Config.get().getString(KEY_MIRRCREDS_PASS);
        String email = Config.get().getString(KEY_MIRRCREDS_EMAIL);

        // Add credentials as long as they have user and password
        MirrorCredentials creds;
        int index = 1;
        while (user != null && password != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Mirror Credentials: " + user + ":" + password + ", " + email);
            }

            // Create credentials object
            creds = new MirrorCredentials(user, password, email);
            credsList.add(creds);

            // Search additional credentials with continuous enumeration
            user = Config.get().getString(KEY_MIRRCREDS_USER + "." + index);
            password = Config.get().getString(KEY_MIRRCREDS_PASS + "." + index);
            email = Config.get().getString(KEY_MIRRCREDS_EMAIL + "." + index);
            index++;
        }

        return credsList;
    }

    /**
     * Store mirror credentials in the filesystem.
     * TODO: Extend this to store a given list of {@link MirrorCredentials}.
     * @param creds mirror credentials to store
     * @return list of validation errors or null in case of success
     */
    public static ValidatorError[] storeMirrorCredentials(MirrorCredentials creds, User userIn) {
        ConfigureSatelliteCommand configCommand = new ConfigureSatelliteCommand(userIn);
        configCommand.updateString(KEY_MIRRCREDS_USER, creds.getUser());
        configCommand.updateString(KEY_MIRRCREDS_PASS, creds.getPassword());
        configCommand.updateString(KEY_MIRRCREDS_EMAIL, creds.getEmail());
        return configCommand.storeConfiguration();
    }

    /**
     * Connect to NCC and return subscriptions for a given pair of credentials.
     * @param creds the mirror credentials to use
     */
    public static void listSubscriptions(MirrorCredentials creds) {
        // Setup XML to send it with the request
        ListSubscriptions listsubs = new ListSubscriptions();
        listsubs.setUser(creds.getUser());
        listsubs.setPassword(creds.getPassword());
        PostMethod post = new PostMethod(NCC_URL);
        try {
            // Serialize into XML
            Serializer serializer = new Persister();
            StringWriter xmlString = new StringWriter();
            serializer.write(listsubs, xmlString);
            RequestEntity entity = new StringRequestEntity(
                    xmlString.toString(), "text/xml", "UTF-8");

            // Manually follow redirects as long as we get 302
            HttpClient httpclient = new HttpClient();
            int result = 0;
            do {
                if (result == 302) {
                    // Read the redirect location from header
                    Header locationHeader = post.getResponseHeader("Location");
                    String location = locationHeader.getValue();
                    logger.info("Got 302, following redirect to: " + location);
                    post = new PostMethod(location);
                }

                // Execute the request
                post.setRequestEntity(entity);
                result = httpclient.executeMethod(post);
                if (logger.isDebugEnabled()) {
                    logger.debug("Response status code: " + result);
                    logger.debug("Response body:\n" + post.getResponseBodyAsString());
                }
            } while (result == 302);
        } catch (HttpException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            logger.debug("Releasing connection");
            post.releaseConnection();
        }
    }
}

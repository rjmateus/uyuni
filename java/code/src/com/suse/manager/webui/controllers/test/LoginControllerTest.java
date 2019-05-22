package com.suse.manager.webui.controllers.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.suse.manager.webui.controllers.login.LoginController;
import com.suse.manager.webui.utils.SparkTestUtils;
import com.suse.utils.Json;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;

public class LoginControllerTest extends BaseControllerTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testUrlBounce() throws UnsupportedEncodingException {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");
        Map<String, String> params = new HashMap<>();
        params.put("url_bounce", "/rhn/UserDetails.do?sid=1");
        Request request = SparkTestUtils.createMockRequestWithBody(
                "http://localhost:8080/rhn/manager/login?url_bounce=:url_bounce",
                new HashMap<String, String>(),
                Json.GSON.toJson(new LoginController.LoginCredentials("admin", "admin")),
                params);
        Response response = RequestResponseFactory.create(new RhnMockHttpServletResponse());

        String modelView = LoginController.login(request, response);

        // verify
        String bounce = (String) request.raw().getAttribute("url_bounce");

        assertNotNull(bounce);
        assertEquals(bounce, "/rhn/UserDetails.do?sid=1");
        assertNotNull(response);
        System.out.println(modelView);
    }

//    public void testLoginWithSSO() {
//        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");
//        LoginSetupAction action = new LoginSetupAction();
//        ActionMapping mapping = new ActionMapping();
//        mapping.addForwardConfig(
//                new ActionForward(RhnHelper.DEFAULT_FORWARD, "path", false));
//        RhnMockDynaActionForm form = new RhnMockDynaActionForm("loginForm");
//        RhnMockHttpServletRequest req = new RhnMockHttpServletRequest();
//        RhnMockHttpServletResponse resp = new RhnMockHttpServletResponse();
//        req.setSession(new MockHttpSession());
//        req.setupServerName("mymachine.rhndev.redhat.com");
//
//        ActionForward rc = action.execute(mapping, form, req, resp);
//        assertNull(rc); // we are redirected to IdP login page
//    }
//
//    public void testLoginWithoutSSO() {
//        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");
//        LoginSetupAction action = new LoginSetupAction();
//        ActionMapping mapping = new ActionMapping();
//        mapping.addForwardConfig(
//                new ActionForward(RhnHelper.DEFAULT_FORWARD, "path", false));
//        RhnMockDynaActionForm form = new RhnMockDynaActionForm("loginForm");
//        RhnMockHttpServletRequest req = new RhnMockHttpServletRequest();
//        RhnMockHttpServletResponse resp = new RhnMockHttpServletResponse();
//        req.setSession(new MockHttpSession());
//        req.setupServerName("mymachine.rhndev.redhat.com");
//
//        ActionForward rc = action.execute(mapping, form, req, resp);
//        assertNotNull(rc);
//    }
}

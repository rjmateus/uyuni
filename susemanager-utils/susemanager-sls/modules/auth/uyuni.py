"""
Provide authentication using a UYUNI backend

UYUNI auth can be defined like any other eauth module:

.. code-block:: yaml

    external_auth:
      uyuni:
        ^url: https://url/for/rest/call
        fred:
          - .*
          - '@runner'

If there are entries underneath the url entry then they are merged with any responses
from the UYUNI call.  In the above example, assuming the Uyuni call does not return
any additional ACLs, this will authenticate Fred via a UYUNI call and allow him to
run any execution module and all runners.

The Uyuni call should return a JSON array that maps to a regular eauth YAML
structure of a user as above.

"""


import logging

import salt.utils.http

from xmlrpc.client import ServerProxy
import ssl

log = logging.getLogger(__name__)

__virtualname__ = "uyuni"


def __virtual__():
    return __virtualname__


def _uyuni_auth_setup():

    if "^url" in __opts__["external_auth"]["uyuni"]:
        return __opts__["external_auth"]["uyuni"]["^url"]
    else:
        return False


def getClient():
    url = _uyuni_auth_setup()

    if not url:
        log.critical("URL not set")
        return False

    log.debug("calling uyuni authentication with URL: %s", url)

    context = ssl._create_unverified_context()
    client = ServerProxy(url, context=context)
    return client


def authenticate(username, password):
    """
    Call the uyuni XML-RPC API authentication endpoint
    """
    client = getClient()
    try:
        log.debug("calling uyuni authentication for USER: %s", username)
        key = client.auth.login(username, password)
        if key is None:
            return False
        else:
            return key
    except Exception as exc:
        log.error("Unable to login to the Uyuni server: %s", exc)
        raise False

def auth(username, password):
    """
    REST authentication
    """
    # Check auth on API endpoint
    result = authenticate(username, password)
    if result is False:
        log.debug("eauth Uyuni call failed: %s", result)
        return False
    else:
        log.debug("eauth Uyuni call Ok: %s", result)
        return True


def fetchAcl(username, password):
    result = authenticate(username, password)
    if result is False:
        log.debug("eauth Uyuni call failed: %s", result)
        return None
    else:
        client = getClient()
        try:
            log.debug("calling uyuni authentication for USER: %s", username)
            key = client.auth.login(username, password)
            if key is None:
                return None
            else:
                acls = client.user.getSaltAcl(key)
                log.critical("server returned ACL: %s", acls)
                client.auth.logout(key)
                return acls
        except Exception as exc:
            log.error("Unable to login to the Uyuni server: %s", exc)
            raise None


def acl(username, **kwargs):
    salt_eauth_acl = __opts__["external_auth"]["uyuni"].get(username, [])
    log.critical("acl from salt for user %s: %s", username, salt_eauth_acl)

    # Get ACL from uyuni API
    eauth_uyuni_acl = []
    result = fetchAcl(username, kwargs["password"])
    if result:
        eauth_uyuni_acl = result
        log.debug("acl from uyuni for user %s: %s", username, eauth_uyuni_acl)

    merged_acl = salt_eauth_acl + eauth_uyuni_acl

    log.debug("acl from salt and uyuni merged for user %s: %s", username, merged_acl)
    # We have to make the .get's above return [] since we can't merge a
    # possible list and None. So if merged_acl is falsey we return None so
    # other eauth's can return an acl.
    if not merged_acl:
        return None
    else:
        return merged_acl

#------------------------------------------------
#
# def process_acl(auth_list, opts=None):
#     log.critical("process_acl input: %s", auth_list)
#     merged_acl =  auth_list + [{'uyuni-minion.suse.lab': ['.*']}, {'uyuni-minion2.suse.lab': ['.*']}]
#     log.critical("process_acl input: %s", merged_acl)
#     return merged_acl


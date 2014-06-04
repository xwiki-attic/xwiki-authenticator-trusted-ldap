Support JAAS remote user based SSO authentication and get informations from LDAP server.

Report any bug or suggest new feature in http://jira.xwiki.org/browse/AUTHTLDAP.

# This authenticator execute the following process

* get remote user from Portlet or Servlet request
** extract the user uid from the domain
** extract the user domain/server id from the remote user and get from the configuration the LDAP configuration to use for each LDAP domain/server
* if no remote user is provided it falback on standard LDAP authentication

If SSO fail, it tries standard LDAP authentication.

# Configuration

## xwiki.cfg file

    #-# A Java regexp used to parse the remote user provided by JAAS
    #-# The following matches re users like UID@DOMAIN:
    # xwiki.authentication.trustedldap.remoteUserParser=(.+)@(.+)
    
    #-# Indicate which of the regexp group correspond to which LDAP properties
    #-# The following LDAP properties are supported:
    #-#   login, password, ldap_server, ldap_base_DN, ldap_bind_DN, ldap_bind_pass
    #-# The following indicate that the first regex group is associated to the login:
    # xwiki.authentication.trustedldap.remoteUserMapping.1=login
    #-# The following indicate that the second regex group is associated everything else (the mapping is then used indicate which is the fallue for each property):
    # xwiki.authentication.trustedldap.remoteUserMapping.2=ldap_server,ldap_base_DN,ldap_bind_DN,ldap_bind_pass
    
    #-# Indicate how to convert each found property
    #-# Here is an example mapping each of the domains MYDOMAIN and MYDOMAIN2 to specific properties:
    # xwiki.authentication.trustedldap.remoteUserMapping.ldap_server=MYDOMAIN=my.domain.com|MYDOMAIN2=my.domain2.com
    # xwiki.authentication.trustedldap.remoteUserMapping.ldap_base_DN=MYDOMAIN=dc=my,dc=domain,dc=com|MYDOMAIN2=dc=my,dc=domain2,dc=com
    # xwiki.authentication.trustedldap.remoteUserMapping.ldap_bind_DN=MYDOMAIN=cn=bind,dc=my,dc=domain,dc=com|MYDOMAIN2=cn=bind,dc=my,dc=domain2,dc=com
    # xwiki.authentication.trustedldap.remoteUserMapping.ldap_bind_pass=MYDOMAIN=password|MYDOMAIN2=password2
    
    #-# For other LDAP related configuration refer to standard LDAP authenticator documentation

## XWikiPreferences

It's also possible to put any of theses configuration in the XWiki.XWikiPreferences object in the XWiki.XWikiPreferences page. Add a string field with the proper name to the class and put the value you want.

The fields names are not exactly the same, you have to change "xwiki.authentication.trustedldap." prefix to "trustedldap_":

xwiki.authentication.trustedldap.remoteUserParser -> trustedldap_remoteUserParser
...

# Install

* copy this authenticator jar file into WEB_INF/lib/
* setup xwiki.cfg with:
xwiki.authentication.authclass=com.xwiki.authentication.trustedldap.TrustedLDAPAuthServiceImpl

# Troubleshoot

## Debug log

    <!-- Standard LDAP debugging -->
    <logger name="com.xpn.xwiki.plugin.ldap" level="trace"/>
    <logger name="com.xpn.xwiki.user.impl.LDAP" level="trace"/>
    <!-- Trusted LDAP debugging -->
    <logger name="com.xwiki.authentication.Config" level="trace"/>
    <logger name="com.xwiki.authentication.trustedldap" level="trace"/>

See http://platform.xwiki.org/xwiki/bin/view/AdminGuide/Logging for general information about logging in XWiki.

# TODO

* generic support of LDAP property in remoteUserMapping configuration

# Changelog

## 1.0.3

AUTHTLDAP-2: Fixed a crash when the xwiki.authentication.encryptionKey key contains non ASCII characters.

## 1.0.1 and 1.0.2

Fix documentation (mostly SAAS -> JAAS).

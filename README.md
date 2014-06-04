Support JAAS remote user based SSO authentication and get informations from LDAP server.

Report any bug or suggest new feature in http://jira.xwiki.org/browse/AUTHTLDAP.

# This authenticator execute the following process

1. get remote user from the request
  1. extract the user uid from the domain
  2. extract the user domain/server id from the remote user and get from the configuration the LDAP configuration to use for each LDAP domain/server
2. if no remote user is provided but the standard XWiki login/pass form is used, **1.** is done but this time with the passed login instead of the remote user and the password is validated
3. if all this fail it falback on standard LDAP authentication

If SSO fail, it tries standard LDAP authentication.

# Configuration

## xwiki.cfg file

    #-# A Java regexp used to parse the remote user provided by JAAS.
    #-# 
    #-# The following matches the users like UID@DOMAIN:
    # xwiki.authentication.trustedldap.remoteUserParser=(.+)@(.+)
    
    #-# Indicate which of the regexp group correspond to which LDAP properties.
    #-# The following LDAP properties are reserved (any other property can be defined as variable for xwiki.authentication.trustedldap.userPageName):
    #-#   * login: the uid of the user
    #-#   * password: the password of the user
    #-#   * ldap_server: the host of the server, see xwiki.authentication.ldap.server for more details
    #-#   * ldap_port: the port of the server, see xwiki.authentication.ldap.port for more details
    #-#   * ldap_base_DN: the base DN used to search in the LDAP server, see xwiki.authentication.ldap.base_DN for more details
    #-#   * ldap_bind_DN: the bind DN used to access the LDAP server, see xwiki.authentication.ldap.bind_DN for more details
    #-#   * ldap_bind_pass: the bind password used to access the LDAP server, see xwiki.authentication.ldap.bind_pass for more details
    #-# 
    #-# The following indicate that the first regexp group is associated to the login:
    # xwiki.authentication.trustedldap.remoteUserMapping.1=login
    #-# The following indicate that the second regexp group is associated to everything else (the mapping is then used to indicate which is the vallue for each property):
    # xwiki.authentication.trustedldap.remoteUserMapping.2=domain,ldap_server,ldap_port,ldap_base_DN,ldap_bind_DN,ldap_bind_pass
    
    #-# Indicate how to convert each found property. If a property is not set, the standard LDAP authenticator setup is used.
    #-# 
    #-# Here is an example mapping each of the domains MYDOMAIN and MYDOMAIN2 to specific properties:
    # xwiki.authentication.trustedldap.remoteUserMapping.ldap_server=MYDOMAIN=my.domain.com|MYDOMAIN2=my.domain2.com
    # xwiki.authentication.trustedldap.remoteUserMapping.ldap_port=MYDOMAIN=388|MYDOMAIN2=387
    # xwiki.authentication.trustedldap.remoteUserMapping.ldap_base_DN=MYDOMAIN=dc=my,dc=domain,dc=com|MYDOMAIN2=dc=my,dc=domain2,dc=com
    # xwiki.authentication.trustedldap.remoteUserMapping.ldap_bind_DN=MYDOMAIN=cn=bind,dc=my,dc=domain,dc=com|MYDOMAIN2=cn=bind,dc=my,dc=domain2,dc=com
    # xwiki.authentication.trustedldap.remoteUserMapping.ldap_bind_pass=MYDOMAIN=password|MYDOMAIN2=password2
    
    #-# The XWiki page name pattern.
    #-# Can use xwiki.authentication.trustedldap.remoteUserParser group or a properties defined in xwiki.authentication.trustedldap.remoteUserMapping.
    #-# The supported syntax is org.apache.commons.lang3.text.StrSubstitutor one,
    #-# see http://commons.apache.org/proper/commons-lang/javadocs/api-3.0/org/apache/commons/lang3/text/StrSubstitutor.html for more details.
    #-# The default is "${login}".
    #-# 
    #-# In this example the XWiki user profile page name will be of the form MYDOMAIN-myuid
    # xwiki.authentication.trustedldap.userPageName=${domain}-${login}
    
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

## 1.1

* [AUTHTLDAP-3](http://jira.xwiki.org/browse/AUTHTLDAP-3): Add support for custom port
* [AUTHTLDAP-4](http://jira.xwiki.org/browse/AUTHTLDAP-4): Make sure users from different servers but with same uid don't collide

## 1.0.3

* [AUTHTLDAP-2](http://jira.xwiki.org/browse/AUTHTLDAP-2): Fixed a crash when the xwiki.authentication.encryptionKey key contains non ASCII characters.

## 1.0.1 and 1.0.2

Fix documentation (mostly SAAS -> JAAS).

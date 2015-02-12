/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xwiki.authentication.trustedldap;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPConfig;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPConnection;
import com.xwiki.authentication.Config;

public class TrustedLDAPConfig extends Config
{
    /**
     * LogFactory <code>LOGGER</code>.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    protected static final String PREF_KEY = "trustedldap";

    protected static final String CONF_KEY = "xwiki.authentication.trustedldap";

    public TrustedLDAPConfig()
    {
        super(PREF_KEY, CONF_KEY);
    }

    public Pattern getRemoteUserPattern(XWikiContext context)
    {
        String param = getParam("remoteUserParser", null, context);

        return param != null ? Pattern.compile(param) : null;
    }

    public List<String> getRemoteUserMapping(int groupId, XWikiContext context)
    {
        return getListParam("remoteUserMapping." + groupId, ',', Collections.<String> emptyList(), context);
    }

    public Map<String, String> getRemoteUserMapping(String propertyName, XWikiContext context)
    {
        return getRemoteUserMapping(propertyName, false, context);
    }

    public Map<String, String> getRemoteUserMapping(String propertyName, boolean forceLowerCaseKey, XWikiContext context)
    {
        return getMapParam("remoteUserMapping." + propertyName, '|', Collections.<String, String> emptyMap(),
            forceLowerCaseKey, context);
    }

    public String getLDAPServer(Map<String, String> remoteUserLDAPConfiguration, XWikiContext context)
    {
        String ldapServer = remoteUserLDAPConfiguration.get("ldap_server");
        if (ldapServer == null) {
            ldapServer = XWikiLDAPConfig.getInstance().getLDAPParam("ldap_server", "localhost", context);
        }

        return ldapServer;
    }

    public int getLDAPPort(Map<String, String> remoteUserLDAPConfiguration, XWikiContext context)
    {
        int ldapPort = 389;

        String ldapPortString = remoteUserLDAPConfiguration.get("ldap_port");
        if (ldapPortString == null) {
            ldapPort = XWikiLDAPConfig.getInstance().getLDAPPort(context);
        } else {
            NumberUtils.toInt(ldapPortString, 389);
        }

        return ldapPort;
    }

    public String getLDAPBaseDN(Map<String, String> remoteUserLDAPConfiguration, XWikiContext context)
    {
        String ldapBaseDN = remoteUserLDAPConfiguration.get("ldap_base_DN");
        if (ldapBaseDN == null) {
            ldapBaseDN = XWikiLDAPConfig.getInstance().getLDAPParam("ldap_base_DN", "", context);
        }

        return ldapBaseDN;
    }

    public String getLDAPBindDNFormat(Map<String, String> remoteUserLDAPConfiguration, XWikiContext context)
    {
        String remoteUser_bind_DN = remoteUserLDAPConfiguration.get("ldap_bind_DN");

        return remoteUser_bind_DN != null ? remoteUser_bind_DN : XWikiLDAPConfig.getInstance().getLDAPBindDN(context);
    }

    public String getLDAPBindDN(Map<String, String> remoteUserLDAPConfiguration, XWikiContext context)
    {
        String login = remoteUserLDAPConfiguration.get("login");
        String password = remoteUserLDAPConfiguration.get("password");

        String format = getLDAPBindDNFormat(remoteUserLDAPConfiguration, context);

        return MessageFormat.format(format, XWikiLDAPConnection.escapeLDAPDNValue(login),
            XWikiLDAPConnection.escapeLDAPDNValue(password));
    }

    public String getLDAPBindPassword(Map<String, String> remoteUserLDAPConfiguration, XWikiContext context)
    {
        String login = remoteUserLDAPConfiguration.get("login");
        String password = remoteUserLDAPConfiguration.get("password");

        String remoteUser_bind_pass = remoteUserLDAPConfiguration.get("ldap_bind_pass");

        return MessageFormat.format(remoteUser_bind_pass != null ? remoteUser_bind_pass : XWikiLDAPConfig.getInstance()
            .getLDAPBindPassword(context), login, password);
    }

    public String getUserPageName(Map<String, String> remoteUserLDAPConfiguration, XWikiContext context)
    {
        String userPageName = getParam("userPageName", "${login}", context);

        String pageName = StrSubstitutor.replace(userPageName, remoteUserLDAPConfiguration);

        // Protected from characters not well supported in user page name depending on the version of XWiki
        pageName = StringUtils.remove(pageName, '.');
        pageName = StringUtils.remove(pageName, ' ');
        pageName = StringUtils.remove(pageName, '/');

        LOGGER.debug("UserPageName: {}", pageName);

        return pageName;
    }

    public Set<String> getTestLoginFor(Map<String, String> remoteUserLDAPConfiguration, XWikiContext context)
    {
        List<String> list = getListParam("testLoginFor", ',', Collections.<String> emptyList(), context);

        Set<String> set = new HashSet<String>(list.size());
        for (String uid : list) {
            set.add(StrSubstitutor.replace(uid, remoteUserLDAPConfiguration));
        }

        LOGGER.debug("TestLoginFor: {}", set);

        return set;
    }

    /**
     * Get mapping between XWiki groups names and LDAP groups names.
     * 
     * @param context the XWiki context.
     * @return the mapping between XWiki users and LDAP users. The key is the XWiki group, and the value is the list of
     *         mapped LDAP groups.
     * @since 1.1
     */
    public Map<String, Set<String>> getGroupMappings(Map<String, String> remoteUserLDAPConfiguration, XWikiContext context)
    {
        String param = remoteUserLDAPConfiguration.get("ldap_group_mapping");

        if (param == null) {
            return XWikiLDAPConfig.getInstance().getGroupMappings(context);
        }

        Map<String, Set<String>> groupMappings = new HashMap<String, Set<String>>();

        if (param.trim().length() > 0) {
            char[] buffer = param.trim().toCharArray();
            boolean escaped = false;
            StringBuilder mapping = new StringBuilder(param.length());
            for (int i = 0; i < buffer.length; ++i) {
                char c = buffer[i];

                if (escaped) {
                    mapping.append(c);
                    escaped = false;
                } else {
                    if (c == '\\') {
                        escaped = true;
                    } else if (c == '|') {
                        addGroupMapping(mapping.toString(), groupMappings);
                        mapping.setLength(0);
                    } else {
                        mapping.append(c);
                    }
                }
            }

            if (mapping.length() > 0) {
                addGroupMapping(mapping.toString(), groupMappings);
            }
        }

        return groupMappings;
    }

    /**
     * @param mapping the mapping to parse
     * @param groupMappings the map to add parsed group mapping to
     */
    private void addGroupMapping(String mapping, Map<String, Set<String>> groupMappings)
    {
        int splitIndex = mapping.indexOf('=');

        if (splitIndex < 1) {
            LOGGER.error("Error parsing ldap_group_mapping attribute [{}]", mapping);
        } else {
            String xwikigroup = mapping.substring(0, splitIndex);
            String ldapgroup = mapping.substring(splitIndex + 1);

            Set<String> ldapGroups = groupMappings.get(xwikigroup);

            if (ldapGroups == null) {
                ldapGroups = new HashSet<String>();
                groupMappings.put(xwikigroup, ldapGroups);
            }

            ldapGroups.add(ldapgroup);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Groupmapping found [{}] [{}]", xwikigroup, ldapGroups);
            }
        }
    }
}

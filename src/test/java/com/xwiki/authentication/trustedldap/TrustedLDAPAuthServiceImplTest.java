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

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Test;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.test.AbstractBridgedComponentTestCase;

public class TrustedLDAPAuthServiceImplTest extends AbstractBridgedComponentTestCase
{
    private Mockery mockery = new Mockery()
    {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private XWiki xwikiMock;

    private TrustedLDAPAuthServiceImpl authenticator;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        this.xwikiMock = this.mockery.mock(XWiki.class);

        getContext().setWiki(this.xwikiMock);
        getContext().setDatabase("xwiki");

        this.mockery.checking(new Expectations()
        {
            {
                allowing(xwikiMock).Param(with(not(equal("xwiki.authentication.encryptionKey"))));
                will(returnValue(null));
                allowing(xwikiMock).Param("xwiki.authentication.encryptionKey");
                will(returnValue("$é zefzekz fzeuhfkz ead;:azdazd\t"));
            }
        });

        this.authenticator = new TrustedLDAPAuthServiceImpl();
    }

    @After
    public void tearDown()
    {
        this.mockery.assertIsSatisfied();
    }

    @Test
    public void testParseRemoteUserWithNoConfiguration() throws Exception
    {
        this.mockery.checking(new Expectations()
        {
            {
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserParser", getContext());
                will(returnValue(null));
            }
        });

        Map<String, String> ldapConfiguration = this.authenticator.parseRemoteUser("remoteuser", getContext());

        Assert.assertEquals("remoteuser", ldapConfiguration.get("login"));
    }

    @Test
    public void testParseRemoteUserWithSimplePattern() throws Exception
    {
        this.mockery.checking(new Expectations()
        {
            {
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserParser", getContext());
                will(returnValue("remote"));
            }
        });

        Map<String, String> ldapConfiguration = this.authenticator.parseRemoteUser("remoteuser", getContext());

        Assert.assertEquals("remote", ldapConfiguration.get("login"));
    }

    @Test
    public void testParseRemoteUserWithGroupsPattern() throws Exception
    {
        this.mockery.checking(new Expectations()
        {
            {
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserParser", getContext());
                will(returnValue("(remote)(user)"));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.1", getContext());
                will(returnValue("login"));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.2", getContext());
                will(returnValue("ldap_server,ldap_port,ldap_base_DN,ldap_bind_DN,ldap_bind_pass,ldap_group_mapping"));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.login", getContext());
                will(returnValue(null));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_server", getContext());
                will(returnValue(null));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_port", getContext());
                will(returnValue(null));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_base_DN", getContext());
                will(returnValue(null));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_bind_DN", getContext());
                will(returnValue(null));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_bind_pass", getContext());
                will(returnValue(null));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_group_mapping", getContext());
                will(returnValue(null));
            }
        });

        Map<String, String> ldapConfiguration = this.authenticator.parseRemoteUser("remoteuser", getContext());

        Assert.assertEquals("remote", ldapConfiguration.get("login"));
        Assert.assertEquals("user", ldapConfiguration.get("ldap_server"));
        Assert.assertEquals("user", ldapConfiguration.get("ldap_base_DN"));
        Assert.assertEquals("user", ldapConfiguration.get("ldap_group_mapping"));
    }

    @Test
    public void testParseRemoteUserWithGroupsPatternandConversions() throws Exception
    {
        this.mockery.checking(new Expectations()
        {
            {
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserParser", getContext());
                will(returnValue("(.+)@(.+)"));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.1", getContext());
                will(returnValue("login"));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.2", getContext());
                will(returnValue("ldap_server,ldap_port,ldap_base_DN,ldap_bind_DN,ldap_bind_pass,ldap_group_mapping"));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.login", getContext());
                will(returnValue(null));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_server", getContext());
                will(returnValue("doMain=my.domain.com|domain2=my.domain2.com"));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_port", getContext());
                will(returnValue("doMain=388|domain2=387"));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_base_DN", getContext());
                will(returnValue("dOmain=dc=my,dc=domain,dc=com|domain2=dc=my,dc=domain2,dc=com"));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_bind_DN", getContext());
                will(returnValue("doMain=cn=bind,dc=my,dc=domain,dc=com|domain2=cn=bind,dc=my,dc=domain2,dc=com"));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_bind_pass", getContext());
                will(returnValue("doMain=password|domain2=password2"));
                allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_group_mapping", getContext());
                will(returnValue("doMain=xgroup11=lgroup11\\|xgroup12=lgroup12|domain2=xgroup21=lgroup21\\|xgroup22=lgroup22"));
            }
        });

        Map<String, String> ldapConfiguration = this.authenticator.parseRemoteUser("user@domain", getContext());

        Assert.assertEquals("user", ldapConfiguration.get("login"));
        Assert.assertEquals("my.domain.com", ldapConfiguration.get("ldap_server"));
        Assert.assertEquals("388", ldapConfiguration.get("ldap_port"));
        Assert.assertEquals("dc=my,dc=domain,dc=com", ldapConfiguration.get("ldap_base_DN"));
        Assert.assertEquals("cn=bind,dc=my,dc=domain,dc=com", ldapConfiguration.get("ldap_bind_DN"));
        Assert.assertEquals("password", ldapConfiguration.get("ldap_bind_pass"));
        Assert.assertEquals("xgroup11=lgroup11|xgroup12=lgroup12", ldapConfiguration.get("ldap_group_mapping"));

        ldapConfiguration = this.authenticator.parseRemoteUser("user@domain2", getContext());

        Assert.assertEquals("user", ldapConfiguration.get("login"));
        Assert.assertEquals("my.domain2.com", ldapConfiguration.get("ldap_server"));
        Assert.assertEquals("387", ldapConfiguration.get("ldap_port"));
        Assert.assertEquals("dc=my,dc=domain2,dc=com", ldapConfiguration.get("ldap_base_DN"));
        Assert.assertEquals("cn=bind,dc=my,dc=domain2,dc=com", ldapConfiguration.get("ldap_bind_DN"));
        Assert.assertEquals("password2", ldapConfiguration.get("ldap_bind_pass"));
        Assert.assertEquals("xgroup21=lgroup21|xgroup22=lgroup22", ldapConfiguration.get("ldap_group_mapping"));
    }

    @Test
    public void testEncryptDecrupt() throws Exception
    {
        String text = "some text $ with various é stuff in it";
        assertEquals(text, TrustedLDAPAuthServiceImpl.decryptText(
            TrustedLDAPAuthServiceImpl.encryptText(text, getContext()), getContext()));
    }
}

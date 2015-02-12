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
package com.xwiki.authentication;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xpn.xwiki.XWikiContext;

/**
 * Get authenticator configuration.
 * 
 * @version $Id$
 */
public class Config
{
    /**
     * LogFactory <code>LOGGER</code>.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    private final String prefPrefix;

    private final String confPrefix;

    public Config(String prefPrefix, String confPrefix)
    {
        this.prefPrefix = prefPrefix;
        this.confPrefix = confPrefix;
    }

    public String getParam(String name, XWikiContext context)
    {
        return getParam(name, "", context);
    }

    public String getParam(String name, String def, XWikiContext context)
    {
        String param = null;

        try {
            param = context.getWiki().getXWikiPreference(prefPrefix + "_" + name, context);
        } catch (Exception e) {
            LOGGER.error("Faile to get preference [{}]", this.prefPrefix + "_" + name, e);
        }

        if (StringUtils.isEmpty(param)) {
            try {
                param = context.getWiki().Param(confPrefix + "." + name);
            } catch (Exception e) {
                // ignore
            }
        }

        if (param == null) {
            param = def;
        }

        LOGGER.debug("Param [{}]: {}", name, param);

        return param;
    }

    public List<String> getListParam(String name, char separator, List<String> def, XWikiContext context)
    {
        List<String> list = def;

        String str = getParam(name, null, context);

        if (str != null) {
            if (!StringUtils.isEmpty(str)) {
                list = Arrays.asList(StringUtils.split(str, separator));
            } else {
                list = Collections.emptyList();
            }
        }

        return list;
    }

    public Set<String> getSetParam(String name, char separator, Set<String> def, XWikiContext context)
    {
        Set<String> set = def;

        String str = getParam(name, null, context);

        if (str != null) {
            if (!StringUtils.isEmpty(str)) {
                set = new HashSet<String>(Arrays.asList(StringUtils.split(str, separator)));
            } else {
                set = Collections.emptySet();
            }
        }

        return set;
    }

    public Map<String, String> getMapParam(String name, char separator, Map<String, String> def,
        boolean forceLowerCaseKey, XWikiContext context)
    {
        Map<String, String> mappings = def;

        List<String> list = getListParam(name, separator, null, context);

        if (list != null) {
            if (list.isEmpty()) {
                mappings = Collections.emptyMap();
            } else {
                mappings = new LinkedHashMap<String, String>();

                for (String fieldStr : list) {
                    int index = fieldStr.indexOf('=');
                    if (index != -1) {
                        String key = fieldStr.substring(0, index);
                        String value = index + 1 == fieldStr.length() ? "" : fieldStr.substring(index + 1);

                        mappings.put(forceLowerCaseKey ? key.toLowerCase() : key, value);
                    } else {
                        LOGGER.warn("Error parsing [{}] attribute in xwiki.cfg: {}", name, fieldStr);
                    }
                }
            }
        }

        return mappings;
    }

    public Map<String, Collection<String>> getOneToManyParam(String name, char separator,
        Map<String, Collection<String>> def, boolean left, XWikiContext context)
    {
        Map<String, Collection<String>> oneToMany = def;

        List<String> list = getListParam(name, separator, null, context);

        if (list != null) {
            if (list.isEmpty()) {
                oneToMany = Collections.emptyMap();
            } else {
                oneToMany = new LinkedHashMap<String, Collection<String>>();

                for (String mapping : list) {
                    int splitIndex = mapping.indexOf('=');

                    if (splitIndex < 1) {
                        LOGGER.error("Error parsing [{}] attribute: {}", name, mapping);
                    } else {
                        String leftProperty =
                            left ? mapping.substring(0, splitIndex) : mapping.substring(splitIndex + 1);
                        String rightProperty =
                            left ? mapping.substring(splitIndex + 1) : mapping.substring(0, splitIndex);

                        Collection<String> rightCollection = oneToMany.get(leftProperty);

                        if (rightCollection == null) {
                            rightCollection = new HashSet<String>();
                            oneToMany.put(leftProperty, rightCollection);
                        }

                        rightCollection.add(rightProperty);

                        LOGGER.debug("[{}] mapping found: {}", name, leftProperty + " " + rightCollection);
                    }
                }
            }
        }

        return oneToMany;
    }
}

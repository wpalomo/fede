/*
 * Copyright 2013 jlgranda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jlgranda.fede.factory;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.naming.NamingException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DatabaseConfiguration;
import org.jlgranda.fede.cdi.Config;
import org.jpapi.util.Database;

/**
 *
 * @author jlgranda
 */
public class ConfigurationFactory {

    private volatile static Configuration configuration;

    private synchronized static Configuration getConfiguration() {

        if (configuration == null) {
            try {
                configuration = new DatabaseConfiguration(Database.getDataSource(), "Setting", "name", "value");
            } catch (NamingException ex) {
                Logger.getLogger(ConfigurationFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(ConfigurationFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return configuration;
    }

    private String getConfigKey(InjectionPoint p) {
        String configKey = p.getMember().getDeclaringClass().getName() + "." + p.getMember().getName();
        if (!getConfiguration().containsKey(configKey)) {
            configKey = p.getMember().getDeclaringClass().getSimpleName() + "." + p.getMember().getName();
            if (!getConfiguration().containsKey(configKey)) {
                configKey = p.getMember().getName();
            }
        }
        System.err.println("Config key= " + configKey + " value = " + getConfiguration().containsKey(configKey));
        return configKey;
    }

    public @Produces
    @Config
    String getConfiguration(InjectionPoint p) {

        return getConfiguration().getString(getConfigKey(p));
    }

    public @Produces
    @Config
    Double getConfigurationDouble(InjectionPoint p) {

        return getConfiguration().getDouble(getConfigKey(p));

    }

    public @Produces
    @Config
    Boolean getConfigurationBoolean(InjectionPoint p) {

        return getConfiguration().getBoolean(getConfigKey(p));

    }
}

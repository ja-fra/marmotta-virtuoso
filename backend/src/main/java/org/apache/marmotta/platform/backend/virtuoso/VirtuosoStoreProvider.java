/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.backend.virtuoso;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.triplestore.SesameService;
import org.apache.marmotta.platform.core.api.triplestore.StoreProvider;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.Sail;
import org.slf4j.Logger;

import virtuoso.sesame2.driver.VirtuosoRepository;

/**
 * A store provider implementation for Apache Marmotta providing instances of Virtuoso stores.
 * This provider uses the Virtuoso Sesame Provider  developed by OpenLink.
 *
 * @see <a href="http://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/VirtSesame2Provider">http://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/VirtSesame2Provider</a>
 *
 * NOTE: the Virtuoso Sesame Provider is published under GPL license, and therefore requires
 * special consideration when distributing packages.
 *
 * @author Sergio Fernández
 */
@ApplicationScoped
public class VirtuosoStoreProvider implements StoreProvider {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private SesameService sesameService;

    /**
     * Create the store provided by this SailProvider
     *
     * @return a new instance of the store
     */
    @Override
    public NotifyingSail createStore() {
        final String host = configurationService.getStringConfiguration("virtuoso.host", "localhost");
        final int port = configurationService.getIntConfiguration("virtuoso.port", 1111);
        final String user = configurationService.getStringConfiguration("virtuoso.user", "dba");
        final String pass = configurationService.getStringConfiguration("virtuoso.pass", "dba");
        final String connString = "jdbc:virtuoso://"+host+":"+port;
        
        log.info("Initializing Backend: Virtuoso Store, over " + connString + "...");
        VirtuosoRepository repository = new VirtuosoRepository(connString, user, pass);
        return new NotifyingSailWrapper(repository);
    }

    public void configurationChanged(@Observes ConfigurationChangedEvent e) {
        if(e.containsChangedKeyWithPrefix("virtuoso")) {
            sesameService.restart();
        }
    }

    /**
     * Create the repository using the sail given as argument.
     * This method is needed because some backends
     * use custom implementations of SailRepository.
     *
     * @param sail
     * @return
     */
    @Override
    public SailRepository createRepository(Sail sail) {
        return new SailRepository(sail);
    }

    /**
     * Return the name of the provider. Used e.g. for displaying status information or logging.
     *
     * @return
     */
    @Override
    public String getName() {
        return "Virtuoso Store";
    }

    /**
     * Return true if this sail provider is enabled in the configuration.
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

}

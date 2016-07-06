/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shiro.test;

import com.gargoylesoftware.htmlunit.WebClient;
import static org.junit.Assert.assertTrue;

import org.eclipse.jetty.server.Connector;
import org.junit.Before;
import org.junit.BeforeClass;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.weld.environment.servlet.Listener;

import java.net.BindException;

public abstract class AbstractContainerTest {
    public static final int MAX_PORT = 9200;

    protected static PauseableServer server;

    private static int port = 9180;

    protected final WebClient webClient = new WebClient();

    @BeforeClass
    public static void startContainer() throws Exception {
        while (server == null && port < MAX_PORT) {
            try {
                server = createAndStartServer(port);
            } catch (BindException e) {
                System.err.printf("Unable to listen on port %d.  Trying next port.", port);
                port++;
            }
        }
        assertTrue(server.isStarted());
    }

    private static PauseableServer createAndStartServer(final int port) throws Exception {
        PauseableServer server = new PauseableServer();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        WebAppContext context = new WebAppContext();
//        String[] jettyConfigurationClasses = {"org.eclipse.jetty.webapp.WebXmlConfiguration", };
//        context.setConfigurationClasses(jettyConfigurationClasses);
        context.setContextPath("/");
        context.setResourceBase("src/main/webapp");
//        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        server.setHandler(context);

        context.addEventListener(new Listener());

        server.start();
        server.join();
        return server;
    }

    protected static String getBaseUri() {
        return "http://localhost:" + port + "/";
    }

    @Before
    public void beforeTest() {
        webClient.setThrowExceptionOnFailingStatusCode(true);
    }

    public void pauseServer(boolean paused) {
        if (server != null) server.pause(paused);
    }

    public static class PauseableServer extends Server {
        public synchronized void pause(boolean paused) {
            try {
                if (paused) for (Connector connector : getConnectors())
                    connector.stop();
                else for (Connector connector : getConnectors())
                    connector.start();
            } catch (Exception e) {
            }
        }
    }
}

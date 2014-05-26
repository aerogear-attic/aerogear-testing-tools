package org.jboss.aerogear.test.container.manager;

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;

import java.io.IOException;
import java.net.URI;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.jboss.as.controller.ControlledProcessState;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;

/**
 * A helper class to join management related operations
 *
 * @author <a href="aslak@redhat.com">Aslak Knutsen</a>
 */
public class ManagementClient {

    private static final String NAME = "name";

    private final String mgmtAddress;
    private final int mgmtPort;
    private final ModelControllerClient client;

    private URI webUri;
    private URI ejbUri;

    // cache static RootNode
    private ModelNode rootNode = null;

    private JMXConnector connector;

    public ManagementClient(ModelControllerClient client, final String mgmtAddress, final int managementPort) {
        if (client == null) {
            throw new IllegalArgumentException("Client must be specified");
        }
        this.client = client;
        this.mgmtAddress = mgmtAddress;
        this.mgmtPort = managementPort;
    }

    // -------------------------------------------------------------------------------------||
    // Public API -------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    public ModelControllerClient getControllerClient() {
        return client;
    }

    /**
     * @return The base URI or the web susbsystem. Usually http://localhost:8080
     */
    public URI getWebUri() {
        if (webUri == null) {
            try {
                if (rootNode == null) {
                    readRootNode();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String socketBinding = rootNode.get("subsystem").get("web").get("connector").get("http").get("socket-binding").asString();
            webUri = getBinding("http", socketBinding);
        }
        return webUri;
    }

    public boolean isServerInRunningState() {
        try {
            ModelNode op = Util.getEmptyOperation(READ_ATTRIBUTE_OPERATION, PathAddress.EMPTY_ADDRESS.toModelNode());
            op.get(NAME).set("server-state");

            ModelNode rsp = client.execute(op);
            return SUCCESS.equals(rsp.get(OUTCOME).asString())
                && !ControlledProcessState.State.STARTING.toString().equals(rsp.get(RESULT).asString())
                && !ControlledProcessState.State.STOPPING.toString().equals(rsp.get(RESULT).asString());
        } catch (Exception ignored) {
            return false;
        }
    }

    public void close() {
        try {
            getControllerClient().close();
        } catch (IOException e) {
            throw new RuntimeException("Could not close connection", e);
        } finally {
            if (connector != null) {
                try {
                    connector.close();
                } catch (IOException e) {
                    throw new RuntimeException("Could not close JMX connection", e);
                }
            }
        }
    }

    // -------------------------------------------------------------------------------------||
    // Subsystem URI Lookup ---------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    private void readRootNode() throws Exception {
        rootNode = readResource(new ModelNode());
    }

    private static ModelNode defined(final ModelNode node, final String message) {
        if (!node.isDefined())
            throw new IllegalStateException(message);
        return node;
    }

    private URI getBinding(final String protocol, final String socketBinding) {
        try {
            // TODO: resolve socket binding group correctly
            final String socketBindingGroupName = rootNode.get("socket-binding-group").keys().iterator().next();

            final ModelNode operation = new ModelNode();
            operation.get(OP_ADDR).get("socket-binding-group").set(socketBindingGroupName);
            operation.get(OP_ADDR).get("socket-binding").set(socketBinding);
            operation.get(OP).set(READ_ATTRIBUTE_OPERATION);
            operation.get(NAME).set("bound-address");
            String ip = executeForResult(operation).asString();
            // it appears some system can return a binding with the zone specifier on the end
            if (ip.contains(":") && ip.contains("%")) {
                ip = ip.split("%")[0];
            }

            final ModelNode portOp = new ModelNode();
            portOp.get(OP_ADDR).get("socket-binding-group").set(socketBindingGroupName);
            portOp.get(OP_ADDR).get("socket-binding").set(socketBinding);
            portOp.get(OP).set(READ_ATTRIBUTE_OPERATION);
            portOp.get(NAME).set("bound-port");
            final int port = defined(executeForResult(portOp), socketBindingGroupName + " -> " + socketBinding + " -> bound-port is undefined").asInt();

            return URI.create(protocol + "://" + NetworkUtils.formatPossibleIpv6Address(ip) + ":" + port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------------------------------------------------||
    // Common Management API Operations ---------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    private ModelNode readResource(ModelNode address) throws Exception {
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_RESOURCE_OPERATION);
        operation.get(RECURSIVE).set("true");
        operation.get(OP_ADDR).set(address);

        return executeForResult(operation);
    }

    private ModelNode executeForResult(final ModelNode operation) throws Exception {
        final ModelNode result = client.execute(operation);
        checkSuccessful(result, operation);
        return result.get(RESULT);
    }

    private void checkSuccessful(final ModelNode result,
        final ModelNode operation) throws UnSuccessfulOperationException {
        if (!SUCCESS.equals(result.get(OUTCOME).asString())) {
            throw new UnSuccessfulOperationException(result.get(
                FAILURE_DESCRIPTION).toString());
        }
    }

    private static class UnSuccessfulOperationException extends Exception {
        private static final long serialVersionUID = 1L;

        public UnSuccessfulOperationException(String message) {
            super(message);
        }
    }

    public JMXServiceURL getRemoteJMXURL() {
        try {
            return new JMXServiceURL("service:jmx:remoting-jmx://" + NetworkUtils.formatPossibleIpv6Address(mgmtAddress) + ":" + mgmtPort);
        } catch (Exception e) {
            throw new RuntimeException("Could not create JMXServiceURL:" + this, e);
        }
    }

    public int getMgmtPort() {
        return mgmtPort;
    }

    public String getMgmtAddress() {
        return NetworkUtils.formatPossibleIpv6Address(mgmtAddress);
    }

    public URI getRemoteEjbURL() {
        if (ejbUri == null) {
            if (rootNode == null) {
                try {
                    readRootNode();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            String socketBinding = rootNode.get("subsystem").get("remoting").get("connector").get("remoting-connector").get("socket-binding").asString();
            ejbUri = getBinding("remote", socketBinding);
        }
        return ejbUri;
    }

    /**
     * Utility methods related to networking.
     *
     * @author Brian Stansberry (c) 2011 Red Hat Inc.
     */
    private static class NetworkUtils {

        public static String formatPossibleIpv6Address(String address) {
            if (address == null) {
                return address;
            }
            if (!address.contains(":")) {
                return address;
            }
            if (address.startsWith("[") && address.endsWith("]")) {
                return address;
            }
            return "[" + address + "]";
        }

        // No instantiation
        private NetworkUtils() {

        }
    }
}
/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.aerogear.unifiedpush.utils.perf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jboss.aerogear.test.UnifiedPushServer;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class BatchUnifiedPushServer extends UnifiedPushServer {

    public BatchUnifiedPushServer(String unifiedPushServerUrl, String authServerUrl) throws MalformedURLException {
        super(unifiedPushServerUrl, authServerUrl);
    }

    public BatchUnifiedPushServer(URL unifiedPushServerUrl, URL authServerUrl) {
        super(unifiedPushServerUrl, authServerUrl);
    }

    public void registerInstallationsViaEndpoint(List<Installation> installations, Variant variant) {
        BatchInstallationUtils.registerViaBatchEndpoint(variant, installations, session);
    }

}

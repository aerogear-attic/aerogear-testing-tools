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
package org.jboss.aerogear.test.arquillian.container.check;

import java.net.URI;

/**
 * Performs check on arbitrary {@link URI} and decides if it is prepared or not for further interaction.
 * 
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * 
 */
public interface StatusCheck {

    /**
     * 
     * @param uri address of target upon status check
     * @throws IllegalArgumentException if {@code uri} is a null object.
     */
    void target(URI uri);

    /**
     * Performs status check
     * 
     * @return true if the status of the target is considered to be ok, false otherwise
     * @throws StatusCheckException if {@link #target(URI)} method was not set prior to this method.
     */
    boolean execute() throws StatusCheckException;
}

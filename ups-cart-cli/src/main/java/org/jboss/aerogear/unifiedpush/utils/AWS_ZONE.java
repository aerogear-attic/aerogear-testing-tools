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
package org.jboss.aerogear.unifiedpush.utils;

/**
 * Taken from {@code rhc region list}.
 * 
 * https://developers.openshift.com/en/overview-platform-features.html#regions-and-zones
 * 
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 * @see GEAR_SIZE
 */
public enum AWS_ZONE {

    AWS_EU_WEST_1A("aws-eu-west-1a", AWS_REGION.AWS_EU_WEST_1), // small gear sizes can not be deployed to this zone
    AWS_EU_WEST_1B("aws-eu-west-1b", AWS_REGION.AWS_EU_WEST_1), // small gear sizes can not be deployed to this zone
    AWS_EU_WEST_1C("aws-eu-west-1c", AWS_REGION.AWS_EU_WEST_1), // small gear sizes can not be deployed to this zone
    AWS_US_EAST_1A("aws-us-east-1a", AWS_REGION.AWS_US_EAST_1),
    AWS_US_EAST_1B("aws-us-east-1b", AWS_REGION.AWS_US_EAST_1),
    AWS_US_EAST_1C("aws-us-east-1c", AWS_REGION.AWS_US_EAST_1),
    AWS_US_EAST_1E("aws-us-east-1e", AWS_REGION.AWS_US_EAST_1);

    private final String name;

    private final AWS_REGION region;

    private AWS_ZONE(String name, AWS_REGION region) {
        this.name = name;
        this.region = region;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getZone() {
        return toString();
    }

    public String getRegion() {
        return region.toString();
    }

}

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
package org.jboss.aerogear.unifiedpush.utils.installation.generation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.ParetoDistribution;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.utils.installation.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.perf.model.MassInstallation;

/**
 * Generates MassInstallation according to Pareto distribution - 80:20 rule (20% of variants will contain 80% of installations).
 *
 * @see <a href="https://en.wikipedia.org/wiki/Pareto_distribution">Pareto distribution</a>
 *
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class ParetoInstallationDistribution implements InstallationDistributionStrategy {

    private static final double PARETO_SCALE_PARAMETER = 1;

    private static final double PARETO_SHAPE_PARAMETER = 1.1609640; // 80:20 ratio

    @Override
    public MassInstallation generate(List<PushApplication> registeredApplications, int installationsCount) {

        ParetoDistribution paretoDistribution = new ParetoDistribution(PARETO_SCALE_PARAMETER, PARETO_SHAPE_PARAMETER);

        List<Variant> variants = new ArrayList<Variant>();

        for (PushApplication pushApplication : registeredApplications) {
            variants.addAll(pushApplication.getVariants());
        }

        double[] distributionSample = paretoDistribution.sample(variants.size());
        
        double installationSum = sum(distributionSample);

        double[] installationsPerVariantFraction = new double[distributionSample.length];

        for (int i = 0; i < installationsPerVariantFraction.length; i++) {
            installationsPerVariantFraction[i] = distributionSample[i] / installationSum;
        }

        double[] installationsPerVariant = new double[distributionSample.length];

        for (int i = 0; i < installationsPerVariant.length; i++) {
            installationsPerVariant[i] = installationsPerVariantFraction[i] * installationsCount;
        }

        int[] installations = roundInstallationsCount(installationsPerVariant);

        MassInstallation massInstallation = new MassInstallation();

        for (int i = 0; i < variants.size(); i++) {
            Variant variant = variants.get(i);
            massInstallation.map.put(variant.getVariantID(), InstallationUtils.generate(variant.getType(), installations[i]));
        }

        return massInstallation;
    }

    private int[] roundInstallationsCount(double[] installationsPerVariant) {
        int[] installations = new int[installationsPerVariant.length];

        for (int i = 0; i < installationsPerVariant.length; i++) {
            if (installationsPerVariant[i] < 1 && installationsPerVariant[i] > 0.5) {
                installations[i] = 1;
            } else {
                installations[i] = (int) Math.round(installationsPerVariant[i]);
            }
        }

        return installations;
    }

    private double sum(double[] array) {

        double total = 0.0;

        for (int i = 0; i < array.length; i++) {
            total += array[i];
        }

        return total;
    }
}

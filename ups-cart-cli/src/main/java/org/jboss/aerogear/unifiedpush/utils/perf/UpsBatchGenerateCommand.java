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

import io.airlift.command.Command;
import io.airlift.command.Option;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.aerogear.test.cli.UnifiedPushServerCommand;
import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.utils.application.PushApplicationUtils;
import org.jboss.aerogear.unifiedpush.utils.installation.InstallationUtils;
import org.jboss.aerogear.unifiedpush.utils.installation.generation.FlatInstallationDistribution;
import org.jboss.aerogear.unifiedpush.utils.installation.generation.ParetoInstallationDistribution;
import org.jboss.aerogear.unifiedpush.utils.installation.generation.RandomCategoryGenerator;
import org.jboss.aerogear.unifiedpush.utils.perf.model.MassInstallation;
import org.jboss.aerogear.unifiedpush.utils.perf.model.MassPushApplication;
import org.jboss.aerogear.unifiedpush.utils.variant.VariantMetadata;
import org.jboss.aerogear.unifiedpush.utils.variant.generation.EqualVariantGeneration;
import org.jboss.aerogear.unifiedpush.utils.variant.generation.RandomVariantGeneration;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@Command(name = "ups-generate-batch", description = "Generates arbitrary number of installations via custom batching endpoint")
public class UpsBatchGenerateCommand extends UnifiedPushServerCommand {

    private static final Logger log = Logger.getLogger(UpsBatchGenerateCommand.class.getName());

    @Option(
        name = { "--categories" },
        title = "categories",
        description = "number of categories.")
    public String categories = "0";

    @Option(
        name = { "--categories-per-installation" },
        title = "categories-per-installations",
        description = "number of categories an installation will be a member of")
    public String categoriesPerInstallation = "0";

    @Option(
        name = { "--applications" },
        title = "applications",
        description = "number of applications to create")
    public String applications;

    @Option(
        name = { "--variants" },
        title = "variants",
        description = "number of variants to create for every application")
    public String variants;

    @Option(
        name = { "--installations" },
        title = "installations",
        description = "number of installations to create")
    public String installations;

    @Option(
        name = "--variant-distribution",
        title = "variant-distribution",
        description = "how to create types of variants, possible values - 'equal', 'random', defaults to 'equal'.",
        allowedValues = { "equal", "random" })
    public String variantDistribution = "equal";

    @Option(
        name = "--variant-type",
        title = "variant-type",
        description = "Type of variant which will be created, possible values - 'android', 'ios', 'simplepush', defaults to 'android'",
        allowedValues = { "android", "ios", "simplepush" })
    public String variantType = "android";

    @Option(
        name = "--installation-distribution",
        title = "installation-distribution",
        description = "which distribution function to use for installation assignement for variants, possible values: "
            + "'pareto', 'flat'. Defauts to 'flat'.",
        allowedValues = { "pareto", "flat" })
    public String installationDistribution = "flat";

    // metadata

    @Option(
        name = { "--google-key" },
        title = "google-key",
        description = "Google API key for Android application variant. "
            + "If set, --project-no is required and Android variant is created.")
    public String googleKey;

    @Option(
        name = { "--project-no" },
        title = "project-number",
        description = "Google Project Number for Android application variant. "
            + "If present, Android variant is created.")
    public String projectNumber;

    @Option(
        name = { "--cert-path" },
        title = "certificate-path",
        description = "Path to iOS certificate. "
            + "If set, --cert-pass is required and iOS variant is created.")
    public String certificatePath;

    @Option(
        name = { "--cert-pass" },
        title = "certificate-passphrase",
        description = "Certificate passphrase")
    public String certificatePass;

    @Option(
        name = { "--production" },
        arity = 0,
        title = "production",
        description = "If set, certificate is marked as production one")
    public boolean production;

    private BatchUnifiedPushServer server;

    private int[] registrationCounts = new int[5];

    @Override
    public void run() {

        parseRegistrationCounts();

        if (variantDistribution.equals("random")) {
            validateAndroidVariantMetadata();
            validateiOSVariantMetadata();
        }

        if (variantDistribution.equals("equal")) {
            if (variantType.equals("android")) {
                validateAndroidVariantMetadata();
            } else if (variantType.equals("ios")) {
                validateiOSVariantMetadata();
            } else if (variantType.equals("simplepush")) {
                // nothing specific yet
            } else {
                throw new IllegalStateException("variant-type was not set to any of 'android', 'ios' nor 'simplepush'");
            }
        }

        validateCategories(registrationCounts[3], registrationCounts[4]);

        log.log(Level.INFO, "Connecting to UPS running at {0}", upsRootUrl());

        server = login(username, password);

        log.log(Level.INFO, "Logged as {0}", username);

        List<PushApplication> applications = PushApplicationUtils.generate(registrationCounts[0]);

        VariantMetadata variantMetadata = getVariantMetadata();

        log.log(Level.INFO, "Setting variants.");

        for (PushApplication pushApp : applications) {
            pushApp.setVariants(generateVariants(variantMetadata, variantDistribution, registrationCounts[1]));
        }

        log.log(Level.INFO, "Registering applications.");

        MassPushApplication registeredApplications = server.registerApplicationsViaEndPoint(applications);

        // applications with variants are created on UPS side with application and variant IDs set

        log.log(Level.INFO, "Generating installations.");

        MassInstallation installations = generateInstallations(registeredApplications.getApplications(), installationDistribution, registrationCounts[2]);

        // do we want to deal with categories?
        if (registrationCounts[3] > 0 && registrationCounts[4] > 0) {

            log.log(Level.INFO, "Putting installations to categories.");

            // put these installations to categories
            List<Category> categories = new RandomCategoryGenerator().generateCategories(registrationCounts[3]);

            for (Map.Entry<String, List<Installation>> entry : installations.map.entrySet()) {
                InstallationUtils.assignInstallationsToCategories(categories, entry.getValue(), registrationCounts[4]);
            }
        }

        log.log(Level.INFO, "Registering installations.");

        server.registerInstallationsViaEndPoint(installations);
    }

    @Override
    public BatchUnifiedPushServer login(String username, String password) {

        BatchUnifiedPushServer server = null;

        try {
            server = new BatchUnifiedPushServer(unifiedPushServerUrl(), authServerUrl());
            server.login(username, password);

            return server;
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Server URL was malformed: " + ex.getMessage());
        }

    }

    // helpers

    private VariantMetadata getVariantMetadata() {
        VariantMetadata variantMetadata = new VariantMetadata();

        variantMetadata.setGoogleKey(googleKey);
        variantMetadata.setProjectNumber(projectNumber);
        variantMetadata.setCertificatePath(certificatePath);
        variantMetadata.setCertificatePass(certificatePass);
        variantMetadata.setProduction(production);

        return variantMetadata;
    }

    private void parseRegistrationCounts() {
        registrationCounts[0] = parseNumber(applications);
        registrationCounts[1] = parseNumber(variants);
        registrationCounts[2] = parseNumber(installations);
        registrationCounts[3] = parseNumber(categories);
        registrationCounts[4] = parseNumber(categoriesPerInstallation);
    }

    private MassInstallation generateInstallations(List<PushApplication> registeredApplications, String installationDistribution, int count) {

        if (installationDistribution.equals("pareto")) {
            return new ParetoInstallationDistribution().generate(registeredApplications, count);
        } else if (installationDistribution.equals("flat")) {
            return new FlatInstallationDistribution().generate(registeredApplications, count);
        }

        throw new IllegalStateException("Installation distribution is not 'pareto' nor 'flat': " + installationDistribution);
    }

    private List<Variant> generateVariants(VariantMetadata variantMetadata, String variantDistribution, int count) {

        if (variantDistribution.equals("random")) {
            return new RandomVariantGeneration().generate(count, variantMetadata);
        } else if (variantDistribution.equals("equal")) {

            VariantType type = getVariantType(variantType);

            return new EqualVariantGeneration().generate(count, variantMetadata, type);
        }

        throw new IllegalArgumentException("variant distribution is not 'random' nor 'equal': " + variantDistribution);
    }

    private void validateiOSVariantMetadata() {
        if ((certificatePath != null && certificatePass == null) || (certificatePath == null || certificatePass != null)) {
            throw new IllegalStateException("You have not set one of 'certificatePath' or 'certificatePass' for iOS variant. "
                + "Both have to be specified or none.");
        }
    }

    private void validateAndroidVariantMetadata() {
        if ((googleKey != null && projectNumber == null) || (googleKey == null && projectNumber != null)) {
            throw new IllegalStateException("You have not set one of 'googleKey' or 'projectNumber' for Android variant. Both "
                + "have to be specified or none.");
        }
    }

    private void validateCategories(int categories, int categoriesPerInstallation) {
        if (categories < categoriesPerInstallation) {
            throw new IllegalStateException(String.format("You want to assign an installation to %s categories but "
                + "there is only %s categories available to choose from.", categoriesPerInstallation, categories));
        }
    }

    private VariantType getVariantType(String variantType) {

        VariantType type = null;

        if (variantType.equals("android")) {
            type = VariantType.ANDROID;
        } else if (variantType.equals("ios")) {
            type = VariantType.IOS;
        } else if (variantType.equals("simplepush")) {
            type = VariantType.SIMPLE_PUSH;
        } else {
            throw new IllegalStateException("variant-type was not set to any of 'android', 'ios' nor 'simplepush'");
        }

        return type;
    }
}

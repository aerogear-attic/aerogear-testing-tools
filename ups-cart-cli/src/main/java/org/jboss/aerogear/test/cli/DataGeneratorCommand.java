package org.jboss.aerogear.test.cli;

import io.airlift.command.Command;
import io.airlift.command.Option;

import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.jboss.aerogear.test.ContentTypes;
import org.jboss.aerogear.test.Headers;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.test.DataGeneratorConfig;
import org.jboss.aerogear.unifiedpush.test.DataGeneratorConfig.InstallationDistribution;
import org.jboss.aerogear.unifiedpush.test.DataGeneratorConfig.VariantDistribution;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

@Command(name = "generate-data", 
         description = "Generates testing data for an UPS instance with usage of DataGeneratorEndpoint from unifiedpush-test-extension-server.war")
public class DataGeneratorCommand extends OpenShiftCommand {

    @Option(name = "--applications", 
            title = "applications", 
            description = "Number of applications to generate.")
    private Integer applicationsCount;

    @Option(name = "--variants", 
            title = "variants", 
            description = "Number of variants to generate for every application.")
    private Integer variantsCount;

    @Option(name = "--installations", 
            title = "installations", 
            description = "Number of installations to generate.")
    private Integer installationsCount;

    @Option(name = "--categories", 
            title = "categories", 
            description = "Number of categories to generate.")
    private Integer categoriesCount;

    @Option(name = "--categories-per-installation", 
            title = "categories-per-installations", 
            description = "Number of categories an installation will be a member of.")
    private Integer categoriesPerInstallation;

    @Option(name = "--variant-type", 
            title = "variant-type", 
            description = "Type of variant which will be created, possible values: 'ANDROID', 'IOS', 'SIMPLE_PUSH', 'CHROME_PACKAGED_APP'.")
    private VariantType variantType;

    @Option(name = "--variant-distribution", 
            title = "variant-distribution", 
            description = "How to create types of variants, possible values - 'EQUAL', 'RANDOM'.")
    private VariantDistribution variantDistribution;

    @Option(name = "--installation-distribution", 
            title = "installation-distribution", 
            description = "Which distribution function to use for installation assignement for variants, possible values: 'pareto', 'flat'. Defauts to 'flat'.", allowedValues = { "PARETO", "FLAT" })    
    private InstallationDistribution installationDistribution;

    @Option(name = "--developer", 
            title = "developer", 
            description = "The developer which created the app.")
    private String developer;

    @Option(name = "--google-key", 
            title = "google-key", 
            description = "Google API key for Android application variant. If set, --project-no is required and Android variant is created.")
    private String googleKey;

    @Option(name = "--project-number", 
            title = "project-number", 
            description = "Google Project Number for Android application variant. If present, Android variant is created.")
    private String projectNumber;

    @Option(name = "--cert-path", 
            title = "certificate-path", 
            description = "Path to iOS certificate. If set, --cert-pass is required and iOS variant is created.")    
    private String certificatePath;

    @Option(name = "--cert-pass", 
            title = "certificate-passphrase", 
            description = "Certificate passphrase.")
    private String certificatePass;

    @Option(name = "--cert-production", 
            title = "certificate-production", 
            description = "If set, certificate is marked as production one.")
    private Boolean certificateProduction;

    @Option(name = "--cleanup-database", 
            title = "cleanup-database", 
            description = "If set, all data will be deleted before generation.")
    private Boolean cleanupDatabase;

    public void run() {
        Response response = RestAssured.given().
                baseUri(getBaseUri()).
                body(getDataGeneratorConfig()).
                header(Headers.acceptJson()).
                contentType(ContentTypes.json()).
                post("/datagenerator");
        
        System.out.println(response.prettyPrint());        
    }
    
    private String getBaseUri() {
        return "https://"+appName+"-"+namespace+".rhcloud.com/unifiedpush-test-extension-server";
    }

    private DataGeneratorConfig getDataGeneratorConfig() {
        DataGeneratorConfig config = new DataGeneratorConfig();
        if( applicationsCount != null ) {
            config.setApplicationsCount(applicationsCount);
        }
        if( variantsCount != null ) {
            config.setVariantsCount(variantsCount);
        }
        if( installationsCount != null ) {
            config.setInstallationsCount(installationsCount);
        }
        if( categoriesCount != null ) {
            config.setCategoriesCount(categoriesCount);
        }
        if( categoriesPerInstallation != null ) {
            config.setCategoriesPerInstallation(categoriesPerInstallation);
        }
        if( variantType != null ) {
            config.setVariantType(variantType);
        }
        if( variantDistribution != null ) {
            config.setVariantDistribution(variantDistribution);
        }
        if( installationDistribution != null ) {
            config.setInstallationDistribution(installationDistribution);
        }
        if( developer != null ) {
            config.setDeveloper(developer);
        }
        if( googleKey != null ) {
            config.setGoogleKey(googleKey);
        }
        if( projectNumber != null ) {
            config.setProjectNumber(projectNumber);            
        }
        if( certificatePath != null ) {
            try(FileInputStream certificateInputStream = new FileInputStream(certificatePath)) {
                byte[] certificateBytes = IOUtils.toByteArray(certificateInputStream);
                config.setCertificateBytes(certificateBytes);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if( certificatePass != null ) {
            config.setCertificatePass(certificatePass);
        }
        if( certificateProduction != null ) {
            config.setCertificateProduction(certificateProduction);
        }
        if( cleanupDatabase != null ) {
            config.setCleanupDatabase(cleanupDatabase);
        }
        return config;
    }

}
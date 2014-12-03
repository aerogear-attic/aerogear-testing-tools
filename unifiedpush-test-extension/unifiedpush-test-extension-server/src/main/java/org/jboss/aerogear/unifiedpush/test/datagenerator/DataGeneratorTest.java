package org.jboss.aerogear.unifiedpush.test.datagenerator;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.test.datagenerator.DataGeneratorConfig.InstallationDistribution;
import org.jboss.aerogear.unifiedpush.test.datagenerator.DataGeneratorConfig.VariantDistribution;

// TODO remove
@Stateless
@Path("/generatortest")
public class DataGeneratorTest {

    @Inject
    private DataGeneratorEndpoint dataGeneratorEndpoint;

    @GET
    public String test() {
        DataGeneratorConfig config = new DataGeneratorConfig();
        config.setApplicationsCount(33);
        config.setVariantsCount(10);
        config.setVariantDistribution(VariantDistribution.RANDOM);
        config.setVariantType(VariantType.ANDROID);
        config.setInstallationDistribution(InstallationDistribution.FLAT);
        config.setInstallationsCount(20);
        config.setCategoriesCount(15);
        config.setCategoriesPerInstallation(5);
        config.setCertificatePass("123456");
        config.setCertificateBase64("certificate");
        config.setCleanupDatabase(true);

        dataGeneratorEndpoint.generate(config);

        return "OK";
    }

}
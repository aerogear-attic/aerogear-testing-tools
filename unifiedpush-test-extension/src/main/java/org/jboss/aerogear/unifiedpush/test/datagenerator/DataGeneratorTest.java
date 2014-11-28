package org.jboss.aerogear.unifiedpush.test.datagenerator;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.jboss.aerogear.unifiedpush.api.VariantType;
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
        config.setApplicationsCount(100);
        config.setVariantsCount(10);
        config.setVariantDistribution(VariantDistribution.EQUAL);
        config.setVariantType(VariantType.ANDROID);
        config.setInstallationsCount(20);
        // TODO categories

        dataGeneratorEndpoint.generate(config);

        return "OK";
    }

}
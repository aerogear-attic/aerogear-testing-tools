package org.jboss.aerogear.unifiedpush.test.datagenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.ChromePackagedAppVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;

@Stateless
@Path("/datagenerator")
public class DataGeneratorEndpoint {

    private static final Random RANDOM = new Random();

    @Resource(name = "java:jboss/datasources/UnifiedPushDS")
    private DataSource ds;
    
    @Inject
    private Validator validator;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generate(DataGeneratorConfig config) {
        validateConfig(config);

        DataGeneratorContext ctx = new DataGeneratorContext(config);

        generateApplications(ctx);
        generateVariants(ctx);
        generateInstallations(ctx);
        generateCategories(ctx);

        return Response.ok().build();
    }

    private void validateConfig(DataGeneratorConfig config) {
        Set<ConstraintViolation<DataGeneratorConfig>> violations = validator.validate(config);
        if( !violations.isEmpty() ) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }
    }

    private void generateApplications(DataGeneratorContext ctx) {
        try {
            Connection c = ds.getConnection();
            PreparedStatement ps = c.prepareStatement("insert into PushApplication(id, name, description, pushApplicationID, masterSecret, developer) values(?, ?, ?, ?, ?, ?)");

            for (int i = 0; i < ctx.getConfig().getApplicationsCount(); i++) {
                PushApplication application = new PushApplication();
                application.setName(application.getId());
                application.setDescription(application.getId());
                application.setPushApplicationID(application.getId());
                application.setMasterSecret(application.getId());
                application.setDeveloper(ctx.getConfig().getDeveloper());
                ctx.getApplications().add(application);
                prepareInsertApplicationStmt(ps, application);
            }

            ps.executeBatch();
            ps.close();
            c.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateVariants(DataGeneratorContext ctx) {
        try {
            Connection c = ds.getConnection();
            PreparedStatement insertVariant = c.prepareStatement("insert into Variant(id, name, description, developer, secret, variantid, variants_id, variant_type, type) values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            PreparedStatement insertAndroidVariant = c.prepareStatement("insert into AndroidVariant(id, projectnumber, googlekey) values(?, ?, ?)");
            PreparedStatement insertIosVariant = c.prepareStatement("insert into iOSVariant(id, certificate, passphrase, production) values(?, ?, ?, ?)");
            PreparedStatement insertSimplePushVariant = c.prepareStatement("insert into SimplePushVariant(id) values(?)");

            for (PushApplication application : ctx.getApplications()) {
                for (int i = 0; i < ctx.getConfig().getVariantsCount(); i++) {
                    VariantType variantType = createVariantType(ctx);
                    Variant variant = createVariant(variantType);
                    variant.setName(variant.getId());
                    variant.setDescription(variant.getId());
                    variant.setDeveloper(ctx.getConfig().getDeveloper());
                    variant.setSecret(variant.getId());
                    variant.setVariantID(variant.getId());
                    prepareInsertVariantStmt(insertVariant, application, variant);

                    switch (variantType) {
                    case ANDROID:
                        AndroidVariant androidVariant = (AndroidVariant) variant;
                        androidVariant.setProjectNumber(ctx.getConfig().getProjectNumber());
                        androidVariant.setGoogleKey(ctx.getConfig().getGoogleKey());
                        prepareInsertAndroidVariantStmt(insertAndroidVariant, androidVariant);
                        break;
                    case IOS:
                        iOSVariant iosVariant = (iOSVariant) variant;
                        iosVariant.setCertificate(null); // TODO ctx.getConfig().getCertificatePath()
                        iosVariant.setPassphrase(ctx.getConfig().getCertificatePass());
                        iosVariant.setProduction(ctx.getConfig().isCertificateProduction());
                        prepareInsertIosVariantStmt(insertIosVariant, iosVariant);
                        break;
                    case SIMPLE_PUSH:
                        prepareInsertSimplePushVariantStmt(insertSimplePushVariant, variant);
                        break;
                    default:
                        throw new RuntimeException("");
                    }

                    application.getVariants().add(variant);
                }
                insertVariant.executeBatch();
                insertAndroidVariant.executeBatch();
                insertIosVariant.executeBatch();
                insertSimplePushVariant.executeBatch();
            }
            insertVariant.close();
            insertAndroidVariant.close();
            insertIosVariant.close();
            insertSimplePushVariant.close();
            c.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateInstallations(DataGeneratorContext ctx) {
        Map<Variant, Integer> installationsCount = calculateInstallationDistribution(ctx);
        for (Map.Entry<Variant, Integer> installationCount : installationsCount.entrySet()) {

            Variant variant = installationCount.getKey();
            int count = installationCount.getValue().intValue();

            for (int i = 0; i < count; i++) {
                Installation installation = new Installation();
                installation.setAlias(installation.getId());
                installation.setVariant(variant);
                ctx.getInstallations().add(installation);

                switch (variant.getType()) {
                case ANDROID:
                    installation.setDeviceToken(RandomStringUtils.randomAlphanumeric(100));
                    installation.setDeviceType("AndroidPhone");
                    installation.setOperatingSystem("ANDROID");
                    installation.setOsVersion("4.2.2");
                    break;
                case IOS:
                    installation.setDeviceToken(installation.getId().replaceAll("-", ""));
                    installation.setDeviceType("IOSPhone");
                    installation.setOperatingSystem("IOS");
                    installation.setOsVersion("6.0");
                    break;
                case SIMPLE_PUSH:
                    installation.setDeviceToken(String.format("http://localhost:8081/endpoint/%s", installation.getId()));
                    installation.setDeviceType("web");
                    installation.setOperatingSystem("MozillaOS");
                    installation.setOsVersion("1");
                    break;
                default:
                    throw new RuntimeException();
                }
            }
        }

        try {
            Connection c = ds.getConnection();
            PreparedStatement ps = c.prepareStatement("insert into Installation(id, alias, deviceToken, deviceType, operatingSystem, osVersion, platform, variantid, enabled) values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            for (Installation installation : ctx.getInstallations()) {
                prepareInsertInstallationStmt(ps, installation);
            }
            ps.executeBatch();
            ps.close();
            c.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateCategories(DataGeneratorContext ctx) {
        /*
         * 
         * TODO
         * 
         * Table found: thradec.Category [id, name]
         * 
         * Table found: thradec.Installation_Category [installation_id,
         * categories_id]
         */
    }

    private Map<Variant, Integer> calculateInstallationDistribution(DataGeneratorContext ctx) {
        Map<Variant, Integer> installationsCount;
        switch (ctx.getConfig().getInstallationDistribution()) {
        case FLAT:
            installationsCount = calculateFlatInstallationDistribution(ctx);
            break;
        case PARETO:
            installationsCount = calculateParetoInstallationDistribution(ctx);
            break;
        default:
            throw new RuntimeException();
        }
        return installationsCount;
    }

    private Map<Variant, Integer> calculateFlatInstallationDistribution(DataGeneratorContext ctx) {
        Map<Variant, Integer> installationsCount = new LinkedHashMap<Variant, Integer>();
        for (PushApplication application : ctx.getApplications()) {
            for (Variant variant : application.getVariants()) {
                installationsCount.put(variant, ctx.getConfig().getInstallationsCount());
            }
        }
        return installationsCount;
    }

    private Map<Variant, Integer> calculateParetoInstallationDistribution(DataGeneratorContext ctx) {
        final double PARETO_SCALE_PARAMETER = 1;
        final double PARETO_SHAPE_PARAMETER = 1.1609640; // 80:20 ratio

        List<Variant> variants = new ArrayList<Variant>();
        for (PushApplication application : ctx.getApplications()) {
            variants.addAll(application.getVariants());
        }

        ParetoDistribution paretoDistribution = new ParetoDistribution(PARETO_SCALE_PARAMETER, PARETO_SHAPE_PARAMETER);
        double[] distributionSample = paretoDistribution.sample(variants.size());

        double installationSum = StatUtils.sum(distributionSample);

        double[] installationsPerVariantFraction = new double[distributionSample.length];
        for (int i = 0; i < installationsPerVariantFraction.length; i++) {
            installationsPerVariantFraction[i] = distributionSample[i] / installationSum;
        }

        double[] installationsPerVariant = new double[distributionSample.length];
        for (int i = 0; i < installationsPerVariant.length; i++) {
            installationsPerVariant[i] = installationsPerVariantFraction[i] * ctx.getConfig().getInstallationsCount();
        }

        int[] installations = new int[installationsPerVariant.length];
        for (int i = 0; i < installationsPerVariant.length; i++) {
            if (installationsPerVariant[i] < 1 && installationsPerVariant[i] > 0.5) {
                installations[i] = 1;
            } else {
                installations[i] = (int) Math.round(installationsPerVariant[i]);
            }
        }

        Map<Variant, Integer> installationsCount = new LinkedHashMap<Variant, Integer>();
        for (int i = 0; i < variants.size(); i++) {
            installationsCount.put(variants.get(i), installations[i]);
        }
        return installationsCount;
    }

    private void prepareInsertApplicationStmt(PreparedStatement ps, PushApplication application) throws SQLException {
        ps.clearParameters();
        ps.setString(1, application.getId());
        ps.setString(2, application.getName());
        ps.setString(3, application.getDescription());
        ps.setString(4, application.getPushApplicationID());
        ps.setString(5, application.getMasterSecret());
        ps.setString(6, application.getDeveloper());
        ps.addBatch();
    }

    private void prepareInsertVariantStmt(PreparedStatement insertVariant, PushApplication application, Variant variant) throws SQLException {
        insertVariant.clearParameters();
        insertVariant.setString(1, variant.getId());
        insertVariant.setString(2, variant.getName());
        insertVariant.setString(3, variant.getDescription());
        insertVariant.setString(4, variant.getDeveloper());
        insertVariant.setString(5, variant.getSecret());
        insertVariant.setString(6, variant.getVariantID());
        insertVariant.setString(7, application.getId());
        insertVariant.setString(8, variant.getType().getTypeName());
        insertVariant.setInt(9, variant.getType().ordinal());
        insertVariant.addBatch();
    }

    private void prepareInsertAndroidVariantStmt(PreparedStatement insertAndroidVariant, AndroidVariant androidVariant) throws SQLException {
        insertAndroidVariant.clearParameters();
        insertAndroidVariant.setString(1, androidVariant.getId());
        insertAndroidVariant.setString(2, androidVariant.getProjectNumber());
        insertAndroidVariant.setString(3, androidVariant.getGoogleKey());
        insertAndroidVariant.addBatch();
    }

    private void prepareInsertIosVariantStmt(PreparedStatement insertIosVariant, iOSVariant iosVariant) throws SQLException {
        insertIosVariant.clearParameters();
        insertIosVariant.setString(1, iosVariant.getId());
        insertIosVariant.setBytes(2, iosVariant.getCertificate());
        insertIosVariant.setString(3, iosVariant.getPassphrase());
        insertIosVariant.setBoolean(4, iosVariant.isProduction());
        insertIosVariant.addBatch();
    }

    private void prepareInsertSimplePushVariantStmt(PreparedStatement insertSimplePushVariant, Variant variant) throws SQLException {
        insertSimplePushVariant.clearParameters();
        insertSimplePushVariant.setString(1, variant.getId());
        insertSimplePushVariant.addBatch();
    }

    private void prepareInsertInstallationStmt(PreparedStatement ps, Installation installation) throws SQLException {
        ps.clearParameters();
        ps.setString(1, installation.getId());
        ps.setString(2, installation.getAlias());
        ps.setString(3, installation.getDeviceToken());
        ps.setString(4, installation.getDeviceType());
        ps.setString(5, installation.getOperatingSystem());
        ps.setString(6, installation.getOsVersion());
        ps.setString(7, installation.getPlatform());
        ps.setString(8, installation.getVariant().getId());
        ps.setBoolean(9, installation.isEnabled());
        ps.addBatch();
    }

    private Variant createVariant(VariantType variantType) {
        Variant variant;
        switch (variantType) {
        case ANDROID:
            variant = new AndroidVariant();
            break;
        case IOS:
            variant = new iOSVariant();
            break;
        case SIMPLE_PUSH:
            variant = new SimplePushVariant();
            break;
        case CHROME_PACKAGED_APP:
            variant = new ChromePackagedAppVariant();
            break;
        default:
            throw new RuntimeException();
        }
        return variant;
    }

    private VariantType createVariantType(DataGeneratorContext ctx) {
        VariantType variantType;
        switch (ctx.getConfig().getVariantDistribution()) {
        case EQUAL:
            variantType = ctx.getConfig().getVariantType();
            break;
        case RANDOM:
            variantType = VariantType.values()[RANDOM.nextInt(VariantType.values().length - 1)]; /* CHROME_PACKAGED_APP isn't supported yet */
            break;
        default:
            throw new RuntimeException();
        }
        return variantType;
    }

}
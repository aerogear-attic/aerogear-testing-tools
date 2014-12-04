package org.jboss.aerogear.unifiedpush.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Category;
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
    
    private static final Logger LOGGER = Logger.getLogger(DataGeneratorEndpoint.class.getName());

    private static final Random RANDOM = new Random();

    @Resource(name = "java:jboss/datasources/UnifiedPushDS")
    private DataSource ds;
    
    @Inject
    private Validator validator;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public Response generate(DataGeneratorConfig config) {
        LOGGER.info("Generating test data started");

        DataGeneratorContext ctx = new DataGeneratorContext(config);
        try {
            ctx.getResponse().put("startTime", new Date().toString());

            validateConfig(config);
            cleanupDatabase(ctx);
            generateApplications(ctx);
            generateVariants(ctx);
            generateInstallations(ctx);
            generateCategories(ctx);
            generateCategoriesPerInstallations(ctx);

            ctx.getResponse().put("stopTime", new Date().toString());
        } catch(Exception e) {
            LOGGER.severe(e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
            ctx.getResponse().put("exception", e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
        }

        LOGGER.info("Generating test data finished");

        return Response.ok(ctx.getResponse()).build();
    }

    private void validateConfig(DataGeneratorConfig config) {
        Set<ConstraintViolation<DataGeneratorConfig>> violations = validator.validate(config);
        if( !violations.isEmpty() ) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }
    }
    
    private void cleanupDatabase(DataGeneratorContext ctx) {
        if (!ctx.getConfig().isCleanupDatabase()) {
            return;
        }

        LOGGER.info("Cleaning of database");

        try {
            Connection c = ds.getConnection();
            Statement stmt = c.createStatement();
            stmt.executeUpdate("delete from Installation_Category");
            stmt.executeUpdate("delete from Category");
            stmt.executeUpdate("delete from Installation");
            stmt.executeUpdate("delete from SimplePushVariant");
            stmt.executeUpdate("delete from iOSVariant");
            stmt.executeUpdate("delete from AndroidVariant");
            stmt.executeUpdate("delete from Variant");
            stmt.executeUpdate("delete from PushApplication");
            stmt.close();
            c.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateApplications(DataGeneratorContext ctx) {
        LOGGER.info("Generating applications");

        for (int i = 0; i < ctx.getConfig().getApplicationsCount(); i++) {
            PushApplication application = new PushApplication();
            application.setName(application.getId());
            application.setDescription(application.getId());
            application.setPushApplicationID(application.getId());
            application.setMasterSecret(application.getId());
            application.setDeveloper(ctx.getConfig().getDeveloper());
            ctx.getApplications().add(application);
        }

        executeBatch("insert into PushApplication(id, name, description, pushApplicationID, masterSecret, developer) values(?, ?, ?, ?, ?, ?)", new PrepareInsertApplicationStmt(), ctx.getApplications());
        
        ctx.getResponse().put("applicationsCount", ctx.getApplications().size());
    }

    private void generateVariants(DataGeneratorContext ctx) {
        LOGGER.info("Generating variants");

        List<Pair<String, Variant>> variants = new ArrayList<Pair<String, Variant>>();
        for (PushApplication application : ctx.getApplications()) {
            for (int i = 0; i < ctx.getConfig().getVariantsCount(); i++) {
                VariantType variantType = createVariantType(ctx);
                Variant variant = createVariant(variantType);
                variant.setName(variant.getId());
                variant.setDescription(variant.getId());
                variant.setDeveloper(ctx.getConfig().getDeveloper());
                variant.setSecret(variant.getId());
                variant.setVariantID(variant.getId());

                switch (variantType) {
                    case ANDROID:
                        AndroidVariant androidVariant = (AndroidVariant) variant;
                        androidVariant.setProjectNumber(ctx.getConfig().getProjectNumber());
                        androidVariant.setGoogleKey(ctx.getConfig().getGoogleKey());
                        break;
                    case IOS:
                        iOSVariant iosVariant = (iOSVariant) variant;
                        iosVariant.setCertificate(ctx.getConfig().getCertificateBytes());
                        iosVariant.setPassphrase(ctx.getConfig().getCertificatePass());
                        iosVariant.setProduction(ctx.getConfig().isCertificateProduction());
                        break;
                    case SIMPLE_PUSH:
                        break;
                    default:
                        throw new RuntimeException("");
                }

                application.getVariants().add(variant);
                variants.add(Pair.of(application.getId(), variant));
            }
        }

        executeBatch("insert into Variant(id, name, description, developer, secret, variantid, variants_id, variant_type, type) values(?, ?, ?, ?, ?, ?, ?, ?, ?)", new PrepareInsertVariantStmt(), variants);
        executeBatch("insert into AndroidVariant(id, projectnumber, googlekey) values(?, ?, ?)", new PrepareInsertAndroidVariantStmt(), variants);
        executeBatch("insert into iOSVariant(id, certificate, passphrase, production) values(?, ?, ?, ?)", new PrepareInsertIosVariantStmt(), variants);
        executeBatch("insert into SimplePushVariant(id) values(?)", new PrepareInsertSimplePushVariantStmt(), variants);
        
        ctx.getResponse().put("variantsCount", variants.size());
    }

    private void generateInstallations(DataGeneratorContext ctx) {
        LOGGER.info("Generating installations");
        
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
        
        executeBatch("insert into Installation(id, alias, deviceToken, deviceType, operatingSystem, osVersion, platform, variantid, enabled) values(?, ?, ?, ?, ?, ?, ?, ?, ?)", new PrepareInsertInstallationStmt(), ctx.getInstallations());
        
        ctx.getResponse().put("installationsCount", ctx.getInstallations().size());
    }

    private void generateCategories(DataGeneratorContext ctx) {
        if (ctx.getConfig().getCategoriesCount() <= 0) {
            return;
        }

        LOGGER.info("Generating categories");

        for (int i = 0; i < ctx.getConfig().getCategoriesCount(); i++) {
            Category category = new Category();
            category.setId(new Long(i));
            category.setName(UUID.randomUUID().toString());
            ctx.getCategories().add(category);
        }
        
        executeBatch("insert into Category(id, name) values(?, ?)", new PrepareInsertCategoryStmt(), ctx.getCategories());
        
        ctx.getResponse().put("categoriesCount", ctx.getCategories().size());
    }
    
    private void generateCategoriesPerInstallations(DataGeneratorContext ctx) {
        if (ctx.getCategories().isEmpty() || ctx.getConfig().getCategoriesPerInstallation() <= 0) {
            return;
        }

        LOGGER.info("Generating categories per installations");

        List<Category> categoriesShuffled = new ArrayList<Category>(ctx.getCategories());
        for (Installation installation : ctx.getInstallations()) {
            Collections.shuffle(categoriesShuffled);
            installation.setCategories(new HashSet<Category>(categoriesShuffled.subList(0, ctx.getConfig().getCategoriesPerInstallation())));
        }
        
        List<Pair<String, Long>> instalationCategoryPairs = new ArrayList<Pair<String, Long>>();
        for (Installation installation : ctx.getInstallations()) {
            for (Category category : installation.getCategories()) {
                instalationCategoryPairs.add(Pair.of(installation.getId(), category.getId()));
            }
        }
        
        executeBatch("insert into Installation_Category(installation_id, categories_id) values(?, ?)", new PrepareInsertInstallationCategoryStmt(), instalationCategoryPairs);
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

    private <T> void executeBatch(String sql, PrepareStatement<T> prepareStatement, List<T> entities) {
        try {
            Connection c = null;
            PreparedStatement ps = null;
            try {
                c = ds.getConnection();
                c.setAutoCommit(false);
                ps = c.prepareStatement(sql);

                int batchSize = 1000;
                int counter = 0;
                
                for (T entity : entities) {
                    ps.clearParameters();
                    prepareStatement.prepare(ps, entity);

                    if (++counter >= batchSize) {
                        counter = 0;
                        ps.executeBatch();
                        c.commit();
                    }
                }
                if (counter > 0) {
                    ps.executeBatch();
                    c.commit();
                }

            } catch (SQLException e) {
                if( c != null ) {
                    c.rollback();
                }
                throw e;
            } finally {
                if( ps != null ) {
                    ps.close();
                }
                if( c != null ) {
                    c.close();
                }
            }
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private interface PrepareStatement<T> {
        
        public void prepare(PreparedStatement ps, T entity) throws SQLException;
    
    }

    private static class PrepareInsertApplicationStmt implements PrepareStatement<PushApplication> {

        @Override
        public void prepare(PreparedStatement ps, PushApplication application) throws SQLException {
            ps.setString(1, application.getId());
            ps.setString(2, application.getName());
            ps.setString(3, application.getDescription());
            ps.setString(4, application.getPushApplicationID());
            ps.setString(5, application.getMasterSecret());
            ps.setString(6, application.getDeveloper());
            ps.addBatch();
        }
    
    }
    
    private static class PrepareInsertVariantStmt implements PrepareStatement<Pair<String, Variant>> {

        @Override
        public void prepare(PreparedStatement ps, Pair<String, Variant> tuple) throws SQLException {
            String applicationId = tuple.getLeft();
            Variant variant = tuple.getRight();
            ps.setString(1, variant.getId());
            ps.setString(2, variant.getName());
            ps.setString(3, variant.getDescription());
            ps.setString(4, variant.getDeveloper());
            ps.setString(5, variant.getSecret());
            ps.setString(6, variant.getVariantID());
            ps.setString(7, applicationId);
            ps.setString(8, variant.getType().getTypeName());
            ps.setInt(9, variant.getType().ordinal());
            ps.addBatch();
        }

    }
    
    private static class PrepareInsertAndroidVariantStmt implements PrepareStatement<Pair<String, Variant>> {

        @Override
        public void prepare(PreparedStatement ps, Pair<String, Variant> tuple) throws SQLException {
            if (tuple.getRight() instanceof AndroidVariant) {
                AndroidVariant androidVariant = (AndroidVariant) tuple.getRight();
                ps.setString(1, androidVariant.getId());
                ps.setString(2, androidVariant.getProjectNumber());
                ps.setString(3, androidVariant.getGoogleKey());
                ps.addBatch();
            }
        }

    }
    
    private static class PrepareInsertIosVariantStmt implements PrepareStatement<Pair<String, Variant>> {

        @Override
        public void prepare(PreparedStatement ps, Pair<String, Variant> tuple) throws SQLException {
            if (tuple.getRight() instanceof iOSVariant) {
                iOSVariant iosVariant = (iOSVariant) tuple.getRight();
                ps.setString(1, iosVariant.getId());
                ps.setBytes(2, iosVariant.getCertificate());
                ps.setString(3, iosVariant.getPassphrase());
                ps.setBoolean(4, iosVariant.isProduction());
                ps.addBatch();
            }
        }

    }
    
    private static class PrepareInsertSimplePushVariantStmt implements PrepareStatement<Pair<String, Variant>> {

        @Override
        public void prepare(PreparedStatement ps, Pair<String, Variant> tuple) throws SQLException {
            if (tuple.getRight() instanceof SimplePushVariant) {
                SimplePushVariant simplePushVariant = (SimplePushVariant) tuple.getRight();
                ps.setString(1, simplePushVariant.getId());
                ps.addBatch();
            }
        }

    }
    
    private static class PrepareInsertInstallationStmt implements PrepareStatement<Installation> {

        @Override
        public void prepare(PreparedStatement ps, Installation installation) throws SQLException {
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

    }
    
    private static class PrepareInsertCategoryStmt implements PrepareStatement<Category> {

        @Override
        public void prepare(PreparedStatement ps, Category category) throws SQLException {
            ps.setLong(1, category.getId());
            ps.setString(2, category.getName());
            ps.addBatch();
        }

    }
    
    private static class PrepareInsertInstallationCategoryStmt implements PrepareStatement<Pair<String, Long>> {

        @Override
        public void prepare(PreparedStatement ps, Pair<String, Long> tuple) throws SQLException {
            ps.setString(1, tuple.getLeft());
            ps.setLong(2, tuple.getRight());
            ps.addBatch();
        }

    }

}
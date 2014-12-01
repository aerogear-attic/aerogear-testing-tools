package org.jboss.aerogear.unifiedpush.test.datagenerator;

import java.io.Serializable;
import java.util.UUID;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jboss.aerogear.unifiedpush.api.VariantType;

public class DataGeneratorConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum InstallationDistribution {

        PARETO, FLAT

    }

    public enum VariantDistribution {

        EQUAL, RANDOM

    }

    @Min(1)
    private int applicationsCount;
    
    @Min(1)
    private int variantsCount;
    
    @Min(1)
    private int installationsCount;
    
    @Min(0)
    private int categoriesCount;
    
    @Min(0)
    private int categoriesPerInstallation;
    
    private VariantType variantType;
    
    @NotNull
    private VariantDistribution variantDistribution = VariantDistribution.RANDOM;
    
    @NotNull
    private InstallationDistribution installationDistribution = InstallationDistribution.FLAT;
    
    @NotNull @Size(min = 1, max = 255)
    private String developer = "admin";
    
    private String googleKey = UUID.randomUUID().toString();
    
    private String projectNumber = UUID.randomUUID().toString();
    
    private String certificatePath;
    
    private String certificatePass;
    
    private boolean certificateProduction = false;

    public int getApplicationsCount() {
        return applicationsCount;
    }

    public void setApplicationsCount(int applicationsCount) {
        this.applicationsCount = applicationsCount;
    }

    public int getVariantsCount() {
        return variantsCount;
    }

    public void setVariantsCount(int variantsCount) {
        this.variantsCount = variantsCount;
    }

    public int getInstallationsCount() {
        return installationsCount;
    }

    public void setInstallationsCount(int installationsCount) {
        this.installationsCount = installationsCount;
    }

    public int getCategoriesCount() {
        return categoriesCount;
    }

    public void setCategoriesCount(int categoriesCount) {
        this.categoriesCount = categoriesCount;
    }

    public int getCategoriesPerInstallation() {
        return categoriesPerInstallation;
    }

    public void setCategoriesPerInstallation(int categoriesPerInstallation) {
        this.categoriesPerInstallation = categoriesPerInstallation;
    }

    public VariantType getVariantType() {
        return variantType;
    }

    public void setVariantType(VariantType variantType) {
        this.variantType = variantType;
    }

    public VariantDistribution getVariantDistribution() {
        return variantDistribution;
    }

    public void setVariantDistribution(VariantDistribution variantDistribution) {
        this.variantDistribution = variantDistribution;
    }

    public InstallationDistribution getInstallationDistribution() {
        return installationDistribution;
    }

    public void setInstallationDistribution(InstallationDistribution installationDistribution) {
        this.installationDistribution = installationDistribution;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getGoogleKey() {
        return googleKey;
    }

    public void setGoogleKey(String googleKey) {
        this.googleKey = googleKey;
    }

    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    public String getCertificatePass() {
        return certificatePass;
    }

    public void setCertificatePass(String certificatePass) {
        this.certificatePass = certificatePass;
    }

    public boolean isCertificateProduction() {
        return certificateProduction;
    }

    public void setCertificateProduction(boolean certificateProduction) {
        this.certificateProduction = certificateProduction;
    }

}
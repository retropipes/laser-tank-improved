package com.puttysoftware.lasertank;

import java.net.MalformedURLException;
import java.net.URL;

public class ProductData {
    public static final int CODE_ALPHA = 1;
    public static final int CODE_BETA = 2;
    public static final int CODE_STABLE = 4;
    // Fields
    private URL updateURL;
    private URL blurbURL;
    private URL newVersionURL;
    private String rDNSCompanyName;
    private String companyName;
    private String productName;
    private int majorVersion;
    private int minorVersion;
    private int bugfixVersion;
    private int codeVersion;
    private int prereleaseVersion;

    // Constructors
    public ProductData() {
	// Do nothing
    }

    public ProductData(final String update, final String blurb, final String newVersion, final String companyMac,
	    final String company, final String product, final int major, final int minor, final int bugfix,
	    final int code, final int beta) {
	String rt;
	if (code == ProductData.CODE_ALPHA) {
	    rt = "alpha_";
	} else if (code == ProductData.CODE_BETA) {
	    rt = "beta_";
	} else {
	    rt = "stable_";
	}
	final String updatetxt = "version.txt";
	final String blurbtxt = "blurb.txt";
	try {
	    this.updateURL = new URL(update + rt + updatetxt);
	} catch (final MalformedURLException mu1) {
	    // Ignore
	}
	try {
	    this.blurbURL = new URL(blurb + rt + blurbtxt);
	} catch (final MalformedURLException mu1) {
	    // Ignore
	}
	try {
	    this.newVersionURL = new URL(newVersion);
	} catch (final MalformedURLException mu1) {
	    // Ignore
	}
	this.rDNSCompanyName = companyMac;
	this.companyName = company;
	this.productName = product;
	this.majorVersion = major;
	this.minorVersion = minor;
	this.bugfixVersion = bugfix;
	this.codeVersion = code;
	this.prereleaseVersion = beta;
    }

    /**
     * @return the blurbURL
     */
    public URL getBlurbURL() {
	return this.blurbURL;
    }

    /**
     * @return the bugfixVersion
     */
    public int getBugfixVersion() {
	return this.bugfixVersion;
    }

    /**
     * @return the codeVersion
     */
    public int getCodeVersion() {
	return this.codeVersion;
    }

    /**
     * @return the companyName
     */
    public String getCompanyName() {
	return this.companyName;
    }

    /**
     * @return the majorVersion
     */
    public int getMajorVersion() {
	return this.majorVersion;
    }

    /**
     * @return the minorVersion
     */
    public int getMinorVersion() {
	return this.minorVersion;
    }

    /**
     * @return the newVersionURL
     */
    public URL getNewVersionURL() {
	return this.newVersionURL;
    }

    /**
     * @return the betaVersion
     */
    public int getPrereleaseVersion() {
	return this.prereleaseVersion;
    }

    /**
     * @return the productName
     */
    public String getProductName() {
	return this.productName;
    }

    /**
     * @return the rDNSCompanyName
     */
    public String getrDNSCompanyName() {
	return this.rDNSCompanyName;
    }

    // Methods
    /**
     * @return the updateURL
     */
    public URL getUpdateURL() {
	return this.updateURL;
    }

    /**
     * @param newBlurbURL the blurbURL to set
     */
    public void setBlurbURL(final URL newBlurbURL) {
	this.blurbURL = newBlurbURL;
    }

    /**
     * @param newBugfixVersion the bugfixVersion to set
     */
    public void setBugfixVersion(final int newBugfixVersion) {
	this.bugfixVersion = newBugfixVersion;
    }

    /**
     * @param newCodeVersion the codeVersion to set
     */
    public void setCodeVersion(final int newCodeVersion) {
	this.codeVersion = newCodeVersion;
    }

    /**
     * @param newCompanyName the companyName to set
     */
    public void setCompanyName(final String newCompanyName) {
	this.companyName = newCompanyName;
    }

    /**
     * @param newMajorVersion the majorVersion to set
     */
    public void setMajorVersion(final int newMajorVersion) {
	this.majorVersion = newMajorVersion;
    }

    /**
     * @param newMinorVersion the minorVersion to set
     */
    public void setMinorVersion(final int newMinorVersion) {
	this.minorVersion = newMinorVersion;
    }

    /**
     * @param newNewVersionURL the newVersionURL to set
     */
    public void setNewVersionURL(final URL newNewVersionURL) {
	this.newVersionURL = newNewVersionURL;
    }

    /**
     * @param newPrereleaseVersion the prereleaseVersion to set
     */
    public void setPrereleaseVersion(final int newPrereleaseVersion) {
	this.prereleaseVersion = newPrereleaseVersion;
    }

    /**
     * @param newProductName the productName to set
     */
    public void setProductName(final String newProductName) {
	this.productName = newProductName;
    }

    /**
     * @param newRDNSCompanyName the rDNSCompanyName to set
     */
    public void setrDNSCompanyName(final String newRDNSCompanyName) {
	this.rDNSCompanyName = newRDNSCompanyName;
    }

    /**
     * @param newUpdateURL the updateURL to set
     */
    public void setUpdateURL(final URL newUpdateURL) {
	this.updateURL = newUpdateURL;
    }
}

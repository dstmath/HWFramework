package com.android.server.pm;

public class HwMdmWpPackageInfo extends HwMdmDFT {
    private String mDoPkg;
    private String mDoPkgSigHash;
    private String mDoPkgVer;

    public String getDopkg() {
        return this.mDoPkg;
    }

    public void setDopkg(String doPkg) {
        this.mDoPkg = doPkg;
    }

    public String getDopkgver() {
        return this.mDoPkgVer;
    }

    public void setDopkgver(String doPkgVer) {
        this.mDoPkgVer = doPkgVer;
    }

    public String getDopkgsighash() {
        return this.mDoPkgSigHash;
    }

    public void setDopkgsighash(String doPkgSigHash) {
        this.mDoPkgSigHash = doPkgSigHash;
    }
}

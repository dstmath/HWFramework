package com.android.server.pm;

public class HwMdmWpPackageInfo extends HwMdmDFT {
    private String dopkg;
    private String dopkgsighash;
    private String dopkgver;

    public String getDopkg() {
        return this.dopkg;
    }

    public void setDopkg(String dopkg2) {
        this.dopkg = dopkg2;
    }

    public String getDopkgver() {
        return this.dopkgver;
    }

    public void setDopkgver(String dopkgver2) {
        this.dopkgver = dopkgver2;
    }

    public String getDopkgsighash() {
        return this.dopkgsighash;
    }

    public void setDopkgsighash(String dopkgsighash2) {
        this.dopkgsighash = dopkgsighash2;
    }
}

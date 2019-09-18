package com.android.server.pm;

public class HwMdmDFT {
    private String pkg;
    private String sighash;
    private String version;

    public HwMdmDFT(String pkg2, String version2, String sighash2) {
        this.pkg = pkg2;
        this.version = version2;
        this.sighash = sighash2;
    }

    public HwMdmDFT() {
    }

    public String getPkg() {
        return this.pkg;
    }

    public void setPkg(String pkg2) {
        this.pkg = pkg2;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version2) {
        this.version = version2;
    }

    public String getSighash() {
        return this.sighash;
    }

    public void setSighash(String sighash2) {
        this.sighash = sighash2;
    }
}

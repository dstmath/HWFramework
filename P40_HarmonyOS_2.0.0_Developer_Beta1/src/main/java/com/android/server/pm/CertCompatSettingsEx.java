package com.android.server.pm;

import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;
import android.content.pm.Signature;
import com.android.server.pm.CertCompatSettings;
import com.huawei.android.content.pm.SignatureEx;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CertCompatSettingsEx {
    private CertCompatSettings certCompatSettings = new CertCompatSettings();

    public static final class PackageEx {
        private CertCompatSettings.Package mPackage;

        public PackageEx() {
        }

        public PackageEx(String packageName, String codePath, long timeStamp, String certType) {
            this.mPackage = new CertCompatSettings.Package(packageName, codePath, timeStamp, certType);
        }

        public CertCompatSettings.Package getPackage() {
            return this.mPackage;
        }

        public void setPackage(CertCompatSettings.Package mPackage2) {
            this.mPackage = mPackage2;
        }

        public long getTimeStamp() {
            return this.mPackage.timeStamp;
        }

        public String getCodePath() {
            CertCompatSettings.Package r0 = this.mPackage;
            if (r0 == null) {
                return null;
            }
            return r0.codePath;
        }

        public String getCertType() {
            return this.mPackage.certType;
        }

        public String getPackageName() {
            return this.mPackage.packageName;
        }
    }

    public CertCompatSettings getCertCompatSettings() {
        return this.certCompatSettings;
    }

    public void setCertCompatSettings(CertCompatSettings certCompatSettings2) {
        this.certCompatSettings = certCompatSettings2;
    }

    public void systemReady() {
        this.certCompatSettings.systemReady();
    }

    public boolean isOldSystemSignature(SignatureEx[] signs) {
        Signature[] signatures = new Signature[signs.length];
        for (int i = 0; i < signs.length; i++) {
            signatures[i] = signs[i].getSignature();
        }
        return this.certCompatSettings.isOldSystemSignature(signatures);
    }

    public PackageEx getCompatPackage(String name) {
        CertCompatSettings.Package packages = this.certCompatSettings.getCompatPackage(name);
        PackageEx packageEx = new PackageEx();
        packageEx.setPackage(packages);
        return packageEx;
    }

    public boolean isUpgrade() {
        return this.certCompatSettings.isUpgrade();
    }

    public boolean isIncompatPackage(PackageParserEx.PackageEx pkg) {
        return this.certCompatSettings.isIncompatPackage((PackageParser.Package) pkg.getPackage());
    }

    public String getOldSignTpye(SignatureEx[] signs) {
        Signature[] signatures = new Signature[signs.length];
        for (int i = 0; i < signs.length; i++) {
            signatures[i] = signs[i].getSignature();
        }
        return this.certCompatSettings.getOldSignTpye(signatures);
    }

    public boolean isCompatAllLegacyPackages() {
        return this.certCompatSettings.isCompatAllLegacyPackages();
    }

    public boolean isWhiteListedApp(PackageParserEx.PackageEx pkg, boolean isBootScan) {
        return this.certCompatSettings.isWhiteListedApp((PackageParser.Package) pkg.getPackage(), isBootScan);
    }

    public boolean isSystemSignatureForWhiteList(SignatureEx[] signs) {
        Signature[] signatures = new Signature[signs.length];
        for (int i = 0; i < signs.length; i++) {
            signatures[i] = signs[i].getSignature();
        }
        return this.certCompatSettings.isSystemSignatureForWhiteList(signatures);
    }

    public String getSignTpyeForWhiteList(SignatureEx[] signs) {
        Signature[] signatures = new Signature[signs.length];
        for (int i = 0; i < signs.length; i++) {
            signatures[i] = signs[i].getSignature();
        }
        return this.certCompatSettings.getSignTpyeForWhiteList(signatures);
    }

    public boolean isLegacySignature(SignatureEx[] signs) {
        Signature[] signatures = new Signature[signs.length];
        for (int i = 0; i < signs.length; i++) {
            signatures[i] = signs[i].getSignature();
        }
        return this.certCompatSettings.isLegacySignature(signatures);
    }

    public SignatureEx[] getNewSign(String type) {
        Signature[] signatures = this.certCompatSettings.getNewSign(type);
        SignatureEx[] signatureExes = new SignatureEx[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            signatureExes[i] = new SignatureEx();
            signatureExes[i].setSignature(signatures[i]);
        }
        return signatureExes;
    }

    public boolean readCertCompatPackages() {
        return this.certCompatSettings.readCertCompatPackages();
    }

    public Collection<PackageEx> getALLCompatPackages() {
        CertCompatSettings.Package[] packages = (CertCompatSettings.Package[]) this.certCompatSettings.getAllCompatPackages().toArray(new CertCompatSettings.Package[0]);
        List<PackageEx> packageExList = new ArrayList<>();
        for (CertCompatSettings.Package r5 : packages) {
            PackageEx packageEx = new PackageEx();
            packageEx.setPackage(r5);
            packageExList.add(packageEx);
        }
        return packageExList;
    }

    public void writeCertCompatPackages() {
        this.certCompatSettings.writeCertCompatPackages();
    }

    public void removeCertCompatPackage(String packageName) {
        this.certCompatSettings.removeCertCompatPackage(packageName);
    }

    public boolean isSystemSignatureUpdated(SignatureEx[] oldSignature, SignatureEx[] newSignature) {
        Signature[] oldSign = new Signature[oldSignature.length];
        Signature[] newSign = new Signature[newSignature.length];
        for (int i = 0; i < oldSignature.length; i++) {
            oldSign[i] = oldSignature[i].getSignature();
        }
        for (int i2 = 0; i2 < newSignature.length; i2++) {
            newSign[i2] = newSignature[i2].getSignature();
        }
        return this.certCompatSettings.isSystemSignatureUpdated(oldSign, newSign);
    }

    public void insertCompatPackage(String packageName, PackageSettingEx ps) {
        this.certCompatSettings.insertCompatPackage(packageName, ps.getPackageSetting());
    }
}

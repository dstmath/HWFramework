package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import android.text.TextUtils;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.util.EncryptionUtils;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipFile;
import org.xmlpull.v1.XmlPullParser;

public class ApkHashProcessor implements IProcessor {
    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean readCert(String certLine, HwCertification.CertificationData certData) {
        if (TextUtils.isEmpty(certLine) || certData == null || !certLine.startsWith(HwCertification.KEY_APK_HASH) || certLine.length() <= HwCertification.KEY_APK_HASH.length() + 1) {
            return false;
        }
        String apkHash = certLine.substring(HwCertification.KEY_APK_HASH.length() + 1);
        if (TextUtils.isEmpty(apkHash)) {
            HwAuthLogger.error(IProcessor.TAG, "AH_RC is empty!");
            return false;
        }
        certData.setApkHash(apkHash);
        return true;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseCert(HwCertification hwCert) {
        HwCertification.CertificationData certData;
        if (hwCert == null || (certData = hwCert.getCertificationData()) == null) {
            return false;
        }
        String apkHash = certData.getApkHash();
        if (!TextUtils.isEmpty(apkHash)) {
            hwCert.setApkHash(apkHash);
            return true;
        }
        HwAuthLogger.error(IProcessor.TAG, "AH_PC error!");
        return false;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean verifyCert(PackageParser.Package pkg, HwCertification hwCert) {
        if (pkg == null || hwCert == null) {
            return false;
        }
        if (hwCert.isContainSpecialPermissions()) {
            return true;
        }
        String apkHash = hwCert.getApkHash();
        if (TextUtils.isEmpty(apkHash)) {
            return false;
        }
        if (!hwCert.isReleased()) {
            if ("*".equals(apkHash)) {
                return true;
            }
            HwAuthLogger.error(IProcessor.TAG, "AH_VC error not * in debug cert!");
            return false;
        } else if ("*".equals(apkHash)) {
            return true;
        } else {
            try {
                if (apkHash.equals(generateApkHash(hwCert))) {
                    return true;
                }
                HwAuthLogger.error(IProcessor.TAG, "AH_VC error:not same!");
                return false;
            } catch (IllegalStateException e) {
                HwAuthLogger.error(IProcessor.TAG, "AH_VC failed illegal state!");
            } catch (Exception e2) {
                HwAuthLogger.error(IProcessor.TAG, "AH_VC failed!");
            }
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification hwCert) {
        if (!TextUtils.isEmpty(tag) && parser != null && hwCert != null && HwCertification.KEY_APK_HASH.equals(tag)) {
            String apkHash = parser.getAttributeValue(null, "value");
            HwCertification.CertificationData certData = hwCert.getCertificationData();
            if (certData != null) {
                certData.setApkHash(apkHash);
                return true;
            }
        }
        return false;
    }

    private String generateApkHash(HwCertification hwCert) {
        byte[] bytes;
        if (hwCert == null) {
            HwAuthLogger.error(IProcessor.TAG, "AH_G cert is null!");
            return "";
        }
        String filePath = null;
        try {
            ZipFile zipFile = hwCert.getZipFile();
            if (zipFile != null) {
                try {
                    filePath = Utils.getSfFileName(zipFile);
                } catch (Throwable th) {
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            }
            if (filePath == null || !Utils.isUsingSignatureSchemaV2(zipFile, zipFile.getEntry(filePath))) {
                HwAuthLogger.info(IProcessor.TAG, " AH_G not V2.");
                bytes = Utils.getManifestFileWithoutHwCer(hwCert.getApkFile());
            } else {
                HwAuthLogger.info(IProcessor.TAG, "AH_G V2 sort manifest content.");
                bytes = Utils.getManifestFileWithoutHwCer(zipFile, zipFile.getEntry("META-INF/MANIFEST.MF"));
            }
            if (bytes != null) {
                if (bytes.length != 0) {
                    String bytesToString = Utils.bytesToString(EncryptionUtils.sha256(bytes));
                    if (zipFile != null) {
                        zipFile.close();
                    }
                    hwCert.resetZipFile();
                    return bytesToString;
                }
            }
            if (zipFile != null) {
                zipFile.close();
            }
            hwCert.resetZipFile();
            return "";
        } catch (NoSuchAlgorithmException e) {
            HwAuthLogger.error(IProcessor.TAG, "AH_G no such algorithm!");
        } catch (IOException e2) {
            HwAuthLogger.error(IProcessor.TAG, "AH_G failed to close zip file!");
        } catch (Throwable th3) {
            hwCert.resetZipFile();
            throw th3;
        }
        hwCert.resetZipFile();
        return "";
    }
}

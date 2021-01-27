package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import android.text.TextUtils;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;

public class HwCertificationProcessor {
    private static final int MAX_LINE_LENGTH = 20000;
    private static final int PROCESSOR_SIZE = 11;
    private static final HashMap<String, IProcessor> S_PROCESSOR_MAP = new HashMap<>(11);
    private static final String TAG = "HwCertificationManager";
    private List<String> mAvailableTagList = new ArrayList();
    private ZipFile mZipFile = null;

    static {
        S_PROCESSOR_MAP.put(HwCertification.KEY_VERSION, new VersionProcessor());
        S_PROCESSOR_MAP.put(HwCertification.KEY_DEVELOPER, new DeveloperKeyProcessor());
        S_PROCESSOR_MAP.put(HwCertification.KEY_PACKAGE_NAME, new PackageNameProcessor());
        S_PROCESSOR_MAP.put(HwCertification.KEY_PERMISSIONS, new PermissionProcessor());
        S_PROCESSOR_MAP.put(HwCertification.KEY_DEVICE_IDS, new DeviceIdProcessor());
        S_PROCESSOR_MAP.put(HwCertification.KEY_VALID_PERIOD, new ValidPeriodProcessor());
        S_PROCESSOR_MAP.put(HwCertification.KEY_APK_HASH, new ApkHashProcessor());
        S_PROCESSOR_MAP.put(HwCertification.KEY_CERTIFICATE, new CertificateProcessor());
        S_PROCESSOR_MAP.put(HwCertification.KEY_EXTENSION, new ExtensionProcessor());
        S_PROCESSOR_MAP.put(HwCertification.KEY_SIGNATURE, new SignatureV1Processor());
        S_PROCESSOR_MAP.put(HwCertification.KEY_SIGNATURE2, new SignatureV2Processor());
        S_PROCESSOR_MAP.put(HwCertification.KEY_SIGNATURE3, new SignatureV3Processor());
    }

    public static IProcessor getProcessor(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            return S_PROCESSOR_MAP.get(tag);
        }
        return null;
    }

    private InputStream readCertFileFromApk(String apkPath, HwCertification hwCert) {
        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            HwAuthLogger.error("HwCertificationManager", "HC_RC read file error!");
            return null;
        }
        HwCertification.CertificationData certData = hwCert.getCertificationData();
        if (certData != null) {
            certData.setApkFile(apkFile);
        }
        hwCert.setApkFile(apkFile);
        ZipFile zipFile = this.mZipFile;
        if (zipFile == null) {
            return Utils.readHwCertFromApk(apkPath);
        }
        return Utils.readHwCertFromApk(this.mZipFile, zipFile.getEntry(Utils.CERT_NAME));
    }

    public boolean createZipFile(String apkPath) {
        if (TextUtils.isEmpty(apkPath)) {
            return false;
        }
        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            HwAuthLogger.error("HwCertificationManager", "HC_CZ read file error!");
            return false;
        }
        try {
            this.mZipFile = new ZipFile(apkFile);
            return true;
        } catch (IOException e) {
            this.mZipFile = null;
            HwAuthLogger.error("HwCertificationManager", "IOException in createZipFile!");
            return false;
        }
    }

    public void releaseZipFileResource() {
        IoUtils.closeQuietly(this.mZipFile);
        this.mZipFile = null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0090, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0091, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0094, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0097, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0098, code lost:
        if (r0 != null) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x009a, code lost:
        $closeResource(r2, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x009d, code lost:
        throw r3;
     */
    public synchronized boolean readCert(String apkPath, HwCertification hwCert) {
        if (!TextUtils.isEmpty(apkPath)) {
            if (hwCert != null) {
                this.mAvailableTagList.clear();
                try {
                    InputStream inputStream = readCertFileFromApk(apkPath, hwCert);
                    BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    for (String certLine = Utils.readLine(bufReader); certLine != null; certLine = Utils.readLine(bufReader)) {
                        String keyTag = certLine.split(AwarenessInnerConstants.COLON_KEY)[0].trim();
                        if (HwCertification.isHwCertKeyContainsTag(keyTag)) {
                            IProcessor processor = S_PROCESSOR_MAP.get(keyTag);
                            if (processor == null) {
                                HwAuthLogger.error("HwCertificationManager", "HC_RC error process is null!");
                                $closeResource(null, bufReader);
                                if (inputStream != null) {
                                    $closeResource(null, inputStream);
                                }
                                return false;
                            } else if (!readCert(processor, certLine, hwCert.getCertificationData())) {
                                HwAuthLogger.error("HwCertificationManager", "HC_RC error line mismatch!");
                                $closeResource(null, bufReader);
                                if (inputStream != null) {
                                    $closeResource(null, inputStream);
                                }
                                return false;
                            } else {
                                this.mAvailableTagList.add(keyTag);
                            }
                        }
                    }
                    hwCert.setZipFile(this.mZipFile);
                    $closeResource(null, bufReader);
                    if (inputStream != null) {
                        $closeResource(null, inputStream);
                    }
                    return true;
                } catch (IOException e) {
                    HwAuthLogger.error("HwCertificationManager", "HC_RC error IOException!");
                    return false;
                } catch (Exception e2) {
                    HwAuthLogger.error("HwCertificationManager", "HC_RC error exception!");
                    return false;
                }
            }
        }
        return false;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    private boolean readCert(IProcessor processor, String certLine, HwCertification.CertificationData certData) {
        if (certLine.length() < MAX_LINE_LENGTH && certData != null) {
            return processor.readCert(certLine, certData);
        }
        HwAuthLogger.error("HwCertificationManager", "HC_RC cert is too long or cert data is null!");
        return false;
    }

    public boolean parseCert(HwCertification hwCert) {
        if (hwCert == null) {
            return false;
        }
        int size = this.mAvailableTagList.size();
        for (int i = 0; i < size; i++) {
            String parserTag = this.mAvailableTagList.get(i);
            IProcessor processor = S_PROCESSOR_MAP.get(parserTag);
            if (processor == null) {
                HwAuthLogger.error("HwCertificationManager", "HC_PC process is null!");
                return false;
            } else if (!processor.parseCert(hwCert)) {
                HwAuthLogger.error("HwCertificationManager", "HC_PC tag is " + parserTag);
                return false;
            }
        }
        return true;
    }

    public boolean verifyCert(PackageParser.Package pkg, HwCertification hwCert) {
        if (pkg == null || hwCert == null) {
            return false;
        }
        int size = this.mAvailableTagList.size();
        for (int i = 0; i < size; i++) {
            String verifyTag = this.mAvailableTagList.get(i);
            IProcessor processor = S_PROCESSOR_MAP.get(verifyTag);
            if (processor == null) {
                HwAuthLogger.error("HwCertificationManager", "HC_VC error process is null tag is " + verifyTag);
                return false;
            } else if (!processor.verifyCert(pkg, hwCert)) {
                HwAuthLogger.error("HwCertificationManager", "HC_VC error error tag is " + verifyTag);
                return false;
            }
        }
        String version = hwCert.getVersion();
        if (!"2".equals(version) && TextUtils.isEmpty(hwCert.getSignatureV3()) && TextUtils.isEmpty(hwCert.getSignatureV2())) {
            HwAuthLogger.error("HwCertificationManager", "This is old cert, no signature v3 or signature v2!");
            return false;
        } else if (!checkSignatureValid(hwCert)) {
            HwAuthLogger.error("HwCertificationManager", "cert's signature is null");
            return false;
        } else {
            HwAuthLogger.info("HwCertificationManager", "HC_VC ok version:" + version);
            return true;
        }
    }

    private boolean checkSignatureValid(HwCertification hwCert) {
        return !TextUtils.isEmpty(hwCert.getSignatureV1()) || !TextUtils.isEmpty(hwCert.getSignatureV2()) || !TextUtils.isEmpty(hwCert.getSignatureV3());
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification hwCert) {
        if (TextUtils.isEmpty(tag) || parser == null || hwCert == null) {
            return false;
        }
        IProcessor processor = S_PROCESSOR_MAP.get(tag);
        if (processor == null) {
            HwAuthLogger.error("HwCertificationManager", "HC_PX error process is null tag is " + tag);
            return false;
        } else if (processor.parseXmlTag(tag, parser, hwCert)) {
            return true;
        } else {
            return false;
        }
    }
}

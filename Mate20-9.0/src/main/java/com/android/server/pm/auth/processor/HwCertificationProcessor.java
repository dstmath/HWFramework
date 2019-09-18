package com.android.server.pm.auth.processor;

import android.annotation.SuppressLint;
import android.content.pm.PackageParser;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;
import org.xmlpull.v1.XmlPullParser;

public class HwCertificationProcessor {
    private static final int HASH_MAP_LENGHT = 10;
    private static final int MAX_LINE_LENGHT = 20000;
    protected static final HashMap<String, IProcessor> PROCESSOR_MAP = new HashMap<>(10);
    public static final String TAG = "HwCertificationManager";
    private List<String> mAvailableTagList = new ArrayList();
    private ZipFile mZipFile = null;

    static {
        PROCESSOR_MAP.put(HwCertification.KEY_VERSION, new VersionProcessor());
        PROCESSOR_MAP.put(HwCertification.KEY_DEVELIOPER, new DeveloperKeyProcessor());
        PROCESSOR_MAP.put("PackageName", new PackageNameProcessor());
        PROCESSOR_MAP.put(HwCertification.KEY_PERMISSIONS, new PermissionProcessor());
        PROCESSOR_MAP.put(HwCertification.KEY_DEVICE_IDS, new DeviceIdProcessor());
        PROCESSOR_MAP.put(HwCertification.KEY_VALID_PERIOD, new ValidPeriodProcessor());
        PROCESSOR_MAP.put(HwCertification.KEY_APK_HASH, new ApkHashProcessor());
        PROCESSOR_MAP.put(HwCertification.KEY_CERTIFICATE, new CertificateProcessor());
        PROCESSOR_MAP.put(HwCertification.KEY_EXTENSION, new ExtenstionProcessor());
        PROCESSOR_MAP.put(HwCertification.KEY_SIGNATURE, new SignatureProcessor());
    }

    public static IProcessor getProcessors(String tag) {
        return PROCESSOR_MAP.get(tag);
    }

    private InputStream readCertFileFromApk(String apkPath, HwCertification cert) {
        InputStream input;
        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            HwAuthLogger.e("HwCertificationManager", "HC_RC read file error");
            return null;
        }
        cert.mCertificationData.mApkFile = apkFile;
        cert.setApkFile(apkFile);
        if (this.mZipFile != null) {
            input = Utils.readHwCertFromApk(this.mZipFile, this.mZipFile.getEntry(Utils.CERT_NAME));
        } else {
            input = Utils.readHwCertFromApk(apkPath);
        }
        return input;
    }

    public boolean createZipFile(String apkPath) {
        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            HwAuthLogger.e("HwCertificationManager", "HC_CZ read file error");
            return false;
        }
        try {
            this.mZipFile = new ZipFile(apkFile);
            return true;
        } catch (IOException e) {
            this.mZipFile = null;
            HwAuthLogger.e("HwCertificationManager", "IOException in createZipFile, e is " + e);
            return false;
        }
    }

    public boolean releaseZipFileResource() {
        try {
            if (this.mZipFile != null) {
                this.mZipFile.close();
                this.mZipFile = null;
            }
            return true;
        } catch (IOException e) {
            HwAuthLogger.e("HwCertificationManager", "IOException in releaseZipFileResource, e is " + e);
            return false;
        } catch (Exception e2) {
            HwAuthLogger.e("HwCertificationManager", "Exception in releaseZipFileResource, e is " + e2);
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:127:0x0163, code lost:
        if (r1 != null) goto L_0x0165;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:?, code lost:
        com.android.server.pm.auth.util.HwAuthLogger.e("HwCertificationManager", "read cert error : close stream failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x0174, code lost:
        if (r2 != null) goto L_0x0176;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:?, code lost:
        com.android.server.pm.auth.util.HwAuthLogger.e("HwCertificationManager", "read cert error : close stream failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x0184, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        com.android.server.pm.auth.util.HwAuthLogger.e("HwCertificationManager", "read cert error : close stream failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0068, code lost:
        com.android.server.pm.auth.util.HwAuthLogger.e("HwCertificationManager", "HC_RC error process is null");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0070, code lost:
        if (r1 == null) goto L_0x0082;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        com.android.server.pm.auth.util.HwAuthLogger.e("HwCertificationManager", "read cert error : close stream failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x009c, code lost:
        com.android.server.pm.auth.util.HwAuthLogger.e("HwCertificationManager", "HC_RC error line mismatch");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00a4, code lost:
        if (r1 == null) goto L_0x00b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:?, code lost:
        com.android.server.pm.auth.util.HwAuthLogger.e("HwCertificationManager", "read cert error : close stream failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x00ce, code lost:
        if (r1 == null) goto L_0x00e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:?, code lost:
        com.android.server.pm.auth.util.HwAuthLogger.e("HwCertificationManager", "read cert error : close stream failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x00f7, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:9:0x0015, B:89:0x00fe] */
    public synchronized boolean readCert(String apkPath, HwCertification cert) {
        BufferedReader br;
        this.mAvailableTagList.clear();
        InputStream input = null;
        br = null;
        try {
            input = readCertFileFromApk(apkPath, cert);
            if (input != null) {
                br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                while (true) {
                    String readLine = br.readLine();
                    String line = readLine;
                    if (readLine == null) {
                        break;
                    }
                    String keyTag = line.split(":")[0].trim();
                    if (HwCertification.isHwCertKeyContainsTag(keyTag)) {
                        IProcessor processor = PROCESSOR_MAP.get(keyTag);
                        if (processor == null) {
                            break;
                        } else if (!readCert(processor, line, cert.mCertificationData)) {
                            break;
                        } else {
                            this.mAvailableTagList.add(keyTag);
                        }
                    }
                }
            } else {
                if (input != null) {
                    input.close();
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        HwAuthLogger.e("HwCertificationManager", "read cert error : close stream failed!");
                    }
                }
            }
        } catch (RuntimeException e2) {
            HwAuthLogger.e("HwCertificationManager", "HC_RC error runtimeException : " + e2);
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e3) {
                    HwAuthLogger.e("HwCertificationManager", "read cert error : close stream failed!");
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e4) {
                    HwAuthLogger.e("HwCertificationManager", "read cert error : close stream failed!");
                }
            }
            return false;
        } catch (Exception e5) {
            HwAuthLogger.e("HwCertificationManager", "HC_RC error throw exception");
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e6) {
                    HwAuthLogger.e("HwCertificationManager", "read cert error : close stream failed!");
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e7) {
                    HwAuthLogger.e("HwCertificationManager", "read cert error : close stream failed!");
                }
            }
            return false;
        }
        try {
            br.close();
        } catch (IOException e8) {
            HwAuthLogger.e("HwCertificationManager", "read cert error : close stream failed!");
        }
        cert.setZipFile(this.mZipFile);
        return true;
        try {
            br.close();
        } catch (IOException e9) {
            HwAuthLogger.e("HwCertificationManager", "read cert error : close stream failed!");
        }
        return false;
        return false;
        cert.setZipFile(this.mZipFile);
        return true;
        return false;
        return false;
        try {
            br.close();
        } catch (IOException e10) {
            HwAuthLogger.e("HwCertificationManager", "read cert error : close stream failed!");
        }
        return false;
    }

    private boolean readCert(IProcessor processor, String line, HwCertification.CertificationData rawCert) {
        if (line.length() < MAX_LINE_LENGHT) {
            return processor.readCert(line, rawCert);
        }
        HwAuthLogger.e("HwCertificationManager", "HC_RC cert is too long");
        return false;
    }

    @SuppressLint({"AvoidMethodInForLoop"})
    public boolean parserCert(HwCertification rawCert) {
        for (String parserTag : this.mAvailableTagList) {
            IProcessor processor = PROCESSOR_MAP.get(parserTag);
            if (processor == null) {
                HwAuthLogger.e("HwCertificationManager", "HC_PC process is null");
                return false;
            } else if (!processor.parserCert(rawCert)) {
                HwAuthLogger.e("HwCertificationManager", "HC_PC tag =" + parserTag);
                return false;
            }
        }
        return true;
    }

    @SuppressLint({"AvoidMethodInForLoop"})
    public boolean verifyCert(PackageParser.Package pkg, HwCertification cert) {
        for (String verifyTag : this.mAvailableTagList) {
            IProcessor processor = PROCESSOR_MAP.get(verifyTag);
            if (processor == null) {
                HwAuthLogger.e("HwCertificationManager", "HC_VC error process is null tag =" + verifyTag);
                return false;
            } else if (!processor.verifyCert(pkg, cert)) {
                HwAuthLogger.e("HwCertificationManager", "HC_VC error error tag =" + verifyTag);
                return false;
            }
        }
        HwAuthLogger.i("HwCertificationManager", "HC_VC ok");
        return true;
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        IProcessor processor = PROCESSOR_MAP.get(tag);
        if (processor == null) {
            HwAuthLogger.e("HwCertificationManager", "HC_PX error process is null tag =" + tag);
            return false;
        } else if (processor.parseXmlTag(tag, parser, cert)) {
            return true;
        } else {
            return false;
        }
    }
}

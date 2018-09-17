package com.android.server.pm.auth;

import android.os.Environment;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.pm.auth.processor.HwCertificationProcessor;
import com.android.server.pm.auth.processor.IProcessor;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class HwCertXmlHandler {
    private static final String FILE_NAME = "hwcert.xml";
    public static final String TAG = "HwCertificationManager";
    public static final String TAG_CERT = "cert";
    public static final String TAG_HWCERT = "hwcerts";
    public static final String TAG_VALUE = "value";

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void readHwCertXml(ConcurrentHashMap<String, HwCertification> certs) {
        Throwable th;
        RuntimeException e;
        Exception e2;
        InputStreamReader fr = null;
        FileInputStream fileInputStream = null;
        try {
            File certXml = new File(new File(Environment.getDataDirectory(), "system"), FILE_NAME);
            if (certXml.exists()) {
                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    FileInputStream fis = new FileInputStream(certXml);
                    try {
                        InputStreamReader fr2 = new InputStreamReader(fis, "UTF-8");
                        try {
                            int type;
                            parser.setInput(fr2);
                            do {
                                type = parser.next();
                                if (type == 1) {
                                    break;
                                }
                            } while (type != 2);
                            String tag = parser.getName();
                            if (TAG_HWCERT.equals(tag)) {
                                int outerDepth = parser.getDepth();
                                while (true) {
                                    type = parser.next();
                                    if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                                        boolean shouldSkip = type == 3 || type == 4;
                                        if (!shouldSkip) {
                                            tag = parser.getName();
                                            if (TAG_CERT.equals(tag)) {
                                                HwCertification cert = processCertTag(parser);
                                                if (!(cert == null || cert.getPackageName() == null)) {
                                                    certs.put(cert.getPackageName(), cert);
                                                }
                                            } else {
                                                HwAuthLogger.e(TAG, "readHwCertXml:unknow tag:" + tag);
                                                XmlUtils.skipCurrentTag(parser);
                                            }
                                        }
                                    } else if (fr2 != null) {
                                        try {
                                            fr2.close();
                                        } catch (IOException e3) {
                                            HwAuthLogger.e(TAG, "readHwCertXml:failed to close FileReader", e3);
                                        }
                                    }
                                }
                                if (fr2 != null) {
                                    fr2.close();
                                }
                                if (fis != null) {
                                    try {
                                        fis.close();
                                    } catch (IOException e32) {
                                        HwAuthLogger.e(TAG, "readHwCertXml:failed to close FileInputStream", e32);
                                    }
                                }
                                fileInputStream = fis;
                                return;
                            }
                            HwAuthLogger.e(TAG, "readHwCertXml:unexpected tag found: " + tag);
                            if (fr2 != null) {
                                try {
                                    fr2.close();
                                } catch (IOException e322) {
                                    HwAuthLogger.e(TAG, "readHwCertXml:failed to close FileReader", e322);
                                } catch (Throwable th2) {
                                    th = th2;
                                    fr = fr2;
                                    throw th;
                                }
                            }
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (IOException e3222) {
                                    HwAuthLogger.e(TAG, "readHwCertXml:failed to close FileInputStream", e3222);
                                }
                            }
                            return;
                        } catch (RuntimeException e4) {
                            e = e4;
                            fileInputStream = fis;
                            fr = fr2;
                        } catch (Exception e5) {
                            e2 = e5;
                            fileInputStream = fis;
                            fr = fr2;
                        } catch (Throwable th3) {
                            th = th3;
                            fileInputStream = fis;
                            fr = fr2;
                        }
                    } catch (RuntimeException e6) {
                        e = e6;
                        fileInputStream = fis;
                        try {
                            HwAuthLogger.e(TAG, "readHwCertXml:RuntimeException", e);
                            if (fr != null) {
                                try {
                                    fr.close();
                                } catch (IOException e32222) {
                                    HwAuthLogger.e(TAG, "readHwCertXml:failed to close FileReader", e32222);
                                }
                            }
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e322222) {
                                    HwAuthLogger.e(TAG, "readHwCertXml:failed to close FileInputStream", e322222);
                                }
                            }
                            return;
                        } catch (Throwable th4) {
                            th = th4;
                            if (fr != null) {
                                try {
                                    fr.close();
                                } catch (IOException e3222222) {
                                    HwAuthLogger.e(TAG, "readHwCertXml:failed to close FileReader", e3222222);
                                }
                            }
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e32222222) {
                                    HwAuthLogger.e(TAG, "readHwCertXml:failed to close FileInputStream", e32222222);
                                }
                            }
                            throw th;
                        }
                    } catch (Exception e7) {
                        e2 = e7;
                        fileInputStream = fis;
                        HwAuthLogger.e(TAG, "readHwCertXml:IOException", e2);
                        if (fr != null) {
                            try {
                                fr.close();
                            } catch (IOException e322222222) {
                                HwAuthLogger.e(TAG, "readHwCertXml:failed to close FileReader", e322222222);
                            }
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        return;
                    } catch (Throwable th5) {
                        th = th5;
                        fileInputStream = fis;
                        if (fr != null) {
                            fr.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        throw th;
                    }
                } catch (RuntimeException e8) {
                    e = e8;
                    HwAuthLogger.e(TAG, "readHwCertXml:RuntimeException", e);
                    if (fr != null) {
                        fr.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return;
                } catch (Exception e9) {
                    e2 = e9;
                    HwAuthLogger.e(TAG, "readHwCertXml:IOException", e2);
                    if (fr != null) {
                        fr.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return;
                }
            }
            HwAuthLogger.e(TAG, "readHwCertXml:hwcert.xml not exists");
        } catch (IOException e3222222222) {
            HwAuthLogger.e(TAG, "readHwCertXml:failed to close FileInputStream", e3222222222);
        } catch (Throwable th6) {
            th = th6;
            throw th;
        }
    }

    private HwCertification processCertTag(XmlPullParser parser) {
        try {
            HwCertification cert = new HwCertification();
            readOneCertXml(parser, cert);
            return cert;
        } catch (Exception ex) {
            HwAuthLogger.e(TAG, "processCertTag:IOException", ex);
            return null;
        }
    }

    public synchronized boolean updateHwCert(List<HwCertification> certs) {
        Throwable th;
        File certXml = new File(new File(Environment.getDataDirectory(), "system"), FILE_NAME);
        if (certXml.exists()) {
            boolean deleted = false;
            try {
                deleted = certXml.delete();
            } catch (SecurityException e) {
                HwAuthLogger.e(TAG, "SecurityException when delete certXml in updateHwCert, e is " + e);
            }
            if (!deleted) {
                HwAuthLogger.e(TAG, "canot delete certXml in updateHwCert.");
                return false;
            }
        }
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fis = new FileOutputStream(certXml, false);
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fis, "utf-8");
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, TAG_HWCERT);
                for (HwCertification cert : certs) {
                    writeToXml(out, cert);
                }
                out.endTag(null, TAG_HWCERT);
                out.endDocument();
                fis.flush();
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ex) {
                        HwAuthLogger.e(TAG, "can not close fis in updateHwCert, ex is " + ex);
                    }
                }
                return true;
            } catch (IOException e2) {
                fileOutputStream = fis;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException ex2) {
                        HwAuthLogger.e(TAG, "can not close fis in updateHwCert, ex is " + ex2);
                    }
                }
                return false;
            } catch (Throwable th2) {
                th = th2;
                fileOutputStream = fis;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException ex22) {
                        HwAuthLogger.e(TAG, "can not close fis in updateHwCert, ex is " + ex22);
                    }
                }
                throw th;
            }
        } catch (IOException e3) {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return false;
        } catch (Throwable th3) {
            th = th3;
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void readOneCertXml(XmlPullParser parser, HwCertification cert) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (!(type == 3 || type == 4)) {
                    String tag = parser.getName();
                    IProcessor processor = HwCertificationProcessor.getProcessors(tag);
                    if (processor == null) {
                        HwAuthLogger.e(TAG, "readOneCertXml:processor is null");
                        return;
                    }
                    processor.parseXmlTag(tag, parser, cert);
                    if (processor.parserCert(cert)) {
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        HwAuthLogger.e(TAG, "processCertTag:error, tag is " + tag);
                        return;
                    }
                }
            }
        }
    }

    public static void writeToXml(XmlSerializer out, HwCertification cert) throws IllegalArgumentException, IllegalStateException, IOException {
        out.startTag(null, TAG_CERT);
        out.attribute(null, MemoryConstant.MEM_POLICY_ACTIONNAME, cert.mCertificationData.mPackageName);
        if (!(cert.getVersion() == null || cert.getVersion().isEmpty())) {
            out.startTag(null, HwCertification.KEY_VERSION);
            out.attribute(null, TAG_VALUE, cert.mCertificationData.mVersion);
            out.endTag(null, HwCertification.KEY_VERSION);
        }
        out.startTag(null, HwCertification.KEY_DEVELIOPER);
        out.attribute(null, TAG_VALUE, cert.mCertificationData.mDelveoperKey);
        out.endTag(null, HwCertification.KEY_DEVELIOPER);
        out.startTag(null, HwSecDiagnoseConstant.ANTIMAL_APK_PACKAGE_NAME);
        out.attribute(null, TAG_VALUE, cert.mCertificationData.mPackageName);
        out.endTag(null, HwSecDiagnoseConstant.ANTIMAL_APK_PACKAGE_NAME);
        out.startTag(null, HwCertification.KEY_PERMISSIONS);
        out.attribute(null, TAG_VALUE, cert.mCertificationData.mPermissionsString);
        out.endTag(null, HwCertification.KEY_PERMISSIONS);
        out.startTag(null, HwCertification.KEY_DEVICE_IDS);
        out.attribute(null, TAG_VALUE, cert.mCertificationData.mDeviceIdsString);
        out.endTag(null, HwCertification.KEY_DEVICE_IDS);
        out.startTag(null, HwCertification.KEY_VALID_PERIOD);
        out.attribute(null, TAG_VALUE, cert.mCertificationData.mPeriodString);
        out.endTag(null, HwCertification.KEY_VALID_PERIOD);
        out.startTag(null, HwCertification.KEY_APK_HASH);
        out.attribute(null, TAG_VALUE, cert.mCertificationData.mApkHash);
        out.endTag(null, HwCertification.KEY_APK_HASH);
        if (!(cert.getCertificate() == null || cert.getCertificate().isEmpty())) {
            out.startTag(null, HwCertification.KEY_CERTIFICATE);
            out.attribute(null, TAG_VALUE, cert.mCertificationData.mCertificate);
            out.endTag(null, HwCertification.KEY_CERTIFICATE);
        }
        if (!(cert.getExtenstion() == null || cert.getExtenstion().isEmpty())) {
            out.startTag(null, HwCertification.KEY_EXTENSION);
            out.attribute(null, TAG_VALUE, cert.mCertificationData.mExtenstion);
            out.endTag(null, HwCertification.KEY_EXTENSION);
        }
        out.startTag(null, HwCertification.KEY_SIGNATURE);
        out.attribute(null, TAG_VALUE, cert.mCertificationData.mSignature);
        out.endTag(null, HwCertification.KEY_SIGNATURE);
        out.endTag(null, TAG_CERT);
    }
}

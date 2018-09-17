package com.android.server.pm.auth;

import android.os.Environment;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.pm.auth.processor.HwCertificationProcessor;
import com.android.server.pm.auth.processor.IProcessor;
import com.android.server.pm.auth.util.HwAuthLogger;
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

    /* JADX WARNING: Missing block: B:69:?, code:
            closeStream(r10, r8);
     */
    /* JADX WARNING: Missing block: B:70:0x0147, code:
            r7 = r8;
            r9 = r10;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void readHwCertXml(ConcurrentHashMap<String, HwCertification> certs) {
        Throwable th;
        RuntimeException e;
        Exception e2;
        InputStreamReader fr = null;
        FileInputStream fis = null;
        try {
            File certXml = new File(new File(Environment.getDataDirectory(), "system"), FILE_NAME);
            if (certXml.exists()) {
                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    FileInputStream fis2 = new FileInputStream(certXml);
                    try {
                        InputStreamReader fr2 = new InputStreamReader(fis2, "UTF-8");
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
                                    if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                                        break;
                                    }
                                    boolean shouldSkip = type == 3 || type == 4;
                                    if (!shouldSkip) {
                                        tag = parser.getName();
                                        if (TAG_CERT.equals(tag)) {
                                            HwCertification cert = processCertTag(parser);
                                            if (!(cert == null || cert.getPackageName() == null)) {
                                                certs.put(cert.getPackageName(), cert);
                                            }
                                        } else {
                                            HwAuthLogger.e("HwCertificationManager", "readHwCertXml:unknow tag:" + tag);
                                            XmlUtils.skipCurrentTag(parser);
                                        }
                                    }
                                }
                            } else {
                                HwAuthLogger.e("HwCertificationManager", "readHwCertXml:unexpected tag found: " + tag);
                                try {
                                    closeStream(fr2, fis2);
                                    return;
                                } catch (Throwable th2) {
                                    th = th2;
                                    fis = fis2;
                                    throw th;
                                }
                            }
                        } catch (RuntimeException e3) {
                            e = e3;
                            fis = fis2;
                            fr = fr2;
                        } catch (Exception e4) {
                            e2 = e4;
                            fis = fis2;
                            fr = fr2;
                        } catch (Throwable th3) {
                            th = th3;
                            fis = fis2;
                            fr = fr2;
                        }
                    } catch (RuntimeException e5) {
                        e = e5;
                        fis = fis2;
                        try {
                            HwAuthLogger.e("HwCertificationManager", "readHwCertXml:RuntimeException", e);
                            closeStream(fr, fis);
                            return;
                        } catch (Throwable th4) {
                            th = th4;
                            closeStream(fr, fis);
                            throw th;
                        }
                    } catch (Exception e6) {
                        e2 = e6;
                        fis = fis2;
                        HwAuthLogger.e("HwCertificationManager", "readHwCertXml:IOException", e2);
                        closeStream(fr, fis);
                        return;
                    } catch (Throwable th5) {
                        th = th5;
                        fis = fis2;
                        closeStream(fr, fis);
                        throw th;
                    }
                } catch (RuntimeException e7) {
                    e = e7;
                    HwAuthLogger.e("HwCertificationManager", "readHwCertXml:RuntimeException", e);
                    closeStream(fr, fis);
                    return;
                } catch (Exception e8) {
                    e2 = e8;
                    HwAuthLogger.e("HwCertificationManager", "readHwCertXml:IOException", e2);
                    closeStream(fr, fis);
                    return;
                }
            }
            HwAuthLogger.e("HwCertificationManager", "readHwCertXml:hwcert.xml not exists");
        } catch (Throwable th6) {
            th = th6;
        }
    }

    private void closeStream(InputStreamReader fr, FileInputStream fis) {
        if (fr != null) {
            try {
                fr.close();
            } catch (IOException e) {
                HwAuthLogger.e("HwCertificationManager", "readHwCertXml:failed to close FileReader", e);
            }
        }
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e2) {
                HwAuthLogger.e("HwCertificationManager", "readHwCertXml:failed to close FileInputStream", e2);
            }
        }
    }

    private HwCertification processCertTag(XmlPullParser parser) {
        try {
            HwCertification cert = new HwCertification();
            readOneCertXml(parser, cert);
            return cert;
        } catch (Exception ex) {
            HwAuthLogger.e("HwCertificationManager", "processCertTag:IOException", ex);
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0087 A:{SYNTHETIC, Splitter: B:31:0x0087} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00dd A:{SYNTHETIC, Splitter: B:51:0x00dd} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean updateHwCert(List<HwCertification> certs) {
        Throwable th;
        File certXml = new File(new File(Environment.getDataDirectory(), "system"), FILE_NAME);
        if (certXml.exists()) {
            boolean deleted = false;
            try {
                deleted = certXml.delete();
            } catch (SecurityException e) {
                HwAuthLogger.e("HwCertificationManager", "SecurityException when delete certXml in updateHwCert, e is " + e);
            }
            if (!deleted) {
                HwAuthLogger.e("HwCertificationManager", "canot delete certXml in updateHwCert.");
                return false;
            }
        }
        FileOutputStream fis = null;
        try {
            FileOutputStream fis2 = new FileOutputStream(certXml, false);
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fis2, "utf-8");
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, TAG_HWCERT);
                for (HwCertification cert : certs) {
                    writeToXml(out, cert);
                }
                out.endTag(null, TAG_HWCERT);
                out.endDocument();
                fis2.flush();
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (IOException ex) {
                        HwAuthLogger.e("HwCertificationManager", "can not close fis in updateHwCert, ex is " + ex);
                    }
                }
                return true;
            } catch (IOException e2) {
                fis = fis2;
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ex2) {
                        HwAuthLogger.e("HwCertificationManager", "can not close fis in updateHwCert, ex is " + ex2);
                    }
                }
                return false;
            } catch (Throwable th2) {
                th = th2;
                fis = fis2;
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ex22) {
                        HwAuthLogger.e("HwCertificationManager", "can not close fis in updateHwCert, ex is " + ex22);
                    }
                }
                throw th;
            }
        } catch (IOException e3) {
            if (fis != null) {
            }
            return false;
        } catch (Throwable th3) {
            th = th3;
            if (fis != null) {
            }
            throw th;
        }
    }

    public static void readOneCertXml(XmlPullParser parser, HwCertification cert) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (!(type == 3 || type == 4)) {
                    String tag = parser.getName();
                    IProcessor processor = HwCertificationProcessor.getProcessors(tag);
                    if (processor == null) {
                        HwAuthLogger.e("HwCertificationManager", "readOneCertXml:processor is null");
                        return;
                    }
                    processor.parseXmlTag(tag, parser, cert);
                    if (processor.parserCert(cert)) {
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        HwAuthLogger.e("HwCertificationManager", "processCertTag:error, tag is " + tag);
                        return;
                    }
                }
            }
        }
    }

    public static void writeToXml(XmlSerializer out, HwCertification cert) throws IllegalArgumentException, IllegalStateException, IOException {
        out.startTag(null, TAG_CERT);
        out.attribute(null, "name", cert.mCertificationData.mPackageName);
        if (!(cert.getVersion() == null || (cert.getVersion().isEmpty() ^ 1) == 0)) {
            out.startTag(null, HwCertification.KEY_VERSION);
            out.attribute(null, "value", cert.mCertificationData.mVersion);
            out.endTag(null, HwCertification.KEY_VERSION);
        }
        out.startTag(null, HwCertification.KEY_DEVELIOPER);
        out.attribute(null, "value", cert.mCertificationData.mDelveoperKey);
        out.endTag(null, HwCertification.KEY_DEVELIOPER);
        out.startTag(null, "PackageName");
        out.attribute(null, "value", cert.mCertificationData.mPackageName);
        out.endTag(null, "PackageName");
        out.startTag(null, HwCertification.KEY_PERMISSIONS);
        out.attribute(null, "value", cert.mCertificationData.mPermissionsString);
        out.endTag(null, HwCertification.KEY_PERMISSIONS);
        out.startTag(null, HwCertification.KEY_DEVICE_IDS);
        out.attribute(null, "value", cert.mCertificationData.mDeviceIdsString);
        out.endTag(null, HwCertification.KEY_DEVICE_IDS);
        out.startTag(null, HwCertification.KEY_VALID_PERIOD);
        out.attribute(null, "value", cert.mCertificationData.mPeriodString);
        out.endTag(null, HwCertification.KEY_VALID_PERIOD);
        out.startTag(null, HwCertification.KEY_APK_HASH);
        out.attribute(null, "value", cert.mCertificationData.mApkHash);
        out.endTag(null, HwCertification.KEY_APK_HASH);
        if (!(cert.getCertificate() == null || (cert.getCertificate().isEmpty() ^ 1) == 0)) {
            out.startTag(null, HwCertification.KEY_CERTIFICATE);
            out.attribute(null, "value", cert.mCertificationData.mCertificate);
            out.endTag(null, HwCertification.KEY_CERTIFICATE);
        }
        if (!(cert.getExtenstion() == null || (cert.getExtenstion().isEmpty() ^ 1) == 0)) {
            out.startTag(null, HwCertification.KEY_EXTENSION);
            out.attribute(null, "value", cert.mCertificationData.mExtenstion);
            out.endTag(null, HwCertification.KEY_EXTENSION);
        }
        out.startTag(null, HwCertification.KEY_SIGNATURE);
        out.attribute(null, "value", cert.mCertificationData.mSignature);
        out.endTag(null, HwCertification.KEY_SIGNATURE);
        out.endTag(null, TAG_CERT);
    }
}

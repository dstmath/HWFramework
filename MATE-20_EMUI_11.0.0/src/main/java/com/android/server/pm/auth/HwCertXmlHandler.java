package com.android.server.pm.auth;

import android.os.Environment;
import android.text.TextUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.processor.HwCertificationProcessor;
import com.android.server.pm.auth.processor.IProcessor;
import com.android.server.pm.auth.util.HwAuthLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class HwCertXmlHandler {
    private static final String BLACKLIST_FILE_NAME = "hwCertBlacklist.xml";
    private static final String FILE_NAME = "hwcert.xml";
    private static final String SYSTEM_DIR = "system";
    private static final String TAG = "HwCertificationManager";
    private static final String TAG_BLACKLIST = "blacklist";
    private static final String TAG_CERT = "cert";
    private static final String TAG_HWCERT = "hwcerts";
    private static final String TAG_SIGNATURE = "signature";
    public static final String TAG_VALUE = "value";

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0080, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0081, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0084, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0087, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0088, code lost:
        $closeResource(r4, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x008b, code lost:
        throw r5;
     */
    public void readHwCertXml(ConcurrentHashMap<String, HwCertification> hwCerts) {
        if (hwCerts != null) {
            File certXml = new File(new File(Environment.getDataDirectory(), SYSTEM_DIR), FILE_NAME);
            if (!certXml.exists()) {
                HwAuthLogger.error("HwCertificationManager", "readHwCertXml hwcert.xml not exists!");
                return;
            }
            try {
                FileInputStream fileInputStream = new FileInputStream(certXml);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(inputStreamReader);
                int type = parser.next();
                while (type != 1 && type != 2) {
                    type = parser.next();
                }
                String tag = parser.getName();
                if (!TAG_HWCERT.equals(tag)) {
                    HwAuthLogger.error("HwCertificationManager", "readHwCertXml unexpected tag found:" + tag);
                    $closeResource(null, inputStreamReader);
                    $closeResource(null, fileInputStream);
                    return;
                }
                readHwCert(parser, hwCerts);
                $closeResource(null, inputStreamReader);
                $closeResource(null, fileInputStream);
            } catch (XmlPullParserException e) {
                HwAuthLogger.error("HwCertificationManager", "readHwCertXml XmlPullParserException!");
            } catch (FileNotFoundException e2) {
                HwAuthLogger.error("HwCertificationManager", "readHwCertXml FileNotFoundException!");
            } catch (IOException e3) {
                HwAuthLogger.error("HwCertificationManager", "readHwCertXml IOException!");
            } catch (Exception e4) {
                HwAuthLogger.error("HwCertificationManager", "readHwCertXml Exception!");
            }
        }
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

    private void readHwCert(XmlPullParser parser, ConcurrentHashMap<String, HwCertification> hwCerts) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type = parser.next();
        while (true) {
            boolean shouldSkip = true;
            if (type == 1) {
                return;
            }
            if (type != 3 || parser.getDepth() > outerDepth) {
                if (!(type == 3 || type == 4)) {
                    shouldSkip = false;
                }
                if (shouldSkip) {
                    type = parser.next();
                } else {
                    String tag = parser.getName();
                    if (TAG_CERT.equals(tag)) {
                        HwCertification hwCert = processCertTag(parser);
                        if (!(hwCert == null || hwCert.getPackageName() == null)) {
                            hwCerts.put(hwCert.getPackageName(), hwCert);
                        }
                    } else {
                        HwAuthLogger.error("HwCertificationManager", "readHwCert unknown tag:" + tag);
                        XmlUtils.skipCurrentTag(parser);
                    }
                    type = parser.next();
                }
            } else {
                return;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x007d, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x007e, code lost:
        $closeResource(r0, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0081, code lost:
        throw r1;
     */
    public boolean updateMdmCertBlacklist(List<String> signatures) {
        if (signatures == null) {
            return false;
        }
        File blackListXml = new File(new File(Environment.getDataDirectory(), SYSTEM_DIR), BLACKLIST_FILE_NAME);
        if (blackListXml.exists() && !blackListXml.delete()) {
            return false;
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(blackListXml, false);
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            out.startTag(null, TAG_BLACKLIST);
            int size = signatures.size();
            for (int i = 0; i < size; i++) {
                out.startTag(null, TAG_SIGNATURE);
                out.attribute(null, "value", signatures.get(i));
                out.endTag(null, TAG_SIGNATURE);
            }
            out.endTag(null, TAG_BLACKLIST);
            out.endDocument();
            fileOutputStream.flush();
            HwCertificationManager.getInstance().updateMdmCertBlacklist();
            $closeResource(null, fileOutputStream);
            return true;
        } catch (IOException e) {
            HwAuthLogger.error("HwCertificationManager", "updateMdmCertBlacklist failed IOException!");
            return false;
        } catch (IllegalArgumentException e2) {
            HwAuthLogger.error("HwCertificationManager", "updateMdmCertBlacklist failed IllegalArgumentException!");
            return false;
        } catch (IllegalStateException e3) {
            HwAuthLogger.error("HwCertificationManager", "updateMdmCertBlacklist failed IllegalStateException!");
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0099, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x009a, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x009d, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a0, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a1, code lost:
        $closeResource(r4, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a4, code lost:
        throw r5;
     */
    public void readMdmCertBlacklist(List<String> signatures) {
        if (signatures != null) {
            File blacklistFile = new File(new File(Environment.getDataDirectory(), SYSTEM_DIR), BLACKLIST_FILE_NAME);
            if (!blacklistFile.exists()) {
                HwAuthLogger.error("HwCertificationManager", "BlackList file not found!");
                return;
            }
            try {
                FileInputStream fileInputStream = new FileInputStream(blacklistFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(inputStreamReader);
                int eventType = parser.getEventType();
                while (eventType != 1) {
                    if (eventType == 2) {
                        if (TAG_SIGNATURE.equals(parser.getName())) {
                            String attributeValue = parser.getAttributeValue(null, "value");
                            if (TextUtils.isEmpty(attributeValue)) {
                                eventType = parser.next();
                            } else {
                                signatures.add(attributeValue);
                                eventType = parser.next();
                            }
                        }
                    }
                    eventType = parser.next();
                }
                HwAuthLogger.info("HwCertificationManager", "the size of signatureBlackList is " + signatures.size());
                $closeResource(null, inputStreamReader);
                $closeResource(null, fileInputStream);
            } catch (XmlPullParserException e) {
                HwAuthLogger.error("HwCertificationManager", "readMdmCertBlacklist failed XmlPullParserException!");
            } catch (FileNotFoundException e2) {
                HwAuthLogger.error("HwCertificationManager", "readMdmCertBlacklist failed FileNotFoundException!");
            } catch (UnsupportedEncodingException e3) {
                HwAuthLogger.error("HwCertificationManager", "readMdmCertBlacklist failed UnsupportedEncodingException!");
            } catch (IOException e4) {
                HwAuthLogger.error("HwCertificationManager", "readMdmCertBlacklist failed IOException!");
            }
        }
    }

    private HwCertification processCertTag(XmlPullParser parser) {
        HwCertification hwCert = new HwCertification();
        try {
            readOneCertXml(parser, hwCert);
            return hwCert;
        } catch (XmlPullParserException e) {
            HwAuthLogger.error("HwCertificationManager", "processCertTag XmlPullParserException!");
            return null;
        } catch (Exception e2) {
            HwAuthLogger.error("HwCertificationManager", "processCertTag Exception!");
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0078, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0079, code lost:
        $closeResource(r0, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x007c, code lost:
        throw r6;
     */
    public boolean updateHwCert(List<HwCertification> certs) {
        if (certs == null) {
            return false;
        }
        File certXml = new File(new File(Environment.getDataDirectory(), SYSTEM_DIR), FILE_NAME);
        if (certXml.exists()) {
            boolean isDeleted = false;
            try {
                isDeleted = certXml.delete();
            } catch (SecurityException e) {
                HwAuthLogger.error("HwCertificationManager", "SecurityException when delete certXml in updateHwCert!");
            }
            if (!isDeleted) {
                HwAuthLogger.error("HwCertificationManager", "can not delete certXml in updateHwCert!");
                return false;
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(certXml, false);
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            out.startTag(null, TAG_HWCERT);
            int size = certs.size();
            for (int i = 0; i < size; i++) {
                writeToXml(out, certs.get(i));
            }
            out.endTag(null, TAG_HWCERT);
            out.endDocument();
            fileOutputStream.flush();
            $closeResource(null, fileOutputStream);
            return true;
        } catch (IOException e2) {
            HwAuthLogger.error("HwCertificationManager", "updateHwCert IOException!");
            return false;
        }
    }

    private void readOneCertXml(XmlPullParser parser, HwCertification hwCert) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type = parser.next();
        while (type != 1) {
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (type == 3 || type == 4) {
                type = parser.next();
            } else {
                String tag = parser.getName();
                IProcessor processor = HwCertificationProcessor.getProcessor(tag);
                if (processor == null) {
                    HwAuthLogger.error("HwCertificationManager", "readOneCertXml processor is null!");
                    return;
                }
                processor.parseXmlTag(tag, parser, hwCert);
                if (!processor.parseCert(hwCert)) {
                    HwAuthLogger.error("HwCertificationManager", "processCertTag error, tag is " + tag);
                    return;
                }
                XmlUtils.skipCurrentTag(parser);
                type = parser.next();
            }
        }
    }

    private void writeToXml(XmlSerializer out, HwCertification hwCert) throws IllegalArgumentException, IllegalStateException, IOException {
        HwCertification.CertificationData certData = hwCert.getCertificationData();
        if (certData != null) {
            out.startTag(null, TAG_CERT);
            out.attribute(null, "name", certData.getPackageName());
            if (hwCert.getVersion() != null && !hwCert.getVersion().isEmpty()) {
                out.startTag(null, HwCertification.KEY_VERSION);
                out.attribute(null, "value", certData.getVersion());
                out.endTag(null, HwCertification.KEY_VERSION);
            }
            out.startTag(null, HwCertification.KEY_DEVELOPER);
            out.attribute(null, "value", certData.getDeveloperKey());
            out.endTag(null, HwCertification.KEY_DEVELOPER);
            out.startTag(null, HwCertification.KEY_PACKAGE_NAME);
            out.attribute(null, "value", certData.getPackageName());
            out.endTag(null, HwCertification.KEY_PACKAGE_NAME);
            out.startTag(null, HwCertification.KEY_PERMISSIONS);
            out.attribute(null, "value", certData.getPermissionsString());
            out.endTag(null, HwCertification.KEY_PERMISSIONS);
            out.startTag(null, HwCertification.KEY_DEVICE_IDS);
            out.attribute(null, "value", certData.getDeviceIdsString());
            out.endTag(null, HwCertification.KEY_DEVICE_IDS);
            out.startTag(null, HwCertification.KEY_VALID_PERIOD);
            out.attribute(null, "value", certData.getPeriodString());
            out.endTag(null, HwCertification.KEY_VALID_PERIOD);
            out.startTag(null, HwCertification.KEY_APK_HASH);
            out.attribute(null, "value", certData.getApkHash());
            out.endTag(null, HwCertification.KEY_APK_HASH);
            if (!TextUtils.isEmpty(hwCert.getCertificate())) {
                out.startTag(null, HwCertification.KEY_CERTIFICATE);
                out.attribute(null, "value", certData.getCertificate());
                out.endTag(null, HwCertification.KEY_CERTIFICATE);
            }
            if (!TextUtils.isEmpty(hwCert.getExtension())) {
                out.startTag(null, HwCertification.KEY_EXTENSION);
                out.attribute(null, "value", certData.getExtension());
                out.endTag(null, HwCertification.KEY_EXTENSION);
            }
            writeSignaturesToXml(out, certData);
            out.endTag(null, TAG_CERT);
        }
    }

    private void writeSignaturesToXml(XmlSerializer out, HwCertification.CertificationData certData) throws IllegalArgumentException, IllegalStateException, IOException {
        out.startTag(null, HwCertification.KEY_SIGNATURE);
        out.attribute(null, "value", certData.getSignatureV1());
        out.endTag(null, HwCertification.KEY_SIGNATURE);
        if (!TextUtils.isEmpty(certData.getSignatureV2())) {
            out.startTag(null, HwCertification.KEY_SIGNATURE2);
            out.attribute(null, "value", certData.getSignatureV2());
            out.endTag(null, HwCertification.KEY_SIGNATURE2);
        }
        if (!TextUtils.isEmpty(certData.getSignatureV3())) {
            out.startTag(null, HwCertification.KEY_SIGNATURE3);
            out.attribute(null, "value", certData.getSignatureV3());
            out.endTag(null, HwCertification.KEY_SIGNATURE3);
        }
    }
}

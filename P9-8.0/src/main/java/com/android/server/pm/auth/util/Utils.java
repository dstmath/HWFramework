package com.android.server.pm.auth.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Utils {
    private static final int BUFFER_LENGTH = 4096;
    public static final String CERT_NAME = "META-INF/HUAWEI.CER";
    private static final String COMMON_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int CR_LF_LENGTH = 2;
    private static final String HW_CER_NAME = "HUAWEI.CER";
    private static final String HW_CER_TAG = "Name: META-INF/HUAWEI.CER";
    private static final String LINE_SEPERATOR = "\r\n";
    public static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
    private static final String SF_ATTRIBUTE_ANDROID_APK_SIGNED_NAME = "X-Android-APK-Signed:";
    private static final String SF_ATTRIBUTE_ANDROID_APK_SIGNED_VALUE = "2";
    public static final String SF_CERT_NAME = "META-INF/CERT.SF";
    public static final String TAG = "HwCertificationManager";

    /* JADX WARNING: Removed duplicated region for block: B:35:0x0069 A:{SYNTHETIC, Splitter: B:35:0x0069} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0080  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x006e  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x005d A:{SYNTHETIC, Splitter: B:27:0x005d} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x007a A:{SYNTHETIC, Splitter: B:44:0x007a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] getManifestFile(File apkFile) {
        Throwable th;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        ZipFile zipFile = null;
        InputStream inputStream = null;
        boolean catchFlag = false;
        try {
            ZipFile zipFile2 = new ZipFile(apkFile);
            try {
                byte[] bArr;
                Enumeration<?> enumeration = zipFile2.entries();
                while (enumeration.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
                    if (!zipEntry.isDirectory() && "META-INF/MANIFEST.MF".equals(zipEntry.getName())) {
                        inputStream = zipFile2.getInputStream(zipEntry);
                        while (true) {
                            int length = inputStream.read(b);
                            if (length <= 0) {
                                break;
                            }
                            os.write(b, 0, length);
                        }
                        catchFlag = true;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (zipFile2 != null) {
                            try {
                                zipFile2.close();
                            } catch (IOException e) {
                            }
                        }
                        if (catchFlag) {
                            bArr = new byte[0];
                        } else {
                            bArr = os.toByteArray();
                        }
                        return bArr;
                    }
                }
                if (zipFile2 != null) {
                }
                if (catchFlag) {
                }
                return bArr;
            } catch (IOException e2) {
                zipFile = zipFile2;
                try {
                    HwAuthLogger.e("HwCertificationManager", "IOException in getManifestFile() in Utils.java ");
                    if (zipFile != null) {
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                zipFile = zipFile2;
                if (zipFile != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            HwAuthLogger.e("HwCertificationManager", "IOException in getManifestFile() in Utils.java ");
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e5) {
                }
            }
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x009a A:{SYNTHETIC, Splitter: B:34:0x009a} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x009f A:{SYNTHETIC, Splitter: B:37:0x009f} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00a4 A:{SYNTHETIC, Splitter: B:40:0x00a4} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a9 A:{SYNTHETIC, Splitter: B:43:0x00a9} */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01c1 A:{SYNTHETIC, Splitter: B:81:0x01c1} */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x01c6 A:{SYNTHETIC, Splitter: B:84:0x01c6} */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01cb A:{SYNTHETIC, Splitter: B:87:0x01cb} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01d0 A:{SYNTHETIC, Splitter: B:90:0x01d0} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x009a A:{SYNTHETIC, Splitter: B:34:0x009a} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x009f A:{SYNTHETIC, Splitter: B:37:0x009f} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00a4 A:{SYNTHETIC, Splitter: B:40:0x00a4} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a9 A:{SYNTHETIC, Splitter: B:43:0x00a9} */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01c1 A:{SYNTHETIC, Splitter: B:81:0x01c1} */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x01c6 A:{SYNTHETIC, Splitter: B:84:0x01c6} */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01cb A:{SYNTHETIC, Splitter: B:87:0x01cb} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01d0 A:{SYNTHETIC, Splitter: B:90:0x01d0} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x009a A:{SYNTHETIC, Splitter: B:34:0x009a} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x009f A:{SYNTHETIC, Splitter: B:37:0x009f} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00a4 A:{SYNTHETIC, Splitter: B:40:0x00a4} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a9 A:{SYNTHETIC, Splitter: B:43:0x00a9} */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01c1 A:{SYNTHETIC, Splitter: B:81:0x01c1} */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x01c6 A:{SYNTHETIC, Splitter: B:84:0x01c6} */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01cb A:{SYNTHETIC, Splitter: B:87:0x01cb} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01d0 A:{SYNTHETIC, Splitter: B:90:0x01d0} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x009a A:{SYNTHETIC, Splitter: B:34:0x009a} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x009f A:{SYNTHETIC, Splitter: B:37:0x009f} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00a4 A:{SYNTHETIC, Splitter: B:40:0x00a4} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a9 A:{SYNTHETIC, Splitter: B:43:0x00a9} */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01c1 A:{SYNTHETIC, Splitter: B:81:0x01c1} */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x01c6 A:{SYNTHETIC, Splitter: B:84:0x01c6} */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01cb A:{SYNTHETIC, Splitter: B:87:0x01cb} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01d0 A:{SYNTHETIC, Splitter: B:90:0x01d0} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] getManifestFileNew(File apkFile) {
        IOException e;
        Throwable th;
        BufferedOutputStream bufOs = null;
        BufferedInputStream bufIn = null;
        byte[] b = new byte[4096];
        ZipFile zipFile = null;
        InputStream inputStream = null;
        try {
            ZipFile zipFile2 = new ZipFile(apkFile);
            try {
                ZipEntry ManifestEntry = zipFile2.getEntry("META-INF/MANIFEST.MF");
                if (ManifestEntry == null || ManifestEntry.isDirectory()) {
                    HwAuthLogger.e("bailong", "can not find manifest.mf file.");
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (IOException e2) {
                            HwAuthLogger.e("HwCertificationManager", "can not close zipFile, e is " + e2);
                        }
                    }
                    return null;
                }
                inputStream = zipFile2.getInputStream(ManifestEntry);
                BufferedInputStream bufIn2 = new BufferedInputStream(inputStream);
                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    try {
                        BufferedOutputStream bufOs2 = new BufferedOutputStream(os);
                        while (true) {
                            try {
                                int readLength = bufIn2.read(b, 0, 4096);
                                if (readLength <= 0) {
                                    break;
                                }
                                bufOs2.write(b, 0, readLength);
                            } catch (IOException e3) {
                                e2 = e3;
                                zipFile = zipFile2;
                                bufIn = bufIn2;
                                bufOs = bufOs2;
                                ByteArrayOutputStream byteArrayOutputStream = os;
                                try {
                                    HwAuthLogger.e("HwCertificationManager", "IOException in getManifestFileNew, e is " + e2);
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (IOException e22) {
                                            HwAuthLogger.e("HwCertificationManager", "can not close inputStream, e is " + e22);
                                        }
                                    }
                                    if (bufIn != null) {
                                        try {
                                            bufIn.close();
                                        } catch (IOException e222) {
                                            HwAuthLogger.e("HwCertificationManager", "can not close bufIn, e is " + e222);
                                        }
                                    }
                                    if (bufOs != null) {
                                        try {
                                            bufOs.close();
                                        } catch (IOException e2222) {
                                            HwAuthLogger.e("HwCertificationManager", "can not close bufOs, e is " + e2222);
                                        }
                                    }
                                    if (zipFile != null) {
                                        try {
                                            zipFile.close();
                                        } catch (IOException e22222) {
                                            HwAuthLogger.e("HwCertificationManager", "can not close zipFile, e is " + e22222);
                                        }
                                    }
                                    return null;
                                } catch (Throwable th2) {
                                    th = th2;
                                    if (inputStream != null) {
                                    }
                                    if (bufIn != null) {
                                    }
                                    if (bufOs != null) {
                                    }
                                    if (zipFile != null) {
                                    }
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                zipFile = zipFile2;
                                bufIn = bufIn2;
                                bufOs = bufOs2;
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e222222) {
                                        HwAuthLogger.e("HwCertificationManager", "can not close inputStream, e is " + e222222);
                                    }
                                }
                                if (bufIn != null) {
                                    try {
                                        bufIn.close();
                                    } catch (IOException e2222222) {
                                        HwAuthLogger.e("HwCertificationManager", "can not close bufIn, e is " + e2222222);
                                    }
                                }
                                if (bufOs != null) {
                                    try {
                                        bufOs.close();
                                    } catch (IOException e22222222) {
                                        HwAuthLogger.e("HwCertificationManager", "can not close bufOs, e is " + e22222222);
                                    }
                                }
                                if (zipFile != null) {
                                    try {
                                        zipFile.close();
                                    } catch (IOException e222222222) {
                                        HwAuthLogger.e("HwCertificationManager", "can not close zipFile, e is " + e222222222);
                                    }
                                }
                                throw th;
                            }
                        }
                        bufOs2.flush();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e2222222222) {
                                HwAuthLogger.e("HwCertificationManager", "can not close inputStream, e is " + e2222222222);
                            }
                        }
                        if (bufIn2 != null) {
                            try {
                                bufIn2.close();
                            } catch (IOException e22222222222) {
                                HwAuthLogger.e("HwCertificationManager", "can not close bufIn, e is " + e22222222222);
                            }
                        }
                        if (bufOs2 != null) {
                            try {
                                bufOs2.close();
                            } catch (IOException e222222222222) {
                                HwAuthLogger.e("HwCertificationManager", "can not close bufOs, e is " + e222222222222);
                            }
                        }
                        if (zipFile2 != null) {
                            try {
                                zipFile2.close();
                            } catch (IOException e2222222222222) {
                                HwAuthLogger.e("HwCertificationManager", "can not close zipFile, e is " + e2222222222222);
                            }
                        }
                        return os.toByteArray();
                    } catch (IOException e4) {
                        e2222222222222 = e4;
                        zipFile = zipFile2;
                        bufIn = bufIn2;
                        HwAuthLogger.e("HwCertificationManager", "IOException in getManifestFileNew, e is " + e2222222222222);
                        if (inputStream != null) {
                        }
                        if (bufIn != null) {
                        }
                        if (bufOs != null) {
                        }
                        if (zipFile != null) {
                        }
                        return null;
                    } catch (Throwable th4) {
                        th = th4;
                        zipFile = zipFile2;
                        bufIn = bufIn2;
                        if (inputStream != null) {
                        }
                        if (bufIn != null) {
                        }
                        if (bufOs != null) {
                        }
                        if (zipFile != null) {
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e2222222222222 = e5;
                    zipFile = zipFile2;
                    bufIn = bufIn2;
                    HwAuthLogger.e("HwCertificationManager", "IOException in getManifestFileNew, e is " + e2222222222222);
                    if (inputStream != null) {
                    }
                    if (bufIn != null) {
                    }
                    if (bufOs != null) {
                    }
                    if (zipFile != null) {
                    }
                    return null;
                } catch (Throwable th5) {
                    th = th5;
                    zipFile = zipFile2;
                    bufIn = bufIn2;
                    if (inputStream != null) {
                    }
                    if (bufIn != null) {
                    }
                    if (bufOs != null) {
                    }
                    if (zipFile != null) {
                    }
                    throw th;
                }
            } catch (IOException e6) {
                e2222222222222 = e6;
                zipFile = zipFile2;
                HwAuthLogger.e("HwCertificationManager", "IOException in getManifestFileNew, e is " + e2222222222222);
                if (inputStream != null) {
                }
                if (bufIn != null) {
                }
                if (bufOs != null) {
                }
                if (zipFile != null) {
                }
                return null;
            } catch (Throwable th6) {
                th = th6;
                zipFile = zipFile2;
                if (inputStream != null) {
                }
                if (bufIn != null) {
                }
                if (bufOs != null) {
                }
                if (zipFile != null) {
                }
                throw th;
            }
        } catch (IOException e7) {
            e2222222222222 = e7;
            HwAuthLogger.e("HwCertificationManager", "IOException in getManifestFileNew, e is " + e2222222222222);
            if (inputStream != null) {
            }
            if (bufIn != null) {
            }
            if (bufOs != null) {
            }
            if (zipFile != null) {
            }
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:60:0x0126 A:{SYNTHETIC, Splitter: B:60:0x0126} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x012b A:{SYNTHETIC, Splitter: B:63:0x012b} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0130 A:{SYNTHETIC, Splitter: B:66:0x0130} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0052 A:{SYNTHETIC, Splitter: B:23:0x0052} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0057 A:{SYNTHETIC, Splitter: B:26:0x0057} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x005c A:{SYNTHETIC, Splitter: B:29:0x005c} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0052 A:{SYNTHETIC, Splitter: B:23:0x0052} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0057 A:{SYNTHETIC, Splitter: B:26:0x0057} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x005c A:{SYNTHETIC, Splitter: B:29:0x005c} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0126 A:{SYNTHETIC, Splitter: B:60:0x0126} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x012b A:{SYNTHETIC, Splitter: B:63:0x012b} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0130 A:{SYNTHETIC, Splitter: B:66:0x0130} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0052 A:{SYNTHETIC, Splitter: B:23:0x0052} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0057 A:{SYNTHETIC, Splitter: B:26:0x0057} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x005c A:{SYNTHETIC, Splitter: B:29:0x005c} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0126 A:{SYNTHETIC, Splitter: B:60:0x0126} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x012b A:{SYNTHETIC, Splitter: B:63:0x012b} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0130 A:{SYNTHETIC, Splitter: B:66:0x0130} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] getManifestFileNew(ZipFile zfile, ZipEntry ManifestEntry) {
        IOException e;
        Throwable th;
        if (zfile == null || ManifestEntry == null) {
            return new byte[0];
        }
        BufferedOutputStream bufOs = null;
        BufferedInputStream bufIn = null;
        byte[] b = new byte[4096];
        InputStream inputStream = null;
        try {
            ByteArrayOutputStream os;
            inputStream = zfile.getInputStream(ManifestEntry);
            BufferedInputStream bufIn2 = new BufferedInputStream(inputStream);
            try {
                os = new ByteArrayOutputStream();
            } catch (IOException e2) {
                e = e2;
                bufIn = bufIn2;
                try {
                    HwAuthLogger.e("HwCertificationManager", "IOException in getManifestFileNew, e is " + e);
                    if (inputStream != null) {
                    }
                    if (bufIn != null) {
                    }
                    if (bufOs != null) {
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e3) {
                            HwAuthLogger.e("HwCertificationManager", "can not close inputStream, e is " + e3);
                        }
                    }
                    if (bufIn != null) {
                        try {
                            bufIn.close();
                        } catch (IOException e32) {
                            HwAuthLogger.e("HwCertificationManager", "can not close bufIn, e is " + e32);
                        }
                    }
                    if (bufOs != null) {
                        try {
                            bufOs.close();
                        } catch (IOException e322) {
                            HwAuthLogger.e("HwCertificationManager", "can not close bufOs, e is " + e322);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufIn = bufIn2;
                if (inputStream != null) {
                }
                if (bufIn != null) {
                }
                if (bufOs != null) {
                }
                throw th;
            }
            try {
                BufferedOutputStream bufOs2 = new BufferedOutputStream(os);
                while (true) {
                    try {
                        int readLength = bufIn2.read(b, 0, 4096);
                        if (readLength <= 0) {
                            break;
                        }
                        bufOs2.write(b, 0, readLength);
                    } catch (IOException e4) {
                        e322 = e4;
                        bufIn = bufIn2;
                        bufOs = bufOs2;
                        ByteArrayOutputStream byteArrayOutputStream = os;
                        HwAuthLogger.e("HwCertificationManager", "IOException in getManifestFileNew, e is " + e322);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e3222) {
                                HwAuthLogger.e("HwCertificationManager", "can not close inputStream, e is " + e3222);
                            }
                        }
                        if (bufIn != null) {
                            try {
                                bufIn.close();
                            } catch (IOException e32222) {
                                HwAuthLogger.e("HwCertificationManager", "can not close bufIn, e is " + e32222);
                            }
                        }
                        if (bufOs != null) {
                            try {
                                bufOs.close();
                            } catch (IOException e322222) {
                                HwAuthLogger.e("HwCertificationManager", "can not close bufOs, e is " + e322222);
                            }
                        }
                        return null;
                    } catch (Throwable th4) {
                        th = th4;
                        bufIn = bufIn2;
                        bufOs = bufOs2;
                        if (inputStream != null) {
                        }
                        if (bufIn != null) {
                        }
                        if (bufOs != null) {
                        }
                        throw th;
                    }
                }
                bufOs2.flush();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3222222) {
                        HwAuthLogger.e("HwCertificationManager", "can not close inputStream, e is " + e3222222);
                    }
                }
                if (bufIn2 != null) {
                    try {
                        bufIn2.close();
                    } catch (IOException e32222222) {
                        HwAuthLogger.e("HwCertificationManager", "can not close bufIn, e is " + e32222222);
                    }
                }
                if (bufOs2 != null) {
                    try {
                        bufOs2.close();
                    } catch (IOException e322222222) {
                        HwAuthLogger.e("HwCertificationManager", "can not close bufOs, e is " + e322222222);
                    }
                }
                return os.toByteArray();
            } catch (IOException e5) {
                e322222222 = e5;
                bufIn = bufIn2;
                HwAuthLogger.e("HwCertificationManager", "IOException in getManifestFileNew, e is " + e322222222);
                if (inputStream != null) {
                }
                if (bufIn != null) {
                }
                if (bufOs != null) {
                }
                return null;
            } catch (Throwable th5) {
                th = th5;
                bufIn = bufIn2;
                if (inputStream != null) {
                }
                if (bufIn != null) {
                }
                if (bufOs != null) {
                }
                throw th;
            }
        } catch (IOException e6) {
            e322222222 = e6;
            HwAuthLogger.e("HwCertificationManager", "IOException in getManifestFileNew, e is " + e322222222);
            if (inputStream != null) {
            }
            if (bufIn != null) {
            }
            if (bufOs != null) {
            }
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x009c A:{SYNTHETIC, Splitter: B:39:0x009c} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00a1 A:{SYNTHETIC, Splitter: B:42:0x00a1} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00be A:{SYNTHETIC, Splitter: B:50:0x00be} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00c3 A:{SYNTHETIC, Splitter: B:53:0x00c3} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isUsingSignatureSchemaV2(ZipFile zFile, ZipEntry entry) {
        IOException e;
        Throwable th;
        if (zFile == null || entry == null || entry.isDirectory()) {
            HwAuthLogger.e("HwCertificationManager", "isUsingSignatureSchemaV2 input is null");
            return false;
        }
        boolean catchFlag = false;
        InputStream inputStream = null;
        BufferedReader br = null;
        try {
            inputStream = zFile.getInputStream(entry);
            BufferedReader br2 = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            int i = 0;
            while (i < 10) {
                try {
                    String line = br2.readLine();
                    if (line == null) {
                        break;
                    } else if (line.startsWith(SF_ATTRIBUTE_ANDROID_APK_SIGNED_NAME)) {
                        String[] attr = line.split(":");
                        if (attr.length == 2 && attr[1].trim().equals("2")) {
                            catchFlag = true;
                        }
                    } else {
                        i++;
                    }
                } catch (IOException e2) {
                    e = e2;
                    br = br2;
                    try {
                        HwAuthLogger.e("HwCertificationManager", "IOException happened in isUsingSignatureSchemaV2, e is" + e);
                        if (br != null) {
                        }
                        if (inputStream != null) {
                        }
                        return catchFlag;
                    } catch (Throwable th2) {
                        th = th2;
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e3) {
                                HwAuthLogger.e("HwCertificationManager", "can not close br in isUsingSignatureSchemaV2");
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e4) {
                                HwAuthLogger.e("HwCertificationManager", "can not close inputStream in isUsingSignatureSchemaV2");
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    br = br2;
                    if (br != null) {
                    }
                    if (inputStream != null) {
                    }
                    throw th;
                }
            }
            if (br2 != null) {
                try {
                    br2.close();
                } catch (IOException e5) {
                    HwAuthLogger.e("HwCertificationManager", "can not close br in isUsingSignatureSchemaV2");
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                    HwAuthLogger.e("HwCertificationManager", "can not close inputStream in isUsingSignatureSchemaV2");
                }
            }
            br = br2;
        } catch (IOException e7) {
            e = e7;
            HwAuthLogger.e("HwCertificationManager", "IOException happened in isUsingSignatureSchemaV2, e is" + e);
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e8) {
                    HwAuthLogger.e("HwCertificationManager", "can not close br in isUsingSignatureSchemaV2");
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e9) {
                    HwAuthLogger.e("HwCertificationManager", "can not close inputStream in isUsingSignatureSchemaV2");
                }
            }
            return catchFlag;
        }
        return catchFlag;
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x0129 A:{SYNTHETIC, Splitter: B:44:0x0129} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x012e A:{SYNTHETIC, Splitter: B:47:0x012e} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0133 A:{SYNTHETIC, Splitter: B:50:0x0133} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0129 A:{SYNTHETIC, Splitter: B:44:0x0129} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x012e A:{SYNTHETIC, Splitter: B:47:0x012e} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0133 A:{SYNTHETIC, Splitter: B:50:0x0133} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x01e2 A:{SYNTHETIC, Splitter: B:79:0x01e2} */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x01e7 A:{SYNTHETIC, Splitter: B:82:0x01e7} */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01ec A:{SYNTHETIC, Splitter: B:85:0x01ec} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0129 A:{SYNTHETIC, Splitter: B:44:0x0129} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x012e A:{SYNTHETIC, Splitter: B:47:0x012e} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0133 A:{SYNTHETIC, Splitter: B:50:0x0133} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x01e2 A:{SYNTHETIC, Splitter: B:79:0x01e2} */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x01e7 A:{SYNTHETIC, Splitter: B:82:0x01e7} */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01ec A:{SYNTHETIC, Splitter: B:85:0x01ec} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0129 A:{SYNTHETIC, Splitter: B:44:0x0129} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x012e A:{SYNTHETIC, Splitter: B:47:0x012e} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0133 A:{SYNTHETIC, Splitter: B:50:0x0133} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x01e2 A:{SYNTHETIC, Splitter: B:79:0x01e2} */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x01e7 A:{SYNTHETIC, Splitter: B:82:0x01e7} */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01ec A:{SYNTHETIC, Splitter: B:85:0x01ec} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x01e2 A:{SYNTHETIC, Splitter: B:79:0x01e2} */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x01e7 A:{SYNTHETIC, Splitter: B:82:0x01e7} */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01ec A:{SYNTHETIC, Splitter: B:85:0x01ec} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @SuppressLint({"PreferForInArrayList"})
    public static byte[] readManifestAndSkipHwTag(byte[] manifest) {
        IOException e;
        byte[] bArr;
        Throwable th;
        ByteArrayOutputStream byteArrayOutputStream;
        ByteArrayInputStream byteIn = null;
        BufferedWriter bufferWriter = null;
        BufferedReader bufferReader = null;
        ArrayList<String> contentsList = new ArrayList();
        boolean findHwCer = false;
        if (manifest == null) {
            HwAuthLogger.e("HwCertificationManager", "manifest is null in readManifestAndSkipHwTag.");
            return new byte[0];
        }
        try {
            BufferedWriter bufferWriter2;
            long readbegin = System.currentTimeMillis();
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try {
                bufferWriter2 = new BufferedWriter(new OutputStreamWriter(byteOut, "UTF-8"));
            } catch (IOException e2) {
                e = e2;
                try {
                    HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, e is " + e);
                    bArr = new byte[0];
                    if (bufferWriter != null) {
                    }
                    if (byteIn != null) {
                    }
                    if (bufferReader != null) {
                    }
                    return bArr;
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferWriter != null) {
                    }
                    if (byteIn != null) {
                    }
                    if (bufferReader != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                byteArrayOutputStream = byteOut;
                if (bufferWriter != null) {
                }
                if (byteIn != null) {
                }
                if (bufferReader != null) {
                }
                throw th;
            }
            try {
                ByteArrayInputStream byteIn2 = new ByteArrayInputStream(manifest);
                try {
                    BufferedReader bufferReader2 = new BufferedReader(new InputStreamReader(byteIn2, "UTF-8"));
                    try {
                        int hwcerLen = HW_CER_TAG.length();
                        while (true) {
                            String line = bufferReader2.readLine();
                            if (line == null) {
                                break;
                            }
                            if (hwcerLen == line.length() && (findHwCer ^ 1) != 0 && line.charAt(hwcerLen - 3) == 'C' && line.charAt(hwcerLen - 2) == 'E' && line.charAt(hwcerLen - 1) == 'R' && line.lastIndexOf(HW_CER_NAME) != -1) {
                                String nextName;
                                findHwCer = true;
                                HwAuthLogger.w("HwCertificationManager", "find HwCer tag");
                                boolean findNextName = false;
                                do {
                                    nextName = bufferReader2.readLine();
                                    if (nextName == null) {
                                        break;
                                    }
                                } while (!nextName.startsWith("Name:"));
                                line = nextName;
                                findNextName = true;
                                HwAuthLogger.w("HwCertificationManager", "find next block name.");
                                if (!findNextName) {
                                }
                            }
                            if (line.length() != 0) {
                                contentsList.add(line);
                            }
                        }
                        if (findHwCer) {
                            Collections.sort(contentsList);
                            for (String name : contentsList) {
                                bufferWriter2.write(name, 0, name.length());
                                bufferWriter2.write(LINE_SEPERATOR, 0, LINE_SEPERATOR.length());
                            }
                            bufferWriter2.flush();
                            HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, cost time is " + (System.currentTimeMillis() - readbegin));
                            if (bufferWriter2 != null) {
                                try {
                                    bufferWriter2.close();
                                } catch (IOException e3) {
                                    HwAuthLogger.i("HwCertificationManager", "bufferWriter.close, e is " + e3);
                                }
                            }
                            if (byteIn2 != null) {
                                try {
                                    byteIn2.close();
                                } catch (IOException e32) {
                                    HwAuthLogger.i("HwCertificationManager", "byteIn.close, e is " + e32);
                                }
                            }
                            if (bufferReader2 != null) {
                                try {
                                    bufferReader2.close();
                                } catch (IOException e322) {
                                    HwAuthLogger.i("HwCertificationManager", "bufferReader.close, e is " + e322);
                                }
                            }
                            return byteOut.toByteArray();
                        }
                        if (bufferWriter2 != null) {
                            try {
                                bufferWriter2.close();
                            } catch (IOException e3222) {
                                HwAuthLogger.i("HwCertificationManager", "bufferWriter.close, e is " + e3222);
                            }
                        }
                        if (byteIn2 != null) {
                            try {
                                byteIn2.close();
                            } catch (IOException e32222) {
                                HwAuthLogger.i("HwCertificationManager", "byteIn.close, e is " + e32222);
                            }
                        }
                        if (bufferReader2 != null) {
                            try {
                                bufferReader2.close();
                            } catch (IOException e322222) {
                                HwAuthLogger.i("HwCertificationManager", "bufferReader.close, e is " + e322222);
                            }
                        }
                        return manifest;
                    } catch (IOException e4) {
                        e322222 = e4;
                        bufferReader = bufferReader2;
                        bufferWriter = bufferWriter2;
                        byteIn = byteIn2;
                        HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, e is " + e322222);
                        bArr = new byte[0];
                        if (bufferWriter != null) {
                            try {
                                bufferWriter.close();
                            } catch (IOException e3222222) {
                                HwAuthLogger.i("HwCertificationManager", "bufferWriter.close, e is " + e3222222);
                            }
                        }
                        if (byteIn != null) {
                            try {
                                byteIn.close();
                            } catch (IOException e32222222) {
                                HwAuthLogger.i("HwCertificationManager", "byteIn.close, e is " + e32222222);
                            }
                        }
                        if (bufferReader != null) {
                            try {
                                bufferReader.close();
                            } catch (IOException e322222222) {
                                HwAuthLogger.i("HwCertificationManager", "bufferReader.close, e is " + e322222222);
                            }
                        }
                        return bArr;
                    } catch (Throwable th4) {
                        th = th4;
                        bufferReader = bufferReader2;
                        bufferWriter = bufferWriter2;
                        byteArrayOutputStream = byteOut;
                        byteIn = byteIn2;
                        if (bufferWriter != null) {
                            try {
                                bufferWriter.close();
                            } catch (IOException e3222222222) {
                                HwAuthLogger.i("HwCertificationManager", "bufferWriter.close, e is " + e3222222222);
                            }
                        }
                        if (byteIn != null) {
                            try {
                                byteIn.close();
                            } catch (IOException e32222222222) {
                                HwAuthLogger.i("HwCertificationManager", "byteIn.close, e is " + e32222222222);
                            }
                        }
                        if (bufferReader != null) {
                            try {
                                bufferReader.close();
                            } catch (IOException e322222222222) {
                                HwAuthLogger.i("HwCertificationManager", "bufferReader.close, e is " + e322222222222);
                            }
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e322222222222 = e5;
                    bufferWriter = bufferWriter2;
                    byteArrayOutputStream = byteOut;
                    byteIn = byteIn2;
                    HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, e is " + e322222222222);
                    bArr = new byte[0];
                    if (bufferWriter != null) {
                    }
                    if (byteIn != null) {
                    }
                    if (bufferReader != null) {
                    }
                    return bArr;
                } catch (Throwable th5) {
                    th = th5;
                    bufferWriter = bufferWriter2;
                    byteIn = byteIn2;
                    if (bufferWriter != null) {
                    }
                    if (byteIn != null) {
                    }
                    if (bufferReader != null) {
                    }
                    throw th;
                }
            } catch (IOException e6) {
                e322222222222 = e6;
                bufferWriter = bufferWriter2;
                byteArrayOutputStream = byteOut;
                HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, e is " + e322222222222);
                bArr = new byte[0];
                if (bufferWriter != null) {
                }
                if (byteIn != null) {
                }
                if (bufferReader != null) {
                }
                return bArr;
            } catch (Throwable th6) {
                th = th6;
                bufferWriter = bufferWriter2;
                byteArrayOutputStream = byteOut;
                if (bufferWriter != null) {
                }
                if (byteIn != null) {
                }
                if (bufferReader != null) {
                }
                throw th;
            }
        } catch (IOException e7) {
            e322222222222 = e7;
            HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, e is " + e322222222222);
            bArr = new byte[0];
            if (bufferWriter != null) {
            }
            if (byteIn != null) {
            }
            if (bufferReader != null) {
            }
            return bArr;
        }
    }

    public static byte[] getManifestFileWithoutHwCER(ZipFile zfile, ZipEntry entry) {
        if (zfile == null || entry == null) {
            return new byte[0];
        }
        long begin = System.currentTimeMillis();
        byte[] manifest = getManifestFileNew(zfile, entry);
        HwAuthLogger.i("HwCertificationManager", "getManifestFileNew cost, cost time is  " + (System.currentTimeMillis() - begin));
        byte[] out = readManifestAndSkipHwTag(manifest);
        HwAuthLogger.i("HwCertificationManager", "getManifestFileWithoutHwCER begin, cost time is  " + (System.currentTimeMillis() - begin));
        return out;
    }

    public static byte[] getManifestFileWithoutHwCER(File apkFile) {
        long begin = System.currentTimeMillis();
        byte[] manifest = getManifestFileNew(apkFile);
        HwAuthLogger.i("HwCertificationManager", "getManifestFileWithoutHwCER begin, cost time is  " + (System.currentTimeMillis() - begin));
        return manifest;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x004b A:{SYNTHETIC, Splitter: B:23:0x004b} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0055 A:{SYNTHETIC, Splitter: B:30:0x0055} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static InputStream readHwCertFromApk(String apkPath) {
        Throwable th;
        ZipFile zipFile = null;
        InputStream input = null;
        try {
            ZipFile zipFile2 = new ZipFile(new File(apkPath));
            try {
                Enumeration enumeration = zipFile2.entries();
                while (enumeration.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
                    if (!zipEntry.isDirectory() && CERT_NAME.equals(zipEntry.getName())) {
                        input = zipFile2.getInputStream(zipEntry);
                        break;
                    }
                }
                if (zipFile2 != null) {
                    try {
                        zipFile2.close();
                    } catch (IOException e) {
                    }
                    return input;
                }
                return input;
            } catch (IOException e2) {
                zipFile = zipFile2;
                try {
                    HwAuthLogger.e("HwCertificationManager", "IOException in readHwCertFromApk() in Utils.java ");
                    if (zipFile != null) {
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                zipFile = zipFile2;
                if (zipFile != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            HwAuthLogger.e("HwCertificationManager", "IOException in readHwCertFromApk() in Utils.java ");
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e5) {
                }
            }
            return null;
        }
    }

    public static InputStream readHwCertFromApk(ZipFile zfile, ZipEntry entry) {
        if (zfile == null || entry == null) {
            return null;
        }
        InputStream input = new ByteArrayInputStream(new byte[0]);
        try {
            if (!entry.isDirectory() && CERT_NAME.equals(entry.getName())) {
                input = zfile.getInputStream(entry);
            }
            return input;
        } catch (IOException e) {
            HwAuthLogger.e("HwCertificationManager", "IOException in readHwCertFromApk() in Utils.java ");
            try {
                input.close();
            } catch (IOException e2) {
            }
            return null;
        }
    }

    public static byte[] stringToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[(len / 2)];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String bytesToString(byte[] bytes) {
        char[] hexChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] chars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int byteValue = bytes[j] & 255;
            chars[j * 2] = hexChars[byteValue >>> 4];
            chars[(j * 2) + 1] = hexChars[byteValue & 15];
        }
        return new String(chars);
    }

    public static Date convertStringToDate(String dateString) throws ParseException {
        return new SimpleDateFormat(COMMON_DATE_FORMAT).parse(dateString);
    }

    public static String convertDateToString(Date from, Date to) {
        if (from == null || to == null) {
            return "";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(COMMON_DATE_FORMAT);
        String fromString = dateFormat.format(from);
        return "from " + fromString + " to " + dateFormat.format(to);
    }

    public static boolean isPackageInstalled(String packagename, Context context) {
        try {
            context.getPackageManager().getPackageInfo(packagename, 1);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isMultiSimEnabled() {
        try {
            return TelephonyManager.getDefault().isMultiSimEnabled();
        } catch (Exception e) {
            HwAuthLogger.w("HwCertificationManager", "isMultiSimEnabled Exception.");
            return false;
        }
    }

    public static boolean isCDMAPhone(int phoneType) {
        return 2 == phoneType;
    }
}

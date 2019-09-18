package com.android.server.pm.auth.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
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
import java.util.Iterator;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Utils {
    private static final int BUFFER_LENGTH = 4096;
    public static final String CERT_NAME = "META-INF/HUAWEI.CER";
    private static final String COMMON_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int CR_LF_LENGTH = 2;
    private static final int HW_CER_LEN_ONE = 1;
    private static final int HW_CER_LEN_THREE = 3;
    private static final int HW_CER_LEN_TWO = 2;
    private static final String HW_CER_NAME = "HUAWEI.CER";
    private static final String HW_CER_TAG = "Name: META-INF/HUAWEI.CER";
    private static final String LINE_SEPERATOR = "\r\n";
    public static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
    private static final String SF_ATTRIBUTE_ANDROID_APK_SIGNED_NAME = "X-Android-APK-Signed:";
    private static final String SF_ATTRIBUTE_ANDROID_APK_SIGNED_VALUE = "2";
    public static final String SF_CERT_NAME = "META-INF/CERT.SF";
    public static final String TAG = "HwCertificationManager";

    public static byte[] getManifestFileNew(File apkFile) {
        BufferedOutputStream bufOs = null;
        BufferedInputStream bufIn = null;
        byte[] b = new byte[4096];
        ZipFile zipFile = null;
        InputStream inputStream = null;
        try {
            zipFile = new ZipFile(apkFile);
            ZipEntry manifestEntry = zipFile.getEntry("META-INF/MANIFEST.MF");
            if (manifestEntry != null) {
                if (!manifestEntry.isDirectory()) {
                    InputStream inputStream2 = zipFile.getInputStream(manifestEntry);
                    BufferedInputStream bufIn2 = new BufferedInputStream(inputStream2);
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    BufferedOutputStream bufOs2 = new BufferedOutputStream(os);
                    while (true) {
                        int read = bufIn2.read(b, 0, 4096);
                        int readLength = read;
                        if (read <= 0) {
                            break;
                        }
                        bufOs2.write(b, 0, readLength);
                    }
                    bufOs2.flush();
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (IOException e) {
                            HwAuthLogger.e("HwCertificationManager", "can not close inputStream, e is " + e);
                        }
                    }
                    try {
                        bufIn2.close();
                    } catch (IOException e2) {
                        HwAuthLogger.e("HwCertificationManager", "can not close bufIn, e is " + e2);
                    }
                    try {
                        bufOs2.close();
                    } catch (IOException e3) {
                        HwAuthLogger.e("HwCertificationManager", "can not close bufOs, e is " + e3);
                    }
                    try {
                        zipFile.close();
                    } catch (IOException e4) {
                        HwAuthLogger.e("HwCertificationManager", "can not close zipFile, e is " + e4);
                    }
                    return 1 == 1 ? os.toByteArray() : new byte[0];
                }
            }
            HwAuthLogger.e("bailong", "can not find manifest.mf file.");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    HwAuthLogger.e("HwCertificationManager", "can not close inputStream, e is " + e5);
                }
            }
            if (bufIn != null) {
                try {
                    bufIn.close();
                } catch (IOException e6) {
                    HwAuthLogger.e("HwCertificationManager", "can not close bufIn, e is " + e6);
                }
            }
            if (bufOs != null) {
                try {
                    bufOs.close();
                } catch (IOException e7) {
                    HwAuthLogger.e("HwCertificationManager", "can not close bufOs, e is " + e7);
                }
            }
            try {
                zipFile.close();
            } catch (IOException e8) {
                HwAuthLogger.e("HwCertificationManager", "can not close zipFile, e is " + e8);
            }
            return null;
        } catch (IOException e9) {
            HwAuthLogger.e("HwCertificationManager", "IOException in getManifestFileNew, e is " + e9);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e10) {
                    HwAuthLogger.e("HwCertificationManager", "can not close inputStream, e is " + e10);
                }
            }
            if (bufIn != null) {
                try {
                    bufIn.close();
                } catch (IOException e11) {
                    HwAuthLogger.e("HwCertificationManager", "can not close bufIn, e is " + e11);
                }
            }
            if (bufOs != null) {
                try {
                    bufOs.close();
                } catch (IOException e12) {
                    HwAuthLogger.e("HwCertificationManager", "can not close bufOs, e is " + e12);
                }
            }
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e13) {
                    HwAuthLogger.e("HwCertificationManager", "can not close zipFile, e is " + e13);
                }
            }
            return null;
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e14) {
                    HwAuthLogger.e("HwCertificationManager", "can not close inputStream, e is " + e14);
                }
            }
            if (bufIn != null) {
                try {
                    bufIn.close();
                } catch (IOException e15) {
                    HwAuthLogger.e("HwCertificationManager", "can not close bufIn, e is " + e15);
                }
            }
            if (bufOs != null) {
                try {
                    bufOs.close();
                } catch (IOException e16) {
                    HwAuthLogger.e("HwCertificationManager", "can not close bufOs, e is " + e16);
                }
            }
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e17) {
                    HwAuthLogger.e("HwCertificationManager", "can not close zipFile, e is " + e17);
                }
            }
            throw th;
        }
    }

    public static byte[] getManifestFileNew(ZipFile zFile, ZipEntry manifestEntry) {
        if (zFile == null || manifestEntry == null) {
            return new byte[0];
        }
        BufferedOutputStream bufOs = null;
        BufferedInputStream bufIn = null;
        byte[] b = new byte[4096];
        InputStream inputStream = null;
        try {
            InputStream inputStream2 = zFile.getInputStream(manifestEntry);
            BufferedInputStream bufIn2 = new BufferedInputStream(inputStream2);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            BufferedOutputStream bufOs2 = new BufferedOutputStream(os);
            while (true) {
                int read = bufIn2.read(b, 0, 4096);
                int readLength = read;
                if (read <= 0) {
                    break;
                }
                bufOs2.write(b, 0, readLength);
            }
            bufOs2.flush();
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (IOException e) {
                    HwAuthLogger.e("HwCertificationManager", "can not close inputStream, e is " + e);
                }
            }
            try {
                bufIn2.close();
            } catch (IOException e2) {
                HwAuthLogger.e("HwCertificationManager", "can not close bufIn, e is " + e2);
            }
            try {
                bufOs2.close();
            } catch (IOException e3) {
                HwAuthLogger.e("HwCertificationManager", "can not close bufOs, e is " + e3);
            }
            return 1 == 1 ? os.toByteArray() : new byte[0];
        } catch (IOException e4) {
            HwAuthLogger.e("HwCertificationManager", "IOException in getManifestFileNew, e is " + e4);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    HwAuthLogger.e("HwCertificationManager", "can not close inputStream, e is " + e5);
                }
            }
            if (bufIn != null) {
                try {
                    bufIn.close();
                } catch (IOException e6) {
                    HwAuthLogger.e("HwCertificationManager", "can not close bufIn, e is " + e6);
                }
            }
            if (bufOs != null) {
                try {
                    bufOs.close();
                } catch (IOException e7) {
                    HwAuthLogger.e("HwCertificationManager", "can not close bufOs, e is " + e7);
                }
            }
            return null;
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e8) {
                    HwAuthLogger.e("HwCertificationManager", "can not close inputStream, e is " + e8);
                }
            }
            if (bufIn != null) {
                try {
                    bufIn.close();
                } catch (IOException e9) {
                    HwAuthLogger.e("HwCertificationManager", "can not close bufIn, e is " + e9);
                }
            }
            if (bufOs != null) {
                try {
                    bufOs.close();
                } catch (IOException e10) {
                    HwAuthLogger.e("HwCertificationManager", "can not close bufOs, e is " + e10);
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005b, code lost:
        com.android.server.pm.auth.util.HwAuthLogger.e("HwCertificationManager", "can not close br in isUsingSignatureSchemaV2");
     */
    public static boolean isUsingSignatureSchemaV2(ZipFile zFile, ZipEntry entry) {
        InputStream inputStream;
        int i = 0;
        if (zFile == null || entry == null || entry.isDirectory()) {
            HwAuthLogger.e("HwCertificationManager", "isUsingSignatureSchemaV2 input is null");
            return false;
        }
        boolean catchFlag = false;
        InputStream inputStream2 = null;
        BufferedReader br = null;
        try {
            inputStream = zFile.getInputStream(entry);
            BufferedReader br2 = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while (true) {
                if (i < 10) {
                    String readLine = br2.readLine();
                    String line = readLine;
                    if (readLine != null) {
                        if (line.startsWith(SF_ATTRIBUTE_ANDROID_APK_SIGNED_NAME)) {
                            String[] attr = line.split(":");
                            if (attr.length == 2 && attr[1].trim().equals("2")) {
                                catchFlag = true;
                            }
                        } else {
                            i++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            HwAuthLogger.e("HwCertificationManager", "IOException happened in isUsingSignatureSchemaV2, e is" + e);
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e2) {
                    HwAuthLogger.e("HwCertificationManager", "can not close br in isUsingSignatureSchemaV2");
                }
            }
            if (inputStream2 != null) {
                inputStream2.close();
            }
        } catch (Throwable th) {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e3) {
                    HwAuthLogger.e("HwCertificationManager", "can not close br in isUsingSignatureSchemaV2");
                }
            }
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (IOException e4) {
                    HwAuthLogger.e("HwCertificationManager", "can not close inputStream in isUsingSignatureSchemaV2");
                }
            }
            throw th;
        }
        return catchFlag;
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e5) {
                HwAuthLogger.e("HwCertificationManager", "can not close inputStream in isUsingSignatureSchemaV2");
            }
        }
        return catchFlag;
    }

    /* JADX WARNING: Removed duplicated region for block: B:119:0x028a A[SYNTHETIC, Splitter:B:119:0x028a] */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x02a8 A[SYNTHETIC, Splitter:B:124:0x02a8] */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x02c6 A[SYNTHETIC, Splitter:B:129:0x02c6] */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x02e7 A[SYNTHETIC, Splitter:B:137:0x02e7] */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x0305 A[SYNTHETIC, Splitter:B:142:0x0305] */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x0323 A[SYNTHETIC, Splitter:B:147:0x0323] */
    @SuppressLint({"PreferForInArrayList"})
    public static byte[] readManifestAndSkipHwTag(byte[] manifest) {
        Throwable th;
        Manifest manifestContent;
        byte[] bArr = manifest;
        ByteArrayInputStream byteIn = null;
        BufferedWriter bufferWriter = null;
        BufferedReader bufferReader = null;
        Manifest manifestContent2 = null;
        ArrayList arrayList = new ArrayList();
        boolean findHwCer = false;
        if (bArr == null) {
            HwAuthLogger.e("HwCertificationManager", "manifest is null in readManifestAndSkipHwTag.");
            return new byte[0];
        }
        try {
            long readbegin = System.currentTimeMillis();
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try {
                bufferWriter = new BufferedWriter(new OutputStreamWriter(byteOut, "UTF-8"));
                byteIn = new ByteArrayInputStream(bArr);
            } catch (IOException e) {
                e = e;
                byteIn = null;
                try {
                    HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, e is " + e);
                    byte[] bArr2 = new byte[0];
                    if (bufferWriter != null) {
                    }
                    if (byteIn != null) {
                    }
                    if (bufferReader != null) {
                    }
                    return bArr2;
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
                byteIn = null;
                if (bufferWriter != null) {
                }
                if (byteIn != null) {
                }
                if (bufferReader != null) {
                }
                throw th;
            }
            try {
                try {
                    bufferReader = new BufferedReader(new InputStreamReader(byteIn, "UTF-8"));
                    try {
                        int hwcerLen = HW_CER_TAG.length();
                        while (true) {
                            int hwcerLen2 = hwcerLen;
                            String readLine = bufferReader.readLine();
                            String line = readLine;
                            if (readLine == null) {
                                break;
                            }
                            try {
                                if (hwcerLen2 != line.length() || findHwCer) {
                                    manifestContent = manifestContent2;
                                } else {
                                    manifestContent = manifestContent2;
                                    if (line.charAt(hwcerLen2 - 3) == 'C') {
                                        try {
                                            if (line.charAt(hwcerLen2 - 2) == 'E' && line.charAt(hwcerLen2 - 1) == 'R' && line.lastIndexOf(HW_CER_NAME) != -1) {
                                                findHwCer = true;
                                                HwAuthLogger.w("HwCertificationManager", "find HwCer tag");
                                                boolean findNextName = false;
                                                while (true) {
                                                    String readLine2 = bufferReader.readLine();
                                                    String nextName = readLine2;
                                                    if (readLine2 != null) {
                                                        if (nextName.startsWith("Name:")) {
                                                            line = nextName;
                                                            HwAuthLogger.w("HwCertificationManager", "find next block name.");
                                                            findNextName = true;
                                                            break;
                                                        }
                                                    } else {
                                                        break;
                                                    }
                                                }
                                                if (!findNextName) {
                                                    hwcerLen = hwcerLen2;
                                                    manifestContent2 = manifestContent;
                                                }
                                            }
                                        } catch (IOException e2) {
                                            e = e2;
                                            HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, e is " + e);
                                            byte[] bArr22 = new byte[0];
                                            if (bufferWriter != null) {
                                            }
                                            if (byteIn != null) {
                                            }
                                            if (bufferReader != null) {
                                            }
                                            return bArr22;
                                        } catch (Throwable th4) {
                                            th = th4;
                                            if (bufferWriter != null) {
                                            }
                                            if (byteIn != null) {
                                            }
                                            if (bufferReader != null) {
                                            }
                                            throw th;
                                        }
                                    }
                                }
                                if (line.length() != 0) {
                                    arrayList.add(line);
                                }
                                hwcerLen = hwcerLen2;
                                manifestContent2 = manifestContent;
                            } catch (IOException e3) {
                                e = e3;
                                Manifest manifest2 = manifestContent2;
                                HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, e is " + e);
                                byte[] bArr222 = new byte[0];
                                if (bufferWriter != null) {
                                }
                                if (byteIn != null) {
                                }
                                if (bufferReader != null) {
                                }
                                return bArr222;
                            } catch (Throwable th5) {
                                Manifest manifest3 = manifestContent2;
                                th = th5;
                                if (bufferWriter != null) {
                                }
                                if (byteIn != null) {
                                }
                                if (bufferReader != null) {
                                }
                                throw th;
                            }
                        }
                        if (!findHwCer) {
                            try {
                                bufferWriter.close();
                            } catch (IOException e4) {
                                IOException iOException = e4;
                                HwAuthLogger.i("HwCertificationManager", "bufferWriter.close, e is " + e4);
                            }
                            try {
                                byteIn.close();
                            } catch (IOException e5) {
                                IOException iOException2 = e5;
                                HwAuthLogger.i("HwCertificationManager", "byteIn.close, e is " + e5);
                            }
                            try {
                                bufferReader.close();
                            } catch (IOException e6) {
                                IOException iOException3 = e6;
                                HwAuthLogger.i("HwCertificationManager", "bufferReader.close, e is " + e6);
                            }
                            return bArr;
                        }
                        try {
                            Collections.sort(arrayList);
                            Iterator it = arrayList.iterator();
                            while (it.hasNext()) {
                                String name = (String) it.next();
                                bufferWriter.write(name, 0, name.length());
                                bufferWriter.write(LINE_SEPERATOR, 0, LINE_SEPERATOR.length());
                                byte[] bArr3 = manifest;
                            }
                            bufferWriter.flush();
                            HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, cost time is " + (System.currentTimeMillis() - readbegin));
                            try {
                                bufferWriter.close();
                            } catch (IOException e7) {
                                IOException iOException4 = e7;
                                HwAuthLogger.i("HwCertificationManager", "bufferWriter.close, e is " + e7);
                            }
                            try {
                                byteIn.close();
                            } catch (IOException e8) {
                                IOException iOException5 = e8;
                                HwAuthLogger.i("HwCertificationManager", "byteIn.close, e is " + e8);
                            }
                            try {
                                bufferReader.close();
                            } catch (IOException e9) {
                                IOException iOException6 = e9;
                                HwAuthLogger.i("HwCertificationManager", "bufferReader.close, e is " + e9);
                            }
                            return byteOut.toByteArray();
                        } catch (IOException e10) {
                            e = e10;
                            HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, e is " + e);
                            byte[] bArr2222 = new byte[0];
                            if (bufferWriter != null) {
                            }
                            if (byteIn != null) {
                            }
                            if (bufferReader != null) {
                            }
                            return bArr2222;
                        }
                    } catch (IOException e11) {
                        e = e11;
                        HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, e is " + e);
                        byte[] bArr22222 = new byte[0];
                        if (bufferWriter != null) {
                            try {
                                bufferWriter.close();
                            } catch (IOException e12) {
                                IOException iOException7 = e12;
                                HwAuthLogger.i("HwCertificationManager", "bufferWriter.close, e is " + e12);
                            }
                        }
                        if (byteIn != null) {
                            try {
                                byteIn.close();
                            } catch (IOException e13) {
                                IOException iOException8 = e13;
                                HwAuthLogger.i("HwCertificationManager", "byteIn.close, e is " + e13);
                            }
                        }
                        if (bufferReader != null) {
                            try {
                                bufferReader.close();
                            } catch (IOException e14) {
                                IOException iOException9 = e14;
                                HwAuthLogger.i("HwCertificationManager", "bufferReader.close, e is " + e14);
                            }
                        }
                        return bArr22222;
                    } catch (Throwable th6) {
                        th = th6;
                        if (bufferWriter != null) {
                            try {
                                bufferWriter.close();
                            } catch (IOException e15) {
                                IOException iOException10 = e15;
                                HwAuthLogger.i("HwCertificationManager", "bufferWriter.close, e is " + e15);
                            }
                        }
                        if (byteIn != null) {
                            try {
                                byteIn.close();
                            } catch (IOException e16) {
                                IOException iOException11 = e16;
                                HwAuthLogger.i("HwCertificationManager", "byteIn.close, e is " + e16);
                            }
                        }
                        if (bufferReader != null) {
                            try {
                                bufferReader.close();
                            } catch (IOException e17) {
                                IOException iOException12 = e17;
                                HwAuthLogger.i("HwCertificationManager", "bufferReader.close, e is " + e17);
                            }
                        }
                        throw th;
                    }
                } catch (IOException e18) {
                    e = e18;
                    bufferReader = null;
                    HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, e is " + e);
                    byte[] bArr222222 = new byte[0];
                    if (bufferWriter != null) {
                    }
                    if (byteIn != null) {
                    }
                    if (bufferReader != null) {
                    }
                    return bArr222222;
                } catch (Throwable th7) {
                    th = th7;
                    bufferReader = null;
                    if (bufferWriter != null) {
                    }
                    if (byteIn != null) {
                    }
                    if (bufferReader != null) {
                    }
                    throw th;
                }
            } catch (IOException e19) {
                e = e19;
                HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, e is " + e);
                byte[] bArr2222222 = new byte[0];
                if (bufferWriter != null) {
                }
                if (byteIn != null) {
                }
                if (bufferReader != null) {
                }
                return bArr2222222;
            } catch (Throwable th8) {
                th = th8;
                if (bufferWriter != null) {
                }
                if (byteIn != null) {
                }
                if (bufferReader != null) {
                }
                throw th;
            }
        } catch (IOException e20) {
            e = e20;
            HwAuthLogger.i("HwCertificationManager", "readManifestAndSkipHwTag, e is " + e);
            byte[] bArr22222222 = new byte[0];
            if (bufferWriter != null) {
            }
            if (byteIn != null) {
            }
            if (bufferReader != null) {
            }
            return bArr22222222;
        } catch (Throwable th9) {
            th = th9;
            if (bufferWriter != null) {
            }
            if (byteIn != null) {
            }
            if (bufferReader != null) {
            }
            throw th;
        }
    }

    public static byte[] getManifestFileWithoutHwCER(ZipFile zfile, ZipEntry entry) {
        if (zfile == null || entry == null) {
            return new byte[0];
        }
        long begin = System.currentTimeMillis();
        byte[] manifest = getManifestFileNew(zfile, entry);
        HwAuthLogger.i("HwCertificationManager", "getManifestFileNew cost, cost time is " + (System.currentTimeMillis() - begin));
        byte[] out = readManifestAndSkipHwTag(manifest);
        HwAuthLogger.i("HwCertificationManager", "getManifestFileWithoutHwCER begin, cost time is " + (System.currentTimeMillis() - begin));
        return out;
    }

    public static byte[] getManifestFileWithoutHwCER(File apkFile) {
        long begin = System.currentTimeMillis();
        byte[] manifest = getManifestFileNew(apkFile);
        HwAuthLogger.i("HwCertificationManager", "getManifestFileWithoutHwCER begin, cost time is  " + (System.currentTimeMillis() - begin));
        return manifest;
    }

    public static InputStream readHwCertFromApk(String apkPath) {
        ZipFile zipFile = null;
        InputStream input = null;
        try {
            ZipFile zipFile2 = new ZipFile(new File(apkPath));
            Enumeration enumeration = zipFile2.entries();
            while (true) {
                if (enumeration.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
                    if (!zipEntry.isDirectory() && CERT_NAME.equals(zipEntry.getName())) {
                        input = zipFile2.getInputStream(zipEntry);
                        break;
                    }
                }
            }
            try {
                zipFile2.close();
            } catch (IOException e) {
                HwAuthLogger.e("HwCertificationManager", "close stream failed when read cert from apk");
            }
            return input;
        } catch (IOException e2) {
            HwAuthLogger.e("HwCertificationManager", "IOException in readHwCertFromApk() in Utils.java ");
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e3) {
                    HwAuthLogger.e("HwCertificationManager", "close stream failed when read cert from apk");
                }
            }
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e4) {
                    HwAuthLogger.e("HwCertificationManager", "close stream failed when read cert from apk");
                }
            }
            return null;
        } catch (Throwable th) {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e5) {
                    HwAuthLogger.e("HwCertificationManager", "close stream failed when read cert from apk");
                }
            }
            throw th;
        }
    }

    public static InputStream readHwCertFromApk(ZipFile zfile, ZipEntry entry) {
        String str;
        String str2;
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
                HwAuthLogger.e("HwCertificationManager", "close stream failed when read cert from apk");
            }
            return null;
        } finally {
            str = "HwCertificationManager";
            str2 = "readHwCertFromApk: finally";
            HwAuthLogger.e(str, str2);
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

    public static Date convertStringToDate(String dateString) throws ParseException {
        return new SimpleDateFormat(COMMON_DATE_FORMAT).parse(dateString);
    }

    public static String convertDateToString(Date from, Date to) {
        if (from == null || to == null) {
            return "";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(COMMON_DATE_FORMAT);
        String fromString = dateFormat.format(from);
        String toString = dateFormat.format(to);
        return "from " + fromString + " to " + toString;
    }

    public static boolean isPackageInstalled(String packagename, Context context) {
        try {
            context.getPackageManager().getPackageInfo(packagename, 1);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
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

    public static String getSfFileName(ZipFile zFile) {
        if (zFile.getEntry(SF_CERT_NAME) != null) {
            return SF_CERT_NAME;
        }
        Enumeration<? extends ZipEntry> entries = zFile.entries();
        while (entries.hasMoreElements()) {
            String zipFileNames = ((ZipEntry) entries.nextElement()).getName();
            if (zipFileNames.contains(".SF") && zipFileNames.contains("META-INF/")) {
                HwAuthLogger.i("HwCertificationManager", "The SF file name is " + zipFileNames);
                return zipFileNames;
            }
        }
        return null;
    }
}

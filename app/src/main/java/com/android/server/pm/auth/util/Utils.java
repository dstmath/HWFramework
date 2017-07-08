package com.android.server.pm.auth.util;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import huawei.com.android.server.policy.HwGlobalActionsData;
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

    public static byte[] getManifestFile(File apkFile) {
        Throwable th;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] b = new byte[HwGlobalActionsData.FLAG_SILENTMODE_NORMAL];
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
                    if (!zipEntry.isDirectory() && MANIFEST_NAME.equals(zipEntry.getName())) {
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
                    zipFile2.close();
                }
                if (catchFlag) {
                    bArr = new byte[0];
                } else {
                    bArr = os.toByteArray();
                }
                return bArr;
            } catch (IOException e2) {
                zipFile = zipFile2;
                try {
                    HwAuthLogger.e(TAG, "IOException in getManifestFile() in Utils.java ");
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e3) {
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                zipFile = zipFile2;
                if (zipFile != null) {
                    zipFile.close();
                }
                throw th;
            }
        } catch (IOException e5) {
            HwAuthLogger.e(TAG, "IOException in getManifestFile() in Utils.java ");
            if (zipFile != null) {
                zipFile.close();
            }
            return null;
        }
    }

    public static byte[] getManifestFileNew(File apkFile) {
        IOException e;
        Throwable th;
        BufferedOutputStream bufOs = null;
        BufferedInputStream bufIn = null;
        byte[] b = new byte[BUFFER_LENGTH];
        ZipFile zipFile = null;
        InputStream inputStream = null;
        try {
            ZipFile zipFile2 = new ZipFile(apkFile);
            try {
                ZipEntry ManifestEntry = zipFile2.getEntry(MANIFEST_NAME);
                if (ManifestEntry == null || ManifestEntry.isDirectory()) {
                    HwAuthLogger.e("bailong", "can not find manifest.mf file.");
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (IOException e2) {
                            HwAuthLogger.e(TAG, "can not close zipFile, e is " + e2);
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
                                int readLength = bufIn2.read(b, 0, BUFFER_LENGTH);
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
                            } catch (Throwable th2) {
                                th = th2;
                                zipFile = zipFile2;
                                bufIn = bufIn2;
                                bufOs = bufOs2;
                            }
                        }
                        bufOs2.flush();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e22) {
                                HwAuthLogger.e(TAG, "can not close inputStream, e is " + e22);
                            }
                        }
                        if (bufIn2 != null) {
                            try {
                                bufIn2.close();
                            } catch (IOException e222) {
                                HwAuthLogger.e(TAG, "can not close bufIn, e is " + e222);
                            }
                        }
                        if (bufOs2 != null) {
                            try {
                                bufOs2.close();
                            } catch (IOException e2222) {
                                HwAuthLogger.e(TAG, "can not close bufOs, e is " + e2222);
                            }
                        }
                        if (zipFile2 != null) {
                            try {
                                zipFile2.close();
                            } catch (IOException e22222) {
                                HwAuthLogger.e(TAG, "can not close zipFile, e is " + e22222);
                            }
                        }
                        return os.toByteArray();
                    } catch (IOException e4) {
                        e22222 = e4;
                        zipFile = zipFile2;
                        bufIn = bufIn2;
                        try {
                            HwAuthLogger.e(TAG, "IOException in getManifestFileNew, e is " + e22222);
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e222222) {
                                    HwAuthLogger.e(TAG, "can not close inputStream, e is " + e222222);
                                }
                            }
                            if (bufIn != null) {
                                try {
                                    bufIn.close();
                                } catch (IOException e2222222) {
                                    HwAuthLogger.e(TAG, "can not close bufIn, e is " + e2222222);
                                }
                            }
                            if (bufOs != null) {
                                try {
                                    bufOs.close();
                                } catch (IOException e22222222) {
                                    HwAuthLogger.e(TAG, "can not close bufOs, e is " + e22222222);
                                }
                            }
                            if (zipFile != null) {
                                try {
                                    zipFile.close();
                                } catch (IOException e222222222) {
                                    HwAuthLogger.e(TAG, "can not close zipFile, e is " + e222222222);
                                }
                            }
                            return null;
                        } catch (Throwable th3) {
                            th = th3;
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e2222222222) {
                                    HwAuthLogger.e(TAG, "can not close inputStream, e is " + e2222222222);
                                }
                            }
                            if (bufIn != null) {
                                try {
                                    bufIn.close();
                                } catch (IOException e22222222222) {
                                    HwAuthLogger.e(TAG, "can not close bufIn, e is " + e22222222222);
                                }
                            }
                            if (bufOs != null) {
                                try {
                                    bufOs.close();
                                } catch (IOException e222222222222) {
                                    HwAuthLogger.e(TAG, "can not close bufOs, e is " + e222222222222);
                                }
                            }
                            if (zipFile != null) {
                                try {
                                    zipFile.close();
                                } catch (IOException e2222222222222) {
                                    HwAuthLogger.e(TAG, "can not close zipFile, e is " + e2222222222222);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        zipFile = zipFile2;
                        bufIn = bufIn2;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (bufIn != null) {
                            bufIn.close();
                        }
                        if (bufOs != null) {
                            bufOs.close();
                        }
                        if (zipFile != null) {
                            zipFile.close();
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e2222222222222 = e5;
                    zipFile = zipFile2;
                    bufIn = bufIn2;
                    HwAuthLogger.e(TAG, "IOException in getManifestFileNew, e is " + e2222222222222);
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (bufIn != null) {
                        bufIn.close();
                    }
                    if (bufOs != null) {
                        bufOs.close();
                    }
                    if (zipFile != null) {
                        zipFile.close();
                    }
                    return null;
                } catch (Throwable th5) {
                    th = th5;
                    zipFile = zipFile2;
                    bufIn = bufIn2;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (bufIn != null) {
                        bufIn.close();
                    }
                    if (bufOs != null) {
                        bufOs.close();
                    }
                    if (zipFile != null) {
                        zipFile.close();
                    }
                    throw th;
                }
            } catch (IOException e6) {
                e2222222222222 = e6;
                zipFile = zipFile2;
                HwAuthLogger.e(TAG, "IOException in getManifestFileNew, e is " + e2222222222222);
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufIn != null) {
                    bufIn.close();
                }
                if (bufOs != null) {
                    bufOs.close();
                }
                if (zipFile != null) {
                    zipFile.close();
                }
                return null;
            } catch (Throwable th6) {
                th = th6;
                zipFile = zipFile2;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufIn != null) {
                    bufIn.close();
                }
                if (bufOs != null) {
                    bufOs.close();
                }
                if (zipFile != null) {
                    zipFile.close();
                }
                throw th;
            }
        } catch (IOException e7) {
            e2222222222222 = e7;
            HwAuthLogger.e(TAG, "IOException in getManifestFileNew, e is " + e2222222222222);
            if (inputStream != null) {
                inputStream.close();
            }
            if (bufIn != null) {
                bufIn.close();
            }
            if (bufOs != null) {
                bufOs.close();
            }
            if (zipFile != null) {
                zipFile.close();
            }
            return null;
        }
    }

    public static byte[] getManifestFileNew(ZipFile zfile, ZipEntry ManifestEntry) {
        IOException e;
        Throwable th;
        if (zfile == null || ManifestEntry == null) {
            return new byte[0];
        }
        BufferedOutputStream bufOs = null;
        BufferedInputStream bufferedInputStream = null;
        byte[] b = new byte[BUFFER_LENGTH];
        InputStream inputStream = null;
        try {
            ByteArrayOutputStream os;
            inputStream = zfile.getInputStream(ManifestEntry);
            BufferedInputStream bufIn = new BufferedInputStream(inputStream);
            try {
                os = new ByteArrayOutputStream();
            } catch (IOException e2) {
                e = e2;
                bufferedInputStream = bufIn;
                try {
                    HwAuthLogger.e(TAG, "IOException in getManifestFileNew, e is " + e);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e3) {
                            HwAuthLogger.e(TAG, "can not close inputStream, e is " + e3);
                        }
                    }
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e32) {
                            HwAuthLogger.e(TAG, "can not close bufIn, e is " + e32);
                        }
                    }
                    if (bufOs != null) {
                        try {
                            bufOs.close();
                        } catch (IOException e322) {
                            HwAuthLogger.e(TAG, "can not close bufOs, e is " + e322);
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e3222) {
                            HwAuthLogger.e(TAG, "can not close inputStream, e is " + e3222);
                        }
                    }
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e32222) {
                            HwAuthLogger.e(TAG, "can not close bufIn, e is " + e32222);
                        }
                    }
                    if (bufOs != null) {
                        try {
                            bufOs.close();
                        } catch (IOException e322222) {
                            HwAuthLogger.e(TAG, "can not close bufOs, e is " + e322222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedInputStream = bufIn;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (bufOs != null) {
                    bufOs.close();
                }
                throw th;
            }
            try {
                BufferedOutputStream bufOs2 = new BufferedOutputStream(os);
                while (true) {
                    try {
                        int readLength = bufIn.read(b, 0, BUFFER_LENGTH);
                        if (readLength <= 0) {
                            break;
                        }
                        bufOs2.write(b, 0, readLength);
                    } catch (IOException e4) {
                        e322222 = e4;
                        bufferedInputStream = bufIn;
                        bufOs = bufOs2;
                        ByteArrayOutputStream byteArrayOutputStream = os;
                    } catch (Throwable th4) {
                        th = th4;
                        bufferedInputStream = bufIn;
                        bufOs = bufOs2;
                    }
                }
                bufOs2.flush();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3222222) {
                        HwAuthLogger.e(TAG, "can not close inputStream, e is " + e3222222);
                    }
                }
                if (bufIn != null) {
                    try {
                        bufIn.close();
                    } catch (IOException e32222222) {
                        HwAuthLogger.e(TAG, "can not close bufIn, e is " + e32222222);
                    }
                }
                if (bufOs2 != null) {
                    try {
                        bufOs2.close();
                    } catch (IOException e322222222) {
                        HwAuthLogger.e(TAG, "can not close bufOs, e is " + e322222222);
                    }
                }
                return os.toByteArray();
            } catch (IOException e5) {
                e322222222 = e5;
                bufferedInputStream = bufIn;
                HwAuthLogger.e(TAG, "IOException in getManifestFileNew, e is " + e322222222);
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (bufOs != null) {
                    bufOs.close();
                }
                return null;
            } catch (Throwable th5) {
                th = th5;
                bufferedInputStream = bufIn;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (bufOs != null) {
                    bufOs.close();
                }
                throw th;
            }
        } catch (IOException e6) {
            e322222222 = e6;
            HwAuthLogger.e(TAG, "IOException in getManifestFileNew, e is " + e322222222);
            if (inputStream != null) {
                inputStream.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (bufOs != null) {
                bufOs.close();
            }
            return null;
        }
    }

    public static boolean isUsingSignatureSchemaV2(ZipFile zFile, ZipEntry entry) {
        IOException e;
        Throwable th;
        if (zFile == null || entry == null || entry.isDirectory()) {
            HwAuthLogger.e(TAG, "isUsingSignatureSchemaV2 input is null");
            return false;
        }
        boolean catchFlag = false;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = zFile.getInputStream(entry);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            int i = 0;
            while (i < 10) {
                try {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    } else if (line.startsWith(SF_ATTRIBUTE_ANDROID_APK_SIGNED_NAME)) {
                        String[] attr = line.split(":");
                        if (attr.length == CR_LF_LENGTH && attr[1].trim().equals(SF_ATTRIBUTE_ANDROID_APK_SIGNED_VALUE)) {
                            catchFlag = true;
                        }
                    } else {
                        i++;
                    }
                } catch (IOException e2) {
                    e = e2;
                    bufferedReader = br;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = br;
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e3) {
                    HwAuthLogger.e(TAG, "can not close br in isUsingSignatureSchemaV2");
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    HwAuthLogger.e(TAG, "can not close inputStream in isUsingSignatureSchemaV2");
                }
            }
            bufferedReader = br;
        } catch (IOException e5) {
            e = e5;
            try {
                HwAuthLogger.e(TAG, "IOException happened in isUsingSignatureSchemaV2, e is" + e);
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e6) {
                        HwAuthLogger.e(TAG, "can not close br in isUsingSignatureSchemaV2");
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e7) {
                        HwAuthLogger.e(TAG, "can not close inputStream in isUsingSignatureSchemaV2");
                    }
                }
                return catchFlag;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e8) {
                        HwAuthLogger.e(TAG, "can not close br in isUsingSignatureSchemaV2");
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e9) {
                        HwAuthLogger.e(TAG, "can not close inputStream in isUsingSignatureSchemaV2");
                    }
                }
                throw th;
            }
        }
        return catchFlag;
    }

    public static byte[] readManifestAndSkipHwTag(byte[] manifest) {
        BufferedWriter bufferWriter;
        IOException e;
        byte[] bArr;
        Throwable th;
        ByteArrayOutputStream byteArrayOutputStream;
        ByteArrayInputStream byteArrayInputStream = null;
        BufferedWriter bufferWriter2 = null;
        BufferedReader bufferReader = null;
        ArrayList<String> contentsList = new ArrayList();
        boolean findHwCer = false;
        if (manifest == null) {
            HwAuthLogger.e(TAG, "manifest is null in readManifestAndSkipHwTag.");
            return new byte[0];
        }
        try {
            long readbegin = System.currentTimeMillis();
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try {
                bufferWriter = new BufferedWriter(new OutputStreamWriter(byteOut, "UTF-8"));
            } catch (IOException e2) {
                e = e2;
                try {
                    HwAuthLogger.i(TAG, "readManifestAndSkipHwTag, e is " + e);
                    bArr = new byte[0];
                    if (bufferWriter2 != null) {
                        try {
                            bufferWriter2.close();
                        } catch (IOException e3) {
                            HwAuthLogger.i(TAG, "bufferWriter.close, e is " + e3);
                        }
                    }
                    if (byteArrayInputStream != null) {
                        try {
                            byteArrayInputStream.close();
                        } catch (IOException e32) {
                            HwAuthLogger.i(TAG, "byteIn.close, e is " + e32);
                        }
                    }
                    if (bufferReader != null) {
                        try {
                            bufferReader.close();
                        } catch (IOException e322) {
                            HwAuthLogger.i(TAG, "bufferReader.close, e is " + e322);
                        }
                    }
                    return bArr;
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferWriter2 != null) {
                        try {
                            bufferWriter2.close();
                        } catch (IOException e3222) {
                            HwAuthLogger.i(TAG, "bufferWriter.close, e is " + e3222);
                        }
                    }
                    if (byteArrayInputStream != null) {
                        try {
                            byteArrayInputStream.close();
                        } catch (IOException e32222) {
                            HwAuthLogger.i(TAG, "byteIn.close, e is " + e32222);
                        }
                    }
                    if (bufferReader != null) {
                        try {
                            bufferReader.close();
                        } catch (IOException e322222) {
                            HwAuthLogger.i(TAG, "bufferReader.close, e is " + e322222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                byteArrayOutputStream = byteOut;
                if (bufferWriter2 != null) {
                    bufferWriter2.close();
                }
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
                if (bufferReader != null) {
                    bufferReader.close();
                }
                throw th;
            }
            try {
                ByteArrayInputStream byteIn = new ByteArrayInputStream(manifest);
                try {
                    BufferedReader bufferReader2 = new BufferedReader(new InputStreamReader(byteIn, "UTF-8"));
                    try {
                        int hwcerLen = HW_CER_TAG.length();
                        while (true) {
                            String line = bufferReader2.readLine();
                            if (line == null) {
                                break;
                            }
                            if (hwcerLen == line.length() && !findHwCer) {
                                if (line.charAt(hwcerLen - 3) == 'C') {
                                    if (line.charAt(hwcerLen - 2) == 'E') {
                                        if (line.charAt(hwcerLen - 1) == 'R') {
                                            if (line.lastIndexOf(HW_CER_NAME) != -1) {
                                                String nextName;
                                                findHwCer = true;
                                                HwAuthLogger.w(TAG, "find HwCer tag");
                                                boolean findNextName = false;
                                                do {
                                                    nextName = bufferReader2.readLine();
                                                    if (nextName == null) {
                                                        break;
                                                    }
                                                } while (!nextName.startsWith("Name:"));
                                                line = nextName;
                                                findNextName = true;
                                                HwAuthLogger.w(TAG, "find next block name.");
                                                if (!findNextName) {
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (line.length() != 0) {
                                contentsList.add(line);
                            }
                        }
                        if (findHwCer) {
                            Collections.sort(contentsList);
                            for (String name : contentsList) {
                                bufferWriter.write(name, 0, name.length());
                                bufferWriter.write(LINE_SEPERATOR, 0, LINE_SEPERATOR.length());
                            }
                            bufferWriter.flush();
                            HwAuthLogger.i(TAG, "readManifestAndSkipHwTag, cost time is " + (System.currentTimeMillis() - readbegin));
                            if (bufferWriter != null) {
                                try {
                                    bufferWriter.close();
                                } catch (IOException e3222222) {
                                    HwAuthLogger.i(TAG, "bufferWriter.close, e is " + e3222222);
                                }
                            }
                            if (byteIn != null) {
                                try {
                                    byteIn.close();
                                } catch (IOException e32222222) {
                                    HwAuthLogger.i(TAG, "byteIn.close, e is " + e32222222);
                                }
                            }
                            if (bufferReader2 != null) {
                                try {
                                    bufferReader2.close();
                                } catch (IOException e322222222) {
                                    HwAuthLogger.i(TAG, "bufferReader.close, e is " + e322222222);
                                }
                            }
                            return byteOut.toByteArray();
                        }
                        if (bufferWriter != null) {
                            try {
                                bufferWriter.close();
                            } catch (IOException e3222222222) {
                                HwAuthLogger.i(TAG, "bufferWriter.close, e is " + e3222222222);
                            }
                        }
                        if (byteIn != null) {
                            try {
                                byteIn.close();
                            } catch (IOException e32222222222) {
                                HwAuthLogger.i(TAG, "byteIn.close, e is " + e32222222222);
                            }
                        }
                        if (bufferReader2 != null) {
                            try {
                                bufferReader2.close();
                            } catch (IOException e322222222222) {
                                HwAuthLogger.i(TAG, "bufferReader.close, e is " + e322222222222);
                            }
                        }
                        return manifest;
                    } catch (IOException e4) {
                        e322222222222 = e4;
                        bufferReader = bufferReader2;
                        bufferWriter2 = bufferWriter;
                        byteArrayInputStream = byteIn;
                    } catch (Throwable th4) {
                        th = th4;
                        bufferReader = bufferReader2;
                        bufferWriter2 = bufferWriter;
                        byteArrayOutputStream = byteOut;
                        byteArrayInputStream = byteIn;
                    }
                } catch (IOException e5) {
                    e322222222222 = e5;
                    bufferWriter2 = bufferWriter;
                    byteArrayOutputStream = byteOut;
                    byteArrayInputStream = byteIn;
                    HwAuthLogger.i(TAG, "readManifestAndSkipHwTag, e is " + e322222222222);
                    bArr = new byte[0];
                    if (bufferWriter2 != null) {
                        bufferWriter2.close();
                    }
                    if (byteArrayInputStream != null) {
                        byteArrayInputStream.close();
                    }
                    if (bufferReader != null) {
                        bufferReader.close();
                    }
                    return bArr;
                } catch (Throwable th5) {
                    th = th5;
                    bufferWriter2 = bufferWriter;
                    byteArrayInputStream = byteIn;
                    if (bufferWriter2 != null) {
                        bufferWriter2.close();
                    }
                    if (byteArrayInputStream != null) {
                        byteArrayInputStream.close();
                    }
                    if (bufferReader != null) {
                        bufferReader.close();
                    }
                    throw th;
                }
            } catch (IOException e6) {
                e322222222222 = e6;
                bufferWriter2 = bufferWriter;
                byteArrayOutputStream = byteOut;
                HwAuthLogger.i(TAG, "readManifestAndSkipHwTag, e is " + e322222222222);
                bArr = new byte[0];
                if (bufferWriter2 != null) {
                    bufferWriter2.close();
                }
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
                if (bufferReader != null) {
                    bufferReader.close();
                }
                return bArr;
            } catch (Throwable th6) {
                th = th6;
                bufferWriter2 = bufferWriter;
                byteArrayOutputStream = byteOut;
                if (bufferWriter2 != null) {
                    bufferWriter2.close();
                }
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
                if (bufferReader != null) {
                    bufferReader.close();
                }
                throw th;
            }
        } catch (IOException e7) {
            e322222222222 = e7;
            HwAuthLogger.i(TAG, "readManifestAndSkipHwTag, e is " + e322222222222);
            bArr = new byte[0];
            if (bufferWriter2 != null) {
                bufferWriter2.close();
            }
            if (byteArrayInputStream != null) {
                byteArrayInputStream.close();
            }
            if (bufferReader != null) {
                bufferReader.close();
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
        HwAuthLogger.i(TAG, "getManifestFileNew cost, cost time is  " + (System.currentTimeMillis() - begin));
        byte[] out = readManifestAndSkipHwTag(manifest);
        HwAuthLogger.i(TAG, "getManifestFileWithoutHwCER begin, cost time is  " + (System.currentTimeMillis() - begin));
        return out;
    }

    public static byte[] getManifestFileWithoutHwCER(File apkFile) {
        long begin = System.currentTimeMillis();
        byte[] manifest = getManifestFileNew(apkFile);
        HwAuthLogger.i(TAG, "getManifestFileWithoutHwCER begin, cost time is  " + (System.currentTimeMillis() - begin));
        return manifest;
    }

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
                    HwAuthLogger.e(TAG, "IOException in readHwCertFromApk() in Utils.java ");
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e3) {
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                zipFile = zipFile2;
                if (zipFile != null) {
                    zipFile.close();
                }
                throw th;
            }
        } catch (IOException e5) {
            HwAuthLogger.e(TAG, "IOException in readHwCertFromApk() in Utils.java ");
            if (zipFile != null) {
                zipFile.close();
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
            HwAuthLogger.e(TAG, "IOException in readHwCertFromApk() in Utils.java ");
            try {
                input.close();
            } catch (IOException e2) {
            }
            return null;
        }
    }

    public static byte[] stringToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[(len / CR_LF_LENGTH)];
        for (int i = 0; i < len; i += CR_LF_LENGTH) {
            data[i / CR_LF_LENGTH] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String bytesToString(byte[] bytes) {
        char[] hexChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] chars = new char[(bytes.length * CR_LF_LENGTH)];
        for (int j = 0; j < bytes.length; j++) {
            int byteValue = bytes[j] & com.android.server.display.Utils.MAXINUM_TEMPERATURE;
            chars[j * CR_LF_LENGTH] = hexChars[byteValue >>> 4];
            chars[(j * CR_LF_LENGTH) + 1] = hexChars[byteValue & 15];
        }
        return new String(chars);
    }

    public static Date convertStringToDate(String dateString) throws ParseException {
        return new SimpleDateFormat(COMMON_DATE_FORMAT).parse(dateString);
    }

    public static String convertDateToString(Date from, Date to) {
        if (from == null || to == null) {
            return AppHibernateCst.INVALID_PKG;
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
            HwAuthLogger.w(TAG, "isMultiSimEnabled Exception.");
            return false;
        }
    }

    public static boolean isCDMAPhone(int phoneType) {
        return CR_LF_LENGTH == phoneType;
    }
}

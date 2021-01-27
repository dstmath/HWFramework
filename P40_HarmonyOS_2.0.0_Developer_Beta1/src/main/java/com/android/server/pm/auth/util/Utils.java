package com.android.server.pm.auth.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.server.location.HwLocalLocationProvider;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Utils {
    private static final int ATTRIBUTE_LENGTH = 2;
    private static final int BUFFER_LENGTH = 4096;
    private static final int BYTE_TO_HEX_MASK = 255;
    private static final int BYTE_TO_HEX_MASK_LOW = 15;
    private static final int BYTE_TO_HEX_NUMBER = 2;
    public static final String CERT_NAME = "META-INF/HUAWEI.CER";
    private static final String COMMON_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int CR_LF_LENGTH = 2;
    private static final int DEFAULT_FLAG = 0;
    private static final int DEFAULT_STRING_LENGTH = 10;
    private static final int HEX_BITS_NUMBER = 4;
    private static final int HEX_VALUE = 16;
    private static final String HW_CER_NAME = "HUAWEI.CER";
    private static final String HW_CER_TAG = "Name: META-INF/HUAWEI.CER";
    private static final int INVALID_DATA = -1;
    private static final int INVALID_INDEX = -1;
    private static final int KB_UNIT = 1024;
    private static final int MAX_LINE_COUNT = 10;
    private static final String SF_ATTRIBUTE_ANDROID_APK_SIGNED_NAME = "X-Android-APK-Signed:";
    private static final String SF_CERT_NAME = "META-INF/CERT.SF";
    private static final String TAG = "HwCertificationManager";
    private static final String WINDOWS_LINE_SEPARATOR = "\r\n";

    private Utils() {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005b, code lost:
        r10 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005c, code lost:
        if (r8 != null) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        $closeResource(r9, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0061, code lost:
        throw r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x007f, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0080, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0083, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0086, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0087, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x008a, code lost:
        throw r5;
     */
    public static byte[] getManifestFile(File apkFile) {
        if (apkFile == null) {
            HwAuthLogger.error("HwCertificationManager", "getManifestFile apk file is null!");
            return new byte[0];
        }
        boolean isSuccessful = false;
        try {
            ZipFile zipFile = new ZipFile(apkFile);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
            while (true) {
                if (!enumeration.hasMoreElements()) {
                    break;
                }
                ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
                if (!zipEntry.isDirectory() && "META-INF/MANIFEST.MF".equals(zipEntry.getName())) {
                    try {
                        InputStream inputStream = zipFile.getInputStream(zipEntry);
                        byte[] bytes = new byte[1024];
                        for (int length = inputStream.read(bytes); length > 0; length = inputStream.read(bytes)) {
                            outputStream.write(bytes, 0, length);
                        }
                        $closeResource(null, inputStream);
                        isSuccessful = true;
                    } catch (IOException e) {
                        byte[] bArr = new byte[0];
                        $closeResource(null, outputStream);
                        $closeResource(null, zipFile);
                        return bArr;
                    }
                }
            }
            byte[] byteArray = isSuccessful ? outputStream.toByteArray() : new byte[0];
            $closeResource(null, outputStream);
            $closeResource(null, zipFile);
            return byteArray;
        } catch (IOException e2) {
            HwAuthLogger.error("HwCertificationManager", "IOException in getManifestFile!");
            return new byte[0];
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

    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00ad, code lost:
        r11 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00ae, code lost:
        $closeResource(r6, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00b1, code lost:
        throw r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00b4, code lost:
        r10 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00b5, code lost:
        $closeResource(r6, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00b8, code lost:
        throw r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x00bb, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x00bc, code lost:
        $closeResource(r6, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x00bf, code lost:
        throw r9;
     */
    public static byte[] getManifestFileNew(File apkFile) {
        if (apkFile == null) {
            return new byte[0];
        }
        InputStream inputStream = null;
        BufferedInputStream bufInputStream = null;
        byte[] bytes = new byte[4096];
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BufferedOutputStream bufOutputStream = new BufferedOutputStream(outputStream);
            ZipFile zipFile = new ZipFile(apkFile);
            ZipEntry entry = zipFile.getEntry("META-INF/MANIFEST.MF");
            if (entry == null) {
                byte[] bArr = new byte[0];
                $closeResource(null, zipFile);
                $closeResource(null, bufOutputStream);
                $closeResource(null, outputStream);
                if (0 != 0) {
                    try {
                        bufInputStream.close();
                    } catch (IOException e) {
                        HwAuthLogger.error("HwCertificationManager", "getManifestFileNew failed to close BufferedInputStream!");
                    }
                }
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e2) {
                        HwAuthLogger.error("HwCertificationManager", "getManifestFileNew failed to close InputStream!");
                    }
                }
                return bArr;
            }
            InputStream inputStream2 = zipFile.getInputStream(entry);
            if (inputStream2 == null) {
                byte[] bArr2 = new byte[0];
                $closeResource(null, zipFile);
                $closeResource(null, bufOutputStream);
                $closeResource(null, outputStream);
                if (0 != 0) {
                    try {
                        bufInputStream.close();
                    } catch (IOException e3) {
                        HwAuthLogger.error("HwCertificationManager", "getManifestFileNew failed to close BufferedInputStream!");
                    }
                }
                if (inputStream2 != null) {
                    try {
                        inputStream2.close();
                    } catch (IOException e4) {
                        HwAuthLogger.error("HwCertificationManager", "getManifestFileNew failed to close InputStream!");
                    }
                }
                return bArr2;
            }
            BufferedInputStream bufInputStream2 = new BufferedInputStream(inputStream2);
            for (int readLength = bufInputStream2.read(bytes, 0, 4096); readLength > 0; readLength = bufInputStream2.read(bytes, 0, 4096)) {
                bufOutputStream.write(bytes, 0, readLength);
            }
            bufOutputStream.flush();
            byte[] byteArray = outputStream.toByteArray();
            $closeResource(null, zipFile);
            $closeResource(null, bufOutputStream);
            $closeResource(null, outputStream);
            try {
                bufInputStream2.close();
            } catch (IOException e5) {
                HwAuthLogger.error("HwCertificationManager", "getManifestFileNew failed to close BufferedInputStream!");
            }
            try {
                inputStream2.close();
            } catch (IOException e6) {
                HwAuthLogger.error("HwCertificationManager", "getManifestFileNew failed to close InputStream!");
            }
            return byteArray;
        } catch (IOException e7) {
            HwAuthLogger.error("HwCertificationManager", "IOException in getManifestFileNew!");
            if (0 != 0) {
                try {
                    bufInputStream.close();
                } catch (IOException e8) {
                    HwAuthLogger.error("HwCertificationManager", "getManifestFileNew failed to close BufferedInputStream!");
                }
            }
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Exception e9) {
            HwAuthLogger.error("HwCertificationManager", "getManifestFileNew exception!");
            if (0 != 0) {
                try {
                    bufInputStream.close();
                } catch (IOException e10) {
                    HwAuthLogger.error("HwCertificationManager", "getManifestFileNew failed to close BufferedInputStream!");
                }
            }
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e11) {
                    HwAuthLogger.error("HwCertificationManager", "getManifestFileNew failed to close InputStream!");
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bufInputStream.close();
                } catch (IOException e12) {
                    HwAuthLogger.error("HwCertificationManager", "getManifestFileNew failed to close BufferedInputStream!");
                }
            }
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e13) {
                    HwAuthLogger.error("HwCertificationManager", "getManifestFileNew failed to close InputStream!");
                }
            }
            throw th;
        }
        return new byte[0];
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004a, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004b, code lost:
        $closeResource(r6, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004e, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0051, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0052, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0055, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0058, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0059, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x005c, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x005f, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0060, code lost:
        if (r2 != null) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0062, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0065, code lost:
        throw r4;
     */
    public static byte[] getManifestFileNew(ZipFile zipFile, ZipEntry manifestEntry) {
        if (zipFile == null || manifestEntry == null) {
            HwAuthLogger.error("HwCertificationManager", "getManifestFileNew zipFile or manifestEntry is null!");
            return new byte[0];
        }
        try {
            InputStream inputStream = zipFile.getInputStream(manifestEntry);
            BufferedInputStream bufInputStream = new BufferedInputStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BufferedOutputStream bufOutputStream = new BufferedOutputStream(outputStream);
            byte[] bytes = new byte[4096];
            for (int readLength = bufInputStream.read(bytes, 0, 4096); readLength > 0; readLength = bufInputStream.read(bytes, 0, 4096)) {
                bufOutputStream.write(bytes, 0, readLength);
            }
            bufOutputStream.flush();
            byte[] byteArray = outputStream.toByteArray();
            $closeResource(null, bufOutputStream);
            $closeResource(null, outputStream);
            $closeResource(null, bufInputStream);
            if (inputStream != null) {
                $closeResource(null, inputStream);
            }
            return byteArray;
        } catch (IOException e) {
            HwAuthLogger.error("HwCertificationManager", "getManifestFileNew IOException!");
            return new byte[0];
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007a, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x007b, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x007e, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0081, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0082, code lost:
        if (r2 != null) goto L_0x0084;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0084, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0087, code lost:
        throw r4;
     */
    public static boolean isUsingSignatureSchemaV2(ZipFile zipFile, ZipEntry zipEntry) {
        if (zipFile == null || zipEntry == null || zipEntry.isDirectory()) {
            HwAuthLogger.error("HwCertificationManager", "isUsingSignatureSchemaV2 input is null!");
            return false;
        }
        boolean isSuccessful = false;
        try {
            InputStream inputStream = zipFile.getInputStream(zipEntry);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            int count = 0;
            String line = readLine(bufferedReader);
            while (true) {
                if (count >= 10 || line == null) {
                    break;
                } else if (line.startsWith(SF_ATTRIBUTE_ANDROID_APK_SIGNED_NAME)) {
                    String[] attrs = line.split(AwarenessInnerConstants.COLON_KEY);
                    HwAuthLogger.info("HwCertificationManager", "isUsingSignatureSchemaV2 X-APK-Signed value is: " + attrs[1].trim());
                    if (attrs.length == 2 && !TextUtils.isEmpty(attrs[1].trim())) {
                        isSuccessful = true;
                    }
                } else {
                    count++;
                    line = readLine(bufferedReader);
                }
            }
            $closeResource(null, bufferedReader);
            if (inputStream != null) {
                $closeResource(null, inputStream);
            }
        } catch (IOException e) {
            HwAuthLogger.error("HwCertificationManager", "IOException in isUsingSignatureSchemaV2!");
        }
        return isSuccessful;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0087, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0088, code lost:
        $closeResource(r0, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x008b, code lost:
        throw r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x008e, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x008f, code lost:
        $closeResource(r0, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0092, code lost:
        throw r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0095, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0096, code lost:
        $closeResource(r0, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0099, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x009c, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x009d, code lost:
        $closeResource(r0, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00a0, code lost:
        throw r6;
     */
    @SuppressLint({"PreferForInArrayList"})
    public static byte[] readManifestAndSkipHwTag(byte[] manifestBytes) {
        if (manifestBytes == null) {
            HwAuthLogger.error("HwCertificationManager", "manifest is null in readManifestAndSkipHwTag!");
            return new byte[0];
        }
        ArrayList<String> contents = new ArrayList<>();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(manifestBytes);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            if (!isHwCertFound(bufferedReader, contents)) {
                $closeResource(null, bufferedWriter);
                $closeResource(null, bufferedReader);
                $closeResource(null, inputStream);
                $closeResource(null, outputStream);
                return manifestBytes;
            }
            Collections.sort(contents);
            int size = contents.size();
            for (int i = 0; i < size; i++) {
                String name = contents.get(i);
                bufferedWriter.write(name, 0, name.length());
                bufferedWriter.write(WINDOWS_LINE_SEPARATOR, 0, WINDOWS_LINE_SEPARATOR.length());
            }
            bufferedWriter.flush();
            byte[] byteArray = outputStream.toByteArray();
            $closeResource(null, bufferedWriter);
            $closeResource(null, bufferedReader);
            $closeResource(null, inputStream);
            $closeResource(null, outputStream);
            return byteArray;
        } catch (IOException e) {
            HwAuthLogger.error("HwCertificationManager", "readManifestAndSkipHwTag IOException!");
            return new byte[0];
        }
    }

    private static boolean isHwCertFound(BufferedReader bufferedReader, ArrayList<String> contents) throws IOException {
        boolean isHwCerFound = false;
        int hwCerLength = HW_CER_TAG.length();
        String line = readLine(bufferedReader);
        while (line != null) {
            if (hwCerLength == line.length() && !isHwCerFound && checkLineEndWithCer(line, hwCerLength) && line.lastIndexOf(HW_CER_NAME) != -1) {
                isHwCerFound = true;
                String nextName = readLine(bufferedReader);
                while (true) {
                    if (nextName == null) {
                        break;
                    } else if (nextName.startsWith("Name:")) {
                        line = nextName;
                        break;
                    } else {
                        nextName = readLine(bufferedReader);
                    }
                }
            }
            if (line.length() != 0) {
                contents.add(line);
            }
            line = readLine(bufferedReader);
        }
        return isHwCerFound;
    }

    private static boolean checkLineEndWithCer(String line, int hwCerLength) {
        if (line.charAt(hwCerLength - 3) == 'C' && line.charAt(hwCerLength - 2) == 'E' && line.charAt(hwCerLength - 1) == 'R') {
            return true;
        }
        return false;
    }

    public static byte[] getManifestFileWithoutHwCer(ZipFile zipFile, ZipEntry zipEntry) {
        if (zipFile == null || zipEntry == null) {
            return new byte[0];
        }
        return readManifestAndSkipHwTag(getManifestFileNew(zipFile, zipEntry));
    }

    public static byte[] getManifestFileWithoutHwCer(File apkFile) {
        if (apkFile == null) {
            return new byte[0];
        }
        return getManifestFileNew(apkFile);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0049, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004a, code lost:
        $closeResource(r3, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004d, code lost:
        throw r4;
     */
    public static InputStream readHwCertFromApk(String apkPath) {
        InputStream inputStream = null;
        if (TextUtils.isEmpty(apkPath)) {
            HwAuthLogger.error("HwCertificationManager", "readHwCertFromApk apk path is null!");
            return null;
        }
        try {
            ZipFile zipFile = new ZipFile(new File(apkPath));
            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
            while (true) {
                if (!enumeration.hasMoreElements()) {
                    break;
                }
                ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
                if (!zipEntry.isDirectory() && CERT_NAME.equals(zipEntry.getName())) {
                    inputStream = zipFile.getInputStream(zipEntry);
                    break;
                }
            }
            $closeResource(null, zipFile);
            return inputStream;
        } catch (IOException e) {
            HwAuthLogger.error("HwCertificationManager", "IOException in readHwCertFromApk!");
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    HwAuthLogger.error("HwCertificationManager", "Failed to close the input stream!");
                }
            }
            return null;
        }
    }

    public static InputStream readHwCertFromApk(ZipFile zipFile, ZipEntry zipEntry) {
        InputStream inputStream = null;
        if (zipFile == null || zipEntry == null) {
            return null;
        }
        try {
            if (zipEntry.isDirectory() || !CERT_NAME.equals(zipEntry.getName())) {
                return null;
            }
            return zipFile.getInputStream(zipEntry);
        } catch (IOException e) {
            HwAuthLogger.error("HwCertificationManager", "IOException in readHwCertFromApk!");
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    HwAuthLogger.error("HwCertificationManager", "Failed to close the input stream in readHwCertFromApk!");
                }
            }
            return null;
        }
    }

    public static byte[] stringToHexBytes(String str) {
        int bytesLength;
        if (TextUtils.isEmpty(str)) {
            return new byte[0];
        }
        int length = str.length();
        if (length % 2 == 0) {
            bytesLength = length / 2;
        } else {
            bytesLength = (length / 2) + 1;
        }
        byte[] bytes = new byte[bytesLength];
        for (int i = 0; i < length; i += 2) {
            if (i + 1 < length) {
                bytes[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
            } else {
                bytes[i / 2] = (byte) (Character.digit(str.charAt(i), 16) << 4);
            }
        }
        return bytes;
    }

    public static String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] chars = new char[(bytes.length * 2)];
        for (int i = 0; i < bytes.length; i++) {
            int byteValue = bytes[i] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY;
            chars[i * 2] = hexChars[byteValue >>> 4];
            chars[(i * 2) + 1] = hexChars[byteValue & 15];
        }
        return String.valueOf(chars);
    }

    public static boolean isPackageInstalled(String packageName, Context context) {
        if (TextUtils.isEmpty(packageName) || context == null) {
            return false;
        }
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isMultiSimEnabled() {
        return TelephonyManager.getDefault().isMultiSimEnabled();
    }

    public static String getSfFileName(ZipFile zipFile) {
        String zipFileName = null;
        if (zipFile == null) {
            return null;
        }
        if (zipFile.getEntry(SF_CERT_NAME) != null) {
            return SF_CERT_NAME;
        }
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            zipFileName = ((ZipEntry) entries.nextElement()).getName();
            if (zipFileName.contains(".SF") && zipFileName.contains("META-INF/")) {
                HwAuthLogger.info("HwCertificationManager", "The SF file name is " + zipFileName);
                return zipFileName;
            }
        }
        return zipFileName;
    }

    public static String readLine(BufferedReader reader) throws IOException {
        int intCh;
        if (reader == null || (intCh = reader.read()) == -1) {
            return null;
        }
        StringBuilder lineBuilder = new StringBuilder(10);
        while (intCh != -1) {
            char ch = (char) intCh;
            if (ch == '\n') {
                break;
            } else if (lineBuilder.length() < 4096) {
                lineBuilder.append(ch);
                intCh = reader.read();
            } else {
                throw new IOException("cert line is too long!");
            }
        }
        String line = lineBuilder.toString();
        if (line.isEmpty() || !line.endsWith("\r")) {
            return line;
        }
        return line.substring(0, line.length() - 1);
    }
}

package huawei.android.hwutil;

import android.hdm.HwDeviceManager;
import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    private static final int BUFFER_BYTE = 4096;
    private static final int BUFF_SIZE = 10240;
    private static final String EXCLUDE_ENTRY = "..";
    private static final int MAX_ENTRY_THEME_SIZE = 200000000;
    private static final String TAG = "ZipUtil";

    public static boolean zipFiles(Collection<File> resFileList, File zipFile) {
        ZipOutputStream zipout = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            FileOutputStream fos2 = new FileOutputStream(zipFile);
            BufferedOutputStream bos2 = new BufferedOutputStream(fos2, BUFF_SIZE);
            ZipOutputStream zipout2 = new ZipOutputStream(bos2);
            for (File resFile : resFileList) {
                zipFile(resFile, zipout2, StorageManagerExt.INVALID_KEY_DESC);
            }
            try {
                zipout2.close();
            } catch (IOException e) {
                Log.e(TAG, "zipFiles do zipout.close catch IOException : " + e.toString());
            }
            try {
                fos2.close();
            } catch (IOException e2) {
                Log.e(TAG, "zipFiles do fos.close catch IOException : " + e2.toString());
            }
            try {
                bos2.close();
            } catch (IOException e3) {
                Log.e(TAG, "zipFiles do bos.close catch IOException : " + e3.toString());
            }
            return true;
        } catch (FileNotFoundException e4) {
            Log.e(TAG, "zipFiles catch FileNotFoundException");
            if (0 != 0) {
                try {
                    zipout.close();
                } catch (IOException e5) {
                    Log.e(TAG, "zipFiles do zipout.close catch IOException : " + e5.toString());
                }
            }
            if (0 != 0) {
                try {
                    fos.close();
                } catch (IOException e6) {
                    Log.e(TAG, "zipFiles do fos.close catch IOException : " + e6.toString());
                }
            }
            if (0 == 0) {
                return false;
            }
            try {
                bos.close();
                return false;
            } catch (IOException e7) {
                Log.e(TAG, "zipFiles do bos.close catch IOException : " + e7.toString());
                return false;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    zipout.close();
                } catch (IOException e8) {
                    Log.e(TAG, "zipFiles do zipout.close catch IOException : " + e8.toString());
                }
            }
            if (0 != 0) {
                try {
                    fos.close();
                } catch (IOException e9) {
                    Log.e(TAG, "zipFiles do fos.close catch IOException : " + e9.toString());
                }
            }
            if (0 != 0) {
                try {
                    bos.close();
                } catch (IOException e10) {
                    Log.e(TAG, "zipFiles do bos.close catch IOException : " + e10.toString());
                }
            }
            throw th;
        }
    }

    public static void zipFiles(Collection<File> resFileList, File zipFile, String comment) throws IOException {
        ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile), BUFF_SIZE));
        for (File resFile : resFileList) {
            zipFile(resFile, zipout, StorageManagerExt.INVALID_KEY_DESC);
        }
        zipout.setComment(comment);
        zipout.close();
    }

    /* JADX INFO: Multiple debug info for r6v11 'out'  java.io.OutputStream: [D('out' java.io.OutputStream), D('desDir' java.io.File)] */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x0201 A[SYNTHETIC, Splitter:B:106:0x0201] */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x0270 A[SYNTHETIC, Splitter:B:136:0x0270] */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x0284 A[SYNTHETIC, Splitter:B:143:0x0284] */
    /* JADX WARNING: Removed duplicated region for block: B:154:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x01d9 A[SYNTHETIC, Splitter:B:97:0x01d9] */
    public static void unZipFile(File zipFile, String folderPath) {
        Throwable th;
        IOException e;
        StringBuilder sb;
        Throwable th2;
        File desDir;
        int i;
        boolean isNewOk;
        OutputStream out;
        Throwable th3;
        OutputStream out2;
        String str = folderPath;
        File desDir2 = new File(str);
        if (desDir2.exists() || desDir2.mkdirs()) {
            int realLength = 0;
            ZipFile zf = null;
            boolean isDisableZipWallpaper = HwDeviceManager.disallowOp(35) && "/data/skin.tmp".equals(str);
            try {
                try {
                    zf = new ZipFile(zipFile);
                    Enumeration<?> entries = zf.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) entries.nextElement();
                        if (entry.getName() != null) {
                            try {
                                if (entry.getName().contains(EXCLUDE_ENTRY)) {
                                }
                            } catch (Exception e2) {
                                try {
                                    Log.e(TAG, "unZipFile catch Exception");
                                    if (zf == null) {
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                if (zf != null) {
                                }
                                throw th;
                            }
                        }
                        if (!(entry.getName() != null && entry.getName().contains("home_wallpaper_0")) || !isDisableZipWallpaper) {
                            String str2 = new String((str + File.separator + entry.getName()).getBytes("8859_1"), "UTF-8");
                            realLength++;
                            try {
                                Log.i(TAG, realLength + "======file=====:" + str2);
                                File desFile = new File(str2);
                                if (entry.isDirectory()) {
                                    desDir = desDir2;
                                    i = realLength;
                                } else {
                                    boolean isNewOk2 = true;
                                    if (!desFile.exists()) {
                                        File fileParentDir = desFile.getParentFile();
                                        if (fileParentDir != null && !fileParentDir.exists()) {
                                            isNewOk2 = fileParentDir.mkdirs();
                                        }
                                        if (isNewOk2) {
                                            isNewOk = desFile.createNewFile();
                                        } else {
                                            isNewOk = isNewOk2;
                                        }
                                    } else {
                                        isNewOk = true;
                                    }
                                    if (isNewOk || desFile.exists()) {
                                        InputStream in = null;
                                        try {
                                            in = zf.getInputStream(entry);
                                            try {
                                                out2 = new FileOutputStream(desFile);
                                            } catch (Throwable th6) {
                                                out = null;
                                                th3 = th6;
                                                if (in != null) {
                                                    try {
                                                        in.close();
                                                    } catch (IOException e3) {
                                                        Log.e(TAG, "unZipFile do in.close catch IOException : " + e3.toString());
                                                    }
                                                }
                                                if (out != null) {
                                                    try {
                                                        out.close();
                                                    } catch (IOException e4) {
                                                        Log.e(TAG, "unZipFile do out.close catch IOException : " + e4.toString());
                                                    }
                                                }
                                                throw th3;
                                            }
                                            try {
                                                byte[] buffer = new byte[BUFF_SIZE];
                                                while (true) {
                                                    try {
                                                        int realLength2 = in.read(buffer);
                                                        if (realLength2 <= 0) {
                                                            break;
                                                        }
                                                        out = out2;
                                                        try {
                                                            out.write(buffer, 0, realLength2);
                                                            in = in;
                                                            out2 = out;
                                                            realLength = realLength;
                                                            str2 = str2;
                                                            desDir2 = desDir2;
                                                        } catch (Throwable th7) {
                                                            in = in;
                                                            th3 = th7;
                                                            if (in != null) {
                                                            }
                                                            if (out != null) {
                                                            }
                                                            throw th3;
                                                        }
                                                    } catch (Throwable th8) {
                                                        out = out2;
                                                        in = in;
                                                        th3 = th8;
                                                        if (in != null) {
                                                        }
                                                        if (out != null) {
                                                        }
                                                        throw th3;
                                                    }
                                                }
                                                desDir = desDir2;
                                                i = realLength;
                                                out2.flush();
                                                try {
                                                    in.close();
                                                } catch (IOException e5) {
                                                    try {
                                                        Log.e(TAG, "unZipFile do in.close catch IOException : " + e5.toString());
                                                    } catch (Exception e6) {
                                                        realLength = i;
                                                    } catch (Throwable th9) {
                                                        th = th9;
                                                        if (zf != null) {
                                                        }
                                                        throw th;
                                                    }
                                                }
                                                try {
                                                    out2.close();
                                                } catch (IOException e7) {
                                                    Log.e(TAG, "unZipFile do out.close catch IOException : " + e7.toString());
                                                }
                                            } catch (Throwable th10) {
                                                out = out2;
                                                th3 = th10;
                                                if (in != null) {
                                                }
                                                if (out != null) {
                                                }
                                                throw th3;
                                            }
                                        } catch (Throwable th11) {
                                            th3 = th11;
                                            out = null;
                                            if (in != null) {
                                            }
                                            if (out != null) {
                                            }
                                            throw th3;
                                        }
                                    } else {
                                        desDir = desDir2;
                                        i = realLength;
                                    }
                                }
                                str = folderPath;
                                realLength = i;
                                desDir2 = desDir;
                            } catch (Exception e8) {
                                Log.e(TAG, "unZipFile catch Exception");
                                if (zf == null) {
                                }
                            } catch (Throwable th12) {
                                th = th12;
                                if (zf != null) {
                                }
                                throw th;
                            }
                        }
                    }
                    try {
                        Log.i(TAG, "unzip end");
                        try {
                            zf.close();
                            return;
                        } catch (IOException e9) {
                            e = e9;
                            sb = new StringBuilder();
                        }
                    } catch (Exception e10) {
                    }
                } catch (Exception e11) {
                    Log.e(TAG, "unZipFile catch Exception");
                    if (zf == null) {
                    }
                } catch (Throwable th13) {
                    th2 = th13;
                    th = th2;
                    if (zf != null) {
                    }
                    throw th;
                }
            } catch (Exception e12) {
                Log.e(TAG, "unZipFile catch Exception");
                if (zf == null) {
                    try {
                        zf.close();
                        return;
                    } catch (IOException e13) {
                        e = e13;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th14) {
                th2 = th14;
                th = th2;
                if (zf != null) {
                    try {
                        zf.close();
                    } catch (IOException e14) {
                        Log.e(TAG, "unZipFile do zf.close catch IOException : " + e14.toString());
                    }
                }
                throw th;
            }
        } else {
            return;
        }
        sb.append("unZipFile do zf.close catch IOException : ");
        sb.append(e.toString());
        Log.e(TAG, sb.toString());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0075, code lost:
        r6 = r6 + r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0076, code lost:
        if (r6 < huawei.android.hwutil.ZipUtil.MAX_ENTRY_THEME_SIZE) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0078, code lost:
        android.util.Log.e(huawei.android.hwutil.ZipUtil.TAG, "isZipError total checkZipIsSize true " + r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0092, code lost:
        r12 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0093, code lost:
        android.util.Log.e(huawei.android.hwutil.ZipUtil.TAG, "isZipError do in.close catch IOException : " + r12.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b3, code lost:
        r5.close();
     */
    public static boolean isZipError(String zipFile) {
        ZipFile zf;
        if (zipFile == null) {
            return true;
        }
        ZipFile zf2 = null;
        InputStream in = null;
        int total = 0;
        try {
            zf = new ZipFile(zipFile);
            Enumeration<?> entries = zf.entries();
            while (entries.hasMoreElements()) {
                in = zf.getInputStream((ZipEntry) entries.nextElement());
                int entrySzie = 0;
                byte[] buffer = new byte[4096];
                while (true) {
                    int bytesRead = in.read(buffer);
                    if (bytesRead < 0) {
                        break;
                    }
                    entrySzie += bytesRead;
                    if (entrySzie >= MAX_ENTRY_THEME_SIZE) {
                        Log.e(TAG, "isZipError entry checkZipIsSize true " + zipFile);
                        try {
                            in.close();
                        } catch (IOException e) {
                            Log.e(TAG, "isZipError do in.close catch IOException : " + e.toString());
                        }
                        try {
                            zf.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "isZipError do zf.close catch Exception");
                        }
                        return true;
                    }
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e3) {
                    Log.e(TAG, "isZipError do in.close catch IOException : " + e3.toString());
                }
            }
            try {
                zf.close();
            } catch (IOException e4) {
                Log.e(TAG, "isZipError do zf.close catch Exception");
            }
            return false;
        } catch (IOException e5) {
            if (0 != 0) {
                try {
                    in.close();
                } catch (IOException e6) {
                    Log.e(TAG, "isZipError do in.close catch IOException : " + e6.toString());
                }
            }
            if (0 != 0) {
                try {
                    zf2.close();
                } catch (IOException e7) {
                    Log.e(TAG, "isZipError do zf.close catch Exception");
                }
            }
            return true;
        } catch (Exception e8) {
            if (0 != 0) {
                try {
                    in.close();
                } catch (IOException e9) {
                    Log.e(TAG, "isZipError do in.close catch IOException : " + e9.toString());
                }
            }
            if (0 != 0) {
                try {
                    zf2.close();
                } catch (IOException e10) {
                    Log.e(TAG, "isZipError do zf.close catch Exception");
                }
            }
            return true;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    in.close();
                } catch (IOException e11) {
                    Log.e(TAG, "isZipError do in.close catch IOException : " + e11.toString());
                }
            }
            if (0 != 0) {
                try {
                    zf2.close();
                } catch (IOException e12) {
                    Log.e(TAG, "isZipError do zf.close catch Exception");
                }
            }
            throw th;
        }
        return true;
        try {
            zf.close();
        } catch (IOException e13) {
            Log.e(TAG, "isZipError do zf.close catch Exception");
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:109:0x01da A[SYNTHETIC, Splitter:B:109:0x01da] */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x01ec A[SYNTHETIC, Splitter:B:116:0x01ec] */
    /* JADX WARNING: Removed duplicated region for block: B:126:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x014f A[SYNTHETIC, Splitter:B:77:0x014f] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0171 A[SYNTHETIC, Splitter:B:83:0x0171] */
    public static void unZipDirectory(String zipFile, String entryName, String desPath) {
        Throwable th;
        IOException e;
        StringBuilder sb;
        Throwable th2;
        File desDir;
        boolean isNewOk;
        Throwable th3;
        String str = desPath;
        File desDir2 = new File(str);
        if (desDir2.exists() || desDir2.mkdirs()) {
            ZipFile zf = null;
            try {
                zf = new ZipFile(zipFile);
                Enumeration<?> entries = zf.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    if (entry.getName() == null || entry.getName().contains(EXCLUDE_ENTRY)) {
                        desDir = desDir2;
                    } else {
                        try {
                            if (entry.getName().contains(entryName)) {
                                File desFile = new File(new String((str + File.separator + entry.getName()).getBytes("8859_1"), "UTF-8"));
                                if (!entry.isDirectory()) {
                                    boolean isNewOk2 = true;
                                    if (!desFile.exists()) {
                                        try {
                                            File fileParentDir = desFile.getParentFile();
                                            if (fileParentDir != null && !fileParentDir.exists()) {
                                                isNewOk2 = fileParentDir.mkdirs();
                                            }
                                            if (isNewOk2) {
                                                isNewOk = desFile.createNewFile();
                                            } else {
                                                isNewOk = isNewOk2;
                                            }
                                        } catch (Exception e2) {
                                            try {
                                                Log.e(TAG, "unZipDirectory catch Exception");
                                                if (zf != null) {
                                                }
                                            } catch (Throwable th4) {
                                                th = th4;
                                            }
                                        } catch (Throwable th5) {
                                            th = th5;
                                            if (zf != null) {
                                            }
                                            throw th;
                                        }
                                    } else {
                                        isNewOk = true;
                                    }
                                    if (isNewOk || desFile.exists()) {
                                        OutputStream out = null;
                                        InputStream in = null;
                                        try {
                                            in = zf.getInputStream(entry);
                                            try {
                                                out = new FileOutputStream(desFile);
                                                byte[] buffer = new byte[BUFF_SIZE];
                                                while (true) {
                                                    try {
                                                        int realLength = in.read(buffer);
                                                        if (realLength <= 0) {
                                                            break;
                                                        }
                                                        try {
                                                            out.write(buffer, 0, realLength);
                                                            in = in;
                                                            desDir2 = desDir2;
                                                        } catch (Throwable th6) {
                                                            in = in;
                                                            th3 = th6;
                                                            if (in != null) {
                                                            }
                                                            if (out != null) {
                                                            }
                                                            throw th3;
                                                        }
                                                    } catch (Throwable th7) {
                                                        in = in;
                                                        th3 = th7;
                                                        if (in != null) {
                                                        }
                                                        if (out != null) {
                                                        }
                                                        throw th3;
                                                    }
                                                }
                                                desDir = desDir2;
                                                try {
                                                    in.close();
                                                } catch (IOException e3) {
                                                    try {
                                                        Log.e(TAG, "unZipDirectory do in.close catch IOException : " + e3.toString());
                                                    } catch (Exception e4) {
                                                    }
                                                }
                                                try {
                                                    out.close();
                                                } catch (IOException e5) {
                                                    Log.e(TAG, "unZipDirectory do out.close catch IOException : " + e5.toString());
                                                }
                                            } catch (Throwable th8) {
                                                th3 = th8;
                                                if (in != null) {
                                                    try {
                                                        in.close();
                                                    } catch (IOException e6) {
                                                        Log.e(TAG, "unZipDirectory do in.close catch IOException : " + e6.toString());
                                                    }
                                                }
                                                if (out != null) {
                                                    try {
                                                        out.close();
                                                    } catch (IOException e7) {
                                                        Log.e(TAG, "unZipDirectory do out.close catch IOException : " + e7.toString());
                                                    }
                                                }
                                                throw th3;
                                            }
                                        } catch (Throwable th9) {
                                            th3 = th9;
                                            if (in != null) {
                                            }
                                            if (out != null) {
                                            }
                                            throw th3;
                                        }
                                    } else {
                                        desDir = desDir2;
                                    }
                                }
                            } else {
                                desDir = desDir2;
                            }
                        } catch (Exception e8) {
                            Log.e(TAG, "unZipDirectory catch Exception");
                            if (zf != null) {
                            }
                        } catch (Throwable th10) {
                            th2 = th10;
                            th = th2;
                            if (zf != null) {
                            }
                            throw th;
                        }
                    }
                    str = desPath;
                    desDir2 = desDir;
                }
                try {
                    zf.close();
                    return;
                } catch (IOException e9) {
                    e = e9;
                    sb = new StringBuilder();
                }
            } catch (Exception e10) {
                Log.e(TAG, "unZipDirectory catch Exception");
                if (zf != null) {
                    try {
                        zf.close();
                        return;
                    } catch (IOException e11) {
                        e = e11;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th11) {
                th2 = th11;
                th = th2;
                if (zf != null) {
                    try {
                        zf.close();
                    } catch (IOException e12) {
                        Log.e(TAG, "unZipDirectory do zf.close catch IOException : " + e12.toString());
                    }
                }
                throw th;
            }
        } else {
            return;
        }
        sb.append("unZipDirectory do zf.close catch IOException : ");
        sb.append(e.toString());
        Log.e(TAG, sb.toString());
    }

    /* JADX WARNING: Removed duplicated region for block: B:110:0x01ba A[SYNTHETIC, Splitter:B:110:0x01ba] */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x01de A[SYNTHETIC, Splitter:B:121:0x01de] */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x0202 A[SYNTHETIC, Splitter:B:133:0x0202] */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x0226 A[SYNTHETIC, Splitter:B:144:0x0226] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0158 A[SYNTHETIC, Splitter:B:85:0x0158] */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x017c A[SYNTHETIC, Splitter:B:96:0x017c] */
    public static boolean upZipSelectedFile(File form, String to, String obj) throws ZipException, IOException {
        File targetDir;
        File targetDir2;
        boolean isNewOk;
        Throwable th;
        IOException e1;
        String str = to;
        if (form == null) {
            return false;
        }
        File targetDir3 = new File(str);
        File file = null;
        if (!targetDir3.exists()) {
            Log.d("ive", "add audio dir");
            if (!targetDir3.mkdir()) {
                return false;
            }
        }
        ZipFile zf = new ZipFile(form);
        Enumeration<?> entries = zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.getName().contains(obj)) {
                String fileName = str + entry.getName();
                File desFile = new File(fileName);
                if (!fileName.contains(EXCLUDE_ENTRY)) {
                    if (entry.isDirectory()) {
                        str = to;
                        targetDir3 = targetDir3;
                    } else if (!desFile.exists()) {
                        File fileParentDir = desFile.getParentFile();
                        if (fileParentDir == null || fileParentDir.exists()) {
                            isNewOk = true;
                        } else {
                            isNewOk = fileParentDir.mkdirs();
                        }
                        if (isNewOk) {
                            try {
                                isNewOk = desFile.createNewFile();
                            } catch (IOException e) {
                                Log.e(TAG, " create new file failed");
                            }
                        }
                        if (!isNewOk) {
                            file = null;
                        } else {
                            InputStream inStream = null;
                            OutputStream outStream = null;
                            try {
                                byte[] buffer = new byte[4096];
                                inStream = zf.getInputStream(entry);
                                OutputStream outStream2 = new FileOutputStream(desFile);
                                while (true) {
                                    try {
                                        int realLength = inStream.read(buffer);
                                        if (realLength <= 0) {
                                            break;
                                        }
                                        targetDir = targetDir3;
                                        targetDir2 = null;
                                        try {
                                            outStream2.write(buffer, 0, realLength);
                                            targetDir3 = targetDir;
                                        } catch (IOException e2) {
                                            e1 = e2;
                                            outStream = outStream2;
                                            Log.e(TAG, "IOException is " + e1);
                                            if (inStream != null) {
                                                try {
                                                    inStream.close();
                                                } catch (IOException ioe) {
                                                    Log.e(TAG, "ioe is " + ioe);
                                                } catch (Throwable th2) {
                                                    throw th2;
                                                }
                                            }
                                            if (outStream != null) {
                                                try {
                                                    outStream.close();
                                                } catch (IOException ioexception) {
                                                    Log.e(TAG, "ioexception is " + ioexception);
                                                } catch (Throwable th3) {
                                                    throw th3;
                                                }
                                            }
                                            str = to;
                                            file = targetDir2;
                                            targetDir3 = targetDir;
                                        } catch (Exception e3) {
                                            outStream = outStream2;
                                            try {
                                                Log.e(TAG, "upZipSelectedFile catch Exception");
                                                if (inStream != null) {
                                                    try {
                                                        inStream.close();
                                                    } catch (IOException ioe2) {
                                                        Log.e(TAG, "ioe is " + ioe2);
                                                    } catch (Throwable th4) {
                                                        throw th4;
                                                    }
                                                }
                                                if (outStream != null) {
                                                    try {
                                                        outStream.close();
                                                    } catch (IOException ioexception2) {
                                                        Log.e(TAG, "ioexception is " + ioexception2);
                                                    } catch (Throwable th5) {
                                                        throw th5;
                                                    }
                                                }
                                                str = to;
                                                file = targetDir2;
                                                targetDir3 = targetDir;
                                            } catch (Throwable th6) {
                                                th = th6;
                                                if (inStream != null) {
                                                }
                                                if (outStream != null) {
                                                }
                                                throw th;
                                            }
                                        } catch (Throwable th7) {
                                            outStream = outStream2;
                                            th = th7;
                                            if (inStream != null) {
                                                try {
                                                    inStream.close();
                                                } catch (IOException ioe3) {
                                                    Log.e(TAG, "ioe is " + ioe3);
                                                } catch (Throwable th8) {
                                                    throw th8;
                                                }
                                            }
                                            if (outStream != null) {
                                                try {
                                                    outStream.close();
                                                } catch (IOException ioexception3) {
                                                    Log.e(TAG, "ioexception is " + ioexception3);
                                                } catch (Throwable th9) {
                                                    throw th9;
                                                }
                                            }
                                            throw th;
                                        }
                                    } catch (IOException e4) {
                                        e1 = e4;
                                        targetDir = targetDir3;
                                        targetDir2 = null;
                                        outStream = outStream2;
                                        Log.e(TAG, "IOException is " + e1);
                                        if (inStream != null) {
                                        }
                                        if (outStream != null) {
                                        }
                                        str = to;
                                        file = targetDir2;
                                        targetDir3 = targetDir;
                                    } catch (Exception e5) {
                                        targetDir = targetDir3;
                                        targetDir2 = null;
                                        outStream = outStream2;
                                        Log.e(TAG, "upZipSelectedFile catch Exception");
                                        if (inStream != null) {
                                        }
                                        if (outStream != null) {
                                        }
                                        str = to;
                                        file = targetDir2;
                                        targetDir3 = targetDir;
                                    } catch (Throwable th10) {
                                        outStream = outStream2;
                                        th = th10;
                                        if (inStream != null) {
                                        }
                                        if (outStream != null) {
                                        }
                                        throw th;
                                    }
                                }
                                targetDir = targetDir3;
                                targetDir2 = null;
                                try {
                                    inStream.close();
                                } catch (IOException ioe4) {
                                    Log.e(TAG, "ioe is " + ioe4);
                                } catch (Throwable th11) {
                                    throw th11;
                                }
                                try {
                                    outStream2.close();
                                } catch (IOException ioexception4) {
                                    Log.e(TAG, "ioexception is " + ioexception4);
                                } catch (Throwable th12) {
                                    throw th12;
                                }
                            } catch (IOException e6) {
                                e1 = e6;
                                targetDir = targetDir3;
                                targetDir2 = null;
                                Log.e(TAG, "IOException is " + e1);
                                if (inStream != null) {
                                }
                                if (outStream != null) {
                                }
                                str = to;
                                file = targetDir2;
                                targetDir3 = targetDir;
                            } catch (Exception e7) {
                                targetDir = targetDir3;
                                targetDir2 = null;
                                Log.e(TAG, "upZipSelectedFile catch Exception");
                                if (inStream != null) {
                                }
                                if (outStream != null) {
                                }
                                str = to;
                                file = targetDir2;
                                targetDir3 = targetDir;
                            } catch (Throwable th13) {
                                th = th13;
                                if (inStream != null) {
                                }
                                if (outStream != null) {
                                }
                                throw th;
                            }
                        }
                    } else {
                        str = to;
                        targetDir3 = targetDir3;
                    }
                }
            } else {
                targetDir = targetDir3;
                targetDir2 = file;
            }
            str = to;
            file = targetDir2;
            targetDir3 = targetDir;
        }
        try {
            zf.close();
        } catch (IOException e8) {
            Log.e(TAG, "upZipSelectedFile do zf.close catch IOException : " + e8.toString());
        }
        return false;
    }

    public static ArrayList<String> getEntriesNames(File zipFile) throws ZipException, IOException {
        ArrayList<String> entryNames = new ArrayList<>();
        ZipFile zf = new ZipFile(zipFile);
        try {
            Enumeration<?> entries = zf.entries();
            while (entries.hasMoreElements()) {
                String entryName = getEntryName((ZipEntry) entries.nextElement());
                if (entryName != null) {
                    entryNames.add(entryName);
                }
            }
            try {
                zf.close();
            } catch (Exception e) {
                Log.e(TAG, "getEntriesNames catch Exception in finally");
            }
        } catch (Exception e2) {
            Log.e(TAG, "getEntriesNames catch Exception");
            zf.close();
        } catch (Throwable th) {
            try {
                zf.close();
            } catch (Exception e3) {
                Log.e(TAG, "getEntriesNames catch Exception in finally");
            }
            throw th;
        }
        return entryNames;
    }

    public static String getEntryComment(ZipEntry entry) throws UnsupportedEncodingException {
        return new String(entry.getComment().getBytes("GB2312"), "8859_1");
    }

    public static String getEntryName(ZipEntry entry) throws UnsupportedEncodingException {
        if (entry.getName() == null || !entry.getName().contains(EXCLUDE_ENTRY)) {
            return new String(entry.getName().getBytes("GB2312"), "8859_1");
        }
        return null;
    }

    private static void zipFile(File resFile, ZipOutputStream zipout, String rootpath) {
        StringBuilder sb;
        StringBuilder sb2 = new StringBuilder();
        sb2.append(rootpath);
        sb2.append(rootpath.trim().length() == 0 ? StorageManagerExt.INVALID_KEY_DESC : File.separator);
        sb2.append(resFile.getName());
        String rootpath2 = sb2.toString();
        BufferedInputStream bis = null;
        FileInputStream fis = null;
        try {
            String rootpath3 = new String(rootpath2.getBytes("8859_1"), "GB2312");
            if (resFile.isDirectory()) {
                File[] fileList = resFile.listFiles();
                if (fileList != null) {
                    for (File file : fileList) {
                        zipFile(file, zipout, rootpath3);
                    }
                }
                zipout.putNextEntry(new ZipEntry(rootpath3));
            } else {
                byte[] buffer = new byte[BUFF_SIZE];
                fis = new FileInputStream(resFile);
                bis = new BufferedInputStream(fis, BUFF_SIZE);
                zipout.putNextEntry(new ZipEntry(rootpath3));
                while (true) {
                    int realLength = bis.read(buffer);
                    if (realLength == -1) {
                        break;
                    }
                    zipout.write(buffer, 0, realLength);
                }
                bis.close();
                zipout.flush();
                zipout.closeEntry();
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    Log.e(TAG, "zipFile do bis.close catch IOException : " + e.toString());
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                    return;
                } catch (IOException e2) {
                    e = e2;
                    sb = new StringBuilder();
                }
            } else {
                return;
            }
            sb.append("zipFile do fis.close catch IOException : ");
            sb.append(e.toString());
            Log.e(TAG, sb.toString());
        } catch (Exception e3) {
            Log.e(TAG, "zipFile catch Exception");
            if (0 != 0) {
                try {
                    bis.close();
                } catch (IOException e4) {
                    Log.e(TAG, "zipFile do bis.close catch IOException : " + e4.toString());
                }
            }
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bis.close();
                } catch (IOException e6) {
                    Log.e(TAG, "zipFile do bis.close catch IOException : " + e6.toString());
                }
            }
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e7) {
                    Log.e(TAG, "zipFile do fis.close catch IOException : " + e7.toString());
                }
            }
            throw th;
        }
    }

    public static void unZipFile(String zipFile, String entryName, String desPath) {
        StringBuilder sb;
        ZipFile zf = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            ZipFile zf2 = new ZipFile(zipFile);
            ZipEntry zipEntry = zf2.getEntry(entryName);
            if (!(zipEntry == null || entryName == null)) {
                if (!entryName.contains(EXCLUDE_ENTRY)) {
                    InputStream in2 = zf2.getInputStream(zipEntry);
                    OutputStream out2 = new FileOutputStream(new String((desPath + File.separator + entryName).getBytes("8859_1"), "GB2312"));
                    byte[] buffer = new byte[4096];
                    while (true) {
                        int realLength = in2.read(buffer);
                        if (realLength > 0) {
                            out2.write(buffer, 0, realLength);
                        } else {
                            try {
                                break;
                            } catch (IOException e) {
                                Log.e(TAG, "unZipFile-String do in.close catch IOException : " + e.toString());
                            }
                        }
                    }
                    in2.close();
                    try {
                        out2.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "unZipFile-String do out.close catch IOException : " + e2.toString());
                    }
                    try {
                        zf2.close();
                        return;
                    } catch (IOException e3) {
                        e = e3;
                        sb = new StringBuilder();
                    }
                }
            }
            if (0 != 0) {
                try {
                    in.close();
                } catch (IOException e4) {
                    Log.e(TAG, "unZipFile-String do in.close catch IOException : " + e4.toString());
                }
            }
            if (0 != 0) {
                try {
                    out.close();
                } catch (IOException e5) {
                    Log.e(TAG, "unZipFile-String do out.close catch IOException : " + e5.toString());
                }
            }
            try {
                zf2.close();
                return;
            } catch (IOException e6) {
                Log.e(TAG, "unZipFile-String do zf.close catch IOException : " + e6.toString());
                return;
            }
            sb.append("unZipFile-String do zf.close catch IOException : ");
            sb.append(e.toString());
            Log.e(TAG, sb.toString());
        } catch (Exception e7) {
            Log.e(TAG, "unZipFile-String catch Exception");
            if (0 != 0) {
                try {
                    in.close();
                } catch (IOException e8) {
                    Log.e(TAG, "unZipFile-String do in.close catch IOException : " + e8.toString());
                }
            }
            if (0 != 0) {
                try {
                    out.close();
                } catch (IOException e9) {
                    Log.e(TAG, "unZipFile-String do out.close catch IOException : " + e9.toString());
                }
            }
            if (0 != 0) {
                try {
                    zf.close();
                } catch (IOException e10) {
                    e = e10;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    in.close();
                } catch (IOException e11) {
                    Log.e(TAG, "unZipFile-String do in.close catch IOException : " + e11.toString());
                }
            }
            if (0 != 0) {
                try {
                    out.close();
                } catch (IOException e12) {
                    Log.e(TAG, "unZipFile-String do out.close catch IOException : " + e12.toString());
                }
            }
            if (0 != 0) {
                try {
                    zf.close();
                } catch (IOException e13) {
                    Log.e(TAG, "unZipFile-String do zf.close catch IOException : " + e13.toString());
                }
            }
            throw th;
        }
    }

    public static void unZipFile(File zipFile, String desPath, String entryName) {
        if (zipFile != null && entryName != null && !entryName.contains(EXCLUDE_ENTRY)) {
            InputStream inStream = null;
            OutputStream outStream = null;
            ZipFile zf = null;
            try {
                zf = new ZipFile(zipFile);
                ZipEntry zipEntry = zf.getEntry(entryName);
                if (zipEntry == null) {
                    try {
                        zf.close();
                    } catch (IOException e) {
                        Log.e(TAG, "unZipFile-File do zf.close catch IOException : " + e.toString());
                    }
                    if (0 != 0) {
                        try {
                            inStream.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "unZipFile-File do inStream.close catch IOException : " + e2.toString());
                        }
                    }
                    if (0 != 0) {
                        try {
                            outStream.close();
                        } catch (IOException e3) {
                            Log.e(TAG, "unZipFile-File do outStream.close catch IOException : " + e3.toString());
                        }
                    }
                } else {
                    InputStream inStream2 = zf.getInputStream(zipEntry);
                    OutputStream outStream2 = new FileOutputStream(new String((desPath + File.separator + entryName).getBytes("8859_1"), "GB2312"));
                    byte[] buffer = new byte[4096];
                    while (true) {
                        int realLength = inStream2.read(buffer);
                        if (realLength > 0) {
                            outStream2.write(buffer, 0, realLength);
                        } else {
                            try {
                                break;
                            } catch (IOException e4) {
                                Log.e(TAG, "unZipFile-File do zf.close catch IOException : " + e4.toString());
                            }
                        }
                    }
                    zf.close();
                    try {
                        inStream2.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "unZipFile-File do inStream.close catch IOException : " + e5.toString());
                    }
                    try {
                        outStream2.close();
                    } catch (IOException e6) {
                        Log.e(TAG, "unZipFile-File do outStream.close catch IOException : " + e6.toString());
                    }
                }
            } catch (Exception e7) {
                Log.e(TAG, "unZipFile-File catch Exception");
                if (zf != null) {
                    try {
                        zf.close();
                    } catch (IOException e8) {
                        Log.e(TAG, "unZipFile-File do zf.close catch IOException : " + e8.toString());
                    }
                }
                if (0 != 0) {
                    try {
                        inStream.close();
                    } catch (IOException e9) {
                        Log.e(TAG, "unZipFile-File do inStream.close catch IOException : " + e9.toString());
                    }
                }
                if (0 != 0) {
                    try {
                        outStream.close();
                    } catch (IOException e10) {
                        Log.e(TAG, "unZipFile-File do outStream.close catch IOException : " + e10.toString());
                    }
                }
            } catch (Throwable th) {
                if (zf != null) {
                    try {
                        zf.close();
                    } catch (IOException e11) {
                        Log.e(TAG, "unZipFile-File do zf.close catch IOException : " + e11.toString());
                    }
                }
                if (0 != 0) {
                    try {
                        inStream.close();
                    } catch (IOException e12) {
                        Log.e(TAG, "unZipFile-File do inStream.close catch IOException : " + e12.toString());
                    }
                }
                if (0 != 0) {
                    try {
                        outStream.close();
                    } catch (IOException e13) {
                        Log.e(TAG, "unZipFile-File do outStream.close catch IOException : " + e13.toString());
                    }
                }
                throw th;
            }
        }
    }
}

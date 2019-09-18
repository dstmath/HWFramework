package huawei.android.hwutil;

import android.util.Log;
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
                zipFile(resFile, zipout2, "");
            }
            try {
                zipout2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fos2.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            try {
                bos2.close();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
            return true;
        } catch (FileNotFoundException e4) {
            e4.printStackTrace();
            if (zipout != null) {
                try {
                    zipout.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            return false;
        } catch (Throwable th) {
            if (zipout != null) {
                try {
                    zipout.close();
                } catch (IOException e8) {
                    e8.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e9) {
                    e9.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e10) {
                    e10.printStackTrace();
                }
            }
            throw th;
        }
    }

    public static void zipFiles(Collection<File> resFileList, File zipFile, String comment) throws IOException {
        ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile), BUFF_SIZE));
        for (File resFile : resFileList) {
            zipFile(resFile, zipout, "");
        }
        zipout.setComment(comment);
        zipout.close();
    }

    public static void unZipFile(File zipFile, String folderPath) {
        String str;
        OutputStream out;
        InputStream in;
        File desDir = new File(folderPath);
        if (desDir.exists() || desDir.mkdirs()) {
            int i = 0;
            ZipFile zf = null;
            try {
                zf = new ZipFile(zipFile);
                Enumeration<? extends ZipEntry> entries = zf.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    if (entry.getName() == null || !entry.getName().contains(EXCLUDE_ENTRY)) {
                        i++;
                        Log.i(TAG, i + "======file=====:" + str);
                        File desFile = new File(str);
                        if (!entry.isDirectory()) {
                            boolean newOk = true;
                            if (!desFile.exists()) {
                                File fileParentDir = desFile.getParentFile();
                                if (fileParentDir != null && !fileParentDir.exists()) {
                                    newOk = fileParentDir.mkdirs();
                                }
                                if (newOk) {
                                    newOk = desFile.createNewFile();
                                }
                            }
                            if (newOk || desFile.exists()) {
                                out = null;
                                in = null;
                                InputStream in2 = zf.getInputStream(entry);
                                OutputStream out2 = new FileOutputStream(desFile);
                                byte[] buffer = new byte[BUFF_SIZE];
                                while (true) {
                                    int read = in2.read(buffer);
                                    int realLength = read;
                                    if (read <= 0) {
                                        break;
                                    }
                                    out2.write(buffer, 0, realLength);
                                }
                                out2.flush();
                                if (in2 != null) {
                                    try {
                                        in2.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    out2.close();
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                    }
                }
                Log.i(TAG, "unzip end");
                try {
                    zf.close();
                } catch (IOException e3) {
                }
            } catch (Exception e4) {
                try {
                    e4.printStackTrace();
                } finally {
                    if (zf != null) {
                        try {
                            zf.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                }
            } catch (Throwable th) {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0060, code lost:
        r4 = r4 + r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0061, code lost:
        if (r4 < MAX_ENTRY_THEME_SIZE) goto L_0x008e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        android.util.Log.e(TAG, "isZipError total checkZipIsSize true " + r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x007a, code lost:
        if (r2 == null) goto L_0x0085;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0080, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0081, code lost:
        r3.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        r2.close();
     */
    public static boolean isZipError(String zipFile) {
        ZipFile zf;
        if (zipFile == null) {
            return true;
        }
        zf = null;
        InputStream in = null;
        int total = 0;
        try {
            zf = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                in = zf.getInputStream((ZipEntry) entries.nextElement());
                int entrySzie = 0;
                byte[] buffer = new byte[4096];
                while (true) {
                    int read = in.read(buffer);
                    int bytesRead = read;
                    if (read < 0) {
                        break;
                    }
                    entrySzie += bytesRead;
                    if (entrySzie >= MAX_ENTRY_THEME_SIZE) {
                        Log.e(TAG, "isZipError entry checkZipIsSize true " + zipFile);
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            zf.close();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                        return true;
                    }
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            try {
                zf.close();
            } catch (Exception e22) {
                e22.printStackTrace();
            }
            return false;
        } catch (IOException e4) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            if (zf != null) {
                try {
                    zf.close();
                } catch (Exception e23) {
                    e23.printStackTrace();
                }
            }
            return true;
        } catch (Exception e6) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            if (zf != null) {
                try {
                    zf.close();
                } catch (Exception e24) {
                    e24.printStackTrace();
                }
            }
            return true;
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e8) {
                    e8.printStackTrace();
                }
            }
            if (zf != null) {
                try {
                    zf.close();
                } catch (Exception e25) {
                    e25.printStackTrace();
                }
            }
            throw th;
        }
        try {
            zf.close();
        } catch (Exception e26) {
            e26.printStackTrace();
        }
        return true;
        return true;
    }

    public static void unZipDirectory(String zipFile, String entryName, String desPath) {
        OutputStream out;
        InputStream in;
        File desDir = new File(desPath);
        if (desDir.exists() || desDir.mkdirs()) {
            ZipFile zf = null;
            try {
                zf = new ZipFile(zipFile);
                Enumeration<? extends ZipEntry> entries = zf.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    if (entry.getName() != null && !entry.getName().contains(EXCLUDE_ENTRY) && entry.getName().contains(entryName)) {
                        File desFile = new File(new String((desPath + File.separator + entry.getName()).getBytes("8859_1"), "UTF-8"));
                        if (!entry.isDirectory()) {
                            boolean newOk = true;
                            if (!desFile.exists()) {
                                File fileParentDir = desFile.getParentFile();
                                if (fileParentDir != null && !fileParentDir.exists()) {
                                    newOk = fileParentDir.mkdirs();
                                }
                                if (newOk) {
                                    newOk = desFile.createNewFile();
                                }
                            }
                            if (newOk || desFile.exists()) {
                                out = null;
                                in = null;
                                InputStream in2 = zf.getInputStream(entry);
                                OutputStream out2 = new FileOutputStream(desFile);
                                byte[] buffer = new byte[BUFF_SIZE];
                                while (true) {
                                    int read = in2.read(buffer);
                                    int realLength = read;
                                    if (read <= 0) {
                                        break;
                                    }
                                    out2.write(buffer, 0, realLength);
                                }
                                if (in2 != null) {
                                    try {
                                        in2.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    out2.close();
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                    }
                }
                try {
                    zf.close();
                } catch (IOException e3) {
                }
            } catch (Exception e4) {
                try {
                    e4.printStackTrace();
                } finally {
                    if (zf != null) {
                        try {
                            zf.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                }
            } catch (Throwable th) {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }

    public static boolean upZipSelectedFile(File form, String to, String obj) throws ZipException, IOException {
        File file = form;
        String str = to;
        if (file == null) {
            return false;
        }
        File targetDir = new File(str);
        if (!targetDir.exists()) {
            Log.d("ive", "add audio dir");
            if (!targetDir.mkdir()) {
                return false;
            }
        }
        ZipFile zf = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (true) {
            Enumeration<? extends ZipEntry> enumeration = entries;
            if (!enumeration.hasMoreElements()) {
                break;
            }
            ZipEntry entry = (ZipEntry) enumeration.nextElement();
            if (entry.getName().contains(obj)) {
                String fileName = str + entry.getName();
                File desFile = new File(fileName);
                if (!fileName.contains(EXCLUDE_ENTRY) && !entry.isDirectory()) {
                    boolean newOk = true;
                    if (!desFile.exists()) {
                        File fileParentDir = desFile.getParentFile();
                        if (fileParentDir != null && !fileParentDir.exists()) {
                            newOk = fileParentDir.mkdirs();
                        }
                        boolean newOk2 = newOk;
                        if (newOk2) {
                            try {
                                newOk2 = desFile.createNewFile();
                            } catch (IOException e) {
                                IOException iOException = e;
                                Log.e(TAG, " create new file failed");
                            }
                        }
                        if (newOk2) {
                            InputStream inStream = null;
                            OutputStream outStream = null;
                            try {
                                byte[] buffer = new byte[4096];
                                InputStream inStream2 = zf.getInputStream(entry);
                                OutputStream outStream2 = new FileOutputStream(desFile);
                                while (true) {
                                    int read = inStream2.read(buffer);
                                    int realLength = read;
                                    if (read <= 0) {
                                        break;
                                    }
                                    int realLength2 = realLength;
                                    outStream2.write(buffer, 0, realLength2);
                                    int i = realLength2;
                                    File file2 = form;
                                }
                                if (inStream2 != null) {
                                    try {
                                        inStream2.close();
                                    } catch (IOException ioe) {
                                        IOException iOException2 = ioe;
                                        Log.e(TAG, "ioe is " + ioe);
                                    } catch (Throwable th) {
                                        throw th;
                                    }
                                }
                                try {
                                    outStream2.close();
                                } catch (IOException ioexception) {
                                    IOException iOException3 = ioexception;
                                    Log.e(TAG, "ioexception is " + ioexception);
                                } catch (Throwable th2) {
                                    throw th2;
                                }
                            } catch (IOException e1) {
                                Log.e(TAG, "IOException is " + e1);
                                if (inStream != null) {
                                    try {
                                        inStream.close();
                                    } catch (IOException ioe2) {
                                        IOException iOException4 = ioe2;
                                        Log.e(TAG, "ioe is " + ioe2);
                                    } catch (Throwable th3) {
                                        throw th3;
                                    }
                                }
                                if (outStream != null) {
                                    try {
                                        outStream.close();
                                    } catch (IOException ioexception2) {
                                        IOException iOException5 = ioexception2;
                                        Log.e(TAG, "ioexception is " + ioexception2);
                                    } catch (Throwable th4) {
                                        throw th4;
                                    }
                                }
                            } catch (Exception e2) {
                                Log.e(TAG, "Exception is " + e2);
                                if (inStream != null) {
                                    try {
                                        inStream.close();
                                    } catch (IOException ioe3) {
                                        IOException iOException6 = ioe3;
                                        Log.e(TAG, "ioe is " + ioe3);
                                    } catch (Throwable th5) {
                                        throw th5;
                                    }
                                }
                                if (outStream != null) {
                                    try {
                                        outStream.close();
                                    } catch (IOException ioexception3) {
                                        IOException iOException7 = ioexception3;
                                        Log.e(TAG, "ioexception is " + ioexception3);
                                    } catch (Throwable th6) {
                                        throw th6;
                                    }
                                }
                            } catch (Throwable th7) {
                                Throwable th8 = th7;
                                if (inStream != null) {
                                    try {
                                        inStream.close();
                                        File file3 = targetDir;
                                    } catch (IOException ioe4) {
                                        IOException iOException8 = ioe4;
                                        StringBuilder sb = new StringBuilder();
                                        File file4 = targetDir;
                                        sb.append("ioe is ");
                                        sb.append(ioe4);
                                        Log.e(TAG, sb.toString());
                                    } catch (Throwable th9) {
                                        th = th9;
                                    }
                                }
                                if (outStream != null) {
                                    try {
                                        outStream.close();
                                    } catch (IOException ioexception4) {
                                        IOException iOException9 = ioexception4;
                                        Log.e(TAG, "ioexception is " + ioexception4);
                                    } catch (Throwable th10) {
                                        throw th10;
                                    }
                                }
                                throw th8;
                            }
                        }
                    }
                }
            }
            entries = enumeration;
            targetDir = targetDir;
            File file5 = form;
            str = to;
        }
        String str2 = obj;
        File file6 = targetDir;
        try {
            zf.close();
        } catch (IOException e3) {
            IOException iOException10 = e3;
            e3.printStackTrace();
        }
        return false;
        throw th;
    }

    public static ArrayList<String> getEntriesNames(File zipFile) throws ZipException, IOException {
        String str;
        StringBuilder sb;
        ArrayList<String> entryNames = new ArrayList<>();
        ZipFile zf = new ZipFile(zipFile);
        try {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                String entryName = getEntryName((ZipEntry) entries.nextElement());
                if (entryName != null) {
                    entryNames.add(entryName);
                }
            }
            try {
                zf.close();
            } catch (Exception e) {
                e1 = e;
                str = TAG;
                sb = new StringBuilder();
            }
        } catch (Exception e2) {
            Log.e(TAG, "Exception e: " + e2);
            try {
                zf.close();
            } catch (Exception e3) {
                e1 = e3;
                str = TAG;
                sb = new StringBuilder();
            }
        } catch (Throwable th) {
            try {
                zf.close();
            } catch (Exception e1) {
                Log.e(TAG, "Exception e1: " + e1);
            }
            throw th;
        }
        return entryNames;
        sb.append("Exception e1: ");
        sb.append(e1);
        Log.e(str, sb.toString());
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
        StringBuilder sb = new StringBuilder();
        sb.append(rootpath);
        sb.append(rootpath.trim().length() == 0 ? "" : File.separator);
        sb.append(resFile.getName());
        String rootpath2 = sb.toString();
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
                    int read = bis.read(buffer);
                    int realLength = read;
                    if (read == -1) {
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
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        } catch (Exception e3) {
            e3.printStackTrace();
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            if (fis != null) {
                fis.close();
            }
        } catch (Throwable th) {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            throw th;
        }
    }

    public static void unZipFile(String zipFile, String entryName, String desPath) {
        ZipFile zf = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            ZipFile zf2 = new ZipFile(zipFile);
            ZipEntry zEntry = zf2.getEntry(entryName);
            if (!(zEntry == null || entryName == null)) {
                if (!entryName.contains(EXCLUDE_ENTRY)) {
                    InputStream in2 = zf2.getInputStream(zEntry);
                    OutputStream out2 = new FileOutputStream(new String((desPath + File.separator + entryName).getBytes("8859_1"), "GB2312"));
                    byte[] buffer = new byte[4096];
                    while (true) {
                        int read = in2.read(buffer);
                        int realLength = read;
                        if (read <= 0) {
                            break;
                        }
                        out2.write(buffer, 0, realLength);
                    }
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        out2.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    try {
                        zf2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                    return;
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            try {
                zf2.close();
            } catch (IOException e6) {
                e6.printStackTrace();
            }
        } catch (Exception e7) {
            e7.printStackTrace();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e8) {
                    e8.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e9) {
                    e9.printStackTrace();
                }
            }
            if (zf != null) {
                zf.close();
            }
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e10) {
                    e10.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e11) {
                    e11.printStackTrace();
                }
            }
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e12) {
                    e12.printStackTrace();
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
                ZipEntry zEntry = zf.getEntry(entryName);
                if (zEntry == null) {
                    try {
                        zf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (inStream != null) {
                        try {
                            inStream.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (outStream != null) {
                        try {
                            outStream.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    return;
                }
                InputStream inStream2 = zf.getInputStream(zEntry);
                OutputStream outStream2 = new FileOutputStream(new String((desPath + File.separator + entryName).getBytes("8859_1"), "GB2312"));
                byte[] buffer = new byte[4096];
                while (true) {
                    int read = inStream2.read(buffer);
                    int realLength = read;
                    if (read > 0) {
                        outStream2.write(buffer, 0, realLength);
                    } else {
                        try {
                            break;
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                }
                zf.close();
                if (inStream2 != null) {
                    try {
                        inStream2.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                try {
                    outStream2.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            } catch (Exception e7) {
                e7.printStackTrace();
                if (zf != null) {
                    try {
                        zf.close();
                    } catch (IOException e8) {
                        e8.printStackTrace();
                    }
                }
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (IOException e9) {
                        e9.printStackTrace();
                    }
                }
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e10) {
                        e10.printStackTrace();
                    }
                }
            } catch (Throwable th) {
                if (zf != null) {
                    try {
                        zf.close();
                    } catch (IOException e11) {
                        e11.printStackTrace();
                    }
                }
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (IOException e12) {
                        e12.printStackTrace();
                    }
                }
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e13) {
                        e13.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }
}

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
        FileNotFoundException e;
        Throwable th;
        ZipOutputStream zipOutputStream = null;
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bos = null;
        try {
            FileOutputStream fos = new FileOutputStream(zipFile);
            try {
                BufferedOutputStream bos2 = new BufferedOutputStream(fos, BUFF_SIZE);
                try {
                    ZipOutputStream zipout = new ZipOutputStream(bos2);
                    try {
                        for (File resFile : resFileList) {
                            zipFile(resFile, zipout, "");
                        }
                        if (zipout != null) {
                            try {
                                zipout.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                        if (bos2 != null) {
                            try {
                                bos2.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                        }
                        return true;
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        bos = bos2;
                        fileOutputStream = fos;
                        zipOutputStream = zipout;
                        try {
                            e.printStackTrace();
                            if (zipOutputStream != null) {
                                try {
                                    zipOutputStream.close();
                                } catch (IOException e2222) {
                                    e2222.printStackTrace();
                                }
                            }
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e22222) {
                                    e22222.printStackTrace();
                                }
                            }
                            if (bos != null) {
                                try {
                                    bos.close();
                                } catch (IOException e222222) {
                                    e222222.printStackTrace();
                                }
                            }
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            if (zipOutputStream != null) {
                                try {
                                    zipOutputStream.close();
                                } catch (IOException e2222222) {
                                    e2222222.printStackTrace();
                                }
                            }
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e22222222) {
                                    e22222222.printStackTrace();
                                }
                            }
                            if (bos != null) {
                                try {
                                    bos.close();
                                } catch (IOException e222222222) {
                                    e222222222.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        bos = bos2;
                        fileOutputStream = fos;
                        zipOutputStream = zipout;
                        if (zipOutputStream != null) {
                            zipOutputStream.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        if (bos != null) {
                            bos.close();
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    bos = bos2;
                    fileOutputStream = fos;
                    e.printStackTrace();
                    if (zipOutputStream != null) {
                        zipOutputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (bos != null) {
                        bos.close();
                    }
                    return false;
                } catch (Throwable th4) {
                    th = th4;
                    bos = bos2;
                    fileOutputStream = fos;
                    if (zipOutputStream != null) {
                        zipOutputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (bos != null) {
                        bos.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e5) {
                e = e5;
                fileOutputStream = fos;
                e.printStackTrace();
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (bos != null) {
                    bos.close();
                }
                return false;
            } catch (Throwable th5) {
                th = th5;
                fileOutputStream = fos;
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (bos != null) {
                    bos.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e = e6;
            e.printStackTrace();
            if (zipOutputStream != null) {
                zipOutputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (bos != null) {
                bos.close();
            }
            return false;
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void unZipFile(File zipFile, String folderPath) {
        Throwable th;
        Exception e;
        File desDir = new File(folderPath);
        if (desDir.exists() || desDir.mkdirs()) {
            int i = 0;
            ZipFile zipFile2 = null;
            try {
                ZipFile zipFile3 = new ZipFile(zipFile);
                try {
                    Enumeration<?> entries = zipFile3.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) entries.nextElement();
                        if (entry.getName() == null || !entry.getName().contains(EXCLUDE_ENTRY)) {
                            String str = new String((folderPath + File.separator + entry.getName()).getBytes("8859_1"), "UTF-8");
                            i++;
                            Log.i(TAG, i + "======file=====:" + str);
                            File desFile = new File(str);
                            if (entry.isDirectory()) {
                                continue;
                            } else {
                                boolean z = true;
                                if (!desFile.exists()) {
                                    File fileParentDir = desFile.getParentFile();
                                    if (!(fileParentDir == null || fileParentDir.exists())) {
                                        z = fileParentDir.mkdirs();
                                    }
                                    if (z) {
                                        z = desFile.createNewFile();
                                    }
                                }
                                if (z || desFile.exists()) {
                                    OutputStream outputStream = null;
                                    InputStream inputStream = null;
                                    try {
                                        inputStream = zipFile3.getInputStream(entry);
                                        OutputStream out = new FileOutputStream(desFile);
                                        try {
                                            byte[] buffer = new byte[BUFF_SIZE];
                                            while (true) {
                                                int realLength = inputStream.read(buffer);
                                                if (realLength <= 0) {
                                                    break;
                                                }
                                                out.write(buffer, 0, realLength);
                                            }
                                            if (inputStream != null) {
                                                try {
                                                    inputStream.close();
                                                } catch (IOException e2) {
                                                    e2.printStackTrace();
                                                }
                                            }
                                            if (out != null) {
                                                try {
                                                    out.close();
                                                } catch (IOException e22) {
                                                    e22.printStackTrace();
                                                }
                                            } else {
                                                continue;
                                            }
                                        } catch (Throwable th2) {
                                            th = th2;
                                            outputStream = out;
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                    }
                                }
                            }
                        }
                    }
                    Log.i(TAG, "unzip end");
                    if (zipFile3 != null) {
                        try {
                            zipFile3.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    zipFile2 = zipFile3;
                } catch (Exception e3) {
                    e = e3;
                    zipFile2 = zipFile3;
                } catch (Throwable th4) {
                    th = th4;
                    zipFile2 = zipFile3;
                }
            } catch (Exception e4) {
                e = e4;
                try {
                    e.printStackTrace();
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (IOException e2222) {
                            e2222.printStackTrace();
                        }
                    }
                } catch (Throwable th5) {
                    th = th5;
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (IOException e22222) {
                            e22222.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isZipError(String zipFile) {
        Throwable th;
        if (zipFile == null) {
            return true;
        }
        ZipFile zipFile2 = null;
        InputStream in = null;
        int total = 0;
        try {
            ZipFile zf = new ZipFile(zipFile);
            try {
                Enumeration<?> entries = zf.entries();
                loop0:
                while (entries.hasMoreElements()) {
                    in = zf.getInputStream((ZipEntry) entries.nextElement());
                    int entrySzie = 0;
                    byte[] buffer = new byte[BUFFER_BYTE];
                    while (true) {
                        int bytesRead = in.read(buffer);
                        if (bytesRead < 0) {
                            break;
                        }
                        entrySzie += bytesRead;
                        if (entrySzie >= MAX_ENTRY_THEME_SIZE) {
                            break loop0;
                        }
                    }
                    total += entrySzie;
                    if (total >= MAX_ENTRY_THEME_SIZE) {
                        Log.e(TAG, "isZipError total checkZipIsSize true " + zipFile);
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (zf != null) {
                            try {
                                zf.close();
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                        return true;
                    }
                    in.close();
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                if (zf != null) {
                    try {
                        zf.close();
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                }
                return false;
            } catch (IOException e4) {
                zipFile2 = zf;
            } catch (Exception e5) {
                zipFile2 = zf;
            } catch (Throwable th2) {
                th = th2;
                zipFile2 = zf;
            }
        } catch (IOException e6) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
            if (zipFile2 != null) {
                try {
                    zipFile2.close();
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }
            return true;
        } catch (Exception e7) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
            if (zipFile2 != null) {
                try {
                    zipFile2.close();
                } catch (Exception e2222) {
                    e2222.printStackTrace();
                }
            }
            return true;
        } catch (Throwable th3) {
            th = th3;
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            }
            if (zipFile2 != null) {
                try {
                    zipFile2.close();
                } catch (Exception e22222) {
                    e22222.printStackTrace();
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void unZipDirectory(String zipFile, String entryName, String desPath) {
        Throwable th;
        Exception e;
        File desDir = new File(desPath);
        if (desDir.exists() || desDir.mkdirs()) {
            ZipFile zipFile2 = null;
            try {
                ZipFile zipFile3 = new ZipFile(zipFile);
                try {
                    Enumeration<?> entries = zipFile3.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) entries.nextElement();
                        if (!(entry.getName() == null || entry.getName().contains(EXCLUDE_ENTRY))) {
                            if (entry.getName().contains(entryName)) {
                                File desFile = new File(new String((desPath + File.separator + entry.getName()).getBytes("8859_1"), "UTF-8"));
                                if (entry.isDirectory()) {
                                    continue;
                                } else {
                                    boolean z = true;
                                    if (!desFile.exists()) {
                                        File fileParentDir = desFile.getParentFile();
                                        if (!(fileParentDir == null || fileParentDir.exists())) {
                                            z = fileParentDir.mkdirs();
                                        }
                                        if (z) {
                                            z = desFile.createNewFile();
                                        }
                                    }
                                    if (z || desFile.exists()) {
                                        OutputStream outputStream = null;
                                        InputStream inputStream = null;
                                        try {
                                            inputStream = zipFile3.getInputStream(entry);
                                            OutputStream out = new FileOutputStream(desFile);
                                            try {
                                                byte[] buffer = new byte[BUFF_SIZE];
                                                while (true) {
                                                    int realLength = inputStream.read(buffer);
                                                    if (realLength <= 0) {
                                                        break;
                                                    }
                                                    out.write(buffer, 0, realLength);
                                                }
                                                if (inputStream != null) {
                                                    try {
                                                        inputStream.close();
                                                    } catch (IOException e2) {
                                                        e2.printStackTrace();
                                                    }
                                                }
                                                if (out != null) {
                                                    try {
                                                        out.close();
                                                    } catch (IOException e22) {
                                                        e22.printStackTrace();
                                                    }
                                                } else {
                                                    continue;
                                                }
                                            } catch (Throwable th2) {
                                                th = th2;
                                                outputStream = out;
                                            }
                                        } catch (Throwable th3) {
                                            th = th3;
                                        }
                                    }
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                    if (zipFile3 != null) {
                        try {
                            zipFile3.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    zipFile2 = zipFile3;
                } catch (Exception e3) {
                    e = e3;
                    zipFile2 = zipFile3;
                } catch (Throwable th4) {
                    th = th4;
                    zipFile2 = zipFile3;
                }
            } catch (Exception e4) {
                e = e4;
                try {
                    e.printStackTrace();
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (IOException e2222) {
                            e2222.printStackTrace();
                        }
                    }
                } catch (Throwable th5) {
                    th = th5;
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (IOException e22222) {
                            e22222.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean upZipSelectedFile(File form, String to, String obj) throws ZipException, IOException {
        IOException e1;
        Exception e;
        Throwable th;
        if (form == null) {
            return false;
        }
        File file = new File(to);
        if (!file.exists()) {
            Log.d("ive", "add audio dir");
            if (!file.mkdir()) {
                return false;
            }
        }
        ZipFile zipFile = new ZipFile(form);
        if (zipFile == null) {
            return false;
        }
        Enumeration<?> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.getName().contains(obj)) {
                String fileName = to + entry.getName();
                File desFile = new File(fileName);
                if (!(fileName.contains(EXCLUDE_ENTRY) || entry.isDirectory())) {
                    boolean newOk = true;
                    if (!(desFile == null || desFile.exists())) {
                        File fileParentDir = desFile.getParentFile();
                        if (!(fileParentDir == null || fileParentDir.exists())) {
                            newOk = fileParentDir.mkdirs();
                        }
                        if (newOk) {
                            try {
                                newOk = desFile.createNewFile();
                            } catch (IOException e2) {
                                Log.e(TAG, " create new file failed");
                            }
                        }
                        if (newOk) {
                            InputStream inputStream = null;
                            OutputStream outputStream = null;
                            try {
                                byte[] buffer = new byte[BUFFER_BYTE];
                                inputStream = zipFile.getInputStream(entry);
                                OutputStream fileOutputStream = new FileOutputStream(desFile);
                                while (true) {
                                    try {
                                        int realLength = inputStream.read(buffer);
                                        if (realLength <= 0) {
                                            break;
                                        }
                                        fileOutputStream.write(buffer, 0, realLength);
                                    } catch (IOException e3) {
                                        e1 = e3;
                                        outputStream = fileOutputStream;
                                    } catch (Exception e4) {
                                        e = e4;
                                        outputStream = fileOutputStream;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        outputStream = fileOutputStream;
                                    }
                                }
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException ioe) {
                                        Log.e(TAG, "ioe is " + ioe);
                                    } catch (Throwable th3) {
                                    }
                                }
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.close();
                                    } catch (IOException ioexception) {
                                        Log.e(TAG, "ioexception is " + ioexception);
                                    } catch (Throwable th4) {
                                    }
                                }
                            } catch (IOException e5) {
                                e1 = e5;
                                Log.e(TAG, "IOException is " + e1);
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException ioe2) {
                                        Log.e(TAG, "ioe is " + ioe2);
                                    } catch (Throwable th5) {
                                    }
                                }
                                if (outputStream != null) {
                                    try {
                                        outputStream.close();
                                    } catch (IOException ioexception2) {
                                        Log.e(TAG, "ioexception is " + ioexception2);
                                    } catch (Throwable th6) {
                                    }
                                }
                            } catch (Exception e6) {
                                e = e6;
                                try {
                                    Log.e(TAG, "Exception is " + e);
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (IOException ioe22) {
                                            Log.e(TAG, "ioe is " + ioe22);
                                        } catch (Throwable th7) {
                                        }
                                    }
                                    if (outputStream != null) {
                                        try {
                                            outputStream.close();
                                        } catch (IOException ioexception22) {
                                            Log.e(TAG, "ioexception is " + ioexception22);
                                        } catch (Throwable th8) {
                                        }
                                    }
                                } catch (Throwable th9) {
                                    th = th9;
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
        try {
            zipFile.close();
        } catch (IOException e7) {
            e7.printStackTrace();
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ArrayList<String> getEntriesNames(File zipFile) throws ZipException, IOException {
        ArrayList<String> entryNames = new ArrayList();
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
            } catch (Exception e1) {
                Log.e(TAG, "Exception e1: " + e1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception e: " + e);
        } catch (Throwable th) {
            try {
                zf.close();
            } catch (Exception e12) {
                Log.e(TAG, "Exception e1: " + e12);
            }
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
        Exception e;
        Throwable th;
        BufferedInputStream bis = null;
        FileInputStream fis = null;
        try {
            String rootpath2 = new String((rootpath + (rootpath.trim().length() == 0 ? "" : File.separator) + resFile.getName()).getBytes("8859_1"), "GB2312");
            try {
                if (resFile.isDirectory()) {
                    File[] fileList = resFile.listFiles();
                    if (fileList != null) {
                        for (File file : fileList) {
                            zipFile(file, zipout, rootpath2);
                        }
                    }
                    zipout.putNextEntry(new ZipEntry(rootpath2));
                } else {
                    BufferedInputStream bis2;
                    byte[] buffer = new byte[BUFF_SIZE];
                    FileInputStream fis2 = new FileInputStream(resFile);
                    try {
                        bis2 = new BufferedInputStream(fis2, BUFF_SIZE);
                    } catch (Exception e2) {
                        e = e2;
                        fis = fis2;
                        rootpath = rootpath2;
                        try {
                            e.printStackTrace();
                            if (bis != null) {
                                try {
                                    bis.close();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                }
                            }
                            if (fis == null) {
                                try {
                                    fis.close();
                                } catch (IOException e32) {
                                    e32.printStackTrace();
                                    return;
                                }
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (bis != null) {
                                try {
                                    bis.close();
                                } catch (IOException e322) {
                                    e322.printStackTrace();
                                }
                            }
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (IOException e3222) {
                                    e3222.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fis = fis2;
                        rootpath = rootpath2;
                        if (bis != null) {
                            bis.close();
                        }
                        if (fis != null) {
                            fis.close();
                        }
                        throw th;
                    }
                    try {
                        zipout.putNextEntry(new ZipEntry(rootpath2));
                        while (true) {
                            int realLength = bis2.read(buffer);
                            if (realLength == -1) {
                                break;
                            }
                            zipout.write(buffer, 0, realLength);
                        }
                        bis2.close();
                        zipout.flush();
                        zipout.closeEntry();
                        fis = fis2;
                        bis = bis2;
                    } catch (Exception e4) {
                        e = e4;
                        fis = fis2;
                        bis = bis2;
                        rootpath = rootpath2;
                        e.printStackTrace();
                        if (bis != null) {
                            bis.close();
                        }
                        if (fis == null) {
                            fis.close();
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        fis = fis2;
                        bis = bis2;
                        rootpath = rootpath2;
                        if (bis != null) {
                            bis.close();
                        }
                        if (fis != null) {
                            fis.close();
                        }
                        throw th;
                    }
                }
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e32222) {
                        e32222.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e322222) {
                        e322222.printStackTrace();
                    }
                }
                rootpath = rootpath2;
            } catch (Exception e5) {
                e = e5;
                rootpath = rootpath2;
                e.printStackTrace();
                if (bis != null) {
                    bis.close();
                }
                if (fis == null) {
                    fis.close();
                }
            } catch (Throwable th5) {
                th = th5;
                rootpath = rootpath2;
                if (bis != null) {
                    bis.close();
                }
                if (fis != null) {
                    fis.close();
                }
                throw th;
            }
        } catch (Exception e6) {
            e = e6;
            e.printStackTrace();
            if (bis != null) {
                bis.close();
            }
            if (fis == null) {
                fis.close();
            }
        }
    }

    public static void unZipFile(String zipFile, String entryName, String desPath) {
        Exception e;
        Throwable th;
        ZipFile zipFile2 = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            ZipFile zf = new ZipFile(zipFile);
            try {
                ZipEntry zEntry = zf.getEntry(entryName);
                if (!(zEntry == null || entryName == null)) {
                    if (!entryName.contains(EXCLUDE_ENTRY)) {
                        in = zf.getInputStream(zEntry);
                        OutputStream out2 = new FileOutputStream(new String((desPath + File.separator + entryName).getBytes("8859_1"), "GB2312"));
                        try {
                            byte[] buffer = new byte[BUFFER_BYTE];
                            while (true) {
                                int realLength = in.read(buffer);
                                if (realLength <= 0) {
                                    break;
                                }
                                out2.write(buffer, 0, realLength);
                            }
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                }
                            }
                            if (out2 != null) {
                                try {
                                    out2.close();
                                } catch (IOException e22) {
                                    e22.printStackTrace();
                                }
                            }
                            if (zf != null) {
                                try {
                                    zf.close();
                                } catch (IOException e222) {
                                    e222.printStackTrace();
                                }
                            }
                        } catch (Exception e3) {
                            e = e3;
                            out = out2;
                            zipFile2 = zf;
                            try {
                                e.printStackTrace();
                                if (in != null) {
                                    try {
                                        in.close();
                                    } catch (IOException e2222) {
                                        e2222.printStackTrace();
                                    }
                                }
                                if (out != null) {
                                    try {
                                        out.close();
                                    } catch (IOException e22222) {
                                        e22222.printStackTrace();
                                    }
                                }
                                if (zipFile2 != null) {
                                    try {
                                        zipFile2.close();
                                    } catch (IOException e222222) {
                                        e222222.printStackTrace();
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (in != null) {
                                    try {
                                        in.close();
                                    } catch (IOException e2222222) {
                                        e2222222.printStackTrace();
                                    }
                                }
                                if (out != null) {
                                    try {
                                        out.close();
                                    } catch (IOException e22222222) {
                                        e22222222.printStackTrace();
                                    }
                                }
                                if (zipFile2 != null) {
                                    try {
                                        zipFile2.close();
                                    } catch (IOException e222222222) {
                                        e222222222.printStackTrace();
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            out = out2;
                            zipFile2 = zf;
                            if (in != null) {
                                in.close();
                            }
                            if (out != null) {
                                out.close();
                            }
                            if (zipFile2 != null) {
                                zipFile2.close();
                            }
                            throw th;
                        }
                    }
                }
                if (zf != null) {
                    try {
                        zf.close();
                    } catch (IOException e2222222222) {
                        e2222222222.printStackTrace();
                    }
                }
            } catch (Exception e4) {
                e = e4;
                zipFile2 = zf;
                e.printStackTrace();
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (zipFile2 != null) {
                    zipFile2.close();
                }
            } catch (Throwable th4) {
                th = th4;
                zipFile2 = zf;
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (zipFile2 != null) {
                    zipFile2.close();
                }
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            e.printStackTrace();
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (zipFile2 != null) {
                zipFile2.close();
            }
        }
    }

    public static void unZipFile(File zipFile, String desPath, String entryName) {
        Exception e;
        Throwable th;
        if (!(zipFile == null || entryName == null)) {
            if (!entryName.contains(EXCLUDE_ENTRY)) {
                InputStream inStream = null;
                OutputStream outputStream = null;
                ZipFile zipFile2 = null;
                try {
                    ZipFile zf = new ZipFile(zipFile);
                    try {
                        ZipEntry zEntry = zf.getEntry(entryName);
                        if (zEntry == null) {
                            if (zf != null) {
                                try {
                                    zf.close();
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                }
                            }
                            return;
                        }
                        inStream = zf.getInputStream(zEntry);
                        OutputStream outStream = new FileOutputStream(new String((desPath + File.separator + entryName).getBytes("8859_1"), "GB2312"));
                        try {
                            byte[] buffer = new byte[BUFFER_BYTE];
                            while (true) {
                                int realLength = inStream.read(buffer);
                                if (realLength <= 0) {
                                    break;
                                }
                                outStream.write(buffer, 0, realLength);
                            }
                            if (zf != null) {
                                try {
                                    zf.close();
                                } catch (IOException e22) {
                                    e22.printStackTrace();
                                }
                            }
                            if (inStream != null) {
                                try {
                                    inStream.close();
                                } catch (IOException e222) {
                                    e222.printStackTrace();
                                }
                            }
                            if (outStream != null) {
                                try {
                                    outStream.close();
                                } catch (IOException e2222) {
                                    e2222.printStackTrace();
                                }
                            }
                        } catch (Exception e3) {
                            e = e3;
                            zipFile2 = zf;
                            outputStream = outStream;
                            try {
                                e.printStackTrace();
                                if (zipFile2 != null) {
                                    try {
                                        zipFile2.close();
                                    } catch (IOException e22222) {
                                        e22222.printStackTrace();
                                    }
                                }
                                if (inStream != null) {
                                    try {
                                        inStream.close();
                                    } catch (IOException e222222) {
                                        e222222.printStackTrace();
                                    }
                                }
                                if (outputStream != null) {
                                    try {
                                        outputStream.close();
                                    } catch (IOException e2222222) {
                                        e2222222.printStackTrace();
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (zipFile2 != null) {
                                    try {
                                        zipFile2.close();
                                    } catch (IOException e22222222) {
                                        e22222222.printStackTrace();
                                    }
                                }
                                if (inStream != null) {
                                    try {
                                        inStream.close();
                                    } catch (IOException e222222222) {
                                        e222222222.printStackTrace();
                                    }
                                }
                                if (outputStream != null) {
                                    try {
                                        outputStream.close();
                                    } catch (IOException e2222222222) {
                                        e2222222222.printStackTrace();
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            zipFile2 = zf;
                            outputStream = outStream;
                            if (zipFile2 != null) {
                                zipFile2.close();
                            }
                            if (inStream != null) {
                                inStream.close();
                            }
                            if (outputStream != null) {
                                outputStream.close();
                            }
                            throw th;
                        }
                    } catch (Exception e4) {
                        e = e4;
                        zipFile2 = zf;
                        e.printStackTrace();
                        if (zipFile2 != null) {
                            zipFile2.close();
                        }
                        if (inStream != null) {
                            inStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        zipFile2 = zf;
                        if (zipFile2 != null) {
                            zipFile2.close();
                        }
                        if (inStream != null) {
                            inStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        throw th;
                    }
                } catch (Exception e5) {
                    e = e5;
                    e.printStackTrace();
                    if (zipFile2 != null) {
                        zipFile2.close();
                    }
                    if (inStream != null) {
                        inStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            }
        }
    }
}

package com.android.timezone.distro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public final class FileUtils {
    private FileUtils() {
    }

    public static File createSubFile(File parentDir, String name) throws IOException {
        File subFile = new File(parentDir, name).getCanonicalFile();
        if (subFile.getPath().startsWith(parentDir.getCanonicalPath())) {
            return subFile;
        }
        throw new IOException(name + " must exist beneath " + parentDir + ". Canonicalized subpath: " + subFile);
    }

    public static void ensureDirectoriesExist(File dir, boolean makeWorldReadable) throws IOException {
        LinkedList<File> dirs = new LinkedList<>();
        File currentDir = dir;
        do {
            dirs.addFirst(currentDir);
            currentDir = currentDir.getParentFile();
        } while (currentDir != null);
        Iterator<File> it = dirs.iterator();
        while (it.hasNext()) {
            File dirToCheck = it.next();
            if (!dirToCheck.exists()) {
                if (!dirToCheck.mkdir()) {
                    throw new IOException("Unable to create directory: " + dir);
                } else if (makeWorldReadable) {
                    makeDirectoryWorldAccessible(dirToCheck);
                }
            } else if (!dirToCheck.isDirectory()) {
                throw new IOException(dirToCheck + " exists but is not a directory");
            }
        }
    }

    public static void makeDirectoryWorldAccessible(File directory) throws IOException {
        if (directory.isDirectory()) {
            makeWorldReadable(directory);
            if (!directory.setExecutable(true, false)) {
                throw new IOException("Unable to make " + directory + " world-executable");
            }
            return;
        }
        throw new IOException(directory + " must be a directory");
    }

    public static void makeWorldReadable(File file) throws IOException {
        if (!file.setReadable(true, false)) {
            throw new IOException("Unable to make " + file + " world-readable");
        }
    }

    public static void rename(File from, File to) throws IOException {
        ensureFileDoesNotExist(to);
        if (!from.renameTo(to)) {
            throw new IOException("Unable to rename " + from + " to " + to);
        }
    }

    public static void ensureFileDoesNotExist(File file) throws IOException {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            doDelete(file);
            return;
        }
        throw new IOException(file + " is not a file");
    }

    public static void doDelete(File file) throws IOException {
        if (!file.delete()) {
            throw new IOException("Unable to delete: " + file);
        }
    }

    public static boolean isSymlink(File file) throws IOException {
        return !file.getCanonicalPath().equals(new File(file.getParentFile().getCanonicalFile(), file.getName()).getPath());
    }

    public static void deleteRecursive(File toDelete) throws IOException {
        if (toDelete.isDirectory()) {
            File[] listFiles = toDelete.listFiles();
            for (File file : listFiles) {
                if (!file.isDirectory() || isSymlink(file)) {
                    doDelete(file);
                } else {
                    deleteRecursive(file);
                }
            }
            String[] remainingFiles = toDelete.list();
            if (remainingFiles.length != 0) {
                throw new IOException("Unable to delete files: " + Arrays.toString(remainingFiles));
            }
        }
        doDelete(toDelete);
    }

    public static boolean filesExist(File rootDir, String... fileNames) {
        for (String fileName : fileNames) {
            if (!new File(rootDir, fileName).exists()) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001f, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0020, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0023, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001a, code lost:
        r2 = move-exception;
     */
    public static byte[] readBytes(File file, int maxBytes) throws IOException {
        if (maxBytes > 0) {
            FileInputStream in = new FileInputStream(file);
            byte[] max = new byte[maxBytes];
            int bytesRead = in.read(max, 0, maxBytes);
            byte[] toReturn = new byte[bytesRead];
            System.arraycopy(max, 0, toReturn, 0, bytesRead);
            in.close();
            return toReturn;
        }
        throw new IllegalArgumentException("maxBytes ==" + maxBytes);
    }

    public static void createEmptyFile(File file) throws IOException {
        new FileOutputStream(file, false).close();
    }
}

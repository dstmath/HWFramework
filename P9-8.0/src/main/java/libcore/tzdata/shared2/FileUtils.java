package libcore.tzdata.shared2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
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
        LinkedList<File> dirs = new LinkedList();
        File currentDir = dir;
        do {
            dirs.addFirst(currentDir);
            currentDir = currentDir.getParentFile();
        } while (currentDir != null);
        for (File dirToCheck : dirs) {
            if (dirToCheck.exists()) {
                if (!dirToCheck.isDirectory()) {
                    throw new IOException(dirToCheck + " exists but is not a directory");
                }
            } else if (!dirToCheck.mkdir()) {
                throw new IOException("Unable to create directory: " + dir);
            } else if (makeWorldReadable) {
                makeDirectoryWorldAccessible(dirToCheck);
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
        return file.getCanonicalPath().equals(new File(file.getParentFile().getCanonicalFile(), file.getName()).getPath()) ^ 1;
    }

    public static void deleteRecursive(File toDelete) throws IOException {
        if (toDelete.isDirectory()) {
            for (File file : toDelete.listFiles()) {
                if (!file.isDirectory() || (isSymlink(file) ^ 1) == 0) {
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

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0044 A:{SYNTHETIC, Splitter: B:22:0x0044} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0055  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0049  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] readBytes(File file, int maxBytes) throws IOException {
        Throwable th;
        Throwable th2 = null;
        if (maxBytes <= 0) {
            throw new IllegalArgumentException("maxBytes ==" + maxBytes);
        }
        FileInputStream in = null;
        try {
            FileInputStream in2 = new FileInputStream(file);
            try {
                byte[] max = new byte[maxBytes];
                int bytesRead = in2.read(max, 0, maxBytes);
                byte[] toReturn = new byte[bytesRead];
                System.arraycopy(max, 0, toReturn, 0, bytesRead);
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 == null) {
                    return toReturn;
                }
                throw th2;
            } catch (Throwable th4) {
                th = th4;
                in = in2;
                if (in != null) {
                    try {
                        in.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    throw th2;
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (in != null) {
            }
            if (th2 == null) {
            }
        }
    }

    public static void createEmptyFile(File file) throws IOException {
        new FileOutputStream(file, false).close();
    }
}

package ohos.data.distributed.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

public class DistFile extends File {
    private static final int BUFFER_LENGTH = 8192;
    private static final long serialVersionUID = -249758070224197024L;
    private final String path;

    public DistFile(String str) {
        super(str);
        this.path = str;
    }

    public boolean isDistFile() {
        String str = this.path;
        return str != null && str.startsWith(DistFileSystem.ROOT_DIST_PATH);
    }

    public String getDevice() {
        return isDistFile() ? DistFileSystem.getXattr(this.path, "location") : "";
    }

    public String getGroup() {
        return isDistFile() ? DistFileSystem.getXattr(this.path, "group") : "";
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0047, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0048, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004b, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x004e, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x004f, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0052, code lost:
        throw r5;
     */
    public boolean copyTo(DistFile distFile) throws IOException {
        if (distFile == null) {
            throw new IOException("Invalid argument");
        } else if (isDirectory() || distFile.isDirectory()) {
            throw new IOException("Doesn't support to copy directory");
        } else {
            int i = 0;
            if (!exists()) {
                return false;
            }
            if (!distFile.exists() && !distFile.createNewFile()) {
                return false;
            }
            byte[] bArr = new byte[8192];
            FileInputStream fileInputStream = new FileInputStream(this);
            FileOutputStream fileOutputStream = new FileOutputStream(distFile);
            while (true) {
                int read = fileInputStream.read(bArr, i, 8192);
                if (read > 0) {
                    fileOutputStream.write(bArr, i, read);
                    i += read;
                } else {
                    $closeResource(null, fileOutputStream);
                    $closeResource(null, fileInputStream);
                    return true;
                }
            }
        }
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th != null) {
            try {
                autoCloseable.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        } else {
            autoCloseable.close();
        }
    }

    @Override // java.io.File
    public DistFile getParentFile() {
        return fileToDistFile(super.getParentFile());
    }

    @Override // java.io.File
    public DistFile[] listFiles() {
        return filesToDistFiles(super.listFiles());
    }

    @Override // java.io.File
    public DistFile[] listFiles(FileFilter fileFilter) {
        return filesToDistFiles(super.listFiles(fileFilter));
    }

    @Override // java.io.File
    public DistFile[] listFiles(FilenameFilter filenameFilter) {
        return filesToDistFiles(super.listFiles(filenameFilter));
    }

    private DistFile fileToDistFile(File file) {
        if (file == null) {
            return null;
        }
        return new DistFile(file.getPath());
    }

    private DistFile[] filesToDistFiles(File[] fileArr) {
        if (fileArr == null) {
            return new DistFile[0];
        }
        int length = fileArr.length;
        DistFile[] distFileArr = new DistFile[length];
        for (int i = 0; i < length; i++) {
            distFileArr[i] = fileToDistFile(fileArr[i]);
        }
        return distFileArr;
    }
}

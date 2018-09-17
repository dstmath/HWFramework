package libcore.tzdata.update;

import android.util.Slog;
import java.io.File;
import java.io.IOException;

public final class TzDataBundleInstaller {
    static final String CURRENT_TZ_DATA_DIR_NAME = "current";
    static final String OLD_TZ_DATA_DIR_NAME = "old";
    static final String WORKING_DIR_NAME = "working";
    private final File installDir;
    private final String logTag;

    public TzDataBundleInstaller(String logTag, File installDir) {
        this.logTag = logTag;
        this.installDir = installDir;
    }

    public boolean install(byte[] content) throws IOException {
        File oldTzDataDir = new File(this.installDir, OLD_TZ_DATA_DIR_NAME);
        if (oldTzDataDir.exists()) {
            FileUtils.deleteRecursive(oldTzDataDir);
        }
        File currentTzDataDir = new File(this.installDir, CURRENT_TZ_DATA_DIR_NAME);
        File workingDir = new File(this.installDir, WORKING_DIR_NAME);
        Slog.i(this.logTag, "Applying time zone update");
        File unpackedContentDir = unpackBundle(content, workingDir);
        try {
            if (!checkBundleFilesExist(unpackedContentDir)) {
                Slog.i(this.logTag, "Update not applied: Bundle is missing files");
                return false;
            } else if (verifySystemChecksums(unpackedContentDir)) {
                FileUtils.makeDirectoryWorldAccessible(unpackedContentDir);
                if (currentTzDataDir.exists()) {
                    Slog.i(this.logTag, "Moving " + currentTzDataDir + " to " + oldTzDataDir);
                    FileUtils.rename(currentTzDataDir, oldTzDataDir);
                }
                Slog.i(this.logTag, "Moving " + unpackedContentDir + " to " + currentTzDataDir);
                FileUtils.rename(unpackedContentDir, currentTzDataDir);
                Slog.i(this.logTag, "Update applied: " + currentTzDataDir + " successfully created");
                deleteBestEffort(oldTzDataDir);
                deleteBestEffort(unpackedContentDir);
                return true;
            } else {
                Slog.i(this.logTag, "Update not applied: System checksum did not match");
                deleteBestEffort(oldTzDataDir);
                deleteBestEffort(unpackedContentDir);
                return false;
            }
        } finally {
            deleteBestEffort(oldTzDataDir);
            deleteBestEffort(unpackedContentDir);
        }
    }

    private void deleteBestEffort(File dir) {
        if (dir.exists()) {
            try {
                FileUtils.deleteRecursive(dir);
            } catch (IOException e) {
                Slog.w(this.logTag, "Unable to delete " + dir, e);
            }
        }
    }

    private File unpackBundle(byte[] content, File targetDir) throws IOException {
        Slog.i(this.logTag, "Unpacking update content to: " + targetDir);
        new ConfigBundle(content).extractTo(targetDir);
        return targetDir;
    }

    private boolean checkBundleFilesExist(File unpackedContentDir) throws IOException {
        Slog.i(this.logTag, "Verifying bundle contents");
        return FileUtils.filesExist(unpackedContentDir, ConfigBundle.TZ_DATA_VERSION_FILE_NAME, ConfigBundle.CHECKSUMS_FILE_NAME, ConfigBundle.ZONEINFO_FILE_NAME, ConfigBundle.ICU_DATA_FILE_NAME);
    }

    private boolean verifySystemChecksums(File unpackedContentDir) throws IOException {
        Slog.i(this.logTag, "Verifying system file checksums");
        for (String line : FileUtils.readLines(new File(unpackedContentDir, ConfigBundle.CHECKSUMS_FILE_NAME))) {
            int delimiterPos = line.indexOf(44);
            if (delimiterPos <= 0 || delimiterPos == line.length() - 1) {
                throw new IOException("Bad checksum entry: " + line);
            }
            try {
                long expectedChecksum = Long.parseLong(line.substring(0, delimiterPos));
                File file = new File(line.substring(delimiterPos + 1));
                if (file.exists()) {
                    long actualChecksum = FileUtils.calculateChecksum(file);
                    if (actualChecksum != expectedChecksum) {
                        Slog.i(this.logTag, "Failed checksum test for file: " + file + ": required=" + expectedChecksum + ", actual=" + actualChecksum);
                        return false;
                    }
                }
                Slog.i(this.logTag, "Failed checksum test for file: " + file + ": file not found");
                return false;
            } catch (NumberFormatException e) {
                throw new IOException("Invalid checksum value: " + line);
            }
        }
        return true;
    }
}

package libcore.tzdata.update2;

import android.util.Slog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import libcore.tzdata.shared2.DistroException;
import libcore.tzdata.shared2.DistroVersion;
import libcore.tzdata.shared2.FileUtils;
import libcore.tzdata.shared2.StagedDistroOperation;
import libcore.tzdata.shared2.TimeZoneDistro;
import libcore.util.TimeZoneFinder;
import libcore.util.ZoneInfoDB.TzData;

public class TimeZoneDistroInstaller {
    private static final String CURRENT_TZ_DATA_DIR_NAME = "current";
    public static final int INSTALL_FAIL_BAD_DISTRO_FORMAT_VERSION = 2;
    public static final int INSTALL_FAIL_BAD_DISTRO_STRUCTURE = 1;
    public static final int INSTALL_FAIL_RULES_TOO_OLD = 3;
    public static final int INSTALL_FAIL_VALIDATION_ERROR = 4;
    public static final int INSTALL_SUCCESS = 0;
    private static final String OLD_TZ_DATA_DIR_NAME = "old";
    private static final String STAGED_TZ_DATA_DIR_NAME = "staged";
    public static final String UNINSTALL_TOMBSTONE_FILE_NAME = "STAGED_UNINSTALL_TOMBSTONE";
    private static final String WORKING_DIR_NAME = "working";
    private final File currentTzDataDir;
    private final String logTag;
    private final File oldStagedDataDir;
    private final File stagedTzDataDir;
    private final File systemTzDataFile;
    private final File workingDir;

    public TimeZoneDistroInstaller(String logTag, File systemTzDataFile, File installDir) {
        this.logTag = logTag;
        this.systemTzDataFile = systemTzDataFile;
        this.oldStagedDataDir = new File(installDir, OLD_TZ_DATA_DIR_NAME);
        this.stagedTzDataDir = new File(installDir, STAGED_TZ_DATA_DIR_NAME);
        this.currentTzDataDir = new File(installDir, CURRENT_TZ_DATA_DIR_NAME);
        this.workingDir = new File(installDir, WORKING_DIR_NAME);
    }

    File getOldStagedDataDir() {
        return this.oldStagedDataDir;
    }

    File getStagedTzDataDir() {
        return this.stagedTzDataDir;
    }

    File getCurrentTzDataDir() {
        return this.currentTzDataDir;
    }

    File getWorkingDir() {
        return this.workingDir;
    }

    public boolean install(byte[] content) throws IOException {
        if (stageInstallWithErrorCode(content) == 0) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:60:0x0184, code:
            deleteBestEffort(r12.oldStagedDataDir);
            deleteBestEffort(r12.workingDir);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int stageInstallWithErrorCode(byte[] content) throws IOException {
        if (this.oldStagedDataDir.exists()) {
            FileUtils.deleteRecursive(this.oldStagedDataDir);
        }
        if (this.workingDir.exists()) {
            FileUtils.deleteRecursive(this.workingDir);
        }
        Slog.i(this.logTag, "Unpacking / verifying time zone update");
        try {
            unpackDistro(content, this.workingDir);
            DistroVersion distroVersion = readDistroVersion(this.workingDir);
            if (distroVersion == null) {
                Slog.i(this.logTag, "Update not applied: Distro version could not be loaded");
                return 1;
            } else if (!DistroVersion.isCompatibleWithThisDevice(distroVersion)) {
                Slog.i(this.logTag, "Update not applied: Distro format version check failed: " + distroVersion);
                deleteBestEffort(this.oldStagedDataDir);
                deleteBestEffort(this.workingDir);
                return 2;
            } else if (!checkDistroDataFilesExist(this.workingDir)) {
                Slog.i(this.logTag, "Update not applied: Distro is missing required data file(s)");
                deleteBestEffort(this.oldStagedDataDir);
                deleteBestEffort(this.workingDir);
                return 1;
            } else if (checkDistroRulesNewerThanSystem(this.systemTzDataFile, distroVersion)) {
                File zoneInfoFile = new File(this.workingDir, TimeZoneDistro.TZDATA_FILE_NAME);
                TzData tzData = TzData.loadTzData(zoneInfoFile.getPath());
                if (tzData == null) {
                    Slog.i(this.logTag, "Update not applied: " + zoneInfoFile + " could not be loaded");
                    deleteBestEffort(this.oldStagedDataDir);
                    deleteBestEffort(this.workingDir);
                    return 4;
                }
                try {
                    tzData.validate();
                    File tzLookupFile = new File(this.workingDir, TimeZoneDistro.TZLOOKUP_FILE_NAME);
                    if (tzLookupFile.exists()) {
                        try {
                            TimeZoneFinder.createInstance(tzLookupFile.getPath()).validate();
                            Slog.i(this.logTag, "Applying time zone update");
                            FileUtils.makeDirectoryWorldAccessible(this.workingDir);
                            if (this.stagedTzDataDir.exists()) {
                                Slog.i(this.logTag, "Moving " + this.stagedTzDataDir + " to " + this.oldStagedDataDir);
                                FileUtils.rename(this.stagedTzDataDir, this.oldStagedDataDir);
                            } else {
                                Slog.i(this.logTag, "Nothing to unstage at " + this.stagedTzDataDir);
                            }
                            Slog.i(this.logTag, "Moving " + this.workingDir + " to " + this.stagedTzDataDir);
                            FileUtils.rename(this.workingDir, this.stagedTzDataDir);
                            Slog.i(this.logTag, "Install staged: " + this.stagedTzDataDir + " successfully created");
                            deleteBestEffort(this.oldStagedDataDir);
                            deleteBestEffort(this.workingDir);
                            return 0;
                        } catch (IOException e) {
                            Slog.i(this.logTag, "Update not applied: " + tzLookupFile + " failed validation", e);
                            deleteBestEffort(this.oldStagedDataDir);
                            deleteBestEffort(this.workingDir);
                            return 4;
                        }
                    }
                    Slog.i(this.logTag, "Update not applied: " + tzLookupFile + " does not exist");
                    deleteBestEffort(this.oldStagedDataDir);
                    deleteBestEffort(this.workingDir);
                    return 1;
                } catch (IOException e2) {
                    Slog.i(this.logTag, "Update not applied: " + zoneInfoFile + " failed validation", e2);
                    deleteBestEffort(this.oldStagedDataDir);
                    deleteBestEffort(this.workingDir);
                    return 4;
                } finally {
                    tzData.close();
                }
            } else {
                Slog.i(this.logTag, "Update not applied: Distro rules version check failed");
                deleteBestEffort(this.oldStagedDataDir);
                deleteBestEffort(this.workingDir);
                return 3;
            }
        } catch (DistroException e3) {
            Slog.i(this.logTag, "Invalid distro version: " + e3.getMessage());
            return 1;
        } finally {
        }
    }

    public boolean stageUninstall() throws IOException {
        Slog.i(this.logTag, "Uninstalling time zone update");
        if (this.oldStagedDataDir.exists()) {
            FileUtils.deleteRecursive(this.oldStagedDataDir);
        }
        if (this.workingDir.exists()) {
            FileUtils.deleteRecursive(this.workingDir);
        }
        try {
            if (this.stagedTzDataDir.exists()) {
                Slog.i(this.logTag, "Moving " + this.stagedTzDataDir + " to " + this.oldStagedDataDir);
                FileUtils.rename(this.stagedTzDataDir, this.oldStagedDataDir);
            } else {
                Slog.i(this.logTag, "Nothing to unstage at " + this.stagedTzDataDir);
            }
            if (this.currentTzDataDir.exists()) {
                FileUtils.ensureDirectoriesExist(this.workingDir, true);
                FileUtils.createEmptyFile(new File(this.workingDir, UNINSTALL_TOMBSTONE_FILE_NAME));
                Slog.i(this.logTag, "Moving " + this.workingDir + " to " + this.stagedTzDataDir);
                FileUtils.rename(this.workingDir, this.stagedTzDataDir);
                Slog.i(this.logTag, "Uninstall staged: " + this.stagedTzDataDir + " successfully created");
                deleteBestEffort(this.oldStagedDataDir);
                deleteBestEffort(this.workingDir);
                return true;
            }
            Slog.i(this.logTag, "Nothing to uninstall at " + this.currentTzDataDir);
            return false;
        } finally {
            deleteBestEffort(this.oldStagedDataDir);
            deleteBestEffort(this.workingDir);
        }
    }

    public DistroVersion getInstalledDistroVersion() throws DistroException, IOException {
        if (this.currentTzDataDir.exists()) {
            return readDistroVersion(this.currentTzDataDir);
        }
        return null;
    }

    public StagedDistroOperation getStagedDistroOperation() throws DistroException, IOException {
        if (!this.stagedTzDataDir.exists()) {
            return null;
        }
        if (new File(this.stagedTzDataDir, UNINSTALL_TOMBSTONE_FILE_NAME).exists()) {
            return StagedDistroOperation.uninstall();
        }
        return StagedDistroOperation.install(readDistroVersion(this.stagedTzDataDir));
    }

    public String getSystemRulesVersion() throws IOException {
        return readSystemRulesVersion(this.systemTzDataFile);
    }

    private void deleteBestEffort(File dir) {
        if (dir.exists()) {
            Slog.i(this.logTag, "Deleting " + dir);
            try {
                FileUtils.deleteRecursive(dir);
            } catch (IOException e) {
                Slog.w(this.logTag, "Unable to delete " + dir, e);
            }
        }
    }

    private void unpackDistro(byte[] content, File targetDir) throws IOException {
        Slog.i(this.logTag, "Unpacking update content to: " + targetDir);
        new TimeZoneDistro(content).extractTo(targetDir);
    }

    private boolean checkDistroDataFilesExist(File unpackedContentDir) throws IOException {
        Slog.i(this.logTag, "Verifying distro contents");
        return FileUtils.filesExist(unpackedContentDir, TimeZoneDistro.TZDATA_FILE_NAME, TimeZoneDistro.ICU_DATA_FILE_NAME);
    }

    private DistroVersion readDistroVersion(File distroDir) throws DistroException, IOException {
        Slog.i(this.logTag, "Reading distro format version");
        File distroVersionFile = new File(distroDir, TimeZoneDistro.DISTRO_VERSION_FILE_NAME);
        if (distroVersionFile.exists()) {
            return DistroVersion.fromBytes(FileUtils.readBytes(distroVersionFile, DistroVersion.DISTRO_VERSION_FILE_LENGTH));
        }
        throw new DistroException("No distro version file found: " + distroVersionFile);
    }

    private boolean checkDistroRulesNewerThanSystem(File systemTzDataFile, DistroVersion distroVersion) throws IOException {
        Slog.i(this.logTag, "Reading /system rules version");
        String systemRulesVersion = readSystemRulesVersion(systemTzDataFile);
        String distroRulesVersion = distroVersion.rulesVersion;
        boolean canApply = distroRulesVersion.compareTo(systemRulesVersion) >= 0;
        if (canApply) {
            Slog.i(this.logTag, "Passed rules version check: distroRulesVersion=" + distroRulesVersion + ", systemRulesVersion=" + systemRulesVersion);
        } else {
            Slog.i(this.logTag, "Failed rules version check: distroRulesVersion=" + distroRulesVersion + ", systemRulesVersion=" + systemRulesVersion);
        }
        return canApply;
    }

    private String readSystemRulesVersion(File systemTzDataFile) throws IOException {
        if (systemTzDataFile.exists()) {
            return TzData.getRulesVersion(systemTzDataFile);
        }
        Slog.i(this.logTag, "tzdata file cannot be found in /system");
        throw new FileNotFoundException("system tzdata does not exist: " + systemTzDataFile);
    }
}

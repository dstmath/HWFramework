package com.android.server.pm;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.Environment;
import android.os.FileUtils;
import android.os.IPowerManager.Stub;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings.Secure;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserDataPreparer {
    private static final int MAX_PREPARE_USER_DATA_TIMES = 2;
    private static final String REBOOT_TIMES_WHEN_PREPARE_USER_DATA_KEY = "reboot_times_when_prepare_user_data";
    private static final String TAG = "UserDataPreparer";
    private static final String XATTR_SERIAL = "user.serial";
    private final Context mContext;
    private final Object mInstallLock;
    private final Installer mInstaller;
    private final SparseBooleanArray mInvalidUserIds = new SparseBooleanArray();
    private final boolean mOnlyCore;

    UserDataPreparer(Installer installer, Object installLock, Context context, boolean onlyCore) {
        this.mInstallLock = installLock;
        this.mContext = context;
        this.mOnlyCore = onlyCore;
        this.mInstaller = installer;
    }

    void prepareUserData(int userId, int userSerial, int flags) {
        synchronized (this.mInstallLock) {
            for (VolumeInfo vol : ((StorageManager) this.mContext.getSystemService(StorageManager.class)).getWritablePrivateVolumes()) {
                prepareUserDataLI(vol.getFsUuid(), userId, userSerial, flags, true);
            }
        }
    }

    private void prepareUserDataLI(String volumeUuid, int userId, int userSerial, int flags, boolean allowRecover) {
        try {
            ((StorageManager) this.mContext.getSystemService(StorageManager.class)).prepareUserStorage(volumeUuid, userId, userSerial, flags);
            if (!((flags & 1) == 0 || (this.mOnlyCore ^ 1) == 0)) {
                enforceSerialNumber(getDataUserDeDirectory(volumeUuid, userId), userSerial);
                if (Objects.equals(volumeUuid, StorageManager.UUID_PRIVATE_INTERNAL)) {
                    enforceSerialNumber(getDataSystemDeDirectory(userId), userSerial);
                }
            }
            if (!((flags & 2) == 0 || (this.mOnlyCore ^ 1) == 0)) {
                enforceSerialNumber(getDataUserCeDirectory(volumeUuid, userId), userSerial);
                if (Objects.equals(volumeUuid, StorageManager.UUID_PRIVATE_INTERNAL)) {
                    enforceSerialNumber(getDataSystemCeDirectory(userId), userSerial);
                }
            }
            this.mInstaller.createUserData(volumeUuid, userId, userSerial, flags);
            resetRebootTimes();
        } catch (Exception e) {
            PackageManagerService.logCriticalInfo(5, "Destroying user " + userId + " on volume " + volumeUuid + " because we failed to prepare: " + e);
            UserInfo info = UserManager.get(this.mContext).getUserInfo(userId);
            if (info == null || (info.isPrimary() ^ 1) == 0) {
                tryToReboot();
            } else if (allowRecover) {
                prepareUserDataLI(volumeUuid, userId, userSerial, flags, false);
            }
        }
    }

    void destroyUserData(int userId, int flags) {
        synchronized (this.mInstallLock) {
            for (VolumeInfo vol : ((StorageManager) this.mContext.getSystemService(StorageManager.class)).getWritablePrivateVolumes()) {
                destroyUserDataLI(vol.getFsUuid(), userId, flags);
            }
        }
    }

    void destroyUserDataLI(String volumeUuid, int userId, int flags) {
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        try {
            this.mInstaller.destroyUserData(volumeUuid, userId, flags);
            if (Objects.equals(volumeUuid, StorageManager.UUID_PRIVATE_INTERNAL)) {
                if ((flags & 1) != 0) {
                    FileUtils.deleteContentsAndDir(getUserSystemDirectory(userId));
                    FileUtils.deleteContentsAndDir(getDataSystemDeDirectory(userId));
                    FileUtils.deleteContentsAndDir(getDataMiscDeDirectory(userId));
                }
                if ((flags & 2) != 0) {
                    FileUtils.deleteContentsAndDir(getDataSystemCeDirectory(userId));
                    FileUtils.deleteContentsAndDir(getDataMiscCeDirectory(userId));
                }
            }
            storage.destroyUserStorage(volumeUuid, userId, flags);
        } catch (Exception e) {
            PackageManagerService.logCriticalInfo(5, "Failed to destroy user " + userId + " on volume " + volumeUuid + ": " + e);
            addInvalidUserIds(userId);
        }
    }

    void reconcileUsers(String volumeUuid, List<UserInfo> validUsersList) {
        List<File> files = new ArrayList();
        Collections.addAll(files, FileUtils.listFilesOrEmpty(Environment.getDataUserDeDirectory(volumeUuid)));
        Collections.addAll(files, FileUtils.listFilesOrEmpty(Environment.getDataUserCeDirectory(volumeUuid)));
        Collections.addAll(files, FileUtils.listFilesOrEmpty(Environment.getDataSystemDeDirectory()));
        Collections.addAll(files, FileUtils.listFilesOrEmpty(Environment.getDataSystemCeDirectory()));
        Collections.addAll(files, FileUtils.listFilesOrEmpty(Environment.getDataMiscCeDirectory()));
        reconcileUsers(volumeUuid, validUsersList, files);
    }

    void reconcileUsers(String volumeUuid, List<UserInfo> validUsersList, List<File> files) {
        int userCount = validUsersList.size();
        SparseArray<UserInfo> users = new SparseArray(userCount);
        for (int i = 0; i < userCount; i++) {
            UserInfo user = (UserInfo) validUsersList.get(i);
            users.put(user.id, user);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                try {
                    int userId = Integer.parseInt(file.getName());
                    UserInfo info = (UserInfo) users.get(userId);
                    boolean destroyUser = false;
                    if (info == null) {
                        PackageManagerService.logCriticalInfo(5, "Destroying user directory " + file + " because no matching user was found");
                        destroyUser = true;
                    } else if (!this.mOnlyCore) {
                        try {
                            enforceSerialNumber(file, info.serialNumber);
                        } catch (IOException e) {
                            PackageManagerService.logCriticalInfo(5, "Destroying user directory " + file + " because we failed to enforce serial number: " + e);
                            destroyUser = true;
                        }
                    }
                    if (destroyUser) {
                        synchronized (this.mInstallLock) {
                            destroyUserDataLI(volumeUuid, userId, 3);
                        }
                    } else {
                        continue;
                    }
                } catch (NumberFormatException e2) {
                    Slog.w(TAG, "Invalid user directory " + file);
                }
            }
        }
    }

    protected File getDataMiscCeDirectory(int userId) {
        return Environment.getDataMiscCeDirectory(userId);
    }

    protected File getDataSystemCeDirectory(int userId) {
        return Environment.getDataSystemCeDirectory(userId);
    }

    protected File getDataMiscDeDirectory(int userId) {
        return Environment.getDataMiscDeDirectory(userId);
    }

    protected File getUserSystemDirectory(int userId) {
        return Environment.getUserSystemDirectory(userId);
    }

    protected File getDataUserCeDirectory(String volumeUuid, int userId) {
        return Environment.getDataUserCeDirectory(volumeUuid, userId);
    }

    protected File getDataSystemDeDirectory(int userId) {
        return Environment.getDataSystemDeDirectory(userId);
    }

    protected File getDataUserDeDirectory(String volumeUuid, int userId) {
        return Environment.getDataUserDeDirectory(volumeUuid, userId);
    }

    protected boolean isFileEncryptedEmulatedOnly() {
        return StorageManager.isFileEncryptedEmulatedOnly();
    }

    void enforceSerialNumber(File file, int serialNumber) throws IOException {
        if (isFileEncryptedEmulatedOnly()) {
            Slog.w(TAG, "Device is emulating FBE; assuming current serial number is valid");
            return;
        }
        int foundSerial = getSerialNumber(file);
        Slog.v(TAG, "Found " + file + " with serial number " + foundSerial);
        if (foundSerial == -1) {
            Slog.d(TAG, "Serial number missing on " + file + "; assuming current is valid");
            try {
                setSerialNumber(file, serialNumber);
            } catch (IOException e) {
                Slog.w(TAG, "Failed to set serial number on " + file, e);
            }
        } else if (foundSerial != serialNumber) {
            throw new IOException("Found serial number " + foundSerial + " doesn't match expected " + serialNumber);
        }
    }

    private static void setSerialNumber(File file, int serialNumber) throws IOException {
        try {
            Os.setxattr(file.getAbsolutePath(), XATTR_SERIAL, Integer.toString(serialNumber).getBytes(StandardCharsets.UTF_8), OsConstants.XATTR_CREATE);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    static int getSerialNumber(File file) throws IOException {
        String serial;
        try {
            serial = new String(Os.getxattr(file.getAbsolutePath(), XATTR_SERIAL));
            return Integer.parseInt(serial);
        } catch (NumberFormatException e) {
            throw new IOException("Bad serial number: " + serial);
        } catch (ErrnoException e2) {
            if (e2.errno == OsConstants.ENODATA) {
                return -1;
            }
            throw e2.rethrowAsIOException();
        }
    }

    private boolean checkWhetherToReboot() {
        int times = Secure.getInt(this.mContext.getContentResolver(), REBOOT_TIMES_WHEN_PREPARE_USER_DATA_KEY, 0);
        if (times < 2) {
            Slog.i(TAG, "check result is to go to reboot, times:" + times);
            Secure.putInt(this.mContext.getContentResolver(), REBOOT_TIMES_WHEN_PREPARE_USER_DATA_KEY, times + 1);
            return true;
        }
        Slog.i(TAG, "check result is to go to eRecovery, times:" + times);
        return false;
    }

    protected void tryToReboot() {
        try {
            if (checkWhetherToReboot()) {
                Thread.sleep(1000);
                Stub.asInterface(ServiceManager.getService("power")).reboot(false, "prepare user data failed! try to reboot...", false);
                return;
            }
            SystemProperties.set("sys.userstorage_block", "1");
        } catch (Exception e) {
            Slog.e(TAG, "try to reboot error, exception:" + e);
            SystemProperties.set("sys.userstorage_block", "1");
        }
    }

    protected void resetRebootTimes() {
        Secure.putInt(this.mContext.getContentResolver(), REBOOT_TIMES_WHEN_PREPARE_USER_DATA_KEY, 0);
    }

    private void addInvalidUserIds(int userId) {
        synchronized (this.mInvalidUserIds) {
            this.mInvalidUserIds.put(userId, true);
        }
    }

    boolean isUserIdInvalid(int userId) {
        boolean z;
        synchronized (this.mInvalidUserIds) {
            z = this.mInvalidUserIds.get(userId);
        }
        return z;
    }
}

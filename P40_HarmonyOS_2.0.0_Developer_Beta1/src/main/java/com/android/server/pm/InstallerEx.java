package com.android.server.pm;

import android.os.IBackupSessionCallback;
import com.android.server.pm.Installer;

public class InstallerEx {
    private Installer mInstaller;

    public static class InstallerExceptionEx extends Exception {
        public InstallerExceptionEx(String detailMessage) {
            super(detailMessage);
        }

        public InstallerExceptionEx(Installer.InstallerException e) {
            super(e.toString());
        }

        public static InstallerExceptionEx from(Exception e) throws InstallerExceptionEx {
            throw new InstallerExceptionEx(e.toString());
        }
    }

    public int startBackupSession(IBackupSessionCallback callback) throws InstallerExceptionEx {
        try {
            if (this.mInstaller != null) {
                return this.mInstaller.startBackupSession(callback);
            }
            return -1;
        } catch (Installer.InstallerException e) {
            throw new InstallerExceptionEx(e.getMessage());
        }
    }

    public int executeBackupTask(int sessionId, String taskCmd) throws InstallerExceptionEx {
        try {
            if (this.mInstaller != null) {
                return this.mInstaller.executeBackupTask(sessionId, taskCmd);
            }
            return -1;
        } catch (Installer.InstallerException e) {
            throw new InstallerExceptionEx(e.getMessage());
        }
    }

    public int finishBackupSession(int sessionId) throws InstallerExceptionEx {
        try {
            if (this.mInstaller != null) {
                return this.mInstaller.finishBackupSession(sessionId);
            }
            return -1;
        } catch (Installer.InstallerException e) {
            throw new InstallerExceptionEx(e.getMessage());
        }
    }

    public Installer getInstaller() {
        return this.mInstaller;
    }

    public void setInstaller(Installer installer) {
        this.mInstaller = installer;
    }

    public void createOatDir(String oatDir, String dexInstructionSet) throws InstallerExceptionEx {
        try {
            this.mInstaller.createOatDir(oatDir, dexInstructionSet);
        } catch (Installer.InstallerException e) {
            throw new InstallerExceptionEx(e);
        }
    }

    public void linkFile(String relativePath, String fromBase, String toBase) throws InstallerExceptionEx {
        try {
            this.mInstaller.linkFile(relativePath, fromBase, toBase);
        } catch (Installer.InstallerException e) {
            throw new InstallerExceptionEx(e);
        }
    }

    public boolean bindFile(String relativePath, String fromBase, String toBase) throws InstallerExceptionEx {
        try {
            return this.mInstaller.bindFile(relativePath, fromBase, toBase);
        } catch (Installer.InstallerException e) {
            throw new InstallerExceptionEx(e);
        }
    }

    public void generateMplCache(String apkPath, int uid, int cacheLevel, String classPath) throws InstallerExceptionEx {
        try {
            this.mInstaller.generateMplCache(apkPath, uid, cacheLevel, classPath);
        } catch (Installer.InstallerException e) {
            throw new InstallerExceptionEx(e);
        }
    }

    public void clearMplCache(String uuid, String packageName, int userId, int flags, long ceDataInode) throws InstallerExceptionEx {
        try {
            this.mInstaller.clearMplCache(uuid, packageName, userId, flags, ceDataInode);
        } catch (Installer.InstallerException e) {
            throw new InstallerExceptionEx(e);
        }
    }
}

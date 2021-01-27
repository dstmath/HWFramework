package com.android.server.pm;

public class MspesExUtilEx {
    private static MspesExUtilEx sInstance;
    private MspesExUtil mspesExUtil;

    public static synchronized MspesExUtilEx getInstance(IHwPackageManagerServiceExInner pmsEx) {
        MspesExUtilEx mspesExUtilEx;
        synchronized (MspesExUtilEx.class) {
            if (sInstance == null) {
                sInstance = new MspesExUtilEx();
                sInstance.setMspesExUtil(MspesExUtil.getInstance(pmsEx));
            }
            mspesExUtilEx = sInstance;
        }
        return mspesExUtilEx;
    }

    public MspesExUtil getMspesExUtil() {
        return this.mspesExUtil;
    }

    public void setMspesExUtil(MspesExUtil mspesExUtil2) {
        this.mspesExUtil = mspesExUtil2;
    }

    public void initMspesForbidInstallApps() {
        this.mspesExUtil.initMspesForbidInstallApps();
    }

    public boolean isForbidMspesUninstall(String pkg) {
        return this.mspesExUtil.isForbidMspesUninstall(pkg);
    }

    public String readMspesFile(String fileName) {
        return this.mspesExUtil.readMspesFile(fileName);
    }

    public boolean writeMspesFile(String fileName, String content) {
        return this.mspesExUtil.writeMspesFile(fileName, content);
    }

    public String getMspesOEMConfig() {
        return this.mspesExUtil.getMspesOEMConfig();
    }

    public int updateMspesOEMConfig(String src) {
        return this.mspesExUtil.updateMspesOEMConfig(src);
    }

    public boolean isInMspesForbidInstallPackageList(String pkg) {
        return this.mspesExUtil.isInMspesForbidInstallPackageList(pkg);
    }
}

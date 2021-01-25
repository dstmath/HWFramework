package com.android.server.cota;

public class CotaInstallImpl {
    private static final int COTA_APP_INSTALL_ILLEGAL = -3;
    private static volatile CotaInstallImpl mInstance = null;
    private CotaInstallCallBack mCotaInstallCallBack;

    public interface CotaInstallCallBack {
        int getStatus();

        void startAutoInstall(String str, String str2, String str3);

        void startInstall();
    }

    private CotaInstallImpl() {
    }

    public static CotaInstallImpl getInstance() {
        if (mInstance == null) {
            mInstance = new CotaInstallImpl();
        }
        return mInstance;
    }

    public void registInstallCallBack(CotaInstallCallBack c) {
        if (c != null) {
            this.mCotaInstallCallBack = c;
        }
    }

    public void unRegistInstallCallBack(CotaInstallCallBack c) {
        this.mCotaInstallCallBack = null;
    }

    public void doInstall() {
        CotaInstallCallBack cotaInstallCallBack = this.mCotaInstallCallBack;
        if (cotaInstallCallBack != null) {
            cotaInstallCallBack.startInstall();
        }
    }

    public int doGetStatus() {
        CotaInstallCallBack cotaInstallCallBack = this.mCotaInstallCallBack;
        if (cotaInstallCallBack != null) {
            return cotaInstallCallBack.getStatus();
        }
        return -3;
    }

    public void doStartAutoInstall(String apkInstallConfig, String removableApkInstallConfig, String strMccMnc) {
        CotaInstallCallBack cotaInstallCallBack = this.mCotaInstallCallBack;
        if (cotaInstallCallBack != null) {
            cotaInstallCallBack.startAutoInstall(apkInstallConfig, removableApkInstallConfig, strMccMnc);
        }
    }
}

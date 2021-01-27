package com.android.server.cota;

public class CotaInstallImpl {
    private static final int COTA_APP_INSTALL_ILLEGAL = -3;
    private static volatile CotaInstallImpl sInstance = null;
    private CotaInstallCallBack mCotaInstallCallBack;

    public interface CotaInstallCallBack {
        int getStatus();

        void startAutoInstall(String str, String str2, String str3);

        void startInstall();
    }

    private CotaInstallImpl() {
    }

    public static CotaInstallImpl getInstance() {
        if (sInstance == null) {
            sInstance = new CotaInstallImpl();
        }
        return sInstance;
    }

    public void registInstallCallBack(CotaInstallCallBack callback) {
        if (callback != null) {
            this.mCotaInstallCallBack = callback;
        }
    }

    public void unRegistInstallCallBack(CotaInstallCallBack callback) {
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

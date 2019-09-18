package com.android.server.cota;

public class CotaInstallImpl {
    private static final int COTA_APP_INSTALL_ILLEGAL = -3;
    private static volatile CotaInstallImpl mInstance = null;
    private CotaInstallCallBack mCotaInstallCallBack;

    public interface CotaInstallCallBack {
        int getStatus();

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
        if (this.mCotaInstallCallBack != null) {
            this.mCotaInstallCallBack.startInstall();
        }
    }

    public int doGetStatus() {
        if (this.mCotaInstallCallBack != null) {
            return this.mCotaInstallCallBack.getStatus();
        }
        return -3;
    }
}

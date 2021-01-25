package com.huawei.internal.telephony.uicc;

import android.os.Handler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;

public class UiccCardApplicationEx {
    private UiccCardApplication mUiccCardApplication;

    public UiccCardApplication getUiccCardApplication() {
        return this.mUiccCardApplication;
    }

    public void setUiccCardApplication(UiccCardApplication uccCardApplication) {
        this.mUiccCardApplication = uccCardApplication;
    }

    public IccRecordsEx getIccRecords() {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication == null) {
            return null;
        }
        IccRecords iccRecords = uiccCardApplication.getIccRecords();
        IccRecordsEx iccRecordsEx = new IccRecordsEx();
        iccRecordsEx.setIccRecords(iccRecords);
        return iccRecordsEx;
    }

    public void registerForReady(Handler handler, int what, Object object) {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication != null) {
            uiccCardApplication.registerForReady(handler, what, object);
        }
    }

    public void unregisterForReady(Handler handler) {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication != null) {
            uiccCardApplication.unregisterForReady(handler);
        }
    }

    public void registerForGetAdDone(Handler handler, int what, Object object) {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication != null) {
            uiccCardApplication.registerForGetAdDone(handler, what, object);
        }
    }

    public void unregisterForGetAdDone(Handler handler) {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication != null) {
            uiccCardApplication.unregisterForGetAdDone(handler);
        }
    }

    public IccCardApplicationStatusEx.AppStateEx getState() {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication == null) {
            return null;
        }
        return IccCardApplicationStatusEx.AppStateEx.getAppStateExByAppState(uiccCardApplication.getState());
    }

    public void registerForFdnStatusChange(Handler h, int what, Object obj) {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication != null) {
            uiccCardApplication.registerForFdnStatusChange(h, what, obj);
        }
    }

    public void unregisterForFdnStatusChange(Handler h) {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication != null) {
            uiccCardApplication.unregisterForFdnStatusChange(h);
        }
    }

    public void queryFdn() {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication != null) {
            uiccCardApplication.queryFdn();
        }
    }

    public boolean getIccFdnAvailable() {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication == null) {
            return false;
        }
        return uiccCardApplication.getIccFdnAvailable();
    }

    public boolean getIccFdnEnabled() {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication == null) {
            return false;
        }
        return uiccCardApplication.getIccFdnEnabled();
    }

    public int getType() {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication == null) {
            return 0;
        }
        return uiccCardApplication.getType().ordinal();
    }

    public UiccProfileEx getUiccProfileHw() {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication == null) {
            return null;
        }
        return uiccCardApplication.getUiccProfileHw();
    }

    public IccFileHandlerEx getIccFileHandler() {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication == null || uiccCardApplication.getIccFileHandler() == null) {
            return null;
        }
        IccFileHandlerEx iccFileHandlerEx = new IccFileHandlerEx();
        iccFileHandlerEx.setIccFileHandle(this.mUiccCardApplication.getIccFileHandler());
        return iccFileHandlerEx;
    }

    public UiccCardExt getUiccCard() {
        if (this.mUiccCardApplication == null) {
            return null;
        }
        UiccCardExt uiccCardExt = new UiccCardExt();
        uiccCardExt.setUiccCard(this.mUiccCardApplication.getUiccCard());
        return uiccCardExt;
    }

    public String getAid() {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication == null) {
            return null;
        }
        return uiccCardApplication.getAid();
    }

    public void notifyGetAdDone() {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication != null) {
            uiccCardApplication.notifyGetAdDone(null);
        }
    }

    public int getPhoneId() {
        UiccCardApplication uiccCardApplication = this.mUiccCardApplication;
        if (uiccCardApplication == null) {
            return -1;
        }
        return uiccCardApplication.getPhoneId();
    }
}

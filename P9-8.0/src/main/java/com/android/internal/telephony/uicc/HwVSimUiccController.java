package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.Rlog;
import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwVSimUiccController extends Handler {
    public static final int APP_FAM_3GPP = 1;
    private static final int EVENT_GET_ICC_STATUS_DONE = 2;
    private static final int EVENT_ICC_STATUS_CHANGED = 1;
    private static final int EVENT_RADIO_UNAVAILABLE = 3;
    private static boolean HWFLOW = false;
    private static final boolean HWLOGW_E = true;
    private static final String LOG_TAG = "VSimUiccController";
    private static HwVSimUiccController mInstance;
    private static final Object mLock = new Object();
    private CommandsInterface mCi;
    private Context mContext;
    protected RegistrantList mIccChangedRegistrants = new RegistrantList();
    private UiccCard mUiccCard;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(LOG_TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public static HwVSimUiccController make(Context c, CommandsInterface ci) {
        HwVSimUiccController hwVSimUiccController;
        synchronized (mLock) {
            if (mInstance != null) {
                throw new RuntimeException("VSimUiccController.make() should only be called once");
            }
            mInstance = new HwVSimUiccController(c, ci);
            hwVSimUiccController = mInstance;
        }
        return hwVSimUiccController;
    }

    private HwVSimUiccController(Context c, CommandsInterface ci) {
        if (HWFLOW) {
            logi("Creating VSimUiccController");
        }
        this.mContext = c;
        this.mCi = ci;
        this.mCi.registerForIccStatusChanged(this, 1, null);
        this.mCi.registerForAvailable(this, 1, null);
    }

    public static HwVSimUiccController getInstance() {
        HwVSimUiccController hwVSimUiccController;
        synchronized (mLock) {
            if (mInstance == null) {
                throw new RuntimeException("VSimUiccController.getInstance can't be called before make()");
            }
            hwVSimUiccController = mInstance;
        }
        return hwVSimUiccController;
    }

    public UiccCard getUiccCard() {
        UiccCard uiccCard;
        synchronized (mLock) {
            uiccCard = this.mUiccCard;
        }
        return uiccCard;
    }

    public IccRecords getIccRecords(int family) {
        synchronized (mLock) {
            UiccCardApplication app = getUiccCardApplication(family);
            if (app != null) {
                IccRecords iccRecords = app.getIccRecords();
                return iccRecords;
            }
            return null;
        }
    }

    public UiccCardApplication getUiccCardApplication(int family) {
        synchronized (mLock) {
            if (this.mUiccCard != null) {
                UiccCardApplication application = this.mUiccCard.getApplication(family);
                return application;
            }
            return null;
        }
    }

    public void registerForIccChanged(Handler h, int what, Object obj) {
        synchronized (mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mIccChangedRegistrants.add(r);
            r.notifyRegistrant();
        }
    }

    public void unregisterForIccChanged(Handler h) {
        synchronized (mLock) {
            this.mIccChangedRegistrants.remove(h);
        }
    }

    public void handleMessage(Message msg) {
        synchronized (mLock) {
            AsyncResult ar = msg.obj;
            switch (msg.what) {
                case 1:
                    if (HWFLOW) {
                        logi("Received EVENT_ICC_STATUS_CHANGED, calling getIccCardStatus");
                    }
                    this.mCi.getIccCardStatus(obtainMessage(2));
                    break;
                case 2:
                    if (HWFLOW) {
                        logi("Received EVENT_GET_ICC_STATUS_DONE");
                    }
                    onGetIccCardStatusDone(ar);
                    break;
                default:
                    Rlog.e(LOG_TAG, " Unknown Event " + msg.what);
                    break;
            }
        }
    }

    private synchronized void onGetIccCardStatusDone(AsyncResult ar) {
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "Error getting ICC status. RIL_REQUEST_GET_ICC_STATUS should never return an error", ar.exception);
            return;
        }
        IccCardStatus status = ar.result;
        if (this.mUiccCard == null) {
            this.mUiccCard = new UiccCard(this.mContext, this.mCi, status, 2);
        } else {
            this.mUiccCard.update(this.mContext, this.mCi, status);
        }
        if (HWFLOW) {
            logi("Notifying IccChangedRegistrants");
        }
        this.mIccChangedRegistrants.notifyRegistrants();
    }

    private void logi(String string) {
        Rlog.i(LOG_TAG, string);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int iccChangedRegistrantsSize = this.mIccChangedRegistrants.size();
        pw.println("VSimUiccController: " + this);
        pw.println(" mContext=" + this.mContext);
        pw.println(" mInstance=" + mInstance);
        pw.println(" mIccChangedRegistrants: size=" + iccChangedRegistrantsSize);
        for (int i = 0; i < iccChangedRegistrantsSize; i++) {
            pw.println("  mIccChangedRegistrants[" + i + "]=" + ((Registrant) this.mIccChangedRegistrants.get(i)).getHandler());
        }
        pw.println();
        pw.flush();
        if (this.mUiccCard == null) {
            pw.println("  mUiccCard=null");
            return;
        }
        pw.println("  mUiccCard=" + this.mUiccCard);
        this.mUiccCard.dump(fd, pw, args);
    }
}

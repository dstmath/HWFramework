package com.android.internal.telephony.uicc;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;

public class HwAdnRecordLoaderEx extends Handler implements IHwAdnRecordLoaderEx {
    static final int EVENT_ADN_LOAD_ALL_DONE = 3;
    static final String LOG_TAG = "HwAdnRecordLoaderEx";
    private IccFileHandler mFh = null;
    private IHwAdnRecordLoaderInner mHwAdnRecordLoaderInner = null;

    public HwAdnRecordLoaderEx(IHwAdnRecordLoaderInner adnRecordLoaderInner, IccFileHandler fh) {
        super(Looper.getMainLooper());
        this.mHwAdnRecordLoaderInner = adnRecordLoaderInner;
        this.mFh = fh;
    }

    public void loadAllAdnFromEFHw(int fileId, int extensionEF, Message response) {
        this.mHwAdnRecordLoaderInner.setLoaderPara(fileId, extensionEF, response);
        Rlog.e(LOG_TAG, "loadAllAdnFromEFWithoutEmpty fileId:" + fileId);
        this.mFh.loadEFLinearFixedAllExcludeEmpty(fileId, obtainMessage(3));
    }

    public void handleMessage(Message msg) {
        Rlog.e(LOG_TAG, "handelmsg: " + msg.what);
        this.mHwAdnRecordLoaderInner.handleMessageForEx(msg);
    }
}

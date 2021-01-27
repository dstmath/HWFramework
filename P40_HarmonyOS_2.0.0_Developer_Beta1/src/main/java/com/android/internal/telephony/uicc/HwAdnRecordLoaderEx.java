package com.android.internal.telephony.uicc;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.huawei.android.telephony.RlogEx;

public class HwAdnRecordLoaderEx extends Handler implements IHwAdnRecordLoaderEx {
    static final int EVENT_ADN_LOAD_ALL_DONE = 3;
    static final String LOG_TAG = "HwAdnRecordLoaderEx";
    private IIccFileHandlerInner mFileHandler = null;
    private IAdnRecordLoaderInner mHwAdnRecordLoaderInner = null;

    public HwAdnRecordLoaderEx(IAdnRecordLoaderInner adnRecordLoaderInner, IIccFileHandlerInner fileHandlerInner) {
        super(Looper.getMainLooper());
        this.mHwAdnRecordLoaderInner = adnRecordLoaderInner;
        this.mFileHandler = fileHandlerInner;
    }

    public void loadAllAdnFromEFHw(int fileId, int extensionEF, Message response) {
        this.mHwAdnRecordLoaderInner.setLoaderPara(fileId, extensionEF, response);
        RlogEx.i(LOG_TAG, "loadAllAdnFromEFWithoutEmpty fileId:" + fileId);
        this.mFileHandler.loadEFLinearFixedAllExcludeEmpty(fileId, obtainMessage(3));
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        RlogEx.i(LOG_TAG, "handelmsg: " + msg.what);
        this.mHwAdnRecordLoaderInner.handleMessageForEx(msg);
    }
}

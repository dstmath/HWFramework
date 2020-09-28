package com.android.internal.telephony.uicc;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Locale;

public class HwAdnRecordCacheEx extends Handler implements IHwAdnRecordCacheEx {
    private static final int EVENT_LOAD_ALL_ADN_LIKE_DONE = 1;
    private static final String LOG_TAG = "HwAdnRecordCacheEx";
    private SparseArray<ArrayList<Message>> mAdnLikeWaiters = null;
    private IccFileHandler mFh = null;
    private IHwAdnRecordCacheInner mHwAdnRecordCacheInner = null;

    public HwAdnRecordCacheEx(IHwAdnRecordCacheInner adnRecordCacheInner, IccFileHandler fh) {
        this.mHwAdnRecordCacheInner = adnRecordCacheInner;
        this.mFh = fh;
        this.mAdnLikeWaiters = this.mHwAdnRecordCacheInner.getAdnLikeWaiters();
    }

    public void requestLoadAllAdnHw(int fileId, int extensionEf, Message response) {
        ArrayList<AdnRecord> result = this.mHwAdnRecordCacheInner.getRecordsIfLoadedForEx(fileId);
        if (result != null) {
            if (response != null) {
                AsyncResult.forMessage(response).result = result;
                response.sendToTarget();
            }
            Rlog.e(LOG_TAG, "getRecords Loaded.");
            return;
        }
        ArrayList<Message> waiters = this.mAdnLikeWaiters.get(fileId);
        if (waiters != null) {
            waiters.add(response);
            return;
        }
        ArrayList<Message> waiters2 = new ArrayList<>();
        waiters2.add(response);
        this.mAdnLikeWaiters.put(fileId, waiters2);
        if (extensionEf < 0) {
            if (response != null) {
                AsyncResult forMessage = AsyncResult.forMessage(response);
                forMessage.exception = new RuntimeException("EF is not known ADN-like EF:0x" + Integer.toHexString(fileId).toUpperCase(Locale.ENGLISH));
                response.sendToTarget();
            }
            Rlog.e(LOG_TAG, "extensionEf < 0.");
            return;
        }
        new AdnRecordLoader(this.mFh).loadAllAdnFromEFHw(fileId, extensionEf, obtainMessage(1, fileId, 0));
    }

    public void handleMessage(Message msg) {
        this.mHwAdnRecordCacheInner.handleMessageForEx(msg);
    }
}

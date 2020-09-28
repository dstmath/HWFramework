package com.android.internal.telephony.uicc;

import android.os.Message;
import android.util.SparseArray;
import java.util.ArrayList;

public interface IHwAdnRecordCacheInner {
    SparseArray<ArrayList<Message>> getAdnLikeWaiters();

    ArrayList<AdnRecord> getRecordsIfLoadedForEx(int i);

    void handleMessageForEx(Message message);
}

package com.android.internal.telephony;

import android.content.Context;
import android.os.Bundle;
import android.os.ServiceManager;
import android.telephony.Rlog;
import com.android.internal.telephony.ISmsInterception.Stub;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmsInterceptionService extends Stub {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "SmsInterceptionService";
    static final int SMS_HANDLE_RESULT_BLOCK = 1;
    static final int SMS_HANDLE_RESULT_INVALID = -1;
    static final int SMS_HANDLE_RESULT_NOT_BLOCK = 0;
    private static SmsInterceptionService sInstance = null;
    private final Context mContext;
    private Map<Integer, ISmsInterceptionListener> mListener = new HashMap();

    private SmsInterceptionService(Context context) {
        this.mContext = context;
        if (ServiceManager.getService("isms_interception") == null) {
            ServiceManager.addService("isms_interception", this);
        }
    }

    public static SmsInterceptionService getDefault(Context context) {
        if (sInstance == null) {
            sInstance = new SmsInterceptionService(context);
        }
        return sInstance;
    }

    public void registerListener(ISmsInterceptionListener listener, int priority) {
        this.mContext.enforceCallingPermission("huawei.permission.RECEIVE_SMS_INTERCEPTION", "Enabling SMS interception");
        synchronized (this.mListener) {
            this.mListener.put(Integer.valueOf(priority), listener);
        }
        Rlog.d(LOG_TAG, "registerListener . priority : " + priority);
    }

    public void unregisterListener(int priority) {
        this.mContext.enforceCallingPermission("huawei.permission.RECEIVE_SMS_INTERCEPTION", "Disabling SMS interception");
        synchronized (this.mListener) {
            this.mListener.remove(Integer.valueOf(priority));
        }
        Rlog.d(LOG_TAG, "unregisterListener . priority : " + priority);
    }

    public boolean dispatchNewSmsToInterceptionProcess(Bundle smsInfo, boolean isWapPush) {
        if (this.mListener == null || this.mListener.size() == 0) {
            Rlog.d(LOG_TAG, "mListener is null or mListener.size is 0 !");
            return false;
        }
        Set<Integer> prioritySet = this.mListener.keySet();
        List<Integer> priorityList = new ArrayList();
        for (Integer keyInteger : prioritySet) {
            priorityList.add(keyInteger);
        }
        Collections.sort(priorityList);
        synchronized (this.mListener) {
            for (int i = priorityList.size() - 1; i >= 0; i--) {
                if (isWapPush) {
                    try {
                        if (1 == ((ISmsInterceptionListener) this.mListener.get(priorityList.get(i))).handleWapPushDeliverActionInner(smsInfo)) {
                            Rlog.d(LOG_TAG, "wap push is intercepted by ..." + this.mListener.get(priorityList.get(i)));
                            return true;
                        }
                    } catch (Exception e) {
                        Rlog.e(LOG_TAG, "Get exception while communicate with sms interception service: " + e.getMessage());
                    }
                } else if (1 == ((ISmsInterceptionListener) this.mListener.get(priorityList.get(i))).handleSmsDeliverActionInner(smsInfo)) {
                    Rlog.d(LOG_TAG, "sms is intercepted by ..." + this.mListener.get(priorityList.get(i)) + " priority " + priorityList.get(i));
                    return true;
                }
            }
            return false;
        }
    }

    public boolean sendNumberBlockedRecord(Bundle smsInfo) {
        if (this.mListener == null || this.mListener.size() == 0) {
            Rlog.d(LOG_TAG, "mListener is null or mListener.size is 0 !");
            return false;
        }
        Set<Integer> prioritySet = this.mListener.keySet();
        List<Integer> priorityList = new ArrayList();
        for (Integer keyInteger : prioritySet) {
            priorityList.add(keyInteger);
        }
        Collections.sort(priorityList);
        synchronized (this.mListener) {
            int i = priorityList.size() - 1;
            while (i >= 0) {
                try {
                    if (((ISmsInterceptionListener) this.mListener.get(priorityList.get(i))).sendNumberBlockedRecordInner(smsInfo)) {
                        Rlog.d(LOG_TAG, "block has record by ..." + this.mListener.get(priorityList.get(i)));
                        return true;
                    }
                    i--;
                } catch (Exception e) {
                    Rlog.e(LOG_TAG, "Get exception while communicate with sms interception service: " + e.getMessage());
                }
            }
            return false;
        }
    }
}

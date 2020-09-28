package com.android.internal.telephony;

import android.content.Context;
import android.os.Bundle;
import android.os.IDeviceIdleController;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import com.android.internal.telephony.ISmsInterception;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmsInterceptionService extends ISmsInterception.Stub {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "SmsInterceptionService";
    static final int SMS_HANDLE_RESULT_BLOCK = 1;
    static final int SMS_HANDLE_RESULT_INVALID = -1;
    static final int SMS_HANDLE_RESULT_NOT_BLOCK = 0;
    private static final int SMS_VERIFICATION_PRIORITY = 20000;
    private static SmsInterceptionService sInstance = null;
    private final Context mContext;
    IDeviceIdleController mDeviceIdleController;
    private Map<Integer, ISmsInterceptionListener> mListener = new HashMap();

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.internal.telephony.SmsInterceptionService */
    /* JADX WARN: Multi-variable type inference failed */
    private SmsInterceptionService(Context context) {
        this.mContext = context;
        if (ServiceManager.getService("isms_interception") == null) {
            ServiceManager.addService("isms_interception", this);
        }
        this.mDeviceIdleController = TelephonyComponentFactory.getInstance().inject(IDeviceIdleController.class.getName()).getIDeviceIdleController();
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
        Map<Integer, ISmsInterceptionListener> map = this.mListener;
        if (map == null || map.size() == 0) {
            Rlog.d(LOG_TAG, "mListener is null or mListener.size is 0 !");
            return false;
        }
        Set<Integer> prioritySet = this.mListener.keySet();
        List<Integer> priorityList = new ArrayList<>();
        for (Integer keyInteger : prioritySet) {
            priorityList.add(keyInteger);
        }
        Collections.sort(priorityList);
        synchronized (this.mListener) {
            for (int i = priorityList.size() - 1; i >= 0; i--) {
                Rlog.d(LOG_TAG, "begin to priorityList.size() = " + priorityList.size());
                if (priorityList.get(i).intValue() == SMS_VERIFICATION_PRIORITY) {
                    addWhiteNameListSms();
                }
                if (isInterception(smsInfo, isWapPush, priorityList, i)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isInterception(Bundle smsInfo, boolean isWapPush, List<Integer> priorityList, int index) {
        if (isWapPush) {
            try {
                if (this.mListener.get(priorityList.get(index)).handleWapPushDeliverActionInner(smsInfo) == 1) {
                    Rlog.d(LOG_TAG, "wap push is intercepted by ..." + this.mListener.get(priorityList.get(index)));
                    return true;
                }
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "dispatchNewSmsToInterceptionProcess get exception while communicate with sms interception service");
                return false;
            }
        } else if (this.mListener.get(priorityList.get(index)).handleSmsDeliverActionInner(smsInfo) == 1) {
            Rlog.d(LOG_TAG, "sms is intercepted by ..." + this.mListener.get(priorityList.get(index)) + " priority " + priorityList.get(index));
            return true;
        }
        return false;
    }

    public boolean sendNumberBlockedRecord(Bundle smsInfo) {
        Map<Integer, ISmsInterceptionListener> map = this.mListener;
        if (map == null || map.size() == 0) {
            Rlog.d(LOG_TAG, "mListener is null or mListener.size is 0 !");
            return false;
        }
        Set<Integer> prioritySet = this.mListener.keySet();
        List<Integer> priorityList = new ArrayList<>();
        for (Integer keyInteger : prioritySet) {
            priorityList.add(keyInteger);
        }
        Collections.sort(priorityList);
        synchronized (this.mListener) {
            for (int i = priorityList.size() - 1; i >= 0; i--) {
                if (sendSiglePriorityServiceRecord(priorityList, smsInfo, i)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean sendSiglePriorityServiceRecord(List<Integer> priorityList, Bundle smsInfo, int index) {
        try {
            if (!this.mListener.get(priorityList.get(index)).sendNumberBlockedRecordInner(smsInfo)) {
                return false;
            }
            Rlog.d(LOG_TAG, "block has record by ..." + this.mListener.get(priorityList.get(index)));
            return true;
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "sendNumberBlockedRecord get exception while communicate with sms interception service");
            return false;
        }
    }

    private void addWhiteNameListSms() {
        String pkg = SmsApplication.getDefaultSmsApplication(this.mContext, false).getPackageName();
        try {
            if (this.mDeviceIdleController != null) {
                this.mDeviceIdleController.addPowerSaveTempWhitelistAppForSms(pkg, 0, "sms_app");
            }
        } catch (RemoteException e) {
            Rlog.d(LOG_TAG, "NameNotFoundException add whitelist ");
        }
    }
}

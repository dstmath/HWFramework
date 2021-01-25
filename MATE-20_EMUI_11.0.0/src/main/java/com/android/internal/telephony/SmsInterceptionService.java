package com.android.internal.telephony;

import android.content.Context;
import android.os.Bundle;
import com.android.internal.telephony.ISmsInterception;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.IDeviceIdleControllerEx;
import com.huawei.internal.telephony.SmsApplicationExt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmsInterceptionService extends ISmsInterception.Stub {
    private static final boolean DBG = true;
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "SmsInterceptionService";
    private static final int SMS_HANDLE_RESULT_BLOCK = 1;
    private static final int SMS_HANDLE_RESULT_INVALID = -1;
    private static final int SMS_HANDLE_RESULT_NOT_BLOCK = 0;
    private static final int SMS_VERIFICATION_PRIORITY = 20000;
    private static SmsInterceptionService sInstance = null;
    private final Context mContext;
    private IDeviceIdleControllerEx mDeviceIdleController;
    private Map<Integer, ISmsInterceptionListener> mListener = new HashMap();

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.internal.telephony.SmsInterceptionService */
    /* JADX WARN: Multi-variable type inference failed */
    private SmsInterceptionService(Context context) {
        this.mContext = context;
        if (ServiceManagerEx.getService("isms_interception") == null) {
            ServiceManagerEx.addService("isms_interception", this);
        }
        this.mDeviceIdleController = new IDeviceIdleControllerEx();
    }

    public static SmsInterceptionService getDefault(Context context) {
        SmsInterceptionService smsInterceptionService;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new SmsInterceptionService(context);
            }
            smsInterceptionService = sInstance;
        }
        return smsInterceptionService;
    }

    public void registerListener(ISmsInterceptionListener listener, int priority) {
        this.mContext.enforceCallingPermission("huawei.permission.RECEIVE_SMS_INTERCEPTION", "Enabling SMS interception");
        synchronized (this.mListener) {
            this.mListener.put(Integer.valueOf(priority), listener);
        }
        RlogEx.i(LOG_TAG, "registerListener . priority : " + priority);
    }

    public void unregisterListener(int priority) {
        this.mContext.enforceCallingPermission("huawei.permission.RECEIVE_SMS_INTERCEPTION", "Disabling SMS interception");
        synchronized (this.mListener) {
            this.mListener.remove(Integer.valueOf(priority));
        }
        RlogEx.i(LOG_TAG, "unregisterListener . priority : " + priority);
    }

    public boolean dispatchNewSmsToInterceptionProcess(Bundle smsInfo, boolean isWapPush) {
        Map<Integer, ISmsInterceptionListener> map = this.mListener;
        if (map == null || map.size() == 0) {
            RlogEx.e(LOG_TAG, "mListener is null or mListener.size is 0 !");
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
                RlogEx.i(LOG_TAG, "begin to priorityList.size() = " + priorityList.size());
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
                    RlogEx.i(LOG_TAG, "wap push is intercepted by ..." + this.mListener.get(priorityList.get(index)));
                    return true;
                }
            } catch (Exception e) {
                RlogEx.e(LOG_TAG, "dispatchNewSmsToInterceptionProcess get exception while communicate with sms interception service");
                return false;
            }
        } else if (this.mListener.get(priorityList.get(index)).handleSmsDeliverActionInner(smsInfo) == 1) {
            RlogEx.i(LOG_TAG, "sms is intercepted by ..." + this.mListener.get(priorityList.get(index)) + " priority " + priorityList.get(index));
            return true;
        }
        return false;
    }

    public boolean sendNumberBlockedRecord(Bundle smsInfo) {
        Map<Integer, ISmsInterceptionListener> map = this.mListener;
        if (map == null || map.size() == 0) {
            RlogEx.e(LOG_TAG, "mListener is null or mListener.size is 0 !");
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
        if (priorityList != null) {
            try {
                if (this.mListener.get(priorityList.get(index)).sendNumberBlockedRecordInner(smsInfo)) {
                    RlogEx.i(LOG_TAG, "block has record by ..." + this.mListener.get(priorityList.get(index)));
                    return true;
                }
            } catch (Exception e) {
                RlogEx.e(LOG_TAG, "sendNumberBlockedRecord get exception while communicate with sms interception service");
                return false;
            }
        }
        return false;
    }

    private void addWhiteNameListSms() {
        String pkg = SmsApplicationExt.getDefaultSmsApplication(this.mContext, false).getPackageName();
        IDeviceIdleControllerEx iDeviceIdleControllerEx = this.mDeviceIdleController;
        if (iDeviceIdleControllerEx != null) {
            iDeviceIdleControllerEx.addPowerSaveTempWhitelistAppForSms(pkg, 0, "sms_app");
        }
    }
}

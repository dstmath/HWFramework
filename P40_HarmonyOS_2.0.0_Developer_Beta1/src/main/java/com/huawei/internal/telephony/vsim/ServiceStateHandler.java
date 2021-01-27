package com.huawei.internal.telephony.vsim;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.ServiceState;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.vsim.util.ArrayUtils;
import com.huawei.internal.telephony.vsim.util.AsyncResultUtil;
import java.util.Objects;

public final class ServiceStateHandler extends Handler {
    private static final String TAG = "ServiceStateHandler";
    private final PhoneExt mPhone;
    private ServiceState mServiceState = new ServiceState();

    public static void register(PhoneExt... phones) {
        if (!ArrayUtils.isEmpty(phones)) {
            ServiceStateHandler[] serviceStateHandlers = new ServiceStateHandler[phones.length];
            for (int i = 0; i < phones.length; i++) {
                serviceStateHandlers[i] = new ServiceStateHandler(Looper.myLooper(), phones[i]);
                phones[i].registerForServiceStateChanged(serviceStateHandlers[i], (int) HwVSimConstants.EVENT_SERVICE_STATE_CHANGE, (Object) null);
            }
        }
    }

    public ServiceStateHandler(Looper looper, PhoneExt phone) {
        super(looper);
        this.mPhone = phone;
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        if (msg == null) {
            sLogD("handleMessage, msg is null, return");
        } else if (msg.what == 103) {
            ServiceState newServiceState = (ServiceState) AsyncResultUtil.getResult(AsyncResultEx.from(msg.obj), ServiceState.class);
            if (newServiceState == null) {
                sLogD("handleMessage SERVICE_STATE_CHANGE fail, newServiceState null");
                return;
            }
            notifyServiceStateChanged(newServiceState, this.mServiceState, this.mPhone);
            this.mServiceState = new ServiceState(newServiceState);
        }
    }

    private void notifyServiceStateChanged(ServiceState newServiceState, ServiceState oldServiceState, PhoneExt phone) {
        if (newServiceState == null || oldServiceState == null || phone == null) {
            sLogD("ss or phone is null, return!");
            return;
        }
        sLogD("newServiceState:" + newServiceState + ", oldServiceState:" + oldServiceState);
        boolean isDataStatusChanged = false;
        boolean isRegisteredStatusChanged = ServiceStateEx.getVoiceRegState(newServiceState) != ServiceStateEx.getVoiceRegState(oldServiceState);
        if (ServiceStateEx.getDataState(newServiceState) != ServiceStateEx.getDataState(oldServiceState)) {
            isDataStatusChanged = true;
        }
        boolean isPlmnStatusChanged = !Objects.equals(ServiceStateEx.getVoiceOperatorNumeric(newServiceState), ServiceStateEx.getVoiceOperatorNumeric(oldServiceState));
        boolean isPlmnDataStatusChanged = true ^ Objects.equals(ServiceStateEx.getDataOperatorNumeric(newServiceState), ServiceStateEx.getDataOperatorNumeric(oldServiceState));
        sLogD("notifyServiceStateChanged isRegisteredStatusChanged:" + isRegisteredStatusChanged + "isDataStatusChanged:" + isDataStatusChanged + " isPlmnStatusChanged:" + isPlmnStatusChanged + "isPlmnDataStatusChanged:" + isPlmnDataStatusChanged + " phoneId:" + phone.getPhoneId());
        if (isRegisteredStatusChanged || isPlmnStatusChanged || isDataStatusChanged || isPlmnDataStatusChanged) {
            int phoneId = phone.getPhoneId();
            int subId = phone.getSubId();
            Intent intent = new Intent(HwVSimConstants.HW_VSIM_SERVICE_STATE_CHANGED);
            Bundle data = new Bundle();
            ServiceStateEx.fillInNotifierBundle(newServiceState, data);
            intent.putExtras(data);
            intent.putExtra("subscription", subId);
            intent.putExtra("slot", phoneId);
            phone.getContext().sendBroadcastAsUser(intent, UserHandleEx.ALL, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
            sLogD("sendBroadcastAsUser HW_VSIM_SERVICE_STATE_CHANGED, phoneId:" + phoneId + ", subId:" + phoneId);
        }
    }

    private static void sLogD(String msg) {
        HwVSimLog.VSimLogD(TAG, msg);
    }
}

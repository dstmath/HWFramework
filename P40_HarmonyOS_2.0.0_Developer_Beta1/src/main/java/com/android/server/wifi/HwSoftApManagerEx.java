package com.android.server.wifi;

import android.content.ComponentName;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.wifi.HwHiLog;
import android.widget.Toast;
import com.android.internal.util.State;
import huawei.android.app.admin.HwDevicePolicyManagerEx;

public class HwSoftApManagerEx implements IHwSoftApManagerEx {
    private static final String POLICY_OPEN_HOTSPOT = "policy-open-hotspot";
    private static final String TAG = "HwSoftApManagerEx";
    private static final String VALUE_DISABLE = "value_disable";
    private Context mContext;
    private IHwSoftApManagerInner mHwSoftApManagerInner;

    public static HwSoftApManagerEx createHwSoftApManagerEx(IHwSoftApManagerInner hwSoftApManagerInner, Context context) {
        HwHiLog.d(TAG, false, "createHwSoftApManagerEx is called!", new Object[0]);
        return new HwSoftApManagerEx(hwSoftApManagerInner, context);
    }

    public HwSoftApManagerEx(IHwSoftApManagerInner hwSoftApManagerInner, Context context) {
        this.mHwSoftApManagerInner = hwSoftApManagerInner;
        this.mContext = context;
    }

    public void logStateAndMessage(State state, Message message) {
        String str;
        if (message.what == this.mHwSoftApManagerInner.getCmd1()) {
            str = "CMD_START";
        } else if (message.what == this.mHwSoftApManagerInner.getCmd2()) {
            str = "CMD_INTERFACE_STATUS_CHANGED";
        } else {
            str = "what:" + Integer.toString(message.what);
        }
        HwHiLog.d(TAG, false, "%{public}s: handle message: %{public}s", new Object[]{state.getClass().getSimpleName(), str});
    }

    public boolean checkOpenHotsoptPolicy(WifiConfiguration apConfig) {
        Bundle bundle = new HwDevicePolicyManagerEx().getPolicy((ComponentName) null, POLICY_OPEN_HOTSPOT);
        if (bundle == null) {
            return true;
        }
        if ((apConfig != null && apConfig.preSharedKey != null) || !bundle.getBoolean(VALUE_DISABLE)) {
            return true;
        }
        HwHiLog.w(TAG, false, "SoftApState: MDM deny start unsecure soft ap!", new Object[0]);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            /* class com.android.server.wifi.HwSoftApManagerEx.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                Toast.makeText(HwSoftApManagerEx.this.mContext, HwSoftApManagerEx.this.mContext.getString(33685942), 0).show();
            }
        });
        return false;
    }
}

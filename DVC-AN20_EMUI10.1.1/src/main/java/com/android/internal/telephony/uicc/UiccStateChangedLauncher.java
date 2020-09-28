package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.uicc.IccCardStatus;

public class UiccStateChangedLauncher extends Handler {
    private static final int EVENT_ICC_CHANGED = 1;
    private static final String TAG = UiccStateChangedLauncher.class.getName();
    private static String sDeviceProvisioningPackage = null;
    private Context mContext;
    private boolean[] mIsRestricted = null;
    private UiccController mUiccController;

    public UiccStateChangedLauncher(Context context, UiccController controller) {
        sDeviceProvisioningPackage = context.getResources().getString(17039836);
        String str = sDeviceProvisioningPackage;
        if (str != null && !str.isEmpty()) {
            this.mContext = context;
            this.mUiccController = controller;
            this.mUiccController.registerForIccChanged(this, 1, null);
        }
    }

    public void handleMessage(Message msg) {
        if (msg.what == 1) {
            boolean shouldNotify = false;
            if (this.mIsRestricted == null) {
                this.mIsRestricted = new boolean[TelephonyManager.getDefault().getPhoneCount()];
                shouldNotify = true;
            }
            for (int i = 0; i < this.mIsRestricted.length; i++) {
                UiccCard uiccCard = this.mUiccController.getUiccCardForPhone(i);
                boolean z = uiccCard == null || uiccCard.getCardState() != IccCardStatus.CardState.CARDSTATE_RESTRICTED;
                boolean[] zArr = this.mIsRestricted;
                if (z != zArr[i]) {
                    zArr[i] = !zArr[i];
                    shouldNotify = true;
                }
            }
            if (shouldNotify) {
                notifyStateChanged();
                return;
            }
            return;
        }
        throw new RuntimeException("unexpected event not handled");
    }

    private void notifyStateChanged() {
        Intent intent = new Intent("android.intent.action.SIM_STATE_CHANGED");
        intent.setPackage(sDeviceProvisioningPackage);
        try {
            this.mContext.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "notifyStateChanged sendBroadcast Exception");
        }
    }
}

package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;

public class UiccStateChangedLauncher extends Handler {
    private static final int EVENT_ICC_CHANGED = 1;
    private static final String TAG = UiccStateChangedLauncher.class.getName();
    private static String sDeviceProvisioningPackage = null;
    private Context mContext;
    private boolean[] mIsRestricted = null;
    private UiccController mUiccController;

    public UiccStateChangedLauncher(Context context, UiccController controller) {
        sDeviceProvisioningPackage = context.getResources().getString(17039775);
        if (sDeviceProvisioningPackage != null && (sDeviceProvisioningPackage.isEmpty() ^ 1) != 0) {
            this.mContext = context;
            this.mUiccController = controller;
            this.mUiccController.registerForIccChanged(this, 1, null);
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                boolean shouldNotify = false;
                if (this.mIsRestricted == null) {
                    this.mIsRestricted = new boolean[TelephonyManager.getDefault().getPhoneCount()];
                    shouldNotify = true;
                }
                UiccCard[] cards = this.mUiccController.getUiccCards();
                int i = 0;
                while (cards != null && i < cards.length) {
                    boolean z = cards[i] != null ? cards[i].getCardState() != CardState.CARDSTATE_RESTRICTED : true;
                    if (z != this.mIsRestricted[i]) {
                        this.mIsRestricted[i] = this.mIsRestricted[i] ^ 1;
                        shouldNotify = true;
                    }
                    i++;
                }
                if (shouldNotify) {
                    notifyStateChanged();
                    return;
                }
                return;
            default:
                throw new RuntimeException("unexpected event not handled");
        }
    }

    private void notifyStateChanged() {
        Intent intent = new Intent("android.intent.action.SIM_STATE_CHANGED");
        intent.setPackage(sDeviceProvisioningPackage);
        try {
            this.mContext.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}

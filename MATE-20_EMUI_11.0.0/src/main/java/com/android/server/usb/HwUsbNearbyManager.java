package com.android.server.usb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import com.android.server.location.HwLocalLocationProvider;
import com.huawei.android.content.IntentExEx;
import java.nio.charset.StandardCharsets;

public class HwUsbNearbyManager {
    private static final String ACTION_STATE_CHANGED = "com.huawei.nearby.peripheral.action.STATE_CHANGED";
    private static final int EXTRA_BYTE_MASK = 255;
    private static final int EXTRA_BYTE_SIZE = 8;
    private static final int EXTRA_DESCRIPTION_MAXSIZE = 256;
    private static final byte[] EXTRA_DESCRIPTION_TAG = "hwnearby".getBytes(StandardCharsets.UTF_8);
    private static final int EXTRA_HEADER_LENGTH = 16;
    private static final int EXTRA_SUBTYPE_HICAR_DISCOVER = 1;
    private static final int EXTRA_TAG_LENGTH = 8;
    private static final int EXTRA_TYPE_HICAR = 1;
    private static final int EXTRA_TYPE_SIZE = 2;
    private static final String MSG_BUNDLE_TAG = "message.bundle";
    private static final String MSG_CONTENT_TAG = "message.content";
    private static final String MSG_SOURCE_TAG = "message.source";
    private static final String MSG_USB_REQUEST = "usb.request";
    private static final String NEARBY_PERMISSION = "com.huawei.permission.NEARBY";
    private static final String PERIPHERAL_STATE = "peripheral.state";
    private static final String TAG = HwUsbNearbyManager.class.getSimpleName();
    private static final String USB_CANCEL_MTP_DIALOG = "usb_cancel_mtp_dialog";
    private static final int USB_CANCEL_MTP_DIALOG_OFF = 0;
    private static final int USB_CANCEL_MTP_DIALOG_ON = 1;
    private static final int USB_STATE_ATTACHED = 1;
    private static final int USB_STATE_DETACHED = 0;
    private final Context mContext;
    private int mNearbyConnected = 0;
    private byte[] mNearbyInfoBytes = null;
    private String mUsbDefaultProperty;

    public HwUsbNearbyManager(Context context) {
        this.mContext = context;
    }

    private boolean isNearbyExtraDescription() {
        byte[] bArr = this.mNearbyInfoBytes;
        if (bArr == null || bArr.length == 0) {
            String magicString = TAG;
            Slog.i(magicString, "invalid nearby info: " + this.mNearbyInfoBytes);
            return false;
        }
        byte[] extraDescTagBytes = new byte[8];
        System.arraycopy(bArr, 0, extraDescTagBytes, 0, 8);
        String magicString2 = new String(extraDescTagBytes, StandardCharsets.UTF_8);
        String str = TAG;
        Slog.i(str, "Nearby Magic String： " + magicString2);
        if (!"hwnearby".equals(magicString2)) {
            Slog.i(TAG, "check Nearby infor string failed");
            return false;
        }
        byte[] bArr2 = this.mNearbyInfoBytes;
        int extraTotalLength = ((bArr2[8 + 1] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 8) | (bArr2[8] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
        if (extraTotalLength <= 256 && extraTotalLength >= 16) {
            return true;
        }
        String str2 = TAG;
        Slog.i(str2, "check total length failed: " + extraTotalLength);
        return false;
    }

    public void notifyNearbyInfo(byte[] nearbyInfoBytes, boolean isConnected) {
        this.mNearbyInfoBytes = nearbyInfoBytes;
        sendInfoToNearby(isConnected);
    }

    private void sendInfoToNearby(boolean isConnected) {
        String str = TAG;
        Slog.i(str, "sendInfoToNearby, isConnected: " + isConnected + " nearby connected: " + this.mNearbyConnected);
        if (!isConnected && this.mNearbyConnected == 0) {
            Slog.i(TAG, "No matter with Nearby");
        } else if (!isConnected || isNearbyExtraDescription()) {
            this.mNearbyConnected = isConnected ? 1 : 0;
            if (isConnected) {
                disableUsbMtpDialog();
            } else {
                enableUsbMtpDialog();
            }
            Bundle bundle = new Bundle();
            bundle.putInt(PERIPHERAL_STATE, this.mNearbyConnected);
            bundle.putString(MSG_SOURCE_TAG, MSG_USB_REQUEST);
            bundle.putByteArray(MSG_CONTENT_TAG, this.mNearbyInfoBytes);
            Intent intent = new Intent(ACTION_STATE_CHANGED);
            intent.putExtra(MSG_BUNDLE_TAG, bundle);
            IntentExEx.addHwFlags(intent, 16);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, NEARBY_PERMISSION);
        } else {
            Slog.i(TAG, "Check nearby header String failed");
        }
    }

    private void setUsbMtpDialogStatus(int status) {
        Settings.System.putInt(this.mContext.getContentResolver(), USB_CANCEL_MTP_DIALOG, status);
        String str = TAG;
        Slog.i(str, "Set cancel mtp dialog status to Settings: " + status);
    }

    private void enableUsbMtpDialog() {
        if (Settings.System.getInt(this.mContext.getContentResolver(), USB_CANCEL_MTP_DIALOG, 0) == 1) {
            setUsbMtpDialogStatus(0);
        }
    }

    private void disableUsbMtpDialog() {
        setUsbMtpDialogStatus(1);
    }
}

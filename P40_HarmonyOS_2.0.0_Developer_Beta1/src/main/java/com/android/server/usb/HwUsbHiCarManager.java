package com.android.server.usb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import com.android.server.location.HwLocalLocationProvider;
import com.huawei.android.content.IntentExEx;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HwUsbHiCarManager {
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
    private static final String TAG = HwUsbHiCarManager.class.getSimpleName();
    private static final String USB_CANCEL_MTP_DIALOG = "usb_cancel_mtp_dialog";
    private static final int USB_CANCEL_MTP_DIALOG_OFF = 0;
    private static final int USB_CANCEL_MTP_DIALOG_ON = 1;
    private static final int USB_STATE_ATTACHED = 1;
    private static final int USB_STATE_DETACHED = 0;
    private static final String USB_STATE_PROPERTY = "sys.usb.state";
    private final Context mContext;
    private byte[] mHiCarInfoBytes = null;
    private boolean mIsHiCarConnection = false;
    private String mUsbDefaultProperty = SystemProperties.get(USB_STATE_PROPERTY, "");
    private int mUsbState = 0;

    public HwUsbHiCarManager(Context context) {
        this.mContext = context;
        String str = TAG;
        Slog.i(str, "mUsbDefaultProperty is " + this.mUsbDefaultProperty);
    }

    private boolean isNearbyExtraDescription() {
        byte[] bArr = this.mHiCarInfoBytes;
        if (bArr == null || bArr.length < 16 || bArr.length > 256) {
            Slog.i(TAG, "invalid hicarinfo");
            return false;
        }
        byte[] extraDescTagBytes = new byte[8];
        System.arraycopy(bArr, 0, extraDescTagBytes, 0, 8);
        if (!Arrays.equals(extraDescTagBytes, EXTRA_DESCRIPTION_TAG)) {
            Slog.i(TAG, "check extra tag failed");
            return false;
        }
        byte[] bArr2 = this.mHiCarInfoBytes;
        int extraTotalLength = (bArr2[8] & 255) | ((bArr2[8 + 1] & 255) << 8);
        if (extraTotalLength > 256 || extraTotalLength < 16) {
            String str = TAG;
            Slog.i(str, "check total length failed: " + extraTotalLength);
            return false;
        }
        byte[] extraTrimInfoBytes = new byte[extraTotalLength];
        System.arraycopy(bArr2, 0, extraTrimInfoBytes, 0, extraTotalLength);
        this.mHiCarInfoBytes = extraTrimInfoBytes;
        int offset = 8 + 2;
        byte[] bArr3 = this.mHiCarInfoBytes;
        int extraType = (bArr3[offset] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) | ((bArr3[offset + 1] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 8);
        if (extraType != 1) {
            String str2 = TAG;
            Slog.i(str2, "check type failed: " + extraType);
            return false;
        }
        int offset2 = offset + 2 + 2;
        int extraSubType = ((bArr3[offset2 + 1] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 8) | (bArr3[offset2] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
        if (extraSubType != 1) {
            String str3 = TAG;
            Slog.i(str3, "check sub type failed: " + extraSubType);
            return false;
        }
        this.mIsHiCarConnection = true;
        this.mUsbState = 1;
        return true;
    }

    public void notifyHiCarInfo(byte[] hiCarInfoBytes, boolean isConnected) {
        this.mHiCarInfoBytes = hiCarInfoBytes;
        this.mUsbState = isConnected ? 1 : 0;
        sendHiCarInfoToNearby();
    }

    private void sendHiCarInfoToNearby() {
        int i = this.mUsbState;
        if (i == 1) {
            if (!isNearbyExtraDescription()) {
                Slog.i(TAG, "Check nearby extra description failed");
                return;
            }
        } else if (i == 0) {
            Slog.i(TAG, "Usb Detach");
            enableUsbMtpDialog();
            if (this.mIsHiCarConnection) {
                this.mIsHiCarConnection = false;
            } else {
                return;
            }
        } else {
            String str = TAG;
            Slog.i(str, "Unknown usb state: " + this.mUsbState);
        }
        Bundle bundle = new Bundle();
        bundle.putInt(PERIPHERAL_STATE, this.mUsbState);
        bundle.putString(MSG_SOURCE_TAG, MSG_USB_REQUEST);
        bundle.putByteArray(MSG_CONTENT_TAG, this.mHiCarInfoBytes);
        Intent intent = new Intent(ACTION_STATE_CHANGED);
        intent.putExtra(MSG_BUNDLE_TAG, bundle);
        IntentExEx.addHwFlags(intent, 16);
        String str2 = TAG;
        Slog.i(str2, "sendHiCarInfoToNearby, mUsbState(0:unplug, 1:plug):" + this.mUsbState);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, NEARBY_PERMISSION);
        if (this.mUsbState == 1) {
            disableUsbMtpDialog();
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

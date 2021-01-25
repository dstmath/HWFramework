package com.huawei.android.bluetooth.hfp;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import java.util.HashMap;

public class RemoteDeviceFeatures {
    private static final int BRSF_AG_CODEC_NEGOTIATION = 512;
    private static final int BRSF_AG_EC_NR = 2;
    private static final int BRSF_AG_ENHANCED_CALL_CONTROL = 128;
    private static final int BRSF_AG_ENHANCED_CALL_STATUS = 64;
    private static final int BRSF_AG_ENHANCED_ERR_RESULT_CODES = 256;
    private static final int BRSF_AG_IN_BAND_RING = 8;
    private static final int BRSF_AG_REJECT_CALL = 32;
    private static final int BRSF_AG_THREE_WAY_CALLING = 1;
    private static final int BRSF_AG_VOICE_RECOG = 4;
    private static final int BRSF_AG_VOICE_TAG_NUMBE = 16;
    private static final int BRSF_HF_CLIP = 4;
    private static final int BRSF_HF_CODEC_NEGOTIATION = 128;
    private static final int BRSF_HF_CW_THREE_WAY_CALLING = 2;
    private static final int BRSF_HF_EC_NR = 1;
    private static final int BRSF_HF_ENHANCED_CALL_CONTROL = 64;
    private static final int BRSF_HF_ENHANCED_CALL_STATUS = 32;
    private static final int BRSF_HF_REMOTE_VOL_CONTROL = 16;
    private static final int BRSF_HF_VOICE_REG_ACT = 8;
    private static final boolean DBG = true;
    protected static final boolean HWFLOW = true;
    private static final int MAX_HEADSET_CONNECTIONS = 5;
    private static final String TAG = "RemoteDeviceFeatures";
    private HashMap<BluetoothDevice, Integer> mHeadsetBrsf = new HashMap<>(5);

    private static native int getRemoteFeaturesNative(long j, byte[] bArr);

    public void add(long bluetoothInterface, BluetoothDevice device, byte[] address) {
        synchronized (this) {
            int remoteBrsf = getRemoteFeaturesNative(bluetoothInterface, address);
            Log.d(TAG, "Remote Brsf: " + remoteBrsf + " for device: " + getFormatMacAddress(device));
            this.mHeadsetBrsf.put(device, Integer.valueOf(remoteBrsf));
        }
    }

    public void remove(BluetoothDevice device, String state) {
        synchronized (this) {
            this.mHeadsetBrsf.remove(device);
        }
    }

    public void clear() {
        synchronized (this) {
            if (this.mHeadsetBrsf != null) {
                this.mHeadsetBrsf.clear();
            }
        }
    }

    public boolean isVoiceRecognitionSupported(BluetoothDevice device) {
        int remoteBrsf = 0;
        synchronized (this) {
            Integer tempRemoteBrsf = this.mHeadsetBrsf.get(device);
            if (tempRemoteBrsf != null) {
                remoteBrsf = tempRemoteBrsf.intValue();
            }
        }
        Log.d(TAG, "isBluetoothVoiceDialingEnabled mRemoteBrsf: " + remoteBrsf + " device: " + getFormatMacAddress(device) + " supported: " + (remoteBrsf & 8));
        if ((remoteBrsf & 8) != 0) {
            return true;
        }
        return false;
    }

    private String getFormatMacAddress(BluetoothDevice device) {
        if (device == null || device.getAddress() == null) {
            return "" + ((Object) null);
        }
        String address = device.getAddress();
        return address.substring(0, address.length() / 2) + "******";
    }

    static {
        Log.d(TAG, "Loading bluetoothcustex_jni JNI Library");
        System.loadLibrary("bluetoothex_jni");
    }
}

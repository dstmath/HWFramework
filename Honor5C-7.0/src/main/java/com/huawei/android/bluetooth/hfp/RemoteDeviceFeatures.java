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
    private static final boolean DBG = false;
    protected static final boolean HWFLOW = false;
    private static final String TAG = "RemoteDeviceFeatures";
    private HashMap<BluetoothDevice, Integer> mHeadsetBrsf;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.bluetooth.hfp.RemoteDeviceFeatures.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.bluetooth.hfp.RemoteDeviceFeatures.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.bluetooth.hfp.RemoteDeviceFeatures.<clinit>():void");
    }

    private static native int getRemoteFeaturesNative(long j, byte[] bArr);

    public RemoteDeviceFeatures() {
        this.mHeadsetBrsf = new HashMap();
    }

    public void add(long bluetoothHfpInterface, BluetoothDevice device, byte[] address) {
        synchronized (this) {
            int remoteBrsf = getRemoteFeaturesNative(bluetoothHfpInterface, address);
            if (DBG) {
                Log.d(TAG, "Remote Brsf: " + remoteBrsf + " for device: " + device);
            }
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
            Integer tempRemoteBrsf = (Integer) this.mHeadsetBrsf.get(device);
            if (tempRemoteBrsf != null) {
                remoteBrsf = tempRemoteBrsf.intValue();
            }
        }
        if (DBG) {
            Log.d(TAG, "isBluetoothVoiceDialingEnabled mRemoteBrsf: " + remoteBrsf + " device: " + device + " supported: " + (remoteBrsf & BRSF_HF_VOICE_REG_ACT));
        }
        return (remoteBrsf & BRSF_HF_VOICE_REG_ACT) != 0 ? true : HWFLOW;
    }
}

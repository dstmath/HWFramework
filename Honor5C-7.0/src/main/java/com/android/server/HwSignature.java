package com.android.server;

import android.util.Log;

public class HwSignature {
    private static final int AUTH_DS_LENGTH = 256;
    public static final int KEY_INDEX_GENERIC = 2;
    public static final int KEY_INDEX_HWCLOUD = 1;
    public static final int KEY_STATE_ERROR = -2;
    public static final int KEY_STATE_OK = 0;
    public static final int KEY_STATE_UNSUPPORT = -1;
    private static final String TAG = "HwSignature";
    private static final int TA_CLIENT_STATUS_INITING = -1;
    private static final int TA_CLIENT_STATUS_INIT_FAIL = -3;
    private static final int TA_CLIENT_STATUS_OK = 0;
    private static final int TA_CLIENT_STATUS_UNINIT = -2;
    private static HwSignature mInstance;
    private static boolean sLibReady;
    private int mClientStatus;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.HwSignature.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.HwSignature.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.HwSignature.<clinit>():void");
    }

    private native int sign_close();

    private native int sign_get_cert(int i, int i2, byte[] bArr);

    private native int sign_get_cert_type(int i);

    private native int sign_get_publickey(int i, byte[] bArr);

    private native int sign_init();

    public native int sign_check_key();

    public native int sign_hwmember(int i, byte[] bArr, int i2, byte[] bArr2, int i3, byte[] bArr3, int i4, byte[] bArr4, int i5, byte[] bArr5);

    private HwSignature() {
        this.mClientStatus = TA_CLIENT_STATUS_UNINIT;
        Log.d(TAG, "create HwSignature");
    }

    public static synchronized HwSignature getInstance() {
        HwSignature hwSignature;
        synchronized (HwSignature.class) {
            if (mInstance == null) {
                mInstance = new HwSignature();
            }
            hwSignature = mInstance;
        }
        return hwSignature;
    }

    private void initClient() {
        int ret = TA_CLIENT_STATUS_INITING;
        Log.d(TAG, "HwSignature 64bits so, initClient");
        this.mClientStatus = TA_CLIENT_STATUS_INITING;
        if (sLibReady) {
            try {
                ret = sign_init();
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "call libarary hwsign sign_init failed >>>>>" + e);
            }
        }
        if (ret == 0) {
            this.mClientStatus = TA_CLIENT_STATUS_OK;
            Log.i(TAG, "tee client init OK");
            return;
        }
        this.mClientStatus = TA_CLIENT_STATUS_INIT_FAIL;
        Log.e(TAG, "tee client init fail, ret = " + ret);
    }

    public byte[] signMessage(int keyIndex, byte[] packageName, int package_len, byte[] deviceId, int devid_len, byte[] signatureType, int sign_type_len, byte[] challenge, int challenge_len) {
        int ret = TA_CLIENT_STATUS_INITING;
        if (this.mClientStatus != 0) {
            Log.i(TAG, "teec client re init... in sign message");
            initClient();
        }
        if (this.mClientStatus == 0) {
            byte[] buf = new byte[AUTH_DS_LENGTH];
            try {
                ret = sign_hwmember(keyIndex, packageName, package_len, deviceId, devid_len, signatureType, sign_type_len, challenge, challenge_len, buf);
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "call libarary hwsign sign_hwmember failed >>>>>" + e);
            }
            if (ret != 0) {
                return new byte[TA_CLIENT_STATUS_OK];
            }
            return buf;
        }
        Log.e(TAG, "teec client init ERROR!");
        return new byte[TA_CLIENT_STATUS_OK];
    }

    public int checkKeyStatus(int keyIndex) {
        int ret = TA_CLIENT_STATUS_INITING;
        if (this.mClientStatus != 0) {
            Log.i(TAG, "teec client re init... in check status");
            initClient();
        }
        if (KEY_INDEX_HWCLOUD == keyIndex && this.mClientStatus == 0) {
            try {
                ret = sign_check_key();
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "call libarary hwsign sign_hwmember failed >>>>>" + e);
            }
            if (ret == 0) {
                return TA_CLIENT_STATUS_OK;
            }
            return TA_CLIENT_STATUS_UNINIT;
        }
        Log.e(TAG, "teec client init ERROR!");
        return TA_CLIENT_STATUS_INITING;
    }

    public int getPublicKey(int keyIndex, byte[] buf) {
        int ret = TA_CLIENT_STATUS_OK;
        if (this.mClientStatus != 0) {
            Log.i(TAG, "teec client re init...");
            initClient();
        }
        if (this.mClientStatus == 0) {
            try {
                ret = sign_get_publickey(keyIndex, buf);
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "call libarary hwsign get_publickey failed >>>>>" + e);
            }
        } else {
            Log.e(TAG, "teec client init ERROR!");
        }
        return ret;
    }

    public int getDeviceCertType(int keyIndex) {
        int ret = TA_CLIENT_STATUS_INITING;
        if (this.mClientStatus != 0) {
            Log.i(TAG, "teec client re init...");
            initClient();
        }
        if (this.mClientStatus == 0) {
            try {
                ret = sign_get_cert_type(keyIndex);
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "call libarary hwsign get_cert_type failed >>>>>" + e);
            }
        } else {
            Log.e(TAG, "teec client init ERROR!");
        }
        return ret;
    }

    public int getDeviceCert(int keyIndex, int certType, byte[] certBuf) {
        int ret = TA_CLIENT_STATUS_INITING;
        if (this.mClientStatus != 0) {
            Log.i(TAG, "teec client re init...");
            initClient();
        }
        if (this.mClientStatus == 0) {
            try {
                ret = sign_get_cert(keyIndex, certType, certBuf);
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "call libarary hwsign get_cert failed >>>>>" + e);
            }
        } else {
            Log.e(TAG, "teec client init ERROR!");
        }
        return ret;
    }
}

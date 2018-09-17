package org.ifaa.android.manager;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.IIFAAPlugin;
import huawei.android.security.IIFAAPluginCallBack.Stub;

public class IFAAManagerV2Impl extends IFAAManagerV2 {
    private static final boolean HW_DEBUG = false;
    public static final int IFAA_AUTH_FINGERPRINT = 1;
    public static final int IFAA_AUTH_IRIS = 2;
    public static final int IFAA_AUTH_NONE = 0;
    public static final int IFAA_FAIL = -1;
    public static final int IFAA_OK = 0;
    private static final int IFAA_PLUGIN_ID = 3;
    public static final int IFAA_VERSION = 2;
    private static final String SETTINGS_FP_CLASS = "com.android.settings.fingerprint.FingerprintSettingsActivity";
    private static final String SETTINGS_FP_PACKAGE = "com.android.settings";
    public static final int STATUS_BUSY = 1;
    public static final int STATUS_IDLE = 0;
    private static final String TAG = "IFAAManagerImpl";
    private IFAACallBack mCallback;
    FingerprintManager mFingerprintManager;
    private IIFAAPlugin mIFAAPlugin;
    private boolean mNotified;
    byte[] mRecData;
    int mRet;
    private Object mSignal;
    int mStatus;
    private Object mStatusLock;

    static class IFAACallBack extends Stub {
        private IFAAManagerV2Impl mImpl;

        public IFAACallBack(IFAAManagerV2Impl impl) {
            this.mImpl = impl;
        }

        public void processCmdResult(int ret, byte[] param) {
            if (this.mImpl != null) {
                if (IFAAManagerV2Impl.HW_DEBUG) {
                    Log.d(IFAAManagerV2Impl.TAG, "IFAA processCmdResult enter notify!!!");
                }
                this.mImpl.notifyResult(ret, param);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.ifaa.android.manager.IFAAManagerV2Impl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.ifaa.android.manager.IFAAManagerV2Impl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.ifaa.android.manager.IFAAManagerV2Impl.<clinit>():void");
    }

    public IFAAManagerV2Impl() {
        this.mSignal = new Object();
        this.mStatusLock = new Object();
        this.mCallback = new IFAACallBack(this);
        this.mStatus = STATUS_IDLE;
    }

    private IIFAAPlugin getIFAAPlugin() {
        if (this.mIFAAPlugin == null) {
            IBinder b = ServiceManager.getService("securityserver");
            if (b != null) {
                if (HW_DEBUG) {
                    Log.d(TAG, "getHwSecurityService");
                }
                try {
                    IBinder ifaaPluginBinder = IHwSecurityService.Stub.asInterface(b).bind(IFAA_PLUGIN_ID, this.mCallback);
                    if (ifaaPluginBinder != null) {
                        this.mIFAAPlugin = IIFAAPlugin.Stub.asInterface(ifaaPluginBinder);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Error in get IFAA Plugin ");
                }
            } else {
                Log.e(TAG, "getHwSecurityService failed!!!!");
            }
        }
        return this.mIFAAPlugin;
    }

    public int getSupportBIOTypes(Context context) {
        if (HW_DEBUG) {
            Log.d(TAG, "getSupportBIOTypes");
        }
        this.mFingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
        if (this.mFingerprintManager == null || !this.mFingerprintManager.isHardwareDetected()) {
            return STATUS_IDLE;
        }
        return STATUS_BUSY;
    }

    public int startBIOManager(Context context, int authType) {
        if (HW_DEBUG) {
            Log.d(TAG, "startBIOManager");
        }
        if (authType != STATUS_BUSY) {
            return IFAA_FAIL;
        }
        Intent i = new Intent("android.settings.SETTINGS");
        i.setClassName(SETTINGS_FP_PACKAGE, SETTINGS_FP_CLASS);
        i.setFlags(268435456);
        context.startActivity(i);
        return STATUS_IDLE;
    }

    public String getDeviceModel() {
        String deviceModel = SystemProperties.get("ro.product.fingerprintName");
        if (HW_DEBUG) {
            Log.d(TAG, "getDeviceModel: [" + deviceModel + "]");
        }
        return deviceModel;
    }

    public int getVersion() {
        if (HW_DEBUG) {
            Log.d(TAG, "getVersion");
        }
        return IFAA_VERSION;
    }

    public byte[] processCmdV2(Context context, byte[] param) {
        if (HW_DEBUG) {
            Log.d(TAG, "IFAA processCmdV2");
        }
        byte[] retByte = new byte[STATUS_IDLE];
        if (preProcessCmd()) {
            IIFAAPlugin ifaaPlugin = getIFAAPlugin();
            if (ifaaPlugin != null) {
                try {
                    ifaaPlugin.processCmd(this.mCallback, param);
                } catch (RemoteException e) {
                    Log.e(TAG, "processCmd failed: " + e);
                }
                try {
                    synchronized (this.mSignal) {
                        if (HW_DEBUG) {
                            Log.d(TAG, "IFAA processCmdV2 enter wait!!!");
                        }
                        if (!this.mNotified) {
                            this.mSignal.wait();
                        }
                        this.mNotified = HW_DEBUG;
                        if (HW_DEBUG) {
                            Log.d(TAG, "IFAA processCmdV2 enter waited ok!!!");
                        }
                    }
                } catch (InterruptedException e2) {
                    if (HW_DEBUG) {
                        Log.d(TAG, "IFAA processCmdV2 interrupted!!!");
                    }
                }
                if (this.mRet == 0) {
                    retByte = this.mRecData;
                }
            } else {
                Log.e(TAG, "IIFAAPlugin get failed!!!");
            }
            endProcessCmd();
            return retByte;
        }
        Log.e(TAG, "IFAA processCmdV2 failed because is busy!");
        return retByte;
    }

    private boolean preProcessCmd() {
        synchronized (this.mStatusLock) {
            if (this.mStatus == 0) {
                this.mStatus = STATUS_BUSY;
                this.mNotified = HW_DEBUG;
                return true;
            }
            return HW_DEBUG;
        }
    }

    private void endProcessCmd() {
        synchronized (this.mStatusLock) {
            this.mStatus = STATUS_IDLE;
        }
    }

    private void notifyResult(int ret, byte[] param) {
        synchronized (this.mStatusLock) {
            if (this.mStatus == STATUS_BUSY) {
                this.mRet = ret;
                this.mRecData = param;
                synchronized (this.mSignal) {
                    this.mSignal.notify();
                    this.mNotified = true;
                }
            }
        }
    }
}

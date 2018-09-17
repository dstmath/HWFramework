package com.android.server.LatticeIrService;

import android.content.Context;
import android.irself.IIrSelfLearningManager.Stub;
import android.util.Slog;

public class IrSelfLearningManagerService extends Stub {
    private static final int MAX_XMIT_BUFFER = 4096;
    private static final String SECURITY_EXCEPTION = "Requires SELFBUILD_IR_SERVICE permission";
    private static final String TAG = "IrSelfLearningManagerService";
    private int err;
    private final Context mContext;
    private boolean mDeviceInit_done;
    private final long mHal;
    private final Object mHalLock;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.LatticeIrService.IrSelfLearningManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.LatticeIrService.IrSelfLearningManagerService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.LatticeIrService.IrSelfLearningManagerService.<clinit>():void");
    }

    private static native long Openirslf_hal();

    private static native int halDeviceExit(long j);

    private static native int halDeviceInit(long j);

    private static native int halGetLearningStatus(long j);

    private static native int[] halReadIRCode(long j);

    private static native int halReadIRFrequency(long j);

    private static native int halStartLearning(long j);

    private static native int halStopLearning(long j);

    private static native int halself_learning_support(long j);

    private static native int halsendIR(long j, int i, int[] iArr);

    public IrSelfLearningManagerService(Context context) {
        this.mHalLock = new Object();
        this.mDeviceInit_done = false;
        this.mContext = context;
        this.mDeviceInit_done = false;
        this.mHal = Openirslf_hal();
        if (this.mHal == 0) {
            Slog.e(TAG, "Lattice IR Service, HAL not loaded");
        }
    }

    private void throwifNoLatticehal() {
        if (this.mHal == 0) {
            throw new UnsupportedOperationException("Lattice IR Service, HAL not loaded");
        } else if (!this.mDeviceInit_done) {
            throw new UnsupportedOperationException("DeviceInit function is not called first or DeviceInit function returned with some failure");
        }
    }

    public boolean hasIrSelfLearning() {
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        boolean DeviceInit_done_temp = this.mDeviceInit_done;
        this.mDeviceInit_done = true;
        throwifNoLatticehal();
        this.mDeviceInit_done = DeviceInit_done_temp;
        if (halself_learning_support(this.mHal) == 1) {
            return true;
        }
        return false;
    }

    public boolean deviceInit() {
        boolean z = true;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        this.mDeviceInit_done = true;
        throwifNoLatticehal();
        synchronized (this.mHalLock) {
            this.err = halDeviceInit(this.mHal);
            if (this.err < 0) {
                this.mDeviceInit_done = false;
                Slog.e(TAG, "Error DeviceInit() : " + this.err);
            }
            if (this.err != 0) {
                z = false;
            }
        }
        return z;
    }

    public boolean deviceExit() {
        boolean z = false;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        throwifNoLatticehal();
        this.mDeviceInit_done = false;
        synchronized (this.mHalLock) {
            this.err = halDeviceExit(this.mHal);
            if (this.err < 0) {
                Slog.e(TAG, "Error halDeviceExit() : " + this.err);
            }
            if (this.err == 0) {
                z = true;
            }
        }
        return z;
    }

    public boolean transmit(int carrierFrequency, int[] pattern) {
        boolean z = false;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        if (pattern == null) {
            return false;
        }
        long totalXmitbyte = 0;
        for (int slice : pattern) {
            if (slice <= 0) {
                throw new IllegalArgumentException("Non-positive IR slice");
            }
            totalXmitbyte += 2;
        }
        if (totalXmitbyte > 4096) {
            throw new IllegalArgumentException("IR pattern too long");
        }
        throwifNoLatticehal();
        synchronized (this.mHalLock) {
            this.err = halsendIR(this.mHal, carrierFrequency, pattern);
            if (this.err < 0) {
                Slog.e(TAG, "Error transmitting: " + this.err);
            }
            if (this.err == 0) {
                z = true;
            }
        }
        return z;
    }

    public boolean startLearning() {
        boolean z = false;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        throwifNoLatticehal();
        synchronized (this.mHalLock) {
            this.err = halStartLearning(this.mHal);
            if (this.err < 0) {
                Slog.e(TAG, "Error halStartLearning() : " + this.err);
            }
            if (this.err == 0) {
                z = true;
            }
        }
        return z;
    }

    public boolean stopLearning() {
        boolean z = false;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        throwifNoLatticehal();
        synchronized (this.mHalLock) {
            this.err = halStopLearning(this.mHal);
            if (this.err < 0) {
                Slog.e(TAG, "Error halStopLearning() : " + this.err);
            }
            if (this.err == 0) {
                z = true;
            }
        }
        return z;
    }

    public boolean getLearningStatus() {
        boolean z = true;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        throwifNoLatticehal();
        synchronized (this.mHalLock) {
            this.err = halGetLearningStatus(this.mHal);
            if (this.err < 0) {
                Slog.e(TAG, "Error GetLearningStatus() : " + this.err);
            }
            if (this.err != 1) {
                z = false;
            }
        }
        return z;
    }

    public int readIRFrequency() {
        int i;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        throwifNoLatticehal();
        synchronized (this.mHalLock) {
            this.err = halReadIRFrequency(this.mHal);
            if (this.err < 0) {
                Slog.e(TAG, "Error halReadIRFrequency : " + this.err);
            }
            i = this.err;
        }
        return i;
    }

    public int[] readIRCode() {
        int[] halReadIRCode;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        throwifNoLatticehal();
        synchronized (this.mHalLock) {
            halReadIRCode = halReadIRCode(this.mHal);
        }
        return halReadIRCode;
    }
}

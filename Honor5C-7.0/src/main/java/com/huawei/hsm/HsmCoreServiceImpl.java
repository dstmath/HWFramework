package com.huawei.hsm;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import com.huawei.hsm.IHsmCoreService.Stub;
import com.huawei.hsm.transacthandler.AbsTransactHandler;
import com.huawei.lcagent.client.MetricConstant;
import com.huawei.motiondetection.MotionTypeApps;

public final class HsmCoreServiceImpl extends Stub {
    private static final String TAG = "HsmCoreServiceImpl";
    private static SparseArray<AbsTransactHandler> sHandlerMap;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hsm.HsmCoreServiceImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hsm.HsmCoreServiceImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hsm.HsmCoreServiceImpl.<clinit>():void");
    }

    public HsmCoreServiceImpl(Context context) {
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        Log.e(TAG, "onTransact in code is: " + code);
        switch (code) {
            case MotionTypeApps.TYPE_PICKUP_REDUCE_CLOCK /*102*/:
            case MotionTypeApps.TYPE_PICKUP_END_HINTS /*103*/:
            case MetricConstant.CAMERA_METRIC_ID_EX /*104*/:
                AbsTransactHandler itf = getHandler(code);
                if (itf != null) {
                    itf.handleTransactCode(code, data, reply);
                    return true;
                }
                Log.w(TAG, "onTransact can't get valid handler by code: " + code);
                return super.onTransact(code, data, reply, flags);
            default:
                Log.d(TAG, "onTransact default call super!");
                return super.onTransact(code, data, reply, flags);
        }
    }

    private AbsTransactHandler getHandler(int code) {
        return (AbsTransactHandler) sHandlerMap.get(code);
    }
}

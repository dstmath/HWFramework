package com.android.server.rms.iaware.srms;

import android.content.Context;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.feature.RFeature;

public class BroadcastFeature extends RFeature {
    private static final int BASE_VERSION = 2;
    private static final String TAG = "BroadcastFeature";
    private static boolean mFeature;
    private static boolean mFlowCtrlEnable;
    private static boolean mImplicitCapEnable;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.srms.BroadcastFeature.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.srms.BroadcastFeature.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.srms.BroadcastFeature.<clinit>():void");
    }

    public BroadcastFeature(Context context, FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
    }

    public static boolean isFeatureEnabled(int feature) {
        boolean z = false;
        if (feature == 10) {
            if (mFeature) {
                z = mFlowCtrlEnable;
            }
            return z;
        } else if (feature != 11) {
            return false;
        } else {
            if (mFeature) {
                z = mImplicitCapEnable;
            }
            return z;
        }
    }

    public boolean reportData(CollectData data) {
        return true;
    }

    public boolean enable() {
        AwareLog.i(TAG, "enable failed! feature based on IAware2.0, enable() method should not be called!");
        return false;
    }

    public boolean disable() {
        AwareLog.i(TAG, "disable iaware broadcast feature!");
        setEnable(false);
        return true;
    }

    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < BASE_VERSION) {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", BroadcastFeature baseVersion: " + BASE_VERSION);
            return false;
        }
        AwareLog.i(TAG, "enableFeatureEx iaware broadcast feature!");
        setEnable(true);
        return true;
    }

    private static void setEnable(boolean enable) {
        mFeature = enable;
    }

    public String saveBigData(boolean clear) {
        return null;
    }
}

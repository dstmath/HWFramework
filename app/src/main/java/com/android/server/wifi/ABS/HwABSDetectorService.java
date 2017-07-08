package com.android.server.wifi.ABS;

import android.content.Context;
import com.android.server.wifi.WifiStateMachine;

public class HwABSDetectorService {
    private static HwABSDetectorService mHwABSDetectorService;
    private HwABSStateMachine mHwABSStateMachine;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.ABS.HwABSDetectorService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.ABS.HwABSDetectorService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.ABS.HwABSDetectorService.<clinit>():void");
    }

    public HwABSDetectorService(Context context, WifiStateMachine wifiStateMachine) {
        this.mHwABSStateMachine = null;
        HwABSUtils.logE("init HwABSScenarioDetectorService");
        this.mHwABSStateMachine = HwABSStateMachine.createHwABSStateMachine(context, wifiStateMachine);
        this.mHwABSStateMachine.onStart();
    }

    public static HwABSDetectorService createHwABSDetectorService(Context context, WifiStateMachine wifiStateMachine) {
        if (mHwABSDetectorService == null) {
            mHwABSDetectorService = new HwABSDetectorService(context, wifiStateMachine);
        }
        return mHwABSDetectorService;
    }

    public static HwABSDetectorService getInstance() {
        return mHwABSDetectorService;
    }

    public boolean isABSSwitching() {
        HwABSUtils.logE("HwABSDetectorService isABSSwitching");
        return this.mHwABSStateMachine.isABSSwitching();
    }
}

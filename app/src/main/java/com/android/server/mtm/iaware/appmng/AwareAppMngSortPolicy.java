package com.android.server.mtm.iaware.appmng;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AwareAppMngSortPolicy {
    private static final boolean DEBUG = false;
    private static final String TAG = null;
    private int mProcessNum;
    Map<Integer, List<AwareProcessBlockInfo>> mSrcProcList;
    private Map<Integer, Integer> mUserHabitMap;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy.<clinit>():void");
    }

    public AwareAppMngSortPolicy(Context context, Map<Integer, List<AwareProcessBlockInfo>> sortProcList) {
        this.mSrcProcList = null;
        this.mUserHabitMap = null;
        this.mProcessNum = 0;
        this.mSrcProcList = sortProcList;
    }

    public List<AwareProcessBlockInfo> getAllowStopProcBlockList() {
        if (this.mSrcProcList == null) {
            return new ArrayList();
        }
        return (List) this.mSrcProcList.get(Integer.valueOf(2));
    }

    public List<AwareProcessBlockInfo> getShortageStopProcBlockList() {
        if (this.mSrcProcList == null) {
            return new ArrayList();
        }
        return (List) this.mSrcProcList.get(Integer.valueOf(1));
    }

    public List<AwareProcessBlockInfo> getForbidStopProcBlockList() {
        if (this.mSrcProcList == null) {
            return new ArrayList();
        }
        return (List) this.mSrcProcList.get(Integer.valueOf(0));
    }
}

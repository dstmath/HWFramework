package com.android.server.rms.iaware.hiber.listener;

import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;

class ResAppHandler extends AbsDataHandler {
    private static ResAppHandler sDataHandle;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.hiber.listener.ResAppHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.hiber.listener.ResAppHandler.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.hiber.listener.ResAppHandler.<clinit>():void");
    }

    protected static synchronized ResAppHandler getInstance() {
        ResAppHandler resAppHandler;
        synchronized (ResAppHandler.class) {
            if (sDataHandle == null) {
                sDataHandle = new ResAppHandler();
            }
            resAppHandler = sDataHandle;
        }
        return resAppHandler;
    }

    private ResAppHandler() {
    }

    protected int reportData(long timestamp, int event, AttrSegments attrSegments) {
        if (this.mAppHibernateTask == null) {
            return -1;
        }
        ArrayMap<String, String> appInfo = attrSegments.getSegment("calledApp");
        if (appInfo == null) {
            AwareLog.w("AppHiber_AbsDataHandler", "appInfo is NULL");
            return -1;
        }
        int retValue = -1;
        if (15005 == event) {
            try {
                retValue = this.mAppHibernateTask.interruptReclaim(Integer.parseInt((String) appInfo.get("uid")), (String) appInfo.get(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY), timestamp);
            } catch (NumberFormatException e) {
                AwareLog.e("AppHiber_AbsDataHandler", "get uid fail, happend NumberFormatException");
            }
        }
        return retValue;
    }
}

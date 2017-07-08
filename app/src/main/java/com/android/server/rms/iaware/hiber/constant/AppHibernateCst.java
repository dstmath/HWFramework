package com.android.server.rms.iaware.hiber.constant;

public class AppHibernateCst {
    private static final int APPHIBER_EVENT_TYPE_BASE = 90000;
    public static final long DELAY_ONE_MINS = 60000;
    public static final long DELAY_ZERO_MINS = 0;
    public static final int DUMP_ID_DUMP = 1;
    public static final int DUMP_ID_STATISTIC = 2;
    public static final int[] EMPTY_INT_ARRAY = null;
    public static final int ETYPE_APP_PROCESS_LAUNCHER_BEGIN = 90003;
    public static final int ETYPE_CONNECT_WITH_PG_SDK = 90005;
    public static final int ETYPE_MSG_WHAT_CREATE = 90001;
    public static final int ETYPE_MSG_WHAT_DESTORY = 90002;
    public static final int ETYPE_PG_CALLBACK_EVENT_TYPE_FROZEN = 1;
    public static final int ETYPE_PG_CALLBACK_EVENT_TYPE_THAWED = 2;
    public static final int FAILURE_OPERATE = -1;
    public static final long FIVE_HUNDRED_MILLISECONDS = 500;
    public static final int FRZ_STATE_FROZEN = 1;
    public static final int FRZ_STATE_THAWED = 0;
    public static final String HIBER_EVENT_SOCKET = "iawared";
    public static final int HIBER_MANAGER_CMD_DUMP = 4;
    public static final int HIBER_MANAGER_CMD_FREEZE = 1;
    public static final int HIBER_MANAGER_CMD_RECLAIM = 2;
    public static final int HIBER_MANAGER_CMD_SWITCH = 3;
    public static final String INVALID_PKG = "";
    public static final int INVALID_UID = -1;
    public static final long MILLISECONDS_PER_SECONDS = 1000;
    public static final int MSB_BASE_VALUE_APPHIBER = 103;
    public static final int ONE_GB = 1024;
    public static final String PG_HIBERNATE_FRZ_REASON = "whitelist";
    public static final int RELAIM_UID_WATER_LINE = 10000;
    public static final int SUCCESS_OPERATE = 0;
    public static final int SWITCH_ID_START = 1;
    public static final int SWITCH_ID_STOP = 0;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.hiber.constant.AppHibernateCst.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.hiber.constant.AppHibernateCst.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.hiber.constant.AppHibernateCst.<clinit>():void");
    }
}

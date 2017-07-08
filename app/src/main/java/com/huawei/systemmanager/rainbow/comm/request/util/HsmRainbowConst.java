package com.huawei.systemmanager.rainbow.comm.request.util;

import java.util.ArrayList;
import java.util.List;

public class HsmRainbowConst {
    public static final int BREAK_ON_FAIL = 3;
    public static final int BREAK_ON_SUCCESS = 2;
    public static final String CHECK_VERSION_BACKGROUND_WHITE = "v2_0005";
    public static final String CHECK_VERSION_COMPONENTS = "components";
    public static final String CHECK_VERSION_CONTROL_BLACK = "wbList_0009";
    public static final String CHECK_VERSION_CONTROL_WHITE = "wbList_0010";
    public static final String CHECK_VERSION_NAME = "name";
    public static final String CHECK_VERSION_PHONE = "wbList_0030";
    public static final String CHECK_VERSION_PUSH = "wbList_0011";
    public static final String CHECK_VERSION_RIGHT = "right";
    public static final String CHECK_VERSION_VERSION = "version";
    public static final int CONTINUE_ON_FAIL = 1;
    public static final int CONTINUE_ON_SUCCESS = 0;
    public static final String HOST_NAME_URL = "https://cloudsafe.hicloud.com/";
    public static final int NO_NEED_UPDATE = 20000;
    public static final String PHONE_EMUI = "emui";
    public static final String PHONE_IMEI = "imei";
    public static final String PHONE_OS_VERSION = "os";
    public static final String PHONE_SYSTEM = "systemid";
    public static final String PHONE_TYPE = "model";
    public static final String POST_CHECK_VERSION = "checkVersion.do";
    private static List<String> mCheckVersionNameList;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.systemmanager.rainbow.comm.request.util.HsmRainbowConst.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.systemmanager.rainbow.comm.request.util.HsmRainbowConst.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.rainbow.comm.request.util.HsmRainbowConst.<clinit>():void");
    }

    public static synchronized List<String> getCheckVersionNameList() {
        synchronized (HsmRainbowConst.class) {
            if (mCheckVersionNameList != null) {
                List<String> list = mCheckVersionNameList;
                return list;
            }
            mCheckVersionNameList = new ArrayList();
            mCheckVersionNameList.add(CHECK_VERSION_RIGHT);
            mCheckVersionNameList.add(CHECK_VERSION_CONTROL_BLACK);
            mCheckVersionNameList.add(CHECK_VERSION_CONTROL_WHITE);
            mCheckVersionNameList.add(CHECK_VERSION_BACKGROUND_WHITE);
            mCheckVersionNameList.add(CHECK_VERSION_PUSH);
            mCheckVersionNameList.add(CHECK_VERSION_PHONE);
            list = mCheckVersionNameList;
            return list;
        }
    }
}

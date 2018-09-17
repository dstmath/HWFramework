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
    private static List<String> mCheckVersionNameList = null;

    public static synchronized List<String> getCheckVersionNameList() {
        synchronized (HsmRainbowConst.class) {
            List<String> list;
            if (mCheckVersionNameList != null) {
                list = mCheckVersionNameList;
                return list;
            }
            mCheckVersionNameList = new ArrayList();
            mCheckVersionNameList.add("right");
            mCheckVersionNameList.add("wbList_0009");
            mCheckVersionNameList.add("wbList_0010");
            mCheckVersionNameList.add("v2_0005");
            mCheckVersionNameList.add("wbList_0011");
            mCheckVersionNameList.add("wbList_0030");
            list = mCheckVersionNameList;
            return list;
        }
    }
}

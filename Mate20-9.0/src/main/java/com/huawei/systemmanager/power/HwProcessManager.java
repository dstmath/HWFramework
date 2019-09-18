package com.huawei.systemmanager.power;

public class HwProcessManager {

    public enum UIDTYPE {
        ROOTUID,
        SYSTEMUID,
        MEDIAUID,
        SHELLUID,
        NORMALUID
    }

    public static UIDTYPE getKindOfUid(int uid) {
        if (uid == 0) {
            return UIDTYPE.ROOTUID;
        }
        if (uid == 1000) {
            return UIDTYPE.SYSTEMUID;
        }
        if (uid == 1013) {
            return UIDTYPE.MEDIAUID;
        }
        if (uid != 2000) {
            return UIDTYPE.NORMALUID;
        }
        return UIDTYPE.SHELLUID;
    }
}

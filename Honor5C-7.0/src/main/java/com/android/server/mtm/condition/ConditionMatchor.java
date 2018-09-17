package com.android.server.mtm.condition;

import android.os.Bundle;

public interface ConditionMatchor {
    public static final int BASE = 1000;
    public static final int COMBINATION = 1000;
    public static final int FORBIDDEN = 2;
    public static final int GROUPBG = 1006;
    public static final int GROUPDEFAULT = 1008;
    public static final int GROUPPERCEPTIBLE = 1007;
    public static final int GROUPTOPVISIBLE = 1005;
    public static final int HWINSTALL = 1002;
    public static final int MATCHED = 1;
    public static final int PACKAGENAME = 1009;
    public static final int PACKAGENAMECONTAINS = 1012;
    public static final int PROCESSNAME = 1010;
    public static final int PROCESSNAMECONTAINS = 1013;
    public static final int SCREENOFF = 1011;
    public static final int SYSTEMAPP = 1003;
    public static final int SYSTEMSERVER = 1004;
    public static final int THIRDPARTAPP = 1001;
    public static final int UNMATCHED = 0;

    int conditionMatch(int i, Bundle bundle);
}

package com.huawei.android.pushagent.constant;

public class a {
    private static final String[] gr = new String[]{"android.intent.action.TIME_SET", "android.intent.action.TIMEZONE_CHANGED", "com.huawei.android.push.intent.GET_PUSH_STATE", "com.huawei.android.push.intent.DEREGISTER", "com.huawei.intent.action.SELF_SHOW_FLAG", "com.huawei.android.push.intent.MSG_RESPONSE", "android.ctrlsocket.all.allowed", "android.scroff.ctrlsocket.status"};
    private static final String[] gs = new String[]{"com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT", "com.huawei.intent.action.PUSH_OFF", "com.huawei.action.CONNECT_PUSHSRV", "com.huawei.action.push.intent.CHECK_CHANNEL_CYCLE", "com.huawei.intent.action.PUSH", "com.huawei.android.push.intent.MSG_RSP_TIMEOUT", "com.huawei.android.push.intent.RESET_BASTET", "com.huawei.android.push.intent.RESPONSE_FAIL"};
    private static final String[] gt = new String[]{"com.huawei.android.push.intent.REGISTER"};

    public static String[] vl() {
        Object obj = new String[gs.length];
        System.arraycopy(gs, 0, obj, 0, gs.length);
        return obj;
    }

    public static String[] vj() {
        Object obj = new String[gr.length];
        System.arraycopy(gr, 0, obj, 0, gr.length);
        return obj;
    }

    public static String[] vk() {
        Object obj = new String[gt.length];
        System.arraycopy(gt, 0, obj, 0, gt.length);
        return obj;
    }
}

package com.huawei.server.pc;

public class DefaultHwPCDataReporter {
    public static final int FAIL_TO_LIGHT_SCREEN_REASON_KEYNOTRESPONSE = 1;

    public void reportKillProcessEvent(String packageName, String processName, int sourceDisplayId, int targetDisplayId) {
    }

    public void reportFailLightScreen(int exceptionType, int keyCode, String packageName) {
    }
}

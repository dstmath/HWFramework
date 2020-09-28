package com.huawei.android.appassistant;

import java.util.List;

public class AppAssistantManagerEx {
    private AppAssistantManagerEx() {
    }

    public static boolean addAssistantList(List<String> packageName, String func) {
        return HwAppAssistantManager.addAssistantList(packageName, func);
    }

    public static boolean delAssistantList(List<String> packageName, String func) {
        return HwAppAssistantManager.delAssistantList(packageName, func);
    }

    private static boolean isAssistantForeground(String func) {
        return HwAppAssistantManager.isAssistantForeground(func);
    }

    private static boolean isInAssistantList(String packageName, String func) {
        return HwAppAssistantManager.isInAssistantList(packageName, func);
    }

    public static List<String> getAssistantList(String func) {
        return HwAppAssistantManager.getAssistantList(func);
    }

    public static boolean isHasAssistantFunc(String func) {
        return HwAppAssistantManager.isHasAssistantFunc(func);
    }
}

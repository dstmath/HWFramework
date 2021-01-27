package com.huawei.ace.runtime;

public class AEventReport {
    public static final int FILE_CACHE_PATH_INIT_ERR = 0;
    public static final int IMAGE_CACHE_PATH_INIT_ERR = 0;
    public static final int TEXT_INPUT_PLUGIN_ERR = 0;
    public static final int VIEW_TYPE_ERR = 3;
    private static IEventReport eventReport;

    private AEventReport() {
    }

    public static void setEventReport(IEventReport iEventReport) {
        eventReport = iEventReport;
    }

    public static void sendFrameworkAppStartEvent(int i) {
        IEventReport iEventReport = eventReport;
        if (iEventReport != null) {
            iEventReport.sendFrameworkAppStartEvent(i);
        }
    }

    public static void sendComponentEvent(int i) {
        IEventReport iEventReport = eventReport;
        if (iEventReport != null) {
            iEventReport.sendComponentEvent(i);
        }
    }

    public static void sendRenderEvent(int i) {
        IEventReport iEventReport = eventReport;
        if (iEventReport != null) {
            iEventReport.sendRenderEvent(i);
        }
    }
}

package com.huawei.nb.utils.reporter.fault;

import android.util.SparseArray;
import com.huawei.android.util.IMonitorEx;

public abstract class Fault {
    protected static final short ACCESSOR = 8;
    protected static final short ACCESS_TYPE = 10;
    protected static final short APP_TYPE = 17;
    protected static final short APP_VERSION = 16;
    protected static final short BUSINESS = 11;
    protected static final short COMP = 6;
    protected static final short DETAIL = 15;
    protected static final short F1NAME = 2;
    protected static final short FINGERPRINT = 4;
    protected static final short INT_TIME_TYPE = 3;
    protected static final short LENGTH = 18;
    protected static final short NAME = 0;
    private static final int ODMF_FAULT_ID = 901002012;
    protected static final short PROCESS_ID = 1;
    private static final String PROCESS_NAME = "com.huawei.HwOPServer";
    protected static final short STATE = 13;
    protected static final short TARGET = 9;
    protected static final short TRIGGER = 14;
    protected static final short TYPE = 7;
    protected static final short URL = 12;
    protected static final short VERSION = 5;
    private static String appVersion = "";
    protected String keyMessage;
    protected SparseArray<String> parameters = new SparseArray<>();

    private SparseArray<String> getParameters() {
        return this.parameters;
    }

    public String getKeyMessage() {
        return this.keyMessage;
    }

    public static void setAppVersion(String str) {
        if (str != null) {
            appVersion = str;
        }
    }

    public IMonitorEx.EventStreamEx createEventStream() {
        IMonitorEx.EventStreamEx openEventStream = IMonitorEx.openEventStream((int) ODMF_FAULT_ID);
        if (openEventStream != null) {
            openEventStream.setParam(openEventStream, (short) F1NAME, getFunctionInfo());
            openEventStream.setParam(openEventStream, (short) PROCESS_ID, PROCESS_NAME);
            openEventStream.setParam(openEventStream, (short) VERSION, appVersion);
            SparseArray<String> parameters2 = getParameters();
            for (short s = NAME; s < 16; s = (short) (s + PROCESS_ID)) {
                if (parameters2.get(s) != null) {
                    openEventStream.setParam(openEventStream, s, parameters2.get(s));
                }
            }
        }
        return openEventStream;
    }

    private String getFunctionInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace == null) {
            return null;
        }
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (!stackTraceElement.isNativeMethod() && !stackTraceElement.getClassName().equals(Thread.class.getName()) && !stackTraceElement.getClassName().equals(Fault.class.getName()) && !stackTraceElement.getMethodName().equals("k") && !stackTraceElement.getMethodName().equals("a") && !stackTraceElement.getMethodName().equals("report") && !stackTraceElement.getMethodName().equals("f")) {
                return stackTraceElement.getMethodName();
            }
        }
        return "";
    }
}

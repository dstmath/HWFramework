package com.android.server.am;

public class ProcessRecordExUtils {
    public static ProcessRecordEx createProcessRecordEx(ProcessRecord pr) {
        return new ProcessRecordEx(pr);
    }
}

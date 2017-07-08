package com.android.server.emcom.grabservice;

import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppInfo {
    private static final boolean DEBUG = false;
    public Map<String, String> autograbParams;
    public String effectiveAutograbParam;
    public List<EventInfo> effectiveEvents;
    public List<EventInfo> events;
    public String packageName;
    int uid;

    public static class EventInfo {
        String activity;
        String description;
        String eventClassName;
        int eventType;
        String packageName;
        String text;
        public int uid;
        public String version;

        public EventInfo(String packageName) {
            this.packageName = AppHibernateCst.INVALID_PKG;
            this.version = AppHibernateCst.INVALID_PKG;
            this.eventClassName = AppHibernateCst.INVALID_PKG;
            this.activity = AppHibernateCst.INVALID_PKG;
            this.text = AppHibernateCst.INVALID_PKG;
            this.description = AppHibernateCst.INVALID_PKG;
            this.packageName = packageName;
        }
    }

    public AppInfo() {
        this.packageName = AppHibernateCst.INVALID_PKG;
        this.events = new ArrayList();
        this.effectiveEvents = new ArrayList();
        this.autograbParams = new HashMap();
        this.effectiveAutograbParam = AppHibernateCst.INVALID_PKG;
        this.uid = -1;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public List<EventInfo> getEvents() {
        return this.events;
    }

    public void setEffectiveAutograbParam(String params) {
        this.effectiveAutograbParam = params;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("package name:" + this.packageName + ", ").append("uid:" + this.uid + ", ").append("events size: " + this.events.size()).append("autograb params size: " + this.autograbParams.size());
        buffer.append("event size:" + this.effectiveEvents.size());
        return buffer.toString();
    }
}

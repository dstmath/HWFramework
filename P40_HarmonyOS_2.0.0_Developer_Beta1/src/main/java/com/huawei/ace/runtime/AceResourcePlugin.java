package com.huawei.ace.runtime;

import java.util.Map;

public abstract class AceResourcePlugin {
    private IAceOnResourceEvent callback;
    protected AceResourceRegister resRegister = null;
    private final String tag;
    private final float version;

    public abstract long create(Map<String, String> map);

    public abstract Object getObject(long j);

    public void onActivityPause() {
    }

    public void onActivityResume() {
    }

    public abstract void release();

    public abstract boolean release(long j);

    public AceResourcePlugin(String str, float f) {
        this.tag = str;
        this.version = f;
    }

    public void setEventCallback(AceResourceRegister aceResourceRegister, IAceOnResourceEvent iAceOnResourceEvent) {
        this.callback = iAceOnResourceEvent;
        this.resRegister = aceResourceRegister;
    }

    public IAceOnResourceEvent getEventCallback() {
        return this.callback;
    }

    public void registerCallMethod(String str, IAceOnCallResourceMethod iAceOnCallResourceMethod) {
        AceResourceRegister aceResourceRegister = this.resRegister;
        if (aceResourceRegister != null) {
            aceResourceRegister.registerCallMethod(str, iAceOnCallResourceMethod);
        }
    }

    public void registerCallMethod(Map<String, IAceOnCallResourceMethod> map) {
        if (this.resRegister != null) {
            for (Map.Entry<String, IAceOnCallResourceMethod> entry : map.entrySet()) {
                this.resRegister.registerCallMethod(entry.getKey(), entry.getValue());
            }
        }
    }

    public void unregisterCallMethod(String str) {
        this.resRegister.unregisterCallMethod(str);
    }

    public void unregisterCallMethod(Map<String, IAceOnCallResourceMethod> map) {
        for (Map.Entry<String, IAceOnCallResourceMethod> entry : map.entrySet()) {
            this.resRegister.unregisterCallMethod(entry.getKey());
        }
    }

    public String pluginType() {
        return this.tag;
    }

    public float version() {
        return this.version;
    }
}

package com.huawei.ace.runtime;

import java.util.HashMap;
import java.util.Map;

public class AceResourceRegister {
    private static final String LOG_TAG = "AceResourceRegister";
    private long aceRegisterPtr;
    private IAceOnResourceEvent callbackHandler = new IAceOnResourceEvent() {
        /* class com.huawei.ace.runtime.$$Lambda$AceResourceRegister$XZj40JN8mstmZ3M7rzQEPCN9IOw */

        @Override // com.huawei.ace.runtime.IAceOnResourceEvent
        public final void onEvent(String str, String str2) {
            AceResourceRegister.this.lambda$new$0$AceResourceRegister(str, str2);
        }
    };
    private Map<String, IAceOnCallResourceMethod> callmethodMap = new HashMap();
    private Map<String, AceResourcePlugin> pluginMap = new HashMap();

    private native void nativeOnEvent(long j, String str, String str2);

    public void registerPlugin(AceResourcePlugin aceResourcePlugin) {
        if (!this.pluginMap.containsKey(aceResourcePlugin.pluginType()) || aceResourcePlugin.version() > this.pluginMap.get(aceResourcePlugin.pluginType()).version()) {
            this.pluginMap.put(aceResourcePlugin.pluginType(), aceResourcePlugin);
            aceResourcePlugin.setEventCallback(this, this.callbackHandler);
        }
    }

    public void setRegisterPtr(long j) {
        this.aceRegisterPtr = j;
    }

    public long createResource(String str, String str2) {
        AceResourcePlugin aceResourcePlugin;
        if (!this.pluginMap.containsKey(str) || (aceResourcePlugin = this.pluginMap.get(str)) == null) {
            return -1;
        }
        return aceResourcePlugin.create(buildParamMap(str2));
    }

    public boolean releaseResource(String str) {
        AceResourcePlugin aceResourcePlugin;
        try {
            String[] split = str.split("@");
            if (split.length == 2 && this.pluginMap.containsKey(split[0]) && (aceResourcePlugin = this.pluginMap.get(split[0])) != null) {
                return aceResourcePlugin.release(Long.parseLong(split[1]));
            }
        } catch (NumberFormatException unused) {
            ALog.w(LOG_TAG, "NumberFormatException");
        }
        return false;
    }

    public Object getObject(String str) {
        AceResourcePlugin aceResourcePlugin;
        try {
            String[] split = str.split("@");
            if (split.length != 2 || !this.pluginMap.containsKey(split[0]) || (aceResourcePlugin = this.pluginMap.get(split[0])) == null) {
                return null;
            }
            return aceResourcePlugin.getObject(Long.parseLong(split[1]));
        } catch (NumberFormatException unused) {
            ALog.w(LOG_TAG, "NumberFormatException");
            return null;
        }
    }

    public Object getObject(String str, long j) {
        AceResourcePlugin aceResourcePlugin;
        if (!this.pluginMap.containsKey(str) || (aceResourcePlugin = this.pluginMap.get(str)) == null) {
            return null;
        }
        return aceResourcePlugin.getObject(j);
    }

    /* renamed from: onEvent */
    public void lambda$new$0$AceResourceRegister(String str, String str2) {
        nativeOnEvent(this.aceRegisterPtr, str, str2);
    }

    public void registerCallMethod(String str, IAceOnCallResourceMethod iAceOnCallResourceMethod) {
        this.callmethodMap.put(str, iAceOnCallResourceMethod);
    }

    public void unregisterCallMethod(String str) {
        this.callmethodMap.remove(str);
    }

    public String onCallMethod(String str, String str2) {
        IAceOnCallResourceMethod iAceOnCallResourceMethod = this.callmethodMap.containsKey(str) ? this.callmethodMap.get(str) : null;
        return iAceOnCallResourceMethod != null ? iAceOnCallResourceMethod.onCall(buildParamMap(str2)) : "no method found";
    }

    private Map<String, String> buildParamMap(String str) {
        HashMap hashMap = new HashMap();
        if (str != null && !str.isEmpty()) {
            for (String str2 : str.split("&")) {
                String[] split = str2.split("=");
                if (split.length == 2) {
                    hashMap.put(split[0], split[1]);
                }
            }
        }
        return hashMap;
    }

    public void release() {
        for (Map.Entry<String, AceResourcePlugin> entry : this.pluginMap.entrySet()) {
            entry.getValue().release();
        }
    }

    public void onActivityResume() {
        for (Map.Entry<String, AceResourcePlugin> entry : this.pluginMap.entrySet()) {
            entry.getValue().onActivityResume();
        }
    }

    public void onActivityPause() {
        for (Map.Entry<String, AceResourcePlugin> entry : this.pluginMap.entrySet()) {
            entry.getValue().onActivityPause();
        }
    }
}

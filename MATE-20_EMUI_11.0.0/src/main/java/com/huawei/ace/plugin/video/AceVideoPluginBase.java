package com.huawei.ace.plugin.video;

import com.huawei.ace.runtime.AceResourcePlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import ohos.ai.engine.bigreport.BigReportKeyValue;

public abstract class AceVideoPluginBase extends AceResourcePlugin {
    private static final String LOG_TAG = "AceVideoPluginBase";
    private final AtomicLong nextVideoid = new AtomicLong(0);
    private Map<Long, AceVideoBase> objectMap = new HashMap();

    @Override // com.huawei.ace.runtime.AceResourcePlugin
    public abstract long create(Map<String, String> map);

    public AceVideoPluginBase() {
        super(BigReportKeyValue.TYPE_VIDEO, 1.0f);
    }

    public long getAtomicId() {
        return this.nextVideoid.getAndIncrement();
    }

    public void addResource(long j, AceVideoBase aceVideoBase) {
        this.objectMap.put(Long.valueOf(j), aceVideoBase);
        registerCallMethod(aceVideoBase.getCallMethod());
    }

    @Override // com.huawei.ace.runtime.AceResourcePlugin
    public Object getObject(long j) {
        if (this.objectMap.containsKey(Long.valueOf(j))) {
            return this.objectMap.get(Long.valueOf(j));
        }
        return null;
    }

    @Override // com.huawei.ace.runtime.AceResourcePlugin
    public void onActivityResume() {
        for (Map.Entry<Long, AceVideoBase> entry : this.objectMap.entrySet()) {
            entry.getValue().onActivityResume();
        }
    }

    @Override // com.huawei.ace.runtime.AceResourcePlugin
    public void onActivityPause() {
        for (Map.Entry<Long, AceVideoBase> entry : this.objectMap.entrySet()) {
            entry.getValue().onActivityPause();
        }
    }

    @Override // com.huawei.ace.runtime.AceResourcePlugin
    public boolean release(long j) {
        if (!this.objectMap.containsKey(Long.valueOf(j))) {
            return false;
        }
        AceVideoBase aceVideoBase = this.objectMap.get(Long.valueOf(j));
        unregisterCallMethod(aceVideoBase.getCallMethod());
        aceVideoBase.release();
        this.objectMap.remove(Long.valueOf(j));
        return true;
    }

    @Override // com.huawei.ace.runtime.AceResourcePlugin
    public void release() {
        for (Map.Entry<Long, AceVideoBase> entry : this.objectMap.entrySet()) {
            entry.getValue().release();
        }
    }
}

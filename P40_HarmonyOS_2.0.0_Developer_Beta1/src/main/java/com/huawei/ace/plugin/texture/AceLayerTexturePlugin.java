package com.huawei.ace.plugin.texture;

import com.huawei.ace.adapter.IAceTextureLayer;
import com.huawei.ace.runtime.AceResourcePlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class AceLayerTexturePlugin extends AceResourcePlugin {
    private static final String LOG_TAG = "AceLayerTexturePlugin";
    private IAceTextureLayer aceTextureLayerCreator;
    private final AtomicLong nextTextureId = new AtomicLong(0);
    private Map<Long, AceLayerTexture> objectMap;
    private final IAceTexture textureImpl;

    private AceLayerTexturePlugin(IAceTexture iAceTexture, IAceTextureLayer iAceTextureLayer) {
        super("texture", 1.0f);
        this.textureImpl = iAceTexture;
        this.aceTextureLayerCreator = iAceTextureLayer;
        this.objectMap = new HashMap();
    }

    public static AceLayerTexturePlugin createRegister(IAceTexture iAceTexture, IAceTextureLayer iAceTextureLayer) {
        return new AceLayerTexturePlugin(iAceTexture, iAceTextureLayer);
    }

    @Override // com.huawei.ace.runtime.AceResourcePlugin
    public long create(Map<String, String> map) {
        AceLayerTexture aceLayerTexture = new AceLayerTexture(this.nextTextureId.get(), this.textureImpl, getEventCallback(), this.aceTextureLayerCreator);
        this.objectMap.put(Long.valueOf(this.nextTextureId.get()), aceLayerTexture);
        registerCallMethod(aceLayerTexture.getCallMethod());
        return this.nextTextureId.getAndIncrement();
    }

    @Override // com.huawei.ace.runtime.AceResourcePlugin
    public Object getObject(long j) {
        if (this.objectMap.containsKey(Long.valueOf(j))) {
            return this.objectMap.get(Long.valueOf(j));
        }
        return null;
    }

    @Override // com.huawei.ace.runtime.AceResourcePlugin
    public boolean release(long j) {
        if (!this.objectMap.containsKey(Long.valueOf(j))) {
            return false;
        }
        this.objectMap.get(Long.valueOf(j)).release();
        this.objectMap.remove(Long.valueOf(j));
        return true;
    }

    @Override // com.huawei.ace.runtime.AceResourcePlugin
    public void release() {
        for (Map.Entry<Long, AceLayerTexture> entry : this.objectMap.entrySet()) {
            entry.getValue().release();
        }
    }
}

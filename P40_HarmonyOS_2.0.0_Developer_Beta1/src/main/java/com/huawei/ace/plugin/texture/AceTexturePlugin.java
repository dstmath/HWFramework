package com.huawei.ace.plugin.texture;

import com.huawei.ace.runtime.AceResourcePlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class AceTexturePlugin extends AceResourcePlugin {
    private static final String LOG_TAG = "TextureSurface";
    private final AtomicLong nextTextureId = new AtomicLong(0);
    private Map<Long, AceTexture> objectMap;
    private final IAceTexture textureImpl;

    private AceTexturePlugin(IAceTexture iAceTexture) {
        super("texture", 1.0f);
        this.textureImpl = iAceTexture;
        this.objectMap = new HashMap();
    }

    public static AceTexturePlugin createRegister(IAceTexture iAceTexture) {
        return new AceTexturePlugin(iAceTexture);
    }

    @Override // com.huawei.ace.runtime.AceResourcePlugin
    public long create(Map<String, String> map) {
        this.objectMap.put(Long.valueOf(this.nextTextureId.get()), new AceTexture(this.nextTextureId.get(), this.textureImpl, getEventCallback()));
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
        for (Map.Entry<Long, AceTexture> entry : this.objectMap.entrySet()) {
            entry.getValue().release();
        }
    }
}

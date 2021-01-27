package com.huawei.ace.plugin.texture;

import android.view.TextureLayer;
import com.huawei.ace.adapter.IAceTextureLayer;
import com.huawei.ace.runtime.IAceOnCallResourceMethod;
import com.huawei.ace.runtime.IAceOnResourceEvent;
import java.util.HashMap;
import java.util.Map;

public class AceLayerTexture extends AceTexture {
    private static final String LOG_TAG = "AceLayerTexture";
    private IAceTextureLayer aceTextureLayerCreator;
    private Map<String, IAceOnCallResourceMethod> callMethodMap = new HashMap();
    private TextureLayer textureLayer;

    @Override // com.huawei.ace.plugin.texture.AceTexture
    public void setSurfaceTexture() {
    }

    public AceLayerTexture(long j, IAceTexture iAceTexture, IAceOnResourceEvent iAceOnResourceEvent, IAceTextureLayer iAceTextureLayer) {
        super(j, iAceTexture, iAceOnResourceEvent);
        this.aceTextureLayerCreator = iAceTextureLayer;
        $$Lambda$AceLayerTexture$z8rYiOt1WFssr36xaM2n2VuNsM r3 = new IAceOnCallResourceMethod() {
            /* class com.huawei.ace.plugin.texture.$$Lambda$AceLayerTexture$z8rYiOt1WFssr36xaM2n2VuNsM */

            @Override // com.huawei.ace.runtime.IAceOnCallResourceMethod
            public final String onCall(Map map) {
                return AceLayerTexture.this.lambda$new$0$AceLayerTexture(map);
            }
        };
        Map<String, IAceOnCallResourceMethod> map = this.callMethodMap;
        map.put("texture@" + j + "method=setTextureSize?", r3);
    }

    public Map<String, IAceOnCallResourceMethod> getCallMethod() {
        return this.callMethodMap;
    }

    /* renamed from: setTextureSize */
    public String lambda$new$0$AceLayerTexture(Map<String, String> map) {
        return updateTexture(Integer.parseInt(map.get("textureWidth")), Integer.parseInt(map.get("textureHeight")));
    }

    private String updateTexture(int i, int i2) {
        TextureLayer textureLayer2 = this.textureLayer;
        if (textureLayer2 == null) {
            return "false";
        }
        textureLayer2.prepare(i, i2, true);
        this.textureLayer.updateSurfaceTexture();
        return "success";
    }

    @Override // com.huawei.ace.plugin.texture.AceTexture
    public void markTextureFrame() {
        if (this.textureLayer == null) {
            this.textureLayer = this.aceTextureLayerCreator.createTextureLayer();
            this.textureImpl.registerTexture(this.id, this.textureLayer);
            this.textureLayer.setSurfaceTexture(this.surfaceTexture);
        }
        this.textureLayer.updateSurfaceTexture();
    }

    @Override // com.huawei.ace.plugin.texture.AceTexture
    public void release() {
        this.surfaceTexture.setOnFrameAvailableListener(null);
        if (this.textureLayer != null) {
            this.textureImpl.unregisterTexture(this.id);
        }
        this.surfaceTexture.release();
    }
}

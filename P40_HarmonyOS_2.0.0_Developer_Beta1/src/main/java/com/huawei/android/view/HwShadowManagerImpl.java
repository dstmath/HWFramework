package com.huawei.android.view;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.RenderNode;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParserException;

public class HwShadowManagerImpl implements IHwShadowManager {
    private static final float DEFAULT_ELEVATION = 0.0f;
    private static final float DENSITY_VALUE = 1.0f;
    private static final int ID_SHADOW_EFFECT_CONFIG = 34340867;
    private static final float INVALIDATE_VALUE = -1.0f;
    private static final String SHADOW_EFFECT_CONFIG = "shadow_effect_config";
    private static final String SHADOW_EFFECT_CONFIG_AMBIENTALPHA = "ambientAlpha";
    private static final String SHADOW_EFFECT_CONFIG_DEVICE = "device";
    private static final String SHADOW_EFFECT_CONFIG_ELEVATION = "elevation";
    private static final String SHADOW_EFFECT_CONFIG_MODE = "mode";
    private static final String SHADOW_EFFECT_CONFIG_SIZE = "size";
    private static final String SHADOW_EFFECT_CONFIG_SPOTALPHA = "spotAlpha";
    private static final String SHADOW_EFFECT_CONFIG_TRANSLATIONY = "translationY";
    private static final String TAG = "HwShadowManagerImpl";
    private static float density;
    private static HwShadowManagerImpl instance;
    private static Map<ShadowParams, ShadowEffectParams> shadowMap = new HashMap();
    private Context mContext;
    private ShadowEffectParams mShadowEffectParams = new ShadowEffectParams();
    private ShadowParams mShadowParams = new ShadowParams();

    private HwShadowManagerImpl(Context context) {
        density = 1.0f;
        if (context != null) {
            this.mContext = context;
            density = context.getResources().getDisplayMetrics().density;
            shadowMap.clear();
            shadowMap.putAll(parseShadowEffectConfigXml(context));
        }
    }

    public static synchronized HwShadowManagerImpl getInstance(Context context) {
        HwShadowManagerImpl hwShadowManagerImpl;
        synchronized (HwShadowManagerImpl.class) {
            if (instance == null) {
                instance = new HwShadowManagerImpl(context);
            }
            hwShadowManagerImpl = instance;
        }
        return hwShadowManagerImpl;
    }

    /* access modifiers changed from: private */
    public static class ShadowEffectParams {
        private float ambientAlpha;
        private float elevation;
        private float spotAlpha;
        private float translationY;

        public ShadowEffectParams() {
            this(0.0f, HwShadowManagerImpl.INVALIDATE_VALUE, HwShadowManagerImpl.INVALIDATE_VALUE, HwShadowManagerImpl.INVALIDATE_VALUE);
        }

        public ShadowEffectParams(float elevation2, float ambientAlpha2, float spotAlpha2, float translationY2) {
            this.elevation = elevation2;
            this.ambientAlpha = ambientAlpha2;
            this.spotAlpha = spotAlpha2;
            this.translationY = translationY2;
        }

        public void set(ShadowEffectParams other) {
            this.elevation = other.elevation;
            this.ambientAlpha = other.ambientAlpha;
            this.spotAlpha = other.spotAlpha;
            this.translationY = other.translationY;
        }

        public void setElevation(String elevation2) {
            try {
                this.elevation = Float.parseFloat(elevation2) * HwShadowManagerImpl.density;
            } catch (NumberFormatException e) {
                Log.e(HwShadowManagerImpl.TAG, "elevation parseFloat failure");
            }
        }

        public void setAmbientAlpha(String ambientAlpha2) {
            try {
                this.ambientAlpha = Float.parseFloat(ambientAlpha2);
            } catch (NumberFormatException e) {
                Log.e(HwShadowManagerImpl.TAG, "ambientAlpha parseFloat failure");
            }
        }

        public void setSpotAlpha(String spotAlpha2) {
            try {
                this.spotAlpha = Float.parseFloat(spotAlpha2);
            } catch (NumberFormatException e) {
                Log.e(HwShadowManagerImpl.TAG, "spotAlpha parseFloat failure");
            }
        }

        public void setTranslationY(String translationY2) {
            try {
                this.translationY = Float.parseFloat(translationY2) * HwShadowManagerImpl.density;
            } catch (NumberFormatException e) {
                Log.e(HwShadowManagerImpl.TAG, "translationY parseFloat failure");
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ShadowParams {
        private int device;
        private int mode;
        private int size;

        private ShadowParams() {
            this(-1, -1, -1);
        }

        public ShadowParams(int size2, int mode2, int device2) {
            this.size = size2;
            this.mode = mode2;
            this.device = device2;
        }

        public void set(ShadowParams other) {
            this.size = other.size;
            this.mode = other.mode;
            this.device = other.device;
        }

        public void setSize(String size2) {
            try {
                this.size = Integer.parseInt(size2);
            } catch (NumberFormatException e) {
                Log.e(HwShadowManagerImpl.TAG, "size parseInt failure");
            }
        }

        public void setMode(String mode2) {
            try {
                this.mode = Integer.parseInt(mode2);
            } catch (NumberFormatException e) {
                Log.e(HwShadowManagerImpl.TAG, "mode parseInt failure");
            }
        }

        public void setDevice(String device2) {
            try {
                this.device = Integer.parseInt(device2);
            } catch (NumberFormatException e) {
                Log.e(HwShadowManagerImpl.TAG, "device parseInt failure");
            }
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ShadowParams)) {
                return false;
            }
            ShadowParams other = (ShadowParams) obj;
            if (other.size == this.size && other.mode == this.mode && other.device == this.device) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(Integer.valueOf(this.size), Integer.valueOf(this.mode), Integer.valueOf(this.device));
        }
    }

    public boolean setShadowStyle(RenderNode renderNode, int size, int mode, int device) {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        int uiMode = context.getResources().getConfiguration().uiMode;
        if (mode == -1) {
            if ((uiMode & 48) == 32) {
                mode = 1;
            } else {
                mode = 0;
            }
        }
        ShadowParams shadowParams = new ShadowParams(size, mode, device == -1 ? uiMode & 15 : device);
        if (shadowParams.equals(this.mShadowParams)) {
            renderNode.setShadowEffectParams(this.mShadowEffectParams.elevation, this.mShadowEffectParams.ambientAlpha, this.mShadowEffectParams.spotAlpha, this.mShadowEffectParams.translationY);
            return false;
        }
        ShadowEffectParams shadowEffectParams = getShadowEffectParams(shadowParams);
        this.mShadowParams.set(shadowParams);
        this.mShadowEffectParams.set(shadowEffectParams);
        renderNode.setShadowEffectParams(this.mShadowEffectParams.elevation, this.mShadowEffectParams.ambientAlpha, this.mShadowEffectParams.spotAlpha, this.mShadowEffectParams.translationY);
        return true;
    }

    private ShadowEffectParams getShadowEffectParams(ShadowParams key) {
        ShadowEffectParams effectParams = new ShadowEffectParams();
        for (Map.Entry<ShadowParams, ShadowEffectParams> entry : shadowMap.entrySet()) {
            if (key.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return effectParams;
    }

    private static Map<ShadowParams, ShadowEffectParams> parseShadowEffectConfigXml(Context context) {
        Map<ShadowParams, ShadowEffectParams> configMap = new HashMap<>();
        if (context == null) {
            Log.e(TAG, "context is null.");
            return configMap;
        }
        XmlResourceParser shadowEffectConfigXml = context.getResources().getXml(ID_SHADOW_EFFECT_CONFIG);
        if (shadowEffectConfigXml == null) {
            Log.e(TAG, "find xml resource failed.");
            if (shadowEffectConfigXml != null) {
                shadowEffectConfigXml.close();
            }
            return configMap;
        }
        try {
            int eventType = shadowEffectConfigXml.next();
            while (eventType != 1) {
                if (eventType != 2) {
                    eventType = shadowEffectConfigXml.next();
                } else {
                    int count = shadowEffectConfigXml.getAttributeCount();
                    if (shadowEffectConfigXml.getName().equals(SHADOW_EFFECT_CONFIG)) {
                        ShadowParams shadowParams = new ShadowParams();
                        ShadowEffectParams effectParams = new ShadowEffectParams();
                        for (int i = 0; i < count; i++) {
                            encapsulateEntity(shadowEffectConfigXml.getAttributeName(i), shadowEffectConfigXml.getAttributeValue(i), shadowParams, effectParams);
                        }
                        configMap.put(shadowParams, effectParams);
                    }
                    eventType = shadowEffectConfigXml.next();
                }
            }
            shadowEffectConfigXml.close();
            return configMap;
        } catch (IOException | XmlPullParserException e) {
            Log.e(TAG, "try catch XmlPullParserException or IOException.");
            configMap.clear();
            if (shadowEffectConfigXml != null) {
                shadowEffectConfigXml.close();
            }
            return configMap;
        } catch (Throwable th) {
            if (shadowEffectConfigXml != null) {
                shadowEffectConfigXml.close();
            }
            throw th;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static void encapsulateEntity(String attributeName, String attributeValue, ShadowParams shadowParams, ShadowEffectParams effectParams) {
        char c;
        switch (attributeName.hashCode()) {
            case -1335157162:
                if (attributeName.equals(SHADOW_EFFECT_CONFIG_DEVICE)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1225497656:
                if (attributeName.equals(SHADOW_EFFECT_CONFIG_TRANSLATIONY)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -959571588:
                if (attributeName.equals(SHADOW_EFFECT_CONFIG_SPOTALPHA)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -4379043:
                if (attributeName.equals(SHADOW_EFFECT_CONFIG_ELEVATION)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 3357091:
                if (attributeName.equals(SHADOW_EFFECT_CONFIG_MODE)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 3530753:
                if (attributeName.equals(SHADOW_EFFECT_CONFIG_SIZE)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 609988006:
                if (attributeName.equals(SHADOW_EFFECT_CONFIG_AMBIENTALPHA)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                shadowParams.setSize(attributeValue);
                return;
            case 1:
                shadowParams.setMode(attributeValue);
                return;
            case 2:
                shadowParams.setDevice(attributeValue);
                return;
            case 3:
                effectParams.setElevation(attributeValue);
                return;
            case 4:
                effectParams.setAmbientAlpha(attributeValue);
                return;
            case 5:
                effectParams.setSpotAlpha(attributeValue);
                return;
            case 6:
                effectParams.setTranslationY(attributeValue);
                return;
            default:
                Log.e(TAG, "no case meets the condition.");
                return;
        }
    }
}

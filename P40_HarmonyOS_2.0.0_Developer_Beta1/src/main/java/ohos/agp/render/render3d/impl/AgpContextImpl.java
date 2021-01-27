package ohos.agp.render.render3d.impl;

import java.util.Map;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.render.render3d.BuildConfig;
import ohos.agp.render.render3d.impl.CoreDeviceCreateInfo;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/* access modifiers changed from: package-private */
public final class AgpContextImpl {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "core: AgpContextImpl");
    private CoreDevice mDevice;
    private CoreEngine mEngine;
    private CoreGraphicsContext mGraphicsContext = CoreGraphicsContext.create(this.mEngine);
    private CorePlatform mPlatform;

    /* access modifiers changed from: package-private */
    public void activateGlContext() {
    }

    /* access modifiers changed from: package-private */
    public void deactivateGlContext() {
    }

    private AgpContextImpl(Context context, String str, int i, int i2, int i3) {
        this.mPlatform = Core.createAndroidPlatform(context);
        this.mEngine = Core.createEngine(new CoreEngineCreateInfo(this.mPlatform, new CoreVersionInfo(str, i, i2, i3), new CoreContextInfo()));
    }

    static AgpContextImpl createAgpContext(Context context, String str, int i, int i2, int i3) {
        return new AgpContextImpl(context, str, i, i2, i3);
    }

    /* access modifiers changed from: package-private */
    public void init() {
        this.mEngine.init();
        this.mGraphicsContext.init();
    }

    /* access modifiers changed from: package-private */
    public int createTextureOes() {
        activateGlContext();
        deactivateGlContext();
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void deleteTextureOes(int i) {
        if (i != 0) {
            activateGlContext();
            deactivateGlContext();
        }
    }

    private int getExtraInt(Map<String, Object> map, String str, int i) {
        if (map == null) {
            return i;
        }
        Object orDefault = map.getOrDefault(str, Integer.valueOf(i));
        if (orDefault instanceof Integer) {
            return ((Integer) orDefault).intValue();
        }
        throw new IllegalArgumentException("Backend extra '" + str + "': Integer expected, got " + orDefault.getClass().toString());
    }

    /* access modifiers changed from: package-private */
    public void initDevice(CoreDeviceCreateInfo.CoreBackend coreBackend, Map<String, Object> map) {
        this.mDevice = Core.createDevice(this.mEngine, coreBackend, 0, 0, getExtraInt(map, "MSAA_SAMPLE_COUNT", 0), getExtraInt(map, "DEPTH_BUFFER_BITS", 24), getExtraInt(map, "ALPHA_BITS", 0), getExtraInt(map, "STENCIL_BITS", 0));
    }

    /* access modifiers changed from: package-private */
    public void release() {
        if (BuildConfig.DEBUG) {
            HiLog.debug(LABEL, "release()", new Object[0]);
        }
        CoreGraphicsContext coreGraphicsContext = this.mGraphicsContext;
        if (coreGraphicsContext != null) {
            CoreGraphicsContext.destroy(coreGraphicsContext);
            this.mGraphicsContext = null;
        }
        CoreEngine coreEngine = this.mEngine;
        if (coreEngine != null) {
            coreEngine.delete();
            this.mEngine = null;
        }
        CorePlatform corePlatform = this.mPlatform;
        if (corePlatform != null) {
            corePlatform.delete();
            this.mPlatform = null;
        }
    }

    /* access modifiers changed from: package-private */
    public CorePlatform getPlatform() {
        return this.mPlatform;
    }

    /* access modifiers changed from: package-private */
    public CoreEngine getEngine() {
        return this.mEngine;
    }

    /* access modifiers changed from: package-private */
    public CoreDevice getDevice() {
        return this.mDevice;
    }

    /* access modifiers changed from: package-private */
    public CoreGraphicsContext getGraphicsContext() {
        return this.mGraphicsContext;
    }
}

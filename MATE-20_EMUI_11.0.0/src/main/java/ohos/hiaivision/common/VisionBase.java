package ohos.hiaivision.common;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import ohos.abilityshell.utils.AbilityContextUtils;
import ohos.ai.cv.common.ConnectionCallback;
import ohos.ai.cv.common.ImageResult;
import ohos.ai.cv.common.VisionCallback;
import ohos.ai.cv.common.VisionConfiguration;
import ohos.ai.cv.common.VisionImage;
import ohos.ai.engine.pluginservice.ILoadPluginCallback;
import ohos.ai.engine.pluginservice.IPluginService;
import ohos.ai.engine.pluginservice.PluginRequest;
import ohos.ai.engine.utils.HiAILog;
import ohos.hiaivision.AiRuntimeException;
import ohos.hiaivision.common.IHiAIVisionCallback;
import ohos.hiaivision.common.IHiAIVisionEngine;
import ohos.hiaivision.common.Reflect;
import ohos.hiaivision.internal.HwVisionManagerPlugin;
import ohos.hiaivision.visionutil.common.BitmapUtils;
import ohos.media.image.PixelMap;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.nfc.tag.NdefTag;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public abstract class VisionBase {
    protected static final float INIT_SCALE = 1.0f;
    protected static final int MAX_DETECT_TIME = 5000;
    protected static final int PIXEL_LIMIT = 20000000;
    private static final String TAG = "VisionBase";
    protected Bundle ability;
    private Context context;
    protected IHiAIVisionEngine engine;
    protected boolean isPrepared;
    protected Reflect reflect;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;

    /* access modifiers changed from: protected */
    public abstract int getApiId();

    /* access modifiers changed from: protected */
    public abstract VisionConfiguration getConfiguration();

    /* access modifiers changed from: protected */
    public int getPixelLimit() {
        return PIXEL_LIMIT;
    }

    protected VisionBase(ohos.app.Context context2) throws ClassCastException {
        Object androidContext = AbilityContextUtils.getAndroidContext(context2);
        if (androidContext instanceof Context) {
            this.context = (Context) androidContext;
        } else {
            HiAILog.error(TAG, "Failed to cast context!");
            throw new ClassCastException("Failed to cast context!");
        }
    }

    public static int init(ohos.app.Context context2, ConnectionCallback connectionCallback) {
        if (context2 != null) {
            return HwVisionManagerPlugin.getInstance().init(context2, connectionCallback);
        }
        throw new AiRuntimeException(201);
    }

    public int prepare() {
        int i;
        if (this.isPrepared) {
            return 0;
        }
        VisionConfiguration configuration = getConfiguration();
        if (configuration == null) {
            HiAILog.error(TAG, "visionConfiguration is null when preparing");
            return -1;
        }
        if (configuration.getProcessMode() == 1) {
            HiAILog.debug(TAG, "out mode prepare");
            i = prepareOutMode();
        } else {
            HiAILog.debug(TAG, "in mode prepare");
            i = prepareInMode();
        }
        if (i == 0) {
            this.isPrepared = true;
        }
        return i;
    }

    public int getAvailability() {
        IPluginService pluginService = getPluginService();
        if (pluginService == null) {
            HiAILog.error(TAG, "getAvailability, pluginService is null");
            return 521;
        }
        try {
            if (!pluginService.isOpen(getApiId()) && Build.VERSION.SDK_INT >= 23 && this.context.checkSelfPermission("com.huawei.hiai.permission.HIAIENGINE_START_COMPONENT") != 0) {
                return -2;
            }
            try {
                return pluginService.checkPluginInstalled(getPluginRequest());
            } catch (RemoteException e) {
                HiAILog.error(TAG, "check pluginInstalled RemoteException: " + e.getMessage());
                return -1;
            }
        } catch (RemoteException e2) {
            HiAILog.error(TAG, "getAvailability, RemoteException: " + e2.getMessage());
            return -1;
        }
    }

    public void loadPlugin(ILoadPluginCallback iLoadPluginCallback) {
        try {
            IPluginService pluginService = HwVisionManagerPlugin.getInstance().getPluginService();
            if (pluginService == null) {
                if (iLoadPluginCallback != null) {
                    iLoadPluginCallback.onResult(521);
                }
                HiAILog.error(TAG, "loadPlugin, pluginService is null");
                return;
            }
            pluginService.startInstallPlugin(getPluginRequest(), this.context.getPackageName(), iLoadPluginCallback);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "startLoadPlugin error");
        }
    }

    public int release() {
        int i;
        VisionConfiguration configuration = getConfiguration();
        if (configuration == null) {
            HiAILog.error(TAG, "get visionConfiguration is null");
            return -1;
        }
        if (configuration.getProcessMode() == 0) {
            Reflect reflect2 = this.reflect;
            if (reflect2 == null) {
                return -1;
            }
            try {
                i = ((Integer) reflect2.call("release", new Class[0]).invoke(new Object[0])).intValue();
            } catch (ReflectiveOperationException e) {
                HiAILog.error(TAG, "mix-built release error" + e.getMessage());
                return -1;
            }
        } else {
            IHiAIVisionEngine iHiAIVisionEngine = this.engine;
            if (iHiAIVisionEngine == null) {
                return -1;
            }
            try {
                i = iHiAIVisionEngine.release();
            } catch (android.os.RemoteException e2) {
                HiAILog.error(TAG, "out-built release error" + e2.getMessage());
                return -1;
            }
        }
        this.isPrepared = false;
        return i;
    }

    public static void destroy() {
        HwVisionManagerPlugin.getInstance().destroy();
    }

    /* access modifiers changed from: protected */
    public Optional<ClassLoader> getRemoteClassLoader(int i) {
        return HwVisionManagerPlugin.getInstance().getRemoteClassLoader(i);
    }

    /* access modifiers changed from: protected */
    public List<PluginRequest> getPluginRequest() {
        PluginRequest pluginRequest = new PluginRequest(getApiId());
        ArrayList arrayList = new ArrayList(1);
        arrayList.add(pluginRequest);
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public IPluginService getPluginService() {
        return HwVisionManagerPlugin.getInstance().getPluginService();
    }

    /* access modifiers changed from: protected */
    public IRemoteObject getHostService() {
        return HwVisionManagerPlugin.getInstance().getHostService();
    }

    /* access modifiers changed from: protected */
    public Context getRemoteContext(int i) {
        try {
            Context remoteContext = HwVisionManagerPlugin.getInstance().getRemoteContext();
            if (remoteContext == null) {
                HiAILog.error(TAG, "remoteContext is null");
                return null;
            }
            IPluginService pluginService = getPluginService();
            if (pluginService != null) {
                return remoteContext.createContextForSplit(pluginService.getPluginName(i));
            }
            HiAILog.error(TAG, "getRemoteContext, pluginService is null");
            return null;
        } catch (PackageManager.NameNotFoundException | RemoteException e) {
            HiAILog.error(TAG, "get split context error" + e.toString());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public Optional<Bitmap> getTargetBitmap(PixelMap pixelMap) {
        if (pixelMap == null) {
            return Optional.empty();
        }
        if (this.ability == null) {
            return Optional.ofNullable(ImageDoubleFwConverter.createShadowBitmap(pixelMap));
        }
        Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap(pixelMap);
        if (createShadowBitmap == null) {
            return Optional.empty();
        }
        int height = createShadowBitmap.getHeight();
        int width = createShadowBitmap.getWidth();
        if (height <= 0 || width <= 0) {
            HiAILog.error(TAG, "bitmap obtained from PixelMap is invalid");
            return Optional.empty();
        }
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        int i = this.ability.getInt("fix_height", 0);
        int i2 = this.ability.getInt("fix_width", 0);
        if (i <= 0 || i2 <= 0) {
            int i3 = this.ability.getInt("max_height", 0);
            int i4 = this.ability.getInt("max_width", 0);
            if ((i3 <= 0 && i4 <= 0) || (height <= i3 && width <= i4)) {
                return Optional.of(createShadowBitmap);
            }
            this.scaleX = ((float) i4) / ((float) width);
            this.scaleY = ((float) i3) / ((float) height);
            float f = this.scaleX;
            float f2 = this.scaleY;
            if (f > f2) {
                this.scaleX = f2;
            } else {
                this.scaleY = f;
            }
            int width2 = (int) (((float) createShadowBitmap.getWidth()) * this.scaleX);
            if (width2 <= 0) {
                width2 = 1;
            }
            int height2 = (int) (((float) createShadowBitmap.getHeight()) * this.scaleY);
            if (height2 <= 0) {
                height2 = 1;
            }
            return BitmapUtils.resizeBitmap(createShadowBitmap, width2, height2);
        }
        this.scaleX = ((float) i2) / ((float) width);
        this.scaleY = ((float) i) / ((float) height);
        return BitmapUtils.resizeBitmap(createShadowBitmap, i2, i);
    }

    /* access modifiers changed from: protected */
    public float getScaleX() {
        return this.scaleX;
    }

    /* access modifiers changed from: protected */
    public float getScaleY() {
        return this.scaleY;
    }

    /* access modifiers changed from: protected */
    public int waitForResult(boolean z, Lock lock, Condition condition) {
        int i;
        if (z) {
            return 700;
        }
        lock.lock();
        try {
            if (!condition.await(5000, TimeUnit.MILLISECONDS)) {
                HiAILog.error(TAG, "time out for running");
                i = NdefTag.TYPE_ICODE_SLI;
                lock.unlock();
                return i;
            }
            lock.unlock();
            return 0;
        } catch (InterruptedException unused) {
            HiAILog.error(TAG, "thread interrupted");
            i = NdefTag.TYPE_MIFARE_CLASSIC;
        } catch (Throwable th) {
            lock.unlock();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void getAsyncResult(Bundle bundle, int i, IHiAIVisionCallback iHiAIVisionCallback) {
        if (i == 1) {
            HiAILog.debug(TAG, "out mode detect");
            try {
                this.engine.run(bundle, iHiAIVisionCallback);
            } catch (android.os.RemoteException unused) {
                HiAILog.error(TAG, "out-built run error");
                try {
                    iHiAIVisionCallback.onError(-1);
                } catch (android.os.RemoteException unused2) {
                    HiAILog.error(TAG, "RemoteException error when call onError");
                }
            }
        } else {
            HiAILog.debug(TAG, "in mode detect");
            try {
                this.reflect.call("run", Bundle.class, Object.class).invoke(bundle, iHiAIVisionCallback);
            } catch (ReflectiveOperationException unused3) {
                HiAILog.error(TAG, "mix-built run error");
                try {
                    iHiAIVisionCallback.onError(-1);
                } catch (android.os.RemoteException unused4) {
                    HiAILog.error(TAG, "RemoteException error when call onError");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public IHiAIVisionCallback createVisionCallback(final ImageResult imageResult, final VisionCallback<ImageResult> visionCallback, final Lock lock, final Condition condition, final int[] iArr) {
        final boolean z = visionCallback != null;
        return new IHiAIVisionCallback.Stub() {
            /* class ohos.hiaivision.common.VisionBase.AnonymousClass1 */

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onInfo(Bundle bundle) throws android.os.RemoteException {
            }

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onResult(Bundle bundle) throws android.os.RemoteException {
                HiAILog.debug(VisionBase.TAG, "onResult");
                if (bundle != null) {
                    imageResult.setPixelMap(ImageDoubleFwConverter.createShellPixelMap((Bitmap) bundle.getParcelable("bitmap_output")));
                    iArr[0] = bundle.getInt("result_code");
                } else {
                    HiAILog.error(VisionBase.TAG, "input bundle is null");
                    iArr[0] = 101;
                }
                if (z) {
                    visionCallback.onResult(imageResult);
                } else {
                    VisionBase.this.signalLockForSyncMode(lock, condition);
                }
            }

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onError(int i) throws android.os.RemoteException {
                HiAILog.debug(VisionBase.TAG, "onError");
                imageResult.setPixelMap((PixelMap) null);
                if (z) {
                    visionCallback.onError(i);
                    return;
                }
                iArr[0] = i;
                VisionBase.this.signalLockForSyncMode(lock, condition);
            }
        };
    }

    /* access modifiers changed from: protected */
    public void signalLockForSyncMode(Lock lock, Condition condition) {
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isInputIllegal(VisionImage visionImage, Object obj, VisionCallback<?> visionCallback) {
        if (visionImage == null || visionImage.getPixelMap() == null) {
            return true;
        }
        return visionCallback == null && obj == null;
    }

    /* access modifiers changed from: protected */
    public void handleErrorCode(boolean z, VisionCallback<?> visionCallback, int i) {
        if (z) {
            visionCallback.onError(i);
        }
    }

    private int prepareOutMode() {
        try {
            IPluginService pluginService = getPluginService();
            if (pluginService == null) {
                HiAILog.error(TAG, "prepareOutMode, pluginService is null");
                return 521;
            }
            Optional<Object> translateToIBinder = IPCAdapter.translateToIBinder(pluginService.getSplitRemoteObject(getApiId()));
            if (!translateToIBinder.isPresent()) {
                HiAILog.error(TAG, "get engine iBinder is null");
                return 601;
            }
            if (translateToIBinder.get() instanceof IBinder) {
                this.engine = IHiAIVisionEngine.Stub.asInterface((IBinder) translateToIBinder.get());
            }
            if (this.engine == null) {
                HiAILog.error(TAG, "get engine asInterface is null");
                return 500;
            }
            int i = -1;
            try {
                i = this.engine.prepare();
                this.ability = this.engine.getAbility();
                return i;
            } catch (android.os.RemoteException e) {
                HiAILog.error(TAG, "out-built prepare error" + e.getMessage());
                return i;
            }
        } catch (RemoteException e2) {
            HiAILog.error(TAG, "out-built init and prepare error" + e2.getMessage());
        }
    }

    private int prepareInMode() {
        try {
            IPluginService pluginService = getPluginService();
            if (pluginService == null) {
                HiAILog.error(TAG, "pluginService is null");
                return 521;
            }
            ClassLoader orElse = getRemoteClassLoader(getApiId()).orElse(null);
            if (orElse == null) {
                HiAILog.error(TAG, "get engine model is null");
                return 601;
            }
            String splitRemoteObjectName = pluginService.getSplitRemoteObjectName(getApiId());
            if (splitRemoteObjectName == null) {
                HiAILog.error(TAG, "splitBinderName is null");
                return -1;
            }
            HiAILog.debug(TAG, "getRemoteInstance");
            this.reflect = Reflect.Builder.on(splitRemoteObjectName, orElse).create(new Object[0]);
            if (this.reflect == null) {
                HiAILog.error(TAG, "reflect is null");
                return -1;
            }
            int initFromReflection = initFromReflection();
            if (initFromReflection != 0) {
                return initFromReflection;
            }
            try {
                int intValue = ((Integer) this.reflect.call("prepare", new Class[0]).invoke(new Object[0])).intValue();
                int abilityFromReflection = getAbilityFromReflection();
                return abilityFromReflection != 0 ? abilityFromReflection : intValue;
            } catch (ReflectiveOperationException e) {
                HiAILog.error(TAG, "mix-built prepare error" + e.getMessage());
                return -1;
            }
        } catch (RemoteException e2) {
            HiAILog.error(TAG, "mix-built create RemoteException error" + e2.getMessage());
            return -1;
        } catch (ReflectiveOperationException e3) {
            HiAILog.error(TAG, "mix-built create ReflectiveOperationException error" + e3.getMessage());
            return -1;
        }
    }

    private int initFromReflection() {
        try {
            Context remoteContext = getRemoteContext(getApiId());
            Object orElse = IPCAdapter.translateToIBinder(getHostService()).orElse(null);
            if (remoteContext != null) {
                if (orElse instanceof IBinder) {
                    return ((Integer) this.reflect.call("init", IBinder.class, Context.class).invoke((IBinder) orElse, remoteContext)).intValue();
                }
            }
            HiAILog.error(TAG, "remote context or host service is null");
            return 201;
        } catch (ReflectiveOperationException e) {
            HiAILog.error(TAG, "mix-built init error" + e.getMessage());
            return -1;
        }
    }

    private int getAbilityFromReflection() {
        try {
            Object invoke = this.reflect.call("getAbility", new Class[0]).invoke(new Object[0]);
            if (invoke instanceof Bundle) {
                this.ability = (Bundle) invoke;
                return 0;
            }
            HiAILog.error(TAG, "object reflected from getAbility() method is not of Bundle type");
            return NdefTag.TYPE_MIFARE_CLASSIC;
        } catch (ReflectiveOperationException e) {
            HiAILog.error(TAG, "Failed to get ability in MODE_IN mode, error: " + e.getMessage());
            return -1;
        }
    }
}

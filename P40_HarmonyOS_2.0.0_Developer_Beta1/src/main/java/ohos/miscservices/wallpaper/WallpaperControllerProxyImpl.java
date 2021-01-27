package ohos.miscservices.wallpaper;

import android.app.IWallpaperManager;
import android.app.IWallpaperManagerCallback;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.ServiceManager;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.utils.Rect;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.miscservices.wallpaper.WallpaperControllerProxyImpl;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;
import ohos.utils.adapter.PacMapUtils;

public class WallpaperControllerProxyImpl {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "WallpaperControllerProxyImpl");
    private static final String WALLPAPER_SERVICE = "wallpaper";
    private IWallpaperManagerCallback.Stub mAdapterCallback = new IWallpaperManagerCallback.Stub() {
        /* class ohos.miscservices.wallpaper.WallpaperControllerProxyImpl.AnonymousClass1 */

        public void onBlurWallpaperChanged() {
        }

        public void onWallpaperChanged() {
        }

        public void onWallpaperColorsChanged(WallpaperColors wallpaperColors, int i, int i2) {
            if (WallpaperControllerProxyImpl.this.mContext == null) {
                HiLog.error(WallpaperControllerProxyImpl.TAG, "WallpaperColorsChanged:Context is null!", new Object[0]);
            } else if (wallpaperColors == null) {
                HiLog.error(WallpaperControllerProxyImpl.TAG, "WallpaperColorsChanged:colors is null!", new Object[0]);
            } else {
                TaskDispatcher mainTaskDispatcher = WallpaperControllerProxyImpl.this.mContext.getMainTaskDispatcher();
                if (mainTaskDispatcher == null) {
                    HiLog.error(WallpaperControllerProxyImpl.TAG, "WallpaperColorsChanged:dispatcher is null!", new Object[0]);
                } else {
                    mainTaskDispatcher.asyncDispatch(new Runnable(wallpaperColors, i) {
                        /* class ohos.miscservices.wallpaper.$$Lambda$WallpaperControllerProxyImpl$1$UkpJdL0FZ3BOv7BiruMGXKfXthM */
                        private final /* synthetic */ WallpaperColors f$1;
                        private final /* synthetic */ int f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            WallpaperControllerProxyImpl.AnonymousClass1.this.lambda$onWallpaperColorsChanged$0$WallpaperControllerProxyImpl$1(this.f$1, this.f$2);
                        }
                    });
                }
            }
        }

        public /* synthetic */ void lambda$onWallpaperColorsChanged$0$WallpaperControllerProxyImpl$1(WallpaperColors wallpaperColors, int i) {
            synchronized (this) {
                if (WallpaperControllerProxyImpl.this.mIWallpaperColorsChangedCallback != null) {
                    WallpaperControllerProxyImpl.this.mIWallpaperColorsChangedCallback.notifyWallpaperColorsChanged(WallpaperUtils.convertToWallpaperColorsCollection(wallpaperColors), i);
                }
            }
        }
    };
    private Context mAospContext;
    private ohos.app.Context mContext;
    private OnWallpaperColorsChangedCallback mIWallpaperColorsChangedCallback;
    private IWallpaperManager mService;
    private WallpaperManager mWallpaperManager;

    /* access modifiers changed from: protected */
    public interface OnWallpaperColorsChangedCallback {
        void notifyWallpaperColorsChanged(WallpaperColorsCollection wallpaperColorsCollection, int i);
    }

    WallpaperControllerProxyImpl(ohos.app.Context context) {
        this.mContext = context;
        tryInit();
    }

    public void registerColorsChangedListener() throws RemoteException {
        if (this.mService != null || tryInit()) {
            try {
                this.mService.registerWallpaperColorsCallback(this.mAdapterCallback, this.mAospContext.getUserId(), this.mAospContext.getDisplayId());
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "registerColorsChangedListener:register colors callback failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "registerColorsChangedListener:Get service instance failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public void unregisterColorsChangedListener() throws RemoteException {
        if (this.mService != null || tryInit()) {
            try {
                this.mService.unregisterWallpaperColorsCallback(this.mAdapterCallback, this.mAospContext.getUserId(), this.mAospContext.getDisplayId());
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "unregisterColorsChangedListener:unregister colors callback failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "unregisterColorsChangedListener:Get service instance failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    /* access modifiers changed from: protected */
    public void setWallpaperColorsChangedCallback(OnWallpaperColorsChangedCallback onWallpaperColorsChangedCallback) {
        synchronized (this) {
            if (this.mIWallpaperColorsChangedCallback == null) {
                HiLog.info(TAG, "setWallpaperColorsChangedCallback:set colors changed callback", new Object[0]);
                this.mIWallpaperColorsChangedCallback = onWallpaperColorsChangedCallback;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeWallpaperColorsChangedCallback() {
        synchronized (this) {
            this.mIWallpaperColorsChangedCallback = null;
        }
    }

    private boolean tryInit() {
        this.mAospContext = WallpaperUtils.convertFromHospContext(this.mContext);
        Context context = this.mAospContext;
        if (context == null) {
            HiLog.error(TAG, "tryInit:convert mContext failed!", new Object[0]);
            return false;
        }
        this.mWallpaperManager = WallpaperManager.getInstance(context);
        this.mService = IWallpaperManager.Stub.asInterface(ServiceManager.getService(WALLPAPER_SERVICE));
        if (this.mWallpaperManager == null || this.mService == null) {
            return false;
        }
        return true;
    }

    public int getMinHeight() throws RemoteException {
        if (this.mService != null || tryInit()) {
            try {
                return this.mService.getHeightHint(this.mAospContext.getDisplayId());
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "getMinHeight:get desired height failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "getMinHeight:Get wallpaper service failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public int getMinWidth() throws RemoteException {
        if (this.mService != null || tryInit()) {
            try {
                return this.mService.getWidthHint(this.mAospContext.getDisplayId());
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "getMinWidth:get desired width failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "getMinWidth:Get wallpaper service failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public boolean isChangePermitted() throws RemoteException {
        if (this.mService != null || tryInit()) {
            try {
                return this.mService.isSetWallpaperAllowed(this.mAospContext.getOpPackageName());
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "isSetWallpaperAllowed:is setting wallpaper allowed called failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "isSetWallpaperAllowed:Get wallpaper service failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public boolean isOperationAllowed() throws RemoteException {
        if (this.mService != null || tryInit()) {
            try {
                return this.mService.isWallpaperSupported(this.mAospContext.getOpPackageName());
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "isOperationAllowed:is wallpaper supported called failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "isOperationAllowed:Get wallpaper service failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public int getId(int i) {
        if (this.mService != null || tryInit()) {
            try {
                return this.mService.getWallpaperIdForUser(i, this.mAospContext.getUserId());
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "getId:get wallpaper id failed!", new Object[0]);
                return -1;
            }
        } else {
            HiLog.error(TAG, "getId:Get wallpaper service failed!", new Object[0]);
            return -1;
        }
    }

    public void suggestDesiredWallpaperDimensions(int i, int i2) throws RemoteException {
        if (this.mWallpaperManager != null || tryInit()) {
            try {
                this.mWallpaperManager.suggestDesiredDimensions(i, i2);
            } catch (RuntimeException unused) {
                HiLog.error(TAG, "suggestDesiredWallpaperDimensions:suggest desired dimensions failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "suggestDesiredWallpaperDimensions:Get wallpaper controller instance failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public void setWallpaperOffsetSteps(float f, float f2) {
        if (this.mWallpaperManager != null || tryInit()) {
            this.mWallpaperManager.setWallpaperOffsetSteps(f, f2);
        } else {
            HiLog.error(TAG, "setWallpaperOffsetSteps:Get wallpaper controller instance failed!", new Object[0]);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:53:0x00be A[SYNTHETIC, Splitter:B:53:0x00be] */
    public PixelMap getPixelmap(int i) {
        FileDescriptor fileDescriptor;
        Throwable th;
        InputStream inputStream;
        try {
            fileDescriptor = getFile(i);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "getPixelmap:getWallpaperFile failed!", new Object[0]);
            fileDescriptor = null;
        }
        if (fileDescriptor != null) {
            ImageSource create = ImageSource.create(fileDescriptor, new ImageSource.SourceOptions());
            if (create == null) {
                HiLog.error(TAG, "getPixelmap:get create image source failed!", new Object[0]);
                return null;
            }
            PixelMap createPixelmap = create.createPixelmap(new ImageSource.DecodingOptions());
            if (createPixelmap != null) {
                return createPixelmap;
            }
            HiLog.error(TAG, "getPixelmap:create pixelmap failed!", new Object[0]);
            return null;
        }
        try {
            InputStream defaultWallpaperIS = HwThemeManager.getDefaultWallpaperIS(this.mAospContext, i);
            if (defaultWallpaperIS == null) {
                try {
                    HiLog.info(TAG, "getPixelmap:get no wallpaper from theme manager!", new Object[0]);
                    inputStream = WallpaperManager.openDefaultWallpaper(this.mAospContext, i);
                } catch (Throwable th2) {
                    th = th2;
                    inputStream = defaultWallpaperIS;
                }
            } else {
                inputStream = defaultWallpaperIS;
            }
            if (inputStream == null) {
                try {
                    HiLog.error(TAG, "getPixelmap:defaultWallpaper is null!", new Object[0]);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException unused2) {
                            HiLog.error(TAG, "defaultWallpaper close error", new Object[0]);
                        }
                    }
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException unused3) {
                            HiLog.error(TAG, "defaultWallpaper close error", new Object[0]);
                        }
                    }
                    throw th;
                }
            } else {
                ImageSource create2 = ImageSource.create(inputStream, new ImageSource.SourceOptions());
                if (create2 == null) {
                    HiLog.error(TAG, "getPixelmap:get create default image source failed!", new Object[0]);
                    try {
                        inputStream.close();
                    } catch (IOException unused4) {
                        HiLog.error(TAG, "defaultWallpaper close error", new Object[0]);
                    }
                    return null;
                }
                try {
                    inputStream.close();
                } catch (IOException unused5) {
                    HiLog.error(TAG, "defaultWallpaper close error", new Object[0]);
                }
                PixelMap createPixelmap2 = create2.createPixelmap(new ImageSource.DecodingOptions());
                if (createPixelmap2 != null) {
                    return createPixelmap2;
                }
                HiLog.error(TAG, "getPixelmap:create default pixelmap failed!", new Object[0]);
                return null;
            }
        } catch (Throwable th4) {
            th = th4;
            inputStream = null;
            if (inputStream != null) {
            }
            throw th;
        }
    }

    public void setWallpaper(PixelMap pixelMap, int i) throws RemoteException {
        if (this.mWallpaperManager == null && !tryInit()) {
            HiLog.error(TAG, "setWallpaper-PixelMap:Get wallpaper controller instance failed!", new Object[0]);
            throw new RemoteException();
        } else if (pixelMap != null) {
            Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap(pixelMap);
            if (createShadowBitmap != null) {
                try {
                    this.mWallpaperManager.setBitmap(createShadowBitmap, null, true, i, this.mAospContext.getUserId());
                } catch (IOException unused) {
                    HiLog.error(TAG, "setWallpaper-PixelMap:set pixelMap failed!", new Object[0]);
                    throw new RemoteException();
                }
            } else {
                HiLog.error(TAG, "setWallpaper-PixelMap:convert pixelMap failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "setWallpaper-PixelMap:pixelMap is null!", new Object[0]);
            throw new RemoteException();
        }
    }

    public void setWallpaper(InputStream inputStream, int i) throws RemoteException {
        if (this.mWallpaperManager == null && !tryInit()) {
            HiLog.error(TAG, "setWallpaper-InputStream:Get wallpaper controller instance failed!", new Object[0]);
            throw new RemoteException();
        } else if (inputStream != null) {
            try {
                this.mWallpaperManager.setStream(inputStream, null, true, i);
            } catch (IOException unused) {
                HiLog.error(TAG, "setWallpaper-InputStream:set file input stream failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "setWallpaper-InputStream:inputStream is null!", new Object[0]);
            throw new RemoteException();
        }
    }

    public void setWallpaperWithOffsets(PixelMap pixelMap, int[] iArr) throws RemoteException {
        if (this.mWallpaperManager == null && !tryInit()) {
            HiLog.error(TAG, "setWallpaperWithOffsets-PixelMap:Get wallpaper controller instance failed!", new Object[0]);
            throw new RemoteException();
        } else if (pixelMap == null || iArr == null) {
            HiLog.error(TAG, "setWallpaperWithOffsets-PixelMap:pixelMap or offsets is null!", new Object[0]);
            throw new RemoteException();
        } else {
            Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap(pixelMap);
            if (createShadowBitmap != null) {
                try {
                    this.mWallpaperManager.setBitmapWithOffsets(createShadowBitmap, iArr);
                } catch (IOException unused) {
                    HiLog.error(TAG, "setWallpaperWithOffsets-PixelMap:set pixelMap with offsets failed!", new Object[0]);
                    throw new RemoteException();
                }
            } else {
                HiLog.error(TAG, "setWallpaperWithOffsets-PixelMap:convert pixelMap failed!", new Object[0]);
                throw new RemoteException();
            }
        }
    }

    public void setWallpaperWithOffsets(InputStream inputStream, int[] iArr) throws RemoteException {
        if (this.mWallpaperManager == null && !tryInit()) {
            HiLog.error(TAG, "setWallpaperWithOffsets-InputStream:Get wallpaper controller instance failed!", new Object[0]);
            throw new RemoteException();
        } else if (inputStream == null || iArr == null) {
            HiLog.error(TAG, "setWallpaperWithOffsets-InputStream:inputStream or offsets is null!", new Object[0]);
            throw new RemoteException();
        } else {
            try {
                this.mWallpaperManager.setStreamWithOffsets(inputStream, iArr);
            } catch (IOException unused) {
                HiLog.error(TAG, "setWallpaperWithOffsets-InputStream:set file input stream with offsets failed!", new Object[0]);
                throw new RemoteException();
            }
        }
    }

    public FileDescriptor getFile(int i) throws RemoteException {
        if (this.mService != null || tryInit()) {
            try {
                ParcelFileDescriptor wallpaper = this.mService.getWallpaper(this.mAospContext.getOpPackageName(), (IWallpaperManagerCallback) null, i, new Bundle(), this.mAospContext.getUserId());
                if (wallpaper != null) {
                    FileDescriptor fileDescriptor = wallpaper.getFileDescriptor();
                    if (fileDescriptor != null) {
                        return fileDescriptor;
                    }
                    HiLog.error(TAG, "getFile:fileDescriptor is null!", new Object[0]);
                    throw new RemoteException();
                }
                HiLog.error(TAG, "getFile:parcelFileDescriptor is null!", new Object[0]);
                throw new RemoteException();
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "getFile:get wallpaper file failed! Call remote failed!", new Object[0]);
                throw new RemoteException();
            } catch (SecurityException unused2) {
                HiLog.error(TAG, "getFile:get wallpaper file failed! No permission to access wallpaper!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "getFile:Get wallpaper service failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public void reset(int i) throws RemoteException {
        if (this.mWallpaperManager != null || tryInit()) {
            try {
                this.mWallpaperManager.clearWallpaper(i, this.mAospContext.getUserId());
            } catch (RuntimeException unused) {
                HiLog.error(TAG, "reset:reset wallpaper failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "reset:Get wallpaper controller instance failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public void setPadding(Rect rect) throws RemoteException {
        if (this.mService == null && !tryInit()) {
            HiLog.error(TAG, "setPadding:Get wallpaper service failed!", new Object[0]);
            throw new RemoteException();
        } else if (rect != null) {
            try {
                this.mService.setDisplayPadding(new android.graphics.Rect(rect.left, rect.top, rect.right, rect.bottom), this.mAospContext.getOpPackageName(), this.mAospContext.getDisplayId());
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "setPadding:set display padding failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "setPadding:padding is null!", new Object[0]);
            throw new RemoteException();
        }
    }

    public int[] getStartingPoints() {
        int[] iArr;
        if (this.mService != null || tryInit()) {
            int[] iArr2 = {-1, -1, -1, -1};
            try {
                iArr = this.mService.getCurrOffsets();
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "getStartingPoints:get starting points failed!", new Object[0]);
                iArr = null;
            }
            return iArr == null ? iArr2 : iArr;
        }
        HiLog.error(TAG, "getStartingPoints:Get wallpaper service failed!", new Object[0]);
        return new int[0];
    }

    public PacMap getUserWallpaperBounds() {
        if (this.mWallpaperManager != null || tryInit()) {
            Bundle userWallpaperBounds = this.mWallpaperManager.getUserWallpaperBounds(this.mAospContext);
            if (userWallpaperBounds != null) {
                return PacMapUtils.convertFromBundle(userWallpaperBounds);
            }
            HiLog.error(TAG, "getUserWallpaperBounds:get user wallpaper pacMap failed!", new Object[0]);
            return null;
        }
        HiLog.error(TAG, "getUserWallpaperBounds:Get wallpaper controller instance failed!", new Object[0]);
        return null;
    }

    public WallpaperColorsCollection getColors(int i) throws RemoteException {
        if (this.mService != null || tryInit()) {
            try {
                WallpaperColors wallpaperColors = this.mService.getWallpaperColors(i, this.mAospContext.getUserId(), this.mAospContext.getDisplayId());
                if (wallpaperColors != null) {
                    return WallpaperUtils.convertToWallpaperColorsCollection(wallpaperColors);
                }
                HiLog.error(TAG, "getColors:WallpaperColors is null!", new Object[0]);
                throw new RemoteException();
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "getColors:get wallpaper colors failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "getColors:Get wallpaper service failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public void setWallpaperOffsets(IRemoteObject iRemoteObject, float f, float f2) throws RemoteException {
        if (this.mWallpaperManager != null || tryInit()) {
            Optional<Object> translateToIBinder = IPCAdapter.translateToIBinder(iRemoteObject);
            if (!translateToIBinder.isPresent() || !(translateToIBinder.get() instanceof IBinder)) {
                HiLog.error(TAG, "setWallpaperOffsets failed!", new Object[0]);
            } else {
                this.mWallpaperManager.setWallpaperOffsets((IBinder) translateToIBinder.get(), f, f2);
            }
        } else {
            HiLog.error(TAG, "setWallpaperOffsets:Get wallpaper controller instance failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public void clearWallpaperOffsets(IRemoteObject iRemoteObject) throws RemoteException {
        if (this.mWallpaperManager != null || tryInit()) {
            Optional<Object> translateToIBinder = IPCAdapter.translateToIBinder(iRemoteObject);
            if (!translateToIBinder.isPresent() || !(translateToIBinder.get() instanceof IBinder)) {
                HiLog.error(TAG, "clearWallpaperOffsets failed!", new Object[0]);
            } else {
                this.mWallpaperManager.clearWallpaperOffsets((IBinder) translateToIBinder.get());
            }
        } else {
            HiLog.error(TAG, "clearWallpaperOffsets:Get wallpaper controller instance failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public void sendWallpaperCommand(IRemoteObject iRemoteObject, String str, int i, int i2, int i3, PacMap pacMap) throws RemoteException {
        if (this.mWallpaperManager != null || tryInit()) {
            Bundle convertIntoBundle = PacMapUtils.convertIntoBundle(pacMap);
            Optional<Object> translateToIBinder = IPCAdapter.translateToIBinder(iRemoteObject);
            if (!translateToIBinder.isPresent() || !(translateToIBinder.get() instanceof IBinder)) {
                HiLog.error(TAG, "sendWallpaperCommand:send wallpaper command failed!", new Object[0]);
            } else {
                this.mWallpaperManager.sendWallpaperCommand((IBinder) translateToIBinder.get(), str, i, i2, i3, convertIntoBundle);
            }
        } else {
            HiLog.error(TAG, "sendWallpaperCommand:Get wallpaper controller instance failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public Element getDefaultElement(int i, int i2, boolean z, float f, float f2, int i3) throws RemoteException {
        if (this.mWallpaperManager != null || tryInit()) {
            try {
                Drawable builtInDrawable = this.mWallpaperManager.getBuiltInDrawable(i, i2, z, f, f2, i3);
                if (builtInDrawable == null) {
                    HiLog.error(TAG, "getDefaultElement:drawable is null!", new Object[0]);
                    throw new RemoteException();
                } else if (!(builtInDrawable instanceof BitmapDrawable)) {
                    return null;
                } else {
                    PixelMap createShellPixelMap = ImageDoubleFwConverter.createShellPixelMap(((BitmapDrawable) builtInDrawable).getBitmap());
                    if (createShellPixelMap != null) {
                        return new PixelMapElement(createShellPixelMap);
                    }
                    HiLog.error(TAG, "getDefaultElement:pixelMap is null!", new Object[0]);
                    throw new RemoteException();
                }
            } catch (IllegalArgumentException unused) {
                HiLog.error(TAG, "getDefaultElement:illegal argument!", new Object[0]);
                throw new RemoteException();
            } catch (RuntimeException unused2) {
                HiLog.error(TAG, "getDefaultElement:Service is not running!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "getDefaultElement:Get wallpaper controller instance failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        return null;
    }

    public Element getElement() {
        HiLog.info(TAG, "getElement begins", new Object[0]);
        if (this.mWallpaperManager != null || tryInit()) {
            Drawable drawable = this.mWallpaperManager.getDrawable();
            if (drawable == null) {
                HiLog.error(TAG, "getElement failed", new Object[0]);
                return null;
            }
            Bitmap drawableToBitmap = drawableToBitmap(drawable);
            if (drawableToBitmap == null) {
                HiLog.error(TAG, "failed in getElement", new Object[0]);
                return null;
            }
            PixelMap createShellPixelMap = ImageDoubleFwConverter.createShellPixelMap(drawableToBitmap.copy(Bitmap.Config.ARGB_8888, true));
            if (createShellPixelMap != null) {
                HiLog.info(TAG, "get pixelMap success", new Object[0]);
                return new PixelMapElement(createShellPixelMap);
            }
            HiLog.error(TAG, "get pixelMap failed", new Object[0]);
            return null;
        }
        HiLog.error(TAG, "getElement:Get wallpaper controller instance failed!", new Object[0]);
        return null;
    }
}

package ohos.miscservices.wallpaper;

import java.io.FileDescriptor;
import java.io.InputStream;
import ohos.agp.components.element.Element;
import ohos.agp.utils.Rect;
import ohos.annotation.SystemApi;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public class WallpaperController {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "WallpaperController");
    public static final int WALLPAPER_LOCK = 2;
    public static final int WALLPAPER_SYSTEM = 1;
    private static volatile WallpaperController sInstance;
    private Context mAppContext;
    private IWallpaperControllerSysAbility mWallpaperControllerSysAbility = null;

    private WallpaperController(Context context) {
        this.mAppContext = context;
        this.mWallpaperControllerSysAbility = WallpaperControllerProxy.getWallpaperControllerSysAbility(context);
    }

    public static WallpaperController getInstance(Context context) {
        if (context == null) {
            return null;
        }
        if (sInstance == null) {
            synchronized (WallpaperController.class) {
                if (sInstance == null) {
                    sInstance = new WallpaperController(context);
                }
            }
        }
        return sInstance;
    }

    public void registerColorsChangedListener(IWallpaperColorsChangedListener iWallpaperColorsChangedListener) {
        if (iWallpaperColorsChangedListener == null) {
            HiLog.error(TAG, "Invaild listener object", new Object[0]);
            return;
        }
        try {
            this.mWallpaperControllerSysAbility.registerColorsChangedListener(iWallpaperColorsChangedListener);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "add listener failed!", new Object[0]);
        }
    }

    public void unregisterColorsChangedListener(IWallpaperColorsChangedListener iWallpaperColorsChangedListener) {
        if (iWallpaperColorsChangedListener == null) {
            HiLog.error(TAG, "Invaild listener object", new Object[0]);
            return;
        }
        try {
            this.mWallpaperControllerSysAbility.unregisterColorsChangedListener(iWallpaperColorsChangedListener);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "unregister listener failed!", new Object[0]);
        }
    }

    public int getMinHeight() {
        try {
            return this.mWallpaperControllerSysAbility.getMinHeight();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "getMinHeight failed!", new Object[0]);
            return 0;
        }
    }

    public int getMinWidth() {
        try {
            return this.mWallpaperControllerSysAbility.getMinWidth();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "getMinWidth failed!", new Object[0]);
            return 0;
        }
    }

    public boolean isChangePermitted() {
        try {
            return this.mWallpaperControllerSysAbility.isChangePermitted();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "isChangePermitted failed!", new Object[0]);
            return false;
        }
    }

    public boolean isOperationAllowed() {
        try {
            return this.mWallpaperControllerSysAbility.isOperationAllowed();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "isOperationAllowed failed!", new Object[0]);
            return false;
        }
    }

    public int getId(int i) {
        return this.mWallpaperControllerSysAbility.getId(i);
    }

    @SystemApi
    public void suggestDesiredWallpaperDimensions(int i, int i2) {
        try {
            this.mWallpaperControllerSysAbility.suggestDesiredWallpaperDimensions(i, i2);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "suggestDesiredWallpaperDimensions failed!", new Object[0]);
        }
    }

    @SystemApi
    public void setWallpaperOffsetSteps(float f, float f2) {
        this.mWallpaperControllerSysAbility.setWallpaperOffsetSteps(f, f2);
    }

    @SystemApi
    public PixelMap getPixelmap(int i) {
        return this.mWallpaperControllerSysAbility.getPixelmap(i);
    }

    public void setWallpaper(PixelMap pixelMap, int i) {
        try {
            this.mWallpaperControllerSysAbility.setWallpaper(pixelMap, i);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "set pixelMap wallpaper failed!", new Object[0]);
        }
    }

    public void setWallpaper(InputStream inputStream, int i) {
        try {
            this.mWallpaperControllerSysAbility.setWallpaper(inputStream, i);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "set inputStream wallpaper failed!", new Object[0]);
        }
    }

    @SystemApi
    public void setWallpaperWithOffsets(PixelMap pixelMap, int[] iArr) {
        try {
            this.mWallpaperControllerSysAbility.setWallpaperWithOffsets(pixelMap, iArr);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "set pixelMap wallpaper offsets failed!", new Object[0]);
        }
    }

    @SystemApi
    public void setWallpaperWithOffsets(InputStream inputStream, int[] iArr) {
        try {
            this.mWallpaperControllerSysAbility.setWallpaperWithOffsets(inputStream, iArr);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "set inputStream wallpaper offsets failed!", new Object[0]);
        }
    }

    public FileDescriptor getFile(int i) {
        try {
            return this.mWallpaperControllerSysAbility.getFile(i);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "getFile failed!", new Object[0]);
            return null;
        }
    }

    public void reset(int i) {
        try {
            this.mWallpaperControllerSysAbility.reset(i);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "reset wallpaper failed!", new Object[0]);
        }
    }

    public void setPadding(Rect rect) {
        try {
            this.mWallpaperControllerSysAbility.setPadding(rect);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "setPadding failed!", new Object[0]);
        }
    }

    @SystemApi
    public int[] getStartingPoints() {
        return this.mWallpaperControllerSysAbility.getStartingPoints();
    }

    @SystemApi
    public PacMap getUserWallpaperBounds() {
        return this.mWallpaperControllerSysAbility.getUserWallpaperBounds();
    }

    public WallpaperColorsCollection getColors(int i) {
        try {
            return this.mWallpaperControllerSysAbility.getColors(i);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "getColors failed!", new Object[0]);
            return null;
        }
    }

    @SystemApi
    public void setWallpaperOffsets(IRemoteObject iRemoteObject, float f, float f2) {
        try {
            this.mWallpaperControllerSysAbility.setWallpaperOffsets(iRemoteObject, f, f2);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "setWallpaperOffsets failed!", new Object[0]);
        }
    }

    @SystemApi
    public void clearWallpaperOffsets(IRemoteObject iRemoteObject) {
        try {
            this.mWallpaperControllerSysAbility.clearWallpaperOffsets(iRemoteObject);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "clearWallpaperOffsets failed!", new Object[0]);
        }
    }

    @SystemApi
    public void sendWallpaperCommand(IRemoteObject iRemoteObject, String str, int i, int i2, int i3, PacMap pacMap) {
        try {
            this.mWallpaperControllerSysAbility.sendWallpaperCommand(iRemoteObject, str, i, i2, i3, pacMap);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "sendWallpaperCommand failed!", new Object[0]);
        }
    }

    public Element getDefaultDrawable(int i, int i2, boolean z, float f, float f2, int i3) {
        try {
            return this.mWallpaperControllerSysAbility.getDefaultDrawable(i, i2, z, f, f2, i3);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "getDefaultDrawable failed!", new Object[0]);
            return null;
        }
    }

    public Element getDrawable() {
        return this.mWallpaperControllerSysAbility.getDrawable();
    }
}

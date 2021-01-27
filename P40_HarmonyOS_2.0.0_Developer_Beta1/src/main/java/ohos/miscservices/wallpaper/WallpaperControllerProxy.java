package ohos.miscservices.wallpaper;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import ohos.agp.components.element.Element;
import ohos.agp.utils.Rect;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;
import ohos.miscservices.wallpaper.WallpaperControllerProxyImpl;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public class WallpaperControllerProxy implements IWallpaperControllerSysAbility {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "WallpaperControllerProxy");
    private static volatile WallpaperControllerProxy sInstance = null;
    private final List<IWallpaperColorsChangedListener> mWallpaperColorsChangedListeners = new ArrayList();
    private final WallpaperControllerProxyImpl.OnWallpaperColorsChangedCallback mWallpaperColorsChangedNotifier = new WallpaperControllerProxyImpl.OnWallpaperColorsChangedCallback() {
        /* class ohos.miscservices.wallpaper.WallpaperControllerProxy.AnonymousClass1 */

        @Override // ohos.miscservices.wallpaper.WallpaperControllerProxyImpl.OnWallpaperColorsChangedCallback
        public void notifyWallpaperColorsChanged(WallpaperColorsCollection wallpaperColorsCollection, int i) {
            synchronized (WallpaperControllerProxy.this.mWallpaperColorsChangedListeners) {
                for (IWallpaperColorsChangedListener iWallpaperColorsChangedListener : WallpaperControllerProxy.this.mWallpaperColorsChangedListeners) {
                    iWallpaperColorsChangedListener.onWallpaperColorsChanged(wallpaperColorsCollection, i);
                }
            }
        }
    };
    private WallpaperControllerProxyImpl mWallpaperControllerProxyImpl;

    private WallpaperControllerProxy(Context context) {
        this.mWallpaperControllerProxyImpl = new WallpaperControllerProxyImpl(context);
    }

    static IWallpaperControllerSysAbility getWallpaperControllerSysAbility(Context context) {
        if (sInstance == null) {
            synchronized (WallpaperControllerProxy.class) {
                if (sInstance == null) {
                    sInstance = new WallpaperControllerProxy(context);
                }
            }
        }
        return sInstance;
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public void registerColorsChangedListener(IWallpaperColorsChangedListener iWallpaperColorsChangedListener) throws RemoteException {
        if (iWallpaperColorsChangedListener == null) {
            HiLog.error(TAG, "add invaild listener", new Object[0]);
            return;
        }
        synchronized (this.mWallpaperColorsChangedListeners) {
            if (this.mWallpaperColorsChangedListeners.isEmpty()) {
                this.mWallpaperControllerProxyImpl.setWallpaperColorsChangedCallback(this.mWallpaperColorsChangedNotifier);
                this.mWallpaperControllerProxyImpl.registerColorsChangedListener();
            }
            if (this.mWallpaperColorsChangedListeners.contains(iWallpaperColorsChangedListener)) {
                HiLog.info(TAG, "duplicate listener", new Object[0]);
            } else {
                this.mWallpaperColorsChangedListeners.add(iWallpaperColorsChangedListener);
            }
        }
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public void unregisterColorsChangedListener(IWallpaperColorsChangedListener iWallpaperColorsChangedListener) throws RemoteException {
        if (iWallpaperColorsChangedListener == null) {
            HiLog.error(TAG, "remove invaild listener", new Object[0]);
            return;
        }
        synchronized (this.mWallpaperColorsChangedListeners) {
            if (this.mWallpaperColorsChangedListeners.contains(iWallpaperColorsChangedListener)) {
                this.mWallpaperColorsChangedListeners.remove(iWallpaperColorsChangedListener);
            }
            if (this.mWallpaperColorsChangedListeners.isEmpty()) {
                this.mWallpaperControllerProxyImpl.unregisterColorsChangedListener();
                this.mWallpaperControllerProxyImpl.removeWallpaperColorsChangedCallback();
            }
        }
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public int getMinHeight() throws RemoteException {
        return this.mWallpaperControllerProxyImpl.getMinHeight();
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public int getMinWidth() throws RemoteException {
        return this.mWallpaperControllerProxyImpl.getMinWidth();
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public boolean isChangePermitted() throws RemoteException {
        return this.mWallpaperControllerProxyImpl.isChangePermitted();
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public boolean isOperationAllowed() throws RemoteException {
        return this.mWallpaperControllerProxyImpl.isOperationAllowed();
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public int getId(int i) {
        return this.mWallpaperControllerProxyImpl.getId(i);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public void suggestDesiredWallpaperDimensions(int i, int i2) throws RemoteException {
        this.mWallpaperControllerProxyImpl.suggestDesiredWallpaperDimensions(i, i2);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public void setWallpaperOffsetSteps(float f, float f2) {
        this.mWallpaperControllerProxyImpl.setWallpaperOffsetSteps(f, f2);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public PixelMap getPixelmap(int i) {
        return this.mWallpaperControllerProxyImpl.getPixelmap(i);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public void setWallpaper(PixelMap pixelMap, int i) throws RemoteException {
        this.mWallpaperControllerProxyImpl.setWallpaper(pixelMap, i);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public void setWallpaper(InputStream inputStream, int i) throws RemoteException {
        this.mWallpaperControllerProxyImpl.setWallpaper(inputStream, i);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public void setWallpaperWithOffsets(PixelMap pixelMap, int[] iArr) throws RemoteException {
        this.mWallpaperControllerProxyImpl.setWallpaperWithOffsets(pixelMap, iArr);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public void setWallpaperWithOffsets(InputStream inputStream, int[] iArr) throws RemoteException {
        this.mWallpaperControllerProxyImpl.setWallpaperWithOffsets(inputStream, iArr);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public FileDescriptor getFile(int i) throws RemoteException {
        return this.mWallpaperControllerProxyImpl.getFile(i);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public void reset(int i) throws RemoteException {
        this.mWallpaperControllerProxyImpl.reset(i);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public void setPadding(Rect rect) throws RemoteException {
        this.mWallpaperControllerProxyImpl.setPadding(rect);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public int[] getStartingPoints() {
        return this.mWallpaperControllerProxyImpl.getStartingPoints();
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public PacMap getUserWallpaperBounds() {
        return this.mWallpaperControllerProxyImpl.getUserWallpaperBounds();
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public WallpaperColorsCollection getColors(int i) throws RemoteException {
        return this.mWallpaperControllerProxyImpl.getColors(i);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public void setWallpaperOffsets(IRemoteObject iRemoteObject, float f, float f2) throws RemoteException {
        this.mWallpaperControllerProxyImpl.setWallpaperOffsets(iRemoteObject, f, f2);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public void clearWallpaperOffsets(IRemoteObject iRemoteObject) throws RemoteException {
        this.mWallpaperControllerProxyImpl.clearWallpaperOffsets(iRemoteObject);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public void sendWallpaperCommand(IRemoteObject iRemoteObject, String str, int i, int i2, int i3, PacMap pacMap) throws RemoteException {
        this.mWallpaperControllerProxyImpl.sendWallpaperCommand(iRemoteObject, str, i, i2, i3, pacMap);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public Element getDefaultElement(int i, int i2, boolean z, float f, float f2, int i3) throws RemoteException {
        return this.mWallpaperControllerProxyImpl.getDefaultElement(i, i2, z, f, f2, i3);
    }

    @Override // ohos.miscservices.wallpaper.IWallpaperControllerSysAbility
    public Element getElement() {
        return this.mWallpaperControllerProxyImpl.getElement();
    }
}

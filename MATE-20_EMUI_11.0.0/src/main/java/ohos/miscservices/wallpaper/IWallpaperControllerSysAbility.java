package ohos.miscservices.wallpaper;

import java.io.FileDescriptor;
import java.io.InputStream;
import ohos.agp.components.element.Element;
import ohos.agp.utils.Rect;
import ohos.media.image.PixelMap;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

interface IWallpaperControllerSysAbility {
    void clearWallpaperOffsets(IRemoteObject iRemoteObject) throws RemoteException;

    WallpaperColorsCollection getColors(int i) throws RemoteException;

    Element getDefaultDrawable(int i, int i2, boolean z, float f, float f2, int i3) throws RemoteException;

    Element getDrawable();

    FileDescriptor getFile(int i) throws RemoteException;

    int getId(int i);

    int getMinHeight() throws RemoteException;

    int getMinWidth() throws RemoteException;

    PixelMap getPixelmap(int i);

    int[] getStartingPoints();

    PacMap getUserWallpaperBounds();

    boolean isChangePermitted() throws RemoteException;

    boolean isOperationAllowed() throws RemoteException;

    void registerColorsChangedListener(IWallpaperColorsChangedListener iWallpaperColorsChangedListener) throws RemoteException;

    void reset(int i) throws RemoteException;

    void sendWallpaperCommand(IRemoteObject iRemoteObject, String str, int i, int i2, int i3, PacMap pacMap) throws RemoteException;

    void setPadding(Rect rect) throws RemoteException;

    void setWallpaper(InputStream inputStream, int i) throws RemoteException;

    void setWallpaper(PixelMap pixelMap, int i) throws RemoteException;

    void setWallpaperOffsetSteps(float f, float f2);

    void setWallpaperOffsets(IRemoteObject iRemoteObject, float f, float f2) throws RemoteException;

    void setWallpaperWithOffsets(InputStream inputStream, int[] iArr) throws RemoteException;

    void setWallpaperWithOffsets(PixelMap pixelMap, int[] iArr) throws RemoteException;

    void suggestDesiredWallpaperDimensions(int i, int i2) throws RemoteException;

    void unregisterColorsChangedListener(IWallpaperColorsChangedListener iWallpaperColorsChangedListener) throws RemoteException;
}

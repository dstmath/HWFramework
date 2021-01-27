package ohos.account.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IUserManager;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.io.IOException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;
import ohos.media.image.inner.ImageDoubleFwConverter;

public class AccountProxyAdapter {
    private static final PixelMap FAIL_PIXEL_MAP = null;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final int LOG_DOMAIN = 218110720;
    private static final String TAG = "AccountProxyAdapter";
    private static final String USER_SERVICE_NAME = "user";
    private final Object remoteLock = new Object();
    private volatile IUserManager service = null;

    private IUserManager getUserService() {
        if (this.service != null) {
            return this.service;
        }
        synchronized (this.remoteLock) {
            if (this.service == null) {
                try {
                    this.service = IUserManager.Stub.asInterface(ServiceManager.getServiceOrThrow(USER_SERVICE_NAME));
                    if (this.service == null) {
                        HiLog.error(LABEL, "ServiceManager get failed", new Object[0]);
                        return this.service;
                    }
                } catch (ServiceManager.ServiceNotFoundException unused) {
                    HiLog.error(LABEL, "get user service not found exception", new Object[0]);
                    return this.service;
                }
            }
            return this.service;
        }
    }

    private boolean isValidLocalId(int i) {
        if (i < 0) {
            HiLog.error(LABEL, "isValidLocalId localId error", new Object[0]);
            return false;
        }
        IUserManager userService = getUserService();
        if (userService == null) {
            HiLog.error(LABEL, "isValidLocalId getAccountService failed", new Object[0]);
            return false;
        }
        try {
            if (userService.getUserInfo(i) != null) {
                return true;
            }
            return false;
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "isValidLocalId RemoteException", new Object[0]);
            return false;
        }
    }

    public PixelMap getOsAccountProfilePhoto(int i) {
        HiLog.debug(LABEL, "getOsAccountProfilePhoto begin", new Object[0]);
        if (i < 0) {
            HiLog.error(LABEL, "localId error", new Object[0]);
            return FAIL_PIXEL_MAP;
        }
        IUserManager userService = getUserService();
        if (userService == null) {
            HiLog.error(LABEL, "getAccountService failed", new Object[0]);
            return FAIL_PIXEL_MAP;
        }
        try {
            ParcelFileDescriptor userIcon = userService.getUserIcon(i);
            if (userIcon == null) {
                HiLog.error(LABEL, "getUserIcon failed", new Object[0]);
                return FAIL_PIXEL_MAP;
            }
            try {
                PixelMap createShellPixelMap = ImageDoubleFwConverter.createShellPixelMap(BitmapFactory.decodeFileDescriptor(userIcon.getFileDescriptor()));
                try {
                    userIcon.close();
                } catch (IOException unused) {
                    HiLog.error(LABEL, "close fd data error", new Object[0]);
                }
                return createShellPixelMap;
            } catch (Throwable th) {
                try {
                    userIcon.close();
                } catch (IOException unused2) {
                    HiLog.error(LABEL, "close fd data error", new Object[0]);
                }
                throw th;
            }
        } catch (RemoteException unused3) {
            HiLog.error(LABEL, "getOsAccountProfilePhoto RemoteException error", new Object[0]);
            return FAIL_PIXEL_MAP;
        }
    }

    public boolean setOsAccountProfilePhoto(int i, PixelMap pixelMap) {
        HiLog.debug(LABEL, "setOsAccountProfilePhoto begin", new Object[0]);
        if (pixelMap == null) {
            HiLog.error(LABEL, "photo error", new Object[0]);
            return false;
        } else if (!isValidLocalId(i)) {
            HiLog.error(LABEL, "local id error", new Object[0]);
            return false;
        } else {
            Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap(pixelMap);
            if (createShadowBitmap == null) {
                HiLog.error(LABEL, "createShadowBitmap failed", new Object[0]);
                return false;
            }
            IUserManager userService = getUserService();
            if (userService == null) {
                HiLog.error(LABEL, "getAccountService failed", new Object[0]);
                return false;
            }
            try {
                userService.setUserIcon(i, createShadowBitmap);
                HiLog.debug(LABEL, "setOsAccountProfilePhoto end", new Object[0]);
                return true;
            } catch (RemoteException unused) {
                HiLog.error(LABEL, "setUserIcon RemoteException", new Object[0]);
                return false;
            }
        }
    }
}

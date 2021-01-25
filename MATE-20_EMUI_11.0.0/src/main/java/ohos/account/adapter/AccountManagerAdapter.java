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

public class AccountManagerAdapter {
    private static final String ACCOUNT_SERVICE_NAME = "user";
    private static final PixelMap FAIL_PIXEL_MAP = null;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final int LOG_DOMAIN = 218110720;
    private static final String TAG = "AccountProxy";
    private IUserManager mService = null;

    private IUserManager getAccountService() {
        IUserManager iUserManager = this.mService;
        if (iUserManager != null) {
            return iUserManager;
        }
        try {
            this.mService = IUserManager.Stub.asInterface(ServiceManager.getServiceOrThrow(ACCOUNT_SERVICE_NAME));
            if (this.mService != null) {
                return this.mService;
            }
            HiLog.error(LABEL, "ServiceManager get failed", new Object[0]);
            return this.mService;
        } catch (ServiceManager.ServiceNotFoundException unused) {
            HiLog.error(LABEL, "get user service not found exception", new Object[0]);
            return this.mService;
        }
    }

    private boolean isValidLocalId(int i) {
        if (i < 0) {
            HiLog.error(LABEL, "isValidLocalId localId error", new Object[0]);
            return false;
        }
        IUserManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "isValidLocalId getAccountService failed", new Object[0]);
            return false;
        }
        try {
            if (accountService.getUserInfo(i) != null) {
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
        IUserManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "getAccountService failed", new Object[0]);
            return FAIL_PIXEL_MAP;
        }
        try {
            ParcelFileDescriptor userIcon = accountService.getUserIcon(i);
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
            IUserManager accountService = getAccountService();
            if (accountService == null) {
                HiLog.error(LABEL, "getAccountService failed", new Object[0]);
                return false;
            }
            try {
                accountService.setUserIcon(i, createShadowBitmap);
                HiLog.debug(LABEL, "setOsAccountProfilePhoto end", new Object[0]);
                return true;
            } catch (RemoteException unused) {
                HiLog.error(LABEL, "setUserIcon RemoteException", new Object[0]);
                return false;
            }
        }
    }
}

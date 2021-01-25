package ohos.ai.engine.system;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import ohos.ai.engine.pluginbridge.CoreServiceSkeleton;
import ohos.ai.engine.pluginbridge.ICoreService;
import ohos.ai.engine.utils.HiAILog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class SystemCoreManager {
    private static final String TAG = SystemCoreManager.class.getSimpleName();
    private static volatile SystemCoreManager instance = null;
    private IRemoteObject mCoreService;

    private SystemCoreManager() {
    }

    public static SystemCoreManager getInstance() {
        if (instance == null) {
            synchronized (SystemCoreManager.class) {
                if (instance == null) {
                    instance = new SystemCoreManager();
                }
            }
        }
        return instance;
    }

    public void setCoreService(IRemoteObject iRemoteObject) {
        this.mCoreService = iRemoteObject;
    }

    public Optional<ISystemCore> getSystemCore() {
        ICoreService orElse = CoreServiceSkeleton.asInterface(this.mCoreService).orElse(null);
        if (orElse == null) {
            HiAILog.info(TAG, "iCoreService is null");
            return Optional.empty();
        }
        try {
            return SystemCoreSkeleton.asInterface(orElse.getSystemCoreRemoteObject());
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "getSystemCore ");
            return Optional.empty();
        }
    }

    public Optional<String> getProp(String str, String str2) {
        HiAILog.info(TAG, "getProp");
        ISystemCore orElse = getSystemCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[getProp] SystemCore is null");
            return Optional.empty();
        }
        try {
            return orElse.getProp(str, str2);
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[getProp] RemoteException e");
            return Optional.empty();
        }
    }

    public Optional<String> getUdid() {
        HiAILog.info(TAG, "getUDID");
        ISystemCore orElse = getSystemCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[getUdid] SystemCore is null");
            return Optional.empty();
        }
        try {
            return orElse.getUdid();
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[getUdid] RemoteException e");
            return Optional.empty();
        }
    }

    public Optional<String> getSerialNumber() {
        HiAILog.info(TAG, "getSerialNumber");
        ISystemCore orElse = getSystemCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[getSerialNumber] SystemCore is null");
            return Optional.empty();
        }
        try {
            return orElse.getSerialNumber();
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[getSerialNumber] RemoteException e");
            return Optional.empty();
        }
    }

    public Optional<String> getSystemVersion() {
        HiAILog.info(TAG, "getSystemVersion");
        ISystemCore orElse = getSystemCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[getSystemVersion] SystemCore is null");
            return Optional.empty();
        }
        try {
            return orElse.getSystemVersion();
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[getSystemVersion] RemoteException e");
            return Optional.empty();
        }
    }

    public Optional<String> getSystemModel() {
        HiAILog.info(TAG, "getSystemModel");
        ISystemCore orElse = getSystemCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[getSystemModel] SystemCore is null");
            return Optional.empty();
        }
        try {
            return orElse.getSystemModel();
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[getSystemModel] RemoteException e");
            return Optional.empty();
        }
    }

    public Optional<String> getDeviceBrand() {
        HiAILog.info(TAG, "getDeviceBrand");
        ISystemCore orElse = getSystemCore().orElse(null);
        if (orElse == null) {
            HiAILog.error(TAG, "[getDeviceBrand] SystemCore is null");
            return Optional.empty();
        }
        try {
            return orElse.getDeviceBrand();
        } catch (RemoteException unused) {
            HiAILog.error(TAG, "[getDeviceBrand] RemoteException e");
            return Optional.empty();
        }
    }

    public Optional<String> getDeviceId() {
        String orElse = getUdid().orElse(null);
        if (orElse == null || orElse.length() == 0) {
            return getSerialNumber();
        }
        return Optional.of(orElse);
    }

    public String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date());
    }

    public String getTimeZone() {
        return TimeZone.getDefault().getDisplayName(false, 0);
    }

    public String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }
}

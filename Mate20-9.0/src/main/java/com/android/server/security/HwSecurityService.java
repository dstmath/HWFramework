package com.android.server.security;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.server.SystemService;
import com.android.server.security.IFAA.IFAAPlugin;
import com.android.server.security.antimal.HwAntiMalPlugin;
import com.android.server.security.ccmode.HwCCModePlugin;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.deviceusage.HwDeviceUsagePlugin;
import com.android.server.security.eidservice.HwEidPlugin;
import com.android.server.security.fileprotect.HwSfpService;
import com.android.server.security.hsm.HwSystemManagerPlugin;
import com.android.server.security.hwkeychain.HwKeychainService;
import com.android.server.security.inseservice.InSEService;
import com.android.server.security.panpay.PanPayService;
import com.android.server.security.pwdprotect.PwdProtectService;
import com.android.server.security.securitydiagnose.HwSecurityDiagnosePlugin;
import com.android.server.security.securityprofile.SecurityProfileService;
import com.android.server.security.trustcircle.TrustCirclePlugin;
import com.android.server.security.trustspace.TrustSpaceManagerService;
import com.android.server.security.tsmagent.service.TSMAgentService;
import com.android.server.security.ukey.UKeyManagerService;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.security.IHwSecurityService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class HwSecurityService extends SystemService {
    private static final int CCMODE_PLUGIN_ID = 9;
    private static final int DEFAULT_PLUGIN_ID = 0;
    private static final int DEFAULT_RESIDENT_PRIORITY = 0;
    private static final int DEVICE_SECURE_DIAGNOSE_ID = 2;
    private static final int DEVICE_USAGE_PLUGIN_ID = 1;
    private static final int DYNAMIC_PLUGIN_FLAG = 0;
    private static final int FILEPROTECT_PLUGIN_ID = 11;
    private static final int HWSYSTEMMANAGER_PLUGIN_ID = 13;
    private static final int HW_ANTIMAL_PLUGIN_ID = 16;
    /* access modifiers changed from: private */
    public static final boolean HW_DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int HW_EID_PLUGIN_ID = 15;
    private static final int HW_INSE_PLUGIN_ID = 14;
    private static final int HW_KEYCHAIN_PLUGIN_ID = 20;
    private static final int IFAA_PLUGIN_ID = 3;
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final String MANAGE_USE_SECURITY = "com.huawei.permission.MANAGE_USE_SECURITY";
    private static final int PAN_PAY_PLUGIN_ID = 12;
    private static final String PROPERTIES_CC_MODE_SUPPORTED = "ro.config.support_ccmode";
    private static final int PWDPROTECT_PLUGIN_ID = 10;
    private static final int RESIDENT_PLUGIN_FLAG = 1;
    private static int SEAPP_RESIDENT_PRIORITY = 100;
    private static final int SECURITYPROFILE_PLUGIN_ID = 8;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final boolean SUPPORT_HW_IUDF = "true".equalsIgnoreCase(SystemProperties.get("ro.config.support_iudf", "false"));
    private static final boolean SUPPORT_HW_SEAPP = "true".equalsIgnoreCase(SystemProperties.get("ro.config.support_iseapp", "false"));
    private static final boolean SUPPORT_PRIVSPACE = "true".equalsIgnoreCase(SystemProperties.get("ro.config.support_privacyspace", "false"));
    private static final String TAG = "HwSecurityService";
    private static final int TRUSTCIRCLE_PLUGIN_ID = 5;
    private static final int TRUSTSPACE_PLUGIN_ID = 4;
    private static final int TSMAGENT_PLUGIN_ID = 7;
    private static final int UKEY_PLUGIN_ID = 6;
    private Context mContext;
    private ArrayMap<Integer, HwSecurityPluginObj> mMapPlugins = new ArrayMap<>();

    public static class HwSecurityDynamicPluginRef implements IHwPluginRef {
        private static final String TAG = "HwSecurityDynamicPluginRef";
        private HashMap<IBinder, Death> mClient = new HashMap<>();
        private HwSecurityService mParentService;
        private int mPlugInID;
        private IHwSecurityPlugin mPlugin;

        private class Death implements IBinder.DeathRecipient {
            IBinder token;

            Death(IBinder token2) {
                this.token = token2;
            }

            public void binderDied() {
                HwSecurityDynamicPluginRef.this.onClientBinderDie(this.token);
            }
        }

        public HwSecurityDynamicPluginRef(int pluginId, HwSecurityService service) {
            this.mPlugInID = pluginId;
            this.mParentService = service;
        }

        public IHwSecurityPlugin get() {
            return this.mPlugin;
        }

        public void set(IHwSecurityPlugin plugIn) {
            this.mPlugin = plugIn;
        }

        public void bind(IBinder client) {
            if (client != null) {
                if (HwSecurityService.HW_DEBUG) {
                    Slog.d(TAG, "HwSecurityDynamicPluginRef, bind");
                }
                Death d = new Death(client);
                try {
                    client.linkToDeath(d, 0);
                } catch (RemoteException e) {
                    Slog.e(TAG, "error");
                }
                this.mClient.put(client, d);
                if (HwSecurityService.HW_DEBUG) {
                    Slog.d(TAG, "HwSecurityDynamicPluginRef mClient size:" + this.mClient.size() + ", client" + client);
                }
            }
        }

        public boolean unBind(IBinder client) {
            if (HwSecurityService.HW_DEBUG) {
                Slog.d(TAG, "HwSecurityDynamicPluginRef, unBind");
            }
            client.unlinkToDeath(this.mClient.remove(client), 0);
            if (HwSecurityService.HW_DEBUG) {
                Slog.d(TAG, "HwSecurityDynamicPluginRef mClient size:" + this.mClient.size() + ", client" + client);
            }
            return this.mClient.isEmpty();
        }

        public void onClientBinderDie(IBinder client) {
            this.mParentService.onClientBinderDie(this.mPlugInID, client);
        }
    }

    private static class HwSecurityPluginObj {
        public boolean isRequiredPermission;
        public IHwSecurityPlugin.Creator mCreator;
        public int mFlag;
        public Object mLock;
        public IHwPluginRef mPluginRef;
        public int mStartupTiming;
        public int residentPriorityLevel;

        private HwSecurityPluginObj() {
            this.isRequiredPermission = true;
        }
    }

    public static class HwSecurityPluginRef implements IHwPluginRef {
        private IHwSecurityPlugin mPlugin;

        public IHwSecurityPlugin get() {
            return this.mPlugin;
        }

        public void set(IHwSecurityPlugin plugIn) {
            this.mPlugin = plugIn;
        }

        public void bind(IBinder binder) {
        }

        public boolean unBind(IBinder binder) {
            return false;
        }
    }

    private final class HwSecurityServiceWrapper extends IHwSecurityService.Stub {
        private HwSecurityServiceWrapper() {
        }

        public IBinder querySecurityInterface(int pluginId) {
            return HwSecurityService.this.queryInterface(pluginId);
        }

        public void unBind(int pluginId, IBinder client) {
            HwSecurityService.this.unBindDynamicPlugin(pluginId, client, false);
        }

        public IBinder bind(int pluginId, IBinder client) {
            return HwSecurityService.this.bindDynamicPlugin(pluginId, client);
        }
    }

    public interface IHwPluginRef {
        void bind(IBinder iBinder);

        IHwSecurityPlugin get();

        void set(IHwSecurityPlugin iHwSecurityPlugin);

        boolean unBind(IBinder iBinder);
    }

    private static class ResidentPriorityComparator implements Comparator<HwSecurityPluginObj>, Serializable {
        private ResidentPriorityComparator() {
        }

        public int compare(HwSecurityPluginObj plugin1, HwSecurityPluginObj plugin2) {
            if (plugin1.residentPriorityLevel == plugin2.residentPriorityLevel) {
                return 0;
            }
            if (plugin1.residentPriorityLevel < plugin2.residentPriorityLevel) {
                return 1;
            }
            return -1;
        }
    }

    public HwSecurityService(Context context) {
        super(context);
        this.mContext = context;
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.security.HwSecurityService$HwSecurityServiceWrapper, android.os.IBinder] */
    public void onStart() {
        if (HW_DEBUG) {
            Slog.d(TAG, "Start HwSecurityService");
        }
        publishBinderService(SECURITY_SERVICE, new HwSecurityServiceWrapper());
        registerPlugin(1, 1, 1000, HwDeviceUsagePlugin.CREATOR, null);
        registerPlugin(2, 1, 500, HwSecurityDiagnosePlugin.CREATOR, null);
        registerPlugin(3, 0, 1000, IFAAPlugin.CREATOR, IFAAPlugin.BINDLOCK);
        Slog.d(TAG, "is add IFAAPlugin");
        registerPlugin(4, 1, 500, TrustSpaceManagerService.CREATOR, null);
        registerPlugin(5, 1, 1000, TrustCirclePlugin.CREATOR, TrustCirclePlugin.BINDLOCK);
        registerPlugin(6, 1, 1000, UKeyManagerService.CREATOR, null, false);
        registerPlugin(7, 1, 1000, TSMAgentService.CREATOR, TSMAgentService.BINDLOCK, false);
        registerPlugin(12, 1, 1000, PanPayService.CREATOR, PanPayService.BINDLOCK, false);
        if (SUPPORT_HW_SEAPP) {
            registerPlugin(8, 1, 500, SecurityProfileService.CREATOR, null, false, SEAPP_RESIDENT_PRIORITY);
        }
        if (SystemProperties.getBoolean(PROPERTIES_CC_MODE_SUPPORTED, false)) {
            registerPlugin(9, 1, 1000, HwCCModePlugin.CREATOR, HwCCModePlugin.BINDLOCK);
        }
        if (SUPPORT_PRIVSPACE) {
            registerPlugin(10, 1, 1000, PwdProtectService.CREATOR, null, false);
        }
        if (SUPPORT_HW_IUDF) {
            registerPlugin(11, 1, 500, HwSfpService.CREATOR, null, false);
        }
        registerPlugin(13, 1, 500, HwSystemManagerPlugin.CREATOR, null, false);
        registerPlugin(14, 1, 500, InSEService.CREATOR, InSEService.BINDLOCK);
        Slog.d(TAG, "is add inEService");
        registerPlugin(20, 1, 1000, HwKeychainService.CREATOR, null, false);
        if (IS_CHINA_AREA) {
            registerPlugin(16, 1, 1000, HwAntiMalPlugin.CREATOR, null, false);
        }
        registerPlugin(15, 1, 500, HwEidPlugin.CREATOR, HwEidPlugin.BINDLOCK);
        Slog.d(TAG, "is add HwEidPlugin");
    }

    public void onBootPhase(int phase) {
        startResidentPlugin(phase);
    }

    private void startResidentPlugin(int startupTiming) {
        if (!this.mMapPlugins.isEmpty()) {
            List<HwSecurityPluginObj> pluginList = new ArrayList<>();
            for (Integer intValue : this.mMapPlugins.keySet()) {
                HwSecurityPluginObj obj = this.mMapPlugins.get(Integer.valueOf(intValue.intValue()));
                if (obj.mStartupTiming == startupTiming && obj.mFlag == 1) {
                    pluginList.add(obj);
                }
            }
            Collections.sort(pluginList, new ResidentPriorityComparator());
            for (HwSecurityPluginObj obj2 : pluginList) {
                IHwSecurityPlugin plugIn = obj2.mCreator.createPlugin(this.mContext);
                if (HW_DEBUG) {
                    Slog.d(TAG, "createPlugin");
                }
                obj2.mPluginRef.set(plugIn);
                if (plugIn != null) {
                    if (HW_DEBUG) {
                        Slog.d(TAG, "Plugin start");
                    }
                    plugIn.onStart();
                } else if (HW_DEBUG) {
                    Slog.d(TAG, "plugIn is null");
                }
            }
        }
    }

    private void checkPluginPermission(HwSecurityPluginObj obj) {
        if (obj != null && obj.mCreator != null && obj.isRequiredPermission) {
            String pluginPermission = obj.mCreator.getPluginPermission();
            if (pluginPermission != null) {
                checkPermission(pluginPermission);
            } else {
                checkPermission(MANAGE_USE_SECURITY);
            }
        }
    }

    /* access modifiers changed from: private */
    public IBinder bindDynamicPlugin(int pluginId, IBinder client) {
        IBinder asBinder;
        Slog.d(TAG, "bindDynamicPlugin");
        if (client == null || this.mMapPlugins == null || !this.mMapPlugins.containsKey(Integer.valueOf(pluginId))) {
            Slog.e(TAG, "client is null or no this dynamic Plugin");
            return null;
        }
        HwSecurityPluginObj obj = this.mMapPlugins.get(Integer.valueOf(pluginId));
        if (obj == null || obj.mFlag != 0) {
            return null;
        }
        checkPluginPermission(obj);
        synchronized (obj.mLock) {
            IHwSecurityPlugin plugIn = obj.mPluginRef.get();
            if (plugIn == null) {
                plugIn = obj.mCreator.createPlugin(this.mContext);
                plugIn.onStart();
                obj.mPluginRef.set(plugIn);
            }
            obj.mPluginRef.bind(client);
            asBinder = plugIn.asBinder();
        }
        return asBinder;
    }

    /* access modifiers changed from: private */
    public IBinder queryInterface(int pluginId) {
        Slog.d(TAG, "find this Resident Plugin");
        if (this.mMapPlugins == null || !this.mMapPlugins.containsKey(Integer.valueOf(pluginId))) {
            Slog.e(TAG, "not find this Resident Plugin");
            return null;
        }
        HwSecurityPluginObj obj = this.mMapPlugins.get(Integer.valueOf(pluginId));
        if (obj == null || obj.mFlag != 1) {
            Slog.e(TAG, "obj == null");
            return null;
        }
        checkPluginPermission(obj);
        IHwSecurityPlugin plugIn = obj.mPluginRef.get();
        if (plugIn != null) {
            return plugIn.asBinder();
        }
        return null;
    }

    private void registerPlugin(int pluginId, int flag, int startupTiming, IHwSecurityPlugin.Creator creator, Object lockObj) {
        registerPlugin(pluginId, flag, startupTiming, creator, lockObj, true, 0);
    }

    private void registerPlugin(int pluginId, int flag, int startupTiming, IHwSecurityPlugin.Creator creator, Object lockObj, boolean isRequiredPermission) {
        registerPlugin(pluginId, flag, startupTiming, creator, lockObj, isRequiredPermission, 0);
    }

    private void registerPlugin(int pluginId, int flag, int startupTiming, IHwSecurityPlugin.Creator creator, Object lockObj, boolean isRequiredPermission, int residentPriorityLevel) {
        if (HW_DEBUG) {
            Slog.d(TAG, "addPlugin");
        }
        if (creator != null) {
            HwSecurityPluginObj obj = new HwSecurityPluginObj();
            obj.mFlag = flag;
            obj.mCreator = creator;
            obj.mLock = lockObj;
            obj.mStartupTiming = startupTiming;
            obj.isRequiredPermission = isRequiredPermission;
            obj.residentPriorityLevel = residentPriorityLevel;
            if (1 == flag) {
                obj.mPluginRef = new HwSecurityPluginRef();
            } else if (flag == 0) {
                obj.mPluginRef = new HwSecurityDynamicPluginRef(pluginId, this);
            }
            this.mMapPlugins.put(Integer.valueOf(pluginId), obj);
        }
    }

    public void onClientBinderDie(int pluginId, IBinder client) {
        if (client != null) {
            unBindDynamicPlugin(pluginId, client, true);
        }
    }

    /* access modifiers changed from: private */
    public void unBindDynamicPlugin(int pluginId, IBinder client, boolean isInnerCalling) {
        if (HW_DEBUG) {
            Slog.d(TAG, "unBindDynamicPlugin");
        }
        if (this.mMapPlugins != null && this.mMapPlugins.containsKey(Integer.valueOf(pluginId))) {
            HwSecurityPluginObj obj = this.mMapPlugins.get(Integer.valueOf(pluginId));
            if (obj != null && obj.mFlag == 0) {
                if (!isInnerCalling) {
                    checkPluginPermission(obj);
                }
                synchronized (obj.mLock) {
                    IHwSecurityPlugin plugIn = obj.mPluginRef.get();
                    if (obj.mPluginRef.unBind(client)) {
                        plugIn.onStop();
                        obj.mPluginRef.set(null);
                    }
                }
            }
        }
    }

    private void checkPermission(String permission) {
        Context context = getContext();
        context.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }
}

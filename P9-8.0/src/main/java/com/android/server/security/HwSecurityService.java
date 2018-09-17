package com.android.server.security;

import android.content.Context;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.server.SystemService;
import com.android.server.am.HwActivityManagerService;
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.security.IFAA.IFAAPlugin;
import com.android.server.security.ccmode.HwCCModePlugin;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.core.IHwSecurityPlugin.Creator;
import com.android.server.security.deviceusage.HwDeviceUsagePlugin;
import com.android.server.security.securitydiagnose.HwSecurityDiagnosePlugin;
import com.android.server.security.securityprofile.SecurityProfileService;
import com.android.server.security.trustcircle.TrustCirclePlugin;
import com.android.server.security.trustspace.TrustSpaceManagerService;
import com.android.server.security.tsmagent.service.TSMAgentService;
import com.android.server.security.ukey.UKeyManagerService;
import huawei.android.security.IHwSecurityService.Stub;
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
    private static final boolean HW_DEBUG;
    private static final int IFAA_PLUGIN_ID = 3;
    private static final String MANAGE_USE_SECURITY = "com.huawei.permission.MANAGE_USE_SECURITY";
    private static final String PROPERTIES_CC_MODE_SUPPORTED = "ro.config.support_ccmode";
    private static final int RESIDENT_PLUGIN_FLAG = 1;
    private static int SEAPP_RESIDENT_PRIORITY = 100;
    private static final int SECURITYPROFILE_PLUGIN_ID = 8;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final boolean SUPPORT_HW_SEAPP = StorageUtils.SDCARD_ROMOUNTED_STATE.equalsIgnoreCase(SystemProperties.get("ro.config.support_iseapp", StorageUtils.SDCARD_RWMOUNTED_STATE));
    private static final String TAG = "HwSecurityService";
    private static final int TRUSTCIRCLE_PLUGIN_ID = 5;
    private static final int TRUSTSPACE_PLUGIN_ID = 4;
    private static final int TSMAGENT_PLUGIN_ID = 7;
    private static final int UKEY_PLUGIN_ID = 6;
    private Context mContext;
    private ArrayMap<Integer, HwSecurityPluginObj> mMapPlugins = new ArrayMap();

    public interface IHwPluginRef {
        void bind(IBinder iBinder);

        IHwSecurityPlugin get();

        void set(IHwSecurityPlugin iHwSecurityPlugin);

        boolean unBind(IBinder iBinder);
    }

    public static class HwSecurityDynamicPluginRef implements IHwPluginRef {
        private static final String TAG = "HwSecurityDynamicPluginRef";
        private HashMap<IBinder, Death> mClient = new HashMap();
        private HwSecurityService mParentService;
        private int mPlugInID;
        private IHwSecurityPlugin mPlugin;

        private class Death implements DeathRecipient {
            IBinder token;

            Death(IBinder token) {
                this.token = token;
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
            client.unlinkToDeath((Death) this.mClient.remove(client), 0);
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
        public Creator mCreator;
        public int mFlag;
        public Object mLock;
        public IHwPluginRef mPluginRef;
        public int mStartupTiming;
        public int residentPriorityLevel;

        /* synthetic */ HwSecurityPluginObj(HwSecurityPluginObj -this0) {
            this();
        }

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

    private final class HwSecurityServiceWrapper extends Stub {
        /* synthetic */ HwSecurityServiceWrapper(HwSecurityService this$0, HwSecurityServiceWrapper -this1) {
            this();
        }

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

    private static class ResidentPriorityComparator implements Comparator<HwSecurityPluginObj>, Serializable {
        /* synthetic */ ResidentPriorityComparator(ResidentPriorityComparator -this0) {
            this();
        }

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

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HW_DEBUG = isLoggable;
    }

    public HwSecurityService(Context context) {
        super(context);
        this.mContext = context;
    }

    public void onStart() {
        if (HW_DEBUG) {
            Slog.d(TAG, "Start HwSecurityService");
        }
        publishBinderService(SECURITY_SERVICE, new HwSecurityServiceWrapper(this, null));
        registerPlugin(1, 1, 1000, HwDeviceUsagePlugin.CREATOR, null);
        registerPlugin(2, 1, HwActivityManagerService.SERVICE_ADJ, HwSecurityDiagnosePlugin.CREATOR, null);
        registerPlugin(3, 0, 1000, IFAAPlugin.CREATOR, IFAAPlugin.BINDLOCK);
        Slog.d(TAG, "is add IFAAPlugin");
        registerPlugin(4, 1, HwActivityManagerService.SERVICE_ADJ, TrustSpaceManagerService.CREATOR, null);
        registerPlugin(5, 1, 1000, TrustCirclePlugin.CREATOR, TrustCirclePlugin.BINDLOCK);
        registerPlugin(6, 1, 1000, UKeyManagerService.CREATOR, null, false);
        registerPlugin(7, 1, 1000, TSMAgentService.CREATOR, TSMAgentService.BINDLOCK, false);
        if (SUPPORT_HW_SEAPP) {
            registerPlugin(8, 1, HwActivityManagerService.SERVICE_ADJ, SecurityProfileService.CREATOR, null, false, SEAPP_RESIDENT_PRIORITY);
        }
        if (SystemProperties.getBoolean(PROPERTIES_CC_MODE_SUPPORTED, false)) {
            registerPlugin(9, 1, 1000, HwCCModePlugin.CREATOR, HwCCModePlugin.BINDLOCK);
        }
    }

    public void onBootPhase(int phase) {
        startResidentPlugin(phase);
    }

    private void startResidentPlugin(int startupTiming) {
        if (!this.mMapPlugins.isEmpty()) {
            HwSecurityPluginObj obj;
            List<HwSecurityPluginObj> pluginList = new ArrayList();
            for (Integer intValue : this.mMapPlugins.keySet()) {
                obj = (HwSecurityPluginObj) this.mMapPlugins.get(Integer.valueOf(intValue.intValue()));
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
        if (obj != null && obj.mCreator != null && (obj.isRequiredPermission ^ 1) == 0) {
            String pluginPermission = obj.mCreator.getPluginPermission();
            if (pluginPermission != null) {
                checkPermission(pluginPermission);
            } else {
                checkPermission(MANAGE_USE_SECURITY);
            }
        }
    }

    private IBinder bindDynamicPlugin(int pluginId, IBinder client) {
        Slog.d(TAG, "bindDynamicPlugin");
        if (client == null || this.mMapPlugins == null || (this.mMapPlugins.containsKey(Integer.valueOf(pluginId)) ^ 1) != 0) {
            Slog.e(TAG, "client is null or no this dynamic Plugin");
            return null;
        }
        HwSecurityPluginObj obj = (HwSecurityPluginObj) this.mMapPlugins.get(Integer.valueOf(pluginId));
        if (obj == null || obj.mFlag != 0) {
            return null;
        }
        IBinder asBinder;
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

    private IBinder queryInterface(int pluginId) {
        Slog.d(TAG, "find this Resident Plugin");
        if (this.mMapPlugins == null || (this.mMapPlugins.containsKey(Integer.valueOf(pluginId)) ^ 1) != 0) {
            Slog.e(TAG, "not find this Resident Plugin");
            return null;
        }
        HwSecurityPluginObj obj = (HwSecurityPluginObj) this.mMapPlugins.get(Integer.valueOf(pluginId));
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

    private void registerPlugin(int pluginId, int flag, int startupTiming, Creator creator, Object lockObj) {
        registerPlugin(pluginId, flag, startupTiming, creator, lockObj, true, 0);
    }

    private void registerPlugin(int pluginId, int flag, int startupTiming, Creator creator, Object lockObj, boolean isRequiredPermission) {
        registerPlugin(pluginId, flag, startupTiming, creator, lockObj, isRequiredPermission, 0);
    }

    private void registerPlugin(int pluginId, int flag, int startupTiming, Creator creator, Object lockObj, boolean isRequiredPermission, int residentPriorityLevel) {
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

    private void unBindDynamicPlugin(int pluginId, IBinder client, boolean isInnerCalling) {
        if (HW_DEBUG) {
            Slog.d(TAG, "unBindDynamicPlugin");
        }
        if (this.mMapPlugins != null && (this.mMapPlugins.containsKey(Integer.valueOf(pluginId)) ^ 1) == 0) {
            HwSecurityPluginObj obj = (HwSecurityPluginObj) this.mMapPlugins.get(Integer.valueOf(pluginId));
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
        getContext().enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }
}

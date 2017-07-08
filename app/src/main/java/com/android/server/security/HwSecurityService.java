package com.android.server.security;

import android.content.Context;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.server.SystemService;
import com.android.server.am.HwActivityManagerService;
import com.android.server.security.IFAA.IFAAPlugin;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.core.IHwSecurityPlugin.Creator;
import com.android.server.security.deviceusage.HwDeviceUsagePlugin;
import com.android.server.security.securitydiagnose.HwSecurityDiagnosePlugin;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.security.trustcircle.TrustCirclePlugin;
import com.android.server.security.trustspace.TrustSpaceManagerService;
import huawei.android.security.IHwSecurityService.Stub;
import java.util.HashMap;

public class HwSecurityService extends SystemService {
    private static final int DEFAULT_PLUGIN_ID = 0;
    private static final int DEVICE_SECURE_DIAGNOSE_ID = 2;
    private static final int DEVICE_USAGE_PLUGIN_ID = 1;
    private static final int DYNAMIC_PLUGIN_FLAG = 0;
    private static final boolean HW_DEBUG;
    private static final int IFAA_PLUGIN_ID = 3;
    private static final String MANAGE_USE_SECURITY = "com.huawei.permission.MANAGE_USE_SECURITY";
    private static final int RESIDENT_PLUGIN_FLAG = 1;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "HwSecurityService";
    private static final int TRUSTCIRCLE_PLUGIN_ID = 5;
    private static final int TRUSTSPACE_PLUGIN_ID = 4;
    private Context mContext;
    private ArrayMap<Integer, HwSecurityPluginObj> mMapPlugins;

    public interface IHwPluginRef {
        void bind(IBinder iBinder);

        IHwSecurityPlugin get();

        void set(IHwSecurityPlugin iHwSecurityPlugin);

        boolean unBind(IBinder iBinder);
    }

    public static class HwSecurityDynamicPluginRef implements IHwPluginRef {
        private static final String TAG = "HwSecurityDynamicPluginRef";
        private HashMap<IBinder, Death> mClient;
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
            this.mClient = new HashMap();
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
                    client.linkToDeath(d, HwSecurityService.DYNAMIC_PLUGIN_FLAG);
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
            client.unlinkToDeath((Death) this.mClient.remove(client), HwSecurityService.DYNAMIC_PLUGIN_FLAG);
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
        public Creator mCreator;
        public int mFlag;
        public Object mLock;
        public IHwPluginRef mPluginRef;
        public int mStartupTiming;

        private HwSecurityPluginObj() {
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
            return HwSecurityService.HW_DEBUG;
        }
    }

    private final class HwSecurityServiceWrapper extends Stub {
        private HwSecurityServiceWrapper() {
        }

        public IBinder querySecurityInterface(int pluginId) {
            return HwSecurityService.this.queryInterface(pluginId);
        }

        public void unBind(int pluginId, IBinder client) {
            HwSecurityService.this.unBindDynamicPlugin(pluginId, client, HwSecurityService.HW_DEBUG);
        }

        public IBinder bind(int pluginId, IBinder client) {
            return HwSecurityService.this.bindDynamicPlugin(pluginId, client);
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, TRUSTSPACE_PLUGIN_ID) : HW_DEBUG : true;
        HW_DEBUG = isLoggable;
    }

    public HwSecurityService(Context context) {
        super(context);
        this.mMapPlugins = new ArrayMap();
        this.mContext = context;
    }

    public void onStart() {
        if (HW_DEBUG) {
            Slog.d(TAG, "Start HwSecurityService");
        }
        publishBinderService(SECURITY_SERVICE, new HwSecurityServiceWrapper());
        registerPlugin(RESIDENT_PLUGIN_FLAG, RESIDENT_PLUGIN_FLAG, IOTController.TYPE_MASTER, HwDeviceUsagePlugin.CREATOR, null);
        registerPlugin(DEVICE_SECURE_DIAGNOSE_ID, RESIDENT_PLUGIN_FLAG, HwActivityManagerService.SERVICE_ADJ, HwSecurityDiagnosePlugin.CREATOR, null);
        registerPlugin(IFAA_PLUGIN_ID, DYNAMIC_PLUGIN_FLAG, IOTController.TYPE_MASTER, IFAAPlugin.CREATOR, IFAAPlugin.BINDLOCK);
        Slog.d(TAG, "is add IFAAPlugin");
        registerPlugin(TRUSTSPACE_PLUGIN_ID, RESIDENT_PLUGIN_FLAG, HwActivityManagerService.SERVICE_ADJ, TrustSpaceManagerService.CREATOR, null);
        registerPlugin(TRUSTCIRCLE_PLUGIN_ID, DYNAMIC_PLUGIN_FLAG, IOTController.TYPE_MASTER, TrustCirclePlugin.CREATOR, TrustCirclePlugin.BINDLOCK);
    }

    public void onBootPhase(int phase) {
        startResidentPlugin(phase);
    }

    private void startResidentPlugin(int startupTiming) {
        if (!this.mMapPlugins.isEmpty()) {
            for (Integer intValue : this.mMapPlugins.keySet()) {
                HwSecurityPluginObj obj = (HwSecurityPluginObj) this.mMapPlugins.get(Integer.valueOf(intValue.intValue()));
                if (obj.mStartupTiming == startupTiming && obj.mFlag == RESIDENT_PLUGIN_FLAG) {
                    IHwSecurityPlugin plugIn = obj.mCreator.createPlugin(this.mContext);
                    if (HW_DEBUG) {
                        Slog.d(TAG, "createPlugin");
                    }
                    obj.mPluginRef.set(plugIn);
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
    }

    private void checkPluginPermission(HwSecurityPluginObj obj) {
        if (obj != null && obj.mCreator != null) {
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
        if (client == null || this.mMapPlugins == null || !this.mMapPlugins.containsKey(Integer.valueOf(pluginId))) {
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
        if (this.mMapPlugins == null || !this.mMapPlugins.containsKey(Integer.valueOf(pluginId))) {
            Slog.e(TAG, "not find this Resident Plugin");
            return null;
        }
        HwSecurityPluginObj obj = (HwSecurityPluginObj) this.mMapPlugins.get(Integer.valueOf(pluginId));
        if (obj == null || obj.mFlag != RESIDENT_PLUGIN_FLAG) {
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

    private void registerPlugin(int pluginId, int flag, int starttupTiming, Creator creator, Object lockObj) {
        if (HW_DEBUG) {
            Slog.d(TAG, "addPlugin");
        }
        if (creator != null) {
            HwSecurityPluginObj obj = new HwSecurityPluginObj();
            obj.mFlag = flag;
            obj.mCreator = creator;
            obj.mLock = lockObj;
            obj.mStartupTiming = starttupTiming;
            if (RESIDENT_PLUGIN_FLAG == flag) {
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
        if (this.mMapPlugins != null && this.mMapPlugins.containsKey(Integer.valueOf(pluginId))) {
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

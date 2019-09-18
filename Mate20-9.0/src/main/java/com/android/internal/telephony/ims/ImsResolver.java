package com.android.internal.telephony.ims;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.ims.aidl.IImsConfig;
import android.telephony.ims.aidl.IImsMmTelFeature;
import android.telephony.ims.aidl.IImsRcsFeature;
import android.telephony.ims.aidl.IImsRegistration;
import android.telephony.ims.stub.ImsFeatureConfiguration;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.ims.internal.IImsServiceFeatureCallback;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.SomeArgs;
import com.android.internal.telephony.ims.ImsResolver;
import com.android.internal.telephony.ims.ImsServiceController;
import com.android.internal.telephony.ims.ImsServiceFeatureQueryManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImsResolver implements ImsServiceController.ImsServiceControllerCallbacks {
    private static final int DELAY_DYNAMIC_QUERY_MS = 5000;
    private static final int HANDLER_ADD_PACKAGE = 0;
    private static final int HANDLER_CONFIG_CHANGED = 2;
    private static final int HANDLER_DYNAMIC_FEATURE_CHANGE = 4;
    private static final int HANDLER_OVERRIDE_IMS_SERVICE_CONFIG = 5;
    private static final int HANDLER_REMOVE_PACKAGE = 1;
    private static final int HANDLER_START_DYNAMIC_FEATURE_QUERY = 3;
    public static final String METADATA_EMERGENCY_MMTEL_FEATURE = "android.telephony.ims.EMERGENCY_MMTEL_FEATURE";
    public static final String METADATA_MMTEL_FEATURE = "android.telephony.ims.MMTEL_FEATURE";
    private static final String METADATA_OVERRIDE_PERM_CHECK = "override_bind_check";
    public static final String METADATA_RCS_FEATURE = "android.telephony.ims.RCS_FEATURE";
    private static final String TAG = "ImsResolver";
    private Map<ComponentName, ImsServiceController> mActiveControllers = new HashMap();
    private BroadcastReceiver mAppChangedReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Removed duplicated region for block: B:22:0x0053 A[RETURN] */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x0054  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0062  */
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            String packageName = intent.getData().getSchemeSpecificPart();
            int hashCode = action.hashCode();
            if (hashCode != -810471698) {
                if (hashCode != 172491798) {
                    if (hashCode != 525384130) {
                        if (hashCode == 1544582882 && action.equals("android.intent.action.PACKAGE_ADDED")) {
                            c = 0;
                            switch (c) {
                                case 0:
                                case 1:
                                case 2:
                                    ImsResolver.this.mHandler.obtainMessage(0, packageName).sendToTarget();
                                    break;
                                case 3:
                                    ImsResolver.this.mHandler.obtainMessage(1, packageName).sendToTarget();
                                    break;
                                default:
                                    return;
                            }
                        }
                    } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                        c = 3;
                        switch (c) {
                            case 0:
                            case 1:
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                    }
                } else if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                    c = 2;
                    switch (c) {
                        case 0:
                        case 1:
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            } else if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                c = 1;
                switch (c) {
                    case 0:
                    case 1:
                    case 2:
                        break;
                    case 3:
                        break;
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                case 1:
                case 2:
                    break;
                case 3:
                    break;
            }
        }
    };
    private List<SparseArray<ImsServiceController>> mBoundImsServicesByFeature;
    private final Object mBoundServicesLock = new Object();
    private final CarrierConfigManager mCarrierConfigManager;
    private String[] mCarrierServices;
    private BroadcastReceiver mConfigChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int slotId = intent.getIntExtra("android.telephony.extra.SLOT_INDEX", -1);
            if (slotId == -1) {
                Log.i(ImsResolver.TAG, "Received SIM change for invalid slot id.");
                return;
            }
            Log.i(ImsResolver.TAG, "Received Carrier Config Changed for SlotId: " + slotId);
            ImsResolver.this.mHandler.obtainMessage(2, Integer.valueOf(slotId)).sendToTarget();
        }
    };
    private final Context mContext;
    private String mDeviceService;
    private ImsServiceFeatureQueryManager.Listener mDynamicQueryListener = new ImsServiceFeatureQueryManager.Listener() {
        public void onComplete(ComponentName name, Set<ImsFeatureConfiguration.FeatureSlotPair> features) {
            Log.d(ImsResolver.TAG, "onComplete called for name: " + name + "features:" + ImsResolver.this.printFeatures(features));
            ImsResolver.this.handleFeaturesChanged(name, features);
        }

        public void onError(ComponentName name) {
            Log.w(ImsResolver.TAG, "onError: " + name + "returned with an error result");
            ImsResolver.this.scheduleQueryForFeatures(name, 5000);
        }
    };
    private ImsDynamicQueryManagerFactory mDynamicQueryManagerFactory = $$Lambda$WamP7BPq0j01TgYE3GvUqU3brs.INSTANCE;
    private ImsServiceFeatureQueryManager mFeatureQueryManager;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        public final boolean handleMessage(Message message) {
            return ImsResolver.lambda$new$0(ImsResolver.this, message);
        }
    });
    private ImsServiceControllerFactory mImsServiceControllerFactory = new ImsServiceControllerFactory() {
        public String getServiceInterface() {
            return "android.telephony.ims.ImsService";
        }

        public ImsServiceController create(Context context, ComponentName componentName, ImsServiceController.ImsServiceControllerCallbacks callbacks) {
            return new ImsServiceController(context, componentName, callbacks);
        }
    };
    private ImsServiceControllerFactory mImsServiceControllerFactoryCompat = new ImsServiceControllerFactory() {
        public String getServiceInterface() {
            return "android.telephony.ims.compat.ImsService";
        }

        public ImsServiceController create(Context context, ComponentName componentName, ImsServiceController.ImsServiceControllerCallbacks callbacks) {
            return new ImsServiceControllerCompat(context, componentName, callbacks);
        }
    };
    private ImsServiceControllerFactory mImsServiceControllerFactoryStaticBindingCompat = new ImsServiceControllerFactory() {
        public String getServiceInterface() {
            return null;
        }

        public ImsServiceController create(Context context, ComponentName componentName, ImsServiceController.ImsServiceControllerCallbacks callbacks) {
            return new ImsServiceControllerStaticCompat(context, componentName, callbacks);
        }
    };
    private Map<ComponentName, ImsServiceInfo> mInstalledServicesCache = new HashMap();
    private final boolean mIsDynamicBinding;
    private final int mNumSlots;
    private final ComponentName mStaticComponent;
    private SubscriptionManagerProxy mSubscriptionManagerProxy = new SubscriptionManagerProxy() {
        public int getSubId(int slotId) {
            int[] subIds = SubscriptionManager.getSubId(slotId);
            if (subIds != null) {
                return subIds[0];
            }
            return -1;
        }

        public int getSlotIndex(int subId) {
            return SubscriptionManager.getSlotIndex(subId);
        }
    };

    @VisibleForTesting
    public interface ImsDynamicQueryManagerFactory {
        ImsServiceFeatureQueryManager create(Context context, ImsServiceFeatureQueryManager.Listener listener);
    }

    @VisibleForTesting
    public interface ImsServiceControllerFactory {
        ImsServiceController create(Context context, ComponentName componentName, ImsServiceController.ImsServiceControllerCallbacks imsServiceControllerCallbacks);

        String getServiceInterface();
    }

    @VisibleForTesting
    public static class ImsServiceInfo {
        public ImsServiceControllerFactory controllerFactory;
        public boolean featureFromMetadata = true;
        private final int mNumSlots;
        private final HashSet<ImsFeatureConfiguration.FeatureSlotPair> mSupportedFeatures;
        public ComponentName name;

        public ImsServiceInfo(int numSlots) {
            this.mNumSlots = numSlots;
            this.mSupportedFeatures = new HashSet<>();
        }

        /* access modifiers changed from: package-private */
        public void addFeatureForAllSlots(int feature) {
            for (int i = 0; i < this.mNumSlots; i++) {
                this.mSupportedFeatures.add(new ImsFeatureConfiguration.FeatureSlotPair(i, feature));
            }
        }

        /* access modifiers changed from: package-private */
        public void replaceFeatures(Set<ImsFeatureConfiguration.FeatureSlotPair> newFeatures) {
            this.mSupportedFeatures.clear();
            this.mSupportedFeatures.addAll(newFeatures);
        }

        @VisibleForTesting
        public HashSet<ImsFeatureConfiguration.FeatureSlotPair> getSupportedFeatures() {
            return this.mSupportedFeatures;
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ImsServiceInfo that = (ImsServiceInfo) o;
            if (this.name == null ? that.name != null : !this.name.equals(that.name)) {
                return false;
            }
            if (!this.mSupportedFeatures.equals(that.mSupportedFeatures)) {
                return false;
            }
            if (this.controllerFactory != null) {
                z = this.controllerFactory.equals(that.controllerFactory);
            } else if (that.controllerFactory != null) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int i = 0;
            int hashCode = 31 * (this.name != null ? this.name.hashCode() : 0);
            if (this.controllerFactory != null) {
                i = this.controllerFactory.hashCode();
            }
            return hashCode + i;
        }

        public String toString() {
            StringBuilder res = new StringBuilder();
            res.append("[ImsServiceInfo] name=");
            res.append(this.name);
            res.append(", supportedFeatures=[ ");
            Iterator<ImsFeatureConfiguration.FeatureSlotPair> it = this.mSupportedFeatures.iterator();
            while (it.hasNext()) {
                ImsFeatureConfiguration.FeatureSlotPair feature = it.next();
                res.append("(");
                res.append(feature.slotId);
                res.append(",");
                res.append(feature.featureType);
                res.append(") ");
            }
            return res.toString();
        }
    }

    @VisibleForTesting
    public interface SubscriptionManagerProxy {
        int getSlotIndex(int i);

        int getSubId(int i);
    }

    public static /* synthetic */ SparseArray lambda$WVd6ghNMbVDukmkxia3ZwNeZzEY() {
        return new SparseArray();
    }

    public static /* synthetic */ boolean lambda$new$0(ImsResolver imsResolver, Message msg) {
        boolean isCarrierImsService = false;
        switch (msg.what) {
            case 0:
                imsResolver.maybeAddedImsService((String) msg.obj);
                break;
            case 1:
                imsResolver.maybeRemovedImsService((String) msg.obj);
                break;
            case 2:
                imsResolver.carrierConfigChanged(((Integer) msg.obj).intValue());
                break;
            case 3:
                imsResolver.startDynamicQuery((ImsServiceInfo) msg.obj);
                break;
            case 4:
                SomeArgs args = (SomeArgs) msg.obj;
                args.recycle();
                imsResolver.dynamicQueryComplete((ComponentName) args.arg1, (Set) args.arg2);
                break;
            case 5:
                int slotId = msg.arg1;
                if (msg.arg2 == 1) {
                    isCarrierImsService = true;
                }
                String packageName = (String) msg.obj;
                if (!isCarrierImsService) {
                    Log.i(TAG, "overriding device ImsService -  packageName=" + packageName);
                    if (packageName == null || packageName.isEmpty()) {
                        imsResolver.unbindImsService(imsResolver.getImsServiceInfoFromCache(imsResolver.mDeviceService));
                    }
                    imsResolver.mDeviceService = packageName;
                    ImsServiceInfo deviceInfo = imsResolver.getImsServiceInfoFromCache(imsResolver.mDeviceService);
                    if (deviceInfo != null) {
                        Log.i(TAG, "overriding device ImsService - deviceInfo.featureFromMetadata=" + deviceInfo.featureFromMetadata);
                        if (!deviceInfo.featureFromMetadata) {
                            imsResolver.scheduleQueryForFeatures(deviceInfo);
                            break;
                        } else {
                            imsResolver.bindImsService(deviceInfo);
                            break;
                        }
                    }
                } else {
                    Log.i(TAG, "overriding carrier ImsService - slot=" + slotId + " packageName=" + packageName);
                    imsResolver.maybeRebindService(slotId, packageName);
                    break;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    public ImsResolver(Context context, String defaultImsPackageName, int numSlots, boolean isDynamicBinding) {
        this.mContext = context;
        this.mDeviceService = defaultImsPackageName;
        this.mNumSlots = numSlots;
        this.mIsDynamicBinding = isDynamicBinding;
        this.mStaticComponent = new ComponentName(this.mContext, ImsResolver.class);
        if (!this.mIsDynamicBinding) {
            Log.i(TAG, "ImsResolver initialized with static binding.");
            this.mDeviceService = this.mStaticComponent.getPackageName();
        }
        this.mCarrierConfigManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        this.mCarrierServices = new String[numSlots];
        this.mBoundImsServicesByFeature = (List) Stream.generate($$Lambda$ImsResolver$WVd6ghNMbVDukmkxia3ZwNeZzEY.INSTANCE).limit((long) this.mNumSlots).collect(Collectors.toList());
        if (this.mIsDynamicBinding) {
            IntentFilter appChangedFilter = new IntentFilter();
            appChangedFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            appChangedFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            appChangedFilter.addAction("android.intent.action.PACKAGE_ADDED");
            appChangedFilter.addDataScheme("package");
            context.registerReceiverAsUser(this.mAppChangedReceiver, UserHandle.ALL, appChangedFilter, null, null);
            context.registerReceiver(this.mConfigChangedReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
        }
    }

    @VisibleForTesting
    public void setSubscriptionManagerProxy(SubscriptionManagerProxy proxy) {
        this.mSubscriptionManagerProxy = proxy;
    }

    @VisibleForTesting
    public void setImsServiceControllerFactory(ImsServiceControllerFactory factory) {
        this.mImsServiceControllerFactory = factory;
    }

    @VisibleForTesting
    public Handler getHandler() {
        return this.mHandler;
    }

    @VisibleForTesting
    public void setImsDynamicQueryManagerFactory(ImsDynamicQueryManagerFactory m) {
        this.mDynamicQueryManagerFactory = m;
    }

    public void initPopulateCacheAndStartBind() {
        Log.i(TAG, "Initializing cache and binding.");
        this.mFeatureQueryManager = this.mDynamicQueryManagerFactory.create(this.mContext, this.mDynamicQueryListener);
        this.mHandler.obtainMessage(2, -1).sendToTarget();
        this.mHandler.obtainMessage(0, null).sendToTarget();
    }

    public void enableIms(int slotId) {
        SparseArray<ImsServiceController> controllers = getImsServiceControllers(slotId);
        if (controllers != null) {
            for (int i = 0; i < controllers.size(); i++) {
                controllers.get(controllers.keyAt(i)).enableIms(slotId);
            }
        }
    }

    public void disableIms(int slotId) {
        SparseArray<ImsServiceController> controllers = getImsServiceControllers(slotId);
        if (controllers != null) {
            for (int i = 0; i < controllers.size(); i++) {
                controllers.get(controllers.keyAt(i)).disableIms(slotId);
            }
        }
    }

    public IImsMmTelFeature getMmTelFeatureAndListen(int slotId, IImsServiceFeatureCallback callback) {
        Log.i(TAG, "getMmTelFeatureAndListen - slot: " + slotId + " callback: " + callback);
        ImsServiceController controller = getImsServiceControllerAndListen(slotId, 1, callback);
        if (controller != null) {
            return controller.getMmTelFeature(slotId);
        }
        return null;
    }

    public IImsRcsFeature getRcsFeatureAndListen(int slotId, IImsServiceFeatureCallback callback) {
        ImsServiceController controller = getImsServiceControllerAndListen(slotId, 2, callback);
        if (controller != null) {
            return controller.getRcsFeature(slotId);
        }
        return null;
    }

    public IImsRegistration getImsRegistration(int slotId, int feature) throws RemoteException {
        ImsServiceController controller = getImsServiceController(slotId, feature);
        if (controller != null) {
            return controller.getRegistration(slotId);
        }
        return null;
    }

    public IImsConfig getImsConfig(int slotId, int feature) throws RemoteException {
        ImsServiceController controller = getImsServiceController(slotId, feature);
        if (controller != null) {
            return controller.getConfig(slotId);
        }
        return null;
    }

    @VisibleForTesting
    public ImsServiceController getImsServiceController(int slotId, int feature) {
        if (slotId < 0 || slotId >= this.mNumSlots) {
            return null;
        }
        synchronized (this.mBoundServicesLock) {
            SparseArray<ImsServiceController> services = this.mBoundImsServicesByFeature.get(slotId);
            if (services == null) {
                return null;
            }
            ImsServiceController controller = services.get(feature);
            return controller;
        }
    }

    private SparseArray<ImsServiceController> getImsServiceControllers(int slotId) {
        if (slotId < 0 || slotId >= this.mNumSlots) {
            return null;
        }
        synchronized (this.mBoundServicesLock) {
            SparseArray<ImsServiceController> services = this.mBoundImsServicesByFeature.get(slotId);
            if (services == null) {
                return null;
            }
            return services;
        }
    }

    @VisibleForTesting
    public ImsServiceController getImsServiceControllerAndListen(int slotId, int feature, IImsServiceFeatureCallback callback) {
        ImsServiceController controller = getImsServiceController(slotId, feature);
        if (controller == null) {
            return null;
        }
        controller.addImsServiceFeatureCallback(callback);
        return controller;
    }

    public boolean overrideImsServiceConfiguration(int slotId, boolean isCarrierService, String packageName) {
        if (slotId < 0 || slotId >= this.mNumSlots) {
            Log.w(TAG, "overrideImsServiceConfiguration: invalid slotId!");
            return false;
        } else if (packageName == null) {
            Log.w(TAG, "overrideImsServiceConfiguration: null packageName!");
            return false;
        } else {
            Message.obtain(this.mHandler, 5, slotId, (int) isCarrierService, packageName).sendToTarget();
            return true;
        }
    }

    public String getImsServiceConfiguration(int slotId, boolean isCarrierService) {
        if (slotId < 0 || slotId >= this.mNumSlots) {
            Log.w(TAG, "getImsServiceConfiguration: invalid slotId!");
            return "";
        }
        return isCarrierService ? this.mCarrierServices[slotId] : this.mDeviceService;
    }

    private void putImsController(int slotId, int feature, ImsServiceController controller) {
        if (slotId < 0 || slotId >= this.mNumSlots || feature <= -1 || feature >= 3) {
            Log.w(TAG, "putImsController received invalid parameters - slot: " + slotId + ", feature: " + feature);
            return;
        }
        synchronized (this.mBoundServicesLock) {
            SparseArray<ImsServiceController> services = this.mBoundImsServicesByFeature.get(slotId);
            if (services == null) {
                services = new SparseArray<>();
                this.mBoundImsServicesByFeature.add(slotId, services);
            }
            Log.i(TAG, "ImsServiceController added on slot: " + slotId + " with feature: " + feature + " using package: " + controller.getComponentName());
            services.put(feature, controller);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0053, code lost:
        return r0;
     */
    private ImsServiceController removeImsController(int slotId, int feature) {
        if (slotId < 0 || slotId >= this.mNumSlots || feature <= -1 || feature >= 3) {
            Log.w(TAG, "removeImsController received invalid parameters - slot: " + slotId + ", feature: " + feature);
            return null;
        }
        synchronized (this.mBoundServicesLock) {
            SparseArray<ImsServiceController> services = this.mBoundImsServicesByFeature.get(slotId);
            if (services == null) {
                return null;
            }
            ImsServiceController c = services.get(feature, null);
            if (c != null) {
                Log.i(TAG, "ImsServiceController removed on slot: " + slotId + " with feature: " + feature + " using package: " + c.getComponentName());
                services.remove(feature);
            }
        }
    }

    private void maybeAddedImsService(String packageName) {
        Log.d(TAG, "maybeAddedImsService, packageName: " + packageName);
        List<ImsServiceInfo> infos = getImsServiceInfo(packageName);
        List<ImsServiceInfo> newlyAddedInfos = new ArrayList<>();
        for (ImsServiceInfo info : infos) {
            ImsServiceInfo match = getInfoByComponentName(this.mInstalledServicesCache, info.name);
            if (match == null) {
                Log.i(TAG, "Adding newly added ImsService to cache: " + info.name);
                this.mInstalledServicesCache.put(info.name, info);
                if (info.featureFromMetadata) {
                    newlyAddedInfos.add(info);
                } else {
                    scheduleQueryForFeatures(info);
                }
            } else if (info.featureFromMetadata) {
                Log.i(TAG, "Updating features in cached ImsService: " + info.name);
                Log.d(TAG, "Updating features - Old features: " + match + " new features: " + info);
                match.replaceFeatures(info.getSupportedFeatures());
                updateImsServiceFeatures(info);
            } else {
                scheduleQueryForFeatures(info);
            }
        }
        for (ImsServiceInfo info2 : newlyAddedInfos) {
            if (isActiveCarrierService(info2)) {
                bindImsService(info2);
                updateImsServiceFeatures(getImsServiceInfoFromCache(this.mDeviceService));
            } else if (isDeviceService(info2)) {
                bindImsService(info2);
            }
        }
    }

    private boolean maybeRemovedImsService(String packageName) {
        ImsServiceInfo match = getInfoByPackageName(this.mInstalledServicesCache, packageName);
        if (match == null) {
            return false;
        }
        this.mInstalledServicesCache.remove(match.name);
        Log.i(TAG, "Removing ImsService: " + match.name);
        unbindImsService(match);
        updateImsServiceFeatures(getImsServiceInfoFromCache(this.mDeviceService));
        return true;
    }

    private boolean isActiveCarrierService(ImsServiceInfo info) {
        for (int i = 0; i < this.mNumSlots; i++) {
            if (TextUtils.equals(this.mCarrierServices[i], info.name.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isDeviceService(ImsServiceInfo info) {
        return TextUtils.equals(this.mDeviceService, info.name.getPackageName());
    }

    private int getSlotForActiveCarrierService(ImsServiceInfo info) {
        for (int i = 0; i < this.mNumSlots; i++) {
            if (TextUtils.equals(this.mCarrierServices[i], info.name.getPackageName())) {
                return i;
            }
        }
        return -1;
    }

    private ImsServiceController getControllerByServiceInfo(Map<ComponentName, ImsServiceController> searchMap, ImsServiceInfo matchValue) {
        return searchMap.values().stream().filter(new Predicate() {
            public final boolean test(Object obj) {
                return Objects.equals(((ImsServiceController) obj).getComponentName(), ImsResolver.ImsServiceInfo.this.name);
            }
        }).findFirst().orElse(null);
    }

    private ImsServiceInfo getInfoByPackageName(Map<ComponentName, ImsServiceInfo> searchMap, String matchValue) {
        return searchMap.values().stream().filter(new Predicate(matchValue) {
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return Objects.equals(((ImsResolver.ImsServiceInfo) obj).name.getPackageName(), this.f$0);
            }
        }).findFirst().orElse(null);
    }

    private ImsServiceInfo getInfoByComponentName(Map<ComponentName, ImsServiceInfo> searchMap, ComponentName matchValue) {
        return searchMap.get(matchValue);
    }

    private void updateImsServiceFeatures(ImsServiceInfo newInfo) {
        if (newInfo != null) {
            ImsServiceController controller = getControllerByServiceInfo(this.mActiveControllers, newInfo);
            HashSet<ImsFeatureConfiguration.FeatureSlotPair> features = calculateFeaturesToCreate(newInfo);
            if (shouldFeaturesCauseBind(features)) {
                if (controller != null) {
                    try {
                        Log.i(TAG, "Updating features for ImsService: " + controller.getComponentName());
                        Log.d(TAG, "Updating Features - New Features: " + features);
                        controller.changeImsServiceFeatures(features);
                    } catch (RemoteException e) {
                        Log.e(TAG, "updateImsServiceFeatures: Remote Exception: " + e.getMessage());
                    }
                } else {
                    Log.i(TAG, "updateImsServiceFeatures: unbound with active features, rebinding");
                    bindImsServiceWithFeatures(newInfo, features);
                }
                if (isActiveCarrierService(newInfo) && !TextUtils.equals(newInfo.name.getPackageName(), this.mDeviceService)) {
                    Log.i(TAG, "Updating device default");
                    updateImsServiceFeatures(getImsServiceInfoFromCache(this.mDeviceService));
                }
            } else if (controller != null) {
                Log.i(TAG, "Unbinding: features = 0 for ImsService: " + controller.getComponentName());
                unbindImsService(newInfo);
            }
        }
    }

    private void bindImsService(ImsServiceInfo info) {
        if (info != null) {
            bindImsServiceWithFeatures(info, calculateFeaturesToCreate(info));
        }
    }

    private void bindImsServiceWithFeatures(ImsServiceInfo info, HashSet<ImsFeatureConfiguration.FeatureSlotPair> features) {
        if (shouldFeaturesCauseBind(features)) {
            ImsServiceController controller = getControllerByServiceInfo(this.mActiveControllers, info);
            if (controller != null) {
                Log.i(TAG, "ImsService connection exists, updating features " + features);
                try {
                    controller.changeImsServiceFeatures(features);
                } catch (RemoteException e) {
                    Log.w(TAG, "bindImsService: error=" + e.getMessage());
                }
            } else {
                controller = info.controllerFactory.create(this.mContext, info.name, this);
                Log.i(TAG, "Binding ImsService: " + controller.getComponentName() + " with features: " + features);
                controller.bind(features);
            }
            this.mActiveControllers.put(info.name, controller);
        }
    }

    private void unbindImsService(ImsServiceInfo info) {
        if (info != null) {
            ImsServiceController controller = getControllerByServiceInfo(this.mActiveControllers, info);
            if (controller != null) {
                try {
                    Log.i(TAG, "Unbinding ImsService: " + controller.getComponentName());
                    controller.unbind();
                } catch (RemoteException e) {
                    Log.e(TAG, "unbindImsService: Remote Exception: " + e.getMessage());
                }
                this.mActiveControllers.remove(info.name);
            }
        }
    }

    private HashSet<ImsFeatureConfiguration.FeatureSlotPair> calculateFeaturesToCreate(ImsServiceInfo info) {
        HashSet<ImsFeatureConfiguration.FeatureSlotPair> imsFeaturesBySlot = new HashSet<>();
        int slotId = getSlotForActiveCarrierService(info);
        if (slotId != -1) {
            imsFeaturesBySlot.addAll((Collection) info.getSupportedFeatures().stream().filter(new Predicate(slotId) {
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean test(Object obj) {
                    return ImsResolver.lambda$calculateFeaturesToCreate$3(this.f$0, (ImsFeatureConfiguration.FeatureSlotPair) obj);
                }
            }).collect(Collectors.toList()));
        } else if (isDeviceService(info)) {
            for (int i = 0; i < this.mNumSlots; i++) {
                int currSlotId = i;
                ImsServiceInfo carrierImsInfo = getImsServiceInfoFromCache(this.mCarrierServices[i]);
                if (carrierImsInfo == null) {
                    imsFeaturesBySlot.addAll((Collection) info.getSupportedFeatures().stream().filter(new Predicate(currSlotId) {
                        private final /* synthetic */ int f$0;

                        {
                            this.f$0 = r1;
                        }

                        public final boolean test(Object obj) {
                            return ImsResolver.lambda$calculateFeaturesToCreate$4(this.f$0, (ImsFeatureConfiguration.FeatureSlotPair) obj);
                        }
                    }).collect(Collectors.toList()));
                } else {
                    HashSet<ImsFeatureConfiguration.FeatureSlotPair> deviceFeatures = new HashSet<>(info.getSupportedFeatures());
                    deviceFeatures.removeAll(carrierImsInfo.getSupportedFeatures());
                    imsFeaturesBySlot.addAll((Collection) deviceFeatures.stream().filter(new Predicate(currSlotId) {
                        private final /* synthetic */ int f$0;

                        {
                            this.f$0 = r1;
                        }

                        public final boolean test(Object obj) {
                            return ImsResolver.lambda$calculateFeaturesToCreate$5(this.f$0, (ImsFeatureConfiguration.FeatureSlotPair) obj);
                        }
                    }).collect(Collectors.toList()));
                }
            }
        }
        return imsFeaturesBySlot;
    }

    static /* synthetic */ boolean lambda$calculateFeaturesToCreate$3(int slotId, ImsFeatureConfiguration.FeatureSlotPair feature) {
        return slotId == feature.slotId;
    }

    static /* synthetic */ boolean lambda$calculateFeaturesToCreate$4(int currSlotId, ImsFeatureConfiguration.FeatureSlotPair feature) {
        return currSlotId == feature.slotId;
    }

    static /* synthetic */ boolean lambda$calculateFeaturesToCreate$5(int currSlotId, ImsFeatureConfiguration.FeatureSlotPair feature) {
        return currSlotId == feature.slotId;
    }

    public void imsServiceFeatureCreated(int slotId, int feature, ImsServiceController controller) {
        putImsController(slotId, feature, controller);
    }

    public void imsServiceFeatureRemoved(int slotId, int feature, ImsServiceController controller) {
        removeImsController(slotId, feature);
    }

    public void imsServiceFeaturesChanged(ImsFeatureConfiguration config, ImsServiceController controller) {
        if (controller != null && config != null) {
            Log.i(TAG, "imsServiceFeaturesChanged: config=" + config.getServiceFeatures() + ", ComponentName=" + controller.getComponentName());
            handleFeaturesChanged(controller.getComponentName(), config.getServiceFeatures());
        }
    }

    private boolean shouldFeaturesCauseBind(HashSet<ImsFeatureConfiguration.FeatureSlotPair> features) {
        return features.stream().filter($$Lambda$ImsResolver$SIkPixrqGLIKusUJIKu6S5BBs.INSTANCE).count() > 0;
    }

    static /* synthetic */ boolean lambda$shouldFeaturesCauseBind$6(ImsFeatureConfiguration.FeatureSlotPair f) {
        return f.featureType != 0;
    }

    private void maybeRebindService(int slotId, String newPackageName) {
        if (slotId <= -1) {
            for (int i = 0; i < this.mNumSlots; i++) {
                updateBoundCarrierServices(i, newPackageName);
            }
            return;
        }
        updateBoundCarrierServices(slotId, newPackageName);
    }

    private void carrierConfigChanged(int slotId) {
        PersistableBundle config = this.mCarrierConfigManager.getConfigForSubId(this.mSubscriptionManagerProxy.getSubId(slotId));
        if (config != null) {
            maybeRebindService(slotId, config.getString("config_ims_package_override_string", null));
        } else {
            Log.w(TAG, "carrierConfigChanged: CarrierConfig is null!");
        }
    }

    private void updateBoundCarrierServices(int slotId, String newPackageName) {
        if (slotId > -1 && slotId < this.mNumSlots) {
            String oldPackageName = this.mCarrierServices[slotId];
            this.mCarrierServices[slotId] = newPackageName;
            if (!TextUtils.equals(newPackageName, oldPackageName)) {
                Log.i(TAG, "Carrier Config updated, binding new ImsService");
                unbindImsService(getImsServiceInfoFromCache(oldPackageName));
                ImsServiceInfo newInfo = getImsServiceInfoFromCache(newPackageName);
                if (newInfo == null || newInfo.featureFromMetadata) {
                    bindImsService(newInfo);
                    updateImsServiceFeatures(getImsServiceInfoFromCache(this.mDeviceService));
                    return;
                }
                scheduleQueryForFeatures(newInfo);
            }
        }
    }

    private void scheduleQueryForFeatures(ImsServiceInfo service, int delayMs) {
        if (isDeviceService(service) || getSlotForActiveCarrierService(service) != -1) {
            Message msg = Message.obtain(this.mHandler, 3, service);
            if (this.mHandler.hasMessages(3, service)) {
                Log.d(TAG, "scheduleQueryForFeatures: dynamic query for " + service.name + " already scheduled");
                return;
            }
            Log.d(TAG, "scheduleQueryForFeatures: starting dynamic query for " + service.name + " in " + delayMs + "ms.");
            this.mHandler.sendMessageDelayed(msg, (long) delayMs);
            return;
        }
        Log.i(TAG, "scheduleQueryForFeatures: skipping query for ImsService that is not set as carrier/device ImsService.");
    }

    /* access modifiers changed from: private */
    public void scheduleQueryForFeatures(ComponentName name, int delayMs) {
        ImsServiceInfo service = getImsServiceInfoFromCache(name.getPackageName());
        if (service == null) {
            Log.w(TAG, "scheduleQueryForFeatures: Couldn't find cached info for name: " + name);
            return;
        }
        scheduleQueryForFeatures(service, delayMs);
    }

    private void scheduleQueryForFeatures(ImsServiceInfo service) {
        scheduleQueryForFeatures(service, 0);
    }

    /* access modifiers changed from: private */
    public void handleFeaturesChanged(ComponentName name, Set<ImsFeatureConfiguration.FeatureSlotPair> features) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = name;
        args.arg2 = features;
        this.mHandler.obtainMessage(4, args).sendToTarget();
    }

    private void startDynamicQuery(ImsServiceInfo service) {
        if (!this.mFeatureQueryManager.startQuery(service.name, service.controllerFactory.getServiceInterface())) {
            Log.w(TAG, "startDynamicQuery: service could not connect. Retrying after delay.");
            scheduleQueryForFeatures(service, 5000);
            return;
        }
        Log.d(TAG, "startDynamicQuery: Service queried, waiting for response.");
    }

    private void dynamicQueryComplete(ComponentName name, Set<ImsFeatureConfiguration.FeatureSlotPair> features) {
        ImsServiceInfo service = getImsServiceInfoFromCache(name.getPackageName());
        if (service == null) {
            Log.w(TAG, "handleFeaturesChanged: Couldn't find cached info for name: " + name);
            return;
        }
        service.replaceFeatures(features);
        if (isActiveCarrierService(service)) {
            bindImsService(service);
            updateImsServiceFeatures(getImsServiceInfoFromCache(this.mDeviceService));
        } else if (isDeviceService(service)) {
            bindImsService(service);
        }
    }

    public boolean isResolvingBinding() {
        return this.mHandler.hasMessages(3) || this.mHandler.hasMessages(4) || this.mFeatureQueryManager.isQueryInProgress();
    }

    /* access modifiers changed from: private */
    public String printFeatures(Set<ImsFeatureConfiguration.FeatureSlotPair> features) {
        StringBuilder featureString = new StringBuilder();
        featureString.append("features: [");
        if (features != null) {
            for (ImsFeatureConfiguration.FeatureSlotPair feature : features) {
                featureString.append("{");
                featureString.append(feature.slotId);
                featureString.append(",");
                featureString.append(feature.featureType);
                featureString.append("} ");
            }
            featureString.append("]");
        }
        return featureString.toString();
    }

    @VisibleForTesting
    public ImsServiceInfo getImsServiceInfoFromCache(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        ImsServiceInfo infoFilter = getInfoByPackageName(this.mInstalledServicesCache, packageName);
        if (infoFilter != null) {
            return infoFilter;
        }
        return null;
    }

    private List<ImsServiceInfo> getImsServiceInfo(String packageName) {
        List<ImsServiceInfo> infos = new ArrayList<>();
        if (!this.mIsDynamicBinding) {
            infos.addAll(getStaticImsService());
        } else {
            infos.addAll(searchForImsServices(packageName, this.mImsServiceControllerFactory));
            infos.addAll(searchForImsServices(packageName, this.mImsServiceControllerFactoryCompat));
        }
        return infos;
    }

    private List<ImsServiceInfo> getStaticImsService() {
        List<ImsServiceInfo> infos = new ArrayList<>();
        ImsServiceInfo info = new ImsServiceInfo(this.mNumSlots);
        info.name = this.mStaticComponent;
        info.controllerFactory = this.mImsServiceControllerFactoryStaticBindingCompat;
        info.addFeatureForAllSlots(0);
        info.addFeatureForAllSlots(1);
        infos.add(info);
        return infos;
    }

    private List<ImsServiceInfo> searchForImsServices(String packageName, ImsServiceControllerFactory controllerFactory) {
        List<ImsServiceInfo> infos = new ArrayList<>();
        Intent serviceIntent = new Intent(controllerFactory.getServiceInterface());
        serviceIntent.setPackage(packageName);
        for (ResolveInfo entry : this.mContext.getPackageManager().queryIntentServicesAsUser(serviceIntent, 128, this.mContext.getUserId())) {
            ServiceInfo serviceInfo = entry.serviceInfo;
            if (serviceInfo != null) {
                ImsServiceInfo info = new ImsServiceInfo(this.mNumSlots);
                info.name = new ComponentName(serviceInfo.packageName, serviceInfo.name);
                info.controllerFactory = controllerFactory;
                if (isDeviceService(info) || this.mImsServiceControllerFactoryCompat == controllerFactory) {
                    if (serviceInfo.metaData != null) {
                        if (serviceInfo.metaData.getBoolean(METADATA_EMERGENCY_MMTEL_FEATURE, false)) {
                            info.addFeatureForAllSlots(0);
                        }
                        if (serviceInfo.metaData.getBoolean(METADATA_MMTEL_FEATURE, false)) {
                            info.addFeatureForAllSlots(1);
                        }
                        if (serviceInfo.metaData.getBoolean(METADATA_RCS_FEATURE, false)) {
                            info.addFeatureForAllSlots(2);
                        }
                    }
                    if (this.mImsServiceControllerFactoryCompat != controllerFactory && info.getSupportedFeatures().isEmpty()) {
                        info.featureFromMetadata = false;
                    }
                } else {
                    info.featureFromMetadata = false;
                }
                Log.i(TAG, "service name: " + info.name + ", manifest query: " + info.featureFromMetadata);
                if (TextUtils.equals(serviceInfo.permission, "android.permission.BIND_IMS_SERVICE") || (serviceInfo.metaData != null && serviceInfo.metaData.getBoolean(METADATA_OVERRIDE_PERM_CHECK, false))) {
                    Log.d(TAG, "ImsService (" + serviceIntent + ") added to cache: " + info.name);
                    infos.add(info);
                } else {
                    Log.w(TAG, "ImsService is not protected with BIND_IMS_SERVICE permission: " + info.name);
                }
            }
        }
        return infos;
    }
}

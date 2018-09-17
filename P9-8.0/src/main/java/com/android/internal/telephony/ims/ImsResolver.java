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
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Pair;
import android.util.SparseArray;
import com.android.ims.internal.IImsServiceController;
import com.android.ims.internal.IImsServiceFeatureListener;
import com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg.AnonymousClass4;
import com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg.AnonymousClass5;
import com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg.AnonymousClass6;
import com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg.AnonymousClass7;
import com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg.AnonymousClass8;
import com.android.internal.telephony.ims.ImsServiceController.ImsServiceControllerCallbacks;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImsResolver implements ImsServiceControllerCallbacks {
    private static final int HANDLER_ADD_PACKAGE = 0;
    private static final int HANDLER_CONFIG_CHANGED = 2;
    private static final int HANDLER_REMOVE_PACKAGE = 1;
    public static final String METADATA_EMERGENCY_MMTEL_FEATURE = "android.telephony.ims.EMERGENCY_MMTEL_FEATURE";
    public static final String METADATA_MMTEL_FEATURE = "android.telephony.ims.MMTEL_FEATURE";
    public static final String METADATA_RCS_FEATURE = "android.telephony.ims.RCS_FEATURE";
    public static final String SERVICE_INTERFACE = "android.telephony.ims.ImsService";
    private static final String TAG = "ImsResolver";
    private static final boolean volte = SystemProperties.getBoolean("ro.config.hw_volte_on", false);
    private Set<ImsServiceController> mActiveControllers = new ArraySet();
    private BroadcastReceiver mAppChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String packageName = intent.getData().getSchemeSpecificPart();
            if (action.equals("android.intent.action.PACKAGE_ADDED") || action.equals("android.intent.action.PACKAGE_CHANGED")) {
                ImsResolver.this.mHandler.obtainMessage(0, packageName).sendToTarget();
            } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                ImsResolver.this.mHandler.obtainMessage(1, packageName).sendToTarget();
            }
        }
    };
    private List<SparseArray<ImsServiceController>> mBoundImsServicesByFeature;
    private final Object mBoundServicesLock = new Object();
    private final CarrierConfigManager mCarrierConfigManager;
    private String[] mCarrierServices;
    private BroadcastReceiver mConfigChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int subId = intent.getIntExtra("subscription", -1);
            if (subId == -1) {
                Rlog.i(ImsResolver.TAG, "Received SIM change for invalid sub id.");
                return;
            }
            Rlog.i(ImsResolver.TAG, "Received Carrier Config Changed for SubId: " + subId);
            ImsResolver.this.mHandler.obtainMessage(2, Integer.valueOf(subId)).sendToTarget();
        }
    };
    private final Context mContext;
    private String mDeviceService;
    private Handler mHandler = new Handler(Looper.getMainLooper(), new com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg.AnonymousClass1(this));
    private ImsServiceControllerFactory mImsServiceControllerFactory = new com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg.AnonymousClass2(this);
    private Set<ImsServiceInfo> mInstalledServicesCache = new ArraySet();
    private final int mNumSlots;
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

    public interface ImsServiceControllerFactory {
        ImsServiceController get(Context context, ComponentName componentName);
    }

    public interface SubscriptionManagerProxy {
        int getSlotIndex(int i);

        int getSubId(int i);
    }

    public static class ImsServiceInfo {
        public ComponentName name;
        public Set<Integer> supportedFeatures;

        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ImsServiceInfo that = (ImsServiceInfo) o;
            if (this.name == null ? that.name != null : (this.name.equals(that.name) ^ 1) != 0) {
                return false;
            }
            if (this.supportedFeatures != null) {
                z = this.supportedFeatures.equals(that.supportedFeatures);
            } else if (that.supportedFeatures != null) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return ((this.name != null ? this.name.hashCode() : 0) * 31) + (this.supportedFeatures != null ? this.supportedFeatures.hashCode() : 0);
        }
    }

    /* synthetic */ ImsServiceController lambda$-com_android_internal_telephony_ims_ImsResolver_8131(Context context, ComponentName componentName) {
        return new ImsServiceController(context, componentName, this);
    }

    /* synthetic */ boolean lambda$-com_android_internal_telephony_ims_ImsResolver_8747(Message msg) {
        switch (msg.what) {
            case 0:
                maybeAddedImsService(msg.obj);
                break;
            case 1:
                maybeRemovedImsService((String) msg.obj);
                break;
            case 2:
                maybeRebindService(((Integer) msg.obj).intValue());
                break;
            default:
                return false;
        }
        return true;
    }

    public ImsResolver(Context context, String defaultImsPackageName, int numSlots) {
        this.mContext = context;
        this.mDeviceService = defaultImsPackageName;
        this.mNumSlots = numSlots;
        this.mCarrierConfigManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        this.mCarrierServices = new String[numSlots];
        this.mBoundImsServicesByFeature = (List) Stream.generate(new -$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg()).limit((long) this.mNumSlots).collect(Collectors.toList());
        IntentFilter appChangedFilter = new IntentFilter();
        appChangedFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        appChangedFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        appChangedFilter.addAction("android.intent.action.PACKAGE_ADDED");
        appChangedFilter.addDataScheme("package");
        context.registerReceiverAsUser(this.mAppChangedReceiver, UserHandle.ALL, appChangedFilter, null, null);
        context.registerReceiver(this.mConfigChangedReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
    }

    public void setSubscriptionManagerProxy(SubscriptionManagerProxy proxy) {
        this.mSubscriptionManagerProxy = proxy;
    }

    public void setImsServiceControllerFactory(ImsServiceControllerFactory factory) {
        this.mImsServiceControllerFactory = factory;
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public void populateCacheAndStartBind() {
        Rlog.i(TAG, "Initializing cache and binding.");
        this.mHandler.obtainMessage(2, Integer.valueOf(-1)).sendToTarget();
        this.mHandler.obtainMessage(0, null).sendToTarget();
    }

    /* JADX WARNING: Missing block: B:19:0x0054, code:
            if (r0 == null) goto L_0x0061;
     */
    /* JADX WARNING: Missing block: B:20:0x0056, code:
            r0.addImsServiceFeatureListener(r9);
     */
    /* JADX WARNING: Missing block: B:21:0x005d, code:
            return r0.getImsServiceController();
     */
    /* JADX WARNING: Missing block: B:25:0x0061, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public IImsServiceController getImsServiceControllerAndListen(int slotId, int feature, IImsServiceFeatureListener callback) {
        Rlog.i(TAG, "getImsServiceControllerAndListen - slot: " + slotId + " with feature: " + feature + " callback: " + callback);
        if (slotId < 0 || slotId >= this.mNumSlots || feature <= -1 || feature >= 3) {
            return null;
        }
        synchronized (this.mBoundServicesLock) {
            SparseArray<ImsServiceController> services = (SparseArray) this.mBoundImsServicesByFeature.get(slotId);
            if (services == null) {
                return null;
            }
            ImsServiceController controller = (ImsServiceController) services.get(feature);
        }
    }

    private void putImsController(int slotId, int feature, ImsServiceController controller) {
        if (slotId < 0 || slotId >= this.mNumSlots || feature <= -1 || feature >= 3) {
            Rlog.w(TAG, "putImsController received invalid parameters - slot: " + slotId + ", feature: " + feature);
            return;
        }
        synchronized (this.mBoundServicesLock) {
            SparseArray<ImsServiceController> services = (SparseArray) this.mBoundImsServicesByFeature.get(slotId);
            if (services == null) {
                services = new SparseArray();
                this.mBoundImsServicesByFeature.add(slotId, services);
            }
            Rlog.i(TAG, "ImsServiceController added on slot: " + slotId + " with feature: " + feature + " using package: " + controller.getComponentName());
            services.put(feature, controller);
        }
    }

    /* JADX WARNING: Missing block: B:23:0x0083, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ImsServiceController removeImsController(int slotId, int feature) {
        if (slotId < 0 || slotId >= this.mNumSlots || feature <= -1 || feature >= 3) {
            Rlog.w(TAG, "removeImsController received invalid parameters - slot: " + slotId + ", feature: " + feature);
            return null;
        }
        synchronized (this.mBoundServicesLock) {
            SparseArray<ImsServiceController> services = (SparseArray) this.mBoundImsServicesByFeature.get(slotId);
            if (services == null) {
                return null;
            }
            ImsServiceController c = (ImsServiceController) services.get(feature, null);
            if (c != null) {
                Rlog.i(TAG, "ImsServiceController removed on slot: " + slotId + " with feature: " + feature + " using package: " + c.getComponentName());
                services.remove(feature);
            }
        }
    }

    private void maybeAddedImsService(String packageName) {
        Rlog.d(TAG, "maybeAddedImsService, packageName: " + packageName);
        List<ImsServiceInfo> infos = getImsServiceInfo(packageName);
        List<ImsServiceInfo> newlyAddedInfos = new ArrayList();
        for (ImsServiceInfo info : infos) {
            Optional<ImsServiceInfo> match = getInfoByComponentName(this.mInstalledServicesCache, info.name);
            if (match.isPresent()) {
                Rlog.i(TAG, "Updating features in cached ImsService: " + info.name);
                Rlog.d(TAG, "Updating features - Old features: " + ((ImsServiceInfo) match.get()).supportedFeatures + " new features: " + info.supportedFeatures);
                ((ImsServiceInfo) match.get()).supportedFeatures = info.supportedFeatures;
                updateImsServiceFeatures(info);
            } else {
                Rlog.i(TAG, "Adding newly added ImsService to cache: " + info.name);
                this.mInstalledServicesCache.add(info);
                newlyAddedInfos.add(info);
            }
        }
        for (ImsServiceInfo info2 : newlyAddedInfos) {
            if (isActiveCarrierService(info2)) {
                bindNewImsService(info2);
                updateImsServiceFeatures(getImsServiceInfoFromCache(this.mDeviceService));
            } else if (isDeviceService(info2)) {
                bindNewImsService(info2);
            }
        }
    }

    private boolean maybeRemovedImsService(String packageName) {
        Optional<ImsServiceInfo> match = getInfoByPackageName(this.mInstalledServicesCache, packageName);
        if (!match.isPresent()) {
            return false;
        }
        this.mInstalledServicesCache.remove(match.get());
        Rlog.i(TAG, "Removing ImsService: " + ((ImsServiceInfo) match.get()).name);
        unbindImsService((ImsServiceInfo) match.get());
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

    private Optional<ImsServiceController> getControllerByServiceInfo(Set<ImsServiceController> searchSet, ImsServiceInfo matchValue) {
        return searchSet.stream().filter(new com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg.AnonymousClass3(matchValue)).findFirst();
    }

    private Optional<ImsServiceInfo> getInfoByPackageName(Set<ImsServiceInfo> searchSet, String matchValue) {
        return searchSet.stream().filter(new AnonymousClass5(matchValue)).findFirst();
    }

    private Optional<ImsServiceInfo> getInfoByComponentName(Set<ImsServiceInfo> searchSet, ComponentName matchValue) {
        return searchSet.stream().filter(new AnonymousClass4(matchValue)).findFirst();
    }

    private void updateImsServiceFeatures(ImsServiceInfo newInfo) {
        if (newInfo != null) {
            Optional<ImsServiceController> o = getControllerByServiceInfo(this.mActiveControllers, newInfo);
            if (o.isPresent()) {
                Rlog.i(TAG, "Updating features for ImsService: " + ((ImsServiceController) o.get()).getComponentName());
                HashSet<Pair<Integer, Integer>> features = calculateFeaturesToCreate(newInfo);
                try {
                    if (features.size() > 0) {
                        Rlog.d(TAG, "Updating Features - New Features: " + features);
                        ((ImsServiceController) o.get()).changeImsServiceFeatures(features);
                        if (isActiveCarrierService(newInfo) && (TextUtils.equals(newInfo.name.getPackageName(), this.mDeviceService) ^ 1) != 0) {
                            Rlog.i(TAG, "Updating device default");
                            updateImsServiceFeatures(getImsServiceInfoFromCache(this.mDeviceService));
                        }
                    } else {
                        Rlog.i(TAG, "Unbinding: features = 0 for ImsService: " + ((ImsServiceController) o.get()).getComponentName());
                        ((ImsServiceController) o.get()).unbind();
                    }
                } catch (RemoteException e) {
                    Rlog.e(TAG, "updateImsServiceFeatures: Remote Exception: " + e.getMessage());
                }
            }
        }
    }

    private void bindNewImsService(ImsServiceInfo info) {
        if (!volte) {
            Rlog.i(TAG, "Not support volte, skip bind ImsService");
        } else if (info != null) {
            ImsServiceController controller = this.mImsServiceControllerFactory.get(this.mContext, info.name);
            HashSet<Pair<Integer, Integer>> features = calculateFeaturesToCreate(info);
            if (features.size() > 0) {
                Rlog.i(TAG, "Binding ImsService: " + controller.getComponentName() + " with features: " + features);
                controller.bind(features);
                this.mActiveControllers.add(controller);
            }
        }
    }

    private void unbindImsService(ImsServiceInfo info) {
        if (info != null) {
            Optional<ImsServiceController> o = getControllerByServiceInfo(this.mActiveControllers, info);
            if (o.isPresent()) {
                try {
                    Rlog.i(TAG, "Unbinding ImsService: " + ((ImsServiceController) o.get()).getComponentName());
                    ((ImsServiceController) o.get()).unbind();
                } catch (RemoteException e) {
                    Rlog.e(TAG, "unbindImsService: Remote Exception: " + e.getMessage());
                }
                this.mActiveControllers.remove(o.get());
            }
        }
    }

    private HashSet<Pair<Integer, Integer>> calculateFeaturesToCreate(ImsServiceInfo info) {
        HashSet<Pair<Integer, Integer>> imsFeaturesBySlot = new HashSet();
        int slotId = getSlotForActiveCarrierService(info);
        if (slotId != -1) {
            imsFeaturesBySlot.addAll((Collection) info.supportedFeatures.stream().map(new AnonymousClass6(slotId)).collect(Collectors.toList()));
        } else if (isDeviceService(info)) {
            for (int i = 0; i < this.mNumSlots; i++) {
                int currSlotId = i;
                ImsServiceInfo carrierImsInfo = getImsServiceInfoFromCache(this.mCarrierServices[i]);
                if (carrierImsInfo == null) {
                    imsFeaturesBySlot.addAll((Collection) info.supportedFeatures.stream().map(new AnonymousClass7(currSlotId)).collect(Collectors.toList()));
                } else {
                    Set<Integer> deviceFeatures = new HashSet(info.supportedFeatures);
                    deviceFeatures.removeAll(carrierImsInfo.supportedFeatures);
                    imsFeaturesBySlot.addAll((Collection) deviceFeatures.stream().map(new AnonymousClass8(currSlotId)).collect(Collectors.toList()));
                }
            }
        }
        return imsFeaturesBySlot;
    }

    public void imsServiceFeatureCreated(int slotId, int feature, ImsServiceController controller) {
        putImsController(slotId, feature, controller);
    }

    public void imsServiceFeatureRemoved(int slotId, int feature, ImsServiceController controller) {
        removeImsController(slotId, feature);
    }

    private void maybeRebindService(int subId) {
        if (subId <= -1) {
            for (int i = 0; i < this.mNumSlots; i++) {
                updateBoundCarrierServices(this.mSubscriptionManagerProxy.getSubId(i));
            }
            return;
        }
        updateBoundCarrierServices(subId);
    }

    private void updateBoundCarrierServices(int subId) {
        int slotId = this.mSubscriptionManagerProxy.getSlotIndex(subId);
        String newPackageName = this.mCarrierConfigManager.getConfigForSubId(subId).getString("config_ims_package_override_string", null);
        if (slotId != -1 && slotId < this.mNumSlots) {
            String oldPackageName = this.mCarrierServices[slotId];
            this.mCarrierServices[slotId] = newPackageName;
            if (!TextUtils.equals(newPackageName, oldPackageName)) {
                Rlog.i(TAG, "Carrier Config updated, binding new ImsService");
                unbindImsService(getImsServiceInfoFromCache(oldPackageName));
                bindNewImsService(getImsServiceInfoFromCache(newPackageName));
                updateImsServiceFeatures(getImsServiceInfoFromCache(this.mDeviceService));
            }
        }
    }

    public ImsServiceInfo getImsServiceInfoFromCache(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        Optional<ImsServiceInfo> infoFilter = getInfoByPackageName(this.mInstalledServicesCache, packageName);
        if (infoFilter.isPresent()) {
            return (ImsServiceInfo) infoFilter.get();
        }
        return null;
    }

    private List<ImsServiceInfo> getImsServiceInfo(String packageName) {
        List<ImsServiceInfo> infos = new ArrayList();
        Intent serviceIntent = new Intent(SERVICE_INTERFACE);
        serviceIntent.setPackage(packageName);
        for (ResolveInfo entry : this.mContext.getPackageManager().queryIntentServicesAsUser(serviceIntent, 128, this.mContext.getUserId())) {
            ServiceInfo serviceInfo = entry.serviceInfo;
            if (serviceInfo != null) {
                ImsServiceInfo info = new ImsServiceInfo();
                info.name = new ComponentName(serviceInfo.packageName, serviceInfo.name);
                info.supportedFeatures = new HashSet(3);
                if (serviceInfo.metaData != null) {
                    if (serviceInfo.metaData.getBoolean(METADATA_EMERGENCY_MMTEL_FEATURE, false)) {
                        info.supportedFeatures.add(Integer.valueOf(0));
                    }
                    if (serviceInfo.metaData.getBoolean(METADATA_MMTEL_FEATURE, false)) {
                        info.supportedFeatures.add(Integer.valueOf(1));
                    }
                    if (serviceInfo.metaData.getBoolean(METADATA_RCS_FEATURE, false)) {
                        info.supportedFeatures.add(Integer.valueOf(2));
                    }
                }
                if (TextUtils.equals(serviceInfo.permission, "android.permission.BIND_IMS_SERVICE")) {
                    Rlog.d(TAG, "ImsService added to cache: " + info.name + " with features: " + info.supportedFeatures);
                    infos.add(info);
                } else {
                    Rlog.w(TAG, "ImsService does not have BIND_IMS_SERVICE permission: " + info.name);
                }
            }
        }
        return infos;
    }
}

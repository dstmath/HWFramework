package ohos.idn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

@SystemApi
public class DeviceManager {
    public static final String KEY_DEVICE_BRMAC = "BR_MAC";
    public static final String KEY_DEVICE_CAPIBILITY = "CONN_CAP";
    public static final String KEY_DEVICE_NAME = "DEVICE_NAME";
    public static final String KEY_DEVICE_TYPE = "DEVICE_TYPE";
    public static final String KEY_GROUP_INFO = "GROUP_INFO";
    public static final String KEY_IS_ACCOUNT_TRUST = "IS_ACCOUNT_TRUST";
    public static final String KEY_IS_BLE_ENABLED = "IS_BLE_ENABLED";
    public static final String KEY_IS_P2P_ENABLED = "IS_P2P_ENABLED";
    public static final String KEY_IS_SUPPORT_BLE = "IS_SUPPORT_BLE";
    public static final String KEY_IS_SUPPORT_P2P = "IS_SUPPORT_P2P";
    public static final String KEY_NETWORK_TYPE = "NETWORK_TYPE";
    public static final String KEY_VERSION_TYPE = "VERSION_TYPE";
    private static final int LOG_ID_DNET = 218109232;
    private static final HiLogLabel TAG = new HiLogLabel(3, LOG_ID_DNET, "DeviceManager");

    public enum TrustedRange {
        SAME_ACCOUNT,
        PUBLIC,
        PRIVATE,
        PUBLIC_AND_PRIVATE
    }

    private static native boolean nativeAddDeviceChangeListener(int i, IDeviceChangeListener iDeviceChangeListener);

    private static native boolean nativeAddNetworkingChangeListener(IDeviceListener iDeviceListener);

    private static native BasicInfo nativeGetLocalBasicInfo();

    private static native BasicInfo nativeGetLocalNodeBasicInfo();

    private static native String nativeGetNodeExtInfo(String str, String str2);

    private static native Object[] nativeGetNodesBasicInfo();

    private static native BasicInfo nativeGetRemoteNodeBasicInfo(String str);

    private static native Object[] nativeGetRemoteNodesBasicInfo();

    private static native String nativeGetUdidByNodeId(String str);

    private static native String nativeGetUuidByNodeId(String str);

    private static native boolean nativeLinkToDeath(DeathNotifier deathNotifier);

    private static native boolean nativeRemoveDeviceChangeListener(int i, IDeviceChangeListener iDeviceChangeListener);

    private static native void nativeRemoveNetworkingChangeListener(IDeviceListener iDeviceListener);

    private static native boolean nativeUnlinkToDeath(DeathNotifier deathNotifier);

    public enum NetworkType {
        NETWORK_TYPE_WIFI(1),
        NETWORK_TYPE_BLE(2),
        NETWORK_TYPE_BR(3);
        
        private int mValue;

        private NetworkType(int i) {
            this.mValue = i;
        }
    }

    static {
        System.loadLibrary("dnetwork_jni.z");
    }

    public boolean addDeviceChangeListener(TrustedRange trustedRange, IDeviceChangeListener iDeviceChangeListener) {
        if (iDeviceChangeListener == null) {
            HiLog.error(TAG, "addDeviceChangeListener: listener is null", new Object[0]);
            return false;
        } else if (trustedRange == TrustedRange.PUBLIC) {
            return nativeAddDeviceChangeListener(trustedRange.ordinal(), iDeviceChangeListener);
        } else {
            HiLog.error(TAG, "unsupported TrustedRange=%{public}d", Integer.valueOf(trustedRange.ordinal()));
            return false;
        }
    }

    public boolean removeDeviceChangeListener(TrustedRange trustedRange, IDeviceChangeListener iDeviceChangeListener) {
        if (iDeviceChangeListener == null) {
            HiLog.error(TAG, "removeDeviceChangeListener: listener is null", new Object[0]);
            return false;
        } else if (trustedRange == TrustedRange.PUBLIC) {
            return nativeRemoveDeviceChangeListener(trustedRange.ordinal(), iDeviceChangeListener);
        } else {
            HiLog.error(TAG, "unsupported TrustedRange=%{public}d", Integer.valueOf(trustedRange.ordinal()));
            return false;
        }
    }

    public Optional<BasicInfo> getLocalBasicInfo() {
        BasicInfo nativeGetLocalBasicInfo = nativeGetLocalBasicInfo();
        if (nativeGetLocalBasicInfo == null) {
            return Optional.empty();
        }
        return Optional.of(nativeGetLocalBasicInfo);
    }

    public List<BasicInfo> getNodesBasicInfo() {
        Object[] nativeGetNodesBasicInfo = nativeGetNodesBasicInfo();
        if (nativeGetNodesBasicInfo == null || nativeGetNodesBasicInfo.length == 0) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        for (Object obj : nativeGetNodesBasicInfo) {
            if (obj instanceof BasicInfo) {
                arrayList.add((BasicInfo) obj);
            }
        }
        return arrayList;
    }

    public boolean linkToDeath(DeathNotifier deathNotifier) {
        if (deathNotifier != null) {
            return nativeLinkToDeath(deathNotifier);
        }
        HiLog.error(TAG, "linkToDeath: notifier is null", new Object[0]);
        return false;
    }

    public boolean unlinkToDeath(DeathNotifier deathNotifier) {
        if (deathNotifier != null) {
            return nativeUnlinkToDeath(deathNotifier);
        }
        HiLog.error(TAG, "unlinkToDeath: notifier is null", new Object[0]);
        return false;
    }

    public boolean addDeviceChangeListener(IDeviceListener iDeviceListener) {
        if (iDeviceListener != null) {
            return nativeAddNetworkingChangeListener(iDeviceListener);
        }
        HiLog.error(TAG, "addDeviceChangeListener: listener is null", new Object[0]);
        return false;
    }

    public void removeDeviceChangeListener(IDeviceListener iDeviceListener) {
        if (iDeviceListener == null) {
            HiLog.error(TAG, "removeDeviceChangeListener: listener is null", new Object[0]);
        } else {
            nativeRemoveNetworkingChangeListener(iDeviceListener);
        }
    }

    public Optional<BasicInfo> getLocalNodeBasicInfo() {
        BasicInfo nativeGetLocalNodeBasicInfo = nativeGetLocalNodeBasicInfo();
        if (nativeGetLocalNodeBasicInfo != null) {
            return Optional.of(nativeGetLocalNodeBasicInfo);
        }
        HiLog.error(TAG, "getLocalNodeBasicInfo: get local node basic info native fail", new Object[0]);
        return Optional.empty();
    }

    public Optional<BasicInfo> getRemoteNodeBasicInfo(String str) {
        if (str == null || str.isEmpty()) {
            HiLog.error(TAG, "getRemoteNodeBasicInfo: nodeId is invalid", new Object[0]);
            return Optional.empty();
        }
        BasicInfo nativeGetRemoteNodeBasicInfo = nativeGetRemoteNodeBasicInfo(str);
        if (nativeGetRemoteNodeBasicInfo != null) {
            return Optional.of(nativeGetRemoteNodeBasicInfo);
        }
        HiLog.error(TAG, "getRemoteNodeBasicInfo: get remote node basic info native fail", new Object[0]);
        return Optional.empty();
    }

    public List<BasicInfo> getRemoteNodesBasicInfo() {
        Object[] nativeGetRemoteNodesBasicInfo = nativeGetRemoteNodesBasicInfo();
        if (nativeGetRemoteNodesBasicInfo == null || nativeGetRemoteNodesBasicInfo.length == 0) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        for (Object obj : nativeGetRemoteNodesBasicInfo) {
            if (obj instanceof BasicInfo) {
                arrayList.add((BasicInfo) obj);
            }
        }
        return arrayList;
    }

    public Optional<String> getNodeExtInfo(String str, String str2) {
        if (str == null || str.isEmpty() || str2 == null || str2.isEmpty()) {
            HiLog.error(TAG, "getNodeExtInfo: nodeId or key is invalid", new Object[0]);
            return Optional.empty();
        }
        String nativeGetNodeExtInfo = nativeGetNodeExtInfo(str, str2);
        if (nativeGetNodeExtInfo != null) {
            return Optional.of(nativeGetNodeExtInfo);
        }
        HiLog.error(TAG, "getNodeExtInfo: get node extInfo native fail", new Object[0]);
        return Optional.empty();
    }

    public Optional<String> getUdidByNodeId(String str) {
        if (str == null || str.isEmpty()) {
            HiLog.error(TAG, "getUdidByNodeId: nodeId is invalid", new Object[0]);
            return Optional.empty();
        }
        String nativeGetUdidByNodeId = nativeGetUdidByNodeId(str);
        if (nativeGetUdidByNodeId != null) {
            return Optional.of(nativeGetUdidByNodeId);
        }
        HiLog.error(TAG, "getUdidByNodeId: get udid by node id native fail", new Object[0]);
        return Optional.empty();
    }

    public Optional<String> getUuidByNodeId(String str) {
        if (str == null || str.isEmpty()) {
            HiLog.error(TAG, "getUuidByNodeId: nodeId is invalid", new Object[0]);
            return Optional.empty();
        }
        String nativeGetUuidByNodeId = nativeGetUuidByNodeId(str);
        if (nativeGetUuidByNodeId != null) {
            return Optional.of(nativeGetUuidByNodeId);
        }
        HiLog.error(TAG, "getUuidByNodeId: get uuid by node id native fail", new Object[0]);
        return Optional.empty();
    }
}

package ohos.security.deviceauth;

import android.content.Context;
import com.huawei.security.deviceauth.GroupOperation;
import com.huawei.security.deviceauth.HwDeviceGroupManager;
import java.util.List;
import java.util.Optional;
import ohos.aafwk.ability.Ability;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class DeviceGroupProxy implements IDeviceGroupProxy {
    private static final int DEFAULT_ERROR = -1;
    private static final String EMPTY_STRING = "";
    private static final HiLogLabel LABEL = new HiLogLabel(3, SUB_DOMAIN_SECURITY, "DeviceGroupProxy");
    private static final int SUB_DOMAIN_SECURITY = 218115840;
    private HwDeviceGroupManager manager = null;

    DeviceGroupProxy(Ability ability, String str, IHichainGroupCallback iHichainGroupCallback) {
        HiLog.info(LABEL, "DeviceGroupProxy begin", new Object[0]);
        if (ability == null || str == null || iHichainGroupCallback == null) {
            this.manager = null;
        } else {
            this.manager = HwDeviceGroupManager.getInstance(convertAbilityToContext(ability).get(), str, new HichainGroupCallbackProxy(iHichainGroupCallback));
        }
        HiLog.info(LABEL, "DeviceGroupProxy end", new Object[0]);
    }

    @Override // ohos.security.deviceauth.IDeviceGroupProxy
    public int createGroup(String str, String str2, int i, String str3) {
        HiLog.info(LABEL, "DeviceGroupProxy createGroup", new Object[0]);
        HwDeviceGroupManager hwDeviceGroupManager = this.manager;
        if (hwDeviceGroupManager != null) {
            return hwDeviceGroupManager.createGroup(str, str2, i, str3);
        }
        return -1;
    }

    @Override // ohos.security.deviceauth.IDeviceGroupProxy
    public int deleteGroup(String str) {
        HiLog.info(LABEL, "DeviceGroupProxy deleteGroup", new Object[0]);
        HwDeviceGroupManager hwDeviceGroupManager = this.manager;
        if (hwDeviceGroupManager != null) {
            return hwDeviceGroupManager.deleteGroup(str);
        }
        return -1;
    }

    @Override // ohos.security.deviceauth.IDeviceGroupProxy
    public String getLocalConnectInfo() {
        HiLog.info(LABEL, "DeviceGroupProxy getLocalConnectInfo", new Object[0]);
        HwDeviceGroupManager hwDeviceGroupManager = this.manager;
        if (hwDeviceGroupManager == null) {
            return null;
        }
        return hwDeviceGroupManager.getLocalConnectInfo();
    }

    @Override // ohos.security.deviceauth.IDeviceGroupProxy
    public int addMemberToGroup(String str, long j, String str2, String str3, int i) {
        HiLog.info(LABEL, "DeviceGroupProxy addMemberToGroup", new Object[0]);
        HwDeviceGroupManager hwDeviceGroupManager = this.manager;
        if (hwDeviceGroupManager != null) {
            return hwDeviceGroupManager.addMemberToGroup(str, j, str2, str3, i);
        }
        return -1;
    }

    @Override // ohos.security.deviceauth.IDeviceGroupProxy
    public int deleteMemberFromGroup(String str, long j, String str2, String str3) {
        HiLog.info(LABEL, "DeviceGroupProxy deleteMemberFromGroup", new Object[0]);
        HwDeviceGroupManager hwDeviceGroupManager = this.manager;
        if (hwDeviceGroupManager != null) {
            return hwDeviceGroupManager.deleteMemberFromGroup(str, j, str2, str3);
        }
        return -1;
    }

    @Override // ohos.security.deviceauth.IDeviceGroupProxy
    public int cancelRequest(long j) {
        HiLog.info(LABEL, "DeviceGroupProxy cancelRequest", new Object[0]);
        HwDeviceGroupManager hwDeviceGroupManager = this.manager;
        if (hwDeviceGroupManager != null) {
            return hwDeviceGroupManager.cancelRequest(j);
        }
        return -1;
    }

    @Override // ohos.security.deviceauth.IDeviceGroupProxy
    public List<String> listJoinedGroups(int i) {
        HiLog.info(LABEL, "DeviceGroupProxy listJoinedGroups", new Object[0]);
        HwDeviceGroupManager hwDeviceGroupManager = this.manager;
        if (hwDeviceGroupManager != null) {
            return hwDeviceGroupManager.listJoinedGroups(i);
        }
        HiLog.error(LABEL, "DeviceGroupProxy is not initialized", new Object[0]);
        return null;
    }

    @Override // ohos.security.deviceauth.IDeviceGroupProxy
    public List<String> listTrustedDevices(String str) {
        HiLog.info(LABEL, "DeviceGroupProxy listTrustedDevices", new Object[0]);
        HwDeviceGroupManager hwDeviceGroupManager = this.manager;
        if (hwDeviceGroupManager != null) {
            return hwDeviceGroupManager.listTrustedDevices(str);
        }
        HiLog.error(LABEL, "DeviceGroupProxy is not initialized", new Object[0]);
        return null;
    }

    @Override // ohos.security.deviceauth.IDeviceGroupProxy
    public boolean isDeviceInGroup(String str, String str2) {
        HiLog.info(LABEL, "DeviceGroupProxy isDeviceInGroup", new Object[0]);
        HwDeviceGroupManager hwDeviceGroupManager = this.manager;
        if (hwDeviceGroupManager != null) {
            return hwDeviceGroupManager.isDeviceInGroup(str, str2);
        }
        return false;
    }

    @Override // ohos.security.deviceauth.IDeviceGroupProxy
    public List<String> getGroupInfo(String str) {
        HiLog.info(LABEL, "DeviceGroupProxy getGroupInfo", new Object[0]);
        HwDeviceGroupManager hwDeviceGroupManager = this.manager;
        if (hwDeviceGroupManager != null) {
            return hwDeviceGroupManager.getGroupInfo(str);
        }
        HiLog.error(LABEL, "DeviceGroupProxy is not initialized", new Object[0]);
        return null;
    }

    @Override // ohos.security.deviceauth.IDeviceGroupProxy
    public int setFriendsList(String str, List<String> list) {
        HiLog.info(LABEL, "DeviceGroupProxy setFriendsList", new Object[0]);
        HwDeviceGroupManager hwDeviceGroupManager = this.manager;
        if (hwDeviceGroupManager != null) {
            return hwDeviceGroupManager.setFriendsList(str, list);
        }
        return -1;
    }

    @Override // ohos.security.deviceauth.IDeviceGroupProxy
    public List<String> getFriendsList(String str) {
        HiLog.info(LABEL, "DeviceGroupProxy getFriendsList", new Object[0]);
        HwDeviceGroupManager hwDeviceGroupManager = this.manager;
        if (hwDeviceGroupManager != null) {
            return hwDeviceGroupManager.getFriendsList(str);
        }
        HiLog.error(LABEL, "DeviceGroupProxy is not initialized", new Object[0]);
        return null;
    }

    @Override // ohos.security.deviceauth.IDeviceGroupProxy
    public int registerGroupNotice(String str, IHichainGroupChangeListener iHichainGroupChangeListener) {
        HiLog.info(LABEL, "DeviceGroupProxy registerGroupNotice", new Object[0]);
        HwDeviceGroupManager hwDeviceGroupManager = this.manager;
        if (hwDeviceGroupManager != null) {
            return hwDeviceGroupManager.registerGroupNotice(str, new HichainGroupChangeListenerProxy(iHichainGroupChangeListener));
        }
        return -1;
    }

    @Override // ohos.security.deviceauth.IDeviceGroupProxy
    public int revokeGroupNotice(String str) {
        HiLog.info(LABEL, "DeviceGroupProxy revokeGroupNotice", new Object[0]);
        HwDeviceGroupManager hwDeviceGroupManager = this.manager;
        if (hwDeviceGroupManager != null) {
            return hwDeviceGroupManager.revokeGroupNotice(str);
        }
        return -1;
    }

    private Optional<Context> convertAbilityToContext(Ability ability) {
        if (ability == null) {
            return Optional.empty();
        }
        Object hostContext = ability.getHostContext();
        if (hostContext instanceof Context) {
            return Optional.of((Context) hostContext);
        }
        return Optional.empty();
    }

    private static class HichainGroupChangeListenerProxy implements HwDeviceGroupManager.HichainGroupChangeListener {
        private final IHichainGroupChangeListener mCallback;

        HichainGroupChangeListenerProxy(IHichainGroupChangeListener iHichainGroupChangeListener) {
            HiLog.info(DeviceGroupProxy.LABEL, "HichainGroupChangeListenerProxy init", new Object[0]);
            if (iHichainGroupChangeListener == null) {
                HiLog.warn(DeviceGroupProxy.LABEL, "IHichainGroupChangeListener is null", new Object[0]);
            }
            this.mCallback = iHichainGroupChangeListener;
        }

        public void onGroupCreated(String str, int i) {
            IHichainGroupChangeListener iHichainGroupChangeListener = this.mCallback;
            if (iHichainGroupChangeListener != null) {
                iHichainGroupChangeListener.onGroupCreated(str, i);
            }
        }

        public void onGroupDeleted(String str, int i) {
            IHichainGroupChangeListener iHichainGroupChangeListener = this.mCallback;
            if (iHichainGroupChangeListener != null) {
                iHichainGroupChangeListener.onGroupDeleted(str, i);
            }
        }

        public void onMemberAdded(String str, int i, List<String> list) {
            IHichainGroupChangeListener iHichainGroupChangeListener = this.mCallback;
            if (iHichainGroupChangeListener != null) {
                iHichainGroupChangeListener.onMemberAdded(str, i, list);
            }
        }

        public void onMemberDeleted(String str, int i, List<String> list) {
            IHichainGroupChangeListener iHichainGroupChangeListener = this.mCallback;
            if (iHichainGroupChangeListener != null) {
                iHichainGroupChangeListener.onMemberDeleted(str, i, list);
            }
        }
    }

    private static class HichainGroupCallbackProxy implements HwDeviceGroupManager.HichainGroupCallback {
        private final IHichainGroupCallback mCallback;

        HichainGroupCallbackProxy(IHichainGroupCallback iHichainGroupCallback) {
            HiLog.info(DeviceGroupProxy.LABEL, "HichainGroupCallbackProxy init", new Object[0]);
            if (iHichainGroupCallback == null) {
                HiLog.warn(DeviceGroupProxy.LABEL, "IHichainGroupChangeListener is null", new Object[0]);
            }
            this.mCallback = iHichainGroupCallback;
        }

        public void onFinish(long j, GroupOperation groupOperation, String str) {
            IHichainGroupCallback iHichainGroupCallback = this.mCallback;
            if (iHichainGroupCallback != null) {
                iHichainGroupCallback.onFinish(j, GroupOperationCode.valueOf(groupOperation.toInt()), str);
            }
        }

        public void onError(long j, GroupOperation groupOperation, int i, String str) {
            IHichainGroupCallback iHichainGroupCallback = this.mCallback;
            if (iHichainGroupCallback != null) {
                iHichainGroupCallback.onError(j, GroupOperationCode.valueOf(groupOperation.toInt()), i, str);
            }
        }

        public String onRequest(long j, GroupOperation groupOperation, String str) {
            IHichainGroupCallback iHichainGroupCallback = this.mCallback;
            if (iHichainGroupCallback == null) {
                return "";
            }
            return iHichainGroupCallback.onRequest(j, GroupOperationCode.valueOf(groupOperation.toInt()), str);
        }
    }
}

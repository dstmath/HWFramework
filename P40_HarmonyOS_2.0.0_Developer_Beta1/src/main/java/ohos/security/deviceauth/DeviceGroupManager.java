package ohos.security.deviceauth;

import java.util.List;
import ohos.aafwk.ability.Ability;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class DeviceGroupManager {
    private static final int DEFAULT_ERROR = -1;
    private static final String EMPTY_STRING = "";
    private static final HiLogLabel LABEL = new HiLogLabel(3, SUB_DOMAIN_SECURITY, "DeviceGroupManager");
    private static final int SUB_DOMAIN_SECURITY = 218115840;
    private static volatile DeviceGroupManager sInstance = null;
    private final DeviceGroupProxy mProxy;

    private DeviceGroupManager(Ability ability, String str, IHichainGroupCallback iHichainGroupCallback) {
        this.mProxy = new DeviceGroupProxy(ability, str, iHichainGroupCallback);
    }

    public static DeviceGroupManager getInstance(Ability ability, String str, IHichainGroupCallback iHichainGroupCallback) {
        HiLog.info(LABEL, "Get DeviceGroupManager instance.", new Object[0]);
        if (sInstance == null) {
            synchronized (DeviceGroupManager.class) {
                if (sInstance == null) {
                    sInstance = new DeviceGroupManager(ability, str, iHichainGroupCallback);
                }
            }
        }
        return sInstance;
    }

    public int createGroup(String str, String str2, int i, String str3) {
        HiLog.info(LABEL, "Create group in DeviceGroupManager.", new Object[0]);
        if (str != null && str2 != null && str3 != null) {
            return this.mProxy.createGroup(str, str2, i, str3);
        }
        HiLog.error(LABEL, "invalid parameter when call createGroup", new Object[0]);
        return -1;
    }

    public int deleteGroup(String str) {
        HiLog.info(LABEL, "Delete group in DeviceGroupManager.", new Object[0]);
        if (str != null) {
            return this.mProxy.deleteGroup(str);
        }
        HiLog.error(LABEL, "invalid parameter when call deleteGroup", new Object[0]);
        return -1;
    }

    public String getLocalConnectInfo() {
        HiLog.info(LABEL, "Get local connect info group in DeviceGroupManager.", new Object[0]);
        return this.mProxy.getLocalConnectInfo();
    }

    public int addMemberToGroup(String str, long j, String str2, String str3, int i) {
        HiLog.info(LABEL, "Add member to group in DeviceGroupManager.", new Object[0]);
        if (str != null && str2 != null && str3 != null) {
            return this.mProxy.addMemberToGroup(str, j, str2, str3, i);
        }
        HiLog.error(LABEL, "invalid parameter when call addMemberToGroup in DeviceGroupManager", new Object[0]);
        return -1;
    }

    public int deleteMemberFromGroup(String str, long j, String str2, String str3) {
        HiLog.info(LABEL, "Delete member from group in DeviceGroupManager.", new Object[0]);
        if (str != null && str2 != null) {
            return this.mProxy.deleteMemberFromGroup(str, j, str2, str3);
        }
        HiLog.error(LABEL, "invalid parameter when call deleteMemberFromGroup in DeviceGroupManager", new Object[0]);
        return -1;
    }

    public int cancelRequest(long j) {
        HiLog.info(LABEL, "cancel the processing async request in DeviceGroupManager.", new Object[0]);
        return this.mProxy.cancelRequest(j);
    }

    public List<String> listJoinedGroups(int i) {
        HiLog.info(LABEL, "list all joined groups by groupType in DeviceGroupManager.", new Object[0]);
        return this.mProxy.listJoinedGroups(i);
    }

    public List<String> listTrustedDevices(String str) {
        HiLog.info(LABEL, "list all trust devices by groupId in DeviceGroupManager.", new Object[0]);
        if (str != null) {
            return this.mProxy.listTrustedDevices(str);
        }
        HiLog.error(LABEL, "invalid parameter when call listTrustedDevices in DeviceGroupManager", new Object[0]);
        return null;
    }

    public boolean isDeviceInGroup(String str, String str2) {
        HiLog.info(LABEL, "query if device is in group in DeviceGroupManager.", new Object[0]);
        if (str != null && str2 != null) {
            return this.mProxy.isDeviceInGroup(str, str2);
        }
        HiLog.error(LABEL, "invalid parameter when call isDeviceInGroup in DeviceGroupManager", new Object[0]);
        return false;
    }

    public List<String> getGroupInfo(String str) {
        HiLog.info(LABEL, "get group information in DeviceGroupManager.", new Object[0]);
        if (str != null) {
            return this.mProxy.getGroupInfo(str);
        }
        HiLog.error(LABEL, "invalid parameter when call getGroupInfo in DeviceGroupManager", new Object[0]);
        return null;
    }

    public int setFriendsList(String str, List<String> list) {
        HiLog.info(LABEL, "set friends list", new Object[0]);
        if (str != null && list != null) {
            return this.mProxy.setFriendsList(str, list);
        }
        HiLog.error(LABEL, "invalid parameters", new Object[0]);
        return -1;
    }

    public List<String> getFriendsList(String str) {
        HiLog.info(LABEL, "get friends list", new Object[0]);
        if (str != null) {
            return this.mProxy.getFriendsList(str);
        }
        HiLog.error(LABEL, "invalid parameters", new Object[0]);
        return null;
    }

    public int registerGroupNotice(String str, IHichainGroupChangeListener iHichainGroupChangeListener) {
        HiLog.info(LABEL, "register group change notice in DeviceGroupManager.", new Object[0]);
        if (str != null && iHichainGroupChangeListener != null) {
            return this.mProxy.registerGroupNotice(str, iHichainGroupChangeListener);
        }
        HiLog.error(LABEL, "invalid parameter when call registerGroupNotice in DeviceGroupManager", new Object[0]);
        return -1;
    }

    public int revokeGroupNotice(String str) {
        HiLog.info(LABEL, "revoke group change notice in DeviceGroupManager.", new Object[0]);
        if (str != null) {
            return this.mProxy.revokeGroupNotice(str);
        }
        HiLog.error(LABEL, "invalid parameter when call revokeGroupNotice in DeviceGroupManager", new Object[0]);
        return -1;
    }
}

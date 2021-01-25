package ohos.security.deviceauth;

import java.util.List;

public interface IDeviceGroupProxy {
    int addMemberToGroup(String str, long j, String str2, String str3, int i);

    int cancelRequest(long j);

    int createGroup(String str, String str2, int i, String str3);

    int deleteGroup(String str);

    int deleteMemberFromGroup(String str, long j, String str2, String str3);

    List<String> getFriendsList(String str);

    List<String> getGroupInfo(String str);

    String getLocalConnectInfo();

    boolean isDeviceInGroup(String str, String str2);

    List<String> listJoinedGroups(int i);

    List<String> listTrustedDevices(String str);

    int registerGroupNotice(String str, IHichainGroupChangeListener iHichainGroupChangeListener);

    int revokeGroupNotice(String str);

    int setFriendsList(String str, List<String> list);
}

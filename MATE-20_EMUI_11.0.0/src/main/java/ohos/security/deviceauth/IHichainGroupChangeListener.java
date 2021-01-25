package ohos.security.deviceauth;

import java.util.List;

public interface IHichainGroupChangeListener {
    void onGroupCreated(String str, int i);

    void onGroupDeleted(String str, int i);

    void onMemberAdded(String str, int i, List<String> list);

    void onMemberDeleted(String str, int i, List<String> list);
}

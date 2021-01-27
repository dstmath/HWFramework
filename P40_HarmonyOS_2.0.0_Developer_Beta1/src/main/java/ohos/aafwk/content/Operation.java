package ohos.aafwk.content;

import java.util.Set;
import ohos.utils.net.Uri;

public interface Operation {
    void addEntity(String str);

    void addFlags(int i);

    String getAbilityName();

    String getAction();

    String getBundleName();

    String getDeviceId();

    Set<String> getEntities();

    int getFlags();

    Uri getUri();

    void removeEntity(String str);

    void removeFlags(int i);

    void setAbilityName(String str);

    void setAction(String str);

    void setBundleName(String str);

    void setDeviceId(String str);

    void setEntities(Set<String> set);

    void setFlags(int i);

    void setUri(Uri uri);
}

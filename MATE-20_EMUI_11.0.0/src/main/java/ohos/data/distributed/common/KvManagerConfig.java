package ohos.data.distributed.common;

import java.util.Objects;
import ohos.app.Context;

public class KvManagerConfig {
    private String bundleName;
    private UserInfo userInfo;

    public KvManagerConfig(Context context) {
        if (context != null) {
            this.bundleName = context.getBundleName();
            this.userInfo = new UserInfo();
            this.userInfo.setUserType(UserType.SAME_USER_ID);
        }
    }

    public boolean isInvalid() {
        return TextUtils.isEmpty(this.bundleName) || Objects.isNull(this.userInfo);
    }

    public UserInfo getUserInfo() {
        return this.userInfo;
    }

    public void setUserInfo(UserInfo userInfo2) {
        this.userInfo = userInfo2;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public int hashCode() {
        return Objects.hash(this.userInfo);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass() && (obj instanceof KvManagerConfig)) {
            return Objects.equals(this.userInfo, ((KvManagerConfig) obj).userInfo);
        }
        return false;
    }
}

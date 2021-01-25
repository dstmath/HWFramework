package ohos.data.distributed.common;

import java.util.Objects;

public class UserInfo {
    private String userId;
    private UserType userType;

    public UserInfo() {
        this.userType = UserType.SAME_USER_ID;
    }

    public UserInfo(String str, UserType userType2) {
        this.userId = str;
        this.userType = userType2;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String str) {
        this.userId = str;
    }

    public UserType getUserType() {
        return this.userType;
    }

    public void setUserType(UserType userType2) {
        this.userType = userType2;
    }

    public int hashCode() {
        return Objects.hash(this.userId, this.userType);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof UserInfo)) {
            return false;
        }
        UserInfo userInfo = (UserInfo) obj;
        return Objects.equals(this.userId, userInfo.userId) && this.userType == userInfo.userType;
    }
}

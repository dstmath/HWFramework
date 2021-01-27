package ohos.data.distributed.common;

import java.util.Objects;

public class UserInfo {
    private String userId;
    private UserType userType;

    public UserInfo() {
        this.userId = "";
        this.userType = UserType.SAME_USER_ID;
    }

    public UserInfo(String str, UserType userType2) {
        this.userId = "";
        if (str != null) {
            this.userId = str;
        } else {
            this.userId = "";
        }
        if (userType2 != null) {
            this.userType = userType2;
        } else {
            this.userType = UserType.SAME_USER_ID;
        }
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String str) {
        if (str != null) {
            this.userId = str;
        } else {
            this.userId = "";
        }
    }

    public UserType getUserType() {
        return this.userType;
    }

    public void setUserType(UserType userType2) {
        if (userType2 != null) {
            this.userType = userType2;
        } else {
            this.userType = UserType.SAME_USER_ID;
        }
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

package com.huawei.security.deviceauth;

public class UserType {
    public static final int ACCESSORY = 0;
    public static final int CONTROLLER = 1;

    private UserType() {
    }

    public static boolean validUserType(int userType) {
        return userType == 0 || userType == 1;
    }
}

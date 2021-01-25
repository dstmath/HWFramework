package com.huawei.android.bastet;

public class BastetPhoneManagerEx {
    private static final BastetPhoneManagerEx sInstance = new BastetPhoneManagerEx();

    public static synchronized BastetPhoneManagerEx getInstance() {
        BastetPhoneManagerEx bastetPhoneManagerEx;
        synchronized (BastetPhoneManagerEx.class) {
            bastetPhoneManagerEx = sInstance;
        }
        return bastetPhoneManagerEx;
    }

    public int configBstBlackList(int action, String[] blacklist, int[] option) throws Exception {
        return BastetPhoneManager.getInstance().configBstBlackList(action, blacklist, option);
    }

    public int deleteBstBlackListNum(String[] blacklist) throws Exception {
        return BastetPhoneManager.getInstance().deleteBstBlackListNum(blacklist);
    }

    public int setBstBarredRule(int rule) throws Exception {
        return BastetPhoneManager.getInstance().setBstBarredRule(rule);
    }

    public int setBstBarredSwitch(int enable_flag) throws Exception {
        return BastetPhoneManager.getInstance().setBstBarredSwitch(enable_flag);
    }
}

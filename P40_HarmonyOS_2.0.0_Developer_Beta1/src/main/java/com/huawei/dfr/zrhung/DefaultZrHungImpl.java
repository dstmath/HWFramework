package com.huawei.dfr.zrhung;

import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;

public class DefaultZrHungImpl implements IZrHung {
    public static IZrHung getZrHung(String wpName) {
        return null;
    }

    public int init(ZrHungData args) {
        return 0;
    }

    public boolean start(ZrHungData args) {
        return false;
    }

    public boolean check(ZrHungData args) {
        return false;
    }

    public boolean cancelCheck(ZrHungData args) {
        return false;
    }

    public boolean stop(ZrHungData args) {
        return false;
    }

    public boolean sendEvent(ZrHungData args) {
        return false;
    }

    public ZrHungData query() {
        return null;
    }

    public boolean addInfo(ZrHungData args) {
        return false;
    }

    public int getLockOwnerPid(Object lock) {
        return 0;
    }
}

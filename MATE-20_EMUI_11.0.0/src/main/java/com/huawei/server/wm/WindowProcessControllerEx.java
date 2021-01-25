package com.huawei.server.wm;

import android.util.ArraySet;
import com.android.server.wm.WindowProcessController;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class WindowProcessControllerEx {
    private WindowProcessController mWindowProcessController;

    public WindowProcessControllerEx() {
    }

    public WindowProcessControllerEx(WindowProcessController winProcController) {
        this.mWindowProcessController = winProcController;
    }

    public boolean isWindowProcessControllerNull() {
        return this.mWindowProcessController == null;
    }

    public String getName() {
        return this.mWindowProcessController.mName;
    }

    public ArraySet<String> getPkgList() {
        return this.mWindowProcessController.mPkgList;
    }

    public int getPid() {
        return this.mWindowProcessController.getPid();
    }

    public int getUid() {
        return this.mWindowProcessController.mUid;
    }
}

package com.huawei.android.content.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.ParceledListSlice;
import java.util.List;

public class ParceledListSliceEx {
    private ParceledListSlice<ApplicationInfo> mParceledListSlice;

    public ParceledListSliceEx() {
    }

    public ParceledListSliceEx(List<ApplicationInfo> appInfos) {
        this.mParceledListSlice = new ParceledListSlice<>(appInfos);
    }

    public void setParceledListSlice(ParceledListSlice<ApplicationInfo> parceledListSlice) {
        this.mParceledListSlice = parceledListSlice;
    }

    public List<ApplicationInfo> getList() {
        return this.mParceledListSlice.getList();
    }

    public boolean isObjNull() {
        return this.mParceledListSlice == null;
    }
}

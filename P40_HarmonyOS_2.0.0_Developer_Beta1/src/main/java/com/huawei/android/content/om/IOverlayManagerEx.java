package com.huawei.android.content.om;

import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;
import java.util.List;

@HwSystemApi
public class IOverlayManagerEx {
    private IOverlayManager mIOverlayManager;

    private IOverlayManagerEx(IOverlayManager iOverlayManager) {
        this.mIOverlayManager = iOverlayManager;
    }

    public static IOverlayManagerEx getInstance(IBinder binder) {
        return new IOverlayManagerEx(IOverlayManager.Stub.asInterface(binder));
    }

    public boolean setEnabledExclusive(String packageName, boolean enable, int userId) throws RemoteException {
        return this.mIOverlayManager.setEnabledExclusive(packageName, enable, userId);
    }

    public List getOverlayInfosForTarget(String packageName, int userId) throws RemoteException {
        List<OverlayInfo> list = this.mIOverlayManager.getOverlayInfosForTarget(packageName, userId);
        if (list == null) {
            return null;
        }
        List<OverlayInfoEx> temp = new ArrayList<>();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            temp.add(new OverlayInfoEx(list.get(i)));
        }
        return temp;
    }
}

package ohos.bundle;

import ohos.rpc.IRemoteBroker;

public interface OnPermissionChanged extends IRemoteBroker {
    public static final String DESCRIPTOR = "OHOS.Appexecfwk.OnPermissionChanged";
    public static final int ON_PERMISSION_CHANGED = 0;

    void onChanged(int i);
}

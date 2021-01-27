package ohos.bundle;

import ohos.rpc.IRemoteBroker;

public interface IShellApplication extends IRemoteBroker {
    public static final int BUNDLE_UPDATED = 0;
    public static final String DESCRIPTOR = "OHOS.Appexecfwk.IShellApplication";

    void onBundleUpdated(BundleInfo bundleInfo);
}

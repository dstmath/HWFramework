package com.android.server.pm;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import com.huawei.android.content.pm.IExtServiceProviderEx;
import com.huawei.android.content.pm.ResolveInfoEx;

public class ExtServiceProviderEx {
    private ExtServiceProvider extServiceProvider = new ExtServiceProvider();

    public ExtServiceProvider getExtServiceProvider() {
        return this.extServiceProvider;
    }

    public void setExtServiceProvider(ExtServiceProvider extServiceProvider2) {
        this.extServiceProvider = extServiceProvider2;
    }

    public void registerExtServiceProvider(IExtServiceProviderEx iext, Intent filter) {
        this.extServiceProvider.registerExtServiceProvider(iext.getExtServiceProvider(), filter);
    }

    public void unregisterExtServiceProvider(IExtServiceProviderEx iext) {
        this.extServiceProvider.unregisterExtServiceProvider(iext.getExtServiceProvider());
    }

    public ResolveInfoEx[] queryExtService(String action, String packageName) {
        ResolveInfo[] infos = this.extServiceProvider.queryExtService(action, packageName);
        if (infos == null) {
            return null;
        }
        ResolveInfoEx[] infoExs = new ResolveInfoEx[infos.length];
        for (int i = 0; i < infos.length; i++) {
            infoExs[i] = new ResolveInfoEx();
            infoExs[i].setResolveInfo(infos[i]);
        }
        return infoExs;
    }
}

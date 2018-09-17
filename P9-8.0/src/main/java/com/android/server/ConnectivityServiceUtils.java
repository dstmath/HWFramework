package com.android.server;

import android.content.Context;
import android.net.LinkProperties;
import android.os.INetworkManagementService;
import com.android.server.connectivity.NetworkAgentInfo;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.InvokeMethod;

public class ConnectivityServiceUtils extends EasyInvokeUtils {
    FieldObject<Context> mContext;
    FieldObject<INetworkManagementService> mNetd;
    MethodObject<Void> updateLinkProperties;

    @GetField(fieldObject = "mContext")
    public Context getContext(ConnectivityService connectivityService) {
        return (Context) getField(this.mContext, connectivityService);
    }

    @GetField(fieldObject = "mNetd")
    public INetworkManagementService getNetd(ConnectivityService connectivityService) {
        return (INetworkManagementService) getField(this.mNetd, connectivityService);
    }

    @InvokeMethod(methodObject = "updateLinkProperties")
    public void updateLinkProperties(ConnectivityService connectivityService, NetworkAgentInfo networkAgent, LinkProperties oldLp) {
        invokeMethod(this.updateLinkProperties, connectivityService, new Object[]{networkAgent, oldLp});
    }
}

package com.android.server;

import android.content.Context;
import android.net.LinkProperties;
import android.os.INetworkManagementService;
import android.os.Messenger;
import com.android.server.connectivity.NetworkAgentInfo;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.InvokeMethod;
import java.util.HashMap;

public class ConnectivityServiceUtils extends EasyInvokeUtils {
    FieldObject<Context> mContext;
    FieldObject<INetworkManagementService> mNetd;
    FieldObject<HashMap<Messenger, NetworkAgentInfo>> mNetworkAgentInfos;
    MethodObject<Void> updateLinkProperties;

    @GetField(fieldObject = "mContext")
    public Context getContext(ConnectivityService connectivityService) {
        return (Context) getField(this.mContext, connectivityService);
    }

    @GetField(fieldObject = "mNetworkAgentInfos")
    public HashMap<Messenger, NetworkAgentInfo> getNetworkAgentInfos(ConnectivityService connectivityService) {
        return (HashMap) getField(this.mNetworkAgentInfos, connectivityService);
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

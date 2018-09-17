package com.android.server.location;

import com.android.server.ServiceWatcher;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.annotation.GetField;

public class LocationProviderProxyUtils extends EasyInvokeUtils {
    FieldObject<ServiceWatcher> mServiceWatcher;

    @GetField(fieldObject = "mServiceWatcher")
    public ServiceWatcher getServiceWatcher(LocationProviderProxy locationProviderProxy) {
        return (ServiceWatcher) getField(this.mServiceWatcher, locationProviderProxy);
    }
}

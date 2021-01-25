package com.android.server;

import android.content.pm.Signature;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.InvokeMethod;
import com.huawei.utils.reflect.annotation.SetField;
import java.util.HashSet;
import java.util.List;

public class ServiceWatcherUtils extends EasyInvokeUtils {
    FieldObject<String> mServicePackageName;
    FieldObject<List<HashSet<Signature>>> mSignatureSets;
    MethodObject<Void> unbind;

    @SetField(fieldObject = "mServicePackageName")
    public void setServicePackageName(ServiceWatcher serviceWatcher, String servicePackageName) {
        setField(this.mServicePackageName, serviceWatcher, servicePackageName);
    }

    @SetField(fieldObject = "mSignatureSets")
    public void setSignatureSets(ServiceWatcher serviceWatcher, List<HashSet<Signature>> signatureSets) {
        setField(this.mSignatureSets, serviceWatcher, signatureSets);
    }

    @InvokeMethod(methodObject = "unbind")
    public void unbindLocked(ServiceWatcher serviceWatcher) {
        invokeMethod(this.unbind, serviceWatcher, new Object[0]);
    }
}

package android.content.pm;

import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.InvokeMethod;

public class ResolveInfoUtils extends EasyInvokeUtils {
    MethodObject<ComponentInfo> mComponentInfo;

    @InvokeMethod(methodObject = "mComponentInfo")
    public ComponentInfo getComponentInfo(ResolveInfo resinfo) {
        return (ComponentInfo) invokeMethod(this.mComponentInfo, resinfo, new Object[0]);
    }
}

package com.android.internal.telephony.cat;

import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.InvokeMethod;
import com.huawei.utils.reflect.annotation.SetField;
import java.util.List;

public class CommandParamsFactoryUtils extends EasyInvokeUtils {
    FieldObject<CommandParams> mCmdParams;
    MethodObject<ComprehensionTlv> searchForTag;

    @SetField(fieldObject = "mCmdParams")
    public void setCmdParams(CommandParamsFactory commandParamsFactory, CommandParams value) {
        setField(this.mCmdParams, commandParamsFactory, value);
    }

    @InvokeMethod(methodObject = "searchForTag")
    public ComprehensionTlv searchForTag(CommandParamsFactory commandParamsFactory, ComprehensionTlvTag tag, List<ComprehensionTlv> ctlvs) {
        return (ComprehensionTlv) invokeMethod(this.searchForTag, commandParamsFactory, new Object[]{tag, ctlvs});
    }
}

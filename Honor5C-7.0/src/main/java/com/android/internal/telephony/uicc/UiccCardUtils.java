package com.android.internal.telephony.uicc;

import com.android.internal.telephony.CommandsInterface;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.annotation.GetField;

public class UiccCardUtils extends EasyInvokeUtils {
    FieldObject<CommandsInterface> mCi;
    FieldObject<UiccCardApplication[]> mUiccApplications;

    @GetField(fieldObject = "mUiccApplications")
    public UiccCardApplication[] getUiccApplications(UiccCard uiccCard) {
        return (UiccCardApplication[]) getField(this.mUiccApplications, uiccCard);
    }

    @GetField(fieldObject = "mCi")
    public CommandsInterface getCi(UiccCard uiccCard) {
        return (CommandsInterface) getField(this.mCi, uiccCard);
    }
}

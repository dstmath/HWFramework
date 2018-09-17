package com.android.internal.telephony.cat;

import android.os.Handler;
import com.android.internal.telephony.HwTelephonyFactory;
import java.util.List;

public abstract class AbstractCommandParamsFactory extends Handler {
    CommandParamsFactoryReference mReference;

    public interface CommandParamsFactoryReference {
        boolean processLanguageNotification(CommandDetails commandDetails, List<ComprehensionTlv> list) throws ResultException;
    }

    public AbstractCommandParamsFactory() {
        this.mReference = HwTelephonyFactory.getHwUiccManager().createHwCommandParamsFactoryReference(this);
    }

    public boolean processLanguageNotification(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        return this.mReference.processLanguageNotification(cmdDet, ctlvs);
    }
}

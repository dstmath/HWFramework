package com.android.internal.telephony;

import android.content.Context;
import android.os.Message;
import com.android.internal.telephony.AbstractPhoneBase;
import com.android.internal.telephony.AbstractRIL;
import com.android.internal.telephony.cat.AbstractCommandParamsFactory;
import com.android.internal.telephony.euicc.EuiccConnector;
import com.android.internal.telephony.imsphone.AbstractImsPhoneCallTracker;
import com.android.internal.telephony.imsphone.IHwImsPhoneEx;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;

public interface HwTelephonyBaseManager {
    AbstractRIL.HwRILReference createHwRILReference(AbstractRIL abstractRIL);

    CommandException fromRilErrnoEx(int i);

    String gsm8BitUnpackedToString(byte[] bArr, int i, int i2, boolean z);

    String requestToStringEx(int i);

    String responseToStringEx(int i);

    default String retToStringEx(int req, Object ret) {
        return ret != null ? ret.toString() : PhoneConfigurationManager.SSSS;
    }

    default IHwImsPhoneEx createHwImsPhoneEx(ImsPhoneCallTracker imsCT) {
        return null;
    }

    default AbstractPhoneBase.HwPhoneBaseReference createHwPhoneBaseReference(AbstractPhoneBase phoneBase) {
        return null;
    }

    default AbstractImsPhoneCallTracker.ImsPhoneCallTrackerReference createHwImsPhoneCallTrackerReference(AbstractImsPhoneCallTracker imsPhoneCallTracker) {
        return null;
    }

    default <T> T createHwRil(Context context, int networkMode, int cdmaSubscription, Integer instanceId) {
        return null;
    }

    default AbstractCommandParamsFactory.CommandParamsFactoryReference createHwCommandParamsFactoryReference(Object commandParamsFactory) {
        return null;
    }

    default EuiccConnector.BaseEuiccCommandCallback getEuiccConnectorCallback(Message message) {
        return null;
    }
}

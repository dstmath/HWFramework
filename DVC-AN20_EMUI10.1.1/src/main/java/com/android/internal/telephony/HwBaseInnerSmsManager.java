package com.android.internal.telephony;

import android.content.Context;
import android.os.BaseBundle;
import android.os.Bundle;
import com.huawei.internal.telephony.cdma.sms.BearerDataEx;
import com.huawei.internal.telephony.cdma.sms.CdmaSmsAddressEx;
import com.huawei.internal.telephony.cdma.sms.UserDataEx;
import com.huawei.internal.telephony.gsm.SmsMessageEx;
import com.huawei.internal.util.BitwiseOutputStreamEx;
import java.util.ArrayList;

public interface HwBaseInnerSmsManager {
    boolean allowToSetSmsWritePermission(String str);

    boolean checkSmsBlacklistFlag(SmsMessageEx.PduParserEx pduParserEx);

    void doubleSmsStatusCheck(com.huawei.internal.telephony.cdma.SmsMessageEx smsMessageEx);

    boolean encode7bitMultiSms(UserDataEx userDataEx, byte[] bArr, boolean z);

    void encodeMsgCenterTimeStampCheck(BearerDataEx bearerDataEx, BitwiseOutputStreamEx bitwiseOutputStreamEx) throws BitwiseOutputStreamEx.AccessExceptionEx;

    ArrayList<String> fragmentForEmptyText();

    int getCdmaSub();

    byte[] getNewbyte();

    String getUserDataGSM8Bit(SmsMessageEx.PduParserEx pduParserEx, int i);

    CdmaSmsAddressEx parseForQcom(String str);

    boolean parseGsmSmsSubmit(SmsMessageEx smsMessageEx, int i, Object obj, int i2);

    void parseRUIMPdu(com.huawei.internal.telephony.cdma.SmsMessageEx smsMessageEx, byte[] bArr);

    void putExtraDataToConfig(BaseBundle baseBundle, Bundle bundle);

    boolean shouldSetDefaultApplicationForPackage(String str, Context context);
}

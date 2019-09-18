package com.android.internal.telephony;

import android.content.Context;
import android.os.BaseBundle;
import android.os.Bundle;
import com.android.internal.telephony.cdma.sms.BearerData;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.gsm.SmsMessage;
import com.android.internal.util.BitwiseOutputStream;
import java.util.ArrayList;

public interface HwBaseInnerSmsManager {
    boolean allowToSetSmsWritePermission(String str);

    boolean checkSmsBlacklistFlag(SmsMessage.PduParser pduParser);

    void doubleSmsStatusCheck(com.android.internal.telephony.cdma.SmsMessage smsMessage);

    boolean encode7bitMultiSms(UserData userData, byte[] bArr, boolean z);

    void encodeMsgCenterTimeStampCheck(BearerData bearerData, BitwiseOutputStream bitwiseOutputStream) throws BitwiseOutputStream.AccessException;

    ArrayList<String> fragmentForEmptyText();

    int getCdmaSub();

    byte[] getNewbyte();

    String getUserDataGSM8Bit(SmsMessage.PduParser pduParser, int i);

    CdmaSmsAddress parseForQcom(String str);

    boolean parseGsmSmsSubmit(SmsMessage smsMessage, int i, Object obj, int i2);

    void parseRUIMPdu(com.android.internal.telephony.cdma.SmsMessage smsMessage, byte[] bArr);

    void putExtraDataToConfig(BaseBundle baseBundle, Bundle bundle);

    boolean shouldSetDefaultApplicationForPackage(String str, Context context);
}

package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.cdma.SmsMessage;
import com.android.internal.telephony.cdma.sms.BearerData;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.gsm.SmsMessage.PduParser;
import com.android.internal.util.BitwiseOutputStream;
import com.android.internal.util.BitwiseOutputStream.AccessException;
import java.util.ArrayList;

public interface HwBaseInnerSmsManager {
    boolean allowToSetSmsWritePermission(String str);

    void doubleSmsStatusCheck(SmsMessage smsMessage);

    boolean encode7bitMultiSms(UserData userData, byte[] bArr, boolean z);

    void encodeMsgCenterTimeStampCheck(BearerData bearerData, BitwiseOutputStream bitwiseOutputStream) throws AccessException;

    ArrayList<String> fragmentForEmptyText();

    int getCdmaSub();

    byte[] getNewbyte();

    String getSmscAddr();

    String getSmscAddr(long j);

    String getUserDataGSM8Bit(PduParser pduParser, int i);

    boolean parseGsmSmsSubmit(com.android.internal.telephony.gsm.SmsMessage smsMessage, int i, Object obj, int i2);

    void parseRUIMPdu(SmsMessage smsMessage, byte[] bArr);

    boolean setSmscAddr(long j, String str);

    boolean setSmscAddr(String str);

    boolean shouldSetDefaultApplicationForPackage(String str, Context context);
}

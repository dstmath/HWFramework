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

public class DefaultHwBaseInnerSmsManager implements HwBaseInnerSmsManager {
    private static HwBaseInnerSmsManager mInstance = new DefaultHwBaseInnerSmsManager();

    public static HwBaseInnerSmsManager getDefault() {
        return mInstance;
    }

    @Override // com.android.internal.telephony.HwBaseInnerSmsManager
    public boolean shouldSetDefaultApplicationForPackage(String packageName, Context context) {
        return false;
    }

    @Override // com.android.internal.telephony.HwBaseInnerSmsManager
    public boolean allowToSetSmsWritePermission(String packageName) {
        return false;
    }

    @Override // com.android.internal.telephony.HwBaseInnerSmsManager
    public boolean parseGsmSmsSubmit(SmsMessageEx smsMessage, int mti, Object p, int firstByte) {
        return false;
    }

    @Override // com.android.internal.telephony.HwBaseInnerSmsManager
    public String getUserDataGSM8Bit(SmsMessageEx.PduParserEx p, int septetCount) {
        return null;
    }

    @Override // com.android.internal.telephony.HwBaseInnerSmsManager
    public void parseRUIMPdu(com.huawei.internal.telephony.cdma.SmsMessageEx msg, byte[] pdu) {
    }

    @Override // com.android.internal.telephony.HwBaseInnerSmsManager
    public boolean encode7bitMultiSms(UserDataEx uData, byte[] udhData, boolean force) {
        return false;
    }

    @Override // com.android.internal.telephony.HwBaseInnerSmsManager
    public void encodeMsgCenterTimeStampCheck(BearerDataEx bData, BitwiseOutputStreamEx outStream) throws BitwiseOutputStreamEx.AccessExceptionEx {
    }

    @Override // com.android.internal.telephony.HwBaseInnerSmsManager
    public void doubleSmsStatusCheck(com.huawei.internal.telephony.cdma.SmsMessageEx msg) {
    }

    @Override // com.android.internal.telephony.HwBaseInnerSmsManager
    public int getCdmaSub() {
        return 0;
    }

    @Override // com.android.internal.telephony.HwBaseInnerSmsManager
    public ArrayList<String> fragmentForEmptyText() {
        return null;
    }

    @Override // com.android.internal.telephony.HwBaseInnerSmsManager
    public byte[] getNewbyte() {
        return new byte[0];
    }

    @Override // com.android.internal.telephony.HwBaseInnerSmsManager
    public void putExtraDataToConfig(BaseBundle config, Bundle filtered) {
    }

    @Override // com.android.internal.telephony.HwBaseInnerSmsManager
    public boolean checkSmsBlacklistFlag(SmsMessageEx.PduParserEx p) {
        return false;
    }

    @Override // com.android.internal.telephony.HwBaseInnerSmsManager
    public CdmaSmsAddressEx parseForQcom(String address) {
        return null;
    }
}

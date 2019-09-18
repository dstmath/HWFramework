package com.android.internal.telephony.cdma;

import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.telephony.SmsMessage;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.android.internal.telephony.cdma.sms.CdmaSmsSubaddress;
import com.android.internal.telephony.cdma.sms.SmsEnvelope;

public class SmsMessageConverter {
    private static final String LOGGABLE_TAG = "CDMA:SMS";
    static final String LOG_TAG = "SmsMessageConverter";
    private static final boolean VDBG = false;

    public static SmsMessage newCdmaSmsMessageFromRil(CdmaSmsMessage cdmaSmsMessage) {
        SmsEnvelope env = new SmsEnvelope();
        CdmaSmsAddress addr = new CdmaSmsAddress();
        CdmaSmsSubaddress subaddr = new CdmaSmsSubaddress();
        env.teleService = cdmaSmsMessage.teleserviceId;
        if (cdmaSmsMessage.isServicePresent) {
            env.messageType = 1;
        } else if (env.teleService == 0) {
            env.messageType = 2;
        } else {
            env.messageType = 0;
        }
        env.serviceCategory = cdmaSmsMessage.serviceCategory;
        int addressDigitMode = cdmaSmsMessage.address.digitMode;
        addr.digitMode = (byte) (255 & addressDigitMode);
        addr.numberMode = (byte) (cdmaSmsMessage.address.numberMode & 255);
        addr.ton = cdmaSmsMessage.address.numberType;
        addr.numberPlan = (byte) (255 & cdmaSmsMessage.address.numberPlan);
        int count = (byte) cdmaSmsMessage.address.digits.size();
        addr.numberOfDigits = count;
        byte[] data = new byte[count];
        for (int index = 0; index < count; index++) {
            data[index] = cdmaSmsMessage.address.digits.get(index).byteValue();
            if (addressDigitMode == 0) {
                data[index] = SmsMessage.convertDtmfToAscii(data[index]);
            }
        }
        addr.origBytes = data;
        HwTelephonyFactory.getHwInnerSmsManager().addNumberPlusSign(addr, data);
        subaddr.type = cdmaSmsMessage.subAddress.subaddressType;
        subaddr.odd = cdmaSmsMessage.subAddress.odd ? (byte) 1 : 0;
        int count2 = (byte) cdmaSmsMessage.subAddress.digits.size();
        if (count2 < 0) {
            count2 = 0;
        }
        byte[] data2 = new byte[count2];
        for (int index2 = 0; index2 < count2; index2++) {
            data2[index2] = cdmaSmsMessage.subAddress.digits.get(index2).byteValue();
        }
        subaddr.origBytes = data2;
        int countInt = cdmaSmsMessage.bearerData.size();
        if (countInt < 0) {
            countInt = 0;
        }
        byte[] data3 = new byte[countInt];
        for (int index3 = 0; index3 < countInt; index3++) {
            data3[index3] = cdmaSmsMessage.bearerData.get(index3).byteValue();
        }
        env.bearerData = data3;
        env.origAddress = addr;
        env.origSubaddress = subaddr;
        return new SmsMessage(addr, env);
    }

    public static SmsMessage newSmsMessageFromCdmaSmsMessage(CdmaSmsMessage msg) {
        return new SmsMessage(newCdmaSmsMessageFromRil(msg));
    }
}

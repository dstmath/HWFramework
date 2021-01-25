package com.huawei.internal.telephony.gsm;

import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.gsm.SmsMessage;
import com.huawei.internal.telephony.EncodeExceptionEx;
import com.huawei.internal.telephony.SmsMessageBaseEx;
import java.io.UnsupportedEncodingException;

public class SmsMessageEx extends SmsMessageBaseEx {
    private SmsMessage mSmsMessage = new SmsMessage();

    public SmsMessageEx() {
        this.mSmsMessageBase = this.mSmsMessage;
    }

    public static byte[] encodeUCS2Hw(String message, byte[] udh) throws EncodeExceptionEx, UnsupportedEncodingException {
        try {
            return SmsMessage.encodeUCS2Hw(message, udh);
        } catch (EncodeException ex) {
            throw new EncodeExceptionEx(ex.getMessage(), ex.getError());
        }
    }

    public SmsMessage getSmsMessage() {
        return this.mSmsMessage;
    }

    public void setSmsMessage(SmsMessage smsMessage) {
        this.mSmsMessage = smsMessage;
        this.mSmsMessageBase = this.mSmsMessage;
    }

    public void setProtocolIdentifierHw(int value) {
        SmsMessage smsMessage = this.mSmsMessage;
        if (smsMessage != null) {
            smsMessage.setProtocolIdentifierHw(value);
        }
    }

    public void setDataCodingSchemeHw(int value) {
        SmsMessage smsMessage = this.mSmsMessage;
        if (smsMessage != null) {
            smsMessage.setDataCodingSchemeHw(value);
        }
    }

    public void parseUserDataHw(PduParserEx p, boolean hasUserDataHeader) {
        SmsMessage smsMessage = this.mSmsMessage;
        if (smsMessage != null) {
            smsMessage.parseUserDataHw(p.mPduParser, hasUserDataHeader);
        }
    }

    public static class SubmitPduEx extends SmsMessageBaseEx.SubmitPduBaseEx {
        SmsMessage.SubmitPdu mSubmitPdu = new SmsMessage.SubmitPdu();

        public SubmitPduEx() {
            setSubmitPduBase(this.mSubmitPdu);
        }

        public static SubmitPduEx getSubmitPduEx(String scAddress, String destinationAddress, String message, byte[] udh) {
            SmsMessageBase.SubmitPduBase spb = SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, false, udh);
            if (spb == null) {
                return null;
            }
            SubmitPduEx spbEx = new SubmitPduEx();
            spbEx.setSubmitPduBase(spb);
            return spbEx;
        }
    }

    public static class PduParserEx {
        private SmsMessage.PduParser mPduParser;

        public static PduParserEx from(Object parser) {
            if (parser == null || !(parser instanceof SmsMessage.PduParser)) {
                return null;
            }
            PduParserEx pduParserEx = new PduParserEx();
            pduParserEx.setPduParser((SmsMessage.PduParser) parser);
            return pduParserEx;
        }

        public GsmSmsAddressEx getAddressHw() {
            if (this.mPduParser == null) {
                return null;
            }
            GsmSmsAddressEx gsmSmsAddressEx = new GsmSmsAddressEx();
            gsmSmsAddressEx.setGsmSmsAddress(this.mPduParser.getAddressHw());
            return gsmSmsAddressEx;
        }

        public SmsMessage.PduParser getPduParser() {
            return this.mPduParser;
        }

        public void setPduParser(SmsMessage.PduParser pduParser) {
            this.mPduParser = pduParser;
        }

        public int getByteHw() {
            SmsMessage.PduParser pduParser = this.mPduParser;
            if (pduParser != null) {
                return pduParser.getByteHw();
            }
            return 0;
        }

        public int getCurHw() {
            SmsMessage.PduParser pduParser = this.mPduParser;
            if (pduParser != null) {
                return pduParser.getCurHw();
            }
            return 0;
        }

        public void setCurHw(int cur) {
            SmsMessage.PduParser pduParser = this.mPduParser;
            if (pduParser != null) {
                pduParser.setCurHw(cur);
            }
        }

        public byte[] getPduHw() {
            SmsMessage.PduParser pduParser = this.mPduParser;
            if (pduParser != null) {
                return pduParser.getPduHw();
            }
            return null;
        }
    }
}

package android.telephony.gsm;

import android.telephony.TelephonyManager;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import java.util.Arrays;

@Deprecated
public class SmsMessage {
    @Deprecated
    public static final int ENCODING_16BIT = 3;
    @Deprecated
    public static final int ENCODING_7BIT = 1;
    @Deprecated
    public static final int ENCODING_8BIT = 2;
    @Deprecated
    public static final int ENCODING_UNKNOWN = 0;
    @Deprecated
    public static final int MAX_USER_DATA_BYTES = 140;
    @Deprecated
    public static final int MAX_USER_DATA_BYTES_WITH_HEADER = 134;
    @Deprecated
    public static final int MAX_USER_DATA_SEPTETS = 160;
    @Deprecated
    public static final int MAX_USER_DATA_SEPTETS_WITH_HEADER = 153;
    @Deprecated
    public SmsMessageBase mWrappedSmsMessage;

    @Deprecated
    public enum MessageClass {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.gsm.SmsMessage.MessageClass.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.gsm.SmsMessage.MessageClass.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.gsm.SmsMessage.MessageClass.<clinit>():void");
        }
    }

    @Deprecated
    public static class SubmitPdu {
        @Deprecated
        public byte[] encodedMessage;
        @Deprecated
        public byte[] encodedScAddress;

        @Deprecated
        public SubmitPdu() {
        }

        @Deprecated
        protected SubmitPdu(SubmitPduBase spb) {
            if (spb != null) {
                this.encodedMessage = spb.encodedMessage;
                this.encodedScAddress = spb.encodedScAddress;
            }
        }

        @Deprecated
        public String toString() {
            return "SubmitPdu: encodedScAddress = " + Arrays.toString(this.encodedScAddress) + ", encodedMessage = " + Arrays.toString(this.encodedMessage);
        }
    }

    @Deprecated
    public SmsMessage() {
        this(getSmsFacility());
    }

    private SmsMessage(SmsMessageBase smb) {
        this.mWrappedSmsMessage = smb;
    }

    @Deprecated
    public static SmsMessage createFromPdu(byte[] pdu) {
        SmsMessageBase wrappedMessage;
        if (ENCODING_8BIT == TelephonyManager.getDefault().getCurrentPhoneType()) {
            wrappedMessage = com.android.internal.telephony.cdma.SmsMessage.createFromPdu(pdu);
        } else {
            wrappedMessage = com.android.internal.telephony.gsm.SmsMessage.createFromPdu(pdu);
        }
        return new SmsMessage(wrappedMessage);
    }

    @Deprecated
    public static int getTPLayerLengthForPDU(String pdu) {
        if (ENCODING_8BIT == TelephonyManager.getDefault().getCurrentPhoneType()) {
            return com.android.internal.telephony.cdma.SmsMessage.getTPLayerLengthForPDU(pdu);
        }
        return com.android.internal.telephony.gsm.SmsMessage.getTPLayerLengthForPDU(pdu);
    }

    @Deprecated
    public static int[] calculateLength(CharSequence messageBody, boolean use7bitOnly) {
        TextEncodingDetails ted = com.android.internal.telephony.gsm.SmsMessage.calculateLength(messageBody, use7bitOnly);
        return new int[]{ted.msgCount, ted.codeUnitCount, ted.codeUnitsRemaining, ted.codeUnitSize};
    }

    @Deprecated
    public static int[] calculateLength(String messageBody, boolean use7bitOnly) {
        return calculateLength((CharSequence) messageBody, use7bitOnly);
    }

    @Deprecated
    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested, byte[] header) {
        SubmitPduBase spb;
        if (ENCODING_8BIT == TelephonyManager.getDefault().getCurrentPhoneType()) {
            spb = com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, SmsHeader.fromByteArray(header));
        } else {
            spb = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, header);
        }
        return new SubmitPdu(spb);
    }

    @Deprecated
    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, String message, boolean statusReportRequested) {
        SubmitPduBase spb;
        if (ENCODING_8BIT == TelephonyManager.getDefault().getCurrentPhoneType()) {
            spb = com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested, null);
        } else {
            spb = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, statusReportRequested);
        }
        return new SubmitPdu(spb);
    }

    @Deprecated
    public static SubmitPdu getSubmitPdu(String scAddress, String destinationAddress, short destinationPort, byte[] data, boolean statusReportRequested) {
        SubmitPduBase spb;
        if (ENCODING_8BIT == TelephonyManager.getDefault().getCurrentPhoneType()) {
            spb = com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddress, destinationAddress, (int) destinationPort, data, statusReportRequested);
        } else {
            spb = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddress, destinationAddress, (int) destinationPort, data, statusReportRequested);
        }
        return new SubmitPdu(spb);
    }

    @Deprecated
    public String getServiceCenterAddress() {
        return this.mWrappedSmsMessage.getServiceCenterAddress();
    }

    @Deprecated
    public String getOriginatingAddress() {
        return this.mWrappedSmsMessage.getOriginatingAddress();
    }

    @Deprecated
    public String getDisplayOriginatingAddress() {
        return this.mWrappedSmsMessage.getDisplayOriginatingAddress();
    }

    @Deprecated
    public String getMessageBody() {
        return this.mWrappedSmsMessage.getMessageBody();
    }

    @Deprecated
    public MessageClass getMessageClass() {
        return MessageClass.values()[this.mWrappedSmsMessage.getMessageClass().ordinal()];
    }

    @Deprecated
    public String getDisplayMessageBody() {
        return this.mWrappedSmsMessage.getDisplayMessageBody();
    }

    @Deprecated
    public String getPseudoSubject() {
        return this.mWrappedSmsMessage.getPseudoSubject();
    }

    @Deprecated
    public long getTimestampMillis() {
        return this.mWrappedSmsMessage.getTimestampMillis();
    }

    @Deprecated
    public boolean isEmail() {
        return this.mWrappedSmsMessage.isEmail();
    }

    @Deprecated
    public String getEmailBody() {
        return this.mWrappedSmsMessage.getEmailBody();
    }

    @Deprecated
    public String getEmailFrom() {
        return this.mWrappedSmsMessage.getEmailFrom();
    }

    @Deprecated
    public int getProtocolIdentifier() {
        return this.mWrappedSmsMessage.getProtocolIdentifier();
    }

    @Deprecated
    public boolean isReplace() {
        return this.mWrappedSmsMessage.isReplace();
    }

    @Deprecated
    public boolean isCphsMwiMessage() {
        return this.mWrappedSmsMessage.isCphsMwiMessage();
    }

    @Deprecated
    public boolean isMWIClearMessage() {
        return this.mWrappedSmsMessage.isMWIClearMessage();
    }

    @Deprecated
    public boolean isMWISetMessage() {
        return this.mWrappedSmsMessage.isMWISetMessage();
    }

    @Deprecated
    public boolean isMwiDontStore() {
        return this.mWrappedSmsMessage.isMwiDontStore();
    }

    @Deprecated
    public byte[] getUserData() {
        return this.mWrappedSmsMessage.getUserData();
    }

    @Deprecated
    public byte[] getPdu() {
        return this.mWrappedSmsMessage.getPdu();
    }

    @Deprecated
    public int getStatusOnSim() {
        return this.mWrappedSmsMessage.getStatusOnIcc();
    }

    @Deprecated
    public int getStatusOnIcc() {
        return this.mWrappedSmsMessage.getStatusOnIcc();
    }

    @Deprecated
    public int getIndexOnSim() {
        return this.mWrappedSmsMessage.getIndexOnIcc();
    }

    @Deprecated
    public int getIndexOnIcc() {
        return this.mWrappedSmsMessage.getIndexOnIcc();
    }

    @Deprecated
    public int getStatus() {
        return this.mWrappedSmsMessage.getStatus();
    }

    @Deprecated
    public boolean isStatusReportMessage() {
        return this.mWrappedSmsMessage.isStatusReportMessage();
    }

    @Deprecated
    public boolean isReplyPathPresent() {
        return this.mWrappedSmsMessage.isReplyPathPresent();
    }

    @Deprecated
    private static final SmsMessageBase getSmsFacility() {
        if (ENCODING_8BIT == TelephonyManager.getDefault().getCurrentPhoneType()) {
            return new com.android.internal.telephony.cdma.SmsMessage();
        }
        return new com.android.internal.telephony.gsm.SmsMessage();
    }
}

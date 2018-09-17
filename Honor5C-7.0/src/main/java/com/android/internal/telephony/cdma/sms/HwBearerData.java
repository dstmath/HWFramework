package com.android.internal.telephony.cdma.sms;

import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.cdma.sms.BearerData.CodingException;
import com.android.internal.telephony.cdma.sms.BearerData.TimeStamp;
import com.android.internal.util.BitwiseOutputStream;
import com.android.internal.util.BitwiseOutputStream.AccessException;

public class HwBearerData {
    private static final String LOG_TAG = "HwBearerData";
    private static BearerDataUtils bearerDataUtils;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cdma.sms.HwBearerData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cdma.sms.HwBearerData.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.sms.HwBearerData.<clinit>():void");
    }

    public static boolean encode7bitMultiSms(UserData uData, byte[] udhData, boolean force) {
        int udhSeptets = (((udhData.length + 1) * 8) + 6) / 7;
        uData.msgEncoding = 2;
        uData.msgEncodingSet = true;
        try {
            uData.payload = BearerData.encode7bitAscii(uData.payloadStr, udhSeptets, force);
            if (uData.payloadStr != null) {
                uData.numFields = uData.payloadStr.length() + udhSeptets;
            }
            uData.payload[0] = (byte) udhData.length;
            System.arraycopy(udhData, 0, uData.payload, 1, udhData.length);
            return true;
        } catch (CodingException e) {
            return false;
        }
    }

    public static void encodeMsgCenterTimeStampCheck(BearerData bData, BitwiseOutputStream outStream) throws AccessException {
        if (bData.msgCenterTimeStamp != null) {
            outStream.write(8, bearerDataUtils.getSubparamMsgCenterTimeStamp(null));
            encodeMsgCenterTimeStamp(bData, outStream);
        }
    }

    private static void encodeMsgCenterTimeStamp(BearerData bData, BitwiseOutputStream outStream) throws AccessException {
        outStream.write(8, 6);
        bData.msgCenterTimeStamp.year %= 100;
        outStream.write(4, (byte) (bData.msgCenterTimeStamp.year / 10));
        outStream.write(4, (byte) (bData.msgCenterTimeStamp.year % 10));
        TimeStamp timeStamp = bData.msgCenterTimeStamp;
        timeStamp.month++;
        outStream.write(4, (byte) (bData.msgCenterTimeStamp.month / 10));
        outStream.write(4, (byte) (bData.msgCenterTimeStamp.month % 10));
        outStream.write(4, (byte) (bData.msgCenterTimeStamp.monthDay / 10));
        outStream.write(4, (byte) (bData.msgCenterTimeStamp.monthDay % 10));
        outStream.write(4, (byte) (bData.msgCenterTimeStamp.hour / 10));
        outStream.write(4, (byte) (bData.msgCenterTimeStamp.hour % 10));
        outStream.write(4, (byte) (bData.msgCenterTimeStamp.minute / 10));
        outStream.write(4, (byte) (bData.msgCenterTimeStamp.minute % 10));
        outStream.write(4, (byte) (bData.msgCenterTimeStamp.second / 10));
        outStream.write(4, (byte) (bData.msgCenterTimeStamp.second % 10));
    }

    public static TextEncodingDetails calcTextEncodingDetailsEx(CharSequence msg, boolean force7BitEncoding) {
        TextEncodingDetails ted = new TextEncodingDetails();
        int septets = bearerDataUtils.countAsciiSeptets(null, msg, force7BitEncoding);
        if (septets != -1) {
            ted.codeUnitCount = septets;
            if (septets > 160) {
                ted.msgCount = (septets + 152) / 153;
                ted.codeUnitsRemaining = (ted.msgCount * 153) - septets;
            } else {
                ted.msgCount = 1;
                ted.codeUnitsRemaining = 160 - septets;
            }
            ted.codeUnitSize = 1;
        } else {
            ted.codeUnitCount = msg.length();
            int octets = ted.codeUnitCount * 2;
            if (octets > 140) {
                ted.msgCount = (octets + 133) / 134;
                ted.codeUnitsRemaining = ((ted.msgCount * 134) - octets) / 2;
            } else {
                ted.msgCount = 1;
                ted.codeUnitsRemaining = (140 - octets) / 2;
            }
            ted.codeUnitSize = 3;
        }
        return ted;
    }
}

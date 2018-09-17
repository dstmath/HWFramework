package com.android.internal.telephony.gsm;

import android.telephony.SmsCbLocation;
import android.telephony.SmsCbMessage;
import android.util.Pair;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.cdma.SignalToneUtil;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduPersister;
import java.io.UnsupportedEncodingException;

public class GsmSmsCbMessage {
    private static final char CARRIAGE_RETURN = '\r';
    private static final String[] LANGUAGE_CODES_GROUP_0 = null;
    private static final String[] LANGUAGE_CODES_GROUP_2 = null;
    private static final int PDU_BODY_PAGE_LENGTH = 82;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.gsm.GsmSmsCbMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.gsm.GsmSmsCbMessage.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.GsmSmsCbMessage.<clinit>():void");
    }

    private GsmSmsCbMessage() {
    }

    public static SmsCbMessage createSmsCbMessage(SmsCbHeader header, SmsCbLocation location, byte[][] pdus) throws IllegalArgumentException {
        if (header.isEtwsPrimaryNotification()) {
            return new SmsCbMessage(1, header.getGeographicalScope(), header.getSerialNumber(), location, header.getServiceCategory(), null, "ETWS", 3, header.getEtwsInfo(), header.getCmasInfo());
        }
        int priority;
        String language = null;
        StringBuilder sb = new StringBuilder();
        for (byte[] pdu : pdus) {
            Pair<String, String> p = parseBody(header, pdu);
            language = p.first;
            sb.append((String) p.second);
        }
        if (header.isEmergencyMessage()) {
            priority = 3;
        } else {
            priority = 0;
        }
        return new SmsCbMessage(1, header.getGeographicalScope(), header.getSerialNumber(), location, header.getServiceCategory(), language, sb.toString(), priority, header.getEtwsInfo(), header.getCmasInfo());
    }

    public static SmsCbMessage createSmsCbMessage(SmsCbLocation location, byte[][] pdus) throws IllegalArgumentException {
        return createSmsCbMessage(new SmsCbHeader(pdus[0]), location, pdus);
    }

    private static Pair<String, String> parseBody(SmsCbHeader header, byte[] pdu) {
        int encoding;
        String language = null;
        boolean hasLanguageIndicator = false;
        int dataCodingScheme = header.getDataCodingScheme();
        switch ((dataCodingScheme & CallFailCause.CALL_BARRED) >> 4) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                encoding = 1;
                language = LANGUAGE_CODES_GROUP_0[dataCodingScheme & 15];
                break;
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                hasLanguageIndicator = true;
                if ((dataCodingScheme & 15) != 1) {
                    encoding = 1;
                    break;
                }
                encoding = 3;
                break;
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                encoding = 1;
                language = LANGUAGE_CODES_GROUP_2[dataCodingScheme & 15];
                break;
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                encoding = 1;
                break;
            case CharacterSets.ISO_8859_1 /*4*/:
            case CharacterSets.ISO_8859_2 /*5*/:
                switch ((dataCodingScheme & 12) >> 2) {
                    case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                        encoding = 2;
                        break;
                    case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                        encoding = 3;
                        break;
                    default:
                        encoding = 1;
                        break;
                }
            case CharacterSets.ISO_8859_3 /*6*/:
            case CharacterSets.ISO_8859_4 /*7*/:
            case CharacterSets.ISO_8859_6 /*9*/:
            case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                throw new IllegalArgumentException("Unsupported GSM dataCodingScheme " + dataCodingScheme);
            case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                if (((dataCodingScheme & 4) >> 2) != 1) {
                    encoding = 1;
                    break;
                }
                encoding = 2;
                break;
            default:
                encoding = 1;
                break;
        }
        if (header.isUmtsFormat()) {
            int nrPages = pdu[6];
            if (pdu.length < (nrPages * 83) + 7) {
                throw new IllegalArgumentException("Pdu length " + pdu.length + " does not match " + nrPages + " pages");
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nrPages; i++) {
                int offset = (i * 83) + 7;
                int length = pdu[offset + PDU_BODY_PAGE_LENGTH];
                if (length > PDU_BODY_PAGE_LENGTH) {
                    throw new IllegalArgumentException("Page length " + length + " exceeds maximum value " + PDU_BODY_PAGE_LENGTH);
                }
                Pair<String, String> p = unpackBody(pdu, encoding, offset, length, hasLanguageIndicator, language);
                language = p.first;
                sb.append((String) p.second);
            }
            return new Pair(language, sb.toString());
        }
        return unpackBody(pdu, encoding, 6, pdu.length - 6, hasLanguageIndicator, language);
    }

    private static Pair<String, String> unpackBody(byte[] pdu, int encoding, int offset, int length, boolean hasLanguageIndicator, String language) {
        Object body = null;
        switch (encoding) {
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                body = GsmAlphabet.gsm7BitPackedToString(pdu, offset, (length * 8) / 7);
                if (hasLanguageIndicator && body != null && body.length() > 2) {
                    language = body.substring(0, 2);
                    body = body.substring(3);
                    break;
                }
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                if (hasLanguageIndicator && pdu.length >= offset + 2) {
                    language = GsmAlphabet.gsm7BitPackedToString(pdu, offset, 2);
                    offset += 2;
                    length -= 2;
                }
                try {
                    body = new String(pdu, offset, 65534 & length, CharacterSets.MIMENAME_UTF_16);
                    break;
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException("Error decoding UTF-16 message", e);
                }
        }
        if (body != null) {
            int i = body.length() - 1;
            while (i >= 0) {
                if (body.charAt(i) != CARRIAGE_RETURN) {
                    body = body.substring(0, i + 1);
                } else {
                    i--;
                }
            }
        } else {
            body = "";
        }
        return new Pair(language, body);
    }
}

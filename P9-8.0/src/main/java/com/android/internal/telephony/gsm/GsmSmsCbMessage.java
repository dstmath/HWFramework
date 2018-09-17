package com.android.internal.telephony.gsm;

import android.content.Context;
import android.content.res.Resources;
import android.telephony.SmsCbLocation;
import android.telephony.SmsCbMessage;
import android.util.LogException;
import android.util.Pair;
import com.android.internal.R;
import com.android.internal.telephony.GsmAlphabet;
import java.io.UnsupportedEncodingException;

public class GsmSmsCbMessage {
    private static final char CARRIAGE_RETURN = '\r';
    private static final String[] LANGUAGE_CODES_GROUP_0 = new String[]{"de", "en", "it", "fr", "es", "nl", "sv", "da", "pt", "fi", "no", "el", "tr", "hu", "pl", null};
    private static final String[] LANGUAGE_CODES_GROUP_2 = new String[]{"cs", "he", "ar", "ru", "is", null, null, null, null, null, null, null, null, null, null, null};
    private static final int PDU_BODY_PAGE_LENGTH = 82;

    private GsmSmsCbMessage() {
    }

    private static String getEtwsPrimaryMessage(Context context, int category) {
        Resources r = context.getResources();
        switch (category) {
            case 0:
                return r.getString(R.string.etws_primary_default_message_earthquake);
            case 1:
                return r.getString(R.string.etws_primary_default_message_tsunami);
            case 2:
                return r.getString(R.string.etws_primary_default_message_earthquake_and_tsunami);
            case 3:
                return r.getString(R.string.etws_primary_default_message_test);
            case 4:
                return r.getString(R.string.etws_primary_default_message_others);
            default:
                return LogException.NO_VALUE;
        }
    }

    public static SmsCbMessage createSmsCbMessage(Context context, SmsCbHeader header, SmsCbLocation location, byte[][] pdus) throws IllegalArgumentException {
        if (header.isEtwsPrimaryNotification()) {
            return new SmsCbMessage(1, header.getGeographicalScope(), header.getSerialNumber(), location, header.getServiceCategory(), null, getEtwsPrimaryMessage(context, header.getEtwsInfo().getWarningType()), 3, header.getEtwsInfo(), header.getCmasInfo());
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

    private static Pair<String, String> parseBody(SmsCbHeader header, byte[] pdu) {
        int encoding;
        String language = null;
        boolean hasLanguageIndicator = false;
        int dataCodingScheme = header.getDataCodingScheme();
        switch ((dataCodingScheme & 240) >> 4) {
            case 0:
                encoding = 1;
                language = LANGUAGE_CODES_GROUP_0[dataCodingScheme & 15];
                break;
            case 1:
                hasLanguageIndicator = true;
                if ((dataCodingScheme & 15) != 1) {
                    encoding = 1;
                    break;
                }
                encoding = 3;
                break;
            case 2:
                encoding = 1;
                language = LANGUAGE_CODES_GROUP_2[dataCodingScheme & 15];
                break;
            case 3:
                encoding = 1;
                break;
            case 4:
            case 5:
                switch ((dataCodingScheme & 12) >> 2) {
                    case 1:
                        encoding = 2;
                        break;
                    case 2:
                        encoding = 3;
                        break;
                    default:
                        encoding = 1;
                        break;
                }
            case 6:
            case 7:
            case 9:
            case 14:
                throw new IllegalArgumentException("Unsupported GSM dataCodingScheme " + dataCodingScheme);
            case 15:
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
                int length = pdu[offset + 82];
                if (length > 82) {
                    throw new IllegalArgumentException("Page length " + length + " exceeds maximum value " + 82);
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
            case 1:
                body = GsmAlphabet.gsm7BitPackedToString(pdu, offset, (length * 8) / 7);
                if (hasLanguageIndicator && body != null && body.length() > 2) {
                    language = body.substring(0, 2);
                    body = body.substring(3);
                    break;
                }
            case 3:
                if (hasLanguageIndicator && pdu.length >= offset + 2) {
                    language = GsmAlphabet.gsm7BitPackedToString(pdu, offset, 2);
                    offset += 2;
                    length -= 2;
                }
                try {
                    body = new String(pdu, offset, 65534 & length, "utf-16");
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
            body = LogException.NO_VALUE;
        }
        return new Pair(language, body);
    }
}

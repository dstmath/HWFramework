package com.android.internal.telephony.gsm;

import android.content.Context;
import android.content.res.Resources;
import android.telephony.SmsCbLocation;
import android.telephony.SmsCbMessage;
import android.util.Pair;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.telephony.GsmAlphabet;
import java.io.UnsupportedEncodingException;

public class GsmSmsCbMessage {
    private static final char CARRIAGE_RETURN = '\r';
    private static final String[] LANGUAGE_CODES_GROUP_0 = {"de", "en", "it", "fr", "es", "nl", "sv", "da", "pt", "fi", "no", "el", "tr", "hu", "pl", null};
    private static final String[] LANGUAGE_CODES_GROUP_2 = {"cs", "he", "ar", "ru", "is", null, null, null, null, null, null, null, null, null, null, null};
    private static final int PDU_BODY_PAGE_LENGTH = 82;

    private GsmSmsCbMessage() {
    }

    private static String getEtwsPrimaryMessage(Context context, int category) {
        Resources r = context.getResources();
        switch (category) {
            case 0:
                return r.getString(17039995);
            case 1:
                return r.getString(17039999);
            case 2:
                return r.getString(17039996);
            case 3:
                return r.getString(17039998);
            case 4:
                return r.getString(17039997);
            default:
                return "";
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v0, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v2, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    public static SmsCbMessage createSmsCbMessage(Context context, SmsCbHeader header, SmsCbLocation location, byte[][] pdus) throws IllegalArgumentException {
        byte[][] bArr = pdus;
        if (header.isEtwsPrimaryNotification()) {
            SmsCbMessage smsCbMessage = new SmsCbMessage(1, header.getGeographicalScope(), header.getSerialNumber(), location, header.getServiceCategory(), null, getEtwsPrimaryMessage(context, header.getEtwsInfo().getWarningType()), 3, header.getEtwsInfo(), header.getCmasInfo());
            return smsCbMessage;
        }
        Context context2 = context;
        StringBuilder sb = new StringBuilder();
        int priority = 0;
        String language = null;
        for (byte[] pdu : bArr) {
            Pair<String, String> p = parseBody(header, pdu);
            language = p.first;
            sb.append((String) p.second);
        }
        SmsCbHeader smsCbHeader = header;
        if (header.isEmergencyMessage()) {
            priority = 3;
        }
        SmsCbMessage smsCbMessage2 = new SmsCbMessage(1, header.getGeographicalScope(), header.getSerialNumber(), location, header.getServiceCategory(), language, sb.toString(), priority, header.getEtwsInfo(), header.getCmasInfo());
        return smsCbMessage2;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v11, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v12, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v16, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v36, resolved type: byte} */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004c, code lost:
        r10 = r1;
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0052, code lost:
        if (r17.isUmtsFormat() == false) goto L_0x00e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0054, code lost:
        r6 = r8[6];
        r7 = 83;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005e, code lost:
        if (r8.length < ((83 * r6) + 7)) goto L_0x00bc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0060, code lost:
        r12 = new java.lang.StringBuilder();
        r2 = 0;
        r13 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0068, code lost:
        r14 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0069, code lost:
        if (r14 >= r6) goto L_0x00b2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x006b, code lost:
        r15 = 7 + (r7 * r14);
        r5 = r8[r15 + 82];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0075, code lost:
        if (r5 > 82) goto L_0x0092;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0077, code lost:
        r7 = r5;
        r0 = unpackBody(r8, r1, r15, r5, r10, r13);
        r13 = r0.first;
        r12.append((java.lang.String) r0.second);
        r7 = 83;
        r2 = r14 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b1, code lost:
        throw new java.lang.IllegalArgumentException("Page length " + r5 + " exceeds maximum value " + 82);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00bb, code lost:
        return new android.util.Pair<>(r13, r12.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00e0, code lost:
        throw new java.lang.IllegalArgumentException("Pdu length " + r8.length + " does not match " + r6 + " pages");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ef, code lost:
        return unpackBody(r8, r1, 6, r8.length - 6, r10, r0);
     */
    /* JADX WARNING: Multi-variable type inference failed */
    private static Pair<String, String> parseBody(SmsCbHeader header, byte[] pdu) {
        int encoding;
        byte[] bArr = pdu;
        String language = null;
        boolean hasLanguageIndicator = false;
        int dataCodingScheme = header.getDataCodingScheme();
        int i = (dataCodingScheme & MetricsProto.MetricsEvent.FINGERPRINT_ENROLLING) >> 4;
        if (i != 9) {
            switch (i) {
                case 0:
                    encoding = 1;
                    language = LANGUAGE_CODES_GROUP_0[dataCodingScheme & 15];
                    break;
                case 1:
                    hasLanguageIndicator = true;
                    if ((dataCodingScheme & 15) != 1) {
                        encoding = 1;
                        break;
                    } else {
                        encoding = 3;
                        break;
                    }
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
                    break;
                default:
                    switch (i) {
                        case 14:
                            break;
                        case 15:
                            if (((dataCodingScheme & 4) >> 2) != 1) {
                                encoding = 1;
                                break;
                            } else {
                                encoding = 2;
                                break;
                            }
                        default:
                            boolean hasLanguageIndicator2 = false;
                            int encoding2 = 1;
                            break;
                    }
            }
        }
        throw new IllegalArgumentException("Unsupported GSM dataCodingScheme " + dataCodingScheme);
    }

    private static Pair<String, String> unpackBody(byte[] pdu, int encoding, int offset, int length, boolean hasLanguageIndicator, String language) {
        String body = null;
        if (encoding == 1) {
            body = GsmAlphabet.gsm7BitPackedToString(pdu, offset, (length * 8) / 7);
            if (hasLanguageIndicator && body != null && body.length() > 2) {
                language = body.substring(0, 2);
                body = body.substring(3);
            }
        } else if (encoding == 3) {
            if (hasLanguageIndicator && pdu.length >= offset + 2) {
                language = GsmAlphabet.gsm7BitPackedToString(pdu, offset, 2);
                offset += 2;
                length -= 2;
            }
            try {
                body = new String(pdu, offset, 65534 & length, "utf-16");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("Error decoding UTF-16 message", e);
            }
        }
        if (body != null) {
            int i = body.length() - 1;
            while (true) {
                if (i < 0) {
                    break;
                } else if (body.charAt(i) != 13) {
                    body = body.substring(0, i + 1);
                    break;
                } else {
                    i--;
                }
            }
        } else {
            body = "";
        }
        return new Pair<>(language, body);
    }
}

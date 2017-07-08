package com.android.internal.telephony;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.SparseIntArray;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.util.XmlUtils;

public class Sms7BitEncodingTranslator {
    private static final boolean DBG = false;
    private static final String TAG = "Sms7BitEncodingTranslator";
    private static final String XML_CHARACTOR_TAG = "Character";
    private static final String XML_FROM_TAG = "from";
    private static final String XML_START_TAG = "SmsEnforce7BitTranslationTable";
    private static final String XML_TO_TAG = "to";
    private static final String XML_TRANSLATION_TYPE_TAG = "TranslationType";
    private static boolean mIs7BitTranslationTableLoaded;
    private static SparseIntArray mTranslationTable;
    private static SparseIntArray mTranslationTableCDMA;
    private static SparseIntArray mTranslationTableCommon;
    private static SparseIntArray mTranslationTableGSM;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.Sms7BitEncodingTranslator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.Sms7BitEncodingTranslator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.Sms7BitEncodingTranslator.<clinit>():void");
    }

    public static String translate(CharSequence message) {
        if (message == null) {
            Rlog.w(TAG, "Null message can not be translated");
            return null;
        }
        int size = message.length();
        if (size <= 0) {
            return "";
        }
        if (!mIs7BitTranslationTableLoaded) {
            mTranslationTableCommon = new SparseIntArray();
            mTranslationTableGSM = new SparseIntArray();
            mTranslationTableCDMA = new SparseIntArray();
            load7BitTranslationTableFromXml();
            mIs7BitTranslationTableLoaded = true;
        }
        if ((mTranslationTableCommon == null || mTranslationTableCommon.size() <= 0) && ((mTranslationTableGSM == null || mTranslationTableGSM.size() <= 0) && (mTranslationTableCDMA == null || mTranslationTableCDMA.size() <= 0))) {
            return null;
        }
        char[] output = new char[size];
        boolean isCdmaFormat = useCdmaFormatForMoSms();
        for (int i = 0; i < size; i++) {
            output[i] = translateIfNeeded(message.charAt(i), isCdmaFormat);
        }
        return String.valueOf(output);
    }

    private static char translateIfNeeded(char c, boolean isCdmaFormat) {
        if (noTranslationNeeded(c, isCdmaFormat)) {
            if (DBG) {
                Rlog.v(TAG, "No translation needed for " + Integer.toHexString(c));
            }
            return c;
        }
        int translation = -1;
        if (mTranslationTableCommon != null) {
            translation = mTranslationTableCommon.get(c, -1);
        }
        if (translation == -1) {
            if (isCdmaFormat) {
                if (mTranslationTableCDMA != null) {
                    translation = mTranslationTableCDMA.get(c, -1);
                }
            } else if (mTranslationTableGSM != null) {
                translation = mTranslationTableGSM.get(c, -1);
            }
        }
        if (translation != -1) {
            if (DBG) {
                Rlog.v(TAG, Integer.toHexString(c) + " (" + c + ")" + " translated to " + Integer.toHexString(translation) + " (" + ((char) translation) + ")");
            }
            return (char) translation;
        }
        if (DBG) {
            Rlog.w(TAG, "No translation found for " + Integer.toHexString(c) + "! Replacing for empty space");
        }
        return ' ';
    }

    private static boolean noTranslationNeeded(char c, boolean isCdmaFormat) {
        boolean z = DBG;
        if (!isCdmaFormat) {
            return GsmAlphabet.isGsmSeptets(c);
        }
        if (GsmAlphabet.isGsmSeptets(c) && UserData.charToAscii.get(c, -1) != -1) {
            z = true;
        }
        return z;
    }

    private static boolean useCdmaFormatForMoSms() {
        if (SmsManager.getDefault().isImsSmsSupported()) {
            return SmsMessage.FORMAT_3GPP2.equals(SmsManager.getDefault().getImsSmsFormat());
        }
        return TelephonyManager.getDefault().getCurrentPhoneType() == 2 ? true : DBG;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void load7BitTranslationTableFromXml() {
        Resources r = Resources.getSystem();
        if (DBG) {
            Rlog.d(TAG, "load7BitTranslationTableFromXml: open normal file");
        }
        XmlResourceParser parser = r.getXml(17891346);
        XmlUtils.beginDocument(parser, XML_START_TAG);
        while (true) {
            XmlUtils.nextElement(parser);
            String tag = parser.getName();
            if (DBG) {
                Rlog.d(TAG, "tag: " + tag);
            }
            if (XML_TRANSLATION_TYPE_TAG.equals(tag)) {
                String type = parser.getAttributeValue(null, "Type");
                if (DBG) {
                    Rlog.d(TAG, "type: " + type);
                }
                if (type.equals("common")) {
                    mTranslationTable = mTranslationTableCommon;
                } else {
                    try {
                        if (type.equals("gsm")) {
                            mTranslationTable = mTranslationTableGSM;
                        } else if (type.equals("cdma")) {
                            mTranslationTable = mTranslationTableCDMA;
                        } else {
                            Rlog.e(TAG, "Error Parsing 7BitTranslationTable: found incorrect type" + type);
                        }
                    } catch (Exception e) {
                        Rlog.e(TAG, "Got exception while loading 7BitTranslationTable file.", e);
                        if (parser instanceof XmlResourceParser) {
                            parser.close();
                            return;
                        }
                        return;
                    } catch (Throwable th) {
                        if (parser instanceof XmlResourceParser) {
                            parser.close();
                        }
                    }
                }
            } else if (XML_CHARACTOR_TAG.equals(tag) && mTranslationTable != null) {
                int from = parser.getAttributeUnsignedIntValue(null, XML_FROM_TAG, -1);
                int to = parser.getAttributeUnsignedIntValue(null, XML_TO_TAG, -1);
                if (from == -1 || to == -1) {
                    Rlog.d(TAG, "Invalid translation table file format");
                } else {
                    if (DBG) {
                        Rlog.d(TAG, "Loading mapping " + Integer.toHexString(from).toUpperCase() + " -> " + Integer.toHexString(to).toUpperCase());
                    }
                    mTranslationTable.put(from, to);
                }
            }
        }
        if (DBG) {
            Rlog.d(TAG, "load7BitTranslationTableFromXml: parsing successful, file loaded");
        }
        if (parser instanceof XmlResourceParser) {
            parser.close();
        }
    }
}

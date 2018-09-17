package com.android.internal.telephony;

import android.content.res.Resources;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.SparseIntArray;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class HwGsmAlphabet {
    public static final byte GSM_EXTENDED_ESCAPE = (byte) 27;
    private static final String TAG = "GSM";
    public static final int UDH_SEPTET_COST_CONCATENATED_MESSAGE = 6;
    public static final int UDH_SEPTET_COST_LENGTH = 1;
    public static final int UDH_SEPTET_COST_ONE_SHIFT_TABLE = 4;
    public static final int UDH_SEPTET_COST_TWO_SHIFT_TABLES = 7;
    private static final SparseIntArray[] sCharsToGsmTables;
    private static final SparseIntArray[] sCharsToShiftTables;
    private static boolean sDisableCountryEncodingCheck = false;
    private static int[] sEnabledLockingShiftTables;
    private static int[] sEnabledSingleShiftTables;
    private static int sHighestEnabledSingleShiftCode;
    private static final String[] sLanguageShiftTables = new String[]{"          \f         ^                   {}     \\            [~] |                                    €                          ", "          \f         ^                   {}     \\            [~] |      Ğ İ         Ş               ç € ğ ı         ş            ", "         ç\f         ^                   {}     \\            [~] |Á       Í     Ó     Ú           á   €   í     ó     ú          ", "     ê   ç\fÔô Áá  ΦΓ^ΩΠΨΣΘ     Ê        {}     \\            [~] |À       Í     Ó     Ú     ÃÕ    Â   €   í     ó     ú     ãõ  â", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*০১ ২৩৪৫৬৭৮৯য়ৠৡৢ{}ৣ৲৳৴৵\\৶৷৸৹৺       [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ૦૧૨૩૪૫૬૭૮૯  {}     \\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ०१२३४५६७८९॒॑{}॓॔क़ख़ग़\\ज़ड़ढ़फ़य़ॠॡॢॣ॰ॱ [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ೦೧೨೩೪೫೬೭೮೯ೞೱ{}ೲ    \\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ൦൧൨൩൪൫൬൭൮൯൰൱{}൲൳൴൵ൺ\\ൻർൽൾൿ       [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ୦୧୨୩୪୫୬୭୮୯ଡ଼ଢ଼{}ୟ୰ୱ  \\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ੦੧੨੩੪੫੬੭੮੯ਖ਼ਗ਼{}ਜ਼ੜਫ਼ੵ \\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ௦௧௨௩௪௫௬௭௮௯௳௴{}௵௶௷௸௺\\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*   ౦౧౨౩౪౫౬౭౮౯ౘౙ{}౸౹౺౻౼\\౽౾౿         [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*؀؁ ۰۱۲۳۴۵۶۷۸۹،؍{}؎؏ؐؑؒ\\ؓؔ؛؟ـْ٘٫٬ٲٳۍ[~]۔|ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          "};
    private static final String[] sLanguageTables = new String[]{"@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞ￿ÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà", "@£$¥€éùıòÇ\nĞğ\rÅåΔ_ΦΓΛΩΠΨΣΘΞ￿ŞşßÉ !\"#¤%&'()*+,-./0123456789:;<=>?İABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§çabcdefghijklmnopqrstuvwxyzäöñüà", "", "@£$¥êéúíóç\nÔô\rÁáΔ_ªÇÀ∞^\\€Ó|￿ÂâÊÉ !\"#º%&'()*+,-./0123456789:;<=>?ÍABCDEFGHIJKLMNOPQRSTUVWXYZÃÕÚÜ§~abcdefghijklmnopqrstuvwxyzãõ`üà", "ঁংঃঅআইঈউঊঋ\nঌ \r এঐ  ওঔকখগঘঙচ￿ছজঝঞ !টঠডঢণত)(থদ,ধ.ন0123456789:; পফ?বভমযর ল   শষসহ়ঽািীুূৃৄ  েৈ  োৌ্ৎabcdefghijklmnopqrstuvwxyzৗড়ঢ়ৰৱ", "ઁંઃઅઆઇઈઉઊઋ\nઌઍ\r એઐઑ ઓઔકખગઘઙચ￿છજઝઞ !ટઠડઢણત)(થદ,ધ.ન0123456789:; પફ?બભમયર લળ વશષસહ઼ઽાિીુૂૃૄૅ ેૈૉ ોૌ્ૐabcdefghijklmnopqrstuvwxyzૠૡૢૣ૱", "ँंःअआइईउऊऋ\nऌऍ\rऎएऐऑऒओऔकखगघङच￿छजझञ !टठडढणत)(थद,ध.न0123456789:;ऩपफ?बभमयरऱलळऴवशषसह़ऽािीुूृॄॅॆेैॉॊोौ्ॐabcdefghijklmnopqrstuvwxyzॲॻॼॾॿ", " ಂಃಅಆಇಈಉಊಋ\nಌ \rಎಏಐ ಒಓಔಕಖಗಘಙಚ￿ಛಜಝಞ !ಟಠಡಢಣತ)(ಥದ,ಧ.ನ0123456789:; ಪಫ?ಬಭಮಯರಱಲಳ ವಶಷಸಹ಼ಽಾಿೀುೂೃೄ ೆೇೈ ೊೋೌ್ೕabcdefghijklmnopqrstuvwxyzೖೠೡೢೣ", " ംഃഅആഇഈഉഊഋ\nഌ \rഎഏഐ ഒഓഔകഖഗഘങച￿ഛജഝഞ !ടഠഡഢണത)(ഥദ,ധ.ന0123456789:; പഫ?ബഭമയരറലളഴവശഷസഹ ഽാിീുൂൃൄ െേൈ ൊോൌ്ൗabcdefghijklmnopqrstuvwxyzൠൡൢൣ൹", "ଁଂଃଅଆଇଈଉଊଋ\nଌ \r ଏଐ  ଓଔକଖଗଘଙଚ￿ଛଜଝଞ !ଟଠଡଢଣତ)(ଥଦ,ଧ.ନ0123456789:; ପଫ?ବଭମଯର ଲଳ ଵଶଷସହ଼ଽାିୀୁୂୃୄ  େୈ  ୋୌ୍ୖabcdefghijklmnopqrstuvwxyzୗୠୡୢୣ", "ਁਂਃਅਆਇਈਉਊ \n  \r ਏਐ  ਓਔਕਖਗਘਙਚ￿ਛਜਝਞ !ਟਠਡਢਣਤ)(ਥਦ,ਧ.ਨ0123456789:; ਪਫ?ਬਭਮਯਰ ਲਲ਼ ਵਸ਼ ਸਹ਼ ਾਿੀੁੂ    ੇੈ  ੋੌ੍ੑabcdefghijklmnopqrstuvwxyzੰੱੲੳੴ", " ஂஃஅஆஇஈஉஊ \n  \rஎஏஐ ஒஓஔக   ஙச￿ ஜ ஞ !ட   ணத)(  , .ந0123456789:;னப ?  மயரறலளழவஶஷஸஹ  ாிீுூ   ெேை ொோௌ்ௐabcdefghijklmnopqrstuvwxyzௗ௰௱௲௹", "ఁంఃఅఆఇఈఉఊఋ\nఌ \rఎఏఐ ఒఓఔకఖగఘఙచ￿ఛజఝఞ !టఠడఢణత)(థద,ధ.న0123456789:; పఫ?బభమయరఱలళ వశషసహ ఽాిీుూృౄ ెేై ొోౌ్ౕabcdefghijklmnopqrstuvwxyzౖౠౡౢౣ", "اآبٻڀپڦتۂٿ\nٹٽ\rٺټثجځڄڃڅچڇحخد￿ڌڈډڊ !ڏڍذرڑړ)(ڙز,ږ.ژ0123456789:;ښسش?صضطظعفقکڪګگڳڱلمنںڻڼوۄەہھءیېےٍُِٗٔabcdefghijklmnopqrstuvwxyzّٰٕٖٓ"};

    private static class LanguagePairCount {
        final int languageCode;
        final int[] septetCounts;
        final int[] unencodableCounts;

        LanguagePairCount(int code) {
            this.languageCode = code;
            int maxSingleShiftCode = HwGsmAlphabet.sHighestEnabledSingleShiftCode;
            this.septetCounts = new int[(maxSingleShiftCode + 1)];
            this.unencodableCounts = new int[(maxSingleShiftCode + 1)];
            int tableOffset = 0;
            for (int i = 1; i <= maxSingleShiftCode; i++) {
                if (HwGsmAlphabet.sEnabledSingleShiftTables[tableOffset] == i) {
                    tableOffset++;
                } else {
                    this.septetCounts[i] = -1;
                }
            }
            if (code == 1 && maxSingleShiftCode >= 1) {
                this.septetCounts[1] = -1;
            } else if (code == 3 && maxSingleShiftCode >= 2) {
                this.septetCounts[2] = -1;
            }
        }
    }

    public static class TextEncodingDetails {
        public int codeUnitCount;
        public int codeUnitSize;
        public int codeUnitsRemaining;
        public int languageShiftTable;
        public int languageTable;
        public int msgCount;

        public String toString() {
            return "TextEncodingDetails { msgCount=" + this.msgCount + ", codeUnitCount=" + this.codeUnitCount + ", codeUnitsRemaining=" + this.codeUnitsRemaining + ", codeUnitSize=" + this.codeUnitSize + ", languageTable=" + this.languageTable + ", languageShiftTable=" + this.languageShiftTable + " }";
        }
    }

    private HwGsmAlphabet() {
    }

    public static int charToGsm(char c) {
        try {
            return charToGsm(c, false);
        } catch (EncodeException e) {
            return sCharsToGsmTables[0].get(32, 32);
        }
    }

    public static int charToGsm(char c, boolean throwException) throws EncodeException {
        int ret = sCharsToGsmTables[0].get(c, -1);
        if (ret != -1) {
            return ret;
        }
        if (sCharsToShiftTables[0].get(c, -1) != -1) {
            return 27;
        }
        if (!throwException) {
            return sCharsToGsmTables[0].get(32, 32);
        }
        throw new EncodeException(c);
    }

    public static int charToGsmExtended(char c) {
        int ret = sCharsToShiftTables[0].get(c, -1);
        if (ret == -1) {
            return sCharsToGsmTables[0].get(32, 32);
        }
        return ret;
    }

    public static char gsmToChar(int gsmChar) {
        if (gsmChar < 0 || gsmChar >= 128) {
            return ' ';
        }
        return sLanguageTables[0].charAt(gsmChar);
    }

    public static char gsmExtendedToChar(int gsmChar) {
        if (gsmChar == 27 || gsmChar < 0 || gsmChar >= 128) {
            return ' ';
        }
        char c = sLanguageShiftTables[0].charAt(gsmChar);
        if (c == ' ') {
            return sLanguageTables[0].charAt(gsmChar);
        }
        return c;
    }

    public static byte[] stringToGsm7BitPackedWithHeader(String data, byte[] header) throws EncodeException {
        return stringToGsm7BitPackedWithHeader(data, header, 0, 0);
    }

    public static byte[] stringToGsm7BitPackedWithHeader(String data, byte[] header, int languageTable, int languageShiftTable) throws EncodeException {
        if (header == null || header.length == 0) {
            return stringToGsm7BitPacked(data, languageTable, languageShiftTable);
        }
        byte[] ret = stringToGsm7BitPacked(data, (((header.length + 1) * 8) + 6) / 7, true, languageTable, languageShiftTable);
        ret[1] = (byte) header.length;
        System.arraycopy(header, 0, ret, 2, header.length);
        return ret;
    }

    public static byte[] stringToGsm7BitPacked(String data) throws EncodeException {
        return stringToGsm7BitPacked(data, 0, true, 0, 0);
    }

    public static byte[] stringToGsm7BitPacked(String data, int languageTable, int languageShiftTable) throws EncodeException {
        return stringToGsm7BitPacked(data, 0, true, languageTable, languageShiftTable);
    }

    public static byte[] stringToGsm7BitPacked(String data, int startingSeptetOffset, boolean throwException, int languageTable, int languageShiftTable) throws EncodeException {
        int dataLen = data.length();
        int septetCount = countGsmSeptetsUsingTables(data, throwException ^ 1, languageTable, languageShiftTable);
        if (septetCount == -1) {
            throw new EncodeException("countGsmSeptetsUsingTables(): unencodable char");
        }
        septetCount += startingSeptetOffset;
        if (septetCount > HwSubscriptionManager.SUB_INIT_STATE) {
            throw new EncodeException("Payload cannot exceed 255 septets");
        }
        byte[] ret = new byte[((((septetCount * 7) + 7) / 8) + 1)];
        SparseIntArray charToLanguageTable = sCharsToGsmTables[languageTable];
        SparseIntArray charToShiftTable = sCharsToShiftTables[languageShiftTable];
        int i = 0;
        int septets = startingSeptetOffset;
        int bitOffset = startingSeptetOffset * 7;
        while (i < dataLen && septets < septetCount) {
            char c = data.charAt(i);
            int v = charToLanguageTable.get(c, -1);
            if (v == -1) {
                v = charToShiftTable.get(c, -1);
                if (v != -1) {
                    packSmsChar(ret, bitOffset, 27);
                    bitOffset += 7;
                    septets++;
                } else if (throwException) {
                    throw new EncodeException("stringToGsm7BitPacked(): unencodable char");
                } else {
                    v = charToLanguageTable.get(32, 32);
                }
            }
            packSmsChar(ret, bitOffset, v);
            septets++;
            i++;
            bitOffset += 7;
        }
        ret[0] = (byte) septetCount;
        return ret;
    }

    private static void packSmsChar(byte[] packedChars, int bitOffset, int value) {
        int shift = bitOffset % 8;
        int byteOffset = (bitOffset / 8) + 1;
        packedChars[byteOffset] = (byte) (packedChars[byteOffset] | (value << shift));
        if (shift > 1) {
            packedChars[byteOffset + 1] = (byte) (value >> (8 - shift));
        }
    }

    public static String gsm7BitPackedToString(byte[] pdu, int offset, int lengthSeptets) {
        return gsm7BitPackedToString(pdu, offset, lengthSeptets, 0, 0, 0);
    }

    public static String gsm7BitPackedToString(byte[] pdu, int offset, int lengthSeptets, int numPaddingBits, int languageTable, int shiftTable) {
        StringBuilder ret = new StringBuilder(lengthSeptets);
        if (languageTable < 0 || languageTable > sLanguageTables.length) {
            Rlog.w(TAG, "unknown language table " + languageTable + ", using default");
            languageTable = 0;
        }
        if (shiftTable < 0 || shiftTable > sLanguageShiftTables.length) {
            Rlog.w(TAG, "unknown single shift table " + shiftTable + ", using default");
            shiftTable = 0;
        }
        boolean prevCharWasEscape = false;
        try {
            String languageTableToChar = sLanguageTables[languageTable];
            String shiftTableToChar = sLanguageShiftTables[shiftTable];
            if (languageTableToChar.isEmpty()) {
                Rlog.w(TAG, "no language table for code " + languageTable + ", using default");
                languageTableToChar = sLanguageTables[0];
            }
            if (shiftTableToChar.isEmpty()) {
                Rlog.w(TAG, "no single shift table for code " + shiftTable + ", using default");
                shiftTableToChar = sLanguageShiftTables[0];
            }
            for (int i = 0; i < lengthSeptets; i++) {
                int bitOffset = (i * 7) + numPaddingBits;
                int byteOffset = bitOffset / 8;
                int shift = bitOffset % 8;
                int gsmVal = (pdu[offset + byteOffset] >> shift) & 127;
                if (shift > 1) {
                    gsmVal = (gsmVal & (127 >> (shift - 1))) | ((pdu[(offset + byteOffset) + 1] << (8 - shift)) & 127);
                }
                if (prevCharWasEscape) {
                    if (gsmVal == 27) {
                        ret.append(' ');
                    } else {
                        char c = shiftTableToChar.charAt(gsmVal);
                        if (c == ' ') {
                            ret.append(languageTableToChar.charAt(gsmVal));
                        } else {
                            ret.append(c);
                        }
                    }
                    prevCharWasEscape = false;
                } else if (gsmVal == 27) {
                    prevCharWasEscape = true;
                } else {
                    ret.append(languageTableToChar.charAt(gsmVal));
                }
            }
            return ret.toString();
        } catch (RuntimeException ex) {
            Rlog.e(TAG, "Error GSM 7 bit packed: ", ex);
            return null;
        }
    }

    public static String gsm8BitUnpackedToString(byte[] data, int offset, int length) {
        return gsm8BitUnpackedToString(data, offset, length, "");
    }

    public static String gsm8BitUnpackedToString(byte[] data, int offset, int length, String characterset) {
        boolean isMbcs = false;
        Charset charset = null;
        ByteBuffer mbcsBuffer = null;
        if (!TextUtils.isEmpty(characterset)) {
            if ((characterset.equalsIgnoreCase("us-ascii") ^ 1) != 0 && Charset.isSupported(characterset)) {
                isMbcs = true;
                charset = Charset.forName(characterset);
                mbcsBuffer = ByteBuffer.allocate(2);
            }
        }
        String languageTableToChar = sLanguageTables[0];
        String shiftTableToChar = sLanguageShiftTables[0];
        StringBuilder ret = new StringBuilder(length);
        boolean prevWasEscape = false;
        int i = offset;
        while (true) {
            int i2 = i;
            if (i2 >= offset + length) {
                break;
            }
            int c = data[i2] & HwSubscriptionManager.SUB_INIT_STATE;
            if (c == HwSubscriptionManager.SUB_INIT_STATE) {
                break;
            }
            if (c != 27) {
                if (prevWasEscape) {
                    char shiftChar = shiftTableToChar.charAt(c);
                    if (shiftChar == ' ') {
                        ret.append(languageTableToChar.charAt(c));
                        i = i2;
                    } else {
                        ret.append(shiftChar);
                        i = i2;
                    }
                } else if (!isMbcs || c < 128 || i2 + 1 >= offset + length) {
                    ret.append(languageTableToChar.charAt(c));
                    i = i2;
                } else {
                    mbcsBuffer.clear();
                    i = i2 + 1;
                    mbcsBuffer.put(data, i2, 2);
                    mbcsBuffer.flip();
                    ret.append(charset.decode(mbcsBuffer).toString());
                }
                prevWasEscape = false;
            } else if (prevWasEscape) {
                ret.append(' ');
                prevWasEscape = false;
                i = i2;
            } else {
                prevWasEscape = true;
                i = i2;
            }
            i++;
        }
        return ret.toString();
    }

    public static String gsm8BitUnpackedToString(byte[] data, int offset, int length, boolean needConvertCharacter) {
        return gsm8BitUnpackedToString(data, offset, length, "", needConvertCharacter);
    }

    public static String gsm8BitUnpackedToString(byte[] data, int offset, int length, String characterset, boolean needConvertCharacter) {
        boolean isMbcs = false;
        Charset charset = null;
        ByteBuffer mbcsBuffer = null;
        if (!(TextUtils.isEmpty(characterset) || (characterset.equalsIgnoreCase("us-ascii") ^ 1) == 0 || !Charset.isSupported(characterset))) {
            isMbcs = true;
            charset = Charset.forName(characterset);
            mbcsBuffer = ByteBuffer.allocate(2);
        }
        StringBuilder ret = new StringBuilder(length);
        boolean prevWasEscape = false;
        int i = offset;
        while (true) {
            int i2 = i;
            if (i2 >= offset + length) {
                break;
            }
            int c = data[i2] & HwSubscriptionManager.SUB_INIT_STATE;
            if (c == HwSubscriptionManager.SUB_INIT_STATE) {
                break;
            }
            if (c != 27) {
                if (needConvertCharacter) {
                    if (95 == c) {
                        c = 17;
                    }
                    if (64 == c) {
                        c = 0;
                    }
                }
                if (prevWasEscape) {
                    ret.append(gsmExtendedToChar(c));
                    i = i2;
                } else if (!isMbcs || c < 128 || i2 + 1 >= offset + length) {
                    ret.append(gsmToChar(c));
                    i = i2;
                } else {
                    mbcsBuffer.clear();
                    i = i2 + 1;
                    mbcsBuffer.put(data, i2, 2);
                    mbcsBuffer.flip();
                    ret.append(charset.decode(mbcsBuffer).toString());
                }
                prevWasEscape = false;
            } else if (prevWasEscape) {
                ret.append(' ');
                prevWasEscape = false;
                i = i2;
            } else {
                prevWasEscape = true;
                i = i2;
            }
            i++;
        }
        return ret.toString();
    }

    public static byte[] stringToGsm8BitPacked(String s) {
        byte[] ret = new byte[countGsmSeptetsUsingTables(s, true, 0, 0)];
        stringToGsm8BitUnpackedField(s, ret, 0, ret.length);
        return ret;
    }

    public static void stringToGsm8BitUnpackedField(String s, byte[] dest, int offset, int length) {
        int outByteIndex = offset;
        SparseIntArray charToLanguageTable = sCharsToGsmTables[0];
        SparseIntArray charToShiftTable = sCharsToShiftTables[0];
        int sz = s.length();
        int outByteIndex2 = outByteIndex;
        for (int i = 0; i < sz && outByteIndex2 - offset < length; i++) {
            char c = s.charAt(i);
            int v = charToLanguageTable.get(c, -1);
            if (v == -1) {
                v = charToShiftTable.get(c, -1);
                if (v == -1) {
                    v = charToLanguageTable.get(32, 32);
                    outByteIndex = outByteIndex2;
                } else if ((outByteIndex2 + 1) - offset >= length) {
                    break;
                } else {
                    outByteIndex = outByteIndex2 + 1;
                    dest[outByteIndex2] = GSM_EXTENDED_ESCAPE;
                }
            } else {
                outByteIndex = outByteIndex2;
            }
            outByteIndex2 = outByteIndex + 1;
            dest[outByteIndex] = (byte) v;
        }
        while (outByteIndex2 - offset < length) {
            outByteIndex = outByteIndex2 + 1;
            dest[outByteIndex2] = (byte) -1;
            outByteIndex2 = outByteIndex;
        }
    }

    public static int UCStoGsm7(char c) {
        if (sCharsToGsmTables[0].get(c, -1) != -1) {
            return 1;
        }
        if (sCharsToShiftTables[0].get(c, -1) != -1) {
            return 2;
        }
        return -1;
    }

    public static int charToGsm7bit(char c) {
        int ret = sCharsToGsmTables[0].get(c, -1);
        if (-1 != ret) {
            return ret;
        }
        if (-1 == sCharsToShiftTables[0].get(c, -1)) {
            return -1;
        }
        return 27;
    }

    public static byte[] stringToUCS81Packed(String s, char baser81, int septets) {
        byte[] dest = new byte[septets];
        dest[0] = (byte) ((baser81 & 32640) >> 7);
        int i = 0;
        int sz = s.length();
        int outByteIndex = 1;
        while (i < sz) {
            int outByteIndex2;
            char c = s.charAt(i);
            int v = charToGsm7bit(c);
            if (-1 == v) {
                outByteIndex2 = outByteIndex + 1;
                dest[outByteIndex] = (byte) ((c & 127) | 128);
            } else if (v == 27) {
                outByteIndex2 = outByteIndex + 1;
                dest[outByteIndex] = GSM_EXTENDED_ESCAPE;
                outByteIndex = outByteIndex2 + 1;
                dest[outByteIndex2] = (byte) GsmAlphabet.charToGsmExtended(c);
                outByteIndex2 = outByteIndex;
            } else {
                outByteIndex2 = outByteIndex + 1;
                dest[outByteIndex] = (byte) v;
            }
            i++;
            outByteIndex = outByteIndex2;
        }
        return dest;
    }

    public static byte[] stringToUCS82Packed(String s, char baser82Low, int septets) {
        byte[] dest = new byte[septets];
        dest[0] = (byte) ((baser82Low >> 8) & HwSubscriptionManager.SUB_INIT_STATE);
        dest[1] = (byte) (baser82Low & HwSubscriptionManager.SUB_INIT_STATE);
        int i = 0;
        int sz = s.length();
        int outByteIndex = 2;
        while (i < sz) {
            int outByteIndex2;
            char c = s.charAt(i);
            int v = charToGsm7bit(c);
            if (-1 == v) {
                outByteIndex2 = outByteIndex + 1;
                dest[outByteIndex] = (byte) ((((c & HwSubscriptionManager.SUB_INIT_STATE) - (baser82Low & HwSubscriptionManager.SUB_INIT_STATE)) & 127) | 128);
            } else if (v == 27) {
                outByteIndex2 = outByteIndex + 1;
                dest[outByteIndex] = GSM_EXTENDED_ESCAPE;
                outByteIndex = outByteIndex2 + 1;
                dest[outByteIndex2] = (byte) GsmAlphabet.charToGsmExtended(c);
                outByteIndex2 = outByteIndex;
            } else {
                outByteIndex2 = outByteIndex + 1;
                dest[outByteIndex] = (byte) v;
            }
            i++;
            outByteIndex = outByteIndex2;
        }
        return dest;
    }

    public static int countGsmSeptets(char c) {
        try {
            return countGsmSeptets(c, false);
        } catch (EncodeException e) {
            return 0;
        }
    }

    public static int countGsmSeptets(char c, boolean throwsException) throws EncodeException {
        if (sCharsToGsmTables[0].get(c, -1) != -1) {
            return 1;
        }
        if (sCharsToShiftTables[0].get(c, -1) != -1) {
            return 2;
        }
        if (!throwsException) {
            return 1;
        }
        throw new EncodeException(c);
    }

    public static int countGsmSeptetsUsingTables(CharSequence s, boolean use7bitOnly, int languageTable, int languageShiftTable) {
        int count = 0;
        int sz = s.length();
        SparseIntArray charToLanguageTable = sCharsToGsmTables[languageTable];
        SparseIntArray charToShiftTable = sCharsToShiftTables[languageShiftTable];
        for (int i = 0; i < sz; i++) {
            char c = s.charAt(i);
            if (c == 27) {
                Rlog.w(TAG, "countGsmSeptets() string contains Escape character, skipping.");
            } else if (charToLanguageTable.get(c, -1) != -1) {
                count++;
            } else if (charToShiftTable.get(c, -1) != -1) {
                count += 2;
            } else if (!use7bitOnly) {
                return -1;
            } else {
                count++;
            }
        }
        return count;
    }

    public static TextEncodingDetails countGsmSeptets(CharSequence s, boolean use7bitOnly) {
        if (!sDisableCountryEncodingCheck) {
            enableCountrySpecificEncodings();
        }
        TextEncodingDetails ted;
        int septets;
        if (sEnabledSingleShiftTables.length + sEnabledLockingShiftTables.length == 0) {
            ted = new TextEncodingDetails();
            septets = GsmAlphabet.countGsmSeptetsUsingTables(s, use7bitOnly, 0, 0);
            if (septets == -1) {
                return null;
            }
            ted.codeUnitSize = 1;
            ted.codeUnitCount = septets;
            if (septets > 160) {
                ted.msgCount = (septets + 152) / 153;
                ted.codeUnitsRemaining = (ted.msgCount * 153) - septets;
            } else {
                ted.msgCount = 1;
                ted.codeUnitsRemaining = 160 - septets;
            }
            ted.codeUnitSize = 1;
            return ted;
        }
        int i;
        int maxSingleShiftCode = sHighestEnabledSingleShiftCode;
        List<LanguagePairCount> lpcList = new ArrayList(sEnabledLockingShiftTables.length + 1);
        lpcList.add(new LanguagePairCount(0));
        for (int i2 : sEnabledLockingShiftTables) {
            if (!(i2 == 0 || (sLanguageTables[i2].isEmpty() ^ 1) == 0)) {
                lpcList.add(new LanguagePairCount(i2));
            }
        }
        int sz = s.length();
        boolean hasLpList = lpcList.isEmpty();
        for (i2 = 0; i2 < sz && (hasLpList ^ 1) != 0; i2++) {
            char c = s.charAt(i2);
            if (c == 27) {
                Rlog.w(TAG, "countGsmSeptets() string contains Escape character, ignoring!");
            } else {
                for (LanguagePairCount lpc : lpcList) {
                    int table;
                    int[] iArr;
                    if (sCharsToGsmTables[lpc.languageCode].get(c, -1) == -1) {
                        for (table = 0; table <= maxSingleShiftCode; table++) {
                            if (lpc.septetCounts[table] != -1) {
                                if (sCharsToShiftTables[table].get(c, -1) != -1) {
                                    iArr = lpc.septetCounts;
                                    iArr[table] = iArr[table] + 2;
                                } else if (use7bitOnly) {
                                    iArr = lpc.septetCounts;
                                    iArr[table] = iArr[table] + 1;
                                    iArr = lpc.unencodableCounts;
                                    iArr[table] = iArr[table] + 1;
                                } else {
                                    lpc.septetCounts[table] = -1;
                                }
                            }
                        }
                    } else {
                        for (table = 0; table <= maxSingleShiftCode; table++) {
                            if (lpc.septetCounts[table] != -1) {
                                iArr = lpc.septetCounts;
                                iArr[table] = iArr[table] + 1;
                            }
                        }
                    }
                }
            }
        }
        ted = new TextEncodingDetails();
        ted.msgCount = Integer.MAX_VALUE;
        ted.codeUnitSize = 1;
        int minUnencodableCount = Integer.MAX_VALUE;
        for (LanguagePairCount lpc2 : lpcList) {
            int shiftTable = 0;
            while (shiftTable <= maxSingleShiftCode) {
                septets = lpc2.septetCounts[shiftTable];
                if (septets != -1) {
                    int udhLength;
                    int msgCount;
                    int septetsRemaining;
                    if (lpc2.languageCode != 0 && shiftTable != 0) {
                        udhLength = 8;
                    } else if (lpc2.languageCode == 0 && shiftTable == 0) {
                        udhLength = 0;
                    } else {
                        udhLength = 5;
                    }
                    if (septets + udhLength > 160) {
                        if (udhLength == 0) {
                            udhLength = 1;
                        }
                        int septetsPerMessage = 160 - (udhLength + 6);
                        msgCount = ((septets + septetsPerMessage) - 1) / septetsPerMessage;
                        septetsRemaining = (msgCount * septetsPerMessage) - septets;
                    } else {
                        msgCount = 1;
                        septetsRemaining = (160 - udhLength) - septets;
                    }
                    int unencodableCount = lpc2.unencodableCounts[shiftTable];
                    if ((!use7bitOnly || unencodableCount <= minUnencodableCount) && ((use7bitOnly && unencodableCount < minUnencodableCount) || msgCount < ted.msgCount || (msgCount == ted.msgCount && septetsRemaining > ted.codeUnitsRemaining))) {
                        minUnencodableCount = unencodableCount;
                        ted.msgCount = msgCount;
                        ted.codeUnitCount = septets;
                        ted.codeUnitsRemaining = septetsRemaining;
                        ted.languageTable = lpc2.languageCode;
                        ted.languageShiftTable = shiftTable;
                    }
                }
                shiftTable++;
            }
        }
        if (ted.msgCount == Integer.MAX_VALUE) {
            return null;
        }
        return ted;
    }

    public static int findGsmSeptetLimitIndex(String s, int start, int limit, int langTable, int langShiftTable) {
        int accumulator = 0;
        int size = s.length();
        SparseIntArray charToLangTable = sCharsToGsmTables[langTable];
        SparseIntArray charToLangShiftTable = sCharsToShiftTables[langShiftTable];
        for (int i = start; i < size; i++) {
            if (charToLangTable.get(s.charAt(i), -1) != -1) {
                accumulator++;
            } else if (charToLangShiftTable.get(s.charAt(i), -1) == -1) {
                accumulator++;
            } else {
                accumulator += 2;
            }
            if (accumulator > limit) {
                return i;
            }
        }
        return size;
    }

    static synchronized void setEnabledSingleShiftTables(int[] tables) {
        synchronized (HwGsmAlphabet.class) {
            sEnabledSingleShiftTables = tables;
            sDisableCountryEncodingCheck = true;
            if (tables.length > 0) {
                sHighestEnabledSingleShiftCode = tables[tables.length - 1];
            } else {
                sHighestEnabledSingleShiftCode = 0;
            }
        }
    }

    static synchronized void setEnabledLockingShiftTables(int[] tables) {
        synchronized (HwGsmAlphabet.class) {
            sEnabledLockingShiftTables = tables;
            sDisableCountryEncodingCheck = true;
        }
    }

    static synchronized int[] getEnabledSingleShiftTables() {
        int[] iArr;
        synchronized (HwGsmAlphabet.class) {
            iArr = sEnabledSingleShiftTables;
        }
        return iArr;
    }

    static synchronized int[] getEnabledLockingShiftTables() {
        int[] iArr;
        synchronized (HwGsmAlphabet.class) {
            iArr = sEnabledLockingShiftTables;
        }
        return iArr;
    }

    private static void enableCountrySpecificEncodings() {
        Resources r = Resources.getSystem();
        sEnabledSingleShiftTables = getSingleShiftTable(r);
        sEnabledLockingShiftTables = r.getIntArray(17236031);
        if (sEnabledSingleShiftTables.length > 0) {
            sHighestEnabledSingleShiftCode = sEnabledSingleShiftTables[sEnabledSingleShiftTables.length - 1];
        } else {
            sHighestEnabledSingleShiftCode = 0;
        }
    }

    static {
        int i;
        int j;
        enableCountrySpecificEncodings();
        int numTables = sLanguageTables.length;
        int numShiftTables = sLanguageShiftTables.length;
        if (numTables != numShiftTables) {
            Rlog.e(TAG, "Error: language tables array length " + numTables + " != shift tables array length " + numShiftTables);
        }
        sCharsToGsmTables = new SparseIntArray[numTables];
        for (i = 0; i < numTables; i++) {
            String table = sLanguageTables[i];
            int tableLen = table.length();
            if (!(tableLen == 0 || tableLen == 128)) {
                Rlog.e(TAG, "Error: language tables index " + i + " length " + tableLen + " (expected 128 or 0)");
            }
            SparseIntArray charToGsmTable = new SparseIntArray(tableLen);
            sCharsToGsmTables[i] = charToGsmTable;
            for (j = 0; j < tableLen; j++) {
                charToGsmTable.put(table.charAt(j), j);
            }
        }
        sCharsToShiftTables = new SparseIntArray[numTables];
        for (i = 0; i < numShiftTables; i++) {
            String shiftTable = sLanguageShiftTables[i];
            int shiftTableLen = shiftTable.length();
            if (!(shiftTableLen == 0 || shiftTableLen == 128)) {
                Rlog.e(TAG, "Error: language shift tables index " + i + " length " + shiftTableLen + " (expected 128 or 0)");
            }
            SparseIntArray charToShiftTable = new SparseIntArray(shiftTableLen);
            sCharsToShiftTables[i] = charToShiftTable;
            for (j = 0; j < shiftTableLen; j++) {
                char c = shiftTable.charAt(j);
                if (c != ' ') {
                    charToShiftTable.put(c, j);
                }
            }
        }
    }

    private static int[] getSingleShiftTable(Resources r) {
        int[] temp = new int[1];
        if (SystemProperties.getInt("ro.config.smsCoding_National", 0) != 0) {
            temp[0] = SystemProperties.getInt("ro.config.smsCoding_National", 0);
        } else if (SystemProperties.getInt("gsm.sms.coding.national", 0) == 0) {
            return r.getIntArray(17236032);
        } else {
            temp[0] = SystemProperties.getInt("gsm.sms.coding.national", 0);
        }
        return temp;
    }

    public static char util_UnicodeToGsm7DefaultExtended(char c) {
        switch (c) {
            case 12:
                return 10;
            case '[':
                return '<';
            case '\\':
                return '/';
            case ']':
                return '>';
            case '^':
                return 20;
            case '{':
                return '(';
            case '|':
                return '@';
            case '}':
                return ')';
            case '~':
                return '=';
            case 8364:
                return 'e';
            default:
                return c;
        }
    }

    public static char ussd_7bit_ucs2_to_gsm_char_default(char c) {
        switch (c) {
            case 192:
            case 193:
            case 194:
            case 195:
            case 196:
                return 'A';
            case 200:
            case 201:
            case 202:
            case 203:
                return 'E';
            case 204:
            case 205:
            case 206:
            case 207:
                return 'I';
            case 208:
                return 'D';
            case 209:
                return 228;
            case 210:
            case 211:
            case 212:
            case 213:
                return 'O';
            case 217:
            case 218:
            case 219:
            case 220:
                return 'U';
            case 221:
            case 222:
                return 'Y';
            case 224:
            case 225:
            case 226:
            case 227:
                return 'a';
            case 231:
                return 'c';
            case 232:
            case 233:
            case 234:
            case 235:
            case 240:
                return 'e';
            case 237:
            case 238:
            case 239:
                return 'i';
            case 242:
            case 243:
            case 244:
            case 245:
                return 'o';
            case 250:
            case 251:
            case 252:
                return 'u';
            case 253:
            case 254:
                return 'y';
            default:
                return c;
        }
    }
}

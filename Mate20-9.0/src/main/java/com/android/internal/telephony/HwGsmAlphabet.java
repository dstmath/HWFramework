package com.android.internal.telephony;

import android.content.res.Resources;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.SparseIntArray;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class HwGsmAlphabet {
    public static final byte GSM_EXTENDED_ESCAPE = 27;
    private static final String TAG = "GSM";
    public static final int UDH_SEPTET_COST_CONCATENATED_MESSAGE = 6;
    public static final int UDH_SEPTET_COST_LENGTH = 1;
    public static final int UDH_SEPTET_COST_ONE_SHIFT_TABLE = 4;
    public static final int UDH_SEPTET_COST_TWO_SHIFT_TABLES = 7;
    private static final SparseIntArray[] sCharsToGsmTables;
    private static final SparseIntArray[] sCharsToShiftTables;
    private static boolean sDisableCountryEncodingCheck = false;
    private static int[] sEnabledLockingShiftTables;
    /* access modifiers changed from: private */
    public static int[] sEnabledSingleShiftTables;
    /* access modifiers changed from: private */
    public static int sHighestEnabledSingleShiftCode;
    private static final String[] sLanguageShiftTables = {"          \f         ^                   {}     \\            [~] |                                    €                          ", "          \f         ^                   {}     \\            [~] |      Ğ İ         Ş               ç € ğ ı         ş            ", "         ç\f         ^                   {}     \\            [~] |Á       Í     Ó     Ú           á   €   í     ó     ú          ", "     ê   ç\fÔô Áá  ΦΓ^ΩΠΨΣΘ     Ê        {}     \\            [~] |À       Í     Ó     Ú     ÃÕ    Â   €   í     ó     ú     ãõ  â", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*০১ ২৩৪৫৬৭৮৯য়ৠৡৢ{}ৣ৲৳৴৵\\৶৷৸৹৺       [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ૦૧૨૩૪૫૬૭૮૯  {}     \\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ०१२३४५६७८९॒॑{}॓॔क़ख़ग़\\ज़ड़ढ़फ़य़ॠॡॢॣ॰ॱ [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ೦೧೨೩೪೫೬೭೮೯ೞೱ{}ೲ    \\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ൦൧൨൩൪൫൬൭൮൯൰൱{}൲൳൴൵ൺ\\ൻർൽൾൿ       [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ୦୧୨୩୪୫୬୭୮୯ଡ଼ଢ଼{}ୟ୰ୱ  \\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ੦੧੨੩੪੫੬੭੮੯ਖ਼ਗ਼{}ਜ਼ੜਫ਼ੵ \\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ௦௧௨௩௪௫௬௭௮௯௳௴{}௵௶௷௸௺\\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*   ౦౧౨౩౪౫౬౭౮౯ౘౙ{}౸౹౺౻౼\\౽౾౿         [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*؀؁ ۰۱۲۳۴۵۶۷۸۹،؍{}؎؏ؐؑؒ\\ؓؔ؛؟ـْ٘٫٬ٲٳۍ[~]۔|ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          "};
    private static final String[] sLanguageTables = {"@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞ￿ÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà", "@£$¥€éùıòÇ\nĞğ\rÅåΔ_ΦΓΛΩΠΨΣΘΞ￿ŞşßÉ !\"#¤%&'()*+,-./0123456789:;<=>?İABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§çabcdefghijklmnopqrstuvwxyzäöñüà", "", "@£$¥êéúíóç\nÔô\rÁáΔ_ªÇÀ∞^\\€Ó|￿ÂâÊÉ !\"#º%&'()*+,-./0123456789:;<=>?ÍABCDEFGHIJKLMNOPQRSTUVWXYZÃÕÚÜ§~abcdefghijklmnopqrstuvwxyzãõ`üà", "ঁংঃঅআইঈউঊঋ\nঌ \r এঐ  ওঔকখগঘঙচ￿ছজঝঞ !টঠডঢণত)(থদ,ধ.ন0123456789:; পফ?বভমযর ল   শষসহ়ঽািীুূৃৄ  েৈ  োৌ্ৎabcdefghijklmnopqrstuvwxyzৗড়ঢ়ৰৱ", "ઁંઃઅઆઇઈઉઊઋ\nઌઍ\r એઐઑ ઓઔકખગઘઙચ￿છજઝઞ !ટઠડઢણત)(થદ,ધ.ન0123456789:; પફ?બભમયર લળ વશષસહ઼ઽાિીુૂૃૄૅ ેૈૉ ોૌ્ૐabcdefghijklmnopqrstuvwxyzૠૡૢૣ૱", "ँंःअआइईउऊऋ\nऌऍ\rऎएऐऑऒओऔकखगघङच￿छजझञ !टठडढणत)(थद,ध.न0123456789:;ऩपफ?बभमयरऱलळऴवशषसह़ऽािीुूृॄॅॆेैॉॊोौ्ॐabcdefghijklmnopqrstuvwxyzॲॻॼॾॿ", " ಂಃಅಆಇಈಉಊಋ\nಌ \rಎಏಐ ಒಓಔಕಖಗಘಙಚ￿ಛಜಝಞ !ಟಠಡಢಣತ)(ಥದ,ಧ.ನ0123456789:; ಪಫ?ಬಭಮಯರಱಲಳ ವಶಷಸಹ಼ಽಾಿೀುೂೃೄ ೆೇೈ ೊೋೌ್ೕabcdefghijklmnopqrstuvwxyzೖೠೡೢೣ", " ംഃഅആഇഈഉഊഋ\nഌ \rഎഏഐ ഒഓഔകഖഗഘങച￿ഛജഝഞ !ടഠഡഢണത)(ഥദ,ധ.ന0123456789:; പഫ?ബഭമയരറലളഴവശഷസഹ ഽാിീുൂൃൄ െേൈ ൊോൌ്ൗabcdefghijklmnopqrstuvwxyzൠൡൢൣ൹", "ଁଂଃଅଆଇଈଉଊଋ\nଌ \r ଏଐ  ଓଔକଖଗଘଙଚ￿ଛଜଝଞ !ଟଠଡଢଣତ)(ଥଦ,ଧ.ନ0123456789:; ପଫ?ବଭମଯର ଲଳ ଵଶଷସହ଼ଽାିୀୁୂୃୄ  େୈ  ୋୌ୍ୖabcdefghijklmnopqrstuvwxyzୗୠୡୢୣ", "ਁਂਃਅਆਇਈਉਊ \n  \r ਏਐ  ਓਔਕਖਗਘਙਚ￿ਛਜਝਞ !ਟਠਡਢਣਤ)(ਥਦ,ਧ.ਨ0123456789:; ਪਫ?ਬਭਮਯਰ ਲਲ਼ ਵਸ਼ ਸਹ਼ ਾਿੀੁੂ    ੇੈ  ੋੌ੍ੑabcdefghijklmnopqrstuvwxyzੰੱੲੳੴ", " ஂஃஅஆஇஈஉஊ \n  \rஎஏஐ ஒஓஔக   ஙச￿ ஜ ஞ !ட   ணத)(  , .ந0123456789:;னப ?  மயரறலளழவஶஷஸஹ  ாிீுூ   ெேை ொோௌ்ௐabcdefghijklmnopqrstuvwxyzௗ௰௱௲௹", "ఁంఃఅఆఇఈఉఊఋ\nఌ \rఎఏఐ ఒఓఔకఖగఘఙచ￿ఛజఝఞ !టఠడఢణత)(థద,ధ.న0123456789:; పఫ?బభమయరఱలళ వశషసహ ఽాిీుూృౄ ెేై ొోౌ్ౕabcdefghijklmnopqrstuvwxyzౖౠౡౢౣ", "اآبٻڀپڦتۂٿ\nٹٽ\rٺټثجځڄڃڅچڇحخد￿ڌڈډڊ !ڏڍذرڑړ)(ڙز,ږ.ژ0123456789:;ښسش?صضطظعفقکڪګگڳڱلمنںڻڼوۄەہھءیېےٍُِٗٔabcdefghijklmnopqrstuvwxyzّٰٕٖٓ"};

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
        String str = data;
        int i = languageTable;
        int i2 = languageShiftTable;
        int dataLen = str.length();
        int septetCount = countGsmSeptetsUsingTables(str, !throwException, i, i2);
        int i3 = -1;
        if (septetCount != -1) {
            int septetCount2 = septetCount + startingSeptetOffset;
            if (septetCount2 <= 255) {
                byte[] ret = new byte[((((septetCount2 * 7) + 7) / 8) + 1)];
                SparseIntArray charToLanguageTable = sCharsToGsmTables[i];
                SparseIntArray charToShiftTable = sCharsToShiftTables[i2];
                int i4 = 0;
                int septets = startingSeptetOffset;
                int bitOffset = startingSeptetOffset * 7;
                while (i4 < dataLen && septets < septetCount2) {
                    char c = str.charAt(i4);
                    int v = charToLanguageTable.get(c, i3);
                    if (v == i3) {
                        int v2 = charToShiftTable.get(c, i3);
                        if (v2 != i3) {
                            packSmsChar(ret, bitOffset, 27);
                            bitOffset += 7;
                            septets++;
                            v = v2;
                        } else if (!throwException) {
                            v = charToLanguageTable.get(32, 32);
                        } else {
                            int i5 = v2;
                            throw new EncodeException("stringToGsm7BitPacked(): unencodable char");
                        }
                    }
                    packSmsChar(ret, bitOffset, v);
                    septets++;
                    i4++;
                    bitOffset += 7;
                    str = data;
                    i3 = -1;
                }
                ret[0] = (byte) septetCount2;
                return ret;
            }
            throw new EncodeException("Payload cannot exceed 255 septets");
        }
        throw new EncodeException("countGsmSeptetsUsingTables(): unencodable char");
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
        int i = lengthSeptets;
        int i2 = languageTable;
        int shiftTable2 = shiftTable;
        StringBuilder ret = new StringBuilder(i);
        if (i2 < 0 || i2 > sLanguageTables.length) {
            Rlog.w(TAG, "unknown language table " + i2 + ", using default");
            i2 = 0;
        }
        int languageTable2 = i2;
        if (shiftTable2 < 0 || shiftTable2 > sLanguageShiftTables.length) {
            Rlog.w(TAG, "unknown single shift table " + shiftTable2 + ", using default");
            shiftTable2 = 0;
        }
        boolean prevCharWasEscape = false;
        try {
            String languageTableToChar = sLanguageTables[languageTable2];
            String shiftTableToChar = sLanguageShiftTables[shiftTable2];
            int i3 = 0;
            if (languageTableToChar.isEmpty()) {
                Rlog.w(TAG, "no language table for code " + languageTable2 + ", using default");
                languageTableToChar = sLanguageTables[0];
            }
            if (shiftTableToChar.isEmpty()) {
                Rlog.w(TAG, "no single shift table for code " + shiftTable2 + ", using default");
                shiftTableToChar = sLanguageShiftTables[0];
            }
            while (true) {
                int i4 = i3;
                if (i4 >= i) {
                    return ret.toString();
                }
                int bitOffset = (7 * i4) + numPaddingBits;
                int byteOffset = bitOffset / 8;
                int shift = bitOffset % 8;
                int gsmVal = (pdu[offset + byteOffset] >> shift) & 127;
                if (shift > 1) {
                    gsmVal = (gsmVal & (127 >> (shift - 1))) | ((pdu[(offset + byteOffset) + 1] << (8 - shift)) & 127);
                }
                if (prevCharWasEscape) {
                    boolean z = prevCharWasEscape;
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
                } else {
                    boolean prevCharWasEscape2 = prevCharWasEscape;
                    if (gsmVal == 27) {
                        prevCharWasEscape = true;
                    } else {
                        ret.append(languageTableToChar.charAt(gsmVal));
                        prevCharWasEscape = prevCharWasEscape2;
                        i3 = i4 + 1;
                    }
                }
                i3 = i4 + 1;
            }
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
        if (!TextUtils.isEmpty(characterset) && !characterset.equalsIgnoreCase("us-ascii") && Charset.isSupported(characterset)) {
            isMbcs = true;
            charset = Charset.forName(characterset);
            mbcsBuffer = ByteBuffer.allocate(2);
        }
        String languageTableToChar = sLanguageTables[0];
        String shiftTableToChar = sLanguageShiftTables[0];
        StringBuilder ret = new StringBuilder(length);
        boolean prevWasEscape = false;
        int i = offset;
        while (i < offset + length) {
            int c = data[i] & HwSubscriptionManager.SUB_INIT_STATE;
            if (c == 255) {
                break;
            }
            if (c != 27) {
                if (prevWasEscape) {
                    char shiftChar = shiftTableToChar.charAt(c);
                    if (shiftChar == ' ') {
                        ret.append(languageTableToChar.charAt(c));
                    } else {
                        ret.append(shiftChar);
                    }
                } else if (!isMbcs || c < 128 || i + 1 >= offset + length) {
                    ret.append(languageTableToChar.charAt(c));
                } else {
                    mbcsBuffer.clear();
                    mbcsBuffer.put(data, i, 2);
                    mbcsBuffer.flip();
                    ret.append(charset.decode(mbcsBuffer).toString());
                    i++;
                }
                prevWasEscape = false;
            } else if (prevWasEscape) {
                ret.append(' ');
                prevWasEscape = false;
            } else {
                prevWasEscape = true;
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
        if (!TextUtils.isEmpty(characterset) && !characterset.equalsIgnoreCase("us-ascii") && Charset.isSupported(characterset)) {
            isMbcs = true;
            charset = Charset.forName(characterset);
            mbcsBuffer = ByteBuffer.allocate(2);
        }
        StringBuilder ret = new StringBuilder(length);
        boolean prevWasEscape = false;
        int i = offset;
        while (i < offset + length) {
            int c = data[i] & HwSubscriptionManager.SUB_INIT_STATE;
            if (c == 255) {
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
                } else if (!isMbcs || c < 128 || i + 1 >= offset + length) {
                    ret.append(gsmToChar(c));
                } else {
                    mbcsBuffer.clear();
                    mbcsBuffer.put(data, i, 2);
                    mbcsBuffer.flip();
                    ret.append(charset.decode(mbcsBuffer).toString());
                    i++;
                }
                prevWasEscape = false;
            } else if (prevWasEscape) {
                ret.append(' ');
                prevWasEscape = false;
            } else {
                prevWasEscape = true;
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
        int i = 0;
        int sz = s.length();
        while (i < sz && outByteIndex - offset < length) {
            char c = s.charAt(i);
            int v = charToLanguageTable.get(c, -1);
            if (v == -1) {
                v = charToShiftTable.get(c, -1);
                if (v == -1) {
                    v = charToLanguageTable.get(32, 32);
                } else if ((outByteIndex + 1) - offset >= length) {
                    break;
                } else {
                    dest[outByteIndex] = GSM_EXTENDED_ESCAPE;
                    outByteIndex++;
                }
            }
            dest[outByteIndex] = (byte) v;
            i++;
            outByteIndex++;
        }
        while (outByteIndex - offset < length) {
            dest[outByteIndex] = -1;
            outByteIndex++;
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
        int outByteIndex;
        byte[] dest = new byte[septets];
        dest[0] = (byte) ((baser81 & 32640) >> 7);
        int outByteIndex2 = 1;
        int sz = s.length();
        for (int i = 0; i < sz; i++) {
            char c = s.charAt(i);
            int v = charToGsm7bit(c);
            if (-1 == v) {
                outByteIndex = outByteIndex2 + 1;
                dest[outByteIndex2] = (byte) (128 | (c & 127));
            } else if (v == 27) {
                int outByteIndex3 = outByteIndex2 + 1;
                dest[outByteIndex2] = GSM_EXTENDED_ESCAPE;
                dest[outByteIndex3] = (byte) GsmAlphabet.charToGsmExtended(c);
                outByteIndex2 = outByteIndex3 + 1;
            } else {
                outByteIndex = outByteIndex2 + 1;
                dest[outByteIndex2] = (byte) v;
            }
            outByteIndex2 = outByteIndex;
        }
        return dest;
    }

    public static byte[] stringToUCS82Packed(String s, char baser82Low, int septets) {
        int outByteIndex;
        byte[] dest = new byte[septets];
        dest[0] = (byte) ((baser82Low >> 8) & HwSubscriptionManager.SUB_INIT_STATE);
        dest[1] = (byte) (baser82Low & 255);
        int outByteIndex2 = 2;
        int sz = s.length();
        for (int i = 0; i < sz; i++) {
            char c = s.charAt(i);
            int v = charToGsm7bit(c);
            if (-1 == v) {
                outByteIndex = outByteIndex2 + 1;
                dest[outByteIndex2] = (byte) (128 | (((c & 255) - (baser82Low & 255)) & 127));
            } else if (v == 27) {
                int outByteIndex3 = outByteIndex2 + 1;
                dest[outByteIndex2] = GSM_EXTENDED_ESCAPE;
                dest[outByteIndex3] = (byte) GsmAlphabet.charToGsmExtended(c);
                outByteIndex2 = outByteIndex3 + 1;
            } else {
                outByteIndex = outByteIndex2 + 1;
                dest[outByteIndex2] = (byte) v;
            }
            outByteIndex2 = outByteIndex;
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
        int udhLength;
        int septetsRemaining;
        int msgCount;
        CharSequence charSequence = s;
        boolean z = use7bitOnly;
        if (!sDisableCountryEncodingCheck) {
            enableCountrySpecificEncodings();
        }
        int i = -1;
        int i2 = 1;
        if (sEnabledSingleShiftTables.length + sEnabledLockingShiftTables.length == 0) {
            TextEncodingDetails ted = new TextEncodingDetails();
            int septets = GsmAlphabet.countGsmSeptetsUsingTables(charSequence, z, 0, 0);
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
        int maxSingleShiftCode = sHighestEnabledSingleShiftCode;
        List<LanguagePairCount> lpcList = new ArrayList<>(sEnabledLockingShiftTables.length + 1);
        lpcList.add(new LanguagePairCount(0));
        for (int i3 : sEnabledLockingShiftTables) {
            if (i3 != 0 && !sLanguageTables[i3].isEmpty()) {
                lpcList.add(new LanguagePairCount(i3));
            }
        }
        int sz = s.length();
        boolean hasLpList = lpcList.isEmpty();
        for (int i4 = 0; i4 < sz && !hasLpList; i4++) {
            char c = charSequence.charAt(i4);
            if (c == 27) {
                Rlog.w(TAG, "countGsmSeptets() string contains Escape character, ignoring!");
            } else {
                for (LanguagePairCount lpc : lpcList) {
                    if (sCharsToGsmTables[lpc.languageCode].get(c, -1) == -1) {
                        for (int table = 0; table <= maxSingleShiftCode; table++) {
                            if (lpc.septetCounts[table] != -1) {
                                if (sCharsToShiftTables[table].get(c, -1) != -1) {
                                    int[] iArr = lpc.septetCounts;
                                    iArr[table] = iArr[table] + 2;
                                } else if (z) {
                                    int[] iArr2 = lpc.septetCounts;
                                    iArr2[table] = iArr2[table] + 1;
                                    int[] iArr3 = lpc.unencodableCounts;
                                    iArr3[table] = iArr3[table] + 1;
                                } else {
                                    lpc.septetCounts[table] = -1;
                                }
                            }
                        }
                    } else {
                        for (int table2 = 0; table2 <= maxSingleShiftCode; table2++) {
                            if (lpc.septetCounts[table2] != -1) {
                                int[] iArr4 = lpc.septetCounts;
                                iArr4[table2] = iArr4[table2] + 1;
                            }
                        }
                    }
                }
            }
        }
        TextEncodingDetails ted2 = new TextEncodingDetails();
        ted2.msgCount = Integer.MAX_VALUE;
        ted2.codeUnitSize = 1;
        int minUnencodableCount = Integer.MAX_VALUE;
        for (LanguagePairCount lpc2 : lpcList) {
            int minUnencodableCount2 = minUnencodableCount;
            int shiftTable = 0;
            while (shiftTable <= maxSingleShiftCode) {
                int septets2 = lpc2.septetCounts[shiftTable];
                if (septets2 != i) {
                    if (lpc2.languageCode != 0 && shiftTable != 0) {
                        udhLength = 8;
                    } else if (lpc2.languageCode == 0 && shiftTable == 0) {
                        udhLength = 0;
                    } else {
                        udhLength = 5;
                    }
                    if (septets2 + udhLength > 160) {
                        if (udhLength == 0) {
                            udhLength = 1;
                        }
                        int septetsPerMessage = 160 - (udhLength + 6);
                        msgCount = ((septets2 + septetsPerMessage) - i2) / septetsPerMessage;
                        septetsRemaining = (msgCount * septetsPerMessage) - septets2;
                    } else {
                        msgCount = 1;
                        septetsRemaining = (160 - udhLength) - septets2;
                    }
                    int septetsRemaining2 = septetsRemaining;
                    int unencodableCount = lpc2.unencodableCounts[shiftTable];
                    if ((!z || unencodableCount <= minUnencodableCount2) && ((z && unencodableCount < minUnencodableCount2) || msgCount < ted2.msgCount || (msgCount == ted2.msgCount && septetsRemaining2 > ted2.codeUnitsRemaining))) {
                        minUnencodableCount2 = unencodableCount;
                        ted2.msgCount = msgCount;
                        ted2.codeUnitCount = septets2;
                        ted2.codeUnitsRemaining = septetsRemaining2;
                        ted2.languageTable = lpc2.languageCode;
                        ted2.languageShiftTable = shiftTable;
                    }
                }
                shiftTable++;
                CharSequence charSequence2 = s;
                i = -1;
                i2 = 1;
            }
            minUnencodableCount = minUnencodableCount2;
            CharSequence charSequence3 = s;
            i = -1;
            i2 = 1;
        }
        if (ted2.msgCount == Integer.MAX_VALUE) {
            return null;
        }
        return ted2;
    }

    public static int findGsmSeptetLimitIndex(String s, int start, int limit, int langTable, int langShiftTable) {
        int size = s.length();
        SparseIntArray charToLangTable = sCharsToGsmTables[langTable];
        SparseIntArray charToLangShiftTable = sCharsToShiftTables[langShiftTable];
        int accumulator = 0;
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
        sEnabledLockingShiftTables = r.getIntArray(17236035);
        if (sEnabledSingleShiftTables.length > 0) {
            sHighestEnabledSingleShiftCode = sEnabledSingleShiftTables[sEnabledSingleShiftTables.length - 1];
        } else {
            sHighestEnabledSingleShiftCode = 0;
        }
    }

    static {
        enableCountrySpecificEncodings();
        int numTables = sLanguageTables.length;
        int numShiftTables = sLanguageShiftTables.length;
        if (numTables != numShiftTables) {
            Rlog.e(TAG, "Error: language tables array length " + numTables + " != shift tables array length " + numShiftTables);
        }
        sCharsToGsmTables = new SparseIntArray[numTables];
        for (int i = 0; i < numTables; i++) {
            String table = sLanguageTables[i];
            int tableLen = table.length();
            if (!(tableLen == 0 || tableLen == 128)) {
                Rlog.e(TAG, "Error: language tables index " + i + " length " + tableLen + " (expected 128 or 0)");
            }
            SparseIntArray charToGsmTable = new SparseIntArray(tableLen);
            sCharsToGsmTables[i] = charToGsmTable;
            for (int j = 0; j < tableLen; j++) {
                charToGsmTable.put(table.charAt(j), j);
            }
        }
        sCharsToShiftTables = new SparseIntArray[numTables];
        for (int i2 = 0; i2 < numShiftTables; i2++) {
            String shiftTable = sLanguageShiftTables[i2];
            int shiftTableLen = shiftTable.length();
            if (!(shiftTableLen == 0 || shiftTableLen == 128)) {
                Rlog.e(TAG, "Error: language shift tables index " + i2 + " length " + shiftTableLen + " (expected 128 or 0)");
            }
            SparseIntArray charToShiftTable = new SparseIntArray(shiftTableLen);
            sCharsToShiftTables[i2] = charToShiftTable;
            for (int j2 = 0; j2 < shiftTableLen; j2++) {
                char c = shiftTable.charAt(j2);
                if (c != ' ') {
                    charToShiftTable.put(c, j2);
                }
            }
        }
    }

    private static int[] getSingleShiftTable(Resources r) {
        int[] temp = new int[1];
        if (SystemProperties.getInt("ro.config.smsCoding_National", 0) != 0) {
            temp[0] = SystemProperties.getInt("ro.config.smsCoding_National", 0);
        } else if (SystemProperties.getInt("gsm.sms.coding.national", 0) == 0) {
            return r.getIntArray(17236036);
        } else {
            temp[0] = SystemProperties.getInt("gsm.sms.coding.national", 0);
        }
        return temp;
    }

    public static char util_UnicodeToGsm7DefaultExtended(char c) {
        if (c == 12) {
            return 10;
        }
        if (c == 8364) {
            return 'e';
        }
        switch (c) {
            case '[':
                return '<';
            case '\\':
                return '/';
            case ']':
                return '>';
            case '^':
                return 20;
            default:
                switch (c) {
                    case '{':
                        return '(';
                    case '|':
                        return '@';
                    case '}':
                        return ')';
                    case '~':
                        return '=';
                    default:
                        return c;
                }
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
            case HwFullNetworkConstants.EVENT_DEFAULT_STATE_BASE /*200*/:
            case HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT /*201*/:
            case HwFullNetworkConstants.EVENT_SET_MAIN_SLOT /*202*/:
            case HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT_FOR_OPEATOR /*203*/:
                return 'E';
            case HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT_FOR_MDM /*204*/:
            case HwFullNetworkConstants.EVENT_CHECK_NETWORK_TYPE /*205*/:
            case HwFullNetworkConstants.EVENT_SET_NETWORK_TYPE /*206*/:
            case HwFullNetworkConstants.EVENT_FORCE_CHECK_MAIN_SLOT_FOR_CMCC /*207*/:
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

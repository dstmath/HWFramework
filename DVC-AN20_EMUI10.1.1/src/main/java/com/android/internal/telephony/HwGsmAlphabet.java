package com.android.internal.telephony;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.SparseIntArray;
import com.android.internal.telephony.gsm.HwSmsMessage;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.EncodeExceptionEx;
import com.huawei.internal.telephony.GsmAlphabetEx;
import com.huawei.utils.HwPartResourceUtils;
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
    private static int[] sEnabledSingleShiftTables;
    private static int sHighestEnabledSingleShiftCode;
    private static final String[] sLanguageShiftTables = {"          \f         ^                   {}     \\            [~] |                                    €                          ", "          \f         ^                   {}     \\            [~] |      Ğ İ         Ş               ç € ğ ı         ş            ", "         ç\f         ^                   {}     \\            [~] |Á       Í     Ó     Ú           á   €   í     ó     ú          ", "     ê   ç\fÔô Áá  ΦΓ^ΩΠΨΣΘ     Ê        {}     \\            [~] |À       Í     Ó     Ú     ÃÕ    Â   €   í     ó     ú     ãõ  â", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*০১ ২৩৪৫৬৭৮৯য়ৠৡৢ{}ৣ৲৳৴৵\\৶৷৸৹৺       [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ૦૧૨૩૪૫૬૭૮૯  {}     \\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ०१२३४५६७८९॒॑{}॓॔क़ख़ग़\\ज़ड़ढ़फ़य़ॠॡॢॣ॰ॱ [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ೦೧೨೩೪೫೬೭೮೯ೞೱ{}ೲ    \\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ൦൧൨൩൪൫൬൭൮൯൰൱{}൲൳൴൵ൺ\\ൻർൽൾൿ       [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ୦୧୨୩୪୫୬୭୮୯ଡ଼ଢ଼{}ୟ୰ୱ  \\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ੦੧੨੩੪੫੬੭੮੯ਖ਼ਗ਼{}ਜ਼ੜਫ਼ੵ \\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*।॥ ௦௧௨௩௪௫௬௭௮௯௳௴{}௵௶௷௸௺\\            [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*   ౦౧౨౩౪౫౬౭౮౯ౘౙ{}౸౹౺౻౼\\౽౾౿         [~] |ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          ", "@£$¥¿\"¤%&'\f*+ -/<=>¡^¡_#*؀؁ ۰۱۲۳۴۵۶۷۸۹،؍{}؎؏ؐؑؒ\\ؓؔ؛؟ـْ٘٫٬ٲٳۍ[~]۔|ABCDEFGHIJKLMNOPQRSTUVWXYZ          €                          "};
    private static final String[] sLanguageTables = {"@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞ￿ÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà", "@£$¥€éùıòÇ\nĞğ\rÅåΔ_ΦΓΛΩΠΨΣΘΞ￿ŞşßÉ !\"#¤%&'()*+,-./0123456789:;<=>?İABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§çabcdefghijklmnopqrstuvwxyzäöñüà", "", "@£$¥êéúíóç\nÔô\rÁáΔ_ªÇÀ∞^\\€Ó|￿ÂâÊÉ !\"#º%&'()*+,-./0123456789:;<=>?ÍABCDEFGHIJKLMNOPQRSTUVWXYZÃÕÚÜ§~abcdefghijklmnopqrstuvwxyzãõ`üà", "ঁংঃঅআইঈউঊঋ\nঌ \r এঐ  ওঔকখগঘঙচ￿ছজঝঞ !টঠডঢণত)(থদ,ধ.ন0123456789:; পফ?বভমযর ল   শষসহ়ঽািীুূৃৄ  েৈ  োৌ্ৎabcdefghijklmnopqrstuvwxyzৗড়ঢ়ৰৱ", "ઁંઃઅઆઇઈઉઊઋ\nઌઍ\r એઐઑ ઓઔકખગઘઙચ￿છજઝઞ !ટઠડઢણત)(થદ,ધ.ન0123456789:; પફ?બભમયર લળ વશષસહ઼ઽાિીુૂૃૄૅ ેૈૉ ોૌ્ૐabcdefghijklmnopqrstuvwxyzૠૡૢૣ૱", "ँंःअआइईउऊऋ\nऌऍ\rऎएऐऑऒओऔकखगघङच￿छजझञ !टठडढणत)(थद,ध.न0123456789:;ऩपफ?बभमयरऱलळऴवशषसह़ऽािीुूृॄॅॆेैॉॊोौ्ॐabcdefghijklmnopqrstuvwxyzॲॻॼॾॿ", " ಂಃಅಆಇಈಉಊಋ\nಌ \rಎಏಐ ಒಓಔಕಖಗಘಙಚ￿ಛಜಝಞ !ಟಠಡಢಣತ)(ಥದ,ಧ.ನ0123456789:; ಪಫ?ಬಭಮಯರಱಲಳ ವಶಷಸಹ಼ಽಾಿೀುೂೃೄ ೆೇೈ ೊೋೌ್ೕabcdefghijklmnopqrstuvwxyzೖೠೡೢೣ", " ംഃഅആഇഈഉഊഋ\nഌ \rഎഏഐ ഒഓഔകഖഗഘങച￿ഛജഝഞ !ടഠഡഢണത)(ഥദ,ധ.ന0123456789:; പഫ?ബഭമയരറലളഴവശഷസഹ ഽാിീുൂൃൄ െേൈ ൊോൌ്ൗabcdefghijklmnopqrstuvwxyzൠൡൢൣ൹", "ଁଂଃଅଆଇଈଉଊଋ\nଌ \r ଏଐ  ଓଔକଖଗଘଙଚ￿ଛଜଝଞ !ଟଠଡଢଣତ)(ଥଦ,ଧ.ନ0123456789:; ପଫ?ବଭମଯର ଲଳ ଵଶଷସହ଼ଽାିୀୁୂୃୄ  େୈ  ୋୌ୍ୖabcdefghijklmnopqrstuvwxyzୗୠୡୢୣ", "ਁਂਃਅਆਇਈਉਊ \n  \r ਏਐ  ਓਔਕਖਗਘਙਚ￿ਛਜਝਞ !ਟਠਡਢਣਤ)(ਥਦ,ਧ.ਨ0123456789:; ਪਫ?ਬਭਮਯਰ ਲਲ਼ ਵਸ਼ ਸਹ਼ ਾਿੀੁੂ    ੇੈ  ੋੌ੍ੑabcdefghijklmnopqrstuvwxyzੰੱੲੳੴ", " ஂஃஅஆஇஈஉஊ \n  \rஎஏஐ ஒஓஔக   ஙச￿ ஜ ஞ !ட   ணத)(  , .ந0123456789:;னப ?  மயரறலளழவஶஷஸஹ  ாிீுூ   ெேை ொோௌ்ௐabcdefghijklmnopqrstuvwxyzௗ௰௱௲௹", "ఁంఃఅఆఇఈఉఊఋ\nఌ \rఎఏఐ ఒఓఔకఖగఘఙచ￿ఛజఝఞ !టఠడఢణత)(థద,ధ.న0123456789:; పఫ?బభమయరఱలళ వశషసహ ఽాిీుూృౄ ెేై ొోౌ్ౕabcdefghijklmnopqrstuvwxyzౖౠౡౢౣ", "اآبٻڀپڦتۂٿ\nٹٽ\rٺټثجځڄڃڅچڇحخد￿ڌڈډڊ !ڏڍذرڑړ)(ڙز,ږ.ژ0123456789:;ښسش?صضطظعفقکڪګگڳڱلمنںڻڼوۄەہھءیېےٍُِٗٔabcdefghijklmnopqrstuvwxyzّٰٕٖٓ"};

    private HwGsmAlphabet() {
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

    public static int charToGsm(char c) {
        try {
            return charToGsm(c, false);
        } catch (EncodeExceptionEx e) {
            return sCharsToGsmTables[0].get(32, 32);
        }
    }

    public static int charToGsm(char c, boolean throwException) throws EncodeExceptionEx {
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
        throw new EncodeExceptionEx("Unencodable char: '" + c + "'", 0);
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

    public static byte[] stringToGsm7BitPackedWithHeader(String data, byte[] header) throws EncodeExceptionEx {
        return stringToGsm7BitPackedWithHeader(data, header, 0, 0);
    }

    public static byte[] stringToGsm7BitPackedWithHeader(String data, byte[] header, int languageTable, int languageShiftTable) throws EncodeExceptionEx {
        if (header == null || header.length == 0) {
            return stringToGsm7BitPacked(data, languageTable, languageShiftTable);
        }
        byte[] ret = stringToGsm7BitPacked(data, (((header.length + 1) * 8) + 6) / 7, true, languageTable, languageShiftTable);
        ret[1] = (byte) header.length;
        System.arraycopy(header, 0, ret, 2, header.length);
        return ret;
    }

    public static byte[] stringToGsm7BitPacked(String data) throws EncodeExceptionEx {
        return stringToGsm7BitPacked(data, 0, true, 0, 0);
    }

    public static byte[] stringToGsm7BitPacked(String data, int languageTable, int languageShiftTable) throws EncodeExceptionEx {
        return stringToGsm7BitPacked(data, 0, true, languageTable, languageShiftTable);
    }

    public static byte[] stringToGsm7BitPacked(String data, int startingSeptetOffset, boolean throwException, int languageTable, int languageShiftTable) throws EncodeExceptionEx {
        String str = data;
        int dataLen = str != null ? data.length() : 0;
        int septetCount = countGsmSeptetsUsingTables(str, !throwException, languageTable, languageShiftTable);
        int i = -1;
        if (septetCount != -1) {
            int septetCount2 = septetCount + startingSeptetOffset;
            if (septetCount2 <= 255) {
                byte[] ret = new byte[((((septetCount2 * 7) + 7) / 8) + 1)];
                SparseIntArray charToLanguageTable = sCharsToGsmTables[languageTable];
                SparseIntArray charToShiftTable = sCharsToShiftTables[languageShiftTable];
                int i2 = 0;
                int septets = startingSeptetOffset;
                int bitOffset = startingSeptetOffset * 7;
                while (i2 < dataLen && septets < septetCount2) {
                    char c = str.charAt(i2);
                    int v = charToLanguageTable.get(c, i);
                    if (v == i) {
                        v = charToShiftTable.get(c, i);
                        if (v != i) {
                            packSmsChar(ret, bitOffset, 27);
                            bitOffset += 7;
                            septets++;
                        } else if (!throwException) {
                            v = charToLanguageTable.get(32, 32);
                        } else {
                            throw new EncodeExceptionEx("stringToGsm7BitPacked(): unencodable char", 0);
                        }
                    }
                    packSmsChar(ret, bitOffset, v);
                    septets++;
                    i2++;
                    bitOffset += 7;
                    i = -1;
                    str = data;
                }
                ret[0] = (byte) septetCount2;
                return ret;
            }
            throw new EncodeExceptionEx("Payload cannot exceed 255 septets", 0);
        }
        throw new EncodeExceptionEx("countGsmSeptetsUsingTables(): unencodable char", 0);
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
        int languageTable2;
        int shiftTable2 = shiftTable;
        StringBuilder ret = new StringBuilder(lengthSeptets);
        if (languageTable < 0 || languageTable > sLanguageTables.length) {
            RlogEx.w(TAG, "unknown language table " + languageTable + ", using default");
            languageTable2 = 0;
        } else {
            languageTable2 = languageTable;
        }
        if (shiftTable2 < 0 || shiftTable2 > sLanguageShiftTables.length) {
            RlogEx.w(TAG, "unknown single shift table " + shiftTable2 + ", using default");
            shiftTable2 = 0;
        }
        boolean prevCharWasEscape = false;
        try {
            String languageTableToChar = sLanguageTables[languageTable2];
            String shiftTableToChar = sLanguageShiftTables[shiftTable2];
            if (languageTableToChar.isEmpty()) {
                RlogEx.w(TAG, "no language table for code " + languageTable2 + ", using default");
                languageTableToChar = sLanguageTables[0];
            }
            if (shiftTableToChar.isEmpty()) {
                RlogEx.w(TAG, "no single shift table for code " + shiftTable2 + ", using default");
                shiftTableToChar = sLanguageShiftTables[0];
            }
            for (int i = 0; i < lengthSeptets; i++) {
                int bitOffset = (i * 7) + numPaddingBits;
                int byteOffset = bitOffset / 8;
                int shift = bitOffset % 8;
                int gsmVal = (pdu[offset + byteOffset] >> shift) & 127;
                if (shift > 1) {
                    gsmVal = (gsmVal & (127 >> (shift - 1))) | (127 & (pdu[(offset + byteOffset) + 1] << (8 - shift)));
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
            RlogEx.e(TAG, "Error GSM 7 bit packed: ", ex);
            return null;
        }
    }

    public static String gsm8BitUnpackedToString(byte[] data, int offset, int length) {
        return gsm8BitUnpackedToString(data, offset, length, "");
    }

    public static String gsm8BitUnpackedToString(byte[] data, int offset, int length, String characterset) {
        int c;
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
        while (i < offset + length && (c = data[i] & 255) != 255) {
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
        int c;
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
        while (i < offset + length && (c = data[i] & 255) != 255) {
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
        int sz = 0;
        SparseIntArray charToLanguageTable = sCharsToGsmTables[0];
        SparseIntArray charToShiftTable = sCharsToShiftTables[0];
        int i = 0;
        if (s != null) {
            sz = s.length();
        }
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

    /* JADX INFO: Multiple debug info for r5v1 int: [D('gsm7bit' int), D('outByteIndex' int)] */
    public static byte[] stringToUCS81Packed(String s, char baser81, int septets) {
        byte[] dest = new byte[septets];
        int sz = 0;
        dest[0] = (byte) ((baser81 & 32640) >> 7);
        int outByteIndex = 1;
        if (s != null) {
            sz = s.length();
        }
        for (int i = 0; i < sz; i++) {
            char oneChar = s.charAt(i);
            int gsm7bit = charToGsm7bit(oneChar);
            if (-1 == gsm7bit) {
                dest[outByteIndex] = (byte) ((oneChar & 127) | HwSmsMessage.SMS_TOA_UNKNOWN);
                outByteIndex++;
            } else if (gsm7bit == 27) {
                int outByteIndex2 = outByteIndex + 1;
                dest[outByteIndex] = GSM_EXTENDED_ESCAPE;
                dest[outByteIndex2] = (byte) GsmAlphabetEx.charToGsmExtended(oneChar);
                outByteIndex = outByteIndex2 + 1;
            } else {
                dest[outByteIndex] = (byte) gsm7bit;
                outByteIndex++;
            }
        }
        return dest;
    }

    /* JADX INFO: Multiple debug info for r5v1 int: [D('gsm7bit' int), D('outByteIndex' int)] */
    public static byte[] stringToUCS82Packed(String s, char baser82Low, int septets) {
        byte[] dest = new byte[septets];
        int sz = 0;
        dest[0] = (byte) ((baser82Low >> '\b') & 255);
        dest[1] = (byte) (baser82Low & 255);
        int outByteIndex = 2;
        if (s != null) {
            sz = s.length();
        }
        for (int i = 0; i < sz; i++) {
            char c = s.charAt(i);
            int gsm7bit = charToGsm7bit(c);
            if (-1 == gsm7bit) {
                dest[outByteIndex] = (byte) ((((c & 255) - (baser82Low & 255)) & 127) | HwSmsMessage.SMS_TOA_UNKNOWN);
                outByteIndex++;
            } else if (gsm7bit == 27) {
                int outByteIndex2 = outByteIndex + 1;
                dest[outByteIndex] = GSM_EXTENDED_ESCAPE;
                dest[outByteIndex2] = (byte) GsmAlphabetEx.charToGsmExtended(c);
                outByteIndex = outByteIndex2 + 1;
            } else {
                dest[outByteIndex] = (byte) gsm7bit;
                outByteIndex++;
            }
        }
        return dest;
    }

    public static int countGsmSeptets(char c) {
        try {
            return countGsmSeptets(c, false);
        } catch (EncodeExceptionEx e) {
            return 0;
        }
    }

    public static int countGsmSeptets(char c, boolean throwsException) throws EncodeExceptionEx {
        if (sCharsToGsmTables[0].get(c, -1) != -1) {
            return 1;
        }
        if (sCharsToShiftTables[0].get(c, -1) != -1) {
            return 2;
        }
        if (!throwsException) {
            return 1;
        }
        throw new EncodeExceptionEx("Unencodable char: '" + c + "'", 0);
    }

    public static int countGsmSeptetsUsingTables(CharSequence sequence, boolean use7bitOnly, int languageTable, int languageShiftTable) {
        int count = 0;
        int sz = sequence != null ? sequence.length() : 0;
        SparseIntArray charToLanguageTable = sCharsToGsmTables[languageTable];
        SparseIntArray charToShiftTable = sCharsToShiftTables[languageShiftTable];
        for (int i = 0; i < sz; i++) {
            char currentChar = sequence.charAt(i);
            if (currentChar == 27) {
                RlogEx.w(TAG, "countGsmSeptets() string contains Escape character, skipping.");
            } else if (charToLanguageTable.get(currentChar, -1) != -1) {
                count++;
            } else if (charToShiftTable.get(currentChar, -1) != -1) {
                count += 2;
            } else if (!use7bitOnly) {
                return -1;
            } else {
                count++;
            }
        }
        return count;
    }

    /* JADX INFO: Multiple debug info for r2v4 int: [D('maxSingleShiftCode' int), D('ted' com.android.internal.telephony.HwGsmAlphabet$TextEncodingDetails)] */
    public static TextEncodingDetails countGsmSeptets(CharSequence sequence, boolean use7bitOnly) {
        int udhLength;
        int septetsRemaining;
        int msgCount;
        if (!sDisableCountryEncodingCheck) {
            enableCountrySpecificEncodings();
        }
        int sz = 0;
        int i = -1;
        int i2 = 1;
        if (sEnabledSingleShiftTables.length + sEnabledLockingShiftTables.length != 0 || sequence == null) {
            int maxSingleShiftCode = sHighestEnabledSingleShiftCode;
            List<LanguagePairCount> lpcList = new ArrayList<>(sEnabledLockingShiftTables.length + 1);
            lpcList.add(new LanguagePairCount(0));
            int[] iArr = sEnabledLockingShiftTables;
            for (int i3 : iArr) {
                if (i3 != 0 && !sLanguageTables[i3].isEmpty()) {
                    lpcList.add(new LanguagePairCount(i3));
                }
            }
            if (sequence != null) {
                sz = sequence.length();
            }
            boolean hasLpList = lpcList.isEmpty();
            for (int i4 = 0; i4 < sz && !hasLpList; i4++) {
                char currentChar = sequence.charAt(i4);
                if (currentChar == 27) {
                    RlogEx.w(TAG, "countGsmSeptets() string contains Escape character, ignoring!");
                } else {
                    for (LanguagePairCount lpc : lpcList) {
                        if (sCharsToGsmTables[lpc.languageCode].get(currentChar, -1) == -1) {
                            for (int table = 0; table <= maxSingleShiftCode; table++) {
                                if (lpc.septetCounts[table] != -1) {
                                    if (sCharsToShiftTables[table].get(currentChar, -1) != -1) {
                                        int[] iArr2 = lpc.septetCounts;
                                        iArr2[table] = iArr2[table] + 2;
                                    } else if (use7bitOnly) {
                                        int[] iArr3 = lpc.septetCounts;
                                        iArr3[table] = iArr3[table] + 1;
                                        int[] iArr4 = lpc.unencodableCounts;
                                        iArr4[table] = iArr4[table] + 1;
                                    } else {
                                        lpc.septetCounts[table] = -1;
                                    }
                                }
                            }
                        } else {
                            for (int table2 = 0; table2 <= maxSingleShiftCode; table2++) {
                                if (lpc.septetCounts[table2] != -1) {
                                    int[] iArr5 = lpc.septetCounts;
                                    iArr5[table2] = iArr5[table2] + 1;
                                }
                            }
                        }
                    }
                }
            }
            TextEncodingDetails ted = new TextEncodingDetails();
            ted.msgCount = HwSignalStrength.WCDMA_STRENGTH_INVALID;
            ted.codeUnitSize = 1;
            int minUnencodableCount = HwSignalStrength.WCDMA_STRENGTH_INVALID;
            for (LanguagePairCount lpc2 : lpcList) {
                int shiftTable = 0;
                while (shiftTable <= maxSingleShiftCode) {
                    int septets = lpc2.septetCounts[shiftTable];
                    if (septets != i) {
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
                            msgCount = ((septets + septetsPerMessage) - i2) / septetsPerMessage;
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
                    i = -1;
                    i2 = 1;
                }
                i = -1;
                i2 = 1;
            }
            if (ted.msgCount == Integer.MAX_VALUE) {
                return null;
            }
            return ted;
        }
        TextEncodingDetails ted2 = new TextEncodingDetails();
        int septets2 = GsmAlphabetEx.countGsmSeptetsUsingTables(sequence, use7bitOnly, 0, 0);
        if (septets2 == -1) {
            return null;
        }
        ted2.codeUnitSize = 1;
        ted2.codeUnitCount = septets2;
        if (septets2 > 160) {
            ted2.msgCount = (septets2 + 152) / 153;
            ted2.codeUnitsRemaining = (ted2.msgCount * 153) - septets2;
        } else {
            ted2.msgCount = 1;
            ted2.codeUnitsRemaining = 160 - septets2;
        }
        ted2.codeUnitSize = 1;
        return ted2;
    }

    public static int findGsmSeptetLimitIndex(String s, int start, int limit, int langTable, int langShiftTable) {
        int accumulator = 0;
        int size = s != null ? s.length() : 0;
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
        sEnabledLockingShiftTables = r.getIntArray(HwPartResourceUtils.getResourceId("config_sms_enabled_locking_shift_tables"));
        int[] iArr = sEnabledSingleShiftTables;
        if (iArr.length > 0) {
            sHighestEnabledSingleShiftCode = iArr[iArr.length - 1];
        } else {
            sHighestEnabledSingleShiftCode = 0;
        }
    }

    static {
        enableCountrySpecificEncodings();
        int numTables = sLanguageTables.length;
        int numShiftTables = sLanguageShiftTables.length;
        if (numTables != numShiftTables) {
            RlogEx.e(TAG, "Error: language tables array length " + numTables + " != shift tables array length " + numShiftTables);
        }
        sCharsToGsmTables = new SparseIntArray[numTables];
        for (int i = 0; i < numTables; i++) {
            String table = sLanguageTables[i];
            int tableLen = table.length();
            if (!(tableLen == 0 || tableLen == 128)) {
                RlogEx.e(TAG, "Error: language tables index " + i + " length " + tableLen + " (expected 128 or 0)");
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
                RlogEx.e(TAG, "Error: language shift tables index " + i2 + " length " + shiftTableLen + " (expected 128 or 0)");
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

    private static int[] getSingleShiftTable(Resources r) {
        int[] temp = new int[1];
        if (SystemPropertiesEx.getInt("ro.config.smsCoding_National", 0) != 0) {
            temp[0] = SystemPropertiesEx.getInt("ro.config.smsCoding_National", 0);
        } else if (SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_SMS_CODING, 0) == 0) {
            return r.getIntArray(HwPartResourceUtils.getResourceId("config_sms_enabled_single_shift_tables"));
        } else {
            temp[0] = SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_SMS_CODING, 0);
        }
        return temp;
    }

    public static char util_UnicodeToGsm7DefaultExtended(char c) {
        if (c == '\f') {
            return '\n';
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
            case 197:
            case 198:
            case 199:
            case 214:
            case 215:
            case 216:
            case 223:
            case 228:
            case 229:
            case 230:
            case 236:
            case 241:
            case 246:
            case 247:
            case 248:
            case 249:
            default:
                return c;
            case HwDctConstants.CMD_RESET_DEFAULT_APN:
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
        }
    }
}

package com.huawei.media.scan;

import android.media.BuildConfig;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import java.io.UnsupportedEncodingException;
import java.lang.Character;
import java.util.regex.Pattern;

public class AudioMetadataMessyUtils {
    private static final int FLAG_ALBUM = 1;
    private static final int FLAG_ARTIST = 2;
    private static final int FLAG_TITLE = 3;
    private static final String INVALID_UTF8_TOKEN = "??";
    private static final String TAG = "AudioMetadataMessyUtils";

    private static boolean isMessyCharacter(char input) {
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(input);
        return isMessyCharacter1(unicodeBlock) || isMessyCharacter2(unicodeBlock) || isMessyCharacter3(unicodeBlock);
    }

    private static boolean isMessyCharacter1(Character.UnicodeBlock unicodeBlock) {
        return unicodeBlock == Character.UnicodeBlock.LATIN_1_SUPPLEMENT || unicodeBlock == Character.UnicodeBlock.SPECIALS || unicodeBlock == Character.UnicodeBlock.HEBREW || unicodeBlock == Character.UnicodeBlock.GREEK;
    }

    private static boolean isMessyCharacter2(Character.UnicodeBlock unicodeBlock) {
        return unicodeBlock == Character.UnicodeBlock.CYRILLIC_SUPPLEMENTARY || unicodeBlock == Character.UnicodeBlock.LATIN_EXTENDED_A || unicodeBlock == Character.UnicodeBlock.LATIN_EXTENDED_B || unicodeBlock == Character.UnicodeBlock.COMBINING_DIACRITICAL_MARKS;
    }

    private static boolean isMessyCharacter3(Character.UnicodeBlock unicodeBlock) {
        return unicodeBlock == Character.UnicodeBlock.PRIVATE_USE_AREA || unicodeBlock == Character.UnicodeBlock.ARMENIAN;
    }

    private static boolean isMessyCharacterOrigin(char input) {
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(input);
        return isMessyCharacterOrigin1(unicodeBlock) || isMessyCharacterOrigin2(unicodeBlock) || isMessyCharacterOrigin3(input);
    }

    private static boolean isMessyCharacterOrigin1(Character.UnicodeBlock unicodeBlock) {
        return unicodeBlock == Character.UnicodeBlock.SPECIALS || unicodeBlock == Character.UnicodeBlock.GREEK || unicodeBlock == Character.UnicodeBlock.CYRILLIC_SUPPLEMENTARY || unicodeBlock == Character.UnicodeBlock.LATIN_EXTENDED_A;
    }

    private static boolean isMessyCharacterOrigin2(Character.UnicodeBlock unicodeBlock) {
        return unicodeBlock == Character.UnicodeBlock.LATIN_EXTENDED_B || unicodeBlock == Character.UnicodeBlock.COMBINING_DIACRITICAL_MARKS || unicodeBlock == Character.UnicodeBlock.PRIVATE_USE_AREA;
    }

    private static boolean isMessyCharacterOrigin3(char input) {
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(input);
        return (unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS && !CharacterTables.isFrequentHan(input)) || unicodeBlock == Character.UnicodeBlock.BOX_DRAWING || unicodeBlock == Character.UnicodeBlock.HANGUL_SYLLABLES || unicodeBlock == Character.UnicodeBlock.ARMENIAN;
    }

    private static boolean isAcceptableCharacter(char input) {
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(input);
        return isCJKCharacter(unicodeBlock) || unicodeBlock == Character.UnicodeBlock.BASIC_LATIN || unicodeBlock == Character.UnicodeBlock.GENERAL_PUNCTUATION || unicodeBlock == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    private static boolean isCJKCharacter(Character.UnicodeBlock unicodeBlock) {
        return unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || unicodeBlock == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || unicodeBlock == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION;
    }

    private static String trimIncorrectPunctuation(String input) {
        return Pattern.compile("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]").matcher(Pattern.compile("\\s*|\t*|\r*|\n*").matcher(input).replaceAll(BuildConfig.FLAVOR).replaceAll("\\p{P}", BuildConfig.FLAVOR)).replaceAll(BuildConfig.FLAVOR);
    }

    private static boolean isAcceptableString(String input) {
        for (char item : trimIncorrectPunctuation(input).trim().toCharArray()) {
            if (!isAcceptableCharacter(item)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isStringMessy(String input) {
        for (char intent : trimIncorrectPunctuation(input).trim().toCharArray()) {
            if (isMessyCharacter(intent)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStringMessyOrigin(String input) {
        for (char intent : trimIncorrectPunctuation(input).trim().toCharArray()) {
            if (isMessyCharacterOrigin(intent)) {
                return true;
            }
        }
        return false;
    }

    private static String getCorrectEncodedString(String input) {
        if (isStringMessy(input)) {
            try {
                String utf8 = new String(input.getBytes("ISO-8859-1"), "UTF-8");
                if (isAcceptableString(utf8)) {
                    return utf8;
                }
                String gbk = new String(input.getBytes("ISO-8859-1"), "GBK");
                if (isAcceptableString(gbk)) {
                    return gbk;
                }
                String big5 = new String(input.getBytes("ISO-8859-1"), "BIG5");
                if (isAcceptableString(big5)) {
                    return big5;
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "unsupported encoding error");
            }
        }
        return input;
    }

    private static boolean isInvalidUtf8(String input) {
        return input != null && input.contains(INVALID_UTF8_TOKEN);
    }

    private static boolean isInvalidString(String input) {
        return TextUtils.isEmpty(input) || isInvalidUtf8(input) || isStringMessy(input);
    }

    private static String finalCheck(String value, String path, int flag) {
        if (!isInvalidString(value)) {
            return value;
        }
        if (flag == 1 || flag == 2) {
            return "<unknown>";
        }
        return getDisplayName(path);
    }

    private static String getDisplayName(String path) {
        int lastdotIndex = path.lastIndexOf(".");
        int lastSlashIndex = path.lastIndexOf("/");
        if ((lastdotIndex <= 0 || lastSlashIndex <= 0) || lastSlashIndex > lastdotIndex) {
            return BuildConfig.FLAVOR;
        }
        return path.substring(lastSlashIndex + 1, lastdotIndex);
    }

    public static boolean useMessyOptimize() {
        String debug = SystemPropertiesEx.get("ro.product.locale.region", BuildConfig.FLAVOR);
        return debug != null && "CN".equals(debug);
    }

    public static boolean isMp3(String mimetype) {
        if (mimetype == null) {
            return false;
        }
        if ("audio/mpeg".equalsIgnoreCase(mimetype) || "audio/x-mp3".equalsIgnoreCase(mimetype) || "audio/x-mpeg".equalsIgnoreCase(mimetype) || "audio/mp3".equalsIgnoreCase(mimetype)) {
            return true;
        }
        return false;
    }

    public static boolean preHandleStringTag(String value, String mimetype) {
        if (!useMessyOptimize() || !isMp3(mimetype) || TextUtils.isEmpty(value) || !isStringMessyOrigin(value)) {
            return false;
        }
        Log.w(TAG, "value: " + value);
        return true;
    }

    public static void initializeSniffer(String path) {
    }

    public static void resetSniffer() {
    }

    public static String postHandleStringTag(String value, String path, int flag) {
        if (flag == 1) {
            return finalCheck(getCorrectEncodedString(value), path, flag);
        }
        if (flag == 2) {
            return finalCheck(getCorrectEncodedString(value), path, flag);
        }
        if (flag != 3) {
            return value;
        }
        return finalCheck(getCorrectEncodedString(value), path, flag);
    }
}

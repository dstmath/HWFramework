package android.text;

import android.icu.lang.UCharacter;

public class Emoji {
    public static int CANCEL_TAG = 917631;
    public static int COMBINING_ENCLOSING_KEYCAP = 8419;
    public static int VARIATION_SELECTOR_16 = 65039;
    public static int ZERO_WIDTH_JOINER = 8205;

    public static boolean isRegionalIndicatorSymbol(int codePoint) {
        return 127462 <= codePoint && codePoint <= 127487;
    }

    public static boolean isEmojiModifier(int codePoint) {
        return UCharacter.hasBinaryProperty(codePoint, 59);
    }

    public static boolean isEmojiModifierBase(int c) {
        if (c == 129309 || c == 129340) {
            return true;
        }
        if ((129461 > c || c > 129462) && (129464 > c || c > 129465)) {
            return UCharacter.hasBinaryProperty(c, 60);
        }
        return true;
    }

    public static boolean isNewEmoji(int c) {
        if (c < 128725 || c > 129685) {
            return false;
        }
        if (c == 128725 || c == 128762 || c == 129343 || c == 129393 || c == 129403 || ((128992 <= c && c <= 129003) || ((129293 <= c && c <= 129295) || ((129445 <= c && c <= 129450) || ((129454 <= c && c <= 129455) || ((129466 <= c && c <= 129471) || ((129475 <= c && c <= 129482) || ((129485 <= c && c <= 129487) || ((129648 <= c && c <= 129651) || ((129656 <= c && c <= 129658) || ((129664 <= c && c <= 129666) || (129680 <= c && c <= 129685)))))))))))) {
            return true;
        }
        return false;
    }

    public static boolean isEmoji(int codePoint) {
        return isNewEmoji(codePoint) || UCharacter.hasBinaryProperty(codePoint, 57);
    }

    public static boolean isKeycapBase(int codePoint) {
        return (48 <= codePoint && codePoint <= 57) || codePoint == 35 || codePoint == 42;
    }

    public static boolean isTagSpecChar(int codePoint) {
        return 917536 <= codePoint && codePoint <= 917630;
    }
}

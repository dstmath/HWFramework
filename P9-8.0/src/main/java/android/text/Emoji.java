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

    /* JADX WARNING: Missing block: B:4:0x000b, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isEmojiModifierBase(int codePoint) {
        if (codePoint == 129309 || codePoint == 129340 || codePoint == 129311 || ((129329 <= codePoint && codePoint <= 129330) || (129489 <= codePoint && codePoint <= 129501))) {
            return true;
        }
        return UCharacter.hasBinaryProperty(codePoint, 60);
    }

    public static boolean isNewEmoji(int codePoint) {
        boolean z = true;
        if (codePoint < 128759 || codePoint > 129510) {
            return false;
        }
        if ((128759 > codePoint || codePoint > 128760) && codePoint != 129311 && ((129320 > codePoint || codePoint > 129327) && ((129329 > codePoint || codePoint > 129330) && codePoint != 129356 && ((129375 > codePoint || codePoint > 129387) && ((129426 > codePoint || codePoint > 129431) && (129488 > codePoint || codePoint > 129510)))))) {
            z = false;
        }
        return z;
    }

    public static boolean isEmoji(int codePoint) {
        return !isNewEmoji(codePoint) ? UCharacter.hasBinaryProperty(codePoint, 57) : true;
    }

    public static boolean isKeycapBase(int codePoint) {
        return (48 <= codePoint && codePoint <= 57) || codePoint == 35 || codePoint == 42;
    }

    public static boolean isTagSpecChar(int codePoint) {
        return 917536 <= codePoint && codePoint <= 917630;
    }
}

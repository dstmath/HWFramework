package android.icu.lang;

import android.icu.lang.UCharacterEnums.ECharacterDirection;

public final class UCharacterDirection implements ECharacterDirection {
    private UCharacterDirection() {
    }

    public static String toString(int dir) {
        switch (dir) {
            case 0:
                return "Left-to-Right";
            case 1:
                return "Right-to-Left";
            case 2:
                return "European Number";
            case 3:
                return "European Number Separator";
            case 4:
                return "European Number Terminator";
            case 5:
                return "Arabic Number";
            case 6:
                return "Common Number Separator";
            case 7:
                return "Paragraph Separator";
            case 8:
                return "Segment Separator";
            case 9:
                return "Whitespace";
            case 10:
                return "Other Neutrals";
            case 11:
                return "Left-to-Right Embedding";
            case 12:
                return "Left-to-Right Override";
            case 13:
                return "Right-to-Left Arabic";
            case 14:
                return "Right-to-Left Embedding";
            case 15:
                return "Right-to-Left Override";
            case 16:
                return "Pop Directional Format";
            case 17:
                return "Non-Spacing Mark";
            case 18:
                return "Boundary Neutral";
            case 19:
                return "First Strong Isolate";
            case 20:
                return "Left-to-Right Isolate";
            case 21:
                return "Right-to-Left Isolate";
            case 22:
                return "Pop Directional Isolate";
            default:
                return "Unassigned";
        }
    }
}

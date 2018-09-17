package android.icu.lang;

import android.icu.lang.UCharacterEnums.ECharacterCategory;

public final class UCharacterCategory implements ECharacterCategory {
    public static String toString(int category) {
        switch (category) {
            case 1:
                return "Letter, Uppercase";
            case 2:
                return "Letter, Lowercase";
            case 3:
                return "Letter, Titlecase";
            case 4:
                return "Letter, Modifier";
            case 5:
                return "Letter, Other";
            case 6:
                return "Mark, Non-Spacing";
            case 7:
                return "Mark, Enclosing";
            case 8:
                return "Mark, Spacing Combining";
            case 9:
                return "Number, Decimal Digit";
            case 10:
                return "Number, Letter";
            case 11:
                return "Number, Other";
            case 12:
                return "Separator, Space";
            case 13:
                return "Separator, Line";
            case 14:
                return "Separator, Paragraph";
            case 15:
                return "Other, Control";
            case 16:
                return "Other, Format";
            case 17:
                return "Other, Private Use";
            case 18:
                return "Other, Surrogate";
            case 19:
                return "Punctuation, Dash";
            case 20:
                return "Punctuation, Open";
            case 21:
                return "Punctuation, Close";
            case 22:
                return "Punctuation, Connector";
            case 23:
                return "Punctuation, Other";
            case 24:
                return "Symbol, Math";
            case 25:
                return "Symbol, Currency";
            case 26:
                return "Symbol, Modifier";
            case 27:
                return "Symbol, Other";
            case 28:
                return "Punctuation, Initial quote";
            case 29:
                return "Punctuation, Final quote";
            default:
                return "Unassigned";
        }
    }

    private UCharacterCategory() {
    }
}

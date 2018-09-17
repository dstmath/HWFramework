package android.icu.lang;

import android.icu.lang.UCharacterEnums.ECharacterCategory;
import dalvik.bytecode.Opcodes;
import libcore.icu.ICU;
import libcore.io.IoBridge;
import org.apache.harmony.security.provider.crypto.SHA1Constants;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public final class UCharacterCategory implements ECharacterCategory {
    public static String toString(int category) {
        switch (category) {
            case NodeFilter.SHOW_ELEMENT /*1*/:
                return "Letter, Uppercase";
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                return "Letter, Lowercase";
            case XmlPullParser.END_TAG /*3*/:
                return "Letter, Titlecase";
            case NodeFilter.SHOW_TEXT /*4*/:
                return "Letter, Modifier";
            case XmlPullParser.CDSECT /*5*/:
                return "Letter, Other";
            case XmlPullParser.ENTITY_REF /*6*/:
                return "Mark, Non-Spacing";
            case XmlPullParser.IGNORABLE_WHITESPACE /*7*/:
                return "Mark, Enclosing";
            case NodeFilter.SHOW_CDATA_SECTION /*8*/:
                return "Mark, Spacing Combining";
            case XmlPullParser.COMMENT /*9*/:
                return "Number, Decimal Digit";
            case XmlPullParser.DOCDECL /*10*/:
                return "Number, Letter";
            case ICU.U_TRUNCATED_CHAR_FOUND /*11*/:
                return "Number, Other";
            case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
                return "Separator, Space";
            case Opcodes.OP_MOVE_EXCEPTION /*13*/:
                return "Separator, Line";
            case Opcodes.OP_RETURN_VOID /*14*/:
                return "Separator, Paragraph";
            case ICU.U_BUFFER_OVERFLOW_ERROR /*15*/:
                return "Other, Control";
            case NodeFilter.SHOW_ENTITY_REFERENCE /*16*/:
                return "Other, Format";
            case IoBridge.JAVA_IP_MULTICAST_TTL /*17*/:
                return "Other, Private Use";
            case Opcodes.OP_CONST_4 /*18*/:
                return "Other, Surrogate";
            case IoBridge.JAVA_MCAST_JOIN_GROUP /*19*/:
                return "Punctuation, Dash";
            case SHA1Constants.DIGEST_LENGTH /*20*/:
                return "Punctuation, Open";
            case IoBridge.JAVA_MCAST_JOIN_SOURCE_GROUP /*21*/:
                return "Punctuation, Close";
            case IoBridge.JAVA_MCAST_LEAVE_SOURCE_GROUP /*22*/:
                return "Punctuation, Connector";
            case IoBridge.JAVA_MCAST_BLOCK_SOURCE /*23*/:
                return "Punctuation, Other";
            case IoBridge.JAVA_MCAST_UNBLOCK_SOURCE /*24*/:
                return "Symbol, Math";
            case Opcodes.OP_CONST_WIDE_HIGH16 /*25*/:
                return "Symbol, Currency";
            case Opcodes.OP_CONST_STRING /*26*/:
                return "Symbol, Modifier";
            case Opcodes.OP_CONST_STRING_JUMBO /*27*/:
                return "Symbol, Other";
            case Opcodes.OP_CONST_CLASS /*28*/:
                return "Punctuation, Initial quote";
            case Opcodes.OP_MONITOR_ENTER /*29*/:
                return "Punctuation, Final quote";
            default:
                return "Unassigned";
        }
    }

    private UCharacterCategory() {
    }
}

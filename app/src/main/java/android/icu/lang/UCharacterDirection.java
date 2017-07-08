package android.icu.lang;

import android.icu.lang.UCharacterEnums.ECharacterDirection;
import dalvik.bytecode.Opcodes;
import libcore.icu.ICU;
import libcore.io.IoBridge;
import org.apache.harmony.security.provider.crypto.SHA1Constants;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public final class UCharacterDirection implements ECharacterDirection {
    private UCharacterDirection() {
    }

    public static String toString(int dir) {
        switch (dir) {
            case XmlPullParser.START_DOCUMENT /*0*/:
                return "Left-to-Right";
            case NodeFilter.SHOW_ELEMENT /*1*/:
                return "Right-to-Left";
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                return "European Number";
            case XmlPullParser.END_TAG /*3*/:
                return "European Number Separator";
            case NodeFilter.SHOW_TEXT /*4*/:
                return "European Number Terminator";
            case XmlPullParser.CDSECT /*5*/:
                return "Arabic Number";
            case XmlPullParser.ENTITY_REF /*6*/:
                return "Common Number Separator";
            case XmlPullParser.IGNORABLE_WHITESPACE /*7*/:
                return "Paragraph Separator";
            case NodeFilter.SHOW_CDATA_SECTION /*8*/:
                return "Segment Separator";
            case XmlPullParser.COMMENT /*9*/:
                return "Whitespace";
            case XmlPullParser.DOCDECL /*10*/:
                return "Other Neutrals";
            case ICU.U_TRUNCATED_CHAR_FOUND /*11*/:
                return "Left-to-Right Embedding";
            case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
                return "Left-to-Right Override";
            case Opcodes.OP_MOVE_EXCEPTION /*13*/:
                return "Right-to-Left Arabic";
            case Opcodes.OP_RETURN_VOID /*14*/:
                return "Right-to-Left Embedding";
            case ICU.U_BUFFER_OVERFLOW_ERROR /*15*/:
                return "Right-to-Left Override";
            case NodeFilter.SHOW_ENTITY_REFERENCE /*16*/:
                return "Pop Directional Format";
            case IoBridge.JAVA_IP_MULTICAST_TTL /*17*/:
                return "Non-Spacing Mark";
            case Opcodes.OP_CONST_4 /*18*/:
                return "Boundary Neutral";
            case IoBridge.JAVA_MCAST_JOIN_GROUP /*19*/:
                return "First Strong Isolate";
            case SHA1Constants.DIGEST_LENGTH /*20*/:
                return "Left-to-Right Isolate";
            case IoBridge.JAVA_MCAST_JOIN_SOURCE_GROUP /*21*/:
                return "Right-to-Left Isolate";
            case IoBridge.JAVA_MCAST_LEAVE_SOURCE_GROUP /*22*/:
                return "Pop Directional Isolate";
            default:
                return "Unassigned";
        }
    }
}

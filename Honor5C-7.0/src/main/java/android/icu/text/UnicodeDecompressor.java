package android.icu.text;

import android.icu.impl.Normalizer2Impl;
import android.icu.lang.UScript;
import dalvik.bytecode.Opcodes;
import libcore.icu.DateUtilsBridge;
import libcore.icu.ICU;
import libcore.io.IoBridge;
import org.apache.harmony.security.provider.crypto.SHA1Constants;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public final class UnicodeDecompressor implements SCSU {
    private static final int BUFSIZE = 3;
    private byte[] fBuffer;
    private int fBufferLength;
    private int fCurrentWindow;
    private int fMode;
    private int[] fOffsets;

    public UnicodeDecompressor() {
        this.fCurrentWindow = 0;
        this.fOffsets = new int[8];
        this.fMode = 0;
        this.fBuffer = new byte[BUFSIZE];
        this.fBufferLength = 0;
        reset();
    }

    public static String decompress(byte[] buffer) {
        return new String(decompress(buffer, 0, buffer.length));
    }

    public static char[] decompress(byte[] buffer, int start, int limit) {
        UnicodeDecompressor comp = new UnicodeDecompressor();
        int len = Math.max(2, (limit - start) * 2);
        char[] temp = new char[len];
        int charCount = comp.decompress(buffer, start, limit, null, temp, 0, len);
        char[] result = new char[charCount];
        System.arraycopy(temp, 0, result, 0, charCount);
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int decompress(byte[] byteBuffer, int byteBufferStart, int byteBufferLimit, int[] bytesRead, char[] charBuffer, int charBufferStart, int charBufferLimit) {
        int bytePos = byteBufferStart;
        int ucPos = charBufferStart;
        if (charBuffer.length < 2 || charBufferLimit - charBufferStart < 2) {
            throw new IllegalArgumentException("charBuffer.length < 2");
        }
        if (this.fBufferLength > 0) {
            int newBytes = 0;
            if (this.fBufferLength != BUFSIZE) {
                newBytes = this.fBuffer.length - this.fBufferLength;
                if (byteBufferLimit - byteBufferStart < newBytes) {
                    newBytes = byteBufferLimit - byteBufferStart;
                }
                System.arraycopy(byteBuffer, byteBufferStart, this.fBuffer, this.fBufferLength, newBytes);
            }
            this.fBufferLength = 0;
            ucPos = charBufferStart + decompress(this.fBuffer, 0, this.fBuffer.length, null, charBuffer, charBufferStart, charBufferLimit);
            bytePos = byteBufferStart + newBytes;
        }
        while (bytePos < byteBufferLimit && ucPos < charBufferLimit) {
            int ucPos2;
            int bytePos2;
            int aByte;
            switch (this.fMode) {
                case XmlPullParser.START_DOCUMENT /*0*/:
                    ucPos2 = ucPos;
                    bytePos2 = bytePos;
                    while (bytePos2 < byteBufferLimit && ucPos2 < charBufferLimit) {
                        bytePos = bytePos2 + 1;
                        aByte = byteBuffer[bytePos2] & Opcodes.OP_CONST_CLASS_JUMBO;
                        switch (aByte) {
                            case XmlPullParser.START_DOCUMENT /*0*/:
                            case XmlPullParser.COMMENT /*9*/:
                            case XmlPullParser.DOCDECL /*10*/:
                            case Opcodes.OP_MOVE_EXCEPTION /*13*/:
                            case NodeFilter.SHOW_ENTITY /*32*/:
                            case Opcodes.OP_ARRAY_LENGTH /*33*/:
                            case Opcodes.OP_NEW_INSTANCE /*34*/:
                            case Opcodes.OP_NEW_ARRAY /*35*/:
                            case Opcodes.OP_FILLED_NEW_ARRAY /*36*/:
                            case Opcodes.OP_FILLED_NEW_ARRAY_RANGE /*37*/:
                            case Opcodes.OP_FILL_ARRAY_DATA /*38*/:
                            case Opcodes.OP_THROW /*39*/:
                            case Opcodes.OP_GOTO /*40*/:
                            case Opcodes.OP_GOTO_16 /*41*/:
                            case Opcodes.OP_GOTO_32 /*42*/:
                            case Opcodes.OP_PACKED_SWITCH /*43*/:
                            case Opcodes.OP_SPARSE_SWITCH /*44*/:
                            case Opcodes.OP_CMPL_FLOAT /*45*/:
                            case Opcodes.OP_CMPG_FLOAT /*46*/:
                            case Opcodes.OP_CMPL_DOUBLE /*47*/:
                            case Opcodes.OP_CMPG_DOUBLE /*48*/:
                            case Opcodes.OP_CMP_LONG /*49*/:
                            case Opcodes.OP_IF_EQ /*50*/:
                            case Opcodes.OP_IF_NE /*51*/:
                            case Opcodes.OP_IF_LT /*52*/:
                            case Opcodes.OP_IF_GE /*53*/:
                            case Opcodes.OP_IF_GT /*54*/:
                            case Opcodes.OP_IF_LE /*55*/:
                            case Opcodes.OP_IF_EQZ /*56*/:
                            case Opcodes.OP_IF_NEZ /*57*/:
                            case Opcodes.OP_IF_LTZ /*58*/:
                            case Opcodes.OP_IF_GEZ /*59*/:
                            case Opcodes.OP_IF_GTZ /*60*/:
                            case Opcodes.OP_IF_LEZ /*61*/:
                            case UScript.BALINESE /*62*/:
                            case UScript.BATAK /*63*/:
                            case NodeFilter.SHOW_PROCESSING_INSTRUCTION /*64*/:
                            case UScript.BRAHMI /*65*/:
                            case UScript.CHAM /*66*/:
                            case UScript.CIRTH /*67*/:
                            case Opcodes.OP_AGET /*68*/:
                            case Opcodes.OP_AGET_WIDE /*69*/:
                            case Opcodes.OP_AGET_OBJECT /*70*/:
                            case Opcodes.OP_AGET_BOOLEAN /*71*/:
                            case Opcodes.OP_AGET_BYTE /*72*/:
                            case Opcodes.OP_AGET_CHAR /*73*/:
                            case Opcodes.OP_AGET_SHORT /*74*/:
                            case Opcodes.OP_APUT /*75*/:
                            case Opcodes.OP_APUT_WIDE /*76*/:
                            case Opcodes.OP_APUT_OBJECT /*77*/:
                            case Opcodes.OP_APUT_BOOLEAN /*78*/:
                            case Opcodes.OP_APUT_BYTE /*79*/:
                            case Opcodes.OP_APUT_CHAR /*80*/:
                            case SHA1Constants.BYTES_OFFSET /*81*/:
                            case SHA1Constants.HASH_OFFSET /*82*/:
                            case Opcodes.OP_IGET_WIDE /*83*/:
                            case Opcodes.OP_IGET_OBJECT /*84*/:
                            case Opcodes.OP_IGET_BOOLEAN /*85*/:
                            case Opcodes.OP_IGET_BYTE /*86*/:
                            case Opcodes.OP_IGET_CHAR /*87*/:
                            case Opcodes.OP_IGET_SHORT /*88*/:
                            case Opcodes.OP_IPUT /*89*/:
                            case Opcodes.OP_IPUT_WIDE /*90*/:
                            case Opcodes.OP_IPUT_OBJECT /*91*/:
                            case Opcodes.OP_IPUT_BOOLEAN /*92*/:
                            case Opcodes.OP_IPUT_BYTE /*93*/:
                            case Opcodes.OP_IPUT_CHAR /*94*/:
                            case Opcodes.OP_IPUT_SHORT /*95*/:
                            case Opcodes.OP_SGET /*96*/:
                            case Opcodes.OP_SGET_WIDE /*97*/:
                            case Opcodes.OP_SGET_OBJECT /*98*/:
                            case Opcodes.OP_SGET_BOOLEAN /*99*/:
                            case Opcodes.OP_SGET_BYTE /*100*/:
                            case Opcodes.OP_SGET_CHAR /*101*/:
                            case Opcodes.OP_SGET_SHORT /*102*/:
                            case Opcodes.OP_SPUT /*103*/:
                            case Opcodes.OP_SPUT_WIDE /*104*/:
                            case Opcodes.OP_SPUT_OBJECT /*105*/:
                            case Opcodes.OP_SPUT_BOOLEAN /*106*/:
                            case Opcodes.OP_SPUT_BYTE /*107*/:
                            case Opcodes.OP_SPUT_CHAR /*108*/:
                            case Opcodes.OP_SPUT_SHORT /*109*/:
                            case Opcodes.OP_INVOKE_VIRTUAL /*110*/:
                            case Opcodes.OP_INVOKE_SUPER /*111*/:
                            case Opcodes.OP_INVOKE_DIRECT /*112*/:
                            case Opcodes.OP_INVOKE_STATIC /*113*/:
                            case Opcodes.OP_INVOKE_INTERFACE /*114*/:
                            case UScript.MEITEI_MAYEK /*115*/:
                            case Opcodes.OP_INVOKE_VIRTUAL_RANGE /*116*/:
                            case Opcodes.OP_INVOKE_SUPER_RANGE /*117*/:
                            case Opcodes.OP_INVOKE_DIRECT_RANGE /*118*/:
                            case Opcodes.OP_INVOKE_STATIC_RANGE /*119*/:
                            case Opcodes.OP_INVOKE_INTERFACE_RANGE /*120*/:
                            case UScript.MANICHAEAN /*121*/:
                            case UScript.INSCRIPTIONAL_PAHLAVI /*122*/:
                            case Opcodes.OP_NEG_INT /*123*/:
                            case Opcodes.OP_NOT_INT /*124*/:
                            case Opcodes.OP_NEG_LONG /*125*/:
                            case Opcodes.OP_NOT_LONG /*126*/:
                            case Opcodes.OP_NEG_FLOAT /*127*/:
                                ucPos = ucPos2 + 1;
                                charBuffer[ucPos2] = (char) aByte;
                                break;
                            case NodeFilter.SHOW_ELEMENT /*1*/:
                            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                            case BUFSIZE /*3*/:
                            case NodeFilter.SHOW_TEXT /*4*/:
                            case XmlPullParser.CDSECT /*5*/:
                            case XmlPullParser.ENTITY_REF /*6*/:
                            case XmlPullParser.IGNORABLE_WHITESPACE /*7*/:
                            case NodeFilter.SHOW_CDATA_SECTION /*8*/:
                                if (bytePos < byteBufferLimit) {
                                    int i;
                                    bytePos2 = bytePos + 1;
                                    int dByte = byteBuffer[bytePos] & Opcodes.OP_CONST_CLASS_JUMBO;
                                    ucPos = ucPos2 + 1;
                                    if (dByte < 0 || dByte >= NodeFilter.SHOW_COMMENT) {
                                        i = this.fOffsets[aByte - 1] - 128;
                                    } else {
                                        i = sOffsets[aByte - 1];
                                    }
                                    charBuffer[ucPos2] = (char) (i + dByte);
                                    bytePos = bytePos2;
                                    break;
                                }
                                bytePos--;
                                System.arraycopy(byteBuffer, bytePos, this.fBuffer, 0, byteBufferLimit - bytePos);
                                this.fBufferLength = byteBufferLimit - bytePos;
                                bytePos += this.fBufferLength;
                                ucPos = ucPos2;
                                break;
                            case ICU.U_TRUNCATED_CHAR_FOUND /*11*/:
                                if (bytePos + 1 < byteBufferLimit) {
                                    bytePos2 = bytePos + 1;
                                    aByte = byteBuffer[bytePos] & Opcodes.OP_CONST_CLASS_JUMBO;
                                    this.fCurrentWindow = (aByte & Opcodes.OP_SHL_INT_LIT8) >> 5;
                                    bytePos = bytePos2 + 1;
                                    this.fOffsets[this.fCurrentWindow] = ((((aByte & 31) << 8) | (byteBuffer[bytePos2] & Opcodes.OP_CONST_CLASS_JUMBO)) * NodeFilter.SHOW_COMMENT) + DateUtilsBridge.FORMAT_ABBREV_MONTH;
                                    ucPos = ucPos2;
                                    break;
                                }
                                bytePos--;
                                System.arraycopy(byteBuffer, bytePos, this.fBuffer, 0, byteBufferLimit - bytePos);
                                this.fBufferLength = byteBufferLimit - bytePos;
                                bytePos += this.fBufferLength;
                                ucPos = ucPos2;
                                break;
                            case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
                                ucPos = ucPos2;
                                break;
                            case Opcodes.OP_RETURN_VOID /*14*/:
                                if (bytePos + 1 < byteBufferLimit) {
                                    bytePos2 = bytePos + 1;
                                    ucPos = ucPos2 + 1;
                                    bytePos = bytePos2 + 1;
                                    charBuffer[ucPos2] = (char) ((byteBuffer[bytePos] << 8) | (byteBuffer[bytePos2] & Opcodes.OP_CONST_CLASS_JUMBO));
                                    break;
                                }
                                bytePos--;
                                System.arraycopy(byteBuffer, bytePos, this.fBuffer, 0, byteBufferLimit - bytePos);
                                this.fBufferLength = byteBufferLimit - bytePos;
                                bytePos += this.fBufferLength;
                                ucPos = ucPos2;
                                break;
                            case ICU.U_BUFFER_OVERFLOW_ERROR /*15*/:
                                this.fMode = 1;
                                break;
                            case NodeFilter.SHOW_ENTITY_REFERENCE /*16*/:
                            case IoBridge.JAVA_IP_MULTICAST_TTL /*17*/:
                            case Opcodes.OP_CONST_4 /*18*/:
                            case IoBridge.JAVA_MCAST_JOIN_GROUP /*19*/:
                            case SHA1Constants.DIGEST_LENGTH /*20*/:
                            case IoBridge.JAVA_MCAST_JOIN_SOURCE_GROUP /*21*/:
                            case IoBridge.JAVA_MCAST_LEAVE_SOURCE_GROUP /*22*/:
                            case IoBridge.JAVA_MCAST_BLOCK_SOURCE /*23*/:
                                this.fCurrentWindow = aByte - 16;
                                ucPos = ucPos2;
                                break;
                            case IoBridge.JAVA_MCAST_UNBLOCK_SOURCE /*24*/:
                            case Opcodes.OP_CONST_WIDE_HIGH16 /*25*/:
                            case Opcodes.OP_CONST_STRING /*26*/:
                            case Opcodes.OP_CONST_STRING_JUMBO /*27*/:
                            case Opcodes.OP_CONST_CLASS /*28*/:
                            case Opcodes.OP_MONITOR_ENTER /*29*/:
                            case Opcodes.OP_MONITOR_EXIT /*30*/:
                            case Opcodes.OP_CHECK_CAST /*31*/:
                                if (bytePos < byteBufferLimit) {
                                    this.fCurrentWindow = aByte - 24;
                                    bytePos2 = bytePos + 1;
                                    this.fOffsets[this.fCurrentWindow] = sOffsetTable[byteBuffer[bytePos] & Opcodes.OP_CONST_CLASS_JUMBO];
                                    ucPos = ucPos2;
                                    bytePos = bytePos2;
                                    break;
                                }
                                bytePos--;
                                System.arraycopy(byteBuffer, bytePos, this.fBuffer, 0, byteBufferLimit - bytePos);
                                this.fBufferLength = byteBufferLimit - bytePos;
                                bytePos += this.fBufferLength;
                                ucPos = ucPos2;
                                break;
                            case NodeFilter.SHOW_COMMENT /*128*/:
                            case Opcodes.OP_INT_TO_LONG /*129*/:
                            case Opcodes.OP_INT_TO_FLOAT /*130*/:
                            case Opcodes.OP_INT_TO_DOUBLE /*131*/:
                            case Opcodes.OP_LONG_TO_INT /*132*/:
                            case Opcodes.OP_LONG_TO_FLOAT /*133*/:
                            case Opcodes.OP_LONG_TO_DOUBLE /*134*/:
                            case Opcodes.OP_FLOAT_TO_INT /*135*/:
                            case Opcodes.OP_FLOAT_TO_LONG /*136*/:
                            case Opcodes.OP_FLOAT_TO_DOUBLE /*137*/:
                            case Opcodes.OP_DOUBLE_TO_INT /*138*/:
                            case Opcodes.OP_DOUBLE_TO_LONG /*139*/:
                            case Opcodes.OP_DOUBLE_TO_FLOAT /*140*/:
                            case Opcodes.OP_INT_TO_BYTE /*141*/:
                            case Opcodes.OP_INT_TO_CHAR /*142*/:
                            case Opcodes.OP_INT_TO_SHORT /*143*/:
                            case Opcodes.OP_ADD_INT /*144*/:
                            case Opcodes.OP_SUB_INT /*145*/:
                            case Opcodes.OP_MUL_INT /*146*/:
                            case Opcodes.OP_DIV_INT /*147*/:
                            case Opcodes.OP_REM_INT /*148*/:
                            case Opcodes.OP_AND_INT /*149*/:
                            case Opcodes.OP_OR_INT /*150*/:
                            case Opcodes.OP_XOR_INT /*151*/:
                            case Opcodes.OP_SHL_INT /*152*/:
                            case Opcodes.OP_SHR_INT /*153*/:
                            case Opcodes.OP_USHR_INT /*154*/:
                            case Opcodes.OP_ADD_LONG /*155*/:
                            case Opcodes.OP_SUB_LONG /*156*/:
                            case Opcodes.OP_MUL_LONG /*157*/:
                            case Opcodes.OP_DIV_LONG /*158*/:
                            case Opcodes.OP_REM_LONG /*159*/:
                            case Opcodes.OP_AND_LONG /*160*/:
                            case Opcodes.OP_OR_LONG /*161*/:
                            case Opcodes.OP_XOR_LONG /*162*/:
                            case Opcodes.OP_SHL_LONG /*163*/:
                            case Opcodes.OP_SHR_LONG /*164*/:
                            case Opcodes.OP_USHR_LONG /*165*/:
                            case Opcodes.OP_ADD_FLOAT /*166*/:
                            case Opcodes.OP_SUB_FLOAT /*167*/:
                            case Opcodes.OP_MUL_FLOAT /*168*/:
                            case Opcodes.OP_DIV_FLOAT /*169*/:
                            case Opcodes.OP_REM_FLOAT /*170*/:
                            case Opcodes.OP_ADD_DOUBLE /*171*/:
                            case Opcodes.OP_SUB_DOUBLE /*172*/:
                            case Opcodes.OP_MUL_DOUBLE /*173*/:
                            case Opcodes.OP_DIV_DOUBLE /*174*/:
                            case Opcodes.OP_REM_DOUBLE /*175*/:
                            case Opcodes.OP_ADD_INT_2ADDR /*176*/:
                            case Opcodes.OP_SUB_INT_2ADDR /*177*/:
                            case Opcodes.OP_MUL_INT_2ADDR /*178*/:
                            case Opcodes.OP_DIV_INT_2ADDR /*179*/:
                            case Opcodes.OP_REM_INT_2ADDR /*180*/:
                            case Opcodes.OP_AND_INT_2ADDR /*181*/:
                            case Opcodes.OP_OR_INT_2ADDR /*182*/:
                            case Opcodes.OP_XOR_INT_2ADDR /*183*/:
                            case Opcodes.OP_SHL_INT_2ADDR /*184*/:
                            case Opcodes.OP_SHR_INT_2ADDR /*185*/:
                            case Opcodes.OP_USHR_INT_2ADDR /*186*/:
                            case Opcodes.OP_ADD_LONG_2ADDR /*187*/:
                            case Opcodes.OP_SUB_LONG_2ADDR /*188*/:
                            case Opcodes.OP_MUL_LONG_2ADDR /*189*/:
                            case Opcodes.OP_DIV_LONG_2ADDR /*190*/:
                            case Opcodes.OP_REM_LONG_2ADDR /*191*/:
                            case Opcodes.OP_AND_LONG_2ADDR /*192*/:
                            case Opcodes.OP_OR_LONG_2ADDR /*193*/:
                            case Opcodes.OP_XOR_LONG_2ADDR /*194*/:
                            case Opcodes.OP_SHL_LONG_2ADDR /*195*/:
                            case Opcodes.OP_SHR_LONG_2ADDR /*196*/:
                            case Opcodes.OP_USHR_LONG_2ADDR /*197*/:
                            case Opcodes.OP_ADD_FLOAT_2ADDR /*198*/:
                            case Opcodes.OP_SUB_FLOAT_2ADDR /*199*/:
                            case Opcodes.OP_MUL_FLOAT_2ADDR /*200*/:
                            case Opcodes.OP_DIV_FLOAT_2ADDR /*201*/:
                            case Opcodes.OP_REM_FLOAT_2ADDR /*202*/:
                            case Opcodes.OP_ADD_DOUBLE_2ADDR /*203*/:
                            case Opcodes.OP_SUB_DOUBLE_2ADDR /*204*/:
                            case Opcodes.OP_MUL_DOUBLE_2ADDR /*205*/:
                            case Opcodes.OP_DIV_DOUBLE_2ADDR /*206*/:
                            case Opcodes.OP_REM_DOUBLE_2ADDR /*207*/:
                            case Opcodes.OP_ADD_INT_LIT16 /*208*/:
                            case Opcodes.OP_RSUB_INT /*209*/:
                            case Opcodes.OP_MUL_INT_LIT16 /*210*/:
                            case Opcodes.OP_DIV_INT_LIT16 /*211*/:
                            case Opcodes.OP_REM_INT_LIT16 /*212*/:
                            case Opcodes.OP_AND_INT_LIT16 /*213*/:
                            case Opcodes.OP_OR_INT_LIT16 /*214*/:
                            case Opcodes.OP_XOR_INT_LIT16 /*215*/:
                            case Opcodes.OP_ADD_INT_LIT8 /*216*/:
                            case Opcodes.OP_RSUB_INT_LIT8 /*217*/:
                            case Opcodes.OP_MUL_INT_LIT8 /*218*/:
                            case Opcodes.OP_DIV_INT_LIT8 /*219*/:
                            case Opcodes.OP_REM_INT_LIT8 /*220*/:
                            case Opcodes.OP_AND_INT_LIT8 /*221*/:
                            case Opcodes.OP_OR_INT_LIT8 /*222*/:
                            case Opcodes.OP_XOR_INT_LIT8 /*223*/:
                            case Opcodes.OP_SHL_INT_LIT8 /*224*/:
                            case Opcodes.OP_SHR_INT_LIT8 /*225*/:
                            case Opcodes.OP_USHR_INT_LIT8 /*226*/:
                            case SCSU.UCHANGE3 /*227*/:
                            case SCSU.UCHANGE4 /*228*/:
                            case SCSU.UCHANGE5 /*229*/:
                            case SCSU.UCHANGE6 /*230*/:
                            case SCSU.UCHANGE7 /*231*/:
                            case Opcodes.OP_IGET_WIDE_VOLATILE /*232*/:
                            case Opcodes.OP_IPUT_WIDE_VOLATILE /*233*/:
                            case Opcodes.OP_SGET_WIDE_VOLATILE /*234*/:
                            case Opcodes.OP_SPUT_WIDE_VOLATILE /*235*/:
                            case Opcodes.OP_BREAKPOINT /*236*/:
                            case Opcodes.OP_THROW_VERIFICATION_ERROR /*237*/:
                            case Opcodes.OP_EXECUTE_INLINE /*238*/:
                            case Opcodes.OP_EXECUTE_INLINE_RANGE /*239*/:
                            case Opcodes.OP_INVOKE_DIRECT_EMPTY /*240*/:
                            case SCSU.UDEFINEX /*241*/:
                            case Opcodes.OP_IGET_QUICK /*242*/:
                            case Opcodes.OP_IGET_WIDE_QUICK /*243*/:
                            case Opcodes.OP_IGET_OBJECT_QUICK /*244*/:
                            case Opcodes.OP_IPUT_QUICK /*245*/:
                            case Opcodes.OP_IPUT_WIDE_QUICK /*246*/:
                            case Opcodes.OP_IPUT_OBJECT_QUICK /*247*/:
                            case Opcodes.OP_INVOKE_VIRTUAL_QUICK /*248*/:
                            case Opcodes.OP_INVOKE_VIRTUAL_QUICK_RANGE /*249*/:
                            case Opcodes.OP_INVOKE_SUPER_QUICK /*250*/:
                            case Opcodes.OP_INVOKE_SUPER_QUICK_RANGE /*251*/:
                            case SCSU.ARMENIANINDEX /*252*/:
                            case SCSU.HIRAGANAINDEX /*253*/:
                            case SCSU.KATAKANAINDEX /*254*/:
                            case Opcodes.OP_CONST_CLASS_JUMBO /*255*/:
                                if (this.fOffsets[this.fCurrentWindow] > 65535) {
                                    if (ucPos2 + 1 < charBufferLimit) {
                                        int normalizedBase = this.fOffsets[this.fCurrentWindow] - DateUtilsBridge.FORMAT_ABBREV_MONTH;
                                        ucPos = ucPos2 + 1;
                                        charBuffer[ucPos2] = (char) ((normalizedBase >> 10) + UTF16.SURROGATE_MIN_VALUE);
                                        ucPos2 = ucPos + 1;
                                        charBuffer[ucPos] = (char) (((normalizedBase & Opcodes.OP_NEW_INSTANCE_JUMBO) + UTF16.TRAIL_SURROGATE_MIN_VALUE) + (aByte & Opcodes.OP_NEG_FLOAT));
                                        ucPos = ucPos2;
                                        break;
                                    }
                                    bytePos--;
                                    System.arraycopy(byteBuffer, bytePos, this.fBuffer, 0, byteBufferLimit - bytePos);
                                    this.fBufferLength = byteBufferLimit - bytePos;
                                    bytePos += this.fBufferLength;
                                    ucPos = ucPos2;
                                    break;
                                }
                                ucPos = ucPos2 + 1;
                                charBuffer[ucPos2] = (char) ((this.fOffsets[this.fCurrentWindow] + aByte) - 128);
                                break;
                            default:
                                ucPos = ucPos2;
                                break;
                        }
                    }
                    bytePos = bytePos2;
                    ucPos = ucPos2;
                    continue;
                case NodeFilter.SHOW_ELEMENT /*1*/:
                    ucPos2 = ucPos;
                    bytePos2 = bytePos;
                    while (bytePos2 < byteBufferLimit && ucPos2 < charBufferLimit) {
                        bytePos = bytePos2 + 1;
                        aByte = byteBuffer[bytePos2] & Opcodes.OP_CONST_CLASS_JUMBO;
                        switch (aByte) {
                            case Opcodes.OP_SHL_INT_LIT8 /*224*/:
                            case Opcodes.OP_SHR_INT_LIT8 /*225*/:
                            case Opcodes.OP_USHR_INT_LIT8 /*226*/:
                            case SCSU.UCHANGE3 /*227*/:
                            case SCSU.UCHANGE4 /*228*/:
                            case SCSU.UCHANGE5 /*229*/:
                            case SCSU.UCHANGE6 /*230*/:
                            case SCSU.UCHANGE7 /*231*/:
                                this.fCurrentWindow = aByte - 224;
                                this.fMode = 0;
                                break;
                            case Opcodes.OP_IGET_WIDE_VOLATILE /*232*/:
                            case Opcodes.OP_IPUT_WIDE_VOLATILE /*233*/:
                            case Opcodes.OP_SGET_WIDE_VOLATILE /*234*/:
                            case Opcodes.OP_SPUT_WIDE_VOLATILE /*235*/:
                            case Opcodes.OP_BREAKPOINT /*236*/:
                            case Opcodes.OP_THROW_VERIFICATION_ERROR /*237*/:
                            case Opcodes.OP_EXECUTE_INLINE /*238*/:
                            case Opcodes.OP_EXECUTE_INLINE_RANGE /*239*/:
                                if (bytePos < byteBufferLimit) {
                                    this.fCurrentWindow = aByte - 232;
                                    bytePos2 = bytePos + 1;
                                    this.fOffsets[this.fCurrentWindow] = sOffsetTable[byteBuffer[bytePos] & Opcodes.OP_CONST_CLASS_JUMBO];
                                    this.fMode = 0;
                                    bytePos = bytePos2;
                                    break;
                                }
                                bytePos--;
                                System.arraycopy(byteBuffer, bytePos, this.fBuffer, 0, byteBufferLimit - bytePos);
                                this.fBufferLength = byteBufferLimit - bytePos;
                                bytePos += this.fBufferLength;
                                ucPos = ucPos2;
                                break;
                            case Opcodes.OP_INVOKE_DIRECT_EMPTY /*240*/:
                                if (bytePos < byteBufferLimit - 1) {
                                    bytePos2 = bytePos + 1;
                                    ucPos = ucPos2 + 1;
                                    bytePos = bytePos2 + 1;
                                    charBuffer[ucPos2] = (char) ((byteBuffer[bytePos] << 8) | (byteBuffer[bytePos2] & Opcodes.OP_CONST_CLASS_JUMBO));
                                    break;
                                }
                                bytePos--;
                                System.arraycopy(byteBuffer, bytePos, this.fBuffer, 0, byteBufferLimit - bytePos);
                                this.fBufferLength = byteBufferLimit - bytePos;
                                bytePos += this.fBufferLength;
                                ucPos = ucPos2;
                                break;
                            case SCSU.UDEFINEX /*241*/:
                                if (bytePos + 1 < byteBufferLimit) {
                                    bytePos2 = bytePos + 1;
                                    aByte = byteBuffer[bytePos] & Opcodes.OP_CONST_CLASS_JUMBO;
                                    this.fCurrentWindow = (aByte & Opcodes.OP_SHL_INT_LIT8) >> 5;
                                    bytePos = bytePos2 + 1;
                                    this.fOffsets[this.fCurrentWindow] = ((((aByte & 31) << 8) | (byteBuffer[bytePos2] & Opcodes.OP_CONST_CLASS_JUMBO)) * NodeFilter.SHOW_COMMENT) + DateUtilsBridge.FORMAT_ABBREV_MONTH;
                                    this.fMode = 0;
                                    break;
                                }
                                bytePos--;
                                System.arraycopy(byteBuffer, bytePos, this.fBuffer, 0, byteBufferLimit - bytePos);
                                this.fBufferLength = byteBufferLimit - bytePos;
                                bytePos += this.fBufferLength;
                                ucPos = ucPos2;
                                break;
                            default:
                                if (bytePos < byteBufferLimit) {
                                    ucPos = ucPos2 + 1;
                                    bytePos2 = bytePos + 1;
                                    charBuffer[ucPos2] = (char) ((aByte << 8) | (byteBuffer[bytePos] & Opcodes.OP_CONST_CLASS_JUMBO));
                                    bytePos = bytePos2;
                                    break;
                                }
                                bytePos--;
                                System.arraycopy(byteBuffer, bytePos, this.fBuffer, 0, byteBufferLimit - bytePos);
                                this.fBufferLength = byteBufferLimit - bytePos;
                                bytePos += this.fBufferLength;
                                ucPos = ucPos2;
                                break;
                        }
                    }
                    bytePos = bytePos2;
                    ucPos = ucPos2;
                    continue;
                default:
                    continue;
            }
        }
        if (bytesRead != null) {
            bytesRead[0] = bytePos - byteBufferStart;
        }
        return ucPos - charBufferStart;
    }

    public void reset() {
        this.fOffsets[0] = NodeFilter.SHOW_COMMENT;
        this.fOffsets[1] = Opcodes.OP_AND_LONG_2ADDR;
        this.fOffsets[2] = NodeFilter.SHOW_DOCUMENT_FRAGMENT;
        this.fOffsets[BUFSIZE] = 1536;
        this.fOffsets[4] = 2304;
        this.fOffsets[5] = 12352;
        this.fOffsets[6] = 12448;
        this.fOffsets[7] = Normalizer2Impl.JAMO_VT;
        this.fCurrentWindow = 0;
        this.fMode = 0;
        this.fBufferLength = 0;
    }
}

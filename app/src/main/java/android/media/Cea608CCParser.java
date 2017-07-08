package android.media;

import android.bluetooth.BluetoothAssignedNumbers;
import android.bluetooth.BluetoothAvrcp;
import android.net.wifi.ScanResult.InformationElement;
import android.net.wifi.WifiScanner.PnoSettings.PnoNetwork;
import android.renderscript.ScriptIntrinsicBLAS;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.view.accessibility.CaptioningManager.CaptionStyle;
import java.util.ArrayList;
import java.util.Arrays;

/* compiled from: ClosedCaptionRenderer */
class Cea608CCParser {
    private static final int AOF = 34;
    private static final int AON = 35;
    private static final int BS = 33;
    private static final int CR = 45;
    private static final boolean DEBUG = false;
    private static final int DER = 36;
    private static final int EDM = 44;
    private static final int ENM = 46;
    private static final int EOC = 47;
    private static final int FON = 40;
    private static final int INVALID = -1;
    public static final int MAX_COLS = 32;
    public static final int MAX_ROWS = 15;
    private static final int MODE_PAINT_ON = 1;
    private static final int MODE_POP_ON = 3;
    private static final int MODE_ROLL_UP = 2;
    private static final int MODE_TEXT = 4;
    private static final int MODE_UNKNOWN = 0;
    private static final int RCL = 32;
    private static final int RDC = 41;
    private static final int RTD = 43;
    private static final int RU2 = 37;
    private static final int RU3 = 38;
    private static final int RU4 = 39;
    private static final String TAG = "Cea608CCParser";
    private static final int TR = 42;
    private static final char TS = '\u00a0';
    private CCMemory mDisplay;
    private final DisplayListener mListener;
    private int mMode;
    private CCMemory mNonDisplay;
    private int mPrevCtrlCode;
    private int mRollUpSize;
    private CCMemory mTextMem;

    /* compiled from: ClosedCaptionRenderer */
    private static class CCData {
        private static final String[] mCtrlCodeMap = null;
        private static final String[] mProtugueseCharMap = null;
        private static final String[] mSpanishCharMap = null;
        private static final String[] mSpecialCharMap = null;
        private final byte mData1;
        private final byte mData2;
        private final byte mType;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.Cea608CCParser.CCData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.Cea608CCParser.CCData.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.Cea608CCParser.CCData.<clinit>():void");
        }

        static CCData[] fromByteArray(byte[] data) {
            CCData[] ccData = new CCData[(data.length / Cea608CCParser.MODE_POP_ON)];
            for (int i = Cea608CCParser.MODE_UNKNOWN; i < ccData.length; i += Cea608CCParser.MODE_PAINT_ON) {
                ccData[i] = new CCData(data[i * Cea608CCParser.MODE_POP_ON], data[(i * Cea608CCParser.MODE_POP_ON) + Cea608CCParser.MODE_PAINT_ON], data[(i * Cea608CCParser.MODE_POP_ON) + Cea608CCParser.MODE_ROLL_UP]);
            }
            return ccData;
        }

        CCData(byte type, byte data1, byte data2) {
            this.mType = type;
            this.mData1 = data1;
            this.mData2 = data2;
        }

        int getCtrlCode() {
            if ((this.mData1 == 20 || this.mData1 == 28) && this.mData2 >= Cea608CCParser.RCL && this.mData2 <= Cea608CCParser.EOC) {
                return this.mData2;
            }
            return Cea608CCParser.INVALID;
        }

        StyleCode getMidRow() {
            if ((this.mData1 == 17 || this.mData1 == 25) && this.mData2 >= Cea608CCParser.RCL && this.mData2 <= Cea608CCParser.EOC) {
                return StyleCode.fromByte(this.mData2);
            }
            return null;
        }

        PAC getPAC() {
            if ((this.mData1 & ScriptIntrinsicBLAS.TRANSPOSE) == 16 && (this.mData2 & 64) == 64 && ((this.mData1 & 7) != 0 || (this.mData2 & Cea608CCParser.RCL) == 0)) {
                return PAC.fromBytes(this.mData1, this.mData2);
            }
            return null;
        }

        int getTabOffset() {
            if ((this.mData1 == 23 || this.mData1 == 31) && this.mData2 >= Cea608CCParser.BS && this.mData2 <= Cea608CCParser.AON) {
                return this.mData2 & Cea608CCParser.MODE_POP_ON;
            }
            return Cea608CCParser.MODE_UNKNOWN;
        }

        boolean isDisplayableChar() {
            return (isBasicChar() || isSpecialChar()) ? true : isExtendedChar();
        }

        String getDisplayText() {
            String str = getBasicChars();
            if (str != null) {
                return str;
            }
            str = getSpecialChar();
            if (str == null) {
                return getExtendedChar();
            }
            return str;
        }

        private String ctrlCodeToString(int ctrlCode) {
            return mCtrlCodeMap[ctrlCode - 32];
        }

        private boolean isBasicChar() {
            return (this.mData1 < Cea608CCParser.RCL || this.mData1 > 127) ? Cea608CCParser.DEBUG : true;
        }

        private boolean isSpecialChar() {
            if ((this.mData1 == 17 || this.mData1 == 25) && this.mData2 >= 48 && this.mData2 <= 63) {
                return true;
            }
            return Cea608CCParser.DEBUG;
        }

        private boolean isExtendedChar() {
            if (!(this.mData1 == 18 || this.mData1 == 26 || this.mData1 == 19)) {
                if (this.mData1 != 27) {
                    return Cea608CCParser.DEBUG;
                }
            }
            if (this.mData2 < Cea608CCParser.RCL || this.mData2 > 63) {
                return Cea608CCParser.DEBUG;
            }
            return true;
        }

        private char getBasicChar(byte data) {
            switch (data) {
                case Cea608CCParser.TR /*42*/:
                    return '\u00e1';
                case ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK /*92*/:
                    return '\u00e9';
                case ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE /*94*/:
                    return '\u00ed';
                case ToneGenerator.TONE_CDMA_CALLDROP_LITE /*95*/:
                    return '\u00f3';
                case ToneGenerator.TONE_CDMA_NETWORK_BUSY_ONE_SHOT /*96*/:
                    return '\u00fa';
                case BluetoothAssignedNumbers.HANLYNN_TECHNOLOGIES /*123*/:
                    return '\u00e7';
                case BluetoothAssignedNumbers.A_AND_R_CAMBRIDGE /*124*/:
                    return '\u00f7';
                case BluetoothAssignedNumbers.SEERS_TECHNOLOGY /*125*/:
                    return '\u00d1';
                case BluetoothAvrcp.PASSTHROUGH_ID_VENDOR /*126*/:
                    return '\u00f1';
                case InformationElement.EID_EXTENDED_CAPS /*127*/:
                    return '\u2588';
                default:
                    return (char) data;
            }
        }

        private String getBasicChars() {
            if (this.mData1 < (byte) 32 || this.mData1 > Byte.MAX_VALUE) {
                return null;
            }
            StringBuilder builder = new StringBuilder(Cea608CCParser.MODE_ROLL_UP);
            builder.append(getBasicChar(this.mData1));
            if (this.mData2 >= (byte) 32 && this.mData2 <= Byte.MAX_VALUE) {
                builder.append(getBasicChar(this.mData2));
            }
            return builder.toString();
        }

        private String getSpecialChar() {
            if ((this.mData1 == 17 || this.mData1 == 25) && this.mData2 >= 48 && this.mData2 <= 63) {
                return mSpecialCharMap[this.mData2 - 48];
            }
            return null;
        }

        private String getExtendedChar() {
            if ((this.mData1 == 18 || this.mData1 == 26) && this.mData2 >= (byte) 32 && this.mData2 <= (byte) 63) {
                return mSpanishCharMap[this.mData2 - 32];
            }
            if ((this.mData1 == 19 || this.mData1 == 27) && this.mData2 >= (byte) 32 && this.mData2 <= (byte) 63) {
                return mProtugueseCharMap[this.mData2 - 32];
            }
            return null;
        }

        public String toString() {
            Object[] objArr;
            if (this.mData1 >= PnoNetwork.FLAG_SAME_NETWORK || this.mData2 >= PnoNetwork.FLAG_SAME_NETWORK) {
                int ctrlCode = getCtrlCode();
                if (ctrlCode != Cea608CCParser.INVALID) {
                    objArr = new Object[Cea608CCParser.MODE_ROLL_UP];
                    objArr[Cea608CCParser.MODE_UNKNOWN] = Byte.valueOf(this.mType);
                    objArr[Cea608CCParser.MODE_PAINT_ON] = ctrlCodeToString(ctrlCode);
                    return String.format("[%d]%s", objArr);
                }
                int tabOffset = getTabOffset();
                if (tabOffset > 0) {
                    objArr = new Object[Cea608CCParser.MODE_ROLL_UP];
                    objArr[Cea608CCParser.MODE_UNKNOWN] = Byte.valueOf(this.mType);
                    objArr[Cea608CCParser.MODE_PAINT_ON] = Integer.valueOf(tabOffset);
                    return String.format("[%d]Tab%d", objArr);
                }
                PAC pac = getPAC();
                if (pac != null) {
                    objArr = new Object[Cea608CCParser.MODE_ROLL_UP];
                    objArr[Cea608CCParser.MODE_UNKNOWN] = Byte.valueOf(this.mType);
                    objArr[Cea608CCParser.MODE_PAINT_ON] = pac.toString();
                    return String.format("[%d]PAC: %s", objArr);
                }
                StyleCode m = getMidRow();
                if (m != null) {
                    objArr = new Object[Cea608CCParser.MODE_ROLL_UP];
                    objArr[Cea608CCParser.MODE_UNKNOWN] = Byte.valueOf(this.mType);
                    objArr[Cea608CCParser.MODE_PAINT_ON] = m.toString();
                    return String.format("[%d]Mid-row: %s", objArr);
                } else if (isDisplayableChar()) {
                    objArr = new Object[Cea608CCParser.MODE_TEXT];
                    objArr[Cea608CCParser.MODE_UNKNOWN] = Byte.valueOf(this.mType);
                    objArr[Cea608CCParser.MODE_PAINT_ON] = getDisplayText();
                    objArr[Cea608CCParser.MODE_ROLL_UP] = Byte.valueOf(this.mData1);
                    objArr[Cea608CCParser.MODE_POP_ON] = Byte.valueOf(this.mData2);
                    return String.format("[%d]Displayable: %s (%02x %02x)", objArr);
                } else {
                    objArr = new Object[Cea608CCParser.MODE_POP_ON];
                    objArr[Cea608CCParser.MODE_UNKNOWN] = Byte.valueOf(this.mType);
                    objArr[Cea608CCParser.MODE_PAINT_ON] = Byte.valueOf(this.mData1);
                    objArr[Cea608CCParser.MODE_ROLL_UP] = Byte.valueOf(this.mData2);
                    return String.format("[%d]Invalid: %02x %02x", objArr);
                }
            }
            objArr = new Object[Cea608CCParser.MODE_POP_ON];
            objArr[Cea608CCParser.MODE_UNKNOWN] = Byte.valueOf(this.mType);
            objArr[Cea608CCParser.MODE_PAINT_ON] = Byte.valueOf(this.mData1);
            objArr[Cea608CCParser.MODE_ROLL_UP] = Byte.valueOf(this.mData2);
            return String.format("[%d]Null: %02x %02x", objArr);
        }
    }

    /* compiled from: ClosedCaptionRenderer */
    private static class CCLineBuilder {
        private final StringBuilder mDisplayChars;
        private final StyleCode[] mMidRowStyles;
        private final StyleCode[] mPACStyles;

        CCLineBuilder(String str) {
            this.mDisplayChars = new StringBuilder(str);
            this.mMidRowStyles = new StyleCode[this.mDisplayChars.length()];
            this.mPACStyles = new StyleCode[this.mDisplayChars.length()];
        }

        void setCharAt(int index, char ch) {
            this.mDisplayChars.setCharAt(index, ch);
            this.mMidRowStyles[index] = null;
        }

        void setMidRowAt(int index, StyleCode m) {
            this.mDisplayChars.setCharAt(index, ' ');
            this.mMidRowStyles[index] = m;
        }

        void setPACAt(int index, PAC pac) {
            this.mPACStyles[index] = pac;
        }

        char charAt(int index) {
            return this.mDisplayChars.charAt(index);
        }

        int length() {
            return this.mDisplayChars.length();
        }

        void applyStyleSpan(SpannableStringBuilder styledText, StyleCode s, int start, int end) {
            if (s.isItalics()) {
                styledText.setSpan(new StyleSpan(Cea608CCParser.MODE_ROLL_UP), start, end, Cea608CCParser.BS);
            }
            if (s.isUnderline()) {
                styledText.setSpan(new UnderlineSpan(), start, end, Cea608CCParser.BS);
            }
        }

        SpannableStringBuilder getStyledText(CaptionStyle captionStyle) {
            SpannableStringBuilder styledText = new SpannableStringBuilder(this.mDisplayChars);
            int start = Cea608CCParser.INVALID;
            int next = Cea608CCParser.MODE_UNKNOWN;
            int styleStart = Cea608CCParser.INVALID;
            StyleCode styleCode = null;
            while (next < this.mDisplayChars.length()) {
                StyleCode newStyle = null;
                if (this.mMidRowStyles[next] != null) {
                    newStyle = this.mMidRowStyles[next];
                } else if (this.mPACStyles[next] != null && (styleStart < 0 || start < 0)) {
                    newStyle = this.mPACStyles[next];
                }
                if (newStyle != null) {
                    styleCode = newStyle;
                    if (styleStart >= 0 && start >= 0) {
                        applyStyleSpan(styledText, newStyle, styleStart, next);
                    }
                    styleStart = next;
                }
                if (this.mDisplayChars.charAt(next) != Cea608CCParser.TS) {
                    if (start < 0) {
                        start = next;
                    }
                } else if (start >= 0) {
                    int expandedStart = this.mDisplayChars.charAt(start) == ' ' ? start : start + Cea608CCParser.INVALID;
                    int expandedEnd = this.mDisplayChars.charAt(next + Cea608CCParser.INVALID) == ' ' ? next : next + Cea608CCParser.MODE_PAINT_ON;
                    styledText.setSpan(new MutableBackgroundColorSpan(captionStyle.backgroundColor), expandedStart, expandedEnd, Cea608CCParser.BS);
                    if (styleStart >= 0) {
                        applyStyleSpan(styledText, styleCode, styleStart, expandedEnd);
                    }
                    start = Cea608CCParser.INVALID;
                }
                next += Cea608CCParser.MODE_PAINT_ON;
            }
            return styledText;
        }
    }

    /* compiled from: ClosedCaptionRenderer */
    private static class CCMemory {
        private final String mBlankLine;
        private int mCol;
        private final CCLineBuilder[] mLines;
        private int mRow;

        CCMemory() {
            this.mLines = new CCLineBuilder[17];
            char[] blank = new char[Cea608CCParser.AOF];
            Arrays.fill(blank, Cea608CCParser.TS);
            this.mBlankLine = new String(blank);
        }

        void erase() {
            for (int i = Cea608CCParser.MODE_UNKNOWN; i < this.mLines.length; i += Cea608CCParser.MODE_PAINT_ON) {
                this.mLines[i] = null;
            }
            this.mRow = Cea608CCParser.MAX_ROWS;
            this.mCol = Cea608CCParser.MODE_PAINT_ON;
        }

        void der() {
            if (this.mLines[this.mRow] != null) {
                for (int i = Cea608CCParser.MODE_UNKNOWN; i < this.mCol; i += Cea608CCParser.MODE_PAINT_ON) {
                    if (this.mLines[this.mRow].charAt(i) != Cea608CCParser.TS) {
                        int j = this.mCol;
                        while (j < this.mLines[this.mRow].length() && j < this.mLines.length) {
                            if (this.mLines[j] != null) {
                                this.mLines[j].setCharAt(j, Cea608CCParser.TS);
                            }
                            j += Cea608CCParser.MODE_PAINT_ON;
                        }
                        return;
                    }
                }
                this.mLines[this.mRow] = null;
            }
        }

        void tab(int tabs) {
            moveCursorByCol(tabs);
        }

        void bs() {
            moveCursorByCol(Cea608CCParser.INVALID);
            if (this.mLines[this.mRow] != null) {
                this.mLines[this.mRow].setCharAt(this.mCol, Cea608CCParser.TS);
                if (this.mCol == 31) {
                    this.mLines[this.mRow].setCharAt(Cea608CCParser.RCL, Cea608CCParser.TS);
                }
            }
        }

        void cr() {
            moveCursorTo(this.mRow + Cea608CCParser.MODE_PAINT_ON, Cea608CCParser.MODE_PAINT_ON);
        }

        void rollUp(int windowSize) {
            int i;
            for (i = Cea608CCParser.MODE_UNKNOWN; i <= this.mRow - windowSize; i += Cea608CCParser.MODE_PAINT_ON) {
                this.mLines[i] = null;
            }
            int startRow = (this.mRow - windowSize) + Cea608CCParser.MODE_PAINT_ON;
            if (startRow < Cea608CCParser.MODE_PAINT_ON) {
                startRow = Cea608CCParser.MODE_PAINT_ON;
            }
            for (i = startRow; i < this.mRow; i += Cea608CCParser.MODE_PAINT_ON) {
                this.mLines[i] = this.mLines[i + Cea608CCParser.MODE_PAINT_ON];
            }
            for (i = this.mRow; i < this.mLines.length; i += Cea608CCParser.MODE_PAINT_ON) {
                this.mLines[i] = null;
            }
            this.mCol = Cea608CCParser.MODE_PAINT_ON;
        }

        void writeText(String text) {
            for (int i = Cea608CCParser.MODE_UNKNOWN; i < text.length(); i += Cea608CCParser.MODE_PAINT_ON) {
                getLineBuffer(this.mRow).setCharAt(this.mCol, text.charAt(i));
                moveCursorByCol(Cea608CCParser.MODE_PAINT_ON);
            }
        }

        void writeMidRowCode(StyleCode m) {
            getLineBuffer(this.mRow).setMidRowAt(this.mCol, m);
            moveCursorByCol(Cea608CCParser.MODE_PAINT_ON);
        }

        void writePAC(PAC pac) {
            if (pac.isIndentPAC()) {
                moveCursorTo(pac.getRow(), pac.getCol());
            } else {
                moveCursorTo(pac.getRow(), Cea608CCParser.MODE_PAINT_ON);
            }
            getLineBuffer(this.mRow).setPACAt(this.mCol, pac);
        }

        SpannableStringBuilder[] getStyledText(CaptionStyle captionStyle) {
            ArrayList<SpannableStringBuilder> rows = new ArrayList(Cea608CCParser.MAX_ROWS);
            for (int i = Cea608CCParser.MODE_PAINT_ON; i <= Cea608CCParser.MAX_ROWS; i += Cea608CCParser.MODE_PAINT_ON) {
                Object styledText;
                if (this.mLines[i] != null) {
                    styledText = this.mLines[i].getStyledText(captionStyle);
                } else {
                    styledText = null;
                }
                rows.add(styledText);
            }
            return (SpannableStringBuilder[]) rows.toArray(new SpannableStringBuilder[Cea608CCParser.MAX_ROWS]);
        }

        private static int clamp(int x, int min, int max) {
            if (x < min) {
                return min;
            }
            return x > max ? max : x;
        }

        private void moveCursorTo(int row, int col) {
            this.mRow = clamp(row, Cea608CCParser.MODE_PAINT_ON, Cea608CCParser.MAX_ROWS);
            this.mCol = clamp(col, Cea608CCParser.MODE_PAINT_ON, Cea608CCParser.RCL);
        }

        private void moveCursorToRow(int row) {
            this.mRow = clamp(row, Cea608CCParser.MODE_PAINT_ON, Cea608CCParser.MAX_ROWS);
        }

        private void moveCursorByCol(int col) {
            this.mCol = clamp(this.mCol + col, Cea608CCParser.MODE_PAINT_ON, Cea608CCParser.RCL);
        }

        private void moveBaselineTo(int baseRow, int windowSize) {
            if (this.mRow != baseRow) {
                int i;
                int actualWindowSize = windowSize;
                if (baseRow < windowSize) {
                    actualWindowSize = baseRow;
                }
                if (this.mRow < actualWindowSize) {
                    actualWindowSize = this.mRow;
                }
                if (baseRow < this.mRow) {
                    for (i = actualWindowSize + Cea608CCParser.INVALID; i >= 0; i += Cea608CCParser.INVALID) {
                        this.mLines[baseRow - i] = this.mLines[this.mRow - i];
                    }
                } else {
                    for (i = Cea608CCParser.MODE_UNKNOWN; i < actualWindowSize; i += Cea608CCParser.MODE_PAINT_ON) {
                        this.mLines[baseRow - i] = this.mLines[this.mRow - i];
                    }
                }
                for (i = Cea608CCParser.MODE_UNKNOWN; i <= baseRow - windowSize; i += Cea608CCParser.MODE_PAINT_ON) {
                    this.mLines[i] = null;
                }
                for (i = baseRow + Cea608CCParser.MODE_PAINT_ON; i < this.mLines.length; i += Cea608CCParser.MODE_PAINT_ON) {
                    this.mLines[i] = null;
                }
            }
        }

        private CCLineBuilder getLineBuffer(int row) {
            if (this.mLines[row] == null) {
                this.mLines[row] = new CCLineBuilder(this.mBlankLine);
            }
            return this.mLines[row];
        }
    }

    /* compiled from: ClosedCaptionRenderer */
    interface DisplayListener {
        CaptionStyle getCaptionStyle();

        void onDisplayChanged(SpannableStringBuilder[] spannableStringBuilderArr);
    }

    /* compiled from: ClosedCaptionRenderer */
    public static class MutableBackgroundColorSpan extends CharacterStyle implements UpdateAppearance {
        private int mColor;

        public MutableBackgroundColorSpan(int color) {
            this.mColor = color;
        }

        public void setBackgroundColor(int color) {
            this.mColor = color;
        }

        public int getBackgroundColor() {
            return this.mColor;
        }

        public void updateDrawState(TextPaint ds) {
            ds.bgColor = this.mColor;
        }
    }

    /* compiled from: ClosedCaptionRenderer */
    private static class StyleCode {
        static final int COLOR_BLUE = 2;
        static final int COLOR_CYAN = 3;
        static final int COLOR_GREEN = 1;
        static final int COLOR_INVALID = 7;
        static final int COLOR_MAGENTA = 6;
        static final int COLOR_RED = 4;
        static final int COLOR_WHITE = 0;
        static final int COLOR_YELLOW = 5;
        static final int STYLE_ITALICS = 1;
        static final int STYLE_UNDERLINE = 2;
        static final String[] mColorMap = null;
        final int mColor;
        final int mStyle;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.Cea608CCParser.StyleCode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.Cea608CCParser.StyleCode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.Cea608CCParser.StyleCode.<clinit>():void");
        }

        static StyleCode fromByte(byte data2) {
            int style = COLOR_WHITE;
            int color = (data2 >> STYLE_ITALICS) & COLOR_INVALID;
            if ((data2 & STYLE_ITALICS) != 0) {
                style = STYLE_UNDERLINE;
            }
            if (color == COLOR_INVALID) {
                color = COLOR_WHITE;
                style |= STYLE_ITALICS;
            }
            return new StyleCode(style, color);
        }

        StyleCode(int style, int color) {
            this.mStyle = style;
            this.mColor = color;
        }

        boolean isItalics() {
            return (this.mStyle & STYLE_ITALICS) != 0 ? true : Cea608CCParser.DEBUG;
        }

        boolean isUnderline() {
            return (this.mStyle & STYLE_UNDERLINE) != 0 ? true : Cea608CCParser.DEBUG;
        }

        int getColor() {
            return this.mColor;
        }

        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("{");
            str.append(mColorMap[this.mColor]);
            if ((this.mStyle & STYLE_ITALICS) != 0) {
                str.append(", ITALICS");
            }
            if ((this.mStyle & STYLE_UNDERLINE) != 0) {
                str.append(", UNDERLINE");
            }
            str.append("}");
            return str.toString();
        }
    }

    /* compiled from: ClosedCaptionRenderer */
    private static class PAC extends StyleCode {
        final int mCol;
        final int mRow;

        static PAC fromBytes(byte data1, byte data2) {
            int row = new int[]{11, Cea608CCParser.MODE_PAINT_ON, Cea608CCParser.MODE_POP_ON, 12, 14, 5, 7, 9}[data1 & 7] + ((data2 & Cea608CCParser.RCL) >> 5);
            int style = Cea608CCParser.MODE_UNKNOWN;
            if ((data2 & Cea608CCParser.MODE_PAINT_ON) != 0) {
                style = Cea608CCParser.MODE_ROLL_UP;
            }
            if ((data2 & 16) != 0) {
                return new PAC(row, ((data2 >> Cea608CCParser.MODE_PAINT_ON) & 7) * Cea608CCParser.MODE_TEXT, style, Cea608CCParser.MODE_UNKNOWN);
            }
            int color = (data2 >> Cea608CCParser.MODE_PAINT_ON) & 7;
            if (color == 7) {
                color = Cea608CCParser.MODE_UNKNOWN;
                style |= Cea608CCParser.MODE_PAINT_ON;
            }
            return new PAC(row, Cea608CCParser.INVALID, style, color);
        }

        PAC(int row, int col, int style, int color) {
            super(style, color);
            this.mRow = row;
            this.mCol = col;
        }

        boolean isIndentPAC() {
            return this.mCol >= 0 ? true : Cea608CCParser.DEBUG;
        }

        int getRow() {
            return this.mRow;
        }

        int getCol() {
            return this.mCol;
        }

        public String toString() {
            Object[] objArr = new Object[Cea608CCParser.MODE_POP_ON];
            objArr[Cea608CCParser.MODE_UNKNOWN] = Integer.valueOf(this.mRow);
            objArr[Cea608CCParser.MODE_PAINT_ON] = Integer.valueOf(this.mCol);
            objArr[Cea608CCParser.MODE_ROLL_UP] = super.toString();
            return String.format("{%d, %d}, %s", objArr);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.Cea608CCParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.Cea608CCParser.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.Cea608CCParser.<clinit>():void");
    }

    Cea608CCParser(DisplayListener listener) {
        this.mMode = MODE_PAINT_ON;
        this.mRollUpSize = MODE_TEXT;
        this.mPrevCtrlCode = INVALID;
        this.mDisplay = new CCMemory();
        this.mNonDisplay = new CCMemory();
        this.mTextMem = new CCMemory();
        this.mListener = listener;
    }

    public void parse(byte[] data) {
        CCData[] ccData = CCData.fromByteArray(data);
        int i = MODE_UNKNOWN;
        while (i < ccData.length) {
            if (DEBUG) {
                Log.d(TAG, ccData[i].toString());
            }
            if (!(handleCtrlCode(ccData[i]) || handleTabOffsets(ccData[i]) || handlePACCode(ccData[i]) || handleMidRowCode(ccData[i]))) {
                handleDisplayableChars(ccData[i]);
            }
            i += MODE_PAINT_ON;
        }
    }

    private CCMemory getMemory() {
        switch (this.mMode) {
            case MODE_PAINT_ON /*1*/:
            case MODE_ROLL_UP /*2*/:
                return this.mDisplay;
            case MODE_POP_ON /*3*/:
                return this.mNonDisplay;
            case MODE_TEXT /*4*/:
                return this.mTextMem;
            default:
                Log.w(TAG, "unrecoginized mode: " + this.mMode);
                return this.mDisplay;
        }
    }

    private boolean handleDisplayableChars(CCData ccData) {
        if (!ccData.isDisplayableChar()) {
            return DEBUG;
        }
        if (ccData.isExtendedChar()) {
            getMemory().bs();
        }
        getMemory().writeText(ccData.getDisplayText());
        if (this.mMode == MODE_PAINT_ON || this.mMode == MODE_ROLL_UP) {
            updateDisplay();
        }
        return true;
    }

    private boolean handleMidRowCode(CCData ccData) {
        StyleCode m = ccData.getMidRow();
        if (m == null) {
            return DEBUG;
        }
        getMemory().writeMidRowCode(m);
        return true;
    }

    private boolean handlePACCode(CCData ccData) {
        PAC pac = ccData.getPAC();
        if (pac == null) {
            return DEBUG;
        }
        if (this.mMode == MODE_ROLL_UP) {
            getMemory().moveBaselineTo(pac.getRow(), this.mRollUpSize);
        }
        getMemory().writePAC(pac);
        return true;
    }

    private boolean handleTabOffsets(CCData ccData) {
        int tabs = ccData.getTabOffset();
        if (tabs <= 0) {
            return DEBUG;
        }
        getMemory().tab(tabs);
        return true;
    }

    private boolean handleCtrlCode(CCData ccData) {
        int ctrlCode = ccData.getCtrlCode();
        if (this.mPrevCtrlCode == INVALID || this.mPrevCtrlCode != ctrlCode) {
            switch (ctrlCode) {
                case RCL /*32*/:
                    this.mMode = MODE_POP_ON;
                    break;
                case BS /*33*/:
                    getMemory().bs();
                    break;
                case DER /*36*/:
                    getMemory().der();
                    break;
                case RU2 /*37*/:
                case RU3 /*38*/:
                case RU4 /*39*/:
                    this.mRollUpSize = ctrlCode - 35;
                    if (this.mMode != MODE_ROLL_UP) {
                        this.mDisplay.erase();
                        this.mNonDisplay.erase();
                    }
                    this.mMode = MODE_ROLL_UP;
                    break;
                case FON /*40*/:
                    Log.i(TAG, "Flash On");
                    break;
                case RDC /*41*/:
                    this.mMode = MODE_PAINT_ON;
                    break;
                case TR /*42*/:
                    this.mMode = MODE_TEXT;
                    this.mTextMem.erase();
                    break;
                case RTD /*43*/:
                    this.mMode = MODE_TEXT;
                    break;
                case EDM /*44*/:
                    this.mDisplay.erase();
                    updateDisplay();
                    break;
                case CR /*45*/:
                    if (this.mMode == MODE_ROLL_UP) {
                        getMemory().rollUp(this.mRollUpSize);
                    } else {
                        getMemory().cr();
                    }
                    if (this.mMode == MODE_ROLL_UP) {
                        updateDisplay();
                        break;
                    }
                    break;
                case ENM /*46*/:
                    this.mNonDisplay.erase();
                    break;
                case EOC /*47*/:
                    swapMemory();
                    this.mMode = MODE_POP_ON;
                    updateDisplay();
                    break;
                default:
                    this.mPrevCtrlCode = INVALID;
                    return DEBUG;
            }
            this.mPrevCtrlCode = ctrlCode;
            return true;
        }
        this.mPrevCtrlCode = INVALID;
        return true;
    }

    private void updateDisplay() {
        if (this.mListener != null) {
            this.mListener.onDisplayChanged(this.mDisplay.getStyledText(this.mListener.getCaptionStyle()));
        }
    }

    private void swapMemory() {
        CCMemory temp = this.mDisplay;
        this.mDisplay = this.mNonDisplay;
        this.mNonDisplay = temp;
    }
}

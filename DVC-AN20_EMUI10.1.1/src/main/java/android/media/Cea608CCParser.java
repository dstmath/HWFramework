package android.media;

import android.bluetooth.BluetoothHidDevice;
import android.net.wifi.WifiScanner;
import android.telecom.Logging.Session;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.view.accessibility.CaptioningManager;
import java.util.ArrayList;
import java.util.Arrays;

/* compiled from: ClosedCaptionRenderer */
class Cea608CCParser {
    private static final int AOF = 34;
    private static final int AON = 35;
    private static final int BS = 33;
    private static final int CR = 45;
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
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
    private static final char TS = 160;
    private CCMemory mDisplay = new CCMemory();
    private final DisplayListener mListener;
    private int mMode = 1;
    private CCMemory mNonDisplay = new CCMemory();
    private int mPrevCtrlCode = -1;
    private int mRollUpSize = 4;
    private CCMemory mTextMem = new CCMemory();

    /* access modifiers changed from: package-private */
    /* compiled from: ClosedCaptionRenderer */
    public interface DisplayListener {
        CaptioningManager.CaptionStyle getCaptionStyle();

        void onDisplayChanged(SpannableStringBuilder[] spannableStringBuilderArr);
    }

    Cea608CCParser(DisplayListener listener) {
        this.mListener = listener;
    }

    public void parse(byte[] data) {
        CCData[] ccData = CCData.fromByteArray(data);
        for (int i = 0; i < ccData.length; i++) {
            if (DEBUG) {
                Log.d(TAG, ccData[i].toString());
            }
            if (!handleCtrlCode(ccData[i]) && !handleTabOffsets(ccData[i]) && !handlePACCode(ccData[i]) && !handleMidRowCode(ccData[i])) {
                handleDisplayableChars(ccData[i]);
            }
        }
    }

    private CCMemory getMemory() {
        int i = this.mMode;
        if (i == 1 || i == 2) {
            return this.mDisplay;
        }
        if (i == 3) {
            return this.mNonDisplay;
        }
        if (i == 4) {
            return this.mTextMem;
        }
        Log.w(TAG, "unrecoginized mode: " + this.mMode);
        return this.mDisplay;
    }

    private boolean handleDisplayableChars(CCData ccData) {
        if (!ccData.isDisplayableChar()) {
            return false;
        }
        if (ccData.isExtendedChar()) {
            getMemory().bs();
        }
        getMemory().writeText(ccData.getDisplayText());
        int i = this.mMode;
        if (i == 1 || i == 2) {
            updateDisplay();
        }
        return true;
    }

    private boolean handleMidRowCode(CCData ccData) {
        StyleCode m = ccData.getMidRow();
        if (m == null) {
            return false;
        }
        getMemory().writeMidRowCode(m);
        return true;
    }

    private boolean handlePACCode(CCData ccData) {
        PAC pac = ccData.getPAC();
        if (pac == null) {
            return false;
        }
        if (this.mMode == 2) {
            getMemory().moveBaselineTo(pac.getRow(), this.mRollUpSize);
        }
        getMemory().writePAC(pac);
        return true;
    }

    private boolean handleTabOffsets(CCData ccData) {
        int tabs = ccData.getTabOffset();
        if (tabs <= 0) {
            return false;
        }
        getMemory().tab(tabs);
        return true;
    }

    private boolean handleCtrlCode(CCData ccData) {
        int ctrlCode = ccData.getCtrlCode();
        int i = this.mPrevCtrlCode;
        if (i == -1 || i != ctrlCode) {
            switch (ctrlCode) {
                case 32:
                    this.mMode = 3;
                    break;
                case 33:
                    getMemory().bs();
                    break;
                case 34:
                case 35:
                default:
                    this.mPrevCtrlCode = -1;
                    return false;
                case 36:
                    getMemory().der();
                    break;
                case 37:
                case 38:
                case 39:
                    this.mRollUpSize = ctrlCode - 35;
                    if (this.mMode != 2) {
                        this.mDisplay.erase();
                        this.mNonDisplay.erase();
                    }
                    this.mMode = 2;
                    break;
                case 40:
                    Log.i(TAG, "Flash On");
                    break;
                case 41:
                    this.mMode = 1;
                    break;
                case 42:
                    this.mMode = 4;
                    this.mTextMem.erase();
                    break;
                case 43:
                    this.mMode = 4;
                    break;
                case 44:
                    this.mDisplay.erase();
                    updateDisplay();
                    break;
                case 45:
                    if (this.mMode == 2) {
                        getMemory().rollUp(this.mRollUpSize);
                    } else {
                        getMemory().cr();
                    }
                    if (this.mMode == 2) {
                        updateDisplay();
                        break;
                    }
                    break;
                case 46:
                    this.mNonDisplay.erase();
                    break;
                case 47:
                    swapMemory();
                    this.mMode = 3;
                    updateDisplay();
                    break;
            }
            this.mPrevCtrlCode = ctrlCode;
            return true;
        }
        this.mPrevCtrlCode = -1;
        return true;
    }

    private void updateDisplay() {
        DisplayListener displayListener = this.mListener;
        if (displayListener != null) {
            this.mListener.onDisplayChanged(this.mDisplay.getStyledText(displayListener.getCaptionStyle()));
        }
    }

    private void swapMemory() {
        CCMemory temp = this.mDisplay;
        this.mDisplay = this.mNonDisplay;
        this.mNonDisplay = temp;
    }

    /* access modifiers changed from: private */
    /* compiled from: ClosedCaptionRenderer */
    public static class StyleCode {
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
        static final String[] mColorMap = {"WHITE", "GREEN", "BLUE", "CYAN", "RED", "YELLOW", "MAGENTA", "INVALID"};
        final int mColor;
        final int mStyle;

        static StyleCode fromByte(byte data2) {
            int style = 0;
            int color = (data2 >> 1) & 7;
            if ((data2 & 1) != 0) {
                style = 0 | 2;
            }
            if (color == 7) {
                color = 0;
                style |= 1;
            }
            return new StyleCode(style, color);
        }

        StyleCode(int style, int color) {
            this.mStyle = style;
            this.mColor = color;
        }

        /* access modifiers changed from: package-private */
        public boolean isItalics() {
            return (this.mStyle & 1) != 0;
        }

        /* access modifiers changed from: package-private */
        public boolean isUnderline() {
            return (this.mStyle & 2) != 0;
        }

        /* access modifiers changed from: package-private */
        public int getColor() {
            return this.mColor;
        }

        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("{");
            str.append(mColorMap[this.mColor]);
            if ((this.mStyle & 1) != 0) {
                str.append(", ITALICS");
            }
            if ((this.mStyle & 2) != 0) {
                str.append(", UNDERLINE");
            }
            str.append("}");
            return str.toString();
        }
    }

    /* access modifiers changed from: private */
    /* compiled from: ClosedCaptionRenderer */
    public static class PAC extends StyleCode {
        final int mCol;
        final int mRow;

        static PAC fromBytes(byte data1, byte data2) {
            int row = new int[]{11, 1, 3, 12, 14, 5, 7, 9}[data1 & 7] + ((data2 & 32) >> 5);
            int style = 0;
            if ((data2 & 1) != 0) {
                style = 0 | 2;
            }
            if ((data2 & WifiScanner.PnoSettings.PnoNetwork.FLAG_SAME_NETWORK) != 0) {
                return new PAC(row, ((data2 >> 1) & 7) * 4, style, 0);
            }
            int color = (data2 >> 1) & 7;
            if (color == 7) {
                color = 0;
                style |= 1;
            }
            return new PAC(row, -1, style, color);
        }

        PAC(int row, int col, int style, int color) {
            super(style, color);
            this.mRow = row;
            this.mCol = col;
        }

        /* access modifiers changed from: package-private */
        public boolean isIndentPAC() {
            return this.mCol >= 0;
        }

        /* access modifiers changed from: package-private */
        public int getRow() {
            return this.mRow;
        }

        /* access modifiers changed from: package-private */
        public int getCol() {
            return this.mCol;
        }

        @Override // android.media.Cea608CCParser.StyleCode
        public String toString() {
            return String.format("{%d, %d}, %s", Integer.valueOf(this.mRow), Integer.valueOf(this.mCol), super.toString());
        }
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

        @Override // android.text.style.CharacterStyle
        public void updateDrawState(TextPaint ds) {
            ds.bgColor = this.mColor;
        }
    }

    /* access modifiers changed from: private */
    /* compiled from: ClosedCaptionRenderer */
    public static class CCLineBuilder {
        private final StringBuilder mDisplayChars;
        private final StyleCode[] mMidRowStyles = new StyleCode[this.mDisplayChars.length()];
        private final StyleCode[] mPACStyles = new StyleCode[this.mDisplayChars.length()];

        CCLineBuilder(String str) {
            this.mDisplayChars = new StringBuilder(str);
        }

        /* access modifiers changed from: package-private */
        public void setCharAt(int index, char ch) {
            this.mDisplayChars.setCharAt(index, ch);
            this.mMidRowStyles[index] = null;
        }

        /* access modifiers changed from: package-private */
        public void setMidRowAt(int index, StyleCode m) {
            this.mDisplayChars.setCharAt(index, ' ');
            this.mMidRowStyles[index] = m;
        }

        /* access modifiers changed from: package-private */
        public void setPACAt(int index, PAC pac) {
            this.mPACStyles[index] = pac;
        }

        /* access modifiers changed from: package-private */
        public char charAt(int index) {
            return this.mDisplayChars.charAt(index);
        }

        /* access modifiers changed from: package-private */
        public int length() {
            return this.mDisplayChars.length();
        }

        /* access modifiers changed from: package-private */
        public void applyStyleSpan(SpannableStringBuilder styledText, StyleCode s, int start, int end) {
            if (s.isItalics()) {
                styledText.setSpan(new StyleSpan(2), start, end, 33);
            }
            if (s.isUnderline()) {
                styledText.setSpan(new UnderlineSpan(), start, end, 33);
            }
        }

        /* access modifiers changed from: package-private */
        public SpannableStringBuilder getStyledText(CaptioningManager.CaptionStyle captionStyle) {
            SpannableStringBuilder styledText = new SpannableStringBuilder(this.mDisplayChars);
            int start = -1;
            int styleStart = -1;
            StyleCode curStyle = null;
            for (int next = 0; next < this.mDisplayChars.length(); next++) {
                StyleCode newStyle = null;
                StyleCode[] styleCodeArr = this.mMidRowStyles;
                if (styleCodeArr[next] != null) {
                    newStyle = styleCodeArr[next];
                } else if (this.mPACStyles[next] != null && (styleStart < 0 || start < 0)) {
                    newStyle = this.mPACStyles[next];
                }
                if (newStyle != null) {
                    curStyle = newStyle;
                    if (styleStart >= 0 && start >= 0) {
                        applyStyleSpan(styledText, newStyle, styleStart, next);
                    }
                    styleStart = next;
                }
                if (this.mDisplayChars.charAt(next) != 160) {
                    if (start < 0) {
                        start = next;
                    }
                } else if (start >= 0) {
                    int expandedStart = this.mDisplayChars.charAt(start) == ' ' ? start : start - 1;
                    int expandedEnd = this.mDisplayChars.charAt(next + -1) == ' ' ? next : next + 1;
                    styledText.setSpan(new MutableBackgroundColorSpan(captionStyle.backgroundColor), expandedStart, expandedEnd, 33);
                    if (styleStart >= 0) {
                        applyStyleSpan(styledText, curStyle, styleStart, expandedEnd);
                    }
                    start = -1;
                }
            }
            return styledText;
        }
    }

    /* access modifiers changed from: private */
    /* compiled from: ClosedCaptionRenderer */
    public static class CCMemory {
        private final String mBlankLine;
        private int mCol;
        private final CCLineBuilder[] mLines = new CCLineBuilder[17];
        private int mRow;

        CCMemory() {
            char[] blank = new char[34];
            Arrays.fill(blank, (char) Cea608CCParser.TS);
            this.mBlankLine = new String(blank);
        }

        /* access modifiers changed from: package-private */
        public void erase() {
            int i = 0;
            while (true) {
                CCLineBuilder[] cCLineBuilderArr = this.mLines;
                if (i < cCLineBuilderArr.length) {
                    cCLineBuilderArr[i] = null;
                    i++;
                } else {
                    this.mRow = 15;
                    this.mCol = 1;
                    return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void der() {
            if (this.mLines[this.mRow] != null) {
                for (int i = 0; i < this.mCol; i++) {
                    if (this.mLines[this.mRow].charAt(i) != 160) {
                        for (int j = this.mCol; j < this.mLines[this.mRow].length(); j++) {
                            this.mLines[j].setCharAt(j, Cea608CCParser.TS);
                        }
                        return;
                    }
                }
                this.mLines[this.mRow] = null;
            }
        }

        /* access modifiers changed from: package-private */
        public void tab(int tabs) {
            moveCursorByCol(tabs);
        }

        /* access modifiers changed from: package-private */
        public void bs() {
            moveCursorByCol(-1);
            CCLineBuilder[] cCLineBuilderArr = this.mLines;
            int i = this.mRow;
            if (cCLineBuilderArr[i] != null) {
                cCLineBuilderArr[i].setCharAt(this.mCol, Cea608CCParser.TS);
                if (this.mCol == 31) {
                    this.mLines[this.mRow].setCharAt(32, Cea608CCParser.TS);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void cr() {
            moveCursorTo(this.mRow + 1, 1);
        }

        /* access modifiers changed from: package-private */
        public void rollUp(int windowSize) {
            int i;
            int i2 = 0;
            while (true) {
                i = this.mRow;
                if (i2 > i - windowSize) {
                    break;
                }
                this.mLines[i2] = null;
                i2++;
            }
            int startRow = (i - windowSize) + 1;
            if (startRow < 1) {
                startRow = 1;
            }
            for (int i3 = startRow; i3 < this.mRow; i3++) {
                CCLineBuilder[] cCLineBuilderArr = this.mLines;
                cCLineBuilderArr[i3] = cCLineBuilderArr[i3 + 1];
            }
            int i4 = this.mRow;
            while (true) {
                CCLineBuilder[] cCLineBuilderArr2 = this.mLines;
                if (i4 < cCLineBuilderArr2.length) {
                    cCLineBuilderArr2[i4] = null;
                    i4++;
                } else {
                    this.mCol = 1;
                    return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void writeText(String text) {
            for (int i = 0; i < text.length(); i++) {
                getLineBuffer(this.mRow).setCharAt(this.mCol, text.charAt(i));
                moveCursorByCol(1);
            }
        }

        /* access modifiers changed from: package-private */
        public void writeMidRowCode(StyleCode m) {
            getLineBuffer(this.mRow).setMidRowAt(this.mCol, m);
            moveCursorByCol(1);
        }

        /* access modifiers changed from: package-private */
        public void writePAC(PAC pac) {
            if (pac.isIndentPAC()) {
                moveCursorTo(pac.getRow(), pac.getCol());
            } else {
                moveCursorTo(pac.getRow(), 1);
            }
            getLineBuffer(this.mRow).setPACAt(this.mCol, pac);
        }

        /* access modifiers changed from: package-private */
        public SpannableStringBuilder[] getStyledText(CaptioningManager.CaptionStyle captionStyle) {
            ArrayList<SpannableStringBuilder> rows = new ArrayList<>(15);
            for (int i = 1; i <= 15; i++) {
                CCLineBuilder[] cCLineBuilderArr = this.mLines;
                rows.add(cCLineBuilderArr[i] != null ? cCLineBuilderArr[i].getStyledText(captionStyle) : null);
            }
            return (SpannableStringBuilder[]) rows.toArray(new SpannableStringBuilder[15]);
        }

        private static int clamp(int x, int min, int max) {
            if (x < min) {
                return min;
            }
            return x > max ? max : x;
        }

        private void moveCursorTo(int row, int col) {
            this.mRow = clamp(row, 1, 15);
            this.mCol = clamp(col, 1, 32);
        }

        private void moveCursorToRow(int row) {
            this.mRow = clamp(row, 1, 15);
        }

        private void moveCursorByCol(int col) {
            this.mCol = clamp(this.mCol + col, 1, 32);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void moveBaselineTo(int baseRow, int windowSize) {
            if (this.mRow != baseRow) {
                int actualWindowSize = windowSize;
                if (baseRow < actualWindowSize) {
                    actualWindowSize = baseRow;
                }
                if (this.mRow < actualWindowSize) {
                    actualWindowSize = this.mRow;
                }
                if (baseRow < this.mRow) {
                    for (int i = actualWindowSize - 1; i >= 0; i--) {
                        CCLineBuilder[] cCLineBuilderArr = this.mLines;
                        cCLineBuilderArr[baseRow - i] = cCLineBuilderArr[this.mRow - i];
                    }
                } else {
                    for (int i2 = 0; i2 < actualWindowSize; i2++) {
                        CCLineBuilder[] cCLineBuilderArr2 = this.mLines;
                        cCLineBuilderArr2[baseRow - i2] = cCLineBuilderArr2[this.mRow - i2];
                    }
                }
                for (int i3 = 0; i3 <= baseRow - windowSize; i3++) {
                    this.mLines[i3] = null;
                }
                int i4 = baseRow + 1;
                while (true) {
                    CCLineBuilder[] cCLineBuilderArr3 = this.mLines;
                    if (i4 < cCLineBuilderArr3.length) {
                        cCLineBuilderArr3[i4] = null;
                        i4++;
                    } else {
                        return;
                    }
                }
            }
        }

        private CCLineBuilder getLineBuffer(int row) {
            CCLineBuilder[] cCLineBuilderArr = this.mLines;
            if (cCLineBuilderArr[row] == null) {
                cCLineBuilderArr[row] = new CCLineBuilder(this.mBlankLine);
            }
            return this.mLines[row];
        }
    }

    /* access modifiers changed from: private */
    /* compiled from: ClosedCaptionRenderer */
    public static class CCData {
        private static final String[] mCtrlCodeMap = {"RCL", "BS", "AOF", "AON", "DER", "RU2", "RU3", "RU4", "FON", "RDC", "TR", "RTD", "EDM", "CR", "ENM", "EOC"};
        private static final String[] mProtugueseCharMap = {"Ã", "ã", "Í", "Ì", "ì", "Ò", "ò", "Õ", "õ", "{", "}", "\\", "^", Session.SESSION_SEPARATION_CHAR_CHILD, "|", "~", "Ä", "ä", "Ö", "ö", "ß", "¥", "¤", "│", "Å", "å", "Ø", "ø", "┌", "┐", "└", "┘"};
        private static final String[] mSpanishCharMap = {"Á", "É", "Ó", "Ú", "Ü", "ü", "‘", "¡", "*", "'", "—", "©", "℠", "•", "“", "”", "À", "Â", "Ç", "È", "Ê", "Ë", "ë", "Î", "Ï", "ï", "Ô", "Ù", "ù", "Û", "«", "»"};
        private static final String[] mSpecialCharMap = {"®", "°", "½", "¿", "™", "¢", "£", "♪", "à", " ", "è", "â", "ê", "î", "ô", "û"};
        private final byte mData1;
        private final byte mData2;
        private final byte mType;

        static CCData[] fromByteArray(byte[] data) {
            CCData[] ccData = new CCData[(data.length / 3)];
            for (int i = 0; i < ccData.length; i++) {
                ccData[i] = new CCData(data[i * 3], data[(i * 3) + 1], data[(i * 3) + 2]);
            }
            return ccData;
        }

        CCData(byte type, byte data1, byte data2) {
            this.mType = type;
            this.mData1 = data1;
            this.mData2 = data2;
        }

        /* access modifiers changed from: package-private */
        public int getCtrlCode() {
            byte b;
            byte b2 = this.mData1;
            if ((b2 == 20 || b2 == 28) && (b = this.mData2) >= 32 && b <= 47) {
                return b;
            }
            return -1;
        }

        /* access modifiers changed from: package-private */
        public StyleCode getMidRow() {
            byte b;
            byte b2 = this.mData1;
            if ((b2 == 17 || b2 == 25) && (b = this.mData2) >= 32 && b <= 47) {
                return StyleCode.fromByte(b);
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public PAC getPAC() {
            byte b = this.mData1;
            if ((b & 112) != 16) {
                return null;
            }
            byte b2 = this.mData2;
            if ((b2 & BluetoothHidDevice.SUBCLASS1_KEYBOARD) != 64) {
                return null;
            }
            if ((b & 7) != 0 || (b2 & 32) == 0) {
                return PAC.fromBytes(this.mData1, this.mData2);
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public int getTabOffset() {
            byte b;
            byte b2 = this.mData1;
            if ((b2 == 23 || b2 == 31) && (b = this.mData2) >= 33 && b <= 35) {
                return b & 3;
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        public boolean isDisplayableChar() {
            return isBasicChar() || isSpecialChar() || isExtendedChar();
        }

        /* access modifiers changed from: package-private */
        public String getDisplayText() {
            String str = getBasicChars();
            if (str != null) {
                return str;
            }
            String str2 = getSpecialChar();
            if (str2 == null) {
                return getExtendedChar();
            }
            return str2;
        }

        private String ctrlCodeToString(int ctrlCode) {
            return mCtrlCodeMap[ctrlCode - 32];
        }

        private boolean isBasicChar() {
            byte b = this.mData1;
            return b >= 32 && b <= Byte.MAX_VALUE;
        }

        private boolean isSpecialChar() {
            byte b;
            byte b2 = this.mData1;
            return (b2 == 17 || b2 == 25) && (b = this.mData2) >= 48 && b <= 63;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isExtendedChar() {
            byte b;
            byte b2 = this.mData1;
            return (b2 == 18 || b2 == 26 || b2 == 19 || b2 == 27) && (b = this.mData2) >= 32 && b <= 63;
        }

        private char getBasicChar(byte data) {
            if (data == 42) {
                return 225;
            }
            if (data == 92) {
                return 233;
            }
            switch (data) {
                case 94:
                    return 237;
                case 95:
                    return 243;
                case 96:
                    return 250;
                default:
                    switch (data) {
                        case 123:
                            return 231;
                        case 124:
                            return 247;
                        case 125:
                            return 209;
                        case 126:
                            return 241;
                        case Byte.MAX_VALUE:
                            return 9608;
                        default:
                            return (char) data;
                    }
            }
        }

        private String getBasicChars() {
            byte b = this.mData1;
            if (b < 32 || b > Byte.MAX_VALUE) {
                return null;
            }
            StringBuilder builder = new StringBuilder(2);
            builder.append(getBasicChar(this.mData1));
            byte b2 = this.mData2;
            if (b2 >= 32 && b2 <= Byte.MAX_VALUE) {
                builder.append(getBasicChar(b2));
            }
            return builder.toString();
        }

        private String getSpecialChar() {
            byte b;
            byte b2 = this.mData1;
            if ((b2 == 17 || b2 == 25) && (b = this.mData2) >= 48 && b <= 63) {
                return mSpecialCharMap[b - 48];
            }
            return null;
        }

        private String getExtendedChar() {
            byte b;
            byte b2;
            byte b3 = this.mData1;
            if ((b3 == 18 || b3 == 26) && (b2 = this.mData2) >= 32 && b2 <= 63) {
                return mSpanishCharMap[b2 - 32];
            }
            byte b4 = this.mData1;
            if ((b4 == 19 || b4 == 27) && (b = this.mData2) >= 32 && b <= 63) {
                return mProtugueseCharMap[b - 32];
            }
            return null;
        }

        public String toString() {
            if (this.mData1 >= 16 || this.mData2 >= 16) {
                int ctrlCode = getCtrlCode();
                if (ctrlCode != -1) {
                    return String.format("[%d]%s", Byte.valueOf(this.mType), ctrlCodeToString(ctrlCode));
                }
                int tabOffset = getTabOffset();
                if (tabOffset > 0) {
                    return String.format("[%d]Tab%d", Byte.valueOf(this.mType), Integer.valueOf(tabOffset));
                }
                PAC pac = getPAC();
                if (pac != null) {
                    return String.format("[%d]PAC: %s", Byte.valueOf(this.mType), pac.toString());
                }
                StyleCode m = getMidRow();
                if (m != null) {
                    return String.format("[%d]Mid-row: %s", Byte.valueOf(this.mType), m.toString());
                } else if (isDisplayableChar()) {
                    return String.format("[%d]Displayable: %s (%02x %02x)", Byte.valueOf(this.mType), getDisplayText(), Byte.valueOf(this.mData1), Byte.valueOf(this.mData2));
                } else {
                    return String.format("[%d]Invalid: %02x %02x", Byte.valueOf(this.mType), Byte.valueOf(this.mData1), Byte.valueOf(this.mData2));
                }
            } else {
                return String.format("[%d]Null: %02x %02x", Byte.valueOf(this.mType), Byte.valueOf(this.mData1), Byte.valueOf(this.mData2));
            }
        }
    }
}

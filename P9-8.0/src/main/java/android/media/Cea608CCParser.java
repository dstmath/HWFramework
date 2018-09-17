package android.media;

import android.net.wifi.WifiScanner.PnoSettings.PnoNetwork;
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
    private static final char TS = ' ';
    private CCMemory mDisplay = new CCMemory();
    private final DisplayListener mListener;
    private int mMode = 1;
    private CCMemory mNonDisplay = new CCMemory();
    private int mPrevCtrlCode = -1;
    private int mRollUpSize = 4;
    private CCMemory mTextMem = new CCMemory();

    /* compiled from: ClosedCaptionRenderer */
    private static class CCData {
        private static final String[] mCtrlCodeMap = new String[]{"RCL", "BS", "AOF", "AON", "DER", "RU2", "RU3", "RU4", "FON", "RDC", "TR", "RTD", "EDM", "CR", "ENM", "EOC"};
        private static final String[] mProtugueseCharMap = new String[]{"Ã", "ã", "Í", "Ì", "ì", "Ò", "ò", "Õ", "õ", "{", "}", "\\", "^", "_", "|", "~", "Ä", "ä", "Ö", "ö", "ß", "¥", "¤", "│", "Å", "å", "Ø", "ø", "┌", "┐", "└", "┘"};
        private static final String[] mSpanishCharMap = new String[]{"Á", "É", "Ó", "Ú", "Ü", "ü", "‘", "¡", "*", "'", "—", "©", "℠", "•", "“", "”", "À", "Â", "Ç", "È", "Ê", "Ë", "ë", "Î", "Ï", "ï", "Ô", "Ù", "ù", "Û", "«", "»"};
        private static final String[] mSpecialCharMap = new String[]{"®", "°", "½", "¿", "™", "¢", "£", "♪", "à", " ", "è", "â", "ê", "î", "ô", "û"};
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

        int getCtrlCode() {
            if ((this.mData1 == (byte) 20 || this.mData1 == (byte) 28) && this.mData2 >= (byte) 32 && this.mData2 <= (byte) 47) {
                return this.mData2;
            }
            return -1;
        }

        StyleCode getMidRow() {
            if ((this.mData1 == (byte) 17 || this.mData1 == (byte) 25) && this.mData2 >= (byte) 32 && this.mData2 <= (byte) 47) {
                return StyleCode.fromByte(this.mData2);
            }
            return null;
        }

        PAC getPAC() {
            if ((this.mData1 & 112) == 16 && (this.mData2 & 64) == 64 && ((this.mData1 & 7) != 0 || (this.mData2 & 32) == 0)) {
                return PAC.fromBytes(this.mData1, this.mData2);
            }
            return null;
        }

        int getTabOffset() {
            if ((this.mData1 == (byte) 23 || this.mData1 == (byte) 31) && this.mData2 >= (byte) 33 && this.mData2 <= (byte) 35) {
                return this.mData2 & 3;
            }
            return 0;
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
            return this.mData1 >= (byte) 32 && this.mData1 <= Byte.MAX_VALUE;
        }

        private boolean isSpecialChar() {
            if ((this.mData1 == (byte) 17 || this.mData1 == (byte) 25) && this.mData2 >= (byte) 48 && this.mData2 <= (byte) 63) {
                return true;
            }
            return false;
        }

        private boolean isExtendedChar() {
            if ((this.mData1 == (byte) 18 || this.mData1 == (byte) 26 || this.mData1 == (byte) 19 || this.mData1 == (byte) 27) && this.mData2 >= (byte) 32 && this.mData2 <= (byte) 63) {
                return true;
            }
            return false;
        }

        private char getBasicChar(byte data) {
            switch (data) {
                case (byte) 42:
                    return 225;
                case (byte) 92:
                    return 233;
                case (byte) 94:
                    return 237;
                case (byte) 95:
                    return 243;
                case (byte) 96:
                    return 250;
                case (byte) 123:
                    return 231;
                case (byte) 124:
                    return 247;
                case (byte) 125:
                    return 209;
                case (byte) 126:
                    return 241;
                case Byte.MAX_VALUE:
                    return 9608;
                default:
                    return (char) data;
            }
        }

        private String getBasicChars() {
            if (this.mData1 < (byte) 32 || this.mData1 > Byte.MAX_VALUE) {
                return null;
            }
            StringBuilder builder = new StringBuilder(2);
            builder.append(getBasicChar(this.mData1));
            if (this.mData2 >= (byte) 32 && this.mData2 <= Byte.MAX_VALUE) {
                builder.append(getBasicChar(this.mData2));
            }
            return builder.toString();
        }

        private String getSpecialChar() {
            if ((this.mData1 == (byte) 17 || this.mData1 == (byte) 25) && this.mData2 >= (byte) 48 && this.mData2 <= (byte) 63) {
                return mSpecialCharMap[this.mData2 - 48];
            }
            return null;
        }

        private String getExtendedChar() {
            if ((this.mData1 == (byte) 18 || this.mData1 == (byte) 26) && this.mData2 >= (byte) 32 && this.mData2 <= (byte) 63) {
                return mSpanishCharMap[this.mData2 - 32];
            }
            if ((this.mData1 == (byte) 19 || this.mData1 == (byte) 27) && this.mData2 >= (byte) 32 && this.mData2 <= (byte) 63) {
                return mProtugueseCharMap[this.mData2 - 32];
            }
            return null;
        }

        public String toString() {
            if (this.mData1 >= PnoNetwork.FLAG_SAME_NETWORK || this.mData2 >= PnoNetwork.FLAG_SAME_NETWORK) {
                if (getCtrlCode() != -1) {
                    return String.format("[%d]%s", new Object[]{Byte.valueOf(this.mType), ctrlCodeToString(getCtrlCode())});
                }
                if (getTabOffset() > 0) {
                    return String.format("[%d]Tab%d", new Object[]{Byte.valueOf(this.mType), Integer.valueOf(getTabOffset())});
                }
                if (getPAC() != null) {
                    return String.format("[%d]PAC: %s", new Object[]{Byte.valueOf(this.mType), getPAC().toString()});
                }
                if (getMidRow() != null) {
                    return String.format("[%d]Mid-row: %s", new Object[]{Byte.valueOf(this.mType), getMidRow().toString()});
                } else if (isDisplayableChar()) {
                    return String.format("[%d]Displayable: %s (%02x %02x)", new Object[]{Byte.valueOf(this.mType), getDisplayText(), Byte.valueOf(this.mData1), Byte.valueOf(this.mData2)});
                } else {
                    return String.format("[%d]Invalid: %02x %02x", new Object[]{Byte.valueOf(this.mType), Byte.valueOf(this.mData1), Byte.valueOf(this.mData2)});
                }
            }
            return String.format("[%d]Null: %02x %02x", new Object[]{Byte.valueOf(this.mType), Byte.valueOf(this.mData1), Byte.valueOf(this.mData2)});
        }
    }

    /* compiled from: ClosedCaptionRenderer */
    private static class CCLineBuilder {
        private final StringBuilder mDisplayChars;
        private final StyleCode[] mMidRowStyles = new StyleCode[this.mDisplayChars.length()];
        private final StyleCode[] mPACStyles = new StyleCode[this.mDisplayChars.length()];

        CCLineBuilder(String str) {
            this.mDisplayChars = new StringBuilder(str);
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
                styledText.setSpan(new StyleSpan(2), start, end, 33);
            }
            if (s.isUnderline()) {
                styledText.setSpan(new UnderlineSpan(), start, end, 33);
            }
        }

        SpannableStringBuilder getStyledText(CaptionStyle captionStyle) {
            SpannableStringBuilder styledText = new SpannableStringBuilder(this.mDisplayChars);
            int start = -1;
            int next = 0;
            int styleStart = -1;
            StyleCode curStyle = null;
            while (next < this.mDisplayChars.length()) {
                StyleCode newStyle = null;
                if (this.mMidRowStyles[next] != null) {
                    newStyle = this.mMidRowStyles[next];
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
                if (this.mDisplayChars.charAt(next) != Cea608CCParser.TS) {
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
                next++;
            }
            return styledText;
        }
    }

    /* compiled from: ClosedCaptionRenderer */
    private static class CCMemory {
        private final String mBlankLine;
        private int mCol;
        private final CCLineBuilder[] mLines = new CCLineBuilder[17];
        private int mRow;

        CCMemory() {
            char[] blank = new char[34];
            Arrays.fill(blank, Cea608CCParser.TS);
            this.mBlankLine = new String(blank);
        }

        void erase() {
            for (int i = 0; i < this.mLines.length; i++) {
                this.mLines[i] = null;
            }
            this.mRow = 15;
            this.mCol = 1;
        }

        void der() {
            if (this.mLines[this.mRow] != null) {
                for (int i = 0; i < this.mCol; i++) {
                    if (this.mLines[this.mRow].charAt(i) != Cea608CCParser.TS) {
                        int j = this.mCol;
                        while (j < this.mLines[this.mRow].length() && j < this.mLines.length) {
                            if (this.mLines[j] != null) {
                                this.mLines[j].setCharAt(j, Cea608CCParser.TS);
                            }
                            j++;
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
            moveCursorByCol(-1);
            if (this.mLines[this.mRow] != null) {
                this.mLines[this.mRow].setCharAt(this.mCol, Cea608CCParser.TS);
                if (this.mCol == 31) {
                    this.mLines[this.mRow].setCharAt(32, Cea608CCParser.TS);
                }
            }
        }

        void cr() {
            moveCursorTo(this.mRow + 1, 1);
        }

        void rollUp(int windowSize) {
            int i;
            for (i = 0; i <= this.mRow - windowSize; i++) {
                this.mLines[i] = null;
            }
            int startRow = (this.mRow - windowSize) + 1;
            if (startRow < 1) {
                startRow = 1;
            }
            for (i = startRow; i < this.mRow; i++) {
                this.mLines[i] = this.mLines[i + 1];
            }
            for (i = this.mRow; i < this.mLines.length; i++) {
                this.mLines[i] = null;
            }
            this.mCol = 1;
        }

        void writeText(String text) {
            for (int i = 0; i < text.length(); i++) {
                getLineBuffer(this.mRow).setCharAt(this.mCol, text.charAt(i));
                moveCursorByCol(1);
            }
        }

        void writeMidRowCode(StyleCode m) {
            getLineBuffer(this.mRow).setMidRowAt(this.mCol, m);
            moveCursorByCol(1);
        }

        void writePAC(PAC pac) {
            if (pac.isIndentPAC()) {
                moveCursorTo(pac.getRow(), pac.getCol());
            } else {
                moveCursorTo(pac.getRow(), 1);
            }
            getLineBuffer(this.mRow).setPACAt(this.mCol, pac);
        }

        SpannableStringBuilder[] getStyledText(CaptionStyle captionStyle) {
            ArrayList<SpannableStringBuilder> rows = new ArrayList(15);
            for (int i = 1; i <= 15; i++) {
                Object styledText;
                if (this.mLines[i] != null) {
                    styledText = this.mLines[i].getStyledText(captionStyle);
                } else {
                    styledText = null;
                }
                rows.add(styledText);
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
                    for (i = actualWindowSize - 1; i >= 0; i--) {
                        this.mLines[baseRow - i] = this.mLines[this.mRow - i];
                    }
                } else {
                    for (i = 0; i < actualWindowSize; i++) {
                        this.mLines[baseRow - i] = this.mLines[this.mRow - i];
                    }
                }
                for (i = 0; i <= baseRow - windowSize; i++) {
                    this.mLines[i] = null;
                }
                for (i = baseRow + 1; i < this.mLines.length; i++) {
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
        static final String[] mColorMap = new String[]{"WHITE", "GREEN", "BLUE", "CYAN", "RED", "YELLOW", "MAGENTA", "INVALID"};
        final int mColor;
        final int mStyle;

        static StyleCode fromByte(byte data2) {
            int style = 0;
            int color = (data2 >> 1) & 7;
            if ((data2 & 1) != 0) {
                style = 2;
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

        boolean isItalics() {
            return (this.mStyle & 1) != 0;
        }

        boolean isUnderline() {
            return (this.mStyle & 2) != 0;
        }

        int getColor() {
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

    /* compiled from: ClosedCaptionRenderer */
    private static class PAC extends StyleCode {
        final int mCol;
        final int mRow;

        static PAC fromBytes(byte data1, byte data2) {
            int row = new int[]{11, 1, 3, 12, 14, 5, 7, 9}[data1 & 7] + ((data2 & 32) >> 5);
            int style = 0;
            if ((data2 & 1) != 0) {
                style = 2;
            }
            if ((data2 & 16) != 0) {
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

        boolean isIndentPAC() {
            return this.mCol >= 0;
        }

        int getRow() {
            return this.mRow;
        }

        int getCol() {
            return this.mCol;
        }

        public String toString() {
            return String.format("{%d, %d}, %s", new Object[]{Integer.valueOf(this.mRow), Integer.valueOf(this.mCol), super.toString()});
        }
    }

    Cea608CCParser(DisplayListener listener) {
        this.mListener = listener;
    }

    public void parse(byte[] data) {
        CCData[] ccData = CCData.fromByteArray(data);
        int i = 0;
        while (i < ccData.length) {
            if (DEBUG) {
                Log.d(TAG, ccData[i].toString());
            }
            if (!(handleCtrlCode(ccData[i]) || handleTabOffsets(ccData[i]) || handlePACCode(ccData[i]) || handleMidRowCode(ccData[i]))) {
                handleDisplayableChars(ccData[i]);
            }
            i++;
        }
    }

    private CCMemory getMemory() {
        switch (this.mMode) {
            case 1:
            case 2:
                return this.mDisplay;
            case 3:
                return this.mNonDisplay;
            case 4:
                return this.mTextMem;
            default:
                Log.w(TAG, "unrecoginized mode: " + this.mMode);
                return this.mDisplay;
        }
    }

    private boolean handleDisplayableChars(CCData ccData) {
        if (!ccData.isDisplayableChar()) {
            return false;
        }
        if (ccData.isExtendedChar()) {
            getMemory().bs();
        }
        getMemory().writeText(ccData.getDisplayText());
        if (this.mMode == 1 || this.mMode == 2) {
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
        if (this.mPrevCtrlCode == -1 || this.mPrevCtrlCode != ctrlCode) {
            switch (ctrlCode) {
                case 32:
                    this.mMode = 3;
                    break;
                case 33:
                    getMemory().bs();
                    break;
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
                default:
                    this.mPrevCtrlCode = -1;
                    return false;
            }
            this.mPrevCtrlCode = ctrlCode;
            return true;
        }
        this.mPrevCtrlCode = -1;
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

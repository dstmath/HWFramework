package android.media;

import android.graphics.Rect;
import android.os.Parcel;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public final class TimedText {
    private static final int FIRST_PRIVATE_KEY = 101;
    private static final int FIRST_PUBLIC_KEY = 1;
    private static final int KEY_BACKGROUND_COLOR_RGBA = 3;
    private static final int KEY_DISPLAY_FLAGS = 1;
    private static final int KEY_END_CHAR = 104;
    private static final int KEY_FONT_ID = 105;
    private static final int KEY_FONT_SIZE = 106;
    private static final int KEY_GLOBAL_SETTING = 101;
    private static final int KEY_HIGHLIGHT_COLOR_RGBA = 4;
    private static final int KEY_LOCAL_SETTING = 102;
    private static final int KEY_SCROLL_DELAY = 5;
    private static final int KEY_START_CHAR = 103;
    private static final int KEY_START_TIME = 7;
    private static final int KEY_STRUCT_BLINKING_TEXT_LIST = 8;
    private static final int KEY_STRUCT_FONT_LIST = 9;
    private static final int KEY_STRUCT_HIGHLIGHT_LIST = 10;
    private static final int KEY_STRUCT_HYPER_TEXT_LIST = 11;
    private static final int KEY_STRUCT_JUSTIFICATION = 15;
    private static final int KEY_STRUCT_KARAOKE_LIST = 12;
    private static final int KEY_STRUCT_STYLE_LIST = 13;
    private static final int KEY_STRUCT_TEXT = 16;
    private static final int KEY_STRUCT_TEXT_POS = 14;
    private static final int KEY_STYLE_FLAGS = 2;
    private static final int KEY_TEXT_COLOR_RGBA = 107;
    private static final int KEY_WRAP_TEXT = 6;
    private static final int LAST_PRIVATE_KEY = 107;
    private static final int LAST_PUBLIC_KEY = 16;
    private static final String TAG = "TimedText";
    private int mBackgroundColorRGBA;
    private List<CharPos> mBlinkingPosList;
    private int mDisplayFlags;
    private List<Font> mFontList;
    private int mHighlightColorRGBA;
    private List<CharPos> mHighlightPosList;
    private List<HyperText> mHyperTextList;
    private Justification mJustification;
    private List<Karaoke> mKaraokeList;
    private final HashMap<Integer, Object> mKeyObjectMap;
    private int mScrollDelay;
    private List<Style> mStyleList;
    private Rect mTextBounds;
    private String mTextChars;
    private int mWrapText;

    public static final class CharPos {
        public final int endChar;
        public final int startChar;

        public CharPos(int startChar, int endChar) {
            this.startChar = startChar;
            this.endChar = endChar;
        }
    }

    public static final class Font {
        public final int ID;
        public final String name;

        public Font(int id, String name) {
            this.ID = id;
            this.name = name;
        }
    }

    public static final class HyperText {
        public final String URL;
        public final String altString;
        public final int endChar;
        public final int startChar;

        public HyperText(int startChar, int endChar, String url, String alt) {
            this.startChar = startChar;
            this.endChar = endChar;
            this.URL = url;
            this.altString = alt;
        }
    }

    public static final class Justification {
        public final int horizontalJustification;
        public final int verticalJustification;

        public Justification(int horizontal, int vertical) {
            this.horizontalJustification = horizontal;
            this.verticalJustification = vertical;
        }
    }

    public static final class Karaoke {
        public final int endChar;
        public final int endTimeMs;
        public final int startChar;
        public final int startTimeMs;

        public Karaoke(int startTimeMs, int endTimeMs, int startChar, int endChar) {
            this.startTimeMs = startTimeMs;
            this.endTimeMs = endTimeMs;
            this.startChar = startChar;
            this.endChar = endChar;
        }
    }

    public static final class Style {
        public final int colorRGBA;
        public final int endChar;
        public final int fontID;
        public final int fontSize;
        public final boolean isBold;
        public final boolean isItalic;
        public final boolean isUnderlined;
        public final int startChar;

        public Style(int startChar, int endChar, int fontId, boolean isBold, boolean isItalic, boolean isUnderlined, int fontSize, int colorRGBA) {
            this.startChar = startChar;
            this.endChar = endChar;
            this.fontID = fontId;
            this.isBold = isBold;
            this.isItalic = isItalic;
            this.isUnderlined = isUnderlined;
            this.fontSize = fontSize;
            this.colorRGBA = colorRGBA;
        }
    }

    public TimedText(Parcel parcel) {
        this.mKeyObjectMap = new HashMap();
        this.mDisplayFlags = -1;
        this.mBackgroundColorRGBA = -1;
        this.mHighlightColorRGBA = -1;
        this.mScrollDelay = -1;
        this.mWrapText = -1;
        this.mBlinkingPosList = null;
        this.mHighlightPosList = null;
        this.mKaraokeList = null;
        this.mFontList = null;
        this.mStyleList = null;
        this.mHyperTextList = null;
        this.mTextBounds = null;
        this.mTextChars = null;
        if (!parseParcel(parcel)) {
            this.mKeyObjectMap.clear();
            throw new IllegalArgumentException("parseParcel() fails");
        }
    }

    public String getText() {
        return this.mTextChars;
    }

    public Rect getBounds() {
        return this.mTextBounds;
    }

    private boolean parseParcel(Parcel parcel) {
        parcel.setDataPosition(0);
        if (parcel.dataAvail() == 0) {
            return false;
        }
        int type = parcel.readInt();
        if (type == KEY_LOCAL_SETTING) {
            type = parcel.readInt();
            if (type != KEY_START_TIME) {
                return false;
            }
            this.mKeyObjectMap.put(Integer.valueOf(type), Integer.valueOf(parcel.readInt()));
            if (parcel.readInt() != LAST_PUBLIC_KEY) {
                return false;
            }
            int textLen = parcel.readInt();
            byte[] text = parcel.createByteArray();
            if (text == null || text.length == 0) {
                this.mTextChars = null;
            } else {
                this.mTextChars = new String(text);
            }
        } else if (type != KEY_GLOBAL_SETTING) {
            Log.w(TAG, "Invalid timed text key found: " + type);
            return false;
        }
        while (parcel.dataAvail() > 0) {
            int key = parcel.readInt();
            if (isValidKey(key)) {
                Object object = null;
                switch (key) {
                    case KEY_DISPLAY_FLAGS /*1*/:
                        this.mDisplayFlags = parcel.readInt();
                        object = Integer.valueOf(this.mDisplayFlags);
                        break;
                    case KEY_BACKGROUND_COLOR_RGBA /*3*/:
                        this.mBackgroundColorRGBA = parcel.readInt();
                        object = Integer.valueOf(this.mBackgroundColorRGBA);
                        break;
                    case KEY_HIGHLIGHT_COLOR_RGBA /*4*/:
                        this.mHighlightColorRGBA = parcel.readInt();
                        object = Integer.valueOf(this.mHighlightColorRGBA);
                        break;
                    case KEY_SCROLL_DELAY /*5*/:
                        this.mScrollDelay = parcel.readInt();
                        object = Integer.valueOf(this.mScrollDelay);
                        break;
                    case KEY_WRAP_TEXT /*6*/:
                        this.mWrapText = parcel.readInt();
                        object = Integer.valueOf(this.mWrapText);
                        break;
                    case KEY_STRUCT_BLINKING_TEXT_LIST /*8*/:
                        readBlinkingText(parcel);
                        object = this.mBlinkingPosList;
                        break;
                    case KEY_STRUCT_FONT_LIST /*9*/:
                        readFont(parcel);
                        object = this.mFontList;
                        break;
                    case KEY_STRUCT_HIGHLIGHT_LIST /*10*/:
                        readHighlight(parcel);
                        object = this.mHighlightPosList;
                        break;
                    case KEY_STRUCT_HYPER_TEXT_LIST /*11*/:
                        readHyperText(parcel);
                        object = this.mHyperTextList;
                        break;
                    case KEY_STRUCT_KARAOKE_LIST /*12*/:
                        readKaraoke(parcel);
                        object = this.mKaraokeList;
                        break;
                    case KEY_STRUCT_STYLE_LIST /*13*/:
                        readStyle(parcel);
                        object = this.mStyleList;
                        break;
                    case KEY_STRUCT_TEXT_POS /*14*/:
                        int top = parcel.readInt();
                        this.mTextBounds = new Rect(parcel.readInt(), top, parcel.readInt(), parcel.readInt());
                        break;
                    case KEY_STRUCT_JUSTIFICATION /*15*/:
                        this.mJustification = new Justification(parcel.readInt(), parcel.readInt());
                        object = this.mJustification;
                        break;
                }
                if (object != null) {
                    if (this.mKeyObjectMap.containsKey(Integer.valueOf(key))) {
                        this.mKeyObjectMap.remove(Integer.valueOf(key));
                    }
                    this.mKeyObjectMap.put(Integer.valueOf(key), object);
                }
            } else {
                Log.w(TAG, "Invalid timed text key found: " + key);
                return false;
            }
        }
        return true;
    }

    private void readStyle(Parcel parcel) {
        boolean endOfStyle = false;
        int startChar = -1;
        int endChar = -1;
        int fontId = -1;
        boolean isBold = false;
        boolean isItalic = false;
        boolean isUnderlined = false;
        int fontSize = -1;
        int colorRGBA = -1;
        while (!endOfStyle && parcel.dataAvail() > 0) {
            switch (parcel.readInt()) {
                case KEY_STYLE_FLAGS /*2*/:
                    int flags = parcel.readInt();
                    isBold = flags % KEY_STYLE_FLAGS == KEY_DISPLAY_FLAGS;
                    isItalic = flags % KEY_HIGHLIGHT_COLOR_RGBA >= KEY_STYLE_FLAGS;
                    if (flags / KEY_HIGHLIGHT_COLOR_RGBA != KEY_DISPLAY_FLAGS) {
                        isUnderlined = false;
                        break;
                    } else {
                        isUnderlined = true;
                        break;
                    }
                case KEY_START_CHAR /*103*/:
                    startChar = parcel.readInt();
                    break;
                case KEY_END_CHAR /*104*/:
                    endChar = parcel.readInt();
                    break;
                case KEY_FONT_ID /*105*/:
                    fontId = parcel.readInt();
                    break;
                case KEY_FONT_SIZE /*106*/:
                    fontSize = parcel.readInt();
                    break;
                case LAST_PRIVATE_KEY /*107*/:
                    colorRGBA = parcel.readInt();
                    break;
                default:
                    parcel.setDataPosition(parcel.dataPosition() - 4);
                    endOfStyle = true;
                    break;
            }
        }
        Style style = new Style(startChar, endChar, fontId, isBold, isItalic, isUnderlined, fontSize, colorRGBA);
        if (this.mStyleList == null) {
            this.mStyleList = new ArrayList();
        }
        this.mStyleList.add(style);
    }

    private void readFont(Parcel parcel) {
        int entryCount = parcel.readInt();
        for (int i = 0; i < entryCount; i += KEY_DISPLAY_FLAGS) {
            Font font = new Font(parcel.readInt(), new String(parcel.createByteArray(), 0, parcel.readInt()));
            if (this.mFontList == null) {
                this.mFontList = new ArrayList();
            }
            this.mFontList.add(font);
        }
    }

    private void readHighlight(Parcel parcel) {
        CharPos pos = new CharPos(parcel.readInt(), parcel.readInt());
        if (this.mHighlightPosList == null) {
            this.mHighlightPosList = new ArrayList();
        }
        this.mHighlightPosList.add(pos);
    }

    private void readKaraoke(Parcel parcel) {
        int entryCount = parcel.readInt();
        for (int i = 0; i < entryCount; i += KEY_DISPLAY_FLAGS) {
            Karaoke kara = new Karaoke(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt());
            if (this.mKaraokeList == null) {
                this.mKaraokeList = new ArrayList();
            }
            this.mKaraokeList.add(kara);
        }
    }

    private void readHyperText(Parcel parcel) {
        HyperText hyperText = new HyperText(parcel.readInt(), parcel.readInt(), new String(parcel.createByteArray(), 0, parcel.readInt()), new String(parcel.createByteArray(), 0, parcel.readInt()));
        if (this.mHyperTextList == null) {
            this.mHyperTextList = new ArrayList();
        }
        this.mHyperTextList.add(hyperText);
    }

    private void readBlinkingText(Parcel parcel) {
        CharPos blinkingPos = new CharPos(parcel.readInt(), parcel.readInt());
        if (this.mBlinkingPosList == null) {
            this.mBlinkingPosList = new ArrayList();
        }
        this.mBlinkingPosList.add(blinkingPos);
    }

    private boolean isValidKey(int key) {
        if ((key < KEY_DISPLAY_FLAGS || key > LAST_PUBLIC_KEY) && (key < KEY_GLOBAL_SETTING || key > LAST_PRIVATE_KEY)) {
            return false;
        }
        return true;
    }

    private boolean containsKey(int key) {
        if (isValidKey(key) && this.mKeyObjectMap.containsKey(Integer.valueOf(key))) {
            return true;
        }
        return false;
    }

    private Set keySet() {
        return this.mKeyObjectMap.keySet();
    }

    private Object getObject(int key) {
        if (containsKey(key)) {
            return this.mKeyObjectMap.get(Integer.valueOf(key));
        }
        throw new IllegalArgumentException("Invalid key: " + key);
    }
}

package android.inputmethodservice;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.xmlpull.v1.XmlPullParserException;

@Deprecated
public class Keyboard {
    public static final int EDGE_BOTTOM = 8;
    public static final int EDGE_LEFT = 1;
    public static final int EDGE_RIGHT = 2;
    public static final int EDGE_TOP = 4;
    private static final int GRID_HEIGHT = 5;
    private static final int GRID_SIZE = 50;
    private static final int GRID_WIDTH = 10;
    public static final int KEYCODE_ALT = -6;
    public static final int KEYCODE_CANCEL = -3;
    public static final int KEYCODE_DELETE = -5;
    public static final int KEYCODE_DONE = -4;
    public static final int KEYCODE_MODE_CHANGE = -2;
    public static final int KEYCODE_SHIFT = -1;
    private static float SEARCH_DISTANCE = 1.8f;
    static final String TAG = "Keyboard";
    private static final String TAG_KEY = "Key";
    private static final String TAG_KEYBOARD = "Keyboard";
    private static final String TAG_ROW = "Row";
    private int mCellHeight;
    private int mCellWidth;
    private int mDefaultHeight;
    private int mDefaultHorizontalGap;
    private int mDefaultVerticalGap;
    private int mDefaultWidth;
    private int mDisplayHeight;
    private int mDisplayWidth;
    private int[][] mGridNeighbors;
    private int mKeyHeight;
    private int mKeyWidth;
    private int mKeyboardMode;
    private List<Key> mKeys;
    private CharSequence mLabel;
    @UnsupportedAppUsage
    private List<Key> mModifierKeys;
    private int mProximityThreshold;
    private int[] mShiftKeyIndices;
    private Key[] mShiftKeys;
    private boolean mShifted;
    @UnsupportedAppUsage
    private int mTotalHeight;
    @UnsupportedAppUsage
    private int mTotalWidth;
    private ArrayList<Row> rows;

    public static class Row {
        public int defaultHeight;
        public int defaultHorizontalGap;
        public int defaultWidth;
        ArrayList<Key> mKeys = new ArrayList<>();
        public int mode;
        private Keyboard parent;
        public int rowEdgeFlags;
        public int verticalGap;

        public Row(Keyboard parent2) {
            this.parent = parent2;
        }

        public Row(Resources res, Keyboard parent2, XmlResourceParser parser) {
            this.parent = parent2;
            TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.Keyboard);
            this.defaultWidth = Keyboard.getDimensionOrFraction(a, 0, parent2.mDisplayWidth, parent2.mDefaultWidth);
            this.defaultHeight = Keyboard.getDimensionOrFraction(a, 1, parent2.mDisplayHeight, parent2.mDefaultHeight);
            this.defaultHorizontalGap = Keyboard.getDimensionOrFraction(a, 2, parent2.mDisplayWidth, parent2.mDefaultHorizontalGap);
            this.verticalGap = Keyboard.getDimensionOrFraction(a, 3, parent2.mDisplayHeight, parent2.mDefaultVerticalGap);
            a.recycle();
            TypedArray a2 = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.Keyboard_Row);
            this.rowEdgeFlags = a2.getInt(0, 0);
            this.mode = a2.getResourceId(1, 0);
        }
    }

    public static class Key {
        private static final int[] KEY_STATE_NORMAL = new int[0];
        private static final int[] KEY_STATE_NORMAL_OFF = {16842911};
        private static final int[] KEY_STATE_NORMAL_ON = {16842911, 16842912};
        private static final int[] KEY_STATE_PRESSED = {16842919};
        private static final int[] KEY_STATE_PRESSED_OFF = {16842919, 16842911};
        private static final int[] KEY_STATE_PRESSED_ON = {16842919, 16842911, 16842912};
        public int[] codes;
        public int edgeFlags;
        public int gap;
        public int height;
        public Drawable icon;
        public Drawable iconPreview;
        private Keyboard keyboard;
        public CharSequence label;
        public boolean modifier;
        public boolean on;
        public CharSequence popupCharacters;
        public int popupResId;
        public boolean pressed;
        public boolean repeatable;
        public boolean sticky;
        public CharSequence text;
        public int width;
        public int x;
        public int y;

        public Key(Row parent) {
            this.keyboard = parent.parent;
            this.height = parent.defaultHeight;
            this.width = parent.defaultWidth;
            this.gap = parent.defaultHorizontalGap;
            this.edgeFlags = parent.rowEdgeFlags;
        }

        public Key(Resources res, Row parent, int x2, int y2, XmlResourceParser parser) {
            this(parent);
            this.x = x2;
            this.y = y2;
            TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.Keyboard);
            this.width = Keyboard.getDimensionOrFraction(a, 0, this.keyboard.mDisplayWidth, parent.defaultWidth);
            this.height = Keyboard.getDimensionOrFraction(a, 1, this.keyboard.mDisplayHeight, parent.defaultHeight);
            this.gap = Keyboard.getDimensionOrFraction(a, 2, this.keyboard.mDisplayWidth, parent.defaultHorizontalGap);
            a.recycle();
            TypedArray a2 = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.Keyboard_Key);
            this.x += this.gap;
            TypedValue codesValue = new TypedValue();
            a2.getValue(0, codesValue);
            if (codesValue.type == 16 || codesValue.type == 17) {
                this.codes = new int[]{codesValue.data};
            } else if (codesValue.type == 3) {
                this.codes = parseCSV(codesValue.string.toString());
            }
            this.iconPreview = a2.getDrawable(7);
            Drawable drawable = this.iconPreview;
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), this.iconPreview.getIntrinsicHeight());
            }
            this.popupCharacters = a2.getText(2);
            this.popupResId = a2.getResourceId(1, 0);
            this.repeatable = a2.getBoolean(6, false);
            this.modifier = a2.getBoolean(4, false);
            this.sticky = a2.getBoolean(5, false);
            this.edgeFlags = a2.getInt(3, 0);
            this.edgeFlags |= parent.rowEdgeFlags;
            this.icon = a2.getDrawable(10);
            Drawable drawable2 = this.icon;
            if (drawable2 != null) {
                drawable2.setBounds(0, 0, drawable2.getIntrinsicWidth(), this.icon.getIntrinsicHeight());
            }
            this.label = a2.getText(9);
            this.text = a2.getText(8);
            if (this.codes == null && !TextUtils.isEmpty(this.label)) {
                this.codes = new int[]{this.label.charAt(0)};
            }
            a2.recycle();
        }

        public void onPressed() {
            this.pressed = !this.pressed;
        }

        public void onReleased(boolean inside) {
            this.pressed = !this.pressed;
            if (this.sticky && inside) {
                this.on = !this.on;
            }
        }

        /* access modifiers changed from: package-private */
        public int[] parseCSV(String value) {
            int count = 0;
            int lastIndex = 0;
            if (value.length() > 0) {
                while (true) {
                    count++;
                    int indexOf = value.indexOf(SmsManager.REGEX_PREFIX_DELIMITER, lastIndex + 1);
                    lastIndex = indexOf;
                    if (indexOf <= 0) {
                        break;
                    }
                }
            }
            int[] values = new int[count];
            int count2 = 0;
            StringTokenizer st = new StringTokenizer(value, SmsManager.REGEX_PREFIX_DELIMITER);
            while (st.hasMoreTokens()) {
                int count3 = count2 + 1;
                try {
                    values[count2] = Integer.parseInt(st.nextToken());
                } catch (NumberFormatException e) {
                    Log.e("Keyboard", "Error parsing keycodes " + value);
                }
                count2 = count3;
            }
            return values;
        }

        public boolean isInside(int x2, int y2) {
            int i;
            boolean leftEdge = (this.edgeFlags & 1) > 0;
            boolean rightEdge = (this.edgeFlags & 2) > 0;
            boolean topEdge = (this.edgeFlags & 4) > 0;
            boolean bottomEdge = (this.edgeFlags & 8) > 0;
            int i2 = this.x;
            if (x2 >= i2 || (leftEdge && x2 <= i2 + this.width)) {
                int i3 = this.x;
                if ((x2 < this.width + i3 || (rightEdge && x2 >= i3)) && (y2 >= (i = this.y) || (topEdge && y2 <= i + this.height))) {
                    int i4 = this.y;
                    if (y2 < this.height + i4 || (bottomEdge && y2 >= i4)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public int squaredDistanceFrom(int x2, int y2) {
            int xDist = (this.x + (this.width / 2)) - x2;
            int yDist = (this.y + (this.height / 2)) - y2;
            return (xDist * xDist) + (yDist * yDist);
        }

        public int[] getCurrentDrawableState() {
            int[] states = KEY_STATE_NORMAL;
            if (this.on) {
                if (this.pressed) {
                    return KEY_STATE_PRESSED_ON;
                }
                return KEY_STATE_NORMAL_ON;
            } else if (this.sticky) {
                if (this.pressed) {
                    return KEY_STATE_PRESSED_OFF;
                }
                return KEY_STATE_NORMAL_OFF;
            } else if (this.pressed) {
                return KEY_STATE_PRESSED;
            } else {
                return states;
            }
        }
    }

    public Keyboard(Context context, int xmlLayoutResId) {
        this(context, xmlLayoutResId, 0);
    }

    public Keyboard(Context context, int xmlLayoutResId, int modeId, int width, int height) {
        this.mShiftKeys = new Key[]{null, null};
        this.mShiftKeyIndices = new int[]{-1, -1};
        this.rows = new ArrayList<>();
        this.mDisplayWidth = width;
        this.mDisplayHeight = height;
        this.mDefaultHorizontalGap = 0;
        this.mDefaultWidth = this.mDisplayWidth / 10;
        this.mDefaultVerticalGap = 0;
        this.mDefaultHeight = this.mDefaultWidth;
        this.mKeys = new ArrayList();
        this.mModifierKeys = new ArrayList();
        this.mKeyboardMode = modeId;
        loadKeyboard(context, context.getResources().getXml(xmlLayoutResId));
    }

    public Keyboard(Context context, int xmlLayoutResId, int modeId) {
        this.mShiftKeys = new Key[]{null, null};
        this.mShiftKeyIndices = new int[]{-1, -1};
        this.rows = new ArrayList<>();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        this.mDisplayWidth = dm.widthPixels;
        this.mDisplayHeight = dm.heightPixels;
        this.mDefaultHorizontalGap = 0;
        this.mDefaultWidth = this.mDisplayWidth / 10;
        this.mDefaultVerticalGap = 0;
        this.mDefaultHeight = this.mDefaultWidth;
        this.mKeys = new ArrayList();
        this.mModifierKeys = new ArrayList();
        this.mKeyboardMode = modeId;
        loadKeyboard(context, context.getResources().getXml(xmlLayoutResId));
    }

    /* JADX DEBUG: Multi-variable search result rejected for r13v0, resolved type: int[] */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r10v0, types: [char] */
    public Keyboard(Context context, int layoutTemplateResId, CharSequence characters, int columns, int horizontalPadding) {
        this(context, layoutTemplateResId);
        int x = 0;
        int y = 0;
        int column = 0;
        this.mTotalWidth = 0;
        Row row = new Row(this);
        row.defaultHeight = this.mDefaultHeight;
        row.defaultWidth = this.mDefaultWidth;
        row.defaultHorizontalGap = this.mDefaultHorizontalGap;
        row.verticalGap = this.mDefaultVerticalGap;
        row.rowEdgeFlags = 12;
        int maxColumns = columns == -1 ? Integer.MAX_VALUE : columns;
        for (int i = 0; i < characters.length(); i++) {
            ?? charAt = characters.charAt(i);
            if (column >= maxColumns || this.mDefaultWidth + x + horizontalPadding > this.mDisplayWidth) {
                x = 0;
                y += this.mDefaultVerticalGap + this.mDefaultHeight;
                column = 0;
            }
            Key key = new Key(row);
            key.x = x;
            key.y = y;
            key.label = String.valueOf((char) charAt);
            key.codes = new int[]{charAt};
            column++;
            x += key.width + key.gap;
            this.mKeys.add(key);
            row.mKeys.add(key);
            if (x > this.mTotalWidth) {
                this.mTotalWidth = x;
            }
        }
        this.mTotalHeight = this.mDefaultHeight + y;
        this.rows.add(row);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public final void resize(int newWidth, int newHeight) {
        int numRows = this.rows.size();
        for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
            Row row = this.rows.get(rowIndex);
            int numKeys = row.mKeys.size();
            int totalGap = 0;
            int totalWidth = 0;
            for (int keyIndex = 0; keyIndex < numKeys; keyIndex++) {
                Key key = row.mKeys.get(keyIndex);
                if (keyIndex > 0) {
                    totalGap += key.gap;
                }
                totalWidth += key.width;
            }
            if (totalGap + totalWidth > newWidth) {
                int x = 0;
                float scaleFactor = ((float) (newWidth - totalGap)) / ((float) totalWidth);
                for (int keyIndex2 = 0; keyIndex2 < numKeys; keyIndex2++) {
                    Key key2 = row.mKeys.get(keyIndex2);
                    key2.width = (int) (((float) key2.width) * scaleFactor);
                    key2.x = x;
                    x += key2.width + key2.gap;
                }
            }
        }
        this.mTotalWidth = newWidth;
    }

    public List<Key> getKeys() {
        return this.mKeys;
    }

    public List<Key> getModifierKeys() {
        return this.mModifierKeys;
    }

    /* access modifiers changed from: protected */
    public int getHorizontalGap() {
        return this.mDefaultHorizontalGap;
    }

    /* access modifiers changed from: protected */
    public void setHorizontalGap(int gap) {
        this.mDefaultHorizontalGap = gap;
    }

    /* access modifiers changed from: protected */
    public int getVerticalGap() {
        return this.mDefaultVerticalGap;
    }

    /* access modifiers changed from: protected */
    public void setVerticalGap(int gap) {
        this.mDefaultVerticalGap = gap;
    }

    /* access modifiers changed from: protected */
    public int getKeyHeight() {
        return this.mDefaultHeight;
    }

    /* access modifiers changed from: protected */
    public void setKeyHeight(int height) {
        this.mDefaultHeight = height;
    }

    /* access modifiers changed from: protected */
    public int getKeyWidth() {
        return this.mDefaultWidth;
    }

    /* access modifiers changed from: protected */
    public void setKeyWidth(int width) {
        this.mDefaultWidth = width;
    }

    public int getHeight() {
        return this.mTotalHeight;
    }

    public int getMinWidth() {
        return this.mTotalWidth;
    }

    public boolean setShifted(boolean shiftState) {
        Key[] keyArr = this.mShiftKeys;
        for (Key shiftKey : keyArr) {
            if (shiftKey != null) {
                shiftKey.on = shiftState;
            }
        }
        if (this.mShifted == shiftState) {
            return false;
        }
        this.mShifted = shiftState;
        return true;
    }

    public boolean isShifted() {
        return this.mShifted;
    }

    public int[] getShiftKeyIndices() {
        return this.mShiftKeyIndices;
    }

    public int getShiftKeyIndex() {
        return this.mShiftKeyIndices[0];
    }

    /* JADX INFO: Multiple debug info for r6v2 int[]: [D('i' int), D('cell' int[])] */
    private void computeNearestNeighbors() {
        this.mCellWidth = ((getMinWidth() + 10) - 1) / 10;
        this.mCellHeight = ((getHeight() + 5) - 1) / 5;
        this.mGridNeighbors = new int[50][];
        int[] indices = new int[this.mKeys.size()];
        int gridWidth = this.mCellWidth * 10;
        int gridHeight = this.mCellHeight * 5;
        int x = 0;
        while (x < gridWidth) {
            int y = 0;
            while (y < gridHeight) {
                int count = 0;
                for (int i = 0; i < this.mKeys.size(); i++) {
                    Key key = this.mKeys.get(i);
                    if (key.squaredDistanceFrom(x, y) < this.mProximityThreshold || key.squaredDistanceFrom((this.mCellWidth + x) - 1, y) < this.mProximityThreshold || key.squaredDistanceFrom((this.mCellWidth + x) - 1, (this.mCellHeight + y) - 1) < this.mProximityThreshold || key.squaredDistanceFrom(x, (this.mCellHeight + y) - 1) < this.mProximityThreshold) {
                        indices[count] = i;
                        count++;
                    }
                }
                int[] cell = new int[count];
                System.arraycopy(indices, 0, cell, 0, count);
                int[][] iArr = this.mGridNeighbors;
                int i2 = this.mCellHeight;
                iArr[((y / i2) * 10) + (x / this.mCellWidth)] = cell;
                y += i2;
            }
            x += this.mCellWidth;
        }
    }

    public int[] getNearestKeys(int x, int y) {
        int index;
        if (this.mGridNeighbors == null) {
            computeNearestNeighbors();
        }
        if (x < 0 || x >= getMinWidth() || y < 0 || y >= getHeight() || (index = ((y / this.mCellHeight) * 10) + (x / this.mCellWidth)) >= 50) {
            return new int[0];
        }
        return this.mGridNeighbors[index];
    }

    /* access modifiers changed from: protected */
    public Row createRowFromXml(Resources res, XmlResourceParser parser) {
        return new Row(res, this, parser);
    }

    /* access modifiers changed from: protected */
    public Key createKeyFromXml(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
        return new Key(res, parent, x, y, parser);
    }

    private void loadKeyboard(Context context, XmlResourceParser parser) {
        boolean leftMostKey;
        Key key;
        boolean event = false;
        Resources res = context.getResources();
        boolean inRow = false;
        int row = 0;
        int x = 0;
        Key key2 = null;
        Row currentRow = null;
        Key key3 = null;
        int y = 0;
        while (true) {
            try {
                int event2 = parser.next();
                if (event2 == 1) {
                    break;
                } else if (event2 == 2) {
                    String tag = parser.getName();
                    boolean skipRow = false;
                    if (TAG_ROW.equals(tag)) {
                        x = 0;
                        try {
                            currentRow = createRowFromXml(res, parser);
                            this.rows.add(currentRow);
                            if (!(currentRow.mode == 0 || currentRow.mode == this.mKeyboardMode)) {
                                skipRow = true;
                            }
                            if (skipRow) {
                                try {
                                    skipToEndOfRow(parser);
                                    inRow = false;
                                    leftMostKey = event;
                                } catch (Exception e) {
                                    e = e;
                                    Log.e("Keyboard", "Parse error:" + e);
                                    e.printStackTrace();
                                    this.mTotalHeight = y - this.mDefaultVerticalGap;
                                }
                            } else {
                                inRow = true;
                                leftMostKey = event;
                            }
                        } catch (Exception e2) {
                            e = e2;
                            Log.e("Keyboard", "Parse error:" + e);
                            e.printStackTrace();
                            this.mTotalHeight = y - this.mDefaultVerticalGap;
                        }
                    } else if (TAG_KEY.equals(tag)) {
                        leftMostKey = event;
                        try {
                            key = createKeyFromXml(res, currentRow, x, y, parser);
                        } catch (Exception e3) {
                            e = e3;
                            Log.e("Keyboard", "Parse error:" + e);
                            e.printStackTrace();
                            this.mTotalHeight = y - this.mDefaultVerticalGap;
                        }
                        try {
                            this.mKeys.add(key);
                            if (key.codes[0] == -1) {
                                int i = 0;
                                while (true) {
                                    if (i >= this.mShiftKeys.length) {
                                        break;
                                    } else if (this.mShiftKeys[i] == null) {
                                        this.mShiftKeys[i] = key;
                                        this.mShiftKeyIndices[i] = this.mKeys.size() - 1;
                                        break;
                                    } else {
                                        i++;
                                    }
                                }
                                this.mModifierKeys.add(key);
                            } else if (key.codes[0] == -6) {
                                this.mModifierKeys.add(key);
                            }
                            currentRow.mKeys.add(key);
                            key2 = key;
                            key3 = 1;
                        } catch (Exception e4) {
                            e = e4;
                            Log.e("Keyboard", "Parse error:" + e);
                            e.printStackTrace();
                            this.mTotalHeight = y - this.mDefaultVerticalGap;
                        }
                    } else {
                        leftMostKey = event;
                        try {
                            if ("Keyboard".equals(tag)) {
                                parseKeyboardAttributes(res, parser);
                            }
                        } catch (Exception e5) {
                            e = e5;
                            Log.e("Keyboard", "Parse error:" + e);
                            e.printStackTrace();
                            this.mTotalHeight = y - this.mDefaultVerticalGap;
                        }
                    }
                    event = leftMostKey;
                } else {
                    if (event2 == 3) {
                        if (key3 != null) {
                            key3 = null;
                            int x2 = x + key2.gap + key2.width;
                            try {
                                if (x2 > this.mTotalWidth) {
                                    this.mTotalWidth = x2;
                                }
                                x = x2;
                                event = event;
                            } catch (Exception e6) {
                                e = e6;
                                Log.e("Keyboard", "Parse error:" + e);
                                e.printStackTrace();
                                this.mTotalHeight = y - this.mDefaultVerticalGap;
                            }
                        } else if (inRow) {
                            inRow = false;
                            y = y + currentRow.verticalGap + currentRow.defaultHeight;
                            row++;
                            event = event;
                        }
                    }
                    event = event;
                }
            } catch (Exception e7) {
                e = e7;
                Log.e("Keyboard", "Parse error:" + e);
                e.printStackTrace();
                this.mTotalHeight = y - this.mDefaultVerticalGap;
            }
        }
        this.mTotalHeight = y - this.mDefaultVerticalGap;
    }

    private void skipToEndOfRow(XmlResourceParser parser) throws XmlPullParserException, IOException {
        while (true) {
            int event = parser.next();
            if (event == 1) {
                return;
            }
            if (event == 3 && parser.getName().equals(TAG_ROW)) {
                return;
            }
        }
    }

    private void parseKeyboardAttributes(Resources res, XmlResourceParser parser) {
        TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.Keyboard);
        int i = this.mDisplayWidth;
        this.mDefaultWidth = getDimensionOrFraction(a, 0, i, i / 10);
        this.mDefaultHeight = getDimensionOrFraction(a, 1, this.mDisplayHeight, 50);
        this.mDefaultHorizontalGap = getDimensionOrFraction(a, 2, this.mDisplayWidth, 0);
        this.mDefaultVerticalGap = getDimensionOrFraction(a, 3, this.mDisplayHeight, 0);
        this.mProximityThreshold = (int) (((float) this.mDefaultWidth) * SEARCH_DISTANCE);
        int i2 = this.mProximityThreshold;
        this.mProximityThreshold = i2 * i2;
        a.recycle();
    }

    static int getDimensionOrFraction(TypedArray a, int index, int base, int defValue) {
        TypedValue value = a.peekValue(index);
        if (value == null) {
            return defValue;
        }
        if (value.type == 5) {
            return a.getDimensionPixelOffset(index, defValue);
        }
        if (value.type == 6) {
            return Math.round(a.getFraction(index, base, base, (float) defValue));
        }
        return defValue;
    }
}

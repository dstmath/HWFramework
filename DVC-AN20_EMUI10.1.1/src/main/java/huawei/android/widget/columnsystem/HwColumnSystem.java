package huawei.android.widget.columnsystem;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.FreezeScreenScene;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HwColumnSystem {
    private static final int BREAKPOINT_360DP = 360;
    private static final int BREAKPOINT_600DP = 600;
    private static final int BREAKPOINT_600DP_COLUMN = 8;
    private static final int BREAKPOINT_600DP_INDEX = 1;
    private static final int BREAKPOINT_840DP = 840;
    private static final int BREAKPOINT_840DP_COLUMN = 12;
    private static final int BREAKPOINT_840DP_INDEX = 2;
    public static final int BUBBLE_TYPE = 4;
    public static final int CARD_TYPE = 3;
    private static final int COLUMNS_BREAKPOINT_COUNT = 3;
    private static final int COLUMN_DOUBLE = 2;
    public static final int CONTENT_TYPE = 0;
    public static final int CUSTOM_TYPE = 11;
    private static final int DEFAULT_COLUMN = 4;
    public static final int DEFAULT_TYPE = -1;
    private static final int DIALOG_SIZE_RATIO3 = 3;
    private static final int DIALOG_SIZE_RATIO4 = 4;
    public static final int DIALOG_TYPE = 12;
    public static final int DOUBLE_BUTTON_TYPE = 2;
    protected static final int FULL_SCREEN_COLUMN = -2;
    private static final int[][] GUTTER_DEFINE = {new int[]{24, 24, 24}, new int[]{12, 12, 12}, new int[]{12, 12, 12}, new int[]{8, 8, 8}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{16, 16, 16}, new int[]{16, 16, 16}, new int[]{16, 16, 16}};
    private static final int INDEX1 = 1;
    private static final int INDEX2 = 2;
    private static final int INDEX3 = 3;
    protected static final boolean IS_DEBUG = false;
    public static final int LARGE_BOTTOMTAB_TYPE = 9;
    private static final int LARGE_DIALOG_TYPE = 14;
    public static final int LARGE_TOOLBAR_TYPE = 7;
    private static final int[][] MARGIN_DEFINE = {new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{8, 8, 8}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{16, 16, 16}, new int[]{16, 16, 16}, new int[]{16, 16, 16}};
    private static final String MATCHER_REGEX = "^c(\\d+)m(\\d+)g(\\d+)";
    private static final int[][] MAX_COLUMN_DEFINE = {new int[]{4, 6, 8}, new int[]{4, 6, 8}, new int[]{4, 6, 8}, new int[]{4, 6, 8}, new int[]{4, 6, 6}, new int[]{4, 6, 6}, new int[]{4, 6, 6}, new int[]{-2, 8, 8}, new int[]{4, 6, 10}, new int[]{-2, 8, 12}, new int[]{4, 6, 8}, new int[]{4, 6, 8}, new int[]{4, 4, 5}, new int[]{3, 4, 5}, new int[]{4, 5, 5}};
    public static final int MENU_TYPE = 10;
    private static final int[][] MIN_COLUMN_DEFINE = {new int[]{4, 6, 8}, new int[]{2, 3, 4}, new int[]{4, 6, 8}, new int[]{4, 6, 8}, new int[]{4, 6, 6}, new int[]{2, 2, 2}, new int[]{4, 6, 6}, new int[]{-2, 8, 8}, new int[]{4, 6, 10}, new int[]{-2, 8, 12}, new int[]{2, 2, 2}, new int[]{4, 6, 8}, new int[]{4, 4, 5}, new int[]{3, 4, 5}, new int[]{4, 5, 5}};
    private static final float PIXEL_PRECISION_OFFSET = 0.5f;
    private static final int REGEX_PARAM_COUNT = 3;
    public static final int SINGLE_BUTTON_TYPE = 1;
    public static final int SMALL_BOTTOMTAB_TYPE = 8;
    private static final int SMALL_DIALOG_TYPE = 13;
    public static final int SMALL_TOOLBAR_TYPE = 6;
    private static final String TAG = HwColumnSystem.class.getSimpleName();
    public static final int TOAST_TYPE = 5;
    private static final int UN_SET = -1;
    private List<Integer[]> mBreakPointList = new ArrayList();
    private int mColumnCount;
    private HwColumnPolicy mColumnPolicy;
    private int mColumnType = -1;
    private Context mContext;
    private float mDensity;
    private int mGutter;
    private int mHeightPixel;
    private int mMargin;
    private int mMaxColumnCount;
    private int mTotalColumn = 4;
    private int mWidthPixel;
    private float mXdpi;
    private float mYdpi;

    public HwColumnSystem(Context context) {
        this.mContext = context;
        initDisplayInfo();
    }

    public HwColumnSystem(Context context, int columnType) {
        this.mColumnType = columnType;
        this.mContext = context;
        initDisplayInfo();
    }

    public HwColumnSystem(Context context, String columnsDefine) throws IllegalArgumentException {
        String columnDefine;
        this.mContext = context;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        String[] columnsDefineArray = columnsDefine.split("-");
        if (columnsDefineArray.length == 3) {
            String str = columnsDefineArray[0];
            this.mTotalColumn = 4;
            if (displayMetrics.widthPixels >= dp2px(BREAKPOINT_840DP, displayMetrics.density)) {
                columnDefine = columnsDefineArray[2];
                this.mTotalColumn = 12;
            } else if (displayMetrics.widthPixels >= dp2px(BREAKPOINT_600DP, displayMetrics.density)) {
                columnDefine = columnsDefineArray[1];
                this.mTotalColumn = 8;
            } else {
                String columnDefine2 = columnsDefineArray[0];
                this.mTotalColumn = 4;
                columnDefine = columnDefine2;
            }
            Matcher matcher = Pattern.compile(MATCHER_REGEX).matcher(columnDefine);
            if (matcher.find() && matcher.groupCount() == 3) {
                this.mMargin = dp2px(Integer.valueOf(matcher.group(2)).intValue(), displayMetrics.density);
                this.mGutter = dp2px(Integer.valueOf(matcher.group(3)).intValue(), displayMetrics.density);
                this.mColumnCount = Integer.valueOf(matcher.group(1)).intValue();
                this.mColumnPolicy = new HwColumnPolicyImpl();
                this.mWidthPixel = displayMetrics.widthPixels;
                this.mHeightPixel = displayMetrics.heightPixels;
                if (Build.VERSION.SDK_INT > 28) {
                    Rect safeRect = HwDisplaySizeUtil.getDisplaySafeInsets();
                    if (isPortrait(this.mContext) && (safeRect.left > 0 || safeRect.right > 0)) {
                        this.mWidthPixel = (dp2px(this.mContext.getResources().getConfiguration().screenWidthDp, displayMetrics.density) - safeRect.left) - safeRect.right;
                    }
                }
                this.mColumnPolicy.updateConfigration(this.mWidthPixel, this.mHeightPixel, displayMetrics.density);
                HwColumnPolicy hwColumnPolicy = this.mColumnPolicy;
                int i = this.mMargin;
                int i2 = this.mGutter;
                int i3 = this.mColumnCount;
                hwColumnPolicy.setColumnConfig(i, i2, i3, i3, this.mTotalColumn);
                return;
            }
            return;
        }
        throw new IllegalArgumentException();
    }

    public int getTotalColumnCount() {
        return this.mTotalColumn;
    }

    public static int getSuggestWidth(Context context, int type) {
        return new HwColumnSystem(context, type).getSuggestWidth();
    }

    public int getSuggestWidth() {
        return this.mColumnPolicy.getColumnWidth();
    }

    public float getColumnWidth(int columnsCount) {
        if (columnsCount <= 0) {
            return 0.0f;
        }
        return this.mColumnPolicy.getColumnWidth(columnsCount);
    }

    private void initDisplayInfo() {
        DisplayMetrics displayMetrics = this.mContext.getResources().getDisplayMetrics();
        this.mWidthPixel = displayMetrics.widthPixels;
        this.mHeightPixel = displayMetrics.heightPixels;
        if (Build.VERSION.SDK_INT > 28) {
            Rect safeRect = HwDisplaySizeUtil.getDisplaySafeInsets();
            if (isPortrait(this.mContext) && (safeRect.left > 0 || safeRect.right > 0)) {
                this.mWidthPixel = (dp2px(this.mContext.getResources().getConfiguration().screenWidthDp, displayMetrics.density) - safeRect.left) - safeRect.right;
            }
        }
        this.mDensity = displayMetrics.density;
        this.mXdpi = displayMetrics.xdpi;
        this.mYdpi = displayMetrics.ydpi;
        initResource();
        this.mColumnPolicy = new HwColumnPolicyImpl();
        this.mColumnPolicy.updateConfigration(this.mWidthPixel, this.mHeightPixel, this.mDensity);
        this.mColumnPolicy.setColumnConfig(this.mMargin, this.mGutter, this.mColumnCount, this.mMaxColumnCount, this.mTotalColumn);
    }

    private void initDisplayInfo(int margin, int gutter, int columns) {
        this.mColumnPolicy = new HwColumnPolicyImpl();
        this.mColumnPolicy.updateConfigration(this.mWidthPixel, this.mHeightPixel, this.mDensity);
        this.mColumnPolicy.setColumnConfig(this.mMargin, this.mGutter, this.mColumnCount, this.mMaxColumnCount, this.mTotalColumn);
    }

    private boolean isPortrait(Context context) {
        WindowManager windowManager;
        int rotate = 0;
        if (!(context == null || (windowManager = (WindowManager) context.getSystemService(FreezeScreenScene.WINDOW_PARAM)) == null)) {
            rotate = windowManager.getDefaultDisplay().getRotation();
        }
        return rotate == 0 || rotate == 2;
    }

    private void initDefineByType(int type, int index, float density) {
        int columnType = type;
        if (columnType < 0 || columnType >= MIN_COLUMN_DEFINE.length) {
            columnType = 0;
        }
        this.mMargin = dp2px(MARGIN_DEFINE[columnType][index], density);
        this.mGutter = dp2px(GUTTER_DEFINE[columnType][index], density);
        this.mColumnCount = MIN_COLUMN_DEFINE[columnType][index];
        this.mMaxColumnCount = MAX_COLUMN_DEFINE[columnType][index];
    }

    private void initContentType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472354);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472353);
        this.mColumnCount = this.mContext.getResources().getInteger(34275356);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275357);
    }

    private void initSingleButtonType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472350);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472349);
        this.mColumnCount = this.mContext.getResources().getInteger(34275352);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275353);
    }

    private void initDoubleButtonType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472356);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472355);
        this.mColumnCount = this.mContext.getResources().getInteger(34275358);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275359);
    }

    private void initToastType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472372);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472371);
        this.mColumnCount = this.mContext.getResources().getInteger(34275374);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275375);
    }

    private void initMenuType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472364);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472363);
        this.mColumnCount = this.mContext.getResources().getInteger(34275366);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275367);
    }

    private void initSmallToolbarType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472370);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472369);
        this.mColumnCount = this.mContext.getResources().getInteger(34275372);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275373);
    }

    private void initLargeToolbarType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472362);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472361);
        this.mColumnCount = this.mContext.getResources().getInteger(34275364);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275365);
    }

    private void initSmallBottomTabType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472366);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472365);
        this.mColumnCount = this.mContext.getResources().getInteger(34275368);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275369);
    }

    private void initLargeBottomTabType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472358);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472357);
        this.mColumnCount = this.mContext.getResources().getInteger(34275360);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275361);
    }

    private void initCardType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472352);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472351);
        this.mColumnCount = this.mContext.getResources().getInteger(34275354);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275355);
    }

    private void initBubbleType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472348);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472347);
        this.mColumnCount = this.mContext.getResources().getInteger(34275350);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275351);
    }

    private void initSmallDialogType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472368);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472367);
        this.mColumnCount = this.mContext.getResources().getInteger(34275370);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275371);
    }

    private void initLargeDialogType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472360);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472359);
        this.mColumnCount = this.mContext.getResources().getInteger(34275362);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275363);
    }

    private void initDefaultType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472354);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472353);
        this.mColumnCount = this.mContext.getResources().getInteger(34275356);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275357);
    }

    private void initResource(float breakPoint, float density) {
        int columnIndex;
        if (breakPoint >= 840.0f) {
            this.mTotalColumn = 12;
            columnIndex = 2;
        } else if (breakPoint >= 600.0f) {
            this.mTotalColumn = 8;
            columnIndex = 1;
        } else {
            this.mTotalColumn = 4;
            columnIndex = 0;
        }
        int i = this.mColumnType;
        if (i == 12 || i == 13 || i == 14) {
            switchDialogType();
        }
        initDefineByType(this.mColumnType, columnIndex, density);
    }

    private void initResource() {
        Context context = this.mContext;
        if (context != null) {
            this.mTotalColumn = context.getResources().getInteger(34275376);
            int i = this.mColumnType;
            if (i == 12 || i == 13 || i == 14) {
                switchDialogType();
            }
            initResourceByType();
        }
    }

    private void initResourceByType() {
        switch (this.mColumnType) {
            case -1:
            case 0:
                initContentType();
                return;
            case 1:
                initSingleButtonType();
                return;
            case 2:
                initDoubleButtonType();
                return;
            case 3:
                initCardType();
                return;
            case 4:
                initBubbleType();
                return;
            case 5:
                initToastType();
                return;
            case 6:
                initSmallToolbarType();
                return;
            case 7:
                initLargeToolbarType();
                return;
            case 8:
                initSmallBottomTabType();
                return;
            case 9:
                initLargeBottomTabType();
                return;
            case 10:
                initMenuType();
                return;
            case 11:
            case 12:
            default:
                initDefaultType();
                return;
            case 13:
                initSmallDialogType();
                return;
            case 14:
                initLargeDialogType();
                return;
        }
    }

    private void switchDialogType() {
        if (this.mTotalColumn == 4) {
            if (this.mWidthPixel * 4 > this.mHeightPixel * 3) {
                this.mColumnType = 13;
            } else {
                this.mColumnType = 14;
            }
        } else if (this.mWidthPixel * 3 > this.mHeightPixel * 4) {
            this.mColumnType = 13;
        } else {
            this.mColumnType = 14;
        }
    }

    public int getColumnType() {
        return this.mColumnType;
    }

    public void setColumnType(int columnType) {
        this.mColumnType = columnType;
        if (this.mContext != null && this.mColumnPolicy != null) {
            initResource();
            this.mColumnPolicy.setColumnConfig(this.mMargin, this.mGutter, this.mColumnCount, this.mMaxColumnCount, this.mTotalColumn);
        }
    }

    public int getMargin() {
        return this.mMargin;
    }

    public int getGutter() {
        return this.mGutter;
    }

    public float getSingleColumnWidth() {
        return getColumnWidth(1);
    }

    public int getMinColumnWidth() {
        return this.mColumnPolicy.getMinColumnWidth();
    }

    public int getMaxColumnWidth() {
        return this.mColumnPolicy.getMaxColumnWidth();
    }

    public int addBreakpoint(int width, int columnsCount) {
        this.mBreakPointList.add(new Integer[]{Integer.valueOf(dp2px(width, this.mDensity)), Integer.valueOf(columnsCount)});
        Collections.sort(this.mBreakPointList, new Comparator<Integer[]>() {
            /* class huawei.android.widget.columnsystem.HwColumnSystem.AnonymousClass1 */

            public int compare(Integer[] t1, Integer[] t2) {
                return Integer.compare(t1[0].intValue(), t2[0].intValue());
            }
        });
        int columnCount = this.mColumnCount;
        for (Integer[] breakPoint : this.mBreakPointList) {
            if (this.mWidthPixel > breakPoint[0].intValue()) {
                columnCount = breakPoint[1].intValue();
            }
        }
        this.mColumnCount = columnCount;
        HwColumnPolicy hwColumnPolicy = this.mColumnPolicy;
        if (hwColumnPolicy == null) {
            return -1;
        }
        hwColumnPolicy.setMinColumn(this.mColumnCount);
        this.mColumnPolicy.onUpdateConfig();
        return (int) (getColumnWidth(columnCount) + 0.5f);
    }

    public int updateConfigation(Context context) {
        this.mContext = context;
        Context context2 = this.mContext;
        if (context2 == null) {
            return getSuggestWidth();
        }
        DisplayMetrics displayMetrics = context2.getResources().getDisplayMetrics();
        if (!(displayMetrics.widthPixels == this.mWidthPixel && displayMetrics.density == this.mDensity)) {
            initDisplayInfo();
        }
        return getSuggestWidth();
    }

    public int updateConfigation(Context context, int width, int height, float density) {
        if (width <= 0 || density <= 0.0f) {
            Log.w(TAG, "width and density should not below to zero!");
            return getSuggestWidth();
        }
        this.mContext = context;
        this.mWidthPixel = width;
        this.mHeightPixel = height;
        this.mDensity = density;
        initResource(((float) width) / density, density);
        if (this.mColumnPolicy == null) {
            this.mColumnPolicy = new HwColumnPolicyImpl();
        }
        this.mColumnPolicy.updateConfigration(width, height, density);
        this.mColumnPolicy.setColumnConfig(this.mMargin, this.mGutter, this.mColumnCount, this.mMaxColumnCount, this.mTotalColumn);
        this.mColumnPolicy.onUpdateConfig();
        return getSuggestWidth();
    }

    private int dp2px(int dp, float density) {
        return (int) ((((float) dp) * density) + 0.5f);
    }
}

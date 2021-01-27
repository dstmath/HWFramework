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
import java.util.regex.PatternSyntaxException;

public class HwColumnSystem {
    public static final int BOTTOM_SHEET_TYPE = 19;
    private static final int BREAKPOINT_360DP = 360;
    private static final int BREAKPOINT_520DP = 520;
    private static final int BREAKPOINT_520DP_COLUMN = 8;
    private static final int BREAKPOINT_520DP_INDEX = 1;
    private static final int BREAKPOINT_840DP = 840;
    private static final int BREAKPOINT_840DP_COLUMN = 12;
    private static final int BREAKPOINT_840DP_INDEX = 2;
    public static final int BUBBLE_TYPE = 4;
    public static final int CARD_DOUBLE_BUTTON_TYPE = 18;
    public static final int CARD_SINGLE_BUTTON_TYPE = 17;
    public static final int CARD_TYPE = 3;
    private static final int COLUMNS_BREAKPOINT_COUNT = 3;
    private static final int COLUMN_DOUBLE = 2;
    public static final int CONTENT_TYPE = 0;
    public static final int CUSTOM_TYPE = 11;
    private static final int DEFAULT_COLUMN = 4;
    public static final int DEFAULT_TYPE = -1;
    private static final int DIALOG_SIZE_RATIO16 = 16;
    private static final int DIALOG_SIZE_RATIO3 = 3;
    private static final int DIALOG_SIZE_RATIO4 = 4;
    private static final int DIALOG_SIZE_RATIO9 = 9;
    public static final int DIALOG_TYPE = 12;
    public static final int DOUBLE_BUTTON_TYPE = 2;
    protected static final int FULL_SCREEN_COLUMN = -2;
    private static final int[][] GUTTER_DEFINE = {new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{12, 12, 12}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{16, 16, 16}, new int[]{12, 12, 12}, new int[]{12, 12, 12}, new int[]{12, 12, 12}, new int[]{12, 12, 12}, new int[]{12, 12, 12}, new int[]{12, 12, 12}, new int[]{12, 12, 12}};
    private static final int INDEX1 = 1;
    private static final int INDEX2 = 2;
    private static final int INDEX3 = 3;
    protected static final boolean IS_DEBUG = false;
    public static final int LARGE_BOTTOMTAB_TYPE = 9;
    public static final int LARGE_TOOLBAR_TYPE = 7;
    private static final int LOWER_LARGE_DIALOG_TYPE = 14;
    private static final int LOWER_SMALL_DIALOG_TYPE = 13;
    private static final int[][] MARGIN_DEFINE = {new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{12, 12, 12}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{24, 24, 24}, new int[]{16, 16, 16}, new int[]{12, 12, 12}, new int[]{12, 12, 12}, new int[]{12, 12, 12}, new int[]{12, 12, 12}, new int[]{12, 12, 12}, new int[]{12, 12, 12}, new int[]{12, 12, 12}};
    private static final String MATCHER_REGEX = "^c(\\d+)m(\\d+)g(\\d+)";
    private static final int[][] MAX_COLUMN_DEFINE = {new int[]{4, 6, 8}, new int[]{4, 6, 8}, new int[]{4, 6, 8}, new int[]{4, 6, 8}, new int[]{4, 6, 6}, new int[]{4, 6, 6}, new int[]{4, 6, 6}, new int[]{-2, 8, 8}, new int[]{4, 6, 10}, new int[]{-2, 8, 12}, new int[]{4, 6, 8}, new int[]{4, 6, 8}, new int[]{4, 4, 5}, new int[]{3, 4, 5}, new int[]{4, 5, 6}, new int[]{2, 3, 4}, new int[]{3, 4, 5}, new int[]{4, 6, 8}, new int[]{4, 6, 8}, new int[]{-2, 6, 6}};
    public static final int MENU_TYPE = 10;
    private static final int[][] MIN_COLUMN_DEFINE = {new int[]{4, 6, 8}, new int[]{2, 3, 4}, new int[]{4, 6, 8}, new int[]{4, 6, 8}, new int[]{4, 6, 6}, new int[]{2, 2, 2}, new int[]{4, 6, 6}, new int[]{-2, 8, 8}, new int[]{4, 6, 10}, new int[]{-2, 8, 12}, new int[]{2, 2, 2}, new int[]{4, 6, 8}, new int[]{4, 4, 5}, new int[]{3, 4, 5}, new int[]{4, 5, 6}, new int[]{2, 3, 4}, new int[]{3, 4, 5}, new int[]{2, 3, 4}, new int[]{4, 6, 8}, new int[]{-2, 6, 6}};
    private static final float PIXEL_PRECISION_OFFSET = 0.5f;
    private static final int REGEX_PARAM_COUNT = 3;
    private static final double SCREEN_SIZE_12_INCH = 12.0d;
    public static final int SINGLE_BUTTON_TYPE = 1;
    public static final int SMALL_BOTTOMTAB_TYPE = 8;
    public static final int SMALL_TOOLBAR_TYPE = 6;
    private static final String TAG = HwColumnSystem.class.getSimpleName();
    public static final int TOAST_TYPE = 5;
    private static final int UN_SET = -1;
    private static final int UPPER_LARGE_DIALOG_TYPE = 16;
    private static final int UPPER_SMALL_DIALOG_TYPE = 15;
    private List<Integer[]> mBreakPointList;
    private int mColumnCount;
    private HwColumnPolicy mColumnPolicy;
    private int mColumnType;
    private Context mContext;
    private String[] mDefineArray;
    private float mDensity;
    private int mGutter;
    private int mHeightPixel;
    private boolean mIsUserDefine;
    private int mMargin;
    private int mMaxColumnCount;
    private int mTotalColumn;
    private int mWidthPixel;

    public HwColumnSystem(Context context) {
        this.mBreakPointList = new ArrayList();
        this.mColumnType = -1;
        this.mTotalColumn = 4;
        this.mIsUserDefine = false;
        this.mContext = context;
        initDisplayInfo();
    }

    public HwColumnSystem(Context context, int columnType) {
        this.mBreakPointList = new ArrayList();
        this.mColumnType = -1;
        this.mTotalColumn = 4;
        this.mIsUserDefine = false;
        this.mColumnType = columnType;
        this.mContext = context;
        initDisplayInfo();
    }

    public HwColumnSystem(Context context, String columnsDefine) throws IllegalArgumentException {
        this.mBreakPointList = new ArrayList();
        this.mColumnType = -1;
        this.mTotalColumn = 4;
        boolean z = false;
        this.mIsUserDefine = false;
        this.mContext = context;
        try {
            this.mDefineArray = columnsDefine.split("-");
            if (this.mDefineArray.length == 3) {
                this.mIsUserDefine = true;
                refreshScreenInfo();
                setColumnDataByUserDefine(this.mWidthPixel, Build.VERSION.SDK_INT > 28 ? true : z);
                return;
            }
            Log.w(TAG, "Input rules count error!");
            throw new IllegalArgumentException();
        } catch (PatternSyntaxException e) {
            Log.w(TAG, "Input format error!");
            throw new IllegalArgumentException();
        }
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

    private void setColumnDataByUserDefine(int width, boolean isSafeRectWidth) {
        String columnDefine;
        String str = this.mDefineArray[0];
        this.mTotalColumn = 4;
        if (width >= dp2px(BREAKPOINT_840DP, this.mDensity)) {
            columnDefine = this.mDefineArray[2];
            this.mTotalColumn = 12;
        } else if (width >= dp2px(520, this.mDensity)) {
            columnDefine = this.mDefineArray[1];
            this.mTotalColumn = 8;
        } else {
            columnDefine = this.mDefineArray[0];
            this.mTotalColumn = 4;
        }
        if (isSafeRectWidth) {
            width = getSafeRectWidth(width, this.mDensity);
        }
        Matcher matcher = Pattern.compile(MATCHER_REGEX).matcher(columnDefine);
        if (matcher.find() && matcher.groupCount() == 3) {
            this.mMargin = dp2px(Integer.valueOf(matcher.group(2)).intValue(), this.mDensity);
            this.mGutter = dp2px(Integer.valueOf(matcher.group(3)).intValue(), this.mDensity);
            this.mColumnCount = Integer.valueOf(matcher.group(1)).intValue();
            this.mMaxColumnCount = this.mColumnCount;
            this.mWidthPixel = width;
            refreshDisplayInfo();
        }
    }

    private int getSafeRectWidth(int width, float density) {
        Rect safeRect = HwDisplaySizeUtil.getDisplaySafeInsets();
        if (!isPortrait(this.mContext)) {
            return width;
        }
        if (safeRect.left > 0 || safeRect.right > 0) {
            return (dp2px(this.mContext.getResources().getConfiguration().screenWidthDp, density) - safeRect.left) - safeRect.right;
        }
        return width;
    }

    private void initDisplayInfo() {
        refreshScreenInfo();
        if (Build.VERSION.SDK_INT > 28) {
            this.mWidthPixel = getSafeRectWidth(this.mWidthPixel, this.mDensity);
        }
        initResource();
        refreshDisplayInfo();
    }

    private void refreshScreenInfo() {
        DisplayMetrics displayMetrics = this.mContext.getResources().getDisplayMetrics();
        this.mWidthPixel = displayMetrics.widthPixels;
        this.mHeightPixel = displayMetrics.heightPixels;
        this.mDensity = displayMetrics.density;
    }

    private void refreshDisplayInfo() {
        this.mColumnPolicy = new HwColumnPolicyImpl();
        this.mColumnPolicy.setColumnType(this.mColumnType);
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
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472400);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472399);
        this.mColumnCount = this.mContext.getResources().getInteger(34275368);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275369);
    }

    private void initSingleButtonType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472392);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472391);
        this.mColumnCount = this.mContext.getResources().getInteger(34275360);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275361);
    }

    private void initDoubleButtonType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472402);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472401);
        this.mColumnCount = this.mContext.getResources().getInteger(34275370);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275371);
    }

    private void initCardSingleButtonType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472394);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472393);
        this.mColumnCount = this.mContext.getResources().getInteger(34275362);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275363);
    }

    private void initCardDoubleButtonType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472396);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472395);
        this.mColumnCount = this.mContext.getResources().getInteger(34275365);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275366);
    }

    private void initToastType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472418);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472417);
        this.mColumnCount = this.mContext.getResources().getInteger(34275386);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275387);
    }

    private void initMenuType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472412);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472411);
        this.mColumnCount = this.mContext.getResources().getInteger(34275380);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275381);
    }

    private void initSmallToolbarType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472416);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472415);
        this.mColumnCount = this.mContext.getResources().getInteger(34275384);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275385);
    }

    private void initLargeToolbarType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472406);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472405);
        this.mColumnCount = this.mContext.getResources().getInteger(34275374);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275375);
    }

    private void initSmallBottomTabType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472414);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472413);
        this.mColumnCount = this.mContext.getResources().getInteger(34275382);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275383);
    }

    private void initLargeBottomTabType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472404);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472403);
        this.mColumnCount = this.mContext.getResources().getInteger(34275372);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275373);
    }

    private void initCardType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472398);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472397);
        this.mColumnCount = this.mContext.getResources().getInteger(34275364);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275367);
    }

    private void initBubbleType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472390);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472389);
        this.mColumnCount = this.mContext.getResources().getInteger(34275358);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275359);
    }

    private void initLowerSmallDialogType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472410);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472409);
        this.mColumnCount = this.mContext.getResources().getInteger(34275378);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275379);
    }

    private void initLowerLargeDialogType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472408);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472407);
        this.mColumnCount = this.mContext.getResources().getInteger(34275376);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275377);
    }

    private void initUpperSmallDialogType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472422);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472421);
        this.mColumnCount = this.mContext.getResources().getInteger(34275391);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275392);
    }

    private void initUpperLargeDialogType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472420);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472419);
        this.mColumnCount = this.mContext.getResources().getInteger(34275389);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275390);
    }

    private void initBottomSheetType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472388);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472387);
        this.mColumnCount = this.mContext.getResources().getInteger(34275356);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275357);
    }

    private void initDefaultType() {
        this.mMargin = this.mContext.getResources().getDimensionPixelOffset(34472400);
        this.mGutter = this.mContext.getResources().getDimensionPixelOffset(34472399);
        this.mColumnCount = this.mContext.getResources().getInteger(34275368);
        this.mMaxColumnCount = this.mContext.getResources().getInteger(34275369);
    }

    private void initResource(float breakPoint, float density) {
        int columnIndex;
        if (breakPoint >= 840.0f) {
            this.mTotalColumn = 12;
            columnIndex = 2;
        } else if (breakPoint >= 520.0f) {
            this.mTotalColumn = 8;
            columnIndex = 1;
        } else {
            this.mTotalColumn = 4;
            columnIndex = 0;
        }
        int i = this.mColumnType;
        if (i >= 12 && i <= 16) {
            switchDialogType();
        }
        initDefineByType(this.mColumnType, columnIndex, density);
    }

    private void initResource() {
        Context context = this.mContext;
        if (context != null) {
            this.mTotalColumn = context.getResources().getInteger(34275388);
            int i = this.mColumnType;
            if (i >= 12 && i <= 16) {
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
                initLowerSmallDialogType();
                return;
            case 14:
                initLowerLargeDialogType();
                return;
            case 15:
                initUpperSmallDialogType();
                return;
            case 16:
                initUpperLargeDialogType();
                return;
            case 17:
                initCardSingleButtonType();
                return;
            case 18:
                initCardDoubleButtonType();
                return;
            case 19:
                initBottomSheetType();
                return;
        }
    }

    private void switchDialogType() {
        if (Double.compare(getWindowSizeByInch(), SCREEN_SIZE_12_INCH) < 0) {
            switchDialogTypeByInch(13, 14);
        } else {
            switchDialogTypeByInch(15, 16);
        }
    }

    private void switchDialogTypeByInch(int smallType, int largeType) {
        int i = this.mTotalColumn;
        if (i != 4) {
            if (i != 8) {
                if (i == 12) {
                    if (this.mWidthPixel * 3 > this.mHeightPixel * 4) {
                        this.mColumnType = smallType;
                    } else {
                        this.mColumnType = largeType;
                    }
                }
            } else if (this.mWidthPixel * 4 > this.mHeightPixel * 3) {
                this.mColumnType = smallType;
            } else {
                this.mColumnType = largeType;
            }
        } else if (this.mWidthPixel * 16 > this.mHeightPixel * 9) {
            this.mColumnType = smallType;
        } else {
            this.mColumnType = largeType;
        }
    }

    private double getWindowSizeByInch() {
        DisplayMetrics displayMetrics = this.mContext.getResources().getDisplayMetrics();
        float width = 0.0f;
        float height = 0.0f;
        if (displayMetrics.xdpi == 0.0f || displayMetrics.ydpi == 0.0f) {
            Log.w(TAG, "displayMetrics.xdpi or displayMetrics.ydpi get failed.");
        } else {
            width = ((float) this.mWidthPixel) / displayMetrics.xdpi;
            height = ((float) this.mHeightPixel) / displayMetrics.ydpi;
        }
        return Math.sqrt((double) ((width * width) + (height * height)));
    }

    public int getColumnType() {
        return this.mColumnType;
    }

    public void setColumnType(int columnType) {
        this.mColumnType = columnType;
        this.mIsUserDefine = false;
        if (this.mContext != null && this.mColumnPolicy != null) {
            initResource();
            this.mColumnPolicy.setColumnType(this.mColumnType);
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
        String[] strArr;
        this.mContext = context;
        if (this.mContext == null) {
            return getSuggestWidth();
        }
        refreshScreenInfo();
        if (!this.mIsUserDefine || (strArr = this.mDefineArray) == null || strArr.length != 3) {
            initDisplayInfo();
        } else {
            setColumnDataByUserDefine(this.mWidthPixel, Build.VERSION.SDK_INT > 28);
        }
        return getSuggestWidth();
    }

    public int updateConfigation(Context context, int width, int height, float density) {
        String[] strArr;
        if (context == null || width <= 0 || height <= 0 || Float.compare(density, 0.0f) <= 0) {
            Log.w(TAG, "width and density should not below to zero!");
            return getSuggestWidth();
        }
        this.mContext = context;
        this.mWidthPixel = width;
        this.mHeightPixel = height;
        this.mDensity = density;
        if (!this.mIsUserDefine || (strArr = this.mDefineArray) == null || strArr.length != 3) {
            initResource(((float) width) / density, density);
            refreshDisplayInfo();
        } else {
            setColumnDataByUserDefine(this.mWidthPixel, false);
        }
        return getSuggestWidth();
    }

    private int dp2px(int dp, float density) {
        return (int) ((((float) dp) * density) + 0.5f);
    }
}

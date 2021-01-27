package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidhwext.R;
import com.huawei.android.os.VibratorEx;
import com.huawei.android.view.ViewEx;
import huawei.android.hwutil.SectionLocaleUtils;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AlphaIndexerListView extends View {
    public static final String BULLET_CHAR = "•";
    private static final int COUNT_ALPHABET_THREE = 3;
    private static final int COUNT_ALPHABET_TWO = 2;
    private static final boolean DEBUG = false;
    private static final int DEFAULT_COLLECT_LENGTH = 10;
    private static final long DELAY_MILLIS = 800;
    public static final String DIGIT_LABEL = "#";
    private static final int HELP_ALPHABET = 2;
    private static final int INVALID_INDEX_POSITION = -1;
    private static final int LANDSCAPE_ALPHA_COUNT_MAX = 18;
    private static final int LANDSCAPE_ALPHA_COUNT_MIN = 6;
    private static final int LANDSCAPE_ALPHA_COUNT_MORE = 10;
    private static final int LANDSCAPE_ALPHA_COUNT_REDUCE = 14;
    private static final int LENGTH = 2;
    private static final int LESS_GAP = 4;
    private static final float OFFSET_TWO = 2.0f;
    private static final int PORTRAIT_ALPHA_COUNT_MAX = 26;
    private static final String TAG = "AlphaIndexerListView";
    public static final int TOO_FEW_ALPHA_COUNT = 6;
    private String[] mAlphaBets;
    private int mAlphaIndexerMargin;
    private float mAlphaTextSize;
    private List<String> mAlphabets;
    private int mBottomGap;
    private int mChoose;
    private Context mContext;
    private String mCurrentString;
    private Map<String, String> mDefaultDescriptionMap;
    Runnable mDismissRunnable;
    private String[] mEnAlphaBets;
    private String mFirstEnglishAlpha;
    private int mFirstNativeIndex;
    private List<String> mFullAlphabets;
    private float mGapBetweenAlpha;
    private Handler mHandler;
    private int mHeight;
    private int mInactiveAlphaColor;
    private boolean mIsDigitLast;
    private boolean mIsHasNativeIndexer;
    private boolean mIsInit;
    private boolean mIsLandscape;
    private boolean mIsNativeIndexerShown;
    private boolean mIsShowPopup;
    private boolean mIsSupportVibrator;
    private float mLastY;
    private int mLessBottomGap;
    private ListView mListView;
    private String mLstEnglishAlpha;
    private OnItemClickListener mOnItemClickListener;
    private Paint mPaint;
    private Drawable mPopupBgDrawable;
    private TextView mPopupText;
    private int mPopupTextColor;
    private PopupWindow mPopupWindow;
    private String mPreviousString;
    private String mSectionText;
    private int mSelectedAlphaColor;
    private int mSpecWidth;
    private int mTopGap;
    private VibratorEx mVibratorEx;

    public interface OnItemClickListener {
        void onItemClick(String str, int i);
    }

    public AlphaIndexerListView(Context context) {
        this(context, null);
    }

    public AlphaIndexerListView(Context context, AttributeSet attrs) {
        this(context, attrs, 33620118);
    }

    public AlphaIndexerListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDismissRunnable = new Runnable() {
            /* class huawei.android.widget.AlphaIndexerListView.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                if (AlphaIndexerListView.this.mPopupWindow != null) {
                    AlphaIndexerListView.this.mPopupWindow.dismiss();
                }
            }
        };
        this.mFirstEnglishAlpha = "A";
        this.mLstEnglishAlpha = "Z";
        this.mFullAlphabets = new ArrayList(10);
        this.mAlphabets = new ArrayList(10);
        this.mFirstNativeIndex = -1;
        this.mChoose = -1;
        this.mPopupTextColor = -1;
        this.mLastY = 0.0f;
        this.mIsLandscape = DEBUG;
        this.mIsInit = DEBUG;
        this.mIsDigitLast = DEBUG;
        this.mIsShowPopup = true;
        this.mIsSupportVibrator = DEBUG;
        this.mPaint = new Paint();
        this.mHandler = new Handler();
        this.mVibratorEx = new VibratorEx();
        this.mDefaultDescriptionMap = new HashMap();
        init(context);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AlphaIndexerListView, defStyle, 0);
        this.mPopupBgDrawable = typedArray.getDrawable(0);
        this.mPopupTextColor = typedArray.getColor(1, -16777216);
        this.mInactiveAlphaColor = typedArray.getColor(2, -16777216);
        this.mSelectedAlphaColor = typedArray.getColor(3, -16776961);
        typedArray.recycle();
    }

    private void init(Context context) {
        this.mContext = context;
        Resources res = ResLoader.getInstance().getResources(context);
        int bucketIndex = SectionLocaleUtils.getInstance().getBucketIndex(this.mFirstEnglishAlpha);
        boolean z = DEBUG;
        this.mIsHasNativeIndexer = bucketIndex != 1;
        if (res.getConfiguration().orientation == 2) {
            z = true;
        }
        this.mIsLandscape = z;
        this.mLessBottomGap = ResLoaderUtil.getDimensionPixelSize(context, "alphaindexer_listview_bottom_gap");
        this.mBottomGap = ResLoaderUtil.getDimensionPixelSize(context, "alphaindexer_bottom_margin");
        this.mSpecWidth = ResLoaderUtil.getDimensionPixelSize(getContext(), "alphaindexer_view_width");
        this.mAlphaTextSize = (float) ResLoaderUtil.getDimensionPixelSize(context, "emui_text_size_body3");
        this.mIsSupportVibrator = this.mVibratorEx.isSupportHwVibrator("haptic.slide.type6");
        setContentDescription(res.getString(33685528));
        setAccessibilityDelegate(new AlphaIndexerAccessibilityDelegate());
        for (String alpha : HwAlphaIndexResourceManager.getRootPortraitDisplayAlphaIndex()) {
            this.mDefaultDescriptionMap.put(alpha, alpha.toLowerCase(Locale.ENGLISH));
        }
    }

    public void buildIndexer(boolean isLand, boolean isDigitLast) {
        String[] tempAlphaArr;
        int sizeNum = getSizeNum();
        String[] tempAlphaArrPortrait = (String[]) HwAlphaIndexResourceManager.getInstance().getPortraitDisplayAlphaIndex().toArray(new String[0]);
        String[] tempAlphaArrLand = (String[]) HwAlphaIndexResourceManager.getInstance().getLandscapeDisplayAlphaIndex().toArray(new String[0]);
        if (this.mIsHasNativeIndexer) {
            hasNativeIndexer(isLand, isDigitLast, sizeNum, tempAlphaArrPortrait, tempAlphaArrLand);
        } else {
            if (sizeNum == PORTRAIT_ALPHA_COUNT_MAX) {
                if (isLand) {
                    tempAlphaArr = tempAlphaArrLand;
                } else {
                    tempAlphaArr = tempAlphaArrPortrait;
                }
            } else if (sizeNum == 18) {
                tempAlphaArr = tempAlphaArrLand;
            } else {
                HwAlphaIndexResourceManager.getInstance();
                tempAlphaArr = (String[]) getDisplayFromComplete(HwAlphaIndexResourceManager.populateBulletAlphaIndex(sizeNum, Arrays.asList(tempAlphaArrPortrait))).toArray(new String[0]);
            }
            this.mAlphaBets = new String[(tempAlphaArr.length + 1)];
            if (isDigitLast) {
                String[] strArr = this.mAlphaBets;
                strArr[strArr.length - 1] = DIGIT_LABEL;
                System.arraycopy(tempAlphaArr, 0, strArr, 0, tempAlphaArr.length);
            } else {
                String[] strArr2 = this.mAlphaBets;
                strArr2[0] = DIGIT_LABEL;
                System.arraycopy(tempAlphaArr, 0, strArr2, 1, tempAlphaArr.length);
            }
        }
        if (!this.mIsHasNativeIndexer || this.mIsNativeIndexerShown) {
            this.mAlphabets = new ArrayList(Arrays.asList(this.mAlphaBets));
        } else {
            this.mAlphabets = new ArrayList(Arrays.asList(this.mEnAlphaBets));
        }
        this.mIsLandscape = isLand;
        invalidate();
    }

    private void hasNativeIndexer(boolean isLand, boolean isDigitLast, int sizeNum, String[] tempAlphaArrPortrait, String[] tempAlphaArrLand) {
        String[] enTempAlphaArr;
        String[] tempAlphaArr;
        String[] enTempAlphaArrLand = (String[]) HwAlphaIndexResourceManager.getRootLandscapeDisplayAlphaIndex().toArray(new String[0]);
        String[] enTempAlphaArrPortrait = (String[]) HwAlphaIndexResourceManager.getRootPortraitDisplayAlphaIndex().toArray(new String[0]);
        if (sizeNum == PORTRAIT_ALPHA_COUNT_MAX) {
            if (isLand) {
                tempAlphaArr = tempAlphaArrLand;
                enTempAlphaArr = enTempAlphaArrLand;
            } else {
                tempAlphaArr = tempAlphaArrPortrait;
                enTempAlphaArr = enTempAlphaArrPortrait;
            }
        } else if (sizeNum == 18) {
            tempAlphaArr = tempAlphaArrLand;
            enTempAlphaArr = enTempAlphaArrLand;
        } else {
            HwAlphaIndexResourceManager.getInstance();
            List<String> list = HwAlphaIndexResourceManager.populateBulletAlphaIndex(sizeNum, Arrays.asList(tempAlphaArrPortrait));
            HwAlphaIndexResourceManager.getInstance();
            enTempAlphaArr = (String[]) getDisplayFromComplete(HwAlphaIndexResourceManager.populateBulletAlphaIndex(sizeNum, Arrays.asList(enTempAlphaArrPortrait))).toArray(new String[0]);
            tempAlphaArr = (String[]) getDisplayFromComplete(list).toArray(new String[0]);
        }
        this.mAlphaBets = new String[(tempAlphaArr.length + 2)];
        this.mEnAlphaBets = new String[(enTempAlphaArr.length + 2)];
        if (isDigitLast) {
            String[] strArr = this.mAlphaBets;
            strArr[strArr.length - 1] = DIGIT_LABEL;
            String[] strArr2 = this.mEnAlphaBets;
            strArr2[strArr2.length - 1] = DIGIT_LABEL;
            System.arraycopy(tempAlphaArr, 0, strArr, 0, tempAlphaArr.length);
            this.mFirstNativeIndex = 0;
            String[] strArr3 = this.mEnAlphaBets;
            strArr3[0] = this.mAlphaBets[this.mFirstNativeIndex];
            System.arraycopy(enTempAlphaArr, 0, strArr3, 1, enTempAlphaArr.length);
            String[] strArr4 = this.mAlphaBets;
            String[] strArr5 = this.mEnAlphaBets;
            strArr4[strArr4.length - 2] = strArr5[strArr5.length - 2];
            return;
        }
        String[] strArr6 = this.mAlphaBets;
        strArr6[0] = DIGIT_LABEL;
        this.mEnAlphaBets[0] = DIGIT_LABEL;
        System.arraycopy(tempAlphaArr, 0, strArr6, 1, tempAlphaArr.length);
        this.mFirstNativeIndex = 1;
        String[] strArr7 = this.mEnAlphaBets;
        strArr7[1] = this.mAlphaBets[this.mFirstNativeIndex];
        System.arraycopy(enTempAlphaArr, 0, strArr7, 2, enTempAlphaArr.length);
        String[] strArr8 = this.mAlphaBets;
        String[] strArr9 = this.mEnAlphaBets;
        strArr8[strArr8.length - 1] = strArr9[strArr9.length - 1];
    }

    private int getSizeNum() {
        int i = this.mHeight;
        int i2 = this.mBottomGap;
        int sizeNum = (int) (((float) ((((i - i2) - i2) - getPaddingBottom()) - getPaddingTop())) / this.mAlphaTextSize);
        if (sizeNum > PORTRAIT_ALPHA_COUNT_MAX) {
            return PORTRAIT_ALPHA_COUNT_MAX;
        }
        if (sizeNum <= PORTRAIT_ALPHA_COUNT_MAX && sizeNum > 12) {
            return 18;
        }
        if (sizeNum <= 12 && sizeNum > 10) {
            return LANDSCAPE_ALPHA_COUNT_REDUCE;
        }
        if (sizeNum > 10 || sizeNum <= 8) {
            return 6;
        }
        return 10;
    }

    private List<String> getDisplayFromComplete(List<String> completeList) {
        List<String> rets = new ArrayList<>(10);
        for (String item : completeList) {
            if (item.split(" ").length > 1) {
                rets.add(BULLET_CHAR);
            } else {
                rets.add(item);
            }
        }
        return rets;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        this.mHeight = getMeasuredHeight();
        buildIndexer(this.mIsLandscape, this.mIsDigitLast);
    }

    public void setListViewAttachTo(ListView listView) {
        this.mListView = listView;
        if (!this.mIsInit) {
            Adapter adapter = this.mListView.getAdapter();
            if (adapter instanceof HwSortedTextListAdapter) {
                this.mIsDigitLast = ((HwSortedTextListAdapter) adapter).isDigitLast();
            }
            buildIndexer(this.mIsLandscape, this.mIsDigitLast);
            this.mIsInit = true;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        float f;
        float f2;
        super.onDraw(canvas);
        calculateVariables();
        int right = getRight();
        int left = getLeft();
        int viewWidth = right - left;
        int i = this.mSpecWidth;
        if (viewWidth > i) {
            right -= (viewWidth - i) / 2;
            left += (viewWidth - i) / 2;
        }
        setRight(right);
        setLeft(left);
        int size = this.mAlphabets.size();
        int highlightPos = this.mChoose;
        if (highlightPos == -1) {
            highlightPos = getHighlightPos();
        }
        int width = getWidth();
        for (int i2 = 0; i2 < size; i2++) {
            this.mPaint.setColor(this.mInactiveAlphaColor);
            this.mPaint.setAntiAlias(true);
            this.mPaint.setTextSize(this.mAlphaTextSize);
            this.mPaint.setTypeface(Typeface.create("sans-serif", 0));
            if (i2 == highlightPos) {
                this.mPaint.setColor(this.mSelectedAlphaColor);
                this.mPaint.setTypeface(Typeface.create("HwChinese-medium", 0));
            }
            String displayChar = this.mAlphabets.get(i2).replace("劃", "");
            float posX = (((float) width) / OFFSET_TWO) - (this.mPaint.measureText(displayChar) / OFFSET_TWO);
            int i3 = this.mTopGap;
            if (i3 == this.mBottomGap) {
                float f3 = this.mAlphaTextSize;
                float f4 = this.mGapBetweenAlpha;
                f = ((float) (i2 + 1)) * (f3 + f4);
                f2 = ((float) i3) - f4;
            } else {
                f = ((float) (i2 + 1)) * (this.mAlphaTextSize + this.mGapBetweenAlpha);
                f2 = (float) i3;
            }
            canvas.drawText(displayChar, posX, f + f2, this.mPaint);
            this.mPaint.reset();
        }
    }

    public boolean ifShowPopup() {
        return this.mIsShowPopup;
    }

    public void setShowPopup(boolean isShowPopup) {
        this.mIsShowPopup = isShowPopup;
    }

    private int getHighlightPos() {
        if (this.mSectionText == null) {
            return -1;
        }
        int len = this.mAlphabets.size();
        for (int i = 0; i < len; i++) {
            if (equalsChar(this.mAlphabets.get(i), this.mSectionText, i)) {
                return i;
            }
        }
        return -1;
    }

    private void calculateVariables() {
        int i;
        int size = this.mAlphabets.size();
        if (size < 6 && (i = this.mLessBottomGap) > 0) {
            this.mBottomGap = (6 - size) * 4 * i;
        }
        ListView listView = this.mListView;
        if (listView != null) {
            this.mAlphaIndexerMargin = listView.getHeight() - this.mHeight;
            int bottom = this.mBottomGap;
            int totalHeight = (this.mListView.getHeight() - bottom) - this.mAlphaIndexerMargin;
            int count = size - 1;
            if (count != 0) {
                this.mGapBetweenAlpha = ((((float) totalHeight) - (((float) size) * this.mAlphaTextSize)) - ((float) bottom)) / ((float) count);
            }
            float f = this.mGapBetweenAlpha;
            this.mTopGap = ((float) bottom) > f ? (int) (((float) bottom) - f) : bottom;
        }
    }

    @Override // android.view.View
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event == null) {
            return DEBUG;
        }
        int action = event.getAction();
        float eventY = event.getY();
        boolean isWithinRange = eventY < ((float) this.mTopGap) || eventY > ((float) (getHeight() - this.mTopGap));
        if ((action == 0 || action == 2) && isWithinRange) {
            return true;
        }
        int height = getHeight();
        int i = this.mTopGap;
        int divisor = height - (i * 2);
        if (divisor == 0) {
            return true;
        }
        int position = (int) (((eventY - ((float) i)) / ((float) divisor)) * ((float) this.mAlphabets.size()));
        if (this.mAlphabets.size() == 0) {
            return true;
        }
        if (action == 0) {
            if (position >= 0 && position < this.mAlphabets.size()) {
                performItemClicked(position);
                invalidate();
            }
            if (this.mIsSupportVibrator) {
                this.mLastY = event.getY();
            }
        } else if (action == 1) {
            dismissPopup();
            this.mChoose = -1;
            invalidate();
        } else if (action == 2) {
            actionMove(event, position);
        } else if (action == 3) {
            dismissPopup();
            return DEBUG;
        }
        return true;
    }

    private void actionMove(MotionEvent event, int position) {
        if (position >= 0 && position < this.mAlphabets.size()) {
            performItemClicked(position);
            invalidate();
            if (this.mIsSupportVibrator && isHapticFeedbackEnabled()) {
                String str = this.mPreviousString;
                if (str == null || !str.equals(this.mCurrentString)) {
                    ViewEx.performHwHapticFeedback(this, 10, 0);
                    this.mPreviousString = this.mCurrentString;
                }
            }
        }
    }

    private void performItemClicked(int item) {
        OnItemClickListener onItemClickListener = this.mOnItemClickListener;
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(this.mAlphabets.get(item), item);
        }
    }

    private void toggleIndexer(boolean isNative) {
        ArrayList arrayList;
        Animation animation;
        this.mAlphabets.clear();
        if (isNative) {
            arrayList = new ArrayList(Arrays.asList(this.mEnAlphaBets));
        } else {
            arrayList = new ArrayList(Arrays.asList(this.mAlphaBets));
        }
        this.mAlphabets = arrayList;
        this.mIsNativeIndexerShown = !isNative;
        int bottom2topAnimId = ResLoaderUtil.getInstance().getIdentifier(getContext(), "anim", "translate_bottom2top");
        int top2bottomAnimId = ResLoaderUtil.getInstance().getIdentifier(getContext(), "anim", "translate_top2bottom");
        if (isNative) {
            animation = AnimationUtils.loadAnimation(this.mContext, bottom2topAnimId);
        } else {
            animation = AnimationUtils.loadAnimation(this.mContext, top2bottomAnimId);
        }
        setAnimation(animation);
        calculateVariables();
        startAnimation(animation);
    }

    public Object[] getSections() {
        ListView listView = this.mListView;
        if (listView != null) {
            Adapter adapter = listView.getAdapter();
            if (adapter instanceof HwSortedTextListAdapter) {
                return ((HwSortedTextListAdapter) adapter).getSections();
            }
        }
        return new String[0];
    }

    private boolean isInNativeSection() {
        if (this.mSectionText == null) {
            return DEBUG;
        }
        Object[] sections = getSections();
        String strDetect = this.mSectionText;
        if (sections != null && DIGIT_LABEL.equals(strDetect)) {
            if (this.mIsDigitLast) {
                return DEBUG;
            }
            if (0 + 1 < sections.length) {
                Object object = sections[0 + 1];
                if (object instanceof String) {
                    strDetect = (String) object;
                }
            }
        }
        if (Collator.getInstance().compare(strDetect, this.mFirstEnglishAlpha) < 0) {
            return true;
        }
        return DEBUG;
    }

    private void updateIndexer() {
        if (!this.mIsHasNativeIndexer) {
            return;
        }
        if (isInNativeSection()) {
            if (!this.mIsNativeIndexerShown) {
                toggleIndexer(DEBUG);
            }
        } else if (this.mIsNativeIndexerShown) {
            toggleIndexer(true);
        }
    }

    public boolean needSwitchIndexer(int pos) {
        if (!this.mIsHasNativeIndexer) {
            return DEBUG;
        }
        if (pos == this.mFirstNativeIndex && !this.mIsNativeIndexerShown) {
            return true;
        }
        if (pos != (this.mIsDigitLast ? this.mAlphabets.size() - 2 : this.mAlphabets.size() - 1) || !this.mIsNativeIndexerShown) {
            return DEBUG;
        }
        return true;
    }

    public void showPopup(String text) {
        int popupHorizontalPos;
        this.mPreviousString = this.mCurrentString;
        this.mCurrentString = text;
        if (this.mIsShowPopup) {
            this.mHandler.removeCallbacks(this.mDismissRunnable);
            int popupHeight = ResLoaderUtil.getDimensionPixelSize(getContext(), "alphaindexer_popup_height");
            if (this.mPopupWindow == null) {
                this.mPopupText = new TextView(getContext());
                this.mPopupText.setTextSize(0, (float) ResLoaderUtil.getDimensionPixelSize(getContext(), "alphaindexer_popup_fontsize"));
                Drawable drawable = this.mPopupBgDrawable;
                if (drawable != null) {
                    this.mPopupText.setBackground(drawable);
                }
                this.mPopupText.setTextColor(this.mPopupTextColor);
                this.mPopupText.setTypeface(Typeface.DEFAULT_BOLD);
                this.mPopupText.setGravity(17);
                this.mPopupWindow = new PopupWindow(this.mPopupText, popupHeight, popupHeight);
                this.mPopupWindow.setAnimationStyle(ResLoaderUtil.getInstance().getIdentifier(getContext(), "style", "Animation_PopupWindow_Emui"));
            }
            boolean isScrolling = this.mChoose == -1 && text != null;
            int i = this.mChoose;
            boolean isShow = i != -1 && i < this.mAlphabets.size() && equalsChar(this.mAlphabets.get(this.mChoose), text, this.mChoose);
            if (isScrolling || isShow) {
                this.mPopupText.setText(text);
                int horizontalOffset = ResLoaderUtil.getDimensionPixelSize(getContext(), "alphaindexer_popup_horizontal_offset");
                int[] location = new int[2];
                getLocationInWindow(location);
                int viewLeft = location[0];
                int viewRight = location[0] + this.mSpecWidth;
                if (getParent().getLayoutDirection() == 1) {
                    popupHorizontalPos = viewRight + horizontalOffset;
                } else {
                    popupHorizontalPos = (viewLeft - horizontalOffset) - popupHeight;
                }
                this.mPopupWindow.showAtLocation(getRootView(), 8388627, popupHorizontalPos, 0);
            }
        }
    }

    public void showPopup() {
        showPopup(this.mSectionText);
    }

    public void dismissPopup() {
        if (this.mIsShowPopup) {
            this.mHandler.postDelayed(this.mDismissRunnable, DELAY_MILLIS);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        PopupWindow popupWindow = this.mPopupWindow;
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    public boolean hasNativeIndexer() {
        return this.mIsHasNativeIndexer;
    }

    public boolean isNativeIndexerShow() {
        return this.mIsNativeIndexerShown;
    }

    public Drawable getPopupWindowBgDrawable() {
        return this.mPopupBgDrawable;
    }

    public void setPopupWindowBgDrawable(Drawable drawable) {
        this.mPopupBgDrawable = drawable;
    }

    public void setInactiveAlphaColor(int color) {
        this.mInactiveAlphaColor = color;
    }

    public void setSelectedAlphaColor(int color) {
        this.mSelectedAlphaColor = color;
    }

    public void setPopupTextColor(int color) {
        this.mPopupTextColor = color;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOverLayInfo(String sectionName) {
        if (sectionName == null) {
            Log.w(TAG, "setOverLayInfo: sectionName is null!");
        } else if ("".equals(sectionName)) {
            this.mSectionText = "@";
        } else {
            if (!sectionName.equals(this.mSectionText)) {
                this.mSectionText = sectionName;
                sendAccessibilityEvent(16384);
            } else {
                this.mSectionText = sectionName;
            }
            updateIndexer();
        }
    }

    public void setOverLayInfo(int sectionPos, String sectionName) {
        this.mChoose = sectionPos;
        setOverLayInfo(sectionName);
    }

    public boolean equalsChar(String str1, String str2) {
        if (str1.length() != str2.length()) {
            return DEBUG;
        }
        Collator coll = Collator.getInstance();
        coll.setStrength(0);
        return coll.equals(str1, str2);
    }

    public boolean equalsChar(String str1, String str2, int index) {
        if (str1 == null || str2 == null || index < 0 || index >= this.mAlphabets.size()) {
            return DEBUG;
        }
        if (!str1.equals(BULLET_CHAR)) {
            return equalsChar(str1, str2);
        }
        this.mFullAlphabets.clear();
        if (!this.mIsDigitLast) {
            this.mFullAlphabets.add(DIGIT_LABEL);
        }
        int sizeNum = getSizeNum();
        List<String> alphaPortraits = HwAlphaIndexResourceManager.getInstance().getPortraitCompleteAlphaIndex();
        HwAlphaIndexResourceManager.getInstance();
        List<String> rootAlphaPortraits = HwAlphaIndexResourceManager.getRootPortraitDisplayAlphaIndex();
        List<String> alphaLands = HwAlphaIndexResourceManager.getInstance().getLandscapeCompleteAlphaIndex();
        if (this.mIsHasNativeIndexer) {
            hasNativeIndexer(sizeNum, alphaPortraits, rootAlphaPortraits, alphaLands);
        } else if (sizeNum == PORTRAIT_ALPHA_COUNT_MAX) {
            if (this.mIsLandscape) {
                this.mFullAlphabets.addAll(alphaLands);
            } else {
                this.mFullAlphabets.addAll(alphaPortraits);
            }
        } else if (sizeNum == 18) {
            this.mFullAlphabets.addAll(alphaLands);
        } else {
            List<String> list = this.mFullAlphabets;
            HwAlphaIndexResourceManager.getInstance();
            list.addAll(HwAlphaIndexResourceManager.populateBulletAlphaIndex(sizeNum, alphaPortraits));
        }
        if (this.mIsDigitLast) {
            this.mFullAlphabets.add(DIGIT_LABEL);
        }
        for (String bullet : this.mFullAlphabets.get(index).split(" ")) {
            if (equalsChar(bullet, str2)) {
                return true;
            }
        }
        return DEBUG;
    }

    private void hasNativeIndexer(int sizeNum, List<String> alphaPortraits, List<String> rootAlphaPortraits, List<String> alphaLands) {
        if (this.mIsNativeIndexerShown) {
            if (sizeNum == PORTRAIT_ALPHA_COUNT_MAX) {
                if (this.mIsLandscape) {
                    this.mFullAlphabets.addAll(alphaLands);
                } else {
                    this.mFullAlphabets.addAll(alphaPortraits);
                }
            } else if (sizeNum == 18) {
                this.mFullAlphabets.addAll(alphaLands);
            } else {
                List<String> list = this.mFullAlphabets;
                HwAlphaIndexResourceManager.getInstance();
                list.addAll(HwAlphaIndexResourceManager.populateBulletAlphaIndex(sizeNum, alphaPortraits));
            }
            this.mFullAlphabets.add(this.mLstEnglishAlpha);
            return;
        }
        this.mFullAlphabets.add(this.mAlphaBets[this.mFirstNativeIndex]);
        if (sizeNum == PORTRAIT_ALPHA_COUNT_MAX) {
            if (this.mIsLandscape) {
                this.mFullAlphabets.addAll(HwAlphaIndexResourceManager.getRootLandscapeCompleteAlphaIndex());
            } else {
                this.mFullAlphabets.addAll(rootAlphaPortraits);
            }
        } else if (sizeNum == 18) {
            this.mFullAlphabets.addAll(HwAlphaIndexResourceManager.getRootLandscapeCompleteAlphaIndex());
        } else {
            List<String> list2 = this.mFullAlphabets;
            HwAlphaIndexResourceManager.getInstance();
            list2.addAll(HwAlphaIndexResourceManager.populateBulletAlphaIndex(sizeNum, rootAlphaPortraits));
        }
    }

    public int getPositionBySection(String sectionName) {
        if (sectionName == null) {
            return -1;
        }
        int len = this.mAlphabets.size();
        for (int i = 0; i < len; i++) {
            if (equalsChar(this.mAlphabets.get(i), sectionName, i)) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public class AlphaIndexerAccessibilityDelegate extends View.AccessibilityDelegate {
        private AlphaIndexerAccessibilityDelegate() {
        }

        @Override // android.view.View.AccessibilityDelegate
        public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
            if (host != null && event != null) {
                super.onInitializeAccessibilityEvent(host, event);
                if (AlphaIndexerListView.this.mSectionText != null) {
                    event.getText().add(AlphaIndexerListView.this.mSectionText);
                    event.setContentDescription((String) AlphaIndexerListView.this.mDefaultDescriptionMap.get(AlphaIndexerListView.this.mSectionText));
                }
            }
        }
    }
}

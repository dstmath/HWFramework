package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Pools;
import android.view.View;
import androidhwext.R;
import java.util.ArrayList;

public class ProgressButton extends View {
    private static final int DEFAULT_MAX_VALUE = 100;
    private static final float DICHOTOMY_SIZE = 2.0f;
    private static final String ELLIPSIS = "...";
    private static final int MAX_LEVEL = 10000;
    private static final String TAG = "ProgressButton";
    private boolean mIsAttached;
    private boolean mIsRefreshPosted;
    private int mMax;
    private int mMaxHeight;
    private int mMaxWidth;
    private int mMinHeight;
    private int mMinWidth;
    private Paint mPaint;
    private int mProgress;
    private Drawable mProgressDrawable;
    private final ArrayList<RefreshData> mRefreshData;
    private RefreshProgressRunnable mRefreshProgressRunnable;
    private CharSequence mText;
    private int mTextColor;
    private int mTextPaddingLeft;
    private int mTextPaddingRight;
    private int mTextSize;
    private long mUiThreadId;

    public ProgressButton(Context context) {
        this(context, null);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 16842871);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyle, int styleRes) {
        super(context, attrs, defStyle, styleRes);
        this.mRefreshData = new ArrayList<>(0);
        this.mUiThreadId = Thread.currentThread().getId();
        initProgressButton(context);
        initInternal();
        initAttrs(context, attrs, defStyle, styleRes);
    }

    private synchronized void initProgressButton(Context context) {
        this.mMax = DEFAULT_MAX_VALUE;
        this.mProgress = 0;
        Resources res = context.getResources();
        this.mMaxWidth = res.getDimensionPixelSize(34472818);
        this.mMinWidth = res.getDimensionPixelSize(34472820);
        this.mMaxHeight = res.getDimensionPixelSize(34472817);
        this.mMinHeight = res.getDimensionPixelSize(34472819);
        this.mTextSize = res.getDimensionPixelSize(34472824);
        this.mTextPaddingLeft = res.getDimensionPixelSize(34472822);
        this.mTextPaddingRight = res.getDimensionPixelSize(34472823);
        this.mProgressDrawable = res.getDrawable(33752342);
        this.mTextColor = res.getColor(33883186);
    }

    private void initInternal() {
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setTextSize((float) getTextSize());
        this.mPaint.setColor(getTextColor());
        setClickable(true);
    }

    private synchronized void initAttrs(Context context, AttributeSet attrs, int defStyle, int styleRes) {
        if (attrs != null) {
            initTextAttrs(context, attrs, defStyle, styleRes);
            initProgressAttrs(context, attrs);
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton, defStyle, styleRes);
            if (array != null) {
                this.mTextPaddingLeft = array.getDimensionPixelSize(0, this.mTextPaddingLeft);
                this.mTextPaddingRight = array.getDimensionPixelSize(1, this.mTextPaddingRight);
                array.recycle();
            }
        }
    }

    private void initTextAttrs(Context context, AttributeSet attrs, int defStyle, int styleRes) {
        TypedArray typedArrayText = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.TextView, defStyle, styleRes);
        if (typedArrayText != null) {
            int indexCount = typedArrayText.getIndexCount();
            for (int i = 0; i < indexCount; i++) {
                int attr = typedArrayText.getIndex(i);
                if (attr == 2) {
                    setTextSize(typedArrayText.getDimensionPixelSize(attr, this.mTextSize));
                } else if (attr == 5) {
                    setTextColor(typedArrayText.getColor(attr, this.mTextColor));
                } else if (attr == 18) {
                    this.mText = typedArrayText.getText(attr);
                }
            }
            setText(this.mText);
            typedArrayText.recycle();
        }
    }

    private void initProgressAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArrayProgress = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.ProgressBar);
        if (typedArrayProgress != null) {
            int indexCount = typedArrayProgress.getIndexCount();
            for (int i = 0; i < indexCount; i++) {
                int attr = typedArrayProgress.getIndex(i);
                if (attr == 0) {
                    this.mMaxWidth = typedArrayProgress.getDimensionPixelSize(attr, this.mMaxWidth);
                } else if (attr == 1) {
                    this.mMaxHeight = typedArrayProgress.getDimensionPixelSize(attr, this.mMaxHeight);
                } else if (attr == 2) {
                    setMax(typedArrayProgress.getInt(attr, this.mMax));
                } else if (attr == 3) {
                    setProgress(typedArrayProgress.getInt(attr, this.mProgress));
                } else if (attr == 8) {
                    Drawable progressDrawable = typedArrayProgress.getDrawable(attr);
                    if (progressDrawable != null) {
                        setProgressDrawable(progressDrawable);
                    }
                } else if (attr == 11) {
                    this.mMinWidth = typedArrayProgress.getDimensionPixelSize(attr, this.mMinWidth);
                } else if (attr == 12) {
                    this.mMinHeight = typedArrayProgress.getDimensionPixelSize(attr, this.mMinHeight);
                }
            }
            typedArrayProgress.recycle();
        }
    }

    public synchronized Drawable getProgressDrawable() {
        return this.mProgressDrawable;
    }

    public void setProgressDrawable(Drawable drawable) {
        setProgressDrawable(drawable, 0);
    }

    public synchronized void setProgressDrawable(Drawable drawable, int progress) {
        boolean isNeedUpdate;
        if (this.mProgressDrawable == null || drawable == this.mProgressDrawable) {
            isNeedUpdate = false;
        } else {
            this.mProgressDrawable.setCallback(null);
            unscheduleDrawable(this.mProgressDrawable);
            isNeedUpdate = true;
        }
        this.mProgressDrawable = drawable;
        if (drawable != null) {
            drawable.setCallback(this);
        }
        if (isNeedUpdate) {
            updateDrawableBounds(getWidth(), getHeight());
            updateDrawableState();
            this.mProgress = progress < 0 ? 0 : progress > this.mMax ? this.mMax : progress;
            doRefreshProgress(16908301, this.mProgress, false, false);
        } else {
            setProgress(progress);
        }
    }

    public synchronized void setTextPaddingLeft(int textPaddingLeft) {
        this.mTextPaddingLeft = textPaddingLeft;
    }

    public synchronized void setTextPaddingRight(int textPaddingRight) {
        this.mTextPaddingRight = textPaddingRight;
    }

    public synchronized CharSequence getText() {
        return this.mText;
    }

    public synchronized void setText(CharSequence text) {
        if (text == null) {
            this.mText = "";
        } else {
            this.mText = text;
        }
        invalidate();
    }

    public synchronized int getTextSize() {
        return this.mTextSize;
    }

    public synchronized void setTextSize(int textSize) {
        if (this.mTextSize != textSize) {
            this.mTextSize = textSize;
            this.mPaint.setTextSize((float) textSize);
            invalidate();
        }
    }

    public int getTextColor() {
        return this.mTextColor;
    }

    public void setTextColor(int textColor) {
        if (this.mTextColor != textColor) {
            this.mTextColor = textColor;
            getPaint().setColor(textColor);
        }
    }

    public synchronized void setTypeface(Typeface typeFace) {
        this.mPaint.setTypeface(typeFace);
        invalidate();
    }

    public synchronized Paint getPaint() {
        return this.mPaint;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public synchronized boolean verifyDrawable(Drawable who) {
        return who == this.mProgressDrawable || super.verifyDrawable(who);
    }

    @Override // android.view.View
    public synchronized void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mProgressDrawable != null) {
            this.mProgressDrawable.jumpToCurrentState();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void doRefreshProgress(int id, int progress, boolean isFromUser, boolean isCallBackToApp) {
        float scale = this.mMax > 0 ? ((float) progress) / ((float) this.mMax) : 0.0f;
        Drawable drawable = this.mProgressDrawable;
        if (drawable != null) {
            drawable.setLevel((int) (10000.0f * scale));
            invalidate();
        }
        if (isCallBackToApp && id == 16908301) {
            onProgressRefresh(scale, isFromUser);
        }
    }

    private void onProgressRefresh(float scale, boolean isFromUser) {
    }

    public synchronized int getProgress() {
        return this.mProgress;
    }

    public synchronized void setProgress(int progress) {
        setProgress(progress, false);
    }

    private synchronized void setProgress(int progress, boolean isFromUser) {
        if (progress != this.mProgress) {
            this.mProgress = progress < 0 ? 0 : progress > this.mMax ? this.mMax : progress;
            refreshProgress(16908301, this.mProgress, isFromUser);
        }
    }

    private synchronized void refreshProgress(int id, int progress, boolean isFromUser) {
        if (this.mUiThreadId == Thread.currentThread().getId()) {
            doRefreshProgress(id, progress, isFromUser, true);
        } else {
            if (this.mRefreshProgressRunnable == null) {
                this.mRefreshProgressRunnable = new RefreshProgressRunnable();
            }
            this.mRefreshData.add(RefreshData.obtain(id, progress, isFromUser));
            if (this.mIsAttached && !this.mIsRefreshPosted) {
                post(this.mRefreshProgressRunnable);
                this.mIsRefreshPosted = true;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        synchronized (this) {
            if (this.mRefreshData != null) {
                int count = this.mRefreshData.size();
                for (int i = 0; i < count; i++) {
                    RefreshData refreshData = this.mRefreshData.get(i);
                    doRefreshProgress(refreshData.mId, refreshData.mProgress, refreshData.mIsFromUser, true);
                    refreshData.recycle();
                }
                this.mRefreshData.clear();
            }
        }
        this.mIsAttached = true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        RefreshProgressRunnable refreshProgressRunnable = this.mRefreshProgressRunnable;
        if (refreshProgressRunnable != null) {
            removeCallbacks(refreshProgressRunnable);
            synchronized (this) {
                this.mIsRefreshPosted = false;
            }
        }
        super.onDetachedFromWindow();
        this.mIsAttached = false;
    }

    public synchronized int getMax() {
        return this.mMax;
    }

    public synchronized void setMax(int max) {
        if (max != this.mMax) {
            this.mMax = max < 0 ? 0 : max;
            postInvalidate();
            if (this.mProgress > this.mMax) {
                this.mProgress = this.mMax;
            }
            refreshProgress(16908301, this.mProgress, false);
        }
    }

    public final synchronized void incrementProgressBy(int diff) {
        setProgress(this.mProgress + diff);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int width, int height, int oldw, int oldh) {
        updateDrawableBounds(width, height);
    }

    private synchronized void updateDrawableBounds(int width, int height) {
        int right = (width - getPaddingLeft()) - getPaddingRight();
        int bottom = (height - getPaddingTop()) - getPaddingBottom();
        if (this.mProgressDrawable != null) {
            this.mProgressDrawable.setBounds(0, 0, right, bottom);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int drawableWidth = 0;
        int drawableHeight = 0;
        Drawable drawable = this.mProgressDrawable;
        if (drawable != null) {
            drawableWidth = maxInt(this.mMinWidth, minInt(this.mMaxWidth, drawable.getIntrinsicWidth()));
            drawableHeight = maxInt(this.mMinHeight, minInt(this.mMaxHeight, drawable.getIntrinsicHeight()));
        }
        updateDrawableState();
        setMeasuredDimension(resolveSizeAndState(drawableWidth + getPaddingLeft() + getPaddingRight(), widthMeasureSpec, 0), resolveSizeAndState(drawableHeight + getPaddingTop() + getPaddingBottom(), heightMeasureSpec, 0));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable drawable = this.mProgressDrawable;
        if (drawable != null) {
            drawable.draw(canvas);
        }
        drawText(canvas);
    }

    private synchronized void drawText(Canvas canvas) {
        if (this.mText != null && this.mText.length() > 0) {
            CharSequence tempText = this.mText.toString().intern();
            if (tempText != null) {
                if (tempText.length() != 0) {
                    int textWidth = Math.round(this.mPaint.measureText(tempText, 0, tempText.length()));
                    int allowMaxWidth = (((getWidth() - getPaddingLeft()) - getPaddingRight()) - this.mTextPaddingLeft) - this.mTextPaddingRight;
                    if (textWidth > allowMaxWidth) {
                        tempText = getInterceptText(tempText, allowMaxWidth);
                    }
                    Rect rect = new Rect();
                    this.mPaint.getTextBounds(tempText.toString(), 0, tempText.length(), rect);
                    canvas.drawText(tempText, 0, tempText.length(), (float) Math.round((((float) getWidth()) / DICHOTOMY_SIZE) - (((float) ((int) this.mPaint.measureText(tempText, 0, tempText.length()))) / DICHOTOMY_SIZE)), (float) Math.round((((float) getHeight()) / DICHOTOMY_SIZE) - ((float) rect.centerY())), this.mPaint);
                }
            }
        } else if (this.mText != null && this.mText.length() == 0) {
            canvas.drawText(this.mText, 0, 0, (float) 0, (float) 0, this.mPaint);
        }
    }

    private CharSequence getInterceptText(CharSequence text, int maxWidth) {
        int textLenth = getText().length();
        int tempMax = maxWidth - Math.round(this.mPaint.measureText(ELLIPSIS, 0, ELLIPSIS.length()));
        if (tempMax > 0) {
            int i = 0;
            while (i < textLenth && Math.round(this.mPaint.measureText(text, 0, i)) <= tempMax) {
                i++;
            }
            return text.toString().substring(0, i) + ELLIPSIS;
        }
        int i2 = 0;
        while (i2 < textLenth && Math.round(this.mPaint.measureText(text, 0, i2)) <= maxWidth) {
            i2++;
        }
        return text.toString().substring(0, i2);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void drawableStateChanged() {
        super.drawableStateChanged();
        updateDrawableState();
    }

    private synchronized void updateDrawableState() {
        int[] states = getDrawableState();
        if (this.mProgressDrawable != null && this.mProgressDrawable.isStateful()) {
            this.mProgressDrawable.setState(states);
        }
    }

    @Override // android.view.View
    public synchronized Parcelable onSaveInstanceState() {
        SavedState savedState;
        savedState = SavedState.getSavedStateInstance(super.onSaveInstanceState());
        savedState.mProgress = this.mProgress;
        return savedState;
    }

    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            setProgress(savedState.mProgress);
        }
    }

    private int maxInt(int valueA, int valueB) {
        return valueA > valueB ? valueA : valueB;
    }

    private int minInt(int valueA, int valueB) {
        return valueA < valueB ? valueA : valueB;
    }

    /* access modifiers changed from: private */
    public static class RefreshData {
        private static final int POOL_MAX = 24;
        private static final Pools.SynchronizedPool<RefreshData> S_POOL = new Pools.SynchronizedPool<>((int) POOL_MAX);
        int mId;
        boolean mIsFromUser;
        int mProgress;

        private RefreshData() {
        }

        static RefreshData obtain(int id, int progress, boolean isFromUser) {
            RefreshData refreshData = (RefreshData) S_POOL.acquire();
            if (refreshData == null) {
                refreshData = new RefreshData();
            }
            refreshData.mId = id;
            refreshData.mProgress = progress;
            refreshData.mIsFromUser = isFromUser;
            return refreshData;
        }

        /* access modifiers changed from: package-private */
        public void recycle() {
            S_POOL.release(this);
        }
    }

    public static final class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class huawei.android.widget.ProgressButton.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private static SavedState sInstance = null;
        int mProgress;

        private SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.mProgress = in.readInt();
        }

        public static SavedState getSavedStateInstance(Parcelable superState) {
            if (sInstance == null) {
                sInstance = new SavedState(superState);
            }
            return sInstance;
        }

        @Override // android.view.View.BaseSavedState, android.os.Parcelable, android.view.AbsSavedState
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.mProgress);
        }
    }

    /* access modifiers changed from: private */
    public class RefreshProgressRunnable implements Runnable {
        private final Object mLock;

        private RefreshProgressRunnable() {
            this.mLock = new Object();
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (this.mLock) {
                int count = ProgressButton.this.mRefreshData.size();
                for (int i = 0; i < count; i++) {
                    RefreshData refreshData = (RefreshData) ProgressButton.this.mRefreshData.get(i);
                    ProgressButton.this.doRefreshProgress(refreshData.mId, refreshData.mProgress, refreshData.mIsFromUser, true);
                    refreshData.recycle();
                }
                ProgressButton.this.mRefreshData.clear();
                ProgressButton.this.mIsRefreshPosted = false;
            }
        }
    }
}

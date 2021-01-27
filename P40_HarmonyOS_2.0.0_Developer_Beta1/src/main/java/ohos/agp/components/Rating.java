package ohos.agp.components;

import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.app.Context;

public class Rating extends AbsSlider {
    private Element mFilled;
    private Element mHalfFilled;
    private RatingChangedListener mRatingChangeListener;
    private Element mUnfilled;

    public interface RatingChangedListener {
        void onProgressChanged(Rating rating, int i, boolean z);

        void onStartTrackingTouch(Rating rating);

        void onStopTrackingTouch(Rating rating);
    }

    private native float nativeGetRating(long j);

    private native long nativeGetRatingHandle();

    private native int nativeGetRatingItems(long j);

    private native float nativeGetStepSize(long j);

    private native boolean nativeIsIndicator(long j);

    private native void nativeSetFilledElement(long j, long j2);

    private native void nativeSetHalfFilledElement(long j, long j2);

    private native void nativeSetIsIndicator(long j, boolean z);

    private native void nativeSetRating(long j, float f);

    private native void nativeSetRatingChangesListener(long j, RatingChangedListener ratingChangedListener);

    private native void nativeSetRatingItems(long j, int i);

    private native void nativeSetStepSize(long j, float f);

    private native void nativeSetUnfilledElement(long j, long j2);

    public Rating(Context context) {
        this(context, null);
    }

    public Rating(Context context, AttrSet attrSet) {
        this(context, attrSet, "RatingDefaultStyle");
    }

    public Rating(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mFilled = null;
        this.mUnfilled = null;
        this.mHalfFilled = null;
        this.mRatingChangeListener = null;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.AbsSeekBar, ohos.agp.components.ProgressBar, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getRatingAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    public float getGrainSize() {
        return nativeGetStepSize(this.mNativeViewPtr);
    }

    public void setGrainSize(float f) {
        nativeSetStepSize(this.mNativeViewPtr, f);
    }

    public void setScore(float f) {
        nativeSetRating(this.mNativeViewPtr, f);
    }

    public float getScore() {
        return nativeGetRating(this.mNativeViewPtr);
    }

    public void setIsOperable(boolean z) {
        nativeSetIsIndicator(this.mNativeViewPtr, z);
    }

    public boolean isOperable() {
        return nativeIsIndicator(this.mNativeViewPtr);
    }

    public void setRatingItems(int i) {
        nativeSetRatingItems(this.mNativeViewPtr, i);
    }

    public int getRatingItems() {
        return nativeGetRatingItems(this.mNativeViewPtr);
    }

    public void setRatingChangedListener(RatingChangedListener ratingChangedListener) {
        this.mRatingChangeListener = ratingChangedListener;
        nativeSetRatingChangesListener(this.mNativeViewPtr, ratingChangedListener);
    }

    public void setFilledElement(Element element) {
        this.mFilled = element;
        nativeSetFilledElement(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    public Element getFilledElement() {
        return this.mFilled;
    }

    public void setUnfilledElement(Element element) {
        this.mUnfilled = element;
        nativeSetUnfilledElement(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    public Element getHalfFilledElement() {
        return this.mHalfFilled;
    }

    public void setHalfFilledElement(Element element) {
        this.mHalfFilled = element;
        nativeSetHalfFilledElement(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    public Element getUnfilledElement() {
        return this.mUnfilled;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ProgressBar, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetRatingHandle();
        }
    }
}

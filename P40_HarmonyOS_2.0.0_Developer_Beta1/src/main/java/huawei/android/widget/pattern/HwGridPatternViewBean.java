package huawei.android.widget.pattern;

import android.graphics.drawable.Drawable;
import android.view.View;

public class HwGridPatternViewBean {
    private View.OnClickListener mButtonListener;
    private String mButtonText;
    private Drawable mPlayBackDrawable;
    private Drawable mPlayIconDrawable;
    private String mSubTitle;
    private String mSubTitleSecond;
    private String mTitle;
    private Drawable mTitleIconDrawable;

    public HwGridPatternViewBean(Drawable playBackDrawable, Drawable playIconDrawable, Drawable titleIconDrawable, String title, String subTitle) {
        this.mPlayBackDrawable = playBackDrawable;
        this.mPlayIconDrawable = playIconDrawable;
        this.mTitleIconDrawable = titleIconDrawable;
        this.mTitle = title;
        this.mSubTitle = subTitle;
    }

    public HwGridPatternViewBean(Drawable playBackDrawable, Drawable playIconDrawable, Drawable titleIconDrawable, String title, String subTitle, String buttonText, View.OnClickListener buttonListener) {
        this.mPlayBackDrawable = playBackDrawable;
        this.mPlayIconDrawable = playIconDrawable;
        this.mTitleIconDrawable = titleIconDrawable;
        this.mTitle = title;
        this.mSubTitle = subTitle;
        this.mButtonText = buttonText;
        this.mButtonListener = buttonListener;
    }

    public HwGridPatternViewBean(Drawable playBackDrawable, Drawable playIconDrawable, String title, String subTitle, String subTitleSecond) {
        this.mPlayBackDrawable = playBackDrawable;
        this.mPlayIconDrawable = playIconDrawable;
        this.mTitle = title;
        this.mSubTitle = subTitle;
        this.mSubTitleSecond = subTitleSecond;
    }

    /* access modifiers changed from: package-private */
    public Drawable getPlayBackDrawable() {
        return this.mPlayBackDrawable;
    }

    /* access modifiers changed from: package-private */
    public void setPlayBackDrawable(Drawable playBackDrawable) {
        this.mPlayBackDrawable = playBackDrawable;
    }

    /* access modifiers changed from: package-private */
    public Drawable getPlayIconDrawable() {
        return this.mPlayIconDrawable;
    }

    /* access modifiers changed from: package-private */
    public void setPlayIconDrawable(Drawable playIconDrawable) {
        this.mPlayIconDrawable = playIconDrawable;
    }

    /* access modifiers changed from: package-private */
    public Drawable getTitleIconDrawable() {
        return this.mTitleIconDrawable;
    }

    /* access modifiers changed from: package-private */
    public void setTitleIconDrawable(Drawable titleIconDrawable) {
        this.mTitleIconDrawable = titleIconDrawable;
    }

    /* access modifiers changed from: package-private */
    public String getTitle() {
        return this.mTitle;
    }

    /* access modifiers changed from: package-private */
    public void setTitle(String title) {
        this.mTitle = title;
    }

    /* access modifiers changed from: package-private */
    public String getSubTitle() {
        return this.mSubTitle;
    }

    /* access modifiers changed from: package-private */
    public void setSubTitle(String subTitle) {
        this.mSubTitle = subTitle;
    }

    /* access modifiers changed from: package-private */
    public String getSubTitleSecond() {
        return this.mSubTitleSecond;
    }

    /* access modifiers changed from: package-private */
    public void setSubTitleSecond(String subTitleSecond) {
        this.mSubTitleSecond = subTitleSecond;
    }

    /* access modifiers changed from: package-private */
    public String getButtonText() {
        return this.mButtonText;
    }

    /* access modifiers changed from: package-private */
    public void setButtonText(String buttonText) {
        this.mButtonText = buttonText;
    }

    /* access modifiers changed from: package-private */
    public View.OnClickListener getButtonListener() {
        return this.mButtonListener;
    }

    /* access modifiers changed from: package-private */
    public void setButtonListener(View.OnClickListener buttonListener) {
        this.mButtonListener = buttonListener;
    }
}

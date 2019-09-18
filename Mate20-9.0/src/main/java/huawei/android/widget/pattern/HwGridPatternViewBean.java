package huawei.android.widget.pattern;

import android.graphics.drawable.Drawable;
import android.view.View;

public class HwGridPatternViewBean {
    private View.OnClickListener buttonListener;
    private String buttonText;
    private Drawable playBackDrawable;
    private Drawable playIconDrawable;
    private String subTitle;
    private String subTitleSecond;
    private String title;
    private Drawable titleIconDrawable;

    public HwGridPatternViewBean(Drawable playBackDrawable2, Drawable playIconDrawable2, Drawable titleIconDrawable2, String title2, String subTitle2) {
        setPlayBackDrawable(playBackDrawable2);
        setPlayIconDrawable(playIconDrawable2);
        setTitleIconDrawable(titleIconDrawable2);
        setTitle(title2);
        setSubTitle(subTitle2);
    }

    public HwGridPatternViewBean(Drawable playBackDrawable2, Drawable playIconDrawable2, Drawable titleIconDrawable2, String title2, String subTitle2, String buttonText2, View.OnClickListener buttonListener2) {
        setPlayBackDrawable(playBackDrawable2);
        setPlayIconDrawable(playIconDrawable2);
        setTitleIconDrawable(titleIconDrawable2);
        setTitle(title2);
        setSubTitle(subTitle2);
        setButtonText(buttonText2);
        setButtonListener(buttonListener2);
    }

    public HwGridPatternViewBean(Drawable playBackDrawable2, Drawable playIconDrawable2, String title2, String subTitle2, String subTitleSecond2) {
        setPlayBackDrawable(playBackDrawable2);
        setPlayIconDrawable(playIconDrawable2);
        setTitle(title2);
        setSubTitle(subTitle2);
        setSubTitleSecond(subTitleSecond2);
    }

    /* access modifiers changed from: package-private */
    public Drawable getPlayBackDrawable() {
        return this.playBackDrawable;
    }

    /* access modifiers changed from: package-private */
    public void setPlayBackDrawable(Drawable playBackDrawable2) {
        this.playBackDrawable = playBackDrawable2;
    }

    /* access modifiers changed from: package-private */
    public Drawable getPlayIconDrawable() {
        return this.playIconDrawable;
    }

    /* access modifiers changed from: package-private */
    public void setPlayIconDrawable(Drawable playIconDrawable2) {
        this.playIconDrawable = playIconDrawable2;
    }

    /* access modifiers changed from: package-private */
    public Drawable getTitleIconDrawable() {
        return this.titleIconDrawable;
    }

    /* access modifiers changed from: package-private */
    public void setTitleIconDrawable(Drawable titleIconDrawable2) {
        this.titleIconDrawable = titleIconDrawable2;
    }

    /* access modifiers changed from: package-private */
    public String getTitle() {
        return this.title;
    }

    /* access modifiers changed from: package-private */
    public void setTitle(String title2) {
        this.title = title2;
    }

    /* access modifiers changed from: package-private */
    public String getSubTitle() {
        return this.subTitle;
    }

    /* access modifiers changed from: package-private */
    public void setSubTitle(String subTitle2) {
        this.subTitle = subTitle2;
    }

    /* access modifiers changed from: package-private */
    public String getSubTitleSecond() {
        return this.subTitleSecond;
    }

    /* access modifiers changed from: package-private */
    public void setSubTitleSecond(String subTitleSecond2) {
        this.subTitleSecond = subTitleSecond2;
    }

    /* access modifiers changed from: package-private */
    public String getButtonText() {
        return this.buttonText;
    }

    /* access modifiers changed from: package-private */
    public void setButtonText(String buttonText2) {
        this.buttonText = buttonText2;
    }

    /* access modifiers changed from: package-private */
    public View.OnClickListener getButtonListener() {
        return this.buttonListener;
    }

    /* access modifiers changed from: package-private */
    public void setButtonListener(View.OnClickListener buttonListener2) {
        this.buttonListener = buttonListener2;
    }
}

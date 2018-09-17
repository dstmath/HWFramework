package com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.view.WindowManager.LayoutParams;
import android.view.animation.DecelerateInterpolator;
import java.util.ArrayList;

public class DrawableHolder implements AnimatorListener {
    private static final boolean DBG = false;
    public static final DecelerateInterpolator EASE_OUT_INTERPOLATOR = null;
    private static final String TAG = "DrawableHolder";
    private float mAlpha;
    private ArrayList<ObjectAnimator> mAnimators;
    private BitmapDrawable mDrawable;
    private ArrayList<ObjectAnimator> mNeedToStart;
    private float mScaleX;
    private float mScaleY;
    private float mX;
    private float mY;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.DrawableHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.DrawableHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.DrawableHolder.<clinit>():void");
    }

    public DrawableHolder(BitmapDrawable drawable) {
        this(drawable, 0.0f, 0.0f);
    }

    public DrawableHolder(BitmapDrawable drawable, float x, float y) {
        this.mX = 0.0f;
        this.mY = 0.0f;
        this.mScaleX = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        this.mScaleY = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        this.mAlpha = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        this.mAnimators = new ArrayList();
        this.mNeedToStart = new ArrayList();
        this.mDrawable = drawable;
        this.mX = x;
        this.mY = y;
        this.mDrawable.getPaint().setAntiAlias(true);
        this.mDrawable.setBounds(0, 0, this.mDrawable.getIntrinsicWidth(), this.mDrawable.getIntrinsicHeight());
    }

    public ObjectAnimator addAnimTo(long duration, long delay, String property, float toValue, boolean replace) {
        if (replace) {
            removeAnimationFor(property);
        }
        ObjectAnimator anim = ObjectAnimator.ofFloat(this, property, new float[]{toValue});
        anim.setDuration(duration);
        anim.setStartDelay(delay);
        anim.setInterpolator(EASE_OUT_INTERPOLATOR);
        addAnimation(anim, replace);
        return anim;
    }

    public void removeAnimationFor(String property) {
        for (ObjectAnimator currentAnim : (ArrayList) this.mAnimators.clone()) {
            if (property.equals(currentAnim.getPropertyName())) {
                currentAnim.cancel();
            }
        }
    }

    public void clearAnimations() {
        for (ObjectAnimator currentAnim : this.mAnimators) {
            currentAnim.cancel();
        }
        this.mAnimators.clear();
    }

    private DrawableHolder addAnimation(ObjectAnimator anim, boolean overwrite) {
        if (anim != null) {
            this.mAnimators.add(anim);
        }
        this.mNeedToStart.add(anim);
        return this;
    }

    public void draw(Canvas canvas) {
        if (this.mAlpha > 0.00390625f) {
            canvas.save(1);
            canvas.translate(this.mX, this.mY);
            canvas.scale(this.mScaleX, this.mScaleY);
            canvas.translate(((float) getWidth()) * -0.5f, ((float) getHeight()) * -0.5f);
            this.mDrawable.setAlpha(Math.round(this.mAlpha * 255.0f));
            this.mDrawable.draw(canvas);
            canvas.restore();
        }
    }

    public void startAnimations(AnimatorUpdateListener listener) {
        for (int i = 0; i < this.mNeedToStart.size(); i++) {
            ObjectAnimator anim = (ObjectAnimator) this.mNeedToStart.get(i);
            anim.addUpdateListener(listener);
            anim.addListener(this);
            anim.start();
        }
        this.mNeedToStart.clear();
    }

    public void setX(float value) {
        this.mX = value;
    }

    public void setY(float value) {
        this.mY = value;
    }

    public void setScaleX(float value) {
        this.mScaleX = value;
    }

    public void setScaleY(float value) {
        this.mScaleY = value;
    }

    public void setAlpha(float alpha) {
        this.mAlpha = alpha;
    }

    public float getX() {
        return this.mX;
    }

    public float getY() {
        return this.mY;
    }

    public float getScaleX() {
        return this.mScaleX;
    }

    public float getScaleY() {
        return this.mScaleY;
    }

    public float getAlpha() {
        return this.mAlpha;
    }

    public BitmapDrawable getDrawable() {
        return this.mDrawable;
    }

    public int getWidth() {
        return this.mDrawable.getIntrinsicWidth();
    }

    public int getHeight() {
        return this.mDrawable.getIntrinsicHeight();
    }

    public void onAnimationCancel(Animator animation) {
    }

    public void onAnimationEnd(Animator animation) {
        this.mAnimators.remove(animation);
    }

    public void onAnimationRepeat(Animator animation) {
    }

    public void onAnimationStart(Animator animation) {
    }
}

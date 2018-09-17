package android.graphics.drawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.RippleComponent.RenderNodeAnimatorSet;
import android.util.FloatProperty;
import android.view.DisplayListCanvas;
import android.view.RenderNodeAnimator;
import huawei.com.android.internal.widget.HwFragmentContainer;
import huawei.com.android.internal.widget.HwFragmentMenuItemView;

class HwRippleForegroundImpl extends RippleComponent {
    public static final int DURATION_ENTER = 100;
    public static final int DURATION_EXIT = 200;
    private static final TimeInterpolator INTERPOLATOR_ENTER = null;
    private static final TimeInterpolator INTERPOLATOR_EXIT = null;
    public static final float LINEAR_FROM = 0.8f;
    private static final FloatProperty<HwRippleForegroundImpl> OPACITY = null;
    public static final int RADIUS_DEF = 111;
    public static final float RAIDUS_FROM = 0.57f;
    private static final FloatProperty<HwRippleForegroundImpl> TWEEN_RADIUS = null;
    private float mOpacity;
    private CanvasProperty<Float> mPropB;
    private CanvasProperty<Float> mPropL;
    private CanvasProperty<Paint> mPropPaint;
    private CanvasProperty<Float> mPropR;
    private CanvasProperty<Float> mPropRadius;
    private CanvasProperty<Float> mPropRx;
    private CanvasProperty<Float> mPropRy;
    private CanvasProperty<Float> mPropT;
    private CanvasProperty<Float> mPropX;
    private CanvasProperty<Float> mPropY;
    private float mTweenRadius;
    private int mType;

    /* renamed from: android.graphics.drawable.HwRippleForegroundImpl.1 */
    static class AnonymousClass1 extends FloatProperty<HwRippleForegroundImpl> {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(HwRippleForegroundImpl object, float value) {
            object.mTweenRadius = value;
            object.invalidateSelf();
        }

        public Float get(HwRippleForegroundImpl object) {
            return Float.valueOf(object.mTweenRadius);
        }
    }

    /* renamed from: android.graphics.drawable.HwRippleForegroundImpl.2 */
    static class AnonymousClass2 extends FloatProperty<HwRippleForegroundImpl> {
        AnonymousClass2(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(HwRippleForegroundImpl object, float value) {
            object.mOpacity = value;
            object.invalidateSelf();
        }

        public Float get(HwRippleForegroundImpl object) {
            return Float.valueOf(object.mOpacity);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.drawable.HwRippleForegroundImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.drawable.HwRippleForegroundImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.HwRippleForegroundImpl.<clinit>():void");
    }

    public HwRippleForegroundImpl(RippleDrawable owner, Rect bounds, boolean isBounded, boolean forceSoftware, int type) {
        super(owner, bounds, forceSoftware);
        this.mTweenRadius = 0.0f;
        this.mOpacity = HwFragmentMenuItemView.ALPHA_NORMAL;
        this.mType = type;
    }

    protected void onTargetRadiusChanged(float targetRadius) {
        if (!this.mHasMaxRadius) {
            this.mHasMaxRadius = true;
            this.mTargetRadius = 111.0f;
        }
    }

    protected void jumpValuesToExit() {
        this.mTweenRadius = HwFragmentMenuItemView.ALPHA_NORMAL;
        this.mOpacity = 0.0f;
    }

    public void exit() {
        if (this.mSoftwareAnimator == null || !this.mSoftwareAnimator.isRunning()) {
            super.exit();
        } else {
            this.mSoftwareAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    HwRippleForegroundImpl.this.mSoftwareAnimator.removeAllListeners();
                    super.exit();
                }
            });
        }
    }

    protected Animator createSoftwareEnter(boolean fast) {
        ObjectAnimator opacity = ObjectAnimator.ofFloat(this, OPACITY, new float[]{HwFragmentMenuItemView.ALPHA_NORMAL, HwFragmentMenuItemView.ALPHA_NORMAL});
        opacity.setDuration(100);
        opacity.setInterpolator(INTERPOLATOR_ENTER);
        AnimatorSet set = new AnimatorSet();
        Builder builder = set.play(opacity);
        switch (this.mType) {
            case HwFragmentContainer.SPLITE_MODE_DEFAULT_SEPARATE /*0*/:
                createSoftwareEnterLinear(builder);
                break;
            default:
                createSoftwareEnterRadial(builder);
                break;
        }
        return set;
    }

    private void createSoftwareEnterLinear(Builder builder) {
        ObjectAnimator tweenRadius = ObjectAnimator.ofFloat(this, TWEEN_RADIUS, new float[]{LINEAR_FROM, HwFragmentMenuItemView.ALPHA_NORMAL});
        tweenRadius.setDuration(100);
        tweenRadius.setInterpolator(INTERPOLATOR_ENTER);
        builder.with(tweenRadius);
    }

    private void createSoftwareEnterRadial(Builder builder) {
        ObjectAnimator tweenRadius = ObjectAnimator.ofFloat(this, TWEEN_RADIUS, new float[]{RAIDUS_FROM, HwFragmentMenuItemView.ALPHA_NORMAL});
        tweenRadius.setDuration(100);
        tweenRadius.setInterpolator(INTERPOLATOR_ENTER);
        builder.with(tweenRadius);
    }

    protected Animator createSoftwareExit() {
        ObjectAnimator opacity = ObjectAnimator.ofFloat(this, OPACITY, new float[]{0.0f});
        opacity.setAutoCancel(true);
        opacity.setDuration(200);
        opacity.setInterpolator(INTERPOLATOR_EXIT);
        AnimatorSet set = new AnimatorSet();
        Builder builder = set.play(opacity);
        switch (this.mType) {
            case HwFragmentContainer.SPLITE_MODE_DEFAULT_SEPARATE /*0*/:
                createSoftwareExitLinear(builder);
                break;
            default:
                createSoftwareExitRadial(builder);
                break;
        }
        return set;
    }

    private void createSoftwareExitLinear(Builder builder) {
        ObjectAnimator tweenRadius = ObjectAnimator.ofFloat(this, "TweenRadius", new float[]{this.mTweenRadius, LINEAR_FROM});
        tweenRadius.setDuration(200);
        tweenRadius.setInterpolator(INTERPOLATOR_EXIT);
        builder.with(tweenRadius);
    }

    private void createSoftwareExitRadial(Builder builder) {
    }

    protected RenderNodeAnimatorSet createHardwareExit(Paint p) {
        p.setAlpha((int) ((this.mOpacity * ((float) p.getAlpha())) + HwFragmentMenuItemView.ALPHA_PRESSED));
        this.mPropPaint = CanvasProperty.createPaint(p);
        RenderNodeAnimator exit = new RenderNodeAnimator(this.mPropPaint, 1, 0.0f);
        exit.setInterpolator(INTERPOLATOR_EXIT);
        exit.setDuration(200);
        RenderNodeAnimatorSet set = new RenderNodeAnimatorSet();
        set.add(exit);
        switch (this.mType) {
            case HwFragmentContainer.SPLITE_MODE_DEFAULT_SEPARATE /*0*/:
                createHardwareExitLinear(set);
                break;
            default:
                createHardwareExitRadical(set);
                break;
        }
        return set;
    }

    private void createHardwareExitLinear(RenderNodeAnimatorSet set) {
        Rect rt = this.mBounds;
        this.mPropRx = CanvasProperty.createFloat(0.0f);
        this.mPropRy = CanvasProperty.createFloat(0.0f);
        this.mPropL = CanvasProperty.createFloat((float) rt.left);
        this.mPropR = CanvasProperty.createFloat((float) rt.right);
        this.mPropT = CanvasProperty.createFloat((float) rt.top);
        this.mPropB = CanvasProperty.createFloat((float) rt.bottom);
        RenderNodeAnimator rectT = new RenderNodeAnimator(this.mPropT, ((float) rt.centerY()) - ((((float) rt.height()) * LINEAR_FROM) / 2.0f));
        rectT.setDuration(200);
        rectT.setInterpolator(INTERPOLATOR_EXIT);
        RenderNodeAnimator rectB = new RenderNodeAnimator(this.mPropB, ((float) rt.centerY()) + ((((float) rt.height()) * LINEAR_FROM) / 2.0f));
        rectB.setDuration(200);
        rectB.setInterpolator(INTERPOLATOR_EXIT);
        set.add(rectT);
        set.add(rectB);
    }

    private void createHardwareExitRadical(RenderNodeAnimatorSet set) {
        this.mPropRadius = CanvasProperty.createFloat(this.mTargetRadius);
        this.mPropX = CanvasProperty.createFloat(0.0f);
        this.mPropY = CanvasProperty.createFloat(0.0f);
    }

    protected boolean drawSoftware(Canvas c, Paint p) {
        int origAlpha = p.getAlpha();
        int alpha = (int) ((((float) origAlpha) * this.mOpacity) + HwFragmentMenuItemView.ALPHA_PRESSED);
        if (alpha > 0) {
            p.setAlpha(alpha);
            switch (this.mType) {
                case HwFragmentContainer.SPLITE_MODE_DEFAULT_SEPARATE /*0*/:
                    Rect rt = this.mBounds;
                    c.drawRect((float) rt.left, ((float) rt.centerY()) - ((this.mTweenRadius * ((float) rt.height())) / 2.0f), (float) rt.right, ((this.mTweenRadius * ((float) rt.height())) / 2.0f) + ((float) rt.centerY()), p);
                    break;
                default:
                    float x = this.mBounds.exactCenterX();
                    float y = this.mBounds.exactCenterY();
                    c.translate(x, y);
                    c.drawCircle(0.0f, 0.0f, this.mTweenRadius * this.mTargetRadius, p);
                    c.translate(-x, -y);
                    break;
            }
            p.setAlpha(origAlpha);
        }
        return false;
    }

    protected boolean drawHardware(DisplayListCanvas c) {
        switch (this.mType) {
            case HwFragmentContainer.SPLITE_MODE_DEFAULT_SEPARATE /*0*/:
                c.drawRoundRect(this.mPropL, this.mPropT, this.mPropR, this.mPropB, this.mPropRx, this.mPropRy, this.mPropPaint);
                break;
            default:
                float x = this.mBounds.exactCenterX();
                float y = this.mBounds.exactCenterY();
                c.translate(x, y);
                c.drawCircle(this.mPropX, this.mPropY, this.mPropRadius, this.mPropPaint);
                c.translate(-x, -y);
                break;
        }
        return true;
    }
}

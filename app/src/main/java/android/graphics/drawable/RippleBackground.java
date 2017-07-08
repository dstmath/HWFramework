package android.graphics.drawable;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.RippleComponent.RenderNodeAnimatorSet;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.speech.tts.TextToSpeech.Engine;
import android.util.FloatProperty;
import android.view.DisplayListCanvas;
import android.view.RenderNodeAnimator;

class RippleBackground extends RippleComponent {
    private static final TimeInterpolator LINEAR_INTERPOLATOR = null;
    private static final BackgroundProperty OPACITY = null;
    private static final int OPACITY_ENTER_DURATION = 600;
    private static final int OPACITY_ENTER_DURATION_FAST = 120;
    private static final int OPACITY_EXIT_DURATION = 480;
    private boolean mIsBounded;
    private float mOpacity;
    private CanvasProperty<Paint> mPropPaint;
    private CanvasProperty<Float> mPropRadius;
    private CanvasProperty<Float> mPropX;
    private CanvasProperty<Float> mPropY;

    private static abstract class BackgroundProperty extends FloatProperty<RippleBackground> {
        public BackgroundProperty(String name) {
            super(name);
        }
    }

    /* renamed from: android.graphics.drawable.RippleBackground.1 */
    static class AnonymousClass1 extends BackgroundProperty {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(RippleBackground object, float value) {
            object.mOpacity = value;
            object.invalidateSelf();
        }

        public Float get(RippleBackground object) {
            return Float.valueOf(object.mOpacity);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.drawable.RippleBackground.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.drawable.RippleBackground.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.RippleBackground.<clinit>():void");
    }

    public RippleBackground(RippleDrawable owner, Rect bounds, boolean isBounded, boolean forceSoftware) {
        super(owner, bounds, forceSoftware);
        this.mOpacity = 0.0f;
        this.mIsBounded = isBounded;
    }

    public boolean isVisible() {
        return this.mOpacity <= 0.0f ? isHardwareAnimating() : true;
    }

    protected boolean drawSoftware(Canvas c, Paint p) {
        int origAlpha = p.getAlpha();
        int alpha = (int) ((((float) origAlpha) * this.mOpacity) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        if (alpha <= 0) {
            return false;
        }
        p.setAlpha(alpha);
        c.drawCircle(0.0f, 0.0f, this.mTargetRadius, p);
        p.setAlpha(origAlpha);
        return true;
    }

    protected boolean drawHardware(DisplayListCanvas c) {
        c.drawCircle(this.mPropX, this.mPropY, this.mPropRadius, this.mPropPaint);
        return true;
    }

    protected Animator createSoftwareEnter(boolean fast) {
        int duration = (int) ((Engine.DEFAULT_VOLUME - this.mOpacity) * ((float) (fast ? OPACITY_ENTER_DURATION_FAST : OPACITY_ENTER_DURATION)));
        ObjectAnimator opacity = ObjectAnimator.ofFloat((Object) this, OPACITY, Engine.DEFAULT_VOLUME);
        opacity.setAutoCancel(true);
        opacity.setDuration((long) duration);
        opacity.setInterpolator(LINEAR_INTERPOLATOR);
        return opacity;
    }

    protected Animator createSoftwareExit() {
        int fastEnterDuration;
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator exit = ObjectAnimator.ofFloat((Object) this, OPACITY, 0.0f);
        exit.setInterpolator(LINEAR_INTERPOLATOR);
        exit.setDuration(480);
        exit.setAutoCancel(true);
        Builder builder = set.play(exit);
        if (this.mIsBounded) {
            fastEnterDuration = (int) ((Engine.DEFAULT_VOLUME - this.mOpacity) * 120.0f);
        } else {
            fastEnterDuration = 0;
        }
        if (fastEnterDuration > 0) {
            Animator enter = ObjectAnimator.ofFloat((Object) this, OPACITY, Engine.DEFAULT_VOLUME);
            enter.setInterpolator(LINEAR_INTERPOLATOR);
            enter.setDuration((long) fastEnterDuration);
            enter.setAutoCancel(true);
            builder.after(enter);
        }
        return set;
    }

    protected RenderNodeAnimatorSet createHardwareExit(Paint p) {
        RenderNodeAnimatorSet set = new RenderNodeAnimatorSet();
        int targetAlpha = p.getAlpha();
        p.setAlpha((int) ((this.mOpacity * ((float) targetAlpha)) + NetworkHistoryUtils.RECOVERY_PERCENTAGE));
        this.mPropPaint = CanvasProperty.createPaint(p);
        this.mPropRadius = CanvasProperty.createFloat(this.mTargetRadius);
        this.mPropX = CanvasProperty.createFloat(0.0f);
        this.mPropY = CanvasProperty.createFloat(0.0f);
        int fastEnterDuration = this.mIsBounded ? (int) ((Engine.DEFAULT_VOLUME - this.mOpacity) * 120.0f) : 0;
        RenderNodeAnimator exit = new RenderNodeAnimator(this.mPropPaint, 1, 0.0f);
        exit.setInterpolator(LINEAR_INTERPOLATOR);
        exit.setDuration(480);
        if (fastEnterDuration > 0) {
            exit.setStartDelay((long) fastEnterDuration);
            exit.setStartValue((float) targetAlpha);
        }
        set.add(exit);
        if (fastEnterDuration > 0) {
            RenderNodeAnimator enter = new RenderNodeAnimator(this.mPropPaint, 1, (float) targetAlpha);
            enter.setInterpolator(LINEAR_INTERPOLATOR);
            enter.setDuration((long) fastEnterDuration);
            set.add(enter);
        }
        return set;
    }

    protected void jumpValuesToExit() {
        this.mOpacity = 0.0f;
    }
}

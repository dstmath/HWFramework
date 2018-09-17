package android.view;

import android.animation.TimeInterpolator;
import android.view.animation.Interpolator;
import com.android.internal.view.animation.FallbackLUTInterpolator;
import java.util.ArrayList;

class ViewPropertyAnimatorRT {
    private static final Interpolator sLinearInterpolator = null;
    private RenderNodeAnimator[] mAnimators;
    private final View mView;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewPropertyAnimatorRT.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.ViewPropertyAnimatorRT.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ViewPropertyAnimatorRT.<clinit>():void");
    }

    ViewPropertyAnimatorRT(View view) {
        this.mAnimators = new RenderNodeAnimator[12];
        this.mView = view;
    }

    public boolean startAnimation(ViewPropertyAnimator parent) {
        cancelAnimators(parent.mPendingAnimations);
        if (!canHandleAnimator(parent)) {
            return false;
        }
        doStartAnimation(parent);
        return true;
    }

    public void cancelAll() {
        for (int i = 0; i < this.mAnimators.length; i++) {
            if (this.mAnimators[i] != null) {
                this.mAnimators[i].cancel();
                this.mAnimators[i] = null;
            }
        }
    }

    private void doStartAnimation(ViewPropertyAnimator parent) {
        int size = parent.mPendingAnimations.size();
        long startDelay = parent.getStartDelay();
        long duration = parent.getDuration();
        TimeInterpolator interpolator = parent.getInterpolator();
        if (interpolator == null) {
            interpolator = sLinearInterpolator;
        }
        if (!RenderNodeAnimator.isNativeInterpolator(interpolator)) {
            interpolator = new FallbackLUTInterpolator(interpolator, duration);
        }
        for (int i = 0; i < size; i++) {
            NameValuesHolder holder = (NameValuesHolder) parent.mPendingAnimations.get(i);
            int property = RenderNodeAnimator.mapViewPropertyToRenderProperty(holder.mNameConstant);
            RenderNodeAnimator animator = new RenderNodeAnimator(property, holder.mFromValue + holder.mDeltaValue);
            animator.setStartDelay(startDelay);
            animator.setDuration(duration);
            animator.setInterpolator(interpolator);
            animator.setTarget(this.mView);
            animator.start();
            this.mAnimators[property] = animator;
        }
        parent.mPendingAnimations.clear();
    }

    private boolean canHandleAnimator(ViewPropertyAnimator parent) {
        if (parent.getUpdateListener() == null && parent.getListener() == null && this.mView.isHardwareAccelerated() && !parent.hasActions()) {
            return true;
        }
        return false;
    }

    private void cancelAnimators(ArrayList<NameValuesHolder> mPendingAnimations) {
        int size = mPendingAnimations.size();
        for (int i = 0; i < size; i++) {
            int property = RenderNodeAnimator.mapViewPropertyToRenderProperty(((NameValuesHolder) mPendingAnimations.get(i)).mNameConstant);
            if (this.mAnimators[property] != null) {
                this.mAnimators[property].cancel();
                this.mAnimators[property] = null;
            }
        }
    }
}

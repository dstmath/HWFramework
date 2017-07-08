package android.transition;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.Animator.AnimatorPauseListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.transition.Transition.TransitionListenerAdapter;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.R;

public abstract class Visibility extends Transition {
    public static final int MODE_IN = 1;
    public static final int MODE_OUT = 2;
    private static final String PROPNAME_PARENT = "android:visibility:parent";
    private static final String PROPNAME_SCREEN_LOCATION = "android:visibility:screenLocation";
    static final String PROPNAME_VISIBILITY = "android:visibility:visibility";
    private static final String[] sTransitionProperties = null;
    private int mMode;
    private boolean mSuppressLayout;

    /* renamed from: android.transition.Visibility.1 */
    class AnonymousClass1 extends TransitionListenerAdapter {
        final /* synthetic */ View val$finalOverlayView;
        final /* synthetic */ ViewGroup val$finalSceneRoot;

        AnonymousClass1(ViewGroup val$finalSceneRoot, View val$finalOverlayView) {
            this.val$finalSceneRoot = val$finalSceneRoot;
            this.val$finalOverlayView = val$finalOverlayView;
        }

        public void onTransitionEnd(Transition transition) {
            this.val$finalSceneRoot.getOverlay().remove(this.val$finalOverlayView);
        }
    }

    private static class DisappearListener extends TransitionListenerAdapter implements AnimatorListener, AnimatorPauseListener {
        boolean mCanceled;
        private final int mFinalVisibility;
        private boolean mLayoutSuppressed;
        private final ViewGroup mParent;
        private final boolean mSuppressLayout;
        private final View mView;

        public DisappearListener(View view, int finalVisibility, boolean suppressLayout) {
            this.mCanceled = false;
            this.mView = view;
            this.mFinalVisibility = finalVisibility;
            this.mParent = (ViewGroup) view.getParent();
            this.mSuppressLayout = suppressLayout;
            suppressLayout(true);
        }

        public void onAnimationPause(Animator animation) {
            if (!this.mCanceled) {
                this.mView.setTransitionVisibility(this.mFinalVisibility);
            }
        }

        public void onAnimationResume(Animator animation) {
            if (!this.mCanceled) {
                this.mView.setTransitionVisibility(0);
            }
        }

        public void onAnimationCancel(Animator animation) {
            this.mCanceled = true;
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            hideViewWhenNotCanceled();
        }

        public void onTransitionEnd(Transition transition) {
            hideViewWhenNotCanceled();
        }

        public void onTransitionPause(Transition transition) {
            suppressLayout(false);
        }

        public void onTransitionResume(Transition transition) {
            suppressLayout(true);
        }

        private void hideViewWhenNotCanceled() {
            if (!this.mCanceled) {
                this.mView.setTransitionVisibility(this.mFinalVisibility);
                if (this.mParent != null) {
                    this.mParent.invalidate();
                }
            }
            suppressLayout(false);
        }

        private void suppressLayout(boolean suppress) {
            if (this.mSuppressLayout && this.mLayoutSuppressed != suppress && this.mParent != null) {
                this.mLayoutSuppressed = suppress;
                this.mParent.suppressLayout(suppress);
            }
        }
    }

    private static class VisibilityInfo {
        ViewGroup endParent;
        int endVisibility;
        boolean fadeIn;
        ViewGroup startParent;
        int startVisibility;
        boolean visibilityChange;

        private VisibilityInfo() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.transition.Visibility.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.transition.Visibility.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.transition.Visibility.<clinit>():void");
    }

    public Visibility() {
        this.mMode = 3;
        this.mSuppressLayout = true;
    }

    public Visibility(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mMode = 3;
        this.mSuppressLayout = true;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VisibilityTransition);
        int mode = a.getInt(0, 0);
        a.recycle();
        if (mode != 0) {
            setMode(mode);
        }
    }

    public void setSuppressLayout(boolean suppress) {
        this.mSuppressLayout = suppress;
    }

    public void setMode(int mode) {
        if ((mode & -4) != 0) {
            throw new IllegalArgumentException("Only MODE_IN and MODE_OUT flags are allowed");
        }
        this.mMode = mode;
    }

    public int getMode() {
        return this.mMode;
    }

    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }

    private void captureValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_VISIBILITY, Integer.valueOf(transitionValues.view.getVisibility()));
        transitionValues.values.put(PROPNAME_PARENT, transitionValues.view.getParent());
        int[] loc = new int[MODE_OUT];
        transitionValues.view.getLocationOnScreen(loc);
        transitionValues.values.put(PROPNAME_SCREEN_LOCATION, loc);
    }

    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public boolean isVisible(TransitionValues values) {
        if (values == null) {
            return false;
        }
        boolean z = ((Integer) values.values.get(PROPNAME_VISIBILITY)).intValue() == 0 && ((View) values.values.get(PROPNAME_PARENT)) != null;
        return z;
    }

    private static VisibilityInfo getVisibilityChangeInfo(TransitionValues startValues, TransitionValues endValues) {
        VisibilityInfo visInfo = new VisibilityInfo();
        visInfo.visibilityChange = false;
        visInfo.fadeIn = false;
        if (startValues == null || !startValues.values.containsKey(PROPNAME_VISIBILITY)) {
            visInfo.startVisibility = -1;
            visInfo.startParent = null;
        } else {
            visInfo.startVisibility = ((Integer) startValues.values.get(PROPNAME_VISIBILITY)).intValue();
            visInfo.startParent = (ViewGroup) startValues.values.get(PROPNAME_PARENT);
        }
        if (endValues == null || !endValues.values.containsKey(PROPNAME_VISIBILITY)) {
            visInfo.endVisibility = -1;
            visInfo.endParent = null;
        } else {
            visInfo.endVisibility = ((Integer) endValues.values.get(PROPNAME_VISIBILITY)).intValue();
            visInfo.endParent = (ViewGroup) endValues.values.get(PROPNAME_PARENT);
        }
        if (startValues == null || endValues == null) {
            if (startValues == null && visInfo.endVisibility == 0) {
                visInfo.fadeIn = true;
                visInfo.visibilityChange = true;
            } else if (endValues == null && visInfo.startVisibility == 0) {
                visInfo.fadeIn = false;
                visInfo.visibilityChange = true;
            }
        } else if (visInfo.startVisibility == visInfo.endVisibility && visInfo.startParent == visInfo.endParent) {
            return visInfo;
        } else {
            if (visInfo.startVisibility != visInfo.endVisibility) {
                if (visInfo.startVisibility == 0) {
                    visInfo.fadeIn = false;
                    visInfo.visibilityChange = true;
                } else if (visInfo.endVisibility == 0) {
                    visInfo.fadeIn = true;
                    visInfo.visibilityChange = true;
                }
            } else if (visInfo.startParent != visInfo.endParent) {
                if (visInfo.endParent == null) {
                    visInfo.fadeIn = false;
                    visInfo.visibilityChange = true;
                } else if (visInfo.startParent == null) {
                    visInfo.fadeIn = true;
                    visInfo.visibilityChange = true;
                }
            }
        }
        return visInfo;
    }

    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        VisibilityInfo visInfo = getVisibilityChangeInfo(startValues, endValues);
        if (!visInfo.visibilityChange || (visInfo.startParent == null && visInfo.endParent == null)) {
            return null;
        }
        if (visInfo.fadeIn) {
            return onAppear(sceneRoot, startValues, visInfo.startVisibility, endValues, visInfo.endVisibility);
        }
        return onDisappear(sceneRoot, startValues, visInfo.startVisibility, endValues, visInfo.endVisibility);
    }

    public Animator onAppear(ViewGroup sceneRoot, TransitionValues startValues, int startVisibility, TransitionValues endValues, int endVisibility) {
        if ((this.mMode & MODE_IN) != MODE_IN || endValues == null) {
            return null;
        }
        if (startValues == null) {
            View endParent = (View) endValues.view.getParent();
            if (getVisibilityChangeInfo(getMatchedTransitionValues(endParent, false), getTransitionValues(endParent, false)).visibilityChange) {
                return null;
            }
        }
        return onAppear(sceneRoot, endValues.view, startValues, endValues);
    }

    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        return null;
    }

    public Animator onDisappear(ViewGroup sceneRoot, TransitionValues startValues, int startVisibility, TransitionValues endValues, int endVisibility) {
        if ((this.mMode & MODE_OUT) != MODE_OUT) {
            return null;
        }
        View view = startValues != null ? startValues.view : null;
        View endView = endValues != null ? endValues.view : null;
        View overlayView = null;
        View viewToKeep = null;
        if (endView == null || endView.getParent() == null) {
            if (endView != null) {
                overlayView = endView;
            } else if (view != null) {
                if (view.getParent() == null) {
                    overlayView = view;
                } else {
                    if (view.getParent() instanceof View) {
                        View startParent = (View) view.getParent();
                        if (!getVisibilityChangeInfo(getTransitionValues(startParent, true), getMatchedTransitionValues(startParent, true)).visibilityChange) {
                            overlayView = TransitionUtils.copyViewImage(sceneRoot, view, startParent);
                        } else if (startParent.getParent() == null) {
                            int id = startParent.getId();
                            if (!(id == -1 || sceneRoot.findViewById(id) == null || !this.mCanRemoveViews)) {
                                overlayView = view;
                            }
                        }
                    }
                }
            }
        } else if (endVisibility == 4) {
            viewToKeep = endView;
        } else if (view == endView) {
            viewToKeep = endView;
        } else {
            overlayView = view;
        }
        int finalVisibility = endVisibility;
        ViewGroup finalSceneRoot = sceneRoot;
        Animator animator;
        if (overlayView != null) {
            int[] screenLoc = (int[]) startValues.values.get(PROPNAME_SCREEN_LOCATION);
            int screenX = screenLoc[0];
            int screenY = screenLoc[MODE_IN];
            int[] loc = new int[MODE_OUT];
            sceneRoot.getLocationOnScreen(loc);
            overlayView.offsetLeftAndRight((screenX - loc[0]) - overlayView.getLeft());
            overlayView.offsetTopAndBottom((screenY - loc[MODE_IN]) - overlayView.getTop());
            sceneRoot.getOverlay().add(overlayView);
            animator = onDisappear(sceneRoot, overlayView, startValues, endValues);
            if (animator == null) {
                sceneRoot.getOverlay().remove(overlayView);
            } else {
                addListener(new AnonymousClass1(sceneRoot, overlayView));
            }
            return animator;
        } else if (viewToKeep == null) {
            return null;
        } else {
            int originalVisibility = viewToKeep.getVisibility();
            viewToKeep.setTransitionVisibility(0);
            animator = onDisappear(sceneRoot, viewToKeep, startValues, endValues);
            if (animator != null) {
                DisappearListener disappearListener = new DisappearListener(viewToKeep, endVisibility, this.mSuppressLayout);
                animator.addListener(disappearListener);
                animator.addPauseListener(disappearListener);
                addListener(disappearListener);
            } else {
                viewToKeep.setTransitionVisibility(originalVisibility);
            }
            return animator;
        }
    }

    public boolean isTransitionRequired(TransitionValues startValues, TransitionValues newValues) {
        boolean z = true;
        if (startValues == null && newValues == null) {
            return false;
        }
        if (startValues != null && newValues != null && newValues.values.containsKey(PROPNAME_VISIBILITY) != startValues.values.containsKey(PROPNAME_VISIBILITY)) {
            return false;
        }
        VisibilityInfo changeInfo = getVisibilityChangeInfo(startValues, newValues);
        if (!changeInfo.visibilityChange) {
            z = false;
        } else if (!(changeInfo.startVisibility == 0 || changeInfo.endVisibility == 0)) {
            z = false;
        }
        return z;
    }

    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        return null;
    }
}

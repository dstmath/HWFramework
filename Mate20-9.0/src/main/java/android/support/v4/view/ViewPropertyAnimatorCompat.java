package android.support.v4.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Build;
import android.view.View;
import android.view.animation.Interpolator;
import java.lang.ref.WeakReference;

public final class ViewPropertyAnimatorCompat {
    static final int LISTENER_TAG_ID = 2113929216;
    private static final String TAG = "ViewAnimatorCompat";
    Runnable mEndAction = null;
    int mOldLayerType = -1;
    Runnable mStartAction = null;
    private WeakReference<View> mView;

    static class ViewPropertyAnimatorListenerApi14 implements ViewPropertyAnimatorListener {
        boolean mAnimEndCalled;
        ViewPropertyAnimatorCompat mVpa;

        ViewPropertyAnimatorListenerApi14(ViewPropertyAnimatorCompat vpa) {
            this.mVpa = vpa;
        }

        public void onAnimationStart(View view) {
            this.mAnimEndCalled = false;
            if (this.mVpa.mOldLayerType > -1) {
                view.setLayerType(2, null);
            }
            if (this.mVpa.mStartAction != null) {
                Runnable startAction = this.mVpa.mStartAction;
                this.mVpa.mStartAction = null;
                startAction.run();
            }
            Object listenerTag = view.getTag(ViewPropertyAnimatorCompat.LISTENER_TAG_ID);
            ViewPropertyAnimatorListener listener = null;
            if (listenerTag instanceof ViewPropertyAnimatorListener) {
                listener = (ViewPropertyAnimatorListener) listenerTag;
            }
            if (listener != null) {
                listener.onAnimationStart(view);
            }
        }

        public void onAnimationEnd(View view) {
            if (this.mVpa.mOldLayerType > -1) {
                view.setLayerType(this.mVpa.mOldLayerType, null);
                this.mVpa.mOldLayerType = -1;
            }
            if (Build.VERSION.SDK_INT >= 16 || !this.mAnimEndCalled) {
                if (this.mVpa.mEndAction != null) {
                    Runnable endAction = this.mVpa.mEndAction;
                    this.mVpa.mEndAction = null;
                    endAction.run();
                }
                Object listenerTag = view.getTag(ViewPropertyAnimatorCompat.LISTENER_TAG_ID);
                ViewPropertyAnimatorListener listener = null;
                if (listenerTag instanceof ViewPropertyAnimatorListener) {
                    listener = (ViewPropertyAnimatorListener) listenerTag;
                }
                if (listener != null) {
                    listener.onAnimationEnd(view);
                }
                this.mAnimEndCalled = true;
            }
        }

        public void onAnimationCancel(View view) {
            Object listenerTag = view.getTag(ViewPropertyAnimatorCompat.LISTENER_TAG_ID);
            ViewPropertyAnimatorListener listener = null;
            if (listenerTag instanceof ViewPropertyAnimatorListener) {
                listener = (ViewPropertyAnimatorListener) listenerTag;
            }
            if (listener != null) {
                listener.onAnimationCancel(view);
            }
        }
    }

    ViewPropertyAnimatorCompat(View view) {
        this.mView = new WeakReference<>(view);
    }

    public ViewPropertyAnimatorCompat setDuration(long value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().setDuration(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat alpha(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().alpha(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat alphaBy(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().alphaBy(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat translationX(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().translationX(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat translationY(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().translationY(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat withEndAction(Runnable runnable) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            if (Build.VERSION.SDK_INT >= 16) {
                view2.animate().withEndAction(runnable);
            } else {
                setListenerInternal(view2, new ViewPropertyAnimatorListenerApi14(this));
                this.mEndAction = runnable;
            }
        }
        return this;
    }

    public long getDuration() {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            return view2.animate().getDuration();
        }
        return 0;
    }

    public ViewPropertyAnimatorCompat setInterpolator(Interpolator value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().setInterpolator(value);
        }
        return this;
    }

    public Interpolator getInterpolator() {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view == null || Build.VERSION.SDK_INT < 18) {
            return null;
        }
        return (Interpolator) view2.animate().getInterpolator();
    }

    public ViewPropertyAnimatorCompat setStartDelay(long value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().setStartDelay(value);
        }
        return this;
    }

    public long getStartDelay() {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            return view2.animate().getStartDelay();
        }
        return 0;
    }

    public ViewPropertyAnimatorCompat rotation(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().rotation(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat rotationBy(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().rotationBy(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat rotationX(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().rotationX(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat rotationXBy(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().rotationXBy(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat rotationY(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().rotationY(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat rotationYBy(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().rotationYBy(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat scaleX(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().scaleX(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat scaleXBy(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().scaleXBy(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat scaleY(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().scaleY(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat scaleYBy(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().scaleYBy(value);
        }
        return this;
    }

    public void cancel() {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().cancel();
        }
    }

    public ViewPropertyAnimatorCompat x(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().x(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat xBy(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().xBy(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat y(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().y(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat yBy(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().yBy(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat translationXBy(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().translationXBy(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat translationYBy(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().translationYBy(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat translationZBy(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null && Build.VERSION.SDK_INT >= 21) {
            view2.animate().translationZBy(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat translationZ(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null && Build.VERSION.SDK_INT >= 21) {
            view2.animate().translationZ(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat z(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null && Build.VERSION.SDK_INT >= 21) {
            view2.animate().z(value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat zBy(float value) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null && Build.VERSION.SDK_INT >= 21) {
            view2.animate().zBy(value);
        }
        return this;
    }

    public void start() {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            view2.animate().start();
        }
    }

    public ViewPropertyAnimatorCompat withLayer() {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            if (Build.VERSION.SDK_INT >= 16) {
                view2.animate().withLayer();
            } else {
                this.mOldLayerType = view2.getLayerType();
                setListenerInternal(view2, new ViewPropertyAnimatorListenerApi14(this));
            }
        }
        return this;
    }

    public ViewPropertyAnimatorCompat withStartAction(Runnable runnable) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            if (Build.VERSION.SDK_INT >= 16) {
                view2.animate().withStartAction(runnable);
            } else {
                setListenerInternal(view2, new ViewPropertyAnimatorListenerApi14(this));
                this.mStartAction = runnable;
            }
        }
        return this;
    }

    public ViewPropertyAnimatorCompat setListener(ViewPropertyAnimatorListener listener) {
        View view = (View) this.mView.get();
        View view2 = view;
        if (view != null) {
            if (Build.VERSION.SDK_INT >= 16) {
                setListenerInternal(view2, listener);
            } else {
                view2.setTag(LISTENER_TAG_ID, listener);
                setListenerInternal(view2, new ViewPropertyAnimatorListenerApi14(this));
            }
        }
        return this;
    }

    private void setListenerInternal(final View view, final ViewPropertyAnimatorListener listener) {
        if (listener != null) {
            view.animate().setListener(new AnimatorListenerAdapter() {
                public void onAnimationCancel(Animator animation) {
                    listener.onAnimationCancel(view);
                }

                public void onAnimationEnd(Animator animation) {
                    listener.onAnimationEnd(view);
                }

                public void onAnimationStart(Animator animation) {
                    listener.onAnimationStart(view);
                }
            });
        } else {
            view.animate().setListener(null);
        }
    }

    public ViewPropertyAnimatorCompat setUpdateListener(final ViewPropertyAnimatorUpdateListener listener) {
        View view = (View) this.mView.get();
        final View view2 = view;
        if (view != null && Build.VERSION.SDK_INT >= 19) {
            ValueAnimator.AnimatorUpdateListener wrapped = null;
            if (listener != null) {
                wrapped = new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        listener.onAnimationUpdate(view2);
                    }
                };
            }
            view2.animate().setUpdateListener(wrapped);
        }
        return this;
    }
}

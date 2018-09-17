package android.graphics.drawable;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.hwtheme.HwThemeManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AnimationDrawable extends DrawableContainer implements Runnable, Animatable {
    private boolean mAnimating;
    private AnimationState mAnimationState;
    private int mCurFrame;
    private boolean mMutated;
    private boolean mRunning;

    private static final class AnimationState extends DrawableContainerState {
        private int[] mDurations;
        private boolean mOneShot = false;

        AnimationState(AnimationState orig, AnimationDrawable owner, Resources res) {
            super(orig, owner, res);
            if (orig != null) {
                this.mDurations = orig.mDurations;
                this.mOneShot = orig.mOneShot;
                return;
            }
            this.mDurations = new int[getCapacity()];
            this.mOneShot = false;
        }

        private void mutate() {
            this.mDurations = (int[]) this.mDurations.clone();
        }

        public Drawable newDrawable() {
            return new AnimationDrawable(this, null, null);
        }

        public Drawable newDrawable(Resources res) {
            return new AnimationDrawable(this, res, null);
        }

        public void addFrame(Drawable dr, int dur) {
            this.mDurations[super.addChild(dr)] = dur;
        }

        public void growArray(int oldSize, int newSize) {
            super.growArray(oldSize, newSize);
            int[] newDurations = new int[newSize];
            System.arraycopy(this.mDurations, 0, newDurations, 0, oldSize);
            this.mDurations = newDurations;
        }
    }

    /* synthetic */ AnimationDrawable(AnimationState state, Resources res, AnimationDrawable -this2) {
        this(state, res);
    }

    public AnimationDrawable() {
        this(null, null);
    }

    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (!visible) {
            unscheduleSelf(this);
        } else if (restart || changed) {
            boolean startFromZero = (restart || !(this.mRunning || (this.mAnimationState.mOneShot ^ 1) == 0)) ? true : this.mCurFrame >= this.mAnimationState.getChildCount();
            setFrame(startFromZero ? 0 : this.mCurFrame, true, this.mAnimating);
        }
        return changed;
    }

    public void start() {
        boolean z = true;
        this.mAnimating = true;
        if (!isRunning()) {
            if (this.mAnimationState.getChildCount() <= 1) {
                z = this.mAnimationState.mOneShot ^ 1;
            }
            setFrame(0, false, z);
        }
    }

    public void stop() {
        this.mAnimating = false;
        if (isRunning()) {
            this.mCurFrame = 0;
            unscheduleSelf(this);
        }
    }

    public boolean isRunning() {
        return this.mRunning;
    }

    public void run() {
        nextFrame(false);
    }

    public void unscheduleSelf(Runnable what) {
        this.mRunning = false;
        super.unscheduleSelf(what);
    }

    public int getNumberOfFrames() {
        return this.mAnimationState.getChildCount();
    }

    public Drawable getFrame(int index) {
        return this.mAnimationState.getChild(index);
    }

    public int getDuration(int i) {
        return this.mAnimationState.mDurations[i];
    }

    public boolean isOneShot() {
        return this.mAnimationState.mOneShot;
    }

    public void setOneShot(boolean oneShot) {
        this.mAnimationState.mOneShot = oneShot;
    }

    public void addFrame(Drawable frame, int duration) {
        this.mAnimationState.addFrame(frame, duration);
        if (!this.mRunning) {
            setFrame(0, true, false);
        }
    }

    private void nextFrame(boolean unschedule) {
        int nextFrame = this.mCurFrame + 1;
        int numFrames = this.mAnimationState.getChildCount();
        boolean isLastFrame = this.mAnimationState.mOneShot && nextFrame >= numFrames - 1;
        if (!this.mAnimationState.mOneShot && nextFrame >= numFrames) {
            nextFrame = 0;
        }
        setFrame(nextFrame, unschedule, isLastFrame ^ 1);
    }

    private void setFrame(int frame, boolean unschedule, boolean animate) {
        if (frame < this.mAnimationState.getChildCount()) {
            this.mAnimating = animate;
            this.mCurFrame = frame;
            selectDrawable(frame);
            if (unschedule || animate) {
                unscheduleSelf(this);
            }
            if (animate) {
                this.mCurFrame = frame;
                this.mRunning = true;
                scheduleSelf(this, SystemClock.uptimeMillis() + ((long) this.mAnimationState.mDurations[frame]));
            }
        }
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.AnimationDrawable);
        super.inflateWithAttributes(r, parser, a, 0);
        updateStateFromTypedArray(a);
        updateDensity(r);
        a.recycle();
        inflateChildElements(r, parser, attrs, theme);
        setFrame(0, true, false);
    }

    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int type = parser.next();
            if (type != 1) {
                int depth = parser.getDepth();
                if (depth < innerDepth && type == 3) {
                    return;
                }
                if (type == 2 && depth <= innerDepth && (parser.getName().equals(HwThemeManager.TAG_ITEM) ^ 1) == 0) {
                    TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.AnimationDrawableItem);
                    int duration = a.getInt(0, -1);
                    if (duration < 0) {
                        throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'duration' attribute");
                    }
                    Drawable dr = a.getDrawable(1);
                    a.recycle();
                    if (dr == null) {
                        do {
                            type = parser.next();
                        } while (type == 4);
                        if (type != 2) {
                            throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'drawable' attribute or child tag" + " defining a drawable");
                        }
                        dr = Drawable.createFromXmlInner(r, parser, attrs, theme);
                    }
                    this.mAnimationState.addFrame(dr, duration);
                    if (dr != null) {
                        dr.setCallback(this);
                    }
                }
            } else {
                return;
            }
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        this.mAnimationState.mVariablePadding = a.getBoolean(1, this.mAnimationState.mVariablePadding);
        this.mAnimationState.mOneShot = a.getBoolean(2, this.mAnimationState.mOneShot);
    }

    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mAnimationState.mutate();
            this.mMutated = true;
        }
        return this;
    }

    AnimationState cloneConstantState() {
        return new AnimationState(this.mAnimationState, this, null);
    }

    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    protected void setConstantState(DrawableContainerState state) {
        super.setConstantState(state);
        if (state instanceof AnimationState) {
            this.mAnimationState = (AnimationState) state;
        }
    }

    private AnimationDrawable(AnimationState state, Resources res) {
        this.mCurFrame = 0;
        setConstantState(new AnimationState(state, this, res));
        if (state != null) {
            setFrame(0, true, false);
        }
    }
}

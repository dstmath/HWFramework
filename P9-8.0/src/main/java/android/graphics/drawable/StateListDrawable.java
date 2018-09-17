package android.graphics.drawable;

import android.R;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.hwtheme.HwThemeManager;
import android.util.AttributeSet;
import android.util.StateSet;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class StateListDrawable extends DrawableContainer {
    private static final boolean DEBUG = false;
    private static final String TAG = "StateListDrawable";
    private boolean mMutated;
    private StateListState mStateListState;

    static class StateListState extends DrawableContainerState {
        int[][] mStateSets;
        int[] mThemeAttrs;

        StateListState(StateListState orig, StateListDrawable owner, Resources res) {
            super(orig, owner, res);
            if (orig != null) {
                this.mThemeAttrs = orig.mThemeAttrs;
                this.mStateSets = orig.mStateSets;
                return;
            }
            this.mThemeAttrs = null;
            this.mStateSets = new int[getCapacity()][];
        }

        void mutate() {
            int[] iArr;
            if (this.mThemeAttrs != null) {
                iArr = (int[]) this.mThemeAttrs.clone();
            } else {
                iArr = null;
            }
            this.mThemeAttrs = iArr;
            int[][] stateSets = new int[this.mStateSets.length][];
            for (int i = this.mStateSets.length - 1; i >= 0; i--) {
                if (this.mStateSets[i] != null) {
                    iArr = (int[]) this.mStateSets[i].clone();
                } else {
                    iArr = null;
                }
                stateSets[i] = iArr;
            }
            this.mStateSets = stateSets;
        }

        int addStateSet(int[] stateSet, Drawable drawable) {
            int pos = addChild(drawable);
            this.mStateSets[pos] = stateSet;
            return pos;
        }

        int indexOfStateSet(int[] stateSet) {
            int[][] stateSets = this.mStateSets;
            int N = getChildCount();
            for (int i = 0; i < N; i++) {
                if (StateSet.stateSetMatches(stateSets[i], stateSet)) {
                    return i;
                }
            }
            return -1;
        }

        boolean hasFocusStateSpecified() {
            return StateSet.containsAttribute(this.mStateSets, R.attr.state_focused);
        }

        public Drawable newDrawable() {
            return new StateListDrawable(this, null, null);
        }

        public Drawable newDrawable(Resources res) {
            return new StateListDrawable(this, res, null);
        }

        public boolean canApplyTheme() {
            return this.mThemeAttrs == null ? super.canApplyTheme() : true;
        }

        public void growArray(int oldSize, int newSize) {
            super.growArray(oldSize, newSize);
            int[][] newStateSets = new int[newSize][];
            System.arraycopy(this.mStateSets, 0, newStateSets, 0, oldSize);
            this.mStateSets = newStateSets;
        }
    }

    /* synthetic */ StateListDrawable(StateListState state, Resources res, StateListDrawable -this2) {
        this(state, res);
    }

    public StateListDrawable() {
        this(null, null);
    }

    public void addState(int[] stateSet, Drawable drawable) {
        if (drawable != null) {
            this.mStateListState.addStateSet(stateSet, drawable);
            onStateChange(getState());
        }
    }

    public boolean isStateful() {
        return true;
    }

    public boolean hasFocusStateSpecified() {
        return this.mStateListState.hasFocusStateSpecified();
    }

    protected boolean onStateChange(int[] stateSet) {
        boolean changed = super.onStateChange(stateSet);
        int idx = this.mStateListState.indexOfStateSet(stateSet);
        if (idx < 0) {
            idx = this.mStateListState.indexOfStateSet(StateSet.WILD_CARD);
        }
        return !selectDrawable(idx) ? changed : true;
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = Drawable.obtainAttributes(r, theme, attrs, com.android.internal.R.styleable.StateListDrawable);
        super.inflateWithAttributes(r, parser, a, 1);
        updateStateFromTypedArray(a);
        updateDensity(r);
        a.recycle();
        inflateChildElements(r, parser, attrs, theme);
        onStateChange(getState());
    }

    private void updateStateFromTypedArray(TypedArray a) {
        StateListState state = this.mStateListState;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        state.mThemeAttrs = a.extractThemeAttrs();
        state.mVariablePadding = a.getBoolean(2, state.mVariablePadding);
        state.mConstantSize = a.getBoolean(3, state.mConstantSize);
        state.mEnterFadeDuration = a.getInt(4, state.mEnterFadeDuration);
        state.mExitFadeDuration = a.getInt(5, state.mExitFadeDuration);
        state.mDither = a.getBoolean(0, state.mDither);
        state.mAutoMirrored = a.getBoolean(6, state.mAutoMirrored);
    }

    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        StateListState state = this.mStateListState;
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int type = parser.next();
            if (type != 1) {
                int depth = parser.getDepth();
                if (depth < innerDepth && type == 3) {
                    return;
                }
                if (type == 2 && depth <= innerDepth && (parser.getName().equals(HwThemeManager.TAG_ITEM) ^ 1) == 0) {
                    TypedArray a = Drawable.obtainAttributes(r, theme, attrs, com.android.internal.R.styleable.StateListDrawableItem);
                    Drawable dr = a.getDrawable(0);
                    a.recycle();
                    int[] states = extractStateSet(attrs);
                    if (dr == null) {
                        do {
                            type = parser.next();
                        } while (type == 4);
                        if (type != 2) {
                            throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'drawable' attribute or " + "child tag defining a drawable");
                        }
                        dr = Drawable.createFromXmlInner(r, parser, attrs, theme);
                    }
                    state.addStateSet(states, dr);
                }
            } else {
                return;
            }
        }
    }

    int[] extractStateSet(AttributeSet attrs) {
        int numAttrs = attrs.getAttributeCount();
        int[] states = new int[numAttrs];
        int i = 0;
        int j = 0;
        while (i < numAttrs) {
            int j2;
            int stateResId = attrs.getAttributeNameResource(i);
            switch (stateResId) {
                case 0:
                    j2 = j;
                    break;
                case R.attr.id /*16842960*/:
                case R.attr.drawable /*16843161*/:
                    j2 = j;
                    break;
                default:
                    j2 = j + 1;
                    if (!attrs.getAttributeBooleanValue(i, false)) {
                        stateResId = -stateResId;
                    }
                    states[j] = stateResId;
                    break;
            }
            i++;
            j = j2;
        }
        return StateSet.trimStateSet(states, j);
    }

    StateListState getStateListState() {
        return this.mStateListState;
    }

    public int getStateCount() {
        return this.mStateListState.getChildCount();
    }

    public int[] getStateSet(int index) {
        return this.mStateListState.mStateSets[index];
    }

    public Drawable getStateDrawable(int index) {
        return this.mStateListState.getChild(index);
    }

    public int getStateDrawableIndex(int[] stateSet) {
        return this.mStateListState.indexOfStateSet(stateSet);
    }

    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mStateListState.mutate();
            this.mMutated = true;
        }
        return this;
    }

    StateListState cloneConstantState() {
        return new StateListState(this.mStateListState, this, null);
    }

    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    public void applyTheme(Theme theme) {
        super.applyTheme(theme);
        onStateChange(getState());
    }

    protected void setConstantState(DrawableContainerState state) {
        super.setConstantState(state);
        if (state instanceof StateListState) {
            this.mStateListState = (StateListState) state;
        }
    }

    private StateListDrawable(StateListState state, Resources res) {
        setConstantState(new StateListState(state, this, res));
        onStateChange(getState());
    }

    StateListDrawable(StateListState state) {
        if (state != null) {
            setConstantState(state);
        }
    }
}

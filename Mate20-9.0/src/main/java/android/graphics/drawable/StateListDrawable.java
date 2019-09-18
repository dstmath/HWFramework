package android.graphics.drawable;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.DrawableContainer;
import android.hwtheme.HwThemeManager;
import android.util.AttributeSet;
import android.util.StateSet;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class StateListDrawable extends DrawableContainer {
    private static final boolean DEBUG = false;
    private static final String TAG = "StateListDrawable";
    private boolean mMutated;
    private StateListState mStateListState;

    static class StateListState extends DrawableContainer.DrawableContainerState {
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

        /* access modifiers changed from: package-private */
        public void mutate() {
            this.mThemeAttrs = this.mThemeAttrs != null ? (int[]) this.mThemeAttrs.clone() : null;
            int[][] stateSets = new int[this.mStateSets.length][];
            for (int i = this.mStateSets.length - 1; i >= 0; i--) {
                stateSets[i] = this.mStateSets[i] != null ? (int[]) this.mStateSets[i].clone() : null;
            }
            this.mStateSets = stateSets;
        }

        /* access modifiers changed from: package-private */
        public int addStateSet(int[] stateSet, Drawable drawable) {
            int pos = addChild(drawable);
            this.mStateSets[pos] = stateSet;
            return pos;
        }

        /* access modifiers changed from: package-private */
        public int indexOfStateSet(int[] stateSet) {
            int[][] stateSets = this.mStateSets;
            int N = getChildCount();
            for (int i = 0; i < N; i++) {
                if (StateSet.stateSetMatches(stateSets[i], stateSet)) {
                    return i;
                }
            }
            return -1;
        }

        /* access modifiers changed from: package-private */
        public boolean hasFocusStateSpecified() {
            return StateSet.containsAttribute(this.mStateSets, 16842908);
        }

        public Drawable newDrawable() {
            return new StateListDrawable(this, null);
        }

        public Drawable newDrawable(Resources res) {
            return new StateListDrawable(this, res);
        }

        public boolean canApplyTheme() {
            return this.mThemeAttrs != null || super.canApplyTheme();
        }

        public void growArray(int oldSize, int newSize) {
            super.growArray(oldSize, newSize);
            int[][] newStateSets = new int[newSize][];
            System.arraycopy(this.mStateSets, 0, newStateSets, 0, oldSize);
            this.mStateSets = newStateSets;
        }
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

    /* access modifiers changed from: protected */
    public boolean onStateChange(int[] stateSet) {
        boolean changed = super.onStateChange(stateSet);
        int idx = this.mStateListState.indexOfStateSet(stateSet);
        if (idx < 0) {
            idx = this.mStateListState.indexOfStateSet(StateSet.WILD_CARD);
        }
        return selectDrawable(idx) || changed;
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.StateListDrawable);
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

    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int next;
        int type;
        StateListState state = this.mStateListState;
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int next2 = parser.next();
            int type2 = next2;
            if (next2 != 1) {
                int depth = parser.getDepth();
                int depth2 = depth;
                if (depth < innerDepth && type2 == 3) {
                    return;
                }
                if (type2 == 2 && depth2 <= innerDepth && parser.getName().equals(HwThemeManager.TAG_ITEM)) {
                    TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.StateListDrawableItem);
                    Drawable dr = a.getDrawable(0);
                    a.recycle();
                    int[] states = extractStateSet(attrs);
                    if (dr == null) {
                        do {
                            next = parser.next();
                            type = next;
                        } while (next == 4);
                        if (type == 2) {
                            dr = Drawable.createFromXmlInner(r, parser, attrs, theme);
                        } else {
                            throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
                        }
                    }
                    state.addStateSet(states, dr);
                }
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int[] extractStateSet(AttributeSet attrs) {
        int i;
        int numAttrs = attrs.getAttributeCount();
        int[] states = new int[numAttrs];
        int j = 0;
        for (int i2 = 0; i2 < numAttrs; i2++) {
            int stateResId = attrs.getAttributeNameResource(i2);
            if (!(stateResId == 0 || stateResId == 16842960 || stateResId == 16843161)) {
                int j2 = j + 1;
                if (attrs.getAttributeBooleanValue(i2, false)) {
                    i = stateResId;
                } else {
                    i = -stateResId;
                }
                states[j] = i;
                j = j2;
            }
        }
        return StateSet.trimStateSet(states, j);
    }

    /* access modifiers changed from: package-private */
    public StateListState getStateListState() {
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

    /* access modifiers changed from: package-private */
    public StateListState cloneConstantState() {
        return new StateListState(this.mStateListState, this, null);
    }

    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    public void applyTheme(Resources.Theme theme) {
        super.applyTheme(theme);
        onStateChange(getState());
    }

    /* access modifiers changed from: protected */
    public void setConstantState(DrawableContainer.DrawableContainerState state) {
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

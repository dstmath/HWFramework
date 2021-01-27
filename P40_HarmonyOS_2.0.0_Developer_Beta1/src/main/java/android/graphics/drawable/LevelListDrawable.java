package android.graphics.drawable;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.DrawableContainer;
import android.util.AttributeSet;
import com.android.ims.ImsConfig;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class LevelListDrawable extends DrawableContainer {
    private LevelListState mLevelListState;
    private boolean mMutated;

    public LevelListDrawable() {
        this(null, null);
    }

    public void addLevel(int low, int high, Drawable drawable) {
        if (drawable != null) {
            this.mLevelListState.addLevel(low, high, drawable);
            onLevelChange(getLevel());
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
    public boolean onLevelChange(int level) {
        if (selectDrawable(this.mLevelListState.indexOfLevel(level))) {
            return true;
        }
        return super.onLevelChange(level);
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        updateDensity(r);
        inflateChildElements(r, parser, attrs, theme);
    }

    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int depth;
        Drawable dr;
        int type;
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int type2 = parser.next();
            if (type2 == 1 || ((depth = parser.getDepth()) < innerDepth && type2 == 3)) {
                break;
            } else if (type2 == 2 && depth <= innerDepth && parser.getName().equals(ImsConfig.EXTRA_CHANGED_ITEM)) {
                TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.LevelListDrawableItem);
                int low = a.getInt(1, 0);
                int high = a.getInt(2, 0);
                int drawableRes = a.getResourceId(0, 0);
                a.recycle();
                if (high >= 0) {
                    if (drawableRes != 0) {
                        dr = r.getDrawable(drawableRes, theme);
                    } else {
                        do {
                            type = parser.next();
                        } while (type == 4);
                        if (type == 2) {
                            dr = Drawable.createFromXmlInner(r, parser, attrs, theme);
                        } else {
                            throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
                        }
                    }
                    this.mLevelListState.addLevel(low, high, dr);
                } else {
                    throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'maxLevel' attribute");
                }
            }
        }
        onLevelChange(getLevel());
    }

    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mLevelListState.mutate();
            this.mMutated = true;
        }
        return this;
    }

    /* access modifiers changed from: package-private */
    @Override // android.graphics.drawable.DrawableContainer
    public LevelListState cloneConstantState() {
        return new LevelListState(this.mLevelListState, this, null);
    }

    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    /* access modifiers changed from: private */
    public static final class LevelListState extends DrawableContainer.DrawableContainerState {
        private int[] mHighs;
        private int[] mLows;

        LevelListState(LevelListState orig, LevelListDrawable owner, Resources res) {
            super(orig, owner, res);
            if (orig != null) {
                this.mLows = orig.mLows;
                this.mHighs = orig.mHighs;
                return;
            }
            this.mLows = new int[getCapacity()];
            this.mHighs = new int[getCapacity()];
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void mutate() {
            this.mLows = (int[]) this.mLows.clone();
            this.mHighs = (int[]) this.mHighs.clone();
        }

        public void addLevel(int low, int high, Drawable drawable) {
            int pos = addChild(drawable);
            this.mLows[pos] = low;
            this.mHighs[pos] = high;
        }

        public int indexOfLevel(int level) {
            int[] lows = this.mLows;
            int[] highs = this.mHighs;
            int N = getChildCount();
            for (int i = 0; i < N; i++) {
                if (level >= lows[i] && level <= highs[i]) {
                    return i;
                }
            }
            return -1;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new LevelListDrawable(this, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new LevelListDrawable(this, res);
        }

        @Override // android.graphics.drawable.DrawableContainer.DrawableContainerState
        public void growArray(int oldSize, int newSize) {
            super.growArray(oldSize, newSize);
            int[] newInts = new int[newSize];
            System.arraycopy(this.mLows, 0, newInts, 0, oldSize);
            this.mLows = newInts;
            int[] newInts2 = new int[newSize];
            System.arraycopy(this.mHighs, 0, newInts2, 0, oldSize);
            this.mHighs = newInts2;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.DrawableContainer
    public void setConstantState(DrawableContainer.DrawableContainerState state) {
        super.setConstantState(state);
        if (state instanceof LevelListState) {
            this.mLevelListState = (LevelListState) state;
        }
    }

    private LevelListDrawable(LevelListState state, Resources res) {
        setConstantState(new LevelListState(state, this, res));
        onLevelChange(getLevel());
    }
}

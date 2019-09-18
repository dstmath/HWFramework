package android.graphics.drawable;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.DrawableContainer;
import android.hwtheme.HwThemeManager;
import android.util.AttributeSet;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class LevelListDrawable extends DrawableContainer {
    private LevelListState mLevelListState;
    private boolean mMutated;

    private static final class LevelListState extends DrawableContainer.DrawableContainerState {
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
        public void mutate() {
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

        public Drawable newDrawable() {
            return new LevelListDrawable(this, null);
        }

        public Drawable newDrawable(Resources res) {
            return new LevelListDrawable(this, res);
        }

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
    public boolean onLevelChange(int level) {
        if (selectDrawable(this.mLevelListState.indexOfLevel(level))) {
            return true;
        }
        return super.onLevelChange(level);
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        updateDensity(r);
        inflateChildElements(r, parser, attrs, theme);
    }

    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        Drawable dr;
        int next;
        int type;
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int next2 = parser.next();
            int type2 = next2;
            if (next2 == 1) {
                break;
            }
            int depth = parser.getDepth();
            int depth2 = depth;
            if (depth < innerDepth && type2 == 3) {
                break;
            } else if (type2 == 2 && depth2 <= innerDepth && parser.getName().equals(HwThemeManager.TAG_ITEM)) {
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
                            next = parser.next();
                            type = next;
                        } while (next == 4);
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

    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mLevelListState.mutate();
            this.mMutated = true;
        }
        return this;
    }

    /* access modifiers changed from: package-private */
    public LevelListState cloneConstantState() {
        return new LevelListState(this.mLevelListState, this, null);
    }

    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    /* access modifiers changed from: protected */
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

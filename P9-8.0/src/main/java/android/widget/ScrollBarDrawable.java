package android.widget;

import android.graphics.Canvas;
import android.graphics.Canvas.EdgeType;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import com.android.internal.widget.ScrollBarUtils;

public class ScrollBarDrawable extends Drawable implements Callback {
    private int mAlpha = 255;
    private boolean mAlwaysDrawHorizontalTrack;
    private boolean mAlwaysDrawVerticalTrack;
    private boolean mBoundsChanged;
    private ColorFilter mColorFilter;
    private int mExtent;
    private boolean mHasSetAlpha;
    private boolean mHasSetColorFilter;
    private Drawable mHorizontalThumb;
    private Drawable mHorizontalTrack;
    private boolean mMutated;
    private int mOffset;
    private int mRange;
    private boolean mRangeChanged;
    private boolean mVertical;
    private Drawable mVerticalThumb;
    private Drawable mVerticalTrack;

    public void setAlwaysDrawHorizontalTrack(boolean alwaysDrawTrack) {
        this.mAlwaysDrawHorizontalTrack = alwaysDrawTrack;
    }

    public void setAlwaysDrawVerticalTrack(boolean alwaysDrawTrack) {
        this.mAlwaysDrawVerticalTrack = alwaysDrawTrack;
    }

    public boolean getAlwaysDrawVerticalTrack() {
        return this.mAlwaysDrawVerticalTrack;
    }

    public boolean getAlwaysDrawHorizontalTrack() {
        return this.mAlwaysDrawHorizontalTrack;
    }

    public void setParameters(int range, int offset, int extent, boolean vertical) {
        if (this.mVertical != vertical) {
            this.mVertical = vertical;
            this.mBoundsChanged = true;
        }
        if (this.mRange != range || this.mOffset != offset || this.mExtent != extent) {
            this.mRange = range;
            this.mOffset = offset;
            this.mExtent = extent;
            this.mRangeChanged = true;
        }
    }

    public void draw(Canvas canvas) {
        boolean vertical = this.mVertical;
        int extent = this.mExtent;
        int range = this.mRange;
        boolean drawTrack = true;
        boolean drawThumb = true;
        if (extent <= 0 || range <= extent) {
            drawTrack = vertical ? this.mAlwaysDrawVerticalTrack : this.mAlwaysDrawHorizontalTrack;
            drawThumb = false;
        }
        Rect r = getBounds();
        if (!canvas.quickReject((float) r.left, (float) r.top, (float) r.right, (float) r.bottom, EdgeType.AA)) {
            if (drawTrack) {
                drawTrack(canvas, r, vertical);
            }
            if (drawThumb) {
                int scrollBarLength = vertical ? r.height() : r.width();
                int thumbLength = ScrollBarUtils.getThumbLength(scrollBarLength, vertical ? r.width() : r.height(), extent, range);
                drawThumb(canvas, r, ScrollBarUtils.getThumbOffset(scrollBarLength, thumbLength, extent, range, this.mOffset), thumbLength, vertical);
            }
        }
    }

    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.mBoundsChanged = true;
    }

    public boolean isStateful() {
        if ((this.mVerticalTrack == null || !this.mVerticalTrack.isStateful()) && ((this.mVerticalThumb == null || !this.mVerticalThumb.isStateful()) && ((this.mHorizontalTrack == null || !this.mHorizontalTrack.isStateful()) && (this.mHorizontalThumb == null || !this.mHorizontalThumb.isStateful())))) {
            return super.isStateful();
        }
        return true;
    }

    protected boolean onStateChange(int[] state) {
        boolean changed = super.onStateChange(state);
        if (this.mVerticalTrack != null) {
            changed |= this.mVerticalTrack.setState(state);
        }
        if (this.mVerticalThumb != null) {
            changed |= this.mVerticalThumb.setState(state);
        }
        if (this.mHorizontalTrack != null) {
            changed |= this.mHorizontalTrack.setState(state);
        }
        if (this.mHorizontalThumb != null) {
            return changed | this.mHorizontalThumb.setState(state);
        }
        return changed;
    }

    private void drawTrack(Canvas canvas, Rect bounds, boolean vertical) {
        Drawable track;
        if (vertical) {
            track = this.mVerticalTrack;
        } else {
            track = this.mHorizontalTrack;
        }
        if (track != null) {
            if (this.mBoundsChanged) {
                track.setBounds(bounds);
            }
            track.draw(canvas);
        }
    }

    private void drawThumb(Canvas canvas, Rect bounds, int offset, int length, boolean vertical) {
        boolean changed = !this.mRangeChanged ? this.mBoundsChanged : true;
        Drawable thumb;
        if (vertical) {
            if (this.mVerticalThumb != null) {
                thumb = this.mVerticalThumb;
                if (changed) {
                    thumb.setBounds(bounds.left, bounds.top + offset, bounds.right, (bounds.top + offset) + length);
                }
                thumb.draw(canvas);
            }
        } else if (this.mHorizontalThumb != null) {
            thumb = this.mHorizontalThumb;
            if (changed) {
                thumb.setBounds(bounds.left + offset, bounds.top, (bounds.left + offset) + length, bounds.bottom);
            }
            thumb.draw(canvas);
        }
    }

    public void setVerticalThumbDrawable(Drawable thumb) {
        if (this.mVerticalThumb != null) {
            this.mVerticalThumb.setCallback(null);
        }
        propagateCurrentState(thumb);
        this.mVerticalThumb = thumb;
    }

    public void setVerticalTrackDrawable(Drawable track) {
        if (this.mVerticalTrack != null) {
            this.mVerticalTrack.setCallback(null);
        }
        propagateCurrentState(track);
        this.mVerticalTrack = track;
    }

    public void setHorizontalThumbDrawable(Drawable thumb) {
        if (this.mHorizontalThumb != null) {
            this.mHorizontalThumb.setCallback(null);
        }
        propagateCurrentState(thumb);
        this.mHorizontalThumb = thumb;
    }

    public void setHorizontalTrackDrawable(Drawable track) {
        if (this.mHorizontalTrack != null) {
            this.mHorizontalTrack.setCallback(null);
        }
        propagateCurrentState(track);
        this.mHorizontalTrack = track;
    }

    private void propagateCurrentState(Drawable d) {
        if (d != null) {
            if (this.mMutated) {
                d.mutate();
            }
            d.setState(getState());
            d.setCallback(this);
            if (this.mHasSetAlpha) {
                d.setAlpha(this.mAlpha);
            }
            if (this.mHasSetColorFilter) {
                d.setColorFilter(this.mColorFilter);
            }
        }
    }

    public int getSize(boolean vertical) {
        int i = 0;
        if (vertical) {
            if (this.mVerticalTrack != null) {
                i = this.mVerticalTrack.getIntrinsicWidth();
            } else if (this.mVerticalThumb != null) {
                i = this.mVerticalThumb.getIntrinsicWidth();
            }
            return i;
        }
        if (this.mHorizontalTrack != null) {
            i = this.mHorizontalTrack.getIntrinsicHeight();
        } else if (this.mHorizontalThumb != null) {
            i = this.mHorizontalThumb.getIntrinsicHeight();
        }
        return i;
    }

    public ScrollBarDrawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            if (this.mVerticalTrack != null) {
                this.mVerticalTrack.mutate();
            }
            if (this.mVerticalThumb != null) {
                this.mVerticalThumb.mutate();
            }
            if (this.mHorizontalTrack != null) {
                this.mHorizontalTrack.mutate();
            }
            if (this.mHorizontalThumb != null) {
                this.mHorizontalThumb.mutate();
            }
            this.mMutated = true;
        }
        return this;
    }

    public void setAlpha(int alpha) {
        this.mAlpha = alpha;
        this.mHasSetAlpha = true;
        if (this.mVerticalTrack != null) {
            this.mVerticalTrack.setAlpha(alpha);
        }
        if (this.mVerticalThumb != null) {
            this.mVerticalThumb.setAlpha(alpha);
        }
        if (this.mHorizontalTrack != null) {
            this.mHorizontalTrack.setAlpha(alpha);
        }
        if (this.mHorizontalThumb != null) {
            this.mHorizontalThumb.setAlpha(alpha);
        }
    }

    public int getAlpha() {
        return this.mAlpha;
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mColorFilter = colorFilter;
        this.mHasSetColorFilter = true;
        if (this.mVerticalTrack != null) {
            this.mVerticalTrack.setColorFilter(colorFilter);
        }
        if (this.mVerticalThumb != null) {
            this.mVerticalThumb.setColorFilter(colorFilter);
        }
        if (this.mHorizontalTrack != null) {
            this.mHorizontalTrack.setColorFilter(colorFilter);
        }
        if (this.mHorizontalThumb != null) {
            this.mHorizontalThumb.setColorFilter(colorFilter);
        }
    }

    public ColorFilter getColorFilter() {
        return this.mColorFilter;
    }

    public int getOpacity() {
        return -3;
    }

    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }

    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }

    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }

    public String toString() {
        return "ScrollBarDrawable: range=" + this.mRange + " offset=" + this.mOffset + " extent=" + this.mExtent + (this.mVertical ? " V" : " H");
    }
}

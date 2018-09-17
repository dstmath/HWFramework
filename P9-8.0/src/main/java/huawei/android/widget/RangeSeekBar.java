package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import java.security.InvalidParameterException;

public class RangeSeekBar extends SeekBar {
    private int mRangeHigh;
    private int mRangeLow;

    public RangeSeekBar(Context context) {
        this(context, null);
    }

    public RangeSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 16842875);
    }

    public RangeSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mRangeLow = -1;
        this.mRangeHigh = -1;
    }

    public void setRange(int low, int high) {
        if (low <= 0 || high <= 0 || low <= high) {
            this.mRangeLow = low;
            this.mRangeHigh = high;
            return;
        }
        throw new InvalidParameterException("Param error: low=" + low + ", high=" + high);
    }

    public boolean setProgressInternal(int progress, boolean fromUser, boolean animate) {
        int p = progress;
        if (this.mRangeLow > 0 && progress < this.mRangeLow) {
            p = this.mRangeLow;
        } else if (this.mRangeHigh > 0 && progress > this.mRangeHigh) {
            p = this.mRangeHigh;
        }
        return super.setProgressInternal(p, fromUser, animate);
    }
}

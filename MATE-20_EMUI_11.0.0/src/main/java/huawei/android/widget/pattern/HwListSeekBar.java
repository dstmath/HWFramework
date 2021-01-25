package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.Locale;

public class HwListSeekBar extends FrameLayout {
    public static final int ELE_LEFT_ICON = 2;
    public static final int ELE_RIGHT_ICON = 3;
    public static final int ELE_SUBTITLE = 1;
    public static final int ELE_TITLE = 0;
    public static final int ICON_ICON = 3;
    public static final int ICON_TITLE = 0;
    public static final int RANGE_RANGE = 1;
    public static final int TITLE_VALUE = 2;
    private ImageView mIvIconLeft;
    private ImageView mIvIconRight;
    private SeekBar mSeekBar;
    private TextView mTvSubTitle;
    private TextView mTvTitle;
    private int mType;

    public HwListSeekBar(Context context) {
        this(context, null);
    }

    public HwListSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwListSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwListSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setType(int type) {
        this.mType = type;
        if (type == 0) {
            processIconTitle();
        } else if (type == 1) {
            processRangeRange();
        } else if (type == 2) {
            processTitleValue();
        } else if (type == 3) {
            processIconIcon();
        }
    }

    private void processIconTitle() {
        LayoutInflater.from(getContext()).inflate(ResLoaderUtil.getLayoutId(getContext(), "hwpattern_seekbar_one"), this);
        this.mTvTitle = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "list_seekbar_title"));
        this.mIvIconLeft = (ImageView) findViewById(ResLoaderUtil.getViewId(getContext(), "list_seekbar_icon_left"));
        this.mSeekBar = (SeekBar) findViewById(ResLoaderUtil.getViewId(getContext(), "list_seekBar"));
        if (isUrLanguage()) {
            this.mTvTitle.setGravity(3);
        }
    }

    private void processRangeRange() {
        LayoutInflater.from(getContext()).inflate(ResLoaderUtil.getLayoutId(getContext(), "hwpattern_seekbar_two"), this);
        this.mTvTitle = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "list_seekbar_title"));
        this.mTvSubTitle = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "list_seekbar_subtitle"));
        this.mSeekBar = (SeekBar) findViewById(ResLoaderUtil.getViewId(getContext(), "list_seekBar"));
        if (isUrLanguage()) {
            this.mTvTitle.setGravity(3);
            this.mTvSubTitle.setGravity(5);
        }
    }

    private void processTitleValue() {
        LayoutInflater.from(getContext()).inflate(ResLoaderUtil.getLayoutId(getContext(), "hwpattern_seekbar_three"), this);
        this.mTvTitle = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "list_seekbar_title"));
        this.mTvSubTitle = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "list_seekbar_subtitle"));
        this.mSeekBar = (SeekBar) findViewById(ResLoaderUtil.getViewId(getContext(), "list_seekBar"));
        if (isUrLanguage()) {
            this.mTvTitle.setGravity(3);
            this.mTvSubTitle.setGravity(5);
        }
    }

    private void processIconIcon() {
        LayoutInflater.from(getContext()).inflate(ResLoaderUtil.getLayoutId(getContext(), "hwpattern_seekbar_four"), this);
        this.mIvIconLeft = (ImageView) findViewById(ResLoaderUtil.getViewId(getContext(), "list_seekbar_icon_left"));
        this.mIvIconRight = (ImageView) findViewById(ResLoaderUtil.getViewId(getContext(), "list_seekbar_icon_right"));
        this.mSeekBar = (SeekBar) findViewById(ResLoaderUtil.getViewId(getContext(), "list_seekBar"));
    }

    public void setSeekBarTip(boolean isDigitalScale, int stepNum, boolean isBubbleTip) {
        SeekBar seekBar = this.mSeekBar;
        if (seekBar != null && (seekBar instanceof huawei.android.widget.SeekBar)) {
            ((huawei.android.widget.SeekBar) seekBar).setTip(isDigitalScale, stepNum, isBubbleTip);
        }
    }

    public void showSeekBarTip() {
        SeekBar seekBar = this.mSeekBar;
        if (seekBar != null && (seekBar instanceof huawei.android.widget.SeekBar)) {
            ReflectUtil.setObject("mIsShowPopWindow", seekBar, true, huawei.android.widget.SeekBar.class);
        }
    }

    public void hideSeekBarTip() {
        SeekBar seekBar = this.mSeekBar;
        if (seekBar != null && (seekBar instanceof huawei.android.widget.SeekBar)) {
            ReflectUtil.setObject("mIsShowPopWindow", seekBar, false, huawei.android.widget.SeekBar.class);
        }
    }

    public void setProgress(int progress) {
        SeekBar seekBar = this.mSeekBar;
        if (seekBar != null) {
            seekBar.setProgress(progress);
        }
    }

    public void setElementTitle(CharSequence text, int elementTag) {
        TextView target;
        if (elementTag == 0) {
            target = this.mTvTitle;
        } else if (elementTag == 1) {
            target = this.mTvSubTitle;
        } else {
            return;
        }
        if (target != null) {
            target.setText(text);
        }
    }

    private boolean isUrLanguage() {
        return "ur".equals(Locale.getDefault().getLanguage());
    }

    public void setElementVisibility(boolean isVisible, int elementTag) {
        View target = null;
        int viewVisible = 0;
        if (this.mType == 1) {
            viewVisible = isVisible ? 0 : 8;
            if (elementTag == 0) {
                target = this.mTvTitle;
            } else if (elementTag == 1) {
                target = this.mTvSubTitle;
            } else {
                return;
            }
        }
        if (target != null) {
            target.setVisibility(viewVisible);
        }
    }

    public void setElementImageDrawable(Drawable drawable, int elementTag) {
        ImageView target;
        if (elementTag == 2) {
            target = this.mIvIconLeft;
        } else if (elementTag == 3) {
            target = this.mIvIconRight;
        } else {
            return;
        }
        if (target != null) {
            target.setImageDrawable(drawable);
        }
    }

    public void setElementImageResource(int resId, int elementTag) {
        ImageView target;
        if (elementTag == 2) {
            target = this.mIvIconLeft;
        } else if (elementTag == 3) {
            target = this.mIvIconRight;
        } else {
            return;
        }
        if (target != null) {
            target.setImageResource(resId);
        }
    }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
        SeekBar seekBar = this.mSeekBar;
        if (seekBar != null && listener != null) {
            seekBar.setOnSeekBarChangeListener(listener);
        }
    }
}

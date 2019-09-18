package android.preference;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

public class SeekBarDialogPreference extends DialogPreference {
    private final Drawable mMyIcon;

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        createActionButtons();
        this.mMyIcon = getDialogIcon();
        setDialogIcon((Drawable) null);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 17891516);
    }

    public SeekBarDialogPreference(Context context) {
        this(context, null);
    }

    public void createActionButtons() {
        setPositiveButtonText(17039370);
        setNegativeButtonText(17039360);
    }

    /* access modifiers changed from: protected */
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        ImageView iconView = (ImageView) view.findViewById(16908294);
        if (this.mMyIcon != null) {
            iconView.setImageDrawable(this.mMyIcon);
        } else {
            iconView.setVisibility(8);
        }
    }

    protected static SeekBar getSeekBar(View dialogView) {
        return (SeekBar) dialogView.findViewById(16909298);
    }
}

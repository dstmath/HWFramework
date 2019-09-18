package android.widget;

import android.content.res.Resources;

public interface ThemedSpinnerAdapter extends SpinnerAdapter {
    Resources.Theme getDropDownViewTheme();

    void setDropDownViewTheme(Resources.Theme theme);
}

package android.widget;

import android.content.res.Resources.Theme;

public interface ThemedSpinnerAdapter extends SpinnerAdapter {
    Theme getDropDownViewTheme();

    void setDropDownViewTheme(Theme theme);
}

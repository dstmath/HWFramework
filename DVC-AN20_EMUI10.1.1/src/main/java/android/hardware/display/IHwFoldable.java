package android.hardware.display;

import android.graphics.Rect;

public interface IHwFoldable {
    int getDisplayState();

    Rect getScreenDispRect(int i);

    boolean isFoldable();

    int setDisplayState(int i);
}

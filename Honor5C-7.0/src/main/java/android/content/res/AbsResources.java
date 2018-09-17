package android.content.res;

import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

public class AbsResources {
    public void setPackageName(String name) {
    }

    protected void setHwTheme(Configuration config) {
    }

    public Bitmap getThemeBitmap(TypedValue value, int id) throws NotFoundException {
        return null;
    }

    public Bitmap getThemeBitmap(TypedValue value, int id, Rect padding) throws NotFoundException {
        return null;
    }

    public Bitmap addShortcutBackgroud(Bitmap bmpSrc) {
        return null;
    }

    public Bitmap getThemeIconByName(String name) {
        return null;
    }

    protected Drawable handleAddIconBackground(Resources res, int id, Drawable dr) {
        return dr;
    }

    public Drawable getDrawableForDynamic(String packageName, String iconName) throws NotFoundException {
        return null;
    }

    protected void clearTypedArray(TypedArray attrs, int len) {
        int index = 0;
        for (int i = 0; i < len; i++) {
            attrs.mData[index + 0] = 0;
            index += 6;
        }
    }

    protected CharSequence serbianSyrillic2Latin(CharSequence res) {
        return res;
    }

    protected CharSequence[] serbianSyrillic2Latin(CharSequence[] res) {
        return res;
    }

    protected String serbianSyrillic2Latin(String res) {
        return res;
    }

    protected String[] serbianSyrillic2Latin(String[] res) {
        return res;
    }

    public void preloadHwThemeZipsAndSomeIcons(int currentUserId) {
    }

    public void clearHwThemeZipsAndSomeIcons() {
    }
}

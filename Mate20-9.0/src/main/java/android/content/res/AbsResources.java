package android.content.res;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

public class AbsResources {
    public void setPackageName(String name) {
    }

    /* access modifiers changed from: protected */
    public void setHwTheme(Configuration config) {
    }

    public Bitmap getThemeBitmap(TypedValue value, int id) throws Resources.NotFoundException {
        return null;
    }

    public Bitmap getThemeBitmap(TypedValue value, int id, Rect padding) throws Resources.NotFoundException {
        return null;
    }

    public Bitmap addShortcutBackgroud(Bitmap bmpSrc) {
        return null;
    }

    public Bitmap getThemeIconByName(String name) {
        return null;
    }

    /* access modifiers changed from: protected */
    public Drawable handleAddIconBackground(Resources res, int id, Drawable dr) {
        return dr;
    }

    public Drawable getDrawableForDynamic(String packageName, String iconName) throws Resources.NotFoundException {
        return null;
    }

    /* access modifiers changed from: protected */
    public void clearTypedArray(TypedArray attrs, int len) {
        int index = 0;
        for (int i = 0; i < len; i++) {
            attrs.mData[index + 0] = 0;
            index += 6;
        }
    }

    /* access modifiers changed from: protected */
    public CharSequence serbianSyrillic2Latin(CharSequence res) {
        return res;
    }

    /* access modifiers changed from: protected */
    public CharSequence[] serbianSyrillic2Latin(CharSequence[] res) {
        return res;
    }

    /* access modifiers changed from: protected */
    public String serbianSyrillic2Latin(String res) {
        return res;
    }

    /* access modifiers changed from: protected */
    public String[] serbianSyrillic2Latin(String[] res) {
        return res;
    }

    public void preloadHwThemeZipsAndSomeIcons(int currentUserId) {
    }

    public void clearHwThemeZipsAndSomeIcons() {
    }
}

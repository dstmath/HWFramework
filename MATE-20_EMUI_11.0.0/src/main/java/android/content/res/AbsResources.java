package android.content.res;

import android.content.res.AbsResourcesImpl;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;

public class AbsResources {
    public static final String TAG = "AbsResources";

    public void setResPackageName(String name) {
    }

    /* access modifiers changed from: protected */
    public void setHwTheme(Configuration config) {
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
            index += 7;
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

    public int rebuildSpecialDimens(int id, TypedValue value, int res) {
        Log.e(TAG, "In rebuildSpecialDimens This should never happen , so we should log a error even if we're not debugging.");
        return 0;
    }

    public AbsResourcesImpl.ThemeColor getColor(Resources res, TypedValue value, int id) {
        return null;
    }

    public boolean isSRLocale() {
        return false;
    }
}

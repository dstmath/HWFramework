package android.content.res;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class AbsResourcesImpl {

    public static class ThemeColor {
        public int mColor;
        public boolean mIsThemed;

        public ThemeColor(int color, boolean isThemed) {
            this.mColor = color;
            this.mIsThemed = isThemed;
        }
    }

    public static class ThemeResource {
        public String packageName;
        public String resName;

        public ThemeResource(String packageName2, String resName2) {
            this.packageName = packageName2;
            this.resName = resName2;
        }
    }

    /* access modifiers changed from: protected */
    public Drawable loadDrawable(Resources wrapper, TypedValue value, int id, Resources.Theme theme, boolean useCache) throws Resources.NotFoundException {
        return null;
    }

    public void updateConfiguration(Configuration config, int configChanges) {
    }

    /* access modifiers changed from: protected */
    public Drawable handleAddIconBackground(Resources res, int id, Drawable dr) {
        return dr;
    }

    /* access modifiers changed from: package-private */
    public DisplayMetrics getDisplayMetrics() {
        return null;
    }

    public DisplayMetrics hwGetDisplayMetrics() {
        return null;
    }

    /* access modifiers changed from: protected */
    public String getDeepThemeType() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public String getResourcePackageName(int resid) throws Resources.NotFoundException {
        return null;
    }

    /* access modifiers changed from: package-private */
    public String getResourceName(int resid) throws Resources.NotFoundException {
        return null;
    }

    /* access modifiers changed from: package-private */
    public String getResourceEntryName(int resid) throws Resources.NotFoundException {
        return null;
    }

    /* access modifiers changed from: package-private */
    public Configuration getConfiguration() {
        return null;
    }

    public void preloadHwThemeZipsAndSomeIcons(int currentUserId) {
    }

    public void clearHwThemeZipsAndSomeIcons() {
    }

    /* access modifiers changed from: protected */
    public void setHwTheme(Configuration config) {
    }

    /* access modifiers changed from: protected */
    public void initDeepTheme() {
    }

    public void setResourcesImpl(ResourcesImpl resourcesImpl) {
    }

    public Bitmap addShortcutBackgroud(Bitmap bmpSrc) {
        return null;
    }

    public Bitmap getThemeIconByName(String name) {
        return null;
    }

    public void setPackageName(String name) {
    }

    public Drawable getJoinBitmap(Drawable srcDraw, int backgroundId) {
        return null;
    }

    public ThemeResource getThemeResource(int id, String packageName) {
        return null;
    }

    public String getThemeDir() {
        return null;
    }

    public ThemeColor getThemeColor(TypedValue value, int id) throws Resources.NotFoundException {
        return null;
    }

    public void initResource() {
    }

    public Drawable getDrawableForDynamic(Resources res, String packageName, String iconName) throws Resources.NotFoundException {
        return null;
    }

    public boolean isInMultiDpiWhiteList(String packageName) {
        return false;
    }

    /* access modifiers changed from: protected */
    public Bundle getMultidpiInfo(String packageName) {
        return null;
    }
}

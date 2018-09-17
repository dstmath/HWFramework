package android.content.res;

import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
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

        public ThemeResource(String packageName, String resName) {
            this.packageName = packageName;
            this.resName = resName;
        }
    }

    protected Drawable loadDrawable(Resources wrapper, TypedValue value, int id, Theme theme, boolean useCache) throws NotFoundException {
        return null;
    }

    public void updateConfiguration(Configuration config, int configChanges) {
    }

    protected Drawable handleAddIconBackground(Resources res, int id, Drawable dr) {
        return dr;
    }

    public DisplayMetrics getDisplayMetrics() {
        return null;
    }

    protected String getDeepThemeType() {
        return null;
    }

    String getResourcePackageName(int resid) throws NotFoundException {
        return null;
    }

    String getResourceName(int resid) throws NotFoundException {
        return null;
    }

    String getResourceEntryName(int resid) throws NotFoundException {
        return null;
    }

    Configuration getConfiguration() {
        return null;
    }

    public void preloadHwThemeZipsAndSomeIcons(int currentUserId) {
    }

    public void clearHwThemeZipsAndSomeIcons() {
    }

    protected void setHwTheme(Configuration config) {
    }

    protected void initDeepTheme() {
    }

    protected ConfigurationBoundResourceCache<ComplexColor> getComplexColorCache() {
        return null;
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

    public void setResourcesPackageName(String name) {
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

    public ThemeColor getThemeColor(TypedValue value, int id) throws NotFoundException {
        return null;
    }

    public void initResource() {
    }

    public void checkChangedNameFile() {
    }

    public Drawable getDrawableForDynamic(Resources res, String packageName, String iconName) throws NotFoundException {
        return null;
    }

    public boolean isInMultiDpiWhiteList(String packageName) {
        return false;
    }

    protected Bundle getMultidpiInfo(String packageName) {
        return null;
    }
}

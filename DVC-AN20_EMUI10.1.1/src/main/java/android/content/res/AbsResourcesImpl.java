package android.content.res;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import java.util.ArrayList;
import java.util.Objects;

public class AbsResourcesImpl {

    public static class ThemeColor {
        public int mColor;
        public boolean mIsThemed;

        public ThemeColor(int color, boolean isThemed) {
            this.mColor = color;
            this.mIsThemed = isThemed;
        }
    }

    public static final class ThemedKey {
        public int id;
        private final int mHashCode = Objects.hash(Integer.valueOf(this.id), Integer.valueOf(this.type));
        public int type;

        public ThemedKey(int thatId, int thatType) {
            this.id = thatId;
            this.type = thatType;
        }

        public boolean equals(Object otherObj) {
            if (!(otherObj instanceof ThemedKey)) {
                return false;
            }
            ThemedKey other = (ThemedKey) otherObj;
            if (this.id == other.id && this.type == other.type) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return this.mHashCode;
        }
    }

    public static final class ThemedValue {
        public int data;
        public int type;

        public ThemedValue(int thatType, int thatData) {
            this.type = thatType;
            this.data = thatData;
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
    public Drawable loadDrawable(Resources wrapper, TypedValue value, int id, Resources.Theme theme, boolean isUseCache) throws Resources.NotFoundException {
        return null;
    }

    /* access modifiers changed from: protected */
    public Drawable getThemeDrawable(TypedValue value, int id, Resources res, String packageName, String file) throws Resources.NotFoundException {
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

    public Bitmap getThemeBitmap(Resources res, TypedValue value, int id, Rect padding) throws Resources.NotFoundException {
        return null;
    }

    public void setResourcesImpl(ResourcesImplEx resourcesImpl) {
    }

    public Bitmap addShortcutBackgroud(Bitmap bmpSrc) {
        return null;
    }

    public Bitmap getThemeIconByName(String name) {
        return null;
    }

    public void setResImplPackageName(String name) {
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

    public ThemedValue getThemeDimension(TypedValue value, int id) throws Resources.NotFoundException {
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

    public int[] updateDpiConfiguration(Configuration configuration, DisplayMetrics metrics) {
        return new int[4];
    }

    public void printErrorResource() {
    }

    public ArrayList<String> getDataThemePackages() {
        return new ArrayList<>(0);
    }

    public boolean removeIconCache(String packageName) {
        return false;
    }
}

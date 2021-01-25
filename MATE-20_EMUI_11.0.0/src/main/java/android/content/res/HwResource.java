package android.content.res;

import android.content.res.AbsResourcesImpl;
import android.hwtheme.HwThemeManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.content.res.ResourcesImplAdapter;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.text.HwTextUtils;
import com.huawei.utils.HwPartResourceUtils;

public class HwResource extends AbsResources {
    static final String TAG = "HwResource";
    private static boolean sIsSerbiaLocale = false;
    private static boolean sIsSupportLockDpi = SystemPropertiesEx.getBoolean("ro.config.auto_display_mode", true);
    private static int sLcdDpi = SystemPropertiesEx.getInt("ro.sf.lcd_density", 0);
    private static int sRealLcdDpi = SystemPropertiesEx.getInt("ro.sf.real_lcd_density", sLcdDpi);

    public AbsResourcesImpl.ThemeColor getColor(Resources res, TypedValue value, int id) {
        if (ResourcesImplAdapter.getResourcesImplEx(res) != null) {
            return ResourcesImplAdapter.getHwResourcesImpl(res).getThemeColor(value, id);
        }
        Log.e(TAG, "In HwResource#getThemeColor This should never happen, so we should log an error even if we're not debugging.");
        return null;
    }

    public int rebuildSpecialDimens(int id, TypedValue value, int res) {
        int dpi;
        int i;
        int resultRes = res;
        if (isRogOn() && isTypedValueInMillimeters(value.data)) {
            resultRes = (int) ((((float) resultRes) * getResolutionScale()) + 0.5f);
        }
        if ((id == HwPartResourceUtils.getResourceId("status_bar_height") || id == HwPartResourceUtils.getResourceId("status_bar_height_portrait") || id == HwPartResourceUtils.getResourceId("status_bar_height_landscape")) && !isLockDpi() && !isTypedValueInMillimetersOrPixel(value.data) && (dpi = getDpi()) > 0 && dpi != (i = sRealLcdDpi) && sIsSupportLockDpi) {
            resultRes = (((i * resultRes) + dpi) - 1) / dpi;
        }
        if ((id == HwPartResourceUtils.getResourceId("navigation_bar_height") || id == HwPartResourceUtils.getResourceId("navigation_bar_height_landscape")) && (res & 1) == 1) {
            return resultRes + 1;
        }
        return resultRes;
    }

    private boolean isLockDpi() {
        String pkgName = ActivityThreadEx.currentPackageName();
        Bundle multiDpiInfo = null;
        if (!TextUtils.isEmpty(pkgName)) {
            multiDpiInfo = HwThemeManager.getPreMultidpiInfo(pkgName);
        }
        if (multiDpiInfo == null) {
            return false;
        }
        return multiDpiInfo.getBoolean("LockDpi", false);
    }

    private boolean isTypedValueInMillimeters(int data) {
        if (5 == ((data >> 0) & 15)) {
            return true;
        }
        return false;
    }

    private boolean isTypedValueInMillimetersOrPixel(int data) {
        int type = (data >> 0) & 15;
        if (type == 5 || type == 0) {
            return true;
        }
        return false;
    }

    private boolean isRogOn() {
        return SystemPropertiesEx.getInt("persist.sys.rog.configmode", 0) == 1;
    }

    private float getResolutionScale() {
        int dpi = getDpi();
        int realDpi = getRealDpi();
        if (dpi == 0 || realDpi == dpi) {
            return 1.0f;
        }
        return ((float) realDpi) / ((float) dpi);
    }

    private int getDpi() {
        return SystemPropertiesEx.getInt("persist.sys.dpi", sRealLcdDpi);
    }

    private int getRealDpi() {
        return SystemPropertiesEx.getInt("persist.sys.realdpi", getDpi());
    }

    public CharSequence serbianSyrillic2Latin(CharSequence res) {
        if (sIsSerbiaLocale) {
            return HwTextUtils.serbianSyrillic2Latin(res);
        }
        return res;
    }

    public CharSequence[] serbianSyrillic2Latin(CharSequence[] res) {
        if (sIsSerbiaLocale) {
            for (int i = 0; i < res.length; i++) {
                res[i] = HwTextUtils.serbianSyrillic2Latin(res[i]);
            }
        }
        return res;
    }

    public String serbianSyrillic2Latin(String res) {
        if (sIsSerbiaLocale) {
            return HwTextUtils.serbianSyrillic2Latin(res);
        }
        return res;
    }

    public String[] serbianSyrillic2Latin(String[] res) {
        if (sIsSerbiaLocale) {
            for (int i = 0; i < res.length; i++) {
                res[i] = HwTextUtils.serbianSyrillic2Latin(res[i]);
            }
        }
        return res;
    }

    public boolean isSRLocale() {
        return sIsSerbiaLocale;
    }

    public static void setIsSRLocale(boolean isSerbia) {
        if (sIsSerbiaLocale != isSerbia) {
            sIsSerbiaLocale = isSerbia;
        }
    }
}

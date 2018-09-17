package android.content.res;

import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.SetField;

public class ResourcesUtils extends EasyInvokeUtils {
    MethodObject<Drawable> loadDrawable;
    FieldObject<Object> mAccessLock;
    FieldObject<DrawableCache> mColorDrawableCache;
    FieldObject<DrawableCache> mDrawableCache;
    FieldObject<Boolean> mPreloading;
    FieldObject<Configuration> mTmpConfig;
    FieldObject<TypedValue> mTmpValue;
    FieldObject<Integer> sPreloadedDensity;

    @GetField(fieldObject = "mTmpConfig")
    public Configuration getTmpConfig(ResourcesImpl obj) {
        return (Configuration) getField(this.mTmpConfig, obj);
    }

    @GetField(fieldObject = "mTmpValue")
    public TypedValue getTmpValue(Resources obj) {
        return (TypedValue) getField(this.mTmpValue, obj);
    }

    @SetField(fieldObject = "mTmpValue")
    public void setTmpValue(Resources obj, TypedValue value) {
        setField(this.mTmpValue, obj, value);
    }

    @GetField(fieldObject = "mPreloading")
    public boolean getPreloading(ResourcesImpl obj) {
        return ((Boolean) getField(this.mPreloading, obj)).booleanValue();
    }

    @GetField(fieldObject = "mAccessLock")
    public Object getAccessLock(ResourcesImpl obj) {
        return getField(this.mAccessLock, obj);
    }

    @GetField(fieldObject = "sPreloadedDensity")
    public int getPreloadedDensity(ResourcesImpl obj) {
        return ((Integer) getField(this.sPreloadedDensity, obj)).intValue();
    }

    @GetField(fieldObject = "mDrawableCache")
    public DrawableCache getDrawableCache(ResourcesImpl obj) {
        return (DrawableCache) getField(this.mDrawableCache, obj);
    }

    @GetField(fieldObject = "mColorDrawableCache")
    public DrawableCache getColorDrawableCache(ResourcesImpl obj) {
        return (DrawableCache) getField(this.mColorDrawableCache, obj);
    }
}

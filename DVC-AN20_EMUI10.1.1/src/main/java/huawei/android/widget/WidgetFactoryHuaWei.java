package huawei.android.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import java.lang.reflect.InvocationTargetException;

public class WidgetFactoryHuaWei implements LayoutInflater.Factory {
    private static final Class[] CONSTRUCTOR_SIGNATURES = {Context.class, AttributeSet.class};
    private static final int INVALIDE_VALUE = -1;
    private static final boolean IS_DEBUG = false;
    private static final String TAG = "WidgetFactoryHuaWei";
    private Context mContext;
    private String mPackageName;

    public WidgetFactoryHuaWei(Context context, String packageName) {
        if (packageName == null || context == null) {
            throw new NullPointerException("Both of packageName and context can not be null");
        }
        this.mContext = context;
        this.mPackageName = packageName;
    }

    public View onCreateView(String name, Context context, AttributeSet attrs) {
        if (name == null || context == null || name.indexOf(".") == -1) {
            return null;
        }
        try {
            return (View) this.mContext.createPackageContext(this.mPackageName, 3).getClassLoader().loadClass(name).getConstructor(CONSTRUCTOR_SIGNATURES).newInstance(context, attrs);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "onCreateView : NameNotFoundException");
            return null;
        } catch (ClassNotFoundException e2) {
            Log.e(TAG, "onCreateView : ClassNotFoundException");
            return null;
        } catch (NoSuchMethodException e3) {
            Log.e(TAG, "onCreateView : NoSuchMethodException");
            return null;
        } catch (InstantiationException e4) {
            Log.e(TAG, "onCreateView : InstantiationException");
            return null;
        } catch (IllegalAccessException e5) {
            Log.e(TAG, "onCreateView : IllegalAccessException");
            return null;
        } catch (IllegalArgumentException e6) {
            Log.e(TAG, "onCreateView : IllegalArgumentException");
            return null;
        } catch (InvocationTargetException e7) {
            Log.e(TAG, "onCreateView : InvocationTargetException");
            return null;
        }
    }
}

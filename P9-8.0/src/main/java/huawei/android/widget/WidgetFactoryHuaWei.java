package huawei.android.widget;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater.Factory;
import android.view.View;
import java.lang.reflect.InvocationTargetException;

public class WidgetFactoryHuaWei implements Factory {
    private static final boolean DBG = false;
    private static final String TAG = "WidgetFactoryHuaWei";
    private static final Class[] mConstructorSignature = new Class[]{Context.class, AttributeSet.class};
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
        if (-1 == name.indexOf(".")) {
            return null;
        }
        try {
            return (View) this.mContext.createPackageContext(this.mPackageName, 3).getClassLoader().loadClass(name).getConstructor(mConstructorSignature).newInstance(new Object[]{context, attrs});
        } catch (NameNotFoundException e) {
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

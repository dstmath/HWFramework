package android.test;

import android.app.Activity;
import android.util.Log;
import java.lang.reflect.Field;

@Deprecated
public abstract class ActivityTestCase extends InstrumentationTestCase {
    private Activity mActivity;

    /* access modifiers changed from: protected */
    public Activity getActivity() {
        return this.mActivity;
    }

    /* access modifiers changed from: protected */
    public void setActivity(Activity testActivity) {
        this.mActivity = testActivity;
    }

    /* access modifiers changed from: protected */
    public void scrubClass(Class<?> testCaseClass) throws IllegalAccessException {
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (testCaseClass.isAssignableFrom(field.getDeclaringClass()) && !field.getType().isPrimitive() && (field.getModifiers() & 16) == 0) {
                try {
                    field.setAccessible(true);
                    field.set(this, null);
                } catch (Exception e) {
                    Log.d("TestCase", "Error: Could not nullify field!");
                }
                if (field.get(this) != null) {
                    Log.d("TestCase", "Error: Could not nullify field!");
                }
            }
        }
    }
}

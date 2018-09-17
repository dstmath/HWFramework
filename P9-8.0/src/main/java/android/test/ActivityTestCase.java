package android.test;

import android.app.Activity;
import android.util.Log;
import java.lang.reflect.Field;

@Deprecated
public abstract class ActivityTestCase extends InstrumentationTestCase {
    private Activity mActivity;

    protected Activity getActivity() {
        return this.mActivity;
    }

    protected void setActivity(Activity testActivity) {
        this.mActivity = testActivity;
    }

    protected void scrubClass(Class<?> testCaseClass) throws IllegalAccessException {
        for (Field field : getClass().getDeclaredFields()) {
            if (testCaseClass.isAssignableFrom(field.getDeclaringClass()) && (field.getType().isPrimitive() ^ 1) != 0 && (field.getModifiers() & 16) == 0) {
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

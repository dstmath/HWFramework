package huawei.android.widget.plume.action.interaction;

import android.content.Context;
import android.util.Log;
import android.view.View;
import huawei.android.widget.plume.util.ReflectUtil;
import java.lang.reflect.Method;

public abstract class UnifiedInteraction {
    private static final String ID = "id";
    private static final int LENGTH = 2;
    private static final String TAG = UnifiedInteraction.class.getSimpleName();
    protected Context mContext;
    protected View mTarget;

    public abstract void handleEvent(String str, String str2);

    protected UnifiedInteraction(Context context, View view) {
        this.mContext = context;
        this.mTarget = view;
    }

    /* access modifiers changed from: package-private */
    public InteractEvent getInteractEvent(String eventName, String value, String tagName, Class[] classArray) {
        InteractEvent event = new InteractEvent(eventName, value, tagName, classArray);
        if (event.mMethod != null && event.mTagId != 0) {
            return event;
        }
        String str = TAG;
        Log.e(str, "Plume: InteractEvent " + eventName + " lacks necessary information.");
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean handleCallback(InteractEvent event, Object[] objects) {
        if (event == null) {
            Log.e(TAG, "Plume: event is null.");
            return false;
        }
        if (event.mHost == null) {
            event.mHost = this.mTarget.getTag(event.mTagId);
        }
        if (event.mHost == null) {
            Log.e(TAG, "Plume: event.mHost is null.");
            return false;
        }
        Object object = ReflectUtil.invokeMethod(event.mHost, event.mMethod, objects);
        if (object instanceof Boolean) {
            return ((Boolean) object).booleanValue();
        }
        return false;
    }

    public class InteractEvent {
        private Object mHost;
        private Method mMethod;
        String mName;
        private int mTagId;

        private InteractEvent(String eventName, String value, String tagName, Class[] classArray) {
            this.mHost = null;
            this.mName = eventName;
            this.mTagId = getTagId(tagName);
            this.mMethod = getMethodByName(value, classArray);
        }

        private int getTagId(String tagName) {
            return UnifiedInteraction.this.mContext.getResources().getIdentifier(tagName, UnifiedInteraction.ID, UnifiedInteraction.this.mContext.getPackageName());
        }

        private Method getMethodByName(String value, Class[] classArray) {
            String[] res = value.split("\\$");
            if (res.length != 2) {
                Log.e(UnifiedInteraction.TAG, "Plume: Incorrect callback format. The standard format is className$methodName!");
                return null;
            }
            String className = res[0].trim();
            String methodName = res[1].trim();
            Class clazz = ReflectUtil.getClass(UnifiedInteraction.this.mContext, className);
            if (clazz != null) {
                return ReflectUtil.getMethod(methodName, classArray, clazz);
            }
            Log.e(UnifiedInteraction.TAG, "Plume: clazz is null.");
            return null;
        }
    }
}

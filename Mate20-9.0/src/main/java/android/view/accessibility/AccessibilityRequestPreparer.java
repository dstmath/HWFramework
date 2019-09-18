package android.view.accessibility;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

public abstract class AccessibilityRequestPreparer {
    public static final int REQUEST_TYPE_EXTRA_DATA = 1;
    private final int mRequestTypes;
    private final WeakReference<View> mViewRef;

    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestTypes {
    }

    private class ViewAttachStateListener implements View.OnAttachStateChangeListener {
        private ViewAttachStateListener() {
        }

        public void onViewAttachedToWindow(View v) {
        }

        public void onViewDetachedFromWindow(View v) {
            Context context = v.getContext();
            if (context != null) {
                ((AccessibilityManager) context.getSystemService(AccessibilityManager.class)).removeAccessibilityRequestPreparer(AccessibilityRequestPreparer.this);
            }
            v.removeOnAttachStateChangeListener(this);
        }
    }

    public abstract void onPrepareExtraData(int i, String str, Bundle bundle, Message message);

    public AccessibilityRequestPreparer(View view, int requestTypes) {
        if (view.isAttachedToWindow()) {
            this.mViewRef = new WeakReference<>(view);
            this.mRequestTypes = requestTypes;
            view.addOnAttachStateChangeListener(new ViewAttachStateListener());
            return;
        }
        throw new IllegalStateException("View must be attached to a window");
    }

    public View getView() {
        return (View) this.mViewRef.get();
    }
}

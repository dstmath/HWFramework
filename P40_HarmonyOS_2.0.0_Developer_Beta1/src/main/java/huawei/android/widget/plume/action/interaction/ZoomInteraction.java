package huawei.android.widget.plume.action.interaction;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import huawei.android.widget.HwOnZoomEventListener;
import huawei.android.widget.plume.action.interaction.UnifiedInteraction;

public class ZoomInteraction extends UnifiedInteraction {
    private static final String TAG = ZoomInteraction.class.getSimpleName();
    private HwOnZoomEventListener mOnZoomListener = null;
    private UnifiedInteraction.InteractEvent mZoomEvent = null;

    public ZoomInteraction(Context context, View view) {
        super(context, view);
    }

    @Override // huawei.android.widget.plume.action.interaction.UnifiedInteraction
    public void handleEvent(String eventName, String value) {
        if (eventName == null || value == null) {
            Log.e(TAG, "Plume: eventName or value is null.");
        } else if (!(this.mTarget instanceof ImageView)) {
            String str = TAG;
            Log.e(str, "Plume: " + this.mTarget.toString() + " can not be cast to ImageView, when executing handleEvent method.");
        } else if (this.mZoomEvent != null) {
            Log.e(TAG, "Plume: onZoom event already exists.");
        } else if (this.mOnZoomListener != null) {
            Log.e(TAG, "Plume: onZoom listener already exists.");
        } else {
            this.mZoomEvent = getInteractEvent(eventName, value, UnifiedInteractionConstants.TAG_ZOOM, new Class[]{Float.TYPE, MotionEvent.class});
            if (this.mZoomEvent == null) {
                Log.e(TAG, "Plume: event is null.");
            } else {
                setOnListener();
            }
        }
    }

    private void setOnListener() {
        this.mOnZoomListener = new HwOnZoomEventListener() {
            /* class huawei.android.widget.plume.action.interaction.ZoomInteraction.AnonymousClass1 */

            public boolean onZoom(float value, MotionEvent event) {
                ZoomInteraction zoomInteraction = ZoomInteraction.this;
                return zoomInteraction.handleCallback(zoomInteraction.mZoomEvent, new Object[]{Float.valueOf(value), event});
            }
        };
        if (!(this.mTarget instanceof ImageView)) {
            Log.e(TAG, "mTarget can not be cast to ImageView");
        } else {
            ((ImageView) this.mTarget).setOnZoomListener(this.mOnZoomListener);
        }
    }
}

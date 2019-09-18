package huawei.com.android.server.fingerprint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import com.android.server.gesture.GestureNavConst;

public class BackFingerprintView extends LinearLayout {
    static final String TAG = "BackFingerprintView";

    public BackFingerprintView(Context context) {
        super(context);
    }

    public BackFingerprintView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.argb(128.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO));
        Log.e(TAG, "fingerprintview onDraw");
    }
}

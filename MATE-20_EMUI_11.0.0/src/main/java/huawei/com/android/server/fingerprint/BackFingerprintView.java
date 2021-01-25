package huawei.com.android.server.fingerprint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

public class BackFingerprintView extends LinearLayout {
    private static final String TAG = "BackFingerprintView";

    public BackFingerprintView(Context context) {
        super(context);
    }

    public BackFingerprintView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.argb(128.0f, 0.0f, 0.0f, 0.0f));
            Log.i(TAG, "fingerprintview onDraw");
        }
    }
}

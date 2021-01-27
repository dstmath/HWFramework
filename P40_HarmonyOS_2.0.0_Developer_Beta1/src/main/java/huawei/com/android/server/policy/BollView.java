package huawei.com.android.server.policy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class BollView extends CallPanelView {
    public BollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean handleTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == 0 || action != 1) {
        }
        return super.onTouchEvent(event);
    }
}

package huawei.com.android.server.fingerprint;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class SuspensionButton extends RelativeLayout {
    private static final String PKGNAME_OF_KEYGUARD = "com.android.systemui";
    private static final String TAG = "SuspensionButton";
    private boolean mClickable = true;
    private float mEndX;
    private float mEndY;
    private ICallBack mHandleViewCallback;
    private int mHeight = -1;
    private boolean mMovable = false;
    private float mStartX;
    private float mStartY;

    public interface ICallBack {
        String getCurrentApp();

        void onButtonClick();

        void onButtonViewMoved(float f, float f2);

        void onConfigurationChanged(Configuration configuration);

        void userActivity();
    }

    public SuspensionButton(Context context) {
        super(context);
    }

    public SuspensionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initStatusBarHeight();
        Log.e(TAG, "SuspensionButton,created");
    }

    private void initStatusBarHeight() {
        int statusBarHeight = 0;
        int resourceId = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getContext().getResources().getDimensionPixelSize(resourceId);
        }
        this.mHeight = statusBarHeight + getContext().getResources().getDimensionPixelSize(34472536);
    }

    public void setCallback(ICallBack callback) {
        this.mHandleViewCallback = callback;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (this.mHandleViewCallback == null) {
            Log.i(TAG, "no callback regesited or disabled");
            return false;
        }
        this.mHandleViewCallback.userActivity();
        switch (action) {
            case 0:
                this.mStartX = event.getRawX();
                this.mStartY = event.getRawY();
                this.mClickable = true;
                this.mMovable = false;
                break;
            case 1:
                this.mEndX = event.getRawX();
                this.mEndY = event.getRawY();
                if (this.mClickable) {
                    this.mHandleViewCallback.onButtonClick();
                }
                if (this.mMovable) {
                    this.mHandleViewCallback.onButtonViewMoved(this.mEndX, this.mEndY);
                    break;
                }
                break;
            case 2:
                this.mEndX = event.getRawX();
                this.mEndY = event.getRawY();
                this.mMovable = false;
                if (this.mEndY > ((float) this.mHeight) && (this.mEndX - this.mStartX > 100.0f || this.mEndX - this.mStartX < -100.0f || this.mEndY - this.mStartY > 100.0f || this.mEndY - this.mStartY < -100.0f)) {
                    this.mClickable = false;
                    this.mMovable = true;
                }
                if ("com.android.systemui".equals(this.mHandleViewCallback.getCurrentApp())) {
                    this.mMovable = false;
                }
                if (this.mMovable) {
                    this.mHandleViewCallback.onButtonViewMoved(this.mEndX, this.mEndY);
                    break;
                }
                break;
        }
        return true;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mHandleViewCallback == null) {
            Log.i(TAG, "no callback regesited");
        } else {
            this.mHandleViewCallback.onConfigurationChanged(newConfig);
        }
    }
}

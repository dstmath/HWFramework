package huawei.com.android.server.policy;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;
import huawei.com.android.server.policy.HwGlobalActionsView;
import java.util.Locale;

public class HwGlobalActionsCountDownTimer extends CountDownTimer {
    private static final int POWER_OFF_TIMEINTERVAL = 1000;
    private static final String TAG = "HwGlobalActionsCountDownTimer";
    private HwGlobalActionsView.ActionPressedCallback mCallback;
    private String mCountText;
    private boolean mIsOnTick;
    private String mNormalText;
    private TextView mTextView;

    public HwGlobalActionsCountDownTimer(long millisInFuture, long countDownInterval, TextView textView, String countText, String normalText) {
        super(millisInFuture, countDownInterval);
        init(textView, countText, normalText);
    }

    private void init(TextView textView, String countText, String normalText) {
        this.mTextView = textView;
        this.mCountText = countText;
        this.mNormalText = normalText;
        this.mCallback = null;
    }

    public boolean isOnTick() {
        return this.mIsOnTick;
    }

    public void setOnTick(boolean isTick) {
        this.mIsOnTick = isTick;
    }

    public void registerActionPressedCallback(HwGlobalActionsView.ActionPressedCallback callback) {
        this.mCallback = callback;
    }

    public void unregisterActionPressedCallback() {
        this.mCallback = null;
    }

    @Override // android.os.CountDownTimer
    public void onTick(long millisUntilFinished) {
        setOnTick(true);
        TextView textView = this.mTextView;
        if (textView == null) {
            Log.e(TAG, "onTick: mTextView is null!");
        } else if (millisUntilFinished / 1000 != 0) {
            this.mTextView.setText(String.format(Locale.ROOT, this.mCountText, Long.valueOf(millisUntilFinished / 1000)));
        } else {
            textView.setText(this.mNormalText);
        }
    }

    @Override // android.os.CountDownTimer
    public void onFinish() {
        TextView textView = this.mTextView;
        if (textView == null || this.mCallback == null) {
            Log.e(TAG, "onTick: mTextView or mCallback is null!");
        } else {
            textView.setText(this.mNormalText);
            this.mCallback.onScreenoffActionPressed(false);
        }
        cancel();
    }

    public void cancelCount() {
        TextView textView = this.mTextView;
        if (textView != null) {
            textView.setText(this.mNormalText);
        }
        if (this.mCallback != null) {
            this.mCallback = null;
        }
        cancel();
    }
}

package android.text.method;

import android.text.Spannable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;

public interface MovementMethod {
    boolean canSelectArbitrarily();

    void initialize(TextView textView, Spannable spannable);

    boolean onGenericMotionEvent(TextView textView, Spannable spannable, MotionEvent motionEvent);

    boolean onKeyDown(TextView textView, Spannable spannable, int i, KeyEvent keyEvent);

    boolean onKeyOther(TextView textView, Spannable spannable, KeyEvent keyEvent);

    boolean onKeyUp(TextView textView, Spannable spannable, int i, KeyEvent keyEvent);

    void onTakeFocus(TextView textView, Spannable spannable, int i);

    boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent motionEvent);

    boolean onTrackballEvent(TextView textView, Spannable spannable, MotionEvent motionEvent);
}

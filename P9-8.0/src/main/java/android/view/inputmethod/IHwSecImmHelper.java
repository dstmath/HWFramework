package android.view.inputmethod;

import android.os.IBinder;
import android.os.ResultReceiver;
import android.view.View;
import com.android.internal.view.IInputMethodClient.Stub;

public interface IHwSecImmHelper {
    public static final String SECURITY_INPUT_METHOD_SERVICE = "input_method_secure";

    boolean hideSoftInputFromWindow(IBinder iBinder, int i, ResultReceiver resultReceiver, View view, Stub stub);

    boolean isPasswordInputType(View view);

    boolean isUseSecureIME();

    boolean showSoftInput(View view, int i, ResultReceiver resultReceiver, Stub stub);
}

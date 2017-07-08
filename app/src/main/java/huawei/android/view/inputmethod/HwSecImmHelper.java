package huawei.android.view.inputmethod;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.IHwSecImmHelper;
import android.widget.TextView;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.IInputMethodManager.Stub;

public class HwSecImmHelper implements IHwSecImmHelper {
    private static final boolean DEBUG = false;
    private static final String DESCRIPTOR = "android.view.inputmethod.InputMethodManager";
    public static final String SECURE_IME_ID = "com.huawei.secime/.SoftKeyboard";
    public static final String SECURE_IME_NAME = "com.huawei.secime.SoftKeyboard";
    public static final int SECURE_IME_NO_HIDE_FLAG = 4096;
    public static final String SECURE_IME_PACKAGENAME = "com.huawei.secime";
    private static final String TAG = "HwSecImmHelper";
    private static final int TRANSACTION_isUseSecureIME = 1001;
    private IInputMethodManager mSecurityInputMethodService;
    final IInputMethodManager mService;

    public HwSecImmHelper(IInputMethodManager service) {
        this.mService = service;
        this.mSecurityInputMethodService = getSecurityInputMethodService();
    }

    private IInputMethodManager getSecurityInputMethodService() {
        if (this.mSecurityInputMethodService == null) {
            this.mSecurityInputMethodService = Stub.asInterface(ServiceManager.getService("input_method_secure"));
        }
        if (this.mSecurityInputMethodService == null) {
            Log.i(TAG, "mSecurityInputMethodService is null");
        }
        return this.mSecurityInputMethodService;
    }

    public boolean showSoftInput(View view, int flags, ResultReceiver resultReceiver, IInputMethodClient.Stub mClient) {
        try {
            return this.mService.showSoftInput(mClient, flags, resultReceiver);
        } catch (RemoteException e) {
            return DEBUG;
        }
    }

    public boolean hideSoftInputFromWindow(IBinder windowToken, int flags, ResultReceiver resultReceiver, View mServedView, IInputMethodClient.Stub mClient) {
        if (isUseSecureIME()) {
            if (isPasswordInputType(mServedView)) {
                try {
                    this.mService.hideSoftInput(mClient, SECURE_IME_NO_HIDE_FLAG, resultReceiver);
                    return this.mSecurityInputMethodService.hideSoftInput(mClient, flags, resultReceiver);
                } catch (RemoteException e) {
                }
            } else {
                try {
                    this.mSecurityInputMethodService.hideSoftInput(mClient, flags, resultReceiver);
                } catch (RemoteException e2) {
                }
            }
        }
        try {
            return this.mService.hideSoftInput(mClient, flags, resultReceiver);
        } catch (RemoteException e3) {
            return DEBUG;
        }
    }

    public boolean isUseSecureIME() {
        try {
            return getSecurityInputMethodService() != null ? isUseSecureIMEInner() : DEBUG;
        } catch (Exception e) {
            return DEBUG;
        }
    }

    private boolean isUseSecureIMEInner() {
        IBinder b = this.mService.asBinder();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean result = DEBUG;
        if (b != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                b.transact(TRANSACTION_isUseSecureIME, data, reply, 0);
                reply.readException();
                result = reply.readInt() != 0 ? true : DEBUG;
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                reply.recycle();
                data.recycle();
            }
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    public boolean isPasswordInputType(View view) {
        if (view == null) {
            return DEBUG;
        }
        boolean isPwType;
        if (view instanceof TextView) {
            isPwType = isPasswordInputType(((TextView) view).getInputType(), DEBUG);
        } else {
            EditorInfo tba = new EditorInfo();
            tba.packageName = view.getContext().getPackageName();
            tba.fieldId = view.getId();
            view.onCreateInputConnection(tba);
            isPwType = isPasswordInputType(tba.inputType, DEBUG);
        }
        return isPwType;
    }

    public static boolean isPasswordInputType(int inputType, boolean debug) {
        int variation = inputType & 4095;
        boolean isPassword = (variation == PduPart.P_CHARSET || variation == PduHeaders.REPORT_ALLOWED || variation == 225 || variation == 18 || variation == PduHeaders.VALUE_YES || variation == PduHeaders.READ_REPORT) ? true : variation == PduHeaders.STORE_STATUS_ERROR_PERMANENT_FAILURE ? true : DEBUG;
        if (debug && inputType != 0) {
            Log.d(TAG, "isPasswordInputType = " + isPassword + ", inputType = " + inputType);
        }
        return isPassword;
    }
}

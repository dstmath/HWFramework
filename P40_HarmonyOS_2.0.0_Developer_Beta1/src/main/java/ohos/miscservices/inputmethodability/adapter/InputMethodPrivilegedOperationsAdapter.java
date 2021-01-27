package ohos.miscservices.inputmethodability.adapter;

import android.os.IBinder;
import android.os.RemoteException;
import com.android.internal.inputmethod.IInputMethodPrivilegedOperations;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.internal.IInputControlChannelSkeleton;
import ohos.miscservices.inputmethod.internal.IUriPermission;
import ohos.net.UriConverter;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.utils.net.Uri;

public class InputMethodPrivilegedOperationsAdapter {
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethodability.interfaces.IInputControlChannelWrapper";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218110976, "InputMethodPrivilegedOperationsAdapter");
    private IInputMethodPrivilegedOperations mPriOps;

    public InputMethodPrivilegedOperationsAdapter(IRemoteObject iRemoteObject) {
        Optional<Object> translateToIBinder = IPCAdapter.translateToIBinder(iRemoteObject);
        if (translateToIBinder.isPresent()) {
            Object obj = translateToIBinder.get();
            if (obj instanceof IBinder) {
                this.mPriOps = IInputMethodPrivilegedOperations.Stub.asInterface((IBinder) obj);
            }
        }
    }

    public IRemoteObject getAdaptControlChannel() {
        HiLog.debug(LABEL, "getAdaptControlChannel.", new Object[0]);
        return new InputControlChannelImpl(DESCRIPTOR);
    }

    class InputControlChannelImpl extends IInputControlChannelSkeleton {
        InputControlChannelImpl(String str) {
            super(str);
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputControlChannel
        public void hideKeyboardSelf(int i) {
            HiLog.debug(InputMethodPrivilegedOperationsAdapter.LABEL, "hideKeyboardSelf", new Object[0]);
            try {
                if (InputMethodPrivilegedOperationsAdapter.this.mPriOps != null) {
                    InputMethodPrivilegedOperationsAdapter.this.mPriOps.hideMySoftInput(i);
                } else {
                    HiLog.error(InputMethodPrivilegedOperationsAdapter.LABEL, "hideKeyboardSelf failed, current priOps is null.", new Object[0]);
                }
            } catch (RemoteException unused) {
                HiLog.error(InputMethodPrivilegedOperationsAdapter.LABEL, "hideMySoftInput RemoteException e", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputControlChannel
        public boolean toNextInputMethod() {
            HiLog.debug(InputMethodPrivilegedOperationsAdapter.LABEL, "toNextInputMethod", new Object[0]);
            try {
                if (InputMethodPrivilegedOperationsAdapter.this.mPriOps != null) {
                    return InputMethodPrivilegedOperationsAdapter.this.mPriOps.switchToNextInputMethod(false);
                }
                HiLog.error(InputMethodPrivilegedOperationsAdapter.LABEL, "toNextInputMethod failed, current priOps is null.", new Object[0]);
                return false;
            } catch (RemoteException unused) {
                HiLog.error(InputMethodPrivilegedOperationsAdapter.LABEL, "switchToNextInputMethod RemoteException e", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputControlChannel
        public void reportScreenMode(int i) {
            HiLog.debug(InputMethodPrivilegedOperationsAdapter.LABEL, "reportScreenMode", new Object[0]);
            if (InputMethodPrivilegedOperationsAdapter.this.mPriOps == null) {
                HiLog.error(InputMethodPrivilegedOperationsAdapter.LABEL, "reportScreenMode failed, current PriOps is null.", new Object[0]);
            } else if (i == 1) {
                try {
                    InputMethodPrivilegedOperationsAdapter.this.mPriOps.reportFullscreenMode(true);
                } catch (RemoteException unused) {
                    HiLog.error(InputMethodPrivilegedOperationsAdapter.LABEL, "reportScreenMode RemoteException e", new Object[0]);
                }
            } else {
                try {
                    InputMethodPrivilegedOperationsAdapter.this.mPriOps.reportFullscreenMode(false);
                } catch (RemoteException unused2) {
                    HiLog.error(InputMethodPrivilegedOperationsAdapter.LABEL, "reportScreenMode RemoteException e", new Object[0]);
                }
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputControlChannel
        public IUriPermission createUriPermission(Uri uri, String str) {
            HiLog.debug(InputMethodPrivilegedOperationsAdapter.LABEL, "createUriPermission", new Object[0]);
            android.net.Uri convertToAndroidContentUri = UriConverter.convertToAndroidContentUri(uri);
            try {
                if (InputMethodPrivilegedOperationsAdapter.this.mPriOps != null) {
                    return new InputContentUriTokenAdapter(InputMethodPrivilegedOperationsAdapter.this.mPriOps.createInputContentUriToken(convertToAndroidContentUri, str)).getAdaptUriPermission();
                }
                HiLog.error(InputMethodPrivilegedOperationsAdapter.LABEL, "createUriPermission failed, current priOps is null.", new Object[0]);
                return null;
            } catch (RemoteException unused) {
                HiLog.error(InputMethodPrivilegedOperationsAdapter.LABEL, "createUriPermission RemoteException e", new Object[0]);
                return null;
            }
        }
    }
}

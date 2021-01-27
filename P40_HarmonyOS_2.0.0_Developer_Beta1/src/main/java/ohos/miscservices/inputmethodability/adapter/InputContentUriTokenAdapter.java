package ohos.miscservices.inputmethodability.adapter;

import com.android.internal.inputmethod.IInputContentUriToken;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.internal.IUriPermission;
import ohos.miscservices.inputmethod.internal.UriPermissionSkeleton;
import ohos.rpc.RemoteException;

public class InputContentUriTokenAdapter {
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IUriPermission";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputContentUriTokenAdapter");
    private IInputContentUriToken mUriToken;

    public InputContentUriTokenAdapter(IInputContentUriToken iInputContentUriToken) {
        this.mUriToken = iInputContentUriToken;
    }

    public IUriPermission getAdaptUriPermission() {
        HiLog.debug(TAG, "getAdaptUriPermission", new Object[0]);
        return new UriPermissionImpl(DESCRIPTOR);
    }

    class UriPermissionImpl extends UriPermissionSkeleton {
        public UriPermissionImpl(String str) {
            super(str);
        }

        @Override // ohos.miscservices.inputmethod.internal.IUriPermission
        public void take() throws RemoteException {
            HiLog.info(InputContentUriTokenAdapter.TAG, "UriPermission take", new Object[0]);
            try {
                if (InputContentUriTokenAdapter.this.mUriToken != null) {
                    InputContentUriTokenAdapter.this.mUriToken.take();
                } else {
                    HiLog.error(InputContentUriTokenAdapter.TAG, "take permission failed, current uri token is null.", new Object[0]);
                }
            } catch (android.os.RemoteException e) {
                HiLog.error(InputContentUriTokenAdapter.TAG, "take permission RemoteException: %{public}s", e.getMessage());
                throw new RemoteException("Take uri permission error.");
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IUriPermission
        public void release() throws RemoteException {
            HiLog.info(InputContentUriTokenAdapter.TAG, "UriPermission release", new Object[0]);
            try {
                if (InputContentUriTokenAdapter.this.mUriToken != null) {
                    InputContentUriTokenAdapter.this.mUriToken.release();
                } else {
                    HiLog.error(InputContentUriTokenAdapter.TAG, "release permission failed, current uri token is null.", new Object[0]);
                }
            } catch (android.os.RemoteException e) {
                HiLog.error(InputContentUriTokenAdapter.TAG, "release permission RemoteException: %{public}s", e.getMessage());
                throw new RemoteException("Release uri permission error.");
            }
        }
    }
}

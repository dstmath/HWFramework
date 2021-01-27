package android.service.euicc;

import android.os.RemoteException;
import android.telephony.Rlog;

public class HwEuiccServiceEx implements IHwEuiccServiceEx {
    private static final String LOG_TAG = "HwEuiccServiceEx";
    private EuiccService mEuiccService;
    private IHwEuiccServiceInner mInner;

    public HwEuiccServiceEx(IHwEuiccServiceInner euiccService) {
        this.mInner = euiccService;
        this.mEuiccService = (EuiccService) euiccService;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logi(String log) {
        Rlog.d(LOG_TAG, log);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String log) {
        Rlog.e(LOG_TAG, log);
    }

    public void requestDefaultSmdpAddress(final String cardId, final IHwGetSmdsAddressCallback callback) {
        logi("requestDefaultSmdpAddress");
        this.mInner.getExecutor().execute(new Runnable() {
            /* class android.service.euicc.HwEuiccServiceEx.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                String result = HwEuiccServiceEx.this.mEuiccService.onRequestDefaultSmdpAddress(cardId);
                HwEuiccServiceEx hwEuiccServiceEx = HwEuiccServiceEx.this;
                hwEuiccServiceEx.logi("requestDefaultSmdpAddress, result = " + result);
                try {
                    callback.onComplete(result);
                } catch (RemoteException e) {
                    HwEuiccServiceEx.this.loge("requestDefaultSmdpAddress has exception");
                }
            }
        });
    }

    public void resetMemory(final String cardId, final int options, final IHwResetMemoryCallback callback) {
        logi("resetMemory, options = " + options);
        this.mInner.getExecutor().execute(new Runnable() {
            /* class android.service.euicc.HwEuiccServiceEx.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    callback.onComplete(HwEuiccServiceEx.this.mEuiccService.onResetMemory(cardId, options));
                } catch (RemoteException e) {
                    HwEuiccServiceEx.this.loge("resetMemory has exception");
                }
            }
        });
    }

    public void setDefaultSmdpAddress(final String cardId, final String address, final IHwSetDefaultSmdpAddressCallback callback) {
        logi("setDefaultSmdpAddress");
        this.mInner.getExecutor().execute(new Runnable() {
            /* class android.service.euicc.HwEuiccServiceEx.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    callback.onComplete(HwEuiccServiceEx.this.mEuiccService.onSetDefaultSmdpAddress(cardId, address));
                } catch (RemoteException e) {
                    HwEuiccServiceEx.this.loge("setDefaultSmdpAddress has exception");
                }
            }
        });
    }

    public void cancelSession() {
        logi("cancelSession");
        this.mEuiccService.onCancelSession();
    }
}

package android.service.euicc;

import android.os.RemoteException;
import android.service.euicc.IGetInnerServiceCallback;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class HwEuiccService {
    private static IHwEuiccService blockingGetInnerEuiccService(IEuiccService euiccService) throws RemoteException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<IHwEuiccService> eUiccServiceRef = new AtomicReference<>();
        euiccService.getHwInnerService(new IGetInnerServiceCallback.Stub() {
            /* class android.service.euicc.HwEuiccService.AnonymousClass1 */

            @Override // android.service.euicc.IGetInnerServiceCallback
            public void onComplete(IHwEuiccService innerService) {
                eUiccServiceRef.set(innerService);
                latch.countDown();
            }
        });
        return (IHwEuiccService) awaitResult(latch, eUiccServiceRef);
    }

    private static <T> T awaitResult(CountDownLatch latch, AtomicReference<T> resultRef) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return resultRef.get();
    }

    public static void requestDefaultSmdpAddress(IEuiccService euiccService, String cardId, IHwGetSmdsAddressCallback callback) throws RemoteException {
        IHwEuiccService hwEuiccService = blockingGetInnerEuiccService(euiccService);
        if (hwEuiccService != null) {
            hwEuiccService.requestDefaultSmdpAddress(cardId, callback);
        }
    }

    public static void resetMemory(IEuiccService euiccService, String cardId, int options, IHwResetMemoryCallback callback) throws RemoteException {
        IHwEuiccService hwEuiccService = blockingGetInnerEuiccService(euiccService);
        if (hwEuiccService != null) {
            hwEuiccService.resetMemory(cardId, options, callback);
        }
    }

    public static void setDefaultSmdpAddress(IEuiccService euiccService, String cardId, String address, IHwSetDefaultSmdpAddressCallback callback) throws RemoteException {
        IHwEuiccService hwEuiccService = blockingGetInnerEuiccService(euiccService);
        if (hwEuiccService != null) {
            hwEuiccService.setDefaultSmdpAddress(cardId, address, callback);
        }
    }

    public static void cancelSession(IEuiccService euiccService) throws RemoteException {
        IHwEuiccService hwEuiccService = blockingGetInnerEuiccService(euiccService);
        if (hwEuiccService != null) {
            hwEuiccService.cancelSession();
        }
    }
}

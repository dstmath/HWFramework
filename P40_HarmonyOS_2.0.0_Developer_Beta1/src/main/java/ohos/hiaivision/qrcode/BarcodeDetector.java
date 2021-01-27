package ohos.hiaivision.qrcode;

import android.os.Bundle;
import android.os.RemoteException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ohos.ai.cv.qrcode.IBarcodeDetector;
import ohos.ai.engine.utils.HiAILog;
import ohos.app.Context;
import ohos.hiaivision.common.IHiAIVisionCallback;
import ohos.hiaivision.common.VisionBase;
import ohos.hiaivision.qrcode.BarcodeConfiguration;
import ohos.utils.adapter.PacMapUtils;

public class BarcodeDetector extends VisionBase implements IBarcodeDetector {
    private static final int CORRECTION_LEVEL = 1;
    private static final int DEFAULT_MARGIN = 4;
    private static final int ENCODE_FORMATE = 11;
    private static final int HEIGHT_MAX = 1680;
    private static final int INT_TO_BYTE = 4;
    private static final String TAG = "BarcodeDetector";
    private static final int WIDTH_MAX = 1920;
    private BarcodeConfiguration barcodeConfiguration = new BarcodeConfiguration.Builder().build();

    /* access modifiers changed from: protected */
    @Override // ohos.hiaivision.common.VisionBase
    public int getApiId() {
        return 659556;
    }

    public BarcodeDetector(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.hiaivision.common.VisionBase
    public BarcodeConfiguration getConfiguration() {
        return this.barcodeConfiguration;
    }

    public int detect(String str, byte[] bArr, int i, int i2) {
        HiAILog.info(TAG, "barcode detector using plugin interface");
        if (str == null || bArr == null) {
            HiAILog.error(TAG, "input parameter null.");
            return 200;
        } else if (i > WIDTH_MAX || i2 > HEIGHT_MAX || i <= 0 || i2 <= 0) {
            HiAILog.error(TAG, "input parameter invalid. width = " + i + ", height = " + i2);
            return 200;
        } else if (bArr.length < i * i2 * 4) {
            HiAILog.error(TAG, "bitmapOutput.length = " + bArr.length);
            return 200;
        } else {
            int prepare = prepare();
            if (prepare != 0) {
                HiAILog.error(TAG, "prepareResult = " + prepare);
                return prepare;
            }
            ReentrantLock reentrantLock = new ReentrantLock();
            Condition newCondition = reentrantLock.newCondition();
            int[] iArr = {-1};
            Bundle convertIntoBundle = PacMapUtils.convertIntoBundle(this.barcodeConfiguration.getParam());
            convertIntoBundle.putString("barcode_input", str);
            convertIntoBundle.putInt("fix_width", i);
            convertIntoBundle.putInt("fix_height", i2);
            convertIntoBundle.putInt("barcode_margin", 4);
            convertIntoBundle.putInt("barcode_format", 11);
            convertIntoBundle.putInt("barcode_errorlevel", 1);
            getAsyncResult(convertIntoBundle, this.barcodeConfiguration.getProcessMode(), getBarcodeCallback(bArr, reentrantLock, newCondition, iArr));
            int waitForResult = waitForResult(false, reentrantLock, newCondition);
            HiAILog.info(TAG, "waitResultCode: " + waitForResult + " resultCodeArr[0]: " + iArr[0]);
            if (waitForResult != 0 || iArr[0] == 0) {
                return waitForResult;
            }
            return iArr[0];
        }
    }

    private IHiAIVisionCallback getBarcodeCallback(final byte[] bArr, final Lock lock, final Condition condition, final int[] iArr) {
        return new IHiAIVisionCallback.Stub() {
            /* class ohos.hiaivision.qrcode.BarcodeDetector.AnonymousClass1 */

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onInfo(Bundle bundle) throws RemoteException {
            }

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onResult(Bundle bundle) throws RemoteException {
                HiAILog.info(BarcodeDetector.TAG, "onResult");
                if (bundle == null) {
                    onError(101);
                    return;
                }
                byte[] byteArray = bundle.getByteArray("bitmap_output");
                if (byteArray == null || byteArray.length == 0) {
                    onError(101);
                    return;
                }
                System.arraycopy(byteArray, 0, bArr, 0, byteArray.length);
                iArr[0] = 0;
                lock.lock();
                try {
                    condition.signalAll();
                } finally {
                    lock.unlock();
                }
            }

            @Override // ohos.hiaivision.common.IHiAIVisionCallback
            public void onError(int i) throws RemoteException {
                HiAILog.error(BarcodeDetector.TAG, "onError， resultCode = " + i);
                iArr[0] = i;
                lock.lock();
                try {
                    condition.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        };
    }
}

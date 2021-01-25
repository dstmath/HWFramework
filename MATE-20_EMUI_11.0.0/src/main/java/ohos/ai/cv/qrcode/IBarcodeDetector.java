package ohos.ai.cv.qrcode;

import ohos.ai.cv.common.ICvBase;

public interface IBarcodeDetector extends ICvBase {
    int detect(String str, byte[] bArr, int i, int i2);
}

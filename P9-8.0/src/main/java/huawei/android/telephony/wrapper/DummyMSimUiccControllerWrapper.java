package huawei.android.telephony.wrapper;

import com.android.internal.telephony.uicc.UiccCard;

public class DummyMSimUiccControllerWrapper implements MSimUiccControllerWrapper {
    private static DummyMSimUiccControllerWrapper mInstance = new DummyMSimUiccControllerWrapper();

    public static MSimUiccControllerWrapper getInstance() {
        return mInstance;
    }

    public UiccCard getUiccCard(Object uiccController, int slotId) {
        return null;
    }
}

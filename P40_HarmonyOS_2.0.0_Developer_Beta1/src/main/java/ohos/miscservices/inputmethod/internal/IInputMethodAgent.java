package ohos.miscservices.inputmethod.internal;

import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IInputMethodAgent extends IRemoteBroker {
    boolean dispatchMultimodalEvent(MultimodalEvent multimodalEvent) throws RemoteException;

    void notifyCursorCoordinateChanged(float f, float f2, float f3, float[] fArr) throws RemoteException;

    void notifyEditingTextChanged(int i, EditingText editingText) throws RemoteException;

    void notifySelectionChanged(int i, int i2, int i3, int i4) throws RemoteException;

    void sendRecommendationInfo(RecommendationInfo[] recommendationInfoArr) throws RemoteException;
}

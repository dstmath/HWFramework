package ohos.miscservices.inputmethod.internal;

import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.miscservices.inputmethod.RichContent;
import ohos.multimodalinput.event.KeyEvent;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public interface IInputDataChannel extends IRemoteBroker {
    boolean clearNoncharacterKeyState(int i) throws RemoteException;

    boolean deleteBackward(int i) throws RemoteException;

    boolean deleteForward(int i) throws RemoteException;

    void getAutoCapitalizeMode(int i, IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException;

    void getBackward(int i, IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException;

    void getEditingText(int i, EditingCapability editingCapability, IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException;

    void getForward(int i, IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException;

    void getSelectedText(int i, IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException;

    boolean insertRichContent(RichContent richContent, IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException;

    boolean insertText(String str) throws RemoteException;

    boolean markText(int i, int i2) throws RemoteException;

    boolean recommendText(RecommendationInfo recommendationInfo) throws RemoteException;

    boolean replaceMarkedText(String str) throws RemoteException;

    boolean requestCurrentCursorContext(IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException;

    boolean reviseText(int i, String str, String str2) throws RemoteException;

    boolean selectText(int i, int i2) throws RemoteException;

    boolean sendCustomizedData(String str, PacMap pacMap) throws RemoteException;

    boolean sendKeyEvent(KeyEvent keyEvent) throws RemoteException;

    boolean sendKeyFunction(int i) throws RemoteException;

    boolean sendMenuFunction(int i) throws RemoteException;

    boolean subscribeCursorContext(IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException;

    boolean subscribeEditingText(int i, EditingCapability editingCapability) throws RemoteException;

    boolean unmarkText() throws RemoteException;

    boolean unsubscribeCursorContext(IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException;

    boolean unsubscribeEditingText(int i) throws RemoteException;
}

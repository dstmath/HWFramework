package ohos.miscservices.inputmethodability.adapter;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputContentInfo;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputContextCallback;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.miscservices.inputmethod.RichContent;
import ohos.miscservices.inputmethod.adapter.CompletionInfoAdapter;
import ohos.miscservices.inputmethod.adapter.EditorInfoAdapter;
import ohos.miscservices.inputmethod.adapter.ExtractedTextAdapter;
import ohos.miscservices.inputmethod.adapter.ExtractedTextRequestAdapter;
import ohos.miscservices.inputmethod.adapter.InputContentInfoAdapter;
import ohos.miscservices.inputmethod.internal.IInputDataChannelCallback;
import ohos.miscservices.inputmethod.internal.InputDataChannelSkeleton;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.utils.PacMap;
import ohos.utils.adapter.PacMapUtils;

public class InputConnectionAdapter {
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IInputDataChannel";
    private static final int GET_EDITING_TEXT = 0;
    private static final int SUBSCRIBE_EDITING_TEXT = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputConnectionAdapter");
    private static final int UNSUBSCRIBE_EDITING_TEXT = 2;
    private static final int WAIT_DELAY_TIME = 1000;
    private IInputContext mInputConnection;

    public InputConnectionAdapter(IRemoteObject iRemoteObject) {
        Optional<Object> translateToIBinder = IPCAdapter.translateToIBinder(iRemoteObject);
        if (translateToIBinder.isPresent()) {
            Object obj = translateToIBinder.get();
            if (obj instanceof IBinder) {
                this.mInputConnection = IInputContext.Stub.asInterface((IBinder) obj);
                HiLog.info(TAG, "get input connection binder success.", new Object[0]);
            }
        }
    }

    public IRemoteObject getAdaptRemoteObject() {
        HiLog.debug(TAG, "getAdaptRemoteObject.", new Object[0]);
        return new InputDataChannelImpl(DESCRIPTOR);
    }

    /* access modifiers changed from: private */
    public class InputDataChannelImpl extends InputDataChannelSkeleton {
        InputDataChannelImpl(String str) {
            super(str);
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean insertText(String str) {
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.commitText(str, 1);
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "insertText error because the InputConnection object is null.", new Object[0]);
                return false;
            } catch (RemoteException unused) {
                HiLog.error(InputConnectionAdapter.TAG, "insertText RemoteException.", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean insertRichContent(RichContent richContent, IInputDataChannelCallback iInputDataChannelCallback) {
            InputConnectionCallback instance = InputConnectionCallback.getInstance();
            InputContentInfo convertToInputContentInfo = InputContentInfoAdapter.convertToInputContentInfo(richContent);
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.commitContent(convertToInputContentInfo, 0, (Bundle) null, 0, instance);
                }
                instance.awaitForResult();
                iInputDataChannelCallback.notifyInsertRichContentResult(instance.mCommitContentResult);
                return true;
            } catch (RemoteException | ohos.rpc.RemoteException e) {
                HiLog.error(InputConnectionAdapter.TAG, "insertRichContent RemoteException %{public}s", e.getMessage());
                return false;
            } finally {
                instance.reset();
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean deleteForward(int i) {
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.deleteSurroundingText(i, 0);
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "deleteForward error because the InputConnection object is null.", new Object[0]);
                return false;
            } catch (RemoteException unused) {
                HiLog.error(InputConnectionAdapter.TAG, "deleteForward RemoteException.", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean deleteBackward(int i) {
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.deleteSurroundingText(0, i);
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "deleteBackward error because the InputConnection object is null.", new Object[0]);
                return false;
            } catch (RemoteException unused) {
                HiLog.error(InputConnectionAdapter.TAG, "deleteBackward RemoteException.", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public void getForward(int i, IInputDataChannelCallback iInputDataChannelCallback) {
            InputConnectionCallback instance = InputConnectionCallback.getInstance();
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.getTextBeforeCursor(i, 0, 0, instance);
                }
                instance.awaitForResult();
                iInputDataChannelCallback.setForward(String.valueOf(instance.mTextBeforeCursor));
            } catch (RemoteException | ohos.rpc.RemoteException e) {
                HiLog.error(InputConnectionAdapter.TAG, "getForward RemoteException %{public}s", e.getMessage());
            } catch (Throwable th) {
                instance.reset();
                throw th;
            }
            instance.reset();
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public void getBackward(int i, IInputDataChannelCallback iInputDataChannelCallback) {
            InputConnectionCallback instance = InputConnectionCallback.getInstance();
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.getTextAfterCursor(i, 0, 0, instance);
                }
                instance.awaitForResult();
                iInputDataChannelCallback.setBackward(String.valueOf(instance.mTextAfterCursor));
            } catch (RemoteException | ohos.rpc.RemoteException e) {
                HiLog.error(InputConnectionAdapter.TAG, "getBackward RemoteException %{public}s", e.getMessage());
            } catch (Throwable th) {
                instance.reset();
                throw th;
            }
            instance.reset();
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean markText(int i, int i2) {
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.setComposingRegion(i, i2);
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "markText error because the InputConnection object is null.", new Object[0]);
                return false;
            } catch (RemoteException unused) {
                HiLog.error(InputConnectionAdapter.TAG, "markText RemoteException.", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean unmarkText() {
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.finishComposingText();
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "markText error because the InputConnection object is null.", new Object[0]);
                return false;
            } catch (RemoteException unused) {
                HiLog.error(InputConnectionAdapter.TAG, "unmarkText RemoteException.", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean replaceMarkedText(String str) {
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.setComposingText(str, 0);
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "replaceMarkedText error because the InputConnection object is null.", new Object[0]);
                return false;
            } catch (RemoteException unused) {
                HiLog.error(InputConnectionAdapter.TAG, "replaceMarkedText RemoteException.", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public void getEditingText(int i, EditingCapability editingCapability, IInputDataChannelCallback iInputDataChannelCallback) {
            InputConnectionCallback instance = InputConnectionCallback.getInstance();
            ExtractedTextRequest convertToExtractedTextRequest = ExtractedTextRequestAdapter.convertToExtractedTextRequest(editingCapability, i);
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.getExtractedText(convertToExtractedTextRequest, 0, 0, instance);
                }
                instance.awaitForResult();
                iInputDataChannelCallback.notifyEditingText(ExtractedTextAdapter.convertToEditingText(instance.mExtractedText));
            } catch (RemoteException | ohos.rpc.RemoteException e) {
                HiLog.error(InputConnectionAdapter.TAG, "getEditingText RemoteException %{public}s", e.getMessage());
            } catch (Throwable th) {
                instance.reset();
                throw th;
            }
            instance.reset();
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean subscribeEditingText(int i, EditingCapability editingCapability) {
            InputConnectionCallback instance = InputConnectionCallback.getInstance();
            ExtractedTextRequest convertToExtractedTextRequest = ExtractedTextRequestAdapter.convertToExtractedTextRequest(editingCapability, i);
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.getExtractedText(convertToExtractedTextRequest, 1, 0, instance);
                    instance.reset();
                    return true;
                }
                instance.awaitForResult();
                instance.reset();
                return false;
            } catch (RemoteException e) {
                HiLog.error(InputConnectionAdapter.TAG, "subscribeEditingText RemoteException %{public}s", e.getMessage());
            } catch (Throwable th) {
                instance.reset();
                throw th;
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean unsubscribeEditingText(int i) {
            InputConnectionCallback instance = InputConnectionCallback.getInstance();
            ExtractedTextRequest extractedTextRequest = new ExtractedTextRequest();
            extractedTextRequest.token = i;
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.getExtractedText(extractedTextRequest, 2, 0, instance);
                    instance.reset();
                    return true;
                }
                instance.awaitForResult();
                instance.reset();
                return false;
            } catch (RemoteException e) {
                HiLog.error(InputConnectionAdapter.TAG, "unsubscribeEditingText RemoteException %{public}s", e.getMessage());
            } catch (Throwable th) {
                instance.reset();
                throw th;
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean sendKeyEvent(KeyEvent keyEvent) {
            Optional<android.view.KeyEvent> hostKeyEvent = MultimodalEventFactory.getHostKeyEvent(keyEvent);
            if (hostKeyEvent.isPresent()) {
                android.view.KeyEvent keyEvent2 = hostKeyEvent.get();
                try {
                    if (InputConnectionAdapter.this.mInputConnection != null) {
                        InputConnectionAdapter.this.mInputConnection.sendKeyEvent(keyEvent2);
                        return true;
                    }
                    HiLog.error(InputConnectionAdapter.TAG, "sendKeyEvent error because the InputConnection object is null.", new Object[0]);
                } catch (RemoteException unused) {
                    HiLog.error(InputConnectionAdapter.TAG, "sendKeyEvent RemoteException.", new Object[0]);
                }
            } else {
                HiLog.error(InputConnectionAdapter.TAG, "sendKeyEvent failed: getHostKeyEvent error.", new Object[0]);
            }
            return false;
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean sendCustomizedData(String str, PacMap pacMap) {
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.performPrivateCommand(str, PacMapUtils.convertIntoBundle(pacMap));
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "sendCustomizedData error because the InputConnection object is null.", new Object[0]);
                return false;
            } catch (RemoteException unused) {
                HiLog.error(InputConnectionAdapter.TAG, "sendCustomizedData RemoteException.", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean sendKeyFunction(int i) {
            int convertToImeOption = EditorInfoAdapter.convertToImeOption(i, 0);
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.performEditorAction(convertToImeOption);
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "sendKeyFunction error because the InputConnection object is null.", new Object[0]);
                return false;
            } catch (RemoteException unused) {
                HiLog.error(InputConnectionAdapter.TAG, "sendKeyFunction RemoteException.", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean selectText(int i, int i2) {
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.setSelection(i, i2);
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "selectText error because the InputConnection object is null.", new Object[0]);
                return false;
            } catch (RemoteException unused) {
                HiLog.error(InputConnectionAdapter.TAG, "selectText RemoteException.", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean clearNoncharacterKeyState(int i) {
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.clearMetaKeyStates(i);
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "clearNoncharacterKeyState error because the InputConnection object is null.", new Object[0]);
                return false;
            } catch (RemoteException unused) {
                HiLog.error(InputConnectionAdapter.TAG, "clearNoncharacterKeyState RemoteException.", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean recommendText(RecommendationInfo recommendationInfo) {
            CompletionInfo convertToCompletionInfo = CompletionInfoAdapter.convertToCompletionInfo(recommendationInfo);
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.commitCompletion(convertToCompletionInfo);
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "recommendText error because the InputConnection object is null.", new Object[0]);
                return false;
            } catch (RemoteException unused) {
                HiLog.error(InputConnectionAdapter.TAG, "recommendText RemoteException.", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean reviseText(int i, String str, String str2) {
            CorrectionInfo correctionInfo = new CorrectionInfo(i, str, str2);
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.commitCorrection(correctionInfo);
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "reviseText error because the InputConnection object is null.", new Object[0]);
                return false;
            } catch (RemoteException unused) {
                HiLog.error(InputConnectionAdapter.TAG, "reviseText RemoteException.", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean sendMenuFunction(int i) {
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.performContextMenuAction(i);
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "sendMenuFunction error because the InputConnection object is null.", new Object[0]);
                return false;
            } catch (RemoteException unused) {
                HiLog.error(InputConnectionAdapter.TAG, "sendMenuFunction RemoteException.", new Object[0]);
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean requestCurrentCursorContext(IInputDataChannelCallback iInputDataChannelCallback) {
            InputConnectionCallback instance = InputConnectionCallback.getInstance();
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.requestUpdateCursorAnchorInfo(1, 1, instance);
                    instance.awaitForResult();
                    iInputDataChannelCallback.notifySubscribeCaretContextResult(instance.mRequestUpdateCursorAnchorInfoResult);
                    instance.reset();
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "requestCurrentCursorContext: inputConnection is null", new Object[0]);
                instance.reset();
                return false;
            } catch (RemoteException | ohos.rpc.RemoteException e) {
                HiLog.error(InputConnectionAdapter.TAG, "requestCurrentCursorContext RemoteException %{public}s", e.getMessage());
            } catch (Throwable th) {
                instance.reset();
                throw th;
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean subscribeCursorContext(IInputDataChannelCallback iInputDataChannelCallback) {
            InputConnectionCallback instance = InputConnectionCallback.getInstance();
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.requestUpdateCursorAnchorInfo(3, 1, instance);
                    instance.awaitForResult();
                    iInputDataChannelCallback.notifySubscribeCaretContextResult(instance.mRequestUpdateCursorAnchorInfoResult);
                    instance.reset();
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "subscribeCursorContext: inputConnection is null", new Object[0]);
                instance.reset();
                return false;
            } catch (RemoteException | ohos.rpc.RemoteException e) {
                HiLog.error(InputConnectionAdapter.TAG, "subscribeCursorContext RemoteException %{public}s", e.getMessage());
            } catch (Throwable th) {
                instance.reset();
                throw th;
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public boolean unsubscribeCursorContext(IInputDataChannelCallback iInputDataChannelCallback) {
            InputConnectionCallback instance = InputConnectionCallback.getInstance();
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.requestUpdateCursorAnchorInfo(0, 1, instance);
                    instance.awaitForResult();
                    iInputDataChannelCallback.notifySubscribeCaretContextResult(instance.mRequestUpdateCursorAnchorInfoResult);
                    instance.reset();
                    return true;
                }
                HiLog.error(InputConnectionAdapter.TAG, "unsubscribeCursorContext: inputConnection is null", new Object[0]);
                instance.reset();
                return false;
            } catch (RemoteException | ohos.rpc.RemoteException e) {
                HiLog.error(InputConnectionAdapter.TAG, "unsubscribeCursorContext RemoteException %{public}s", e.getMessage());
            } catch (Throwable th) {
                instance.reset();
                throw th;
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public void getAutoCapitalizeMode(int i, IInputDataChannelCallback iInputDataChannelCallback) {
            HiLog.info(InputConnectionAdapter.TAG, "getAutoCapitalizeMode", new Object[0]);
            InputConnectionCallback instance = InputConnectionCallback.getInstance();
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.getCursorCapsMode(EditorInfoAdapter.convertToCursorCapsMode(i), 0, instance);
                }
                instance.awaitForResult();
                iInputDataChannelCallback.setAutoCapitalizeMode(EditorInfoAdapter.convertToAutoCapMode(instance.mCursorCapsMode));
            } catch (RemoteException | ohos.rpc.RemoteException e) {
                HiLog.error(InputConnectionAdapter.TAG, "getAutoCapitalizeMode RemoteException %{public}s", e.getMessage());
            } catch (Throwable th) {
                instance.reset();
                throw th;
            }
            instance.reset();
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
        public void getSelectedText(int i, IInputDataChannelCallback iInputDataChannelCallback) {
            HiLog.info(InputConnectionAdapter.TAG, "getSelectedText", new Object[0]);
            InputConnectionCallback instance = InputConnectionCallback.getInstance();
            try {
                if (InputConnectionAdapter.this.mInputConnection != null) {
                    InputConnectionAdapter.this.mInputConnection.getSelectedText(i, 0, instance);
                }
                instance.awaitForResult();
                iInputDataChannelCallback.setSelectedText(String.valueOf(instance.mSelectedText));
            } catch (RemoteException | ohos.rpc.RemoteException e) {
                HiLog.error(InputConnectionAdapter.TAG, "getSelectedText RemoteException %{public}s", e.getMessage());
            } catch (Throwable th) {
                instance.reset();
                throw th;
            }
            instance.reset();
        }
    }

    private static class InputConnectionCallback extends IInputContextCallback.Stub {
        private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputConnectionCallback");
        private static InputConnectionCallback sInstance = new InputConnectionCallback();
        private boolean mCommitContentResult;
        private int mCursorCapsMode;
        private ExtractedText mExtractedText;
        private CountDownLatch mLatch;
        private boolean mRequestUpdateCursorAnchorInfoResult;
        private CharSequence mSelectedText;
        private CharSequence mTextAfterCursor;
        private CharSequence mTextBeforeCursor;

        private InputConnectionCallback() {
        }

        /* access modifiers changed from: private */
        public static InputConnectionCallback getInstance() {
            InputConnectionCallback inputConnectionCallback;
            synchronized (InputConnectionCallback.class) {
                if (sInstance != null) {
                    inputConnectionCallback = sInstance;
                    sInstance = null;
                } else {
                    inputConnectionCallback = new InputConnectionCallback();
                }
                inputConnectionCallback.mLatch = new CountDownLatch(1);
            }
            return inputConnectionCallback;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void awaitForResult() {
            try {
                this.mLatch.await(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                HiLog.error(TAG, "awaitForResult InterruptedException: %{public}s", e.getMessage());
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void reset() {
            synchronized (InputConnectionCallback.class) {
                if (sInstance == null) {
                    this.mTextBeforeCursor = null;
                    this.mTextAfterCursor = null;
                    this.mSelectedText = null;
                    this.mExtractedText = null;
                    this.mCursorCapsMode = 0;
                    this.mCommitContentResult = false;
                    this.mRequestUpdateCursorAnchorInfoResult = false;
                    sInstance = this;
                }
            }
        }

        public void setTextBeforeCursor(CharSequence charSequence, int i) {
            this.mTextBeforeCursor = charSequence;
            this.mLatch.countDown();
        }

        public void setTextAfterCursor(CharSequence charSequence, int i) {
            this.mTextAfterCursor = charSequence;
            this.mLatch.countDown();
        }

        public void setCursorCapsMode(int i, int i2) {
            this.mCursorCapsMode = i;
            this.mLatch.countDown();
        }

        public void setExtractedText(ExtractedText extractedText, int i) {
            this.mExtractedText = extractedText;
            this.mLatch.countDown();
        }

        public void setSelectedText(CharSequence charSequence, int i) {
            this.mSelectedText = charSequence;
            this.mLatch.countDown();
        }

        public void setRequestUpdateCursorAnchorInfoResult(boolean z, int i) throws RemoteException {
            this.mRequestUpdateCursorAnchorInfoResult = z;
            this.mLatch.countDown();
        }

        public void setCommitContentResult(boolean z, int i) throws RemoteException {
            this.mCommitContentResult = z;
            this.mLatch.countDown();
        }
    }
}

package ohos.miscservices.inputmethodability;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.DefaultInputDataChannelImpl;
import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.miscservices.inputmethod.RichContent;
import ohos.miscservices.inputmethod.internal.IInputDataChannel;
import ohos.miscservices.inputmethod.internal.InputDataChannelCallbackSkeleton;
import ohos.multimodalinput.event.KeyEvent;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public class InputDataChannelWrapper extends DefaultInputDataChannelImpl {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputDataChannelWrapper");
    private static final long WAIT_DELAY_TIME = 1000;
    private final IInputDataChannel mInputDataChannelProxy;

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean subscribeCaretContext(int i) {
        return true;
    }

    public InputDataChannelWrapper(IInputDataChannel iInputDataChannel) {
        this.mInputDataChannelProxy = iInputDataChannel;
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean insertText(String str) {
        try {
            return this.mInputDataChannelProxy.insertText(str);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "insertText RemoteException.", new Object[0]);
            return false;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean insertRichContent(RichContent richContent) {
        InputDataChannelCallback instance = InputDataChannelCallback.getInstance();
        try {
            this.mInputDataChannelProxy.insertRichContent(richContent, instance);
            instance.awaitForResult();
            return instance.mInsertRichContentResult;
        } catch (RemoteException e) {
            HiLog.error(TAG, "insertRichContent occurs RemoteException: %{public}s", e.getMessage());
            return false;
        } finally {
            instance.reset();
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean deleteBackward(int i) {
        try {
            return this.mInputDataChannelProxy.deleteBackward(i);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "deleteBackward RemoteException.", new Object[0]);
            return false;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean deleteForward(int i) {
        try {
            return this.mInputDataChannelProxy.deleteForward(i);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "deleteForward RemoteException.", new Object[0]);
            return false;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public String getForward(int i) {
        InputDataChannelCallback instance = InputDataChannelCallback.getInstance();
        try {
            this.mInputDataChannelProxy.getForward(i, instance);
            instance.awaitForResult();
            String str = instance.mForwardText;
            instance.reset();
            return str;
        } catch (RemoteException e) {
            HiLog.error(TAG, "getForward occurs RemoteException: %{public}s", e.getMessage());
            instance.reset();
            return "";
        } catch (Throwable th) {
            instance.reset();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public String getBackward(int i) {
        InputDataChannelCallback instance = InputDataChannelCallback.getInstance();
        try {
            this.mInputDataChannelProxy.getBackward(i, instance);
            instance.awaitForResult();
            String str = instance.mBackwardText;
            instance.reset();
            return str;
        } catch (RemoteException e) {
            HiLog.error(TAG, "getBackward occurs RemoteException: %{public}s", e.getMessage());
            instance.reset();
            return "";
        } catch (Throwable th) {
            instance.reset();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean markText(int i, int i2) {
        try {
            return this.mInputDataChannelProxy.markText(i, i2);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "markText RemoteException.", new Object[0]);
            return false;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean unmarkText() {
        try {
            return this.mInputDataChannelProxy.unmarkText();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "unmarkText RemoteException.", new Object[0]);
            return false;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean replaceMarkedText(String str) {
        try {
            return this.mInputDataChannelProxy.replaceMarkedText(str);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "replaceMarkedText RemoteException.", new Object[0]);
            return false;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public EditingText getEditingText(int i, EditingCapability editingCapability) {
        InputDataChannelCallback instance = InputDataChannelCallback.getInstance();
        try {
            this.mInputDataChannelProxy.getEditingText(i, editingCapability, instance);
            instance.awaitForResult();
            EditingText editingText = instance.mEditingText;
            instance.reset();
            return editingText;
        } catch (RemoteException e) {
            HiLog.error(TAG, "getEditingText occurs RemoteException: %{public}s", e.getMessage());
            instance.reset();
            return new EditingText();
        } catch (Throwable th) {
            instance.reset();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean subscribeEditingText(int i, EditingCapability editingCapability) {
        HiLog.debug(TAG, "subscribeEditingText", new Object[0]);
        try {
            return this.mInputDataChannelProxy.subscribeEditingText(i, editingCapability);
        } catch (RemoteException e) {
            HiLog.error(TAG, "subscribeEditingText occurs RemoteException: %{public}s", e.getMessage());
            return false;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean unsubscribeEditingText(int i) {
        try {
            return this.mInputDataChannelProxy.unsubscribeEditingText(i);
        } catch (RemoteException e) {
            HiLog.error(TAG, "unsubscribeEditingText occurs RemoteException: %{public}s", e.getMessage());
            return false;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean sendKeyEvent(KeyEvent keyEvent) {
        try {
            return this.mInputDataChannelProxy.sendKeyEvent(keyEvent);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "sendKeyEvent RemoteException.", new Object[0]);
            return true;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean sendCustomizedData(String str, PacMap pacMap) {
        try {
            return this.mInputDataChannelProxy.sendCustomizedData(str, pacMap);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "sendCustomizedData RemoteException.", new Object[0]);
            return false;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean sendKeyFunction(int i) {
        try {
            return this.mInputDataChannelProxy.sendKeyFunction(i);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "sendKeyFunction RemoteException.", new Object[0]);
            return false;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean selectText(int i, int i2) {
        try {
            return this.mInputDataChannelProxy.selectText(i, i2);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "selectText RemoteException.", new Object[0]);
            return false;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean clearNoncharacterKeyState(int i) {
        try {
            return this.mInputDataChannelProxy.clearNoncharacterKeyState(i);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "clearNoncharacterKeyState RemoteException.", new Object[0]);
            return false;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean recommendText(RecommendationInfo recommendationInfo) {
        try {
            return this.mInputDataChannelProxy.recommendText(recommendationInfo);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "recommendText RemoteException.", new Object[0]);
            return false;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean reviseText(int i, String str, String str2) {
        try {
            return this.mInputDataChannelProxy.reviseText(i, str, str2);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "reviseText RemoteException.", new Object[0]);
            return false;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean sendMenuFunction(int i) {
        try {
            return this.mInputDataChannelProxy.sendMenuFunction(i);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "sendMenuFunction RemoteException.", new Object[0]);
            return false;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean requestCurrentCursorContext() {
        try {
            InputDataChannelCallback instance = InputDataChannelCallback.getInstance();
            this.mInputDataChannelProxy.requestCurrentCursorContext(instance);
            instance.awaitForResult();
            return instance.mSubscribeCaretContextResult;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "requestCurrentCursorContext remoteException.", new Object[0]);
            return true;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean subscribeCursorContext() {
        try {
            InputDataChannelCallback instance = InputDataChannelCallback.getInstance();
            this.mInputDataChannelProxy.subscribeCursorContext(instance);
            instance.awaitForResult();
            return instance.mSubscribeCaretContextResult;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "subscribeCaretContext remoteException.", new Object[0]);
            return true;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public boolean unsubscribeCursorContext() {
        try {
            InputDataChannelCallback instance = InputDataChannelCallback.getInstance();
            this.mInputDataChannelProxy.unsubscribeCursorContext(instance);
            instance.awaitForResult();
            return instance.mSubscribeCaretContextResult;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "unsubscribeCursorContext remoteException.", new Object[0]);
            return true;
        }
    }

    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public int getAutoCapitalizeMode(int i) {
        InputDataChannelCallback instance = InputDataChannelCallback.getInstance();
        try {
            this.mInputDataChannelProxy.getAutoCapitalizeMode(i, instance);
            instance.awaitForResult();
            return instance.mAutoCapitalizeMode;
        } catch (RemoteException e) {
            HiLog.error(TAG, "getAutoCapitalizeMode occurs RemoteException: %{public}s", e.getMessage());
            return 0;
        } finally {
            instance.reset();
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.miscservices.inputmethod.DefaultInputDataChannelImpl, ohos.miscservices.inputmethod.InputDataChannel
    public String getSelectedText(int i) {
        InputDataChannelCallback instance = InputDataChannelCallback.getInstance();
        try {
            this.mInputDataChannelProxy.getSelectedText(i, instance);
            instance.awaitForResult();
            String str = instance.mSelectedText;
            instance.reset();
            return str;
        } catch (RemoteException e) {
            HiLog.error(TAG, "getSelectedText occurs RemoteException: %{public}s", e.getMessage());
            instance.reset();
            return "";
        } catch (Throwable th) {
            instance.reset();
            throw th;
        }
    }

    private static class InputDataChannelCallback extends InputDataChannelCallbackSkeleton {
        private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IInputDataChannelCallback";
        private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputDataChannelCallback");
        private static InputDataChannelCallback sInstance = new InputDataChannelCallback(DESCRIPTOR);
        private int mAutoCapitalizeMode;
        private String mBackwardText;
        private EditingText mEditingText;
        private String mForwardText;
        private boolean mInsertRichContentResult;
        private CountDownLatch mLatch;
        private String mSelectedText;
        private boolean mSubscribeCaretContextResult;

        private InputDataChannelCallback(String str) {
            super(str);
        }

        /* access modifiers changed from: private */
        public static InputDataChannelCallback getInstance() {
            InputDataChannelCallback inputDataChannelCallback;
            synchronized (InputDataChannelCallback.class) {
                if (sInstance != null) {
                    inputDataChannelCallback = sInstance;
                    sInstance = null;
                } else {
                    inputDataChannelCallback = new InputDataChannelCallback(DESCRIPTOR);
                }
                inputDataChannelCallback.mLatch = new CountDownLatch(1);
            }
            return inputDataChannelCallback;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void awaitForResult() {
            try {
                this.mLatch.await(InputDataChannelWrapper.WAIT_DELAY_TIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                HiLog.error(TAG, "awaitForResult InterruptedException: %{public}s", e.getMessage());
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void reset() {
            synchronized (InputDataChannelCallback.class) {
                if (sInstance == null) {
                    sInstance = this;
                    this.mForwardText = null;
                    this.mBackwardText = null;
                    this.mSelectedText = null;
                    this.mEditingText = null;
                    this.mAutoCapitalizeMode = 0;
                    this.mInsertRichContentResult = false;
                    this.mSubscribeCaretContextResult = false;
                }
            }
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannelCallback
        public void setForward(String str) {
            this.mForwardText = str;
            this.mLatch.countDown();
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannelCallback
        public void setBackward(String str) {
            this.mBackwardText = str;
            this.mLatch.countDown();
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannelCallback
        public void notifyEditingText(EditingText editingText) {
            this.mEditingText = editingText;
            this.mLatch.countDown();
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannelCallback
        public void setAutoCapitalizeMode(int i) {
            this.mAutoCapitalizeMode = i;
            this.mLatch.countDown();
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannelCallback
        public void setSelectedText(String str) {
            this.mSelectedText = str;
            this.mLatch.countDown();
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannelCallback
        public void notifySubscribeCaretContextResult(boolean z) {
            this.mSubscribeCaretContextResult = z;
            this.mLatch.countDown();
        }

        @Override // ohos.miscservices.inputmethod.internal.IInputDataChannelCallback
        public void notifyInsertRichContentResult(boolean z) {
            this.mInsertRichContentResult = z;
            this.mLatch.countDown();
        }
    }
}

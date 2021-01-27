package ohos.miscservices.inputmethodability.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.InputMethodSession;
import com.android.internal.os.SomeArgs;
import com.android.internal.view.IInputMethodSession;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.miscservices.inputmethod.adapter.CompletionInfoAdapter;
import ohos.miscservices.inputmethod.adapter.ExtractedTextAdapter;
import ohos.miscservices.inputmethod.internal.IInputMethodAgent;
import ohos.miscservices.inputmethod.internal.InputMethodAgentSkeleton;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class InputMethodSessionAdapter {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218110976, "InputMethodSessionAdapter");
    private static String sInvokeMethodInfo;
    private ImeInputEventReceiver mEventReceiver;
    private InputChannel mInputChannel;
    private IInputMethodAgent mInputMethodAgent;
    private IInputMethodSessionImpl mSessionImpl;

    public InputMethodSessionAdapter(Context context, InputChannel inputChannel, IRemoteObject iRemoteObject) {
        this.mInputChannel = inputChannel;
        if (iRemoteObject == null) {
            HiLog.error(LABEL, "inputMethodAgent is null", new Object[0]);
            return;
        }
        this.mInputMethodAgent = InputMethodAgentSkeleton.asInterface(iRemoteObject);
        if (inputChannel == null) {
            HiLog.error(LABEL, "the input event channel is null.", new Object[0]);
        } else if (context == null) {
            HiLog.error(LABEL, "The application context is null.", new Object[0]);
            return;
        } else {
            this.mEventReceiver = new ImeInputEventReceiver(inputChannel, context.getMainLooper());
        }
        this.mSessionImpl = new IInputMethodSessionImpl(context);
    }

    public IInputMethodSession getInputMethodSession() {
        return this.mSessionImpl;
    }

    public static String getInvokeMethodInfo() {
        return sInvokeMethodInfo;
    }

    class IInputMethodSessionImpl extends IInputMethodSession.Stub implements Handler.Callback {
        private static final int DO_APP_SPECIAL_COMMAND = 90;
        private static final int DO_FINISH_SESSION = 100;
        private static final int DO_NOTIFY_IMA_HIDDEN = 110;
        private static final int DO_REPORT_COMPLETIONS = 65;
        private static final int DO_SELECTION_CHANGED = 75;
        private static final int DO_SWITCH_SOFT_INPUT = 95;
        private static final int DO_UPDATE_CURSOR = 80;
        private static final int DO_UPDATE_CURSOR_INFO = 85;
        private static final int DO_UPDATE_EDITING_TEXT = 70;
        private static final int DO_VIEW_CLICKED = 105;
        private static final int MATRIX_VALUE_LEN = 9;
        private Handler mHandler;
        private final HiLogLabel subTag = new HiLogLabel(3, 218110976, InputMethodSessionAdapter.LABEL.tag.concat("#InputMethodSessionAdapterImpl"));

        IInputMethodSessionImpl(Context context) {
            this.mHandler = Handler.createAsync(context.getMainLooper(), this);
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message message) {
            return processMsg(message);
        }

        private boolean processMsg(Message message) {
            if (InputMethodSessionAdapter.this.mInputMethodAgent == null) {
                recycleArgs(message);
                return true;
            }
            executeMessage(message);
            return true;
        }

        private void recycleArgs(Message message) {
            int i = message.what;
            if (i != DO_SELECTION_CHANGED && i != 90) {
                HiLog.debug(InputMethodSessionAdapter.LABEL, "Args does not need to be recycled.", new Object[0]);
            } else if (!(message.obj instanceof SomeArgs)) {
                HiLog.debug(InputMethodSessionAdapter.LABEL, "The type does not match. No recycling is required.", new Object[0]);
            } else {
                ((SomeArgs) message.obj).recycle();
            }
        }

        private void executeMessage(Message message) {
            int i = message.what;
            if (i == DO_REPORT_COMPLETIONS) {
                processReportCompletions(message);
            } else if (i == DO_UPDATE_EDITING_TEXT) {
                processUpdateEditingText(message);
            } else if (i == DO_SELECTION_CHANGED) {
                processSelectionChanged(message);
            } else if (i == DO_UPDATE_CURSOR) {
                String unused = InputMethodSessionAdapter.sInvokeMethodInfo = "updateCursor";
            } else if (i == DO_UPDATE_CURSOR_INFO) {
                processUpdateCursorInfo(message);
            } else if (i == 90) {
                String unused2 = InputMethodSessionAdapter.sInvokeMethodInfo = "appPrivateCommand";
                recycleArgs(message);
            } else if (i == DO_SWITCH_SOFT_INPUT) {
                String unused3 = InputMethodSessionAdapter.sInvokeMethodInfo = "toggleSoftInput";
            } else if (i == 100) {
                doSessionFinished();
            } else if (i == DO_VIEW_CLICKED) {
                String unused4 = InputMethodSessionAdapter.sInvokeMethodInfo = "viewClicked";
            } else if (i != 110) {
                HiLog.debug(InputMethodSessionAdapter.LABEL, "Invalid message, Ignore", new Object[0]);
            } else {
                String unused5 = InputMethodSessionAdapter.sInvokeMethodInfo = "notifyImeHidden";
            }
        }

        private void processReportCompletions(Message message) {
            if (!(message.obj instanceof CompletionInfo[])) {
                HiLog.error(this.subTag, "processReportCompletions:Incompatible Type!", new Object[0]);
                return;
            }
            try {
                InputMethodSessionAdapter.this.mInputMethodAgent.sendRecommendationInfo(convertToRecommendationInfos((CompletionInfo[]) message.obj));
            } catch (RemoteException unused) {
                HiLog.error(this.subTag, "processReportCompletions failed!", new Object[0]);
            }
        }

        private void processUpdateEditingText(Message message) {
            try {
                InputMethodSessionAdapter.this.mInputMethodAgent.notifyEditingTextChanged(message.arg1, ExtractedTextAdapter.convertToEditingText((ExtractedText) message.obj));
            } catch (RemoteException unused) {
                HiLog.error(this.subTag, "processUpdateEditingText failed!", new Object[0]);
            }
        }

        private void processSelectionChanged(Message message) {
            SomeArgs someArgs = (SomeArgs) message.obj;
            try {
                InputMethodSessionAdapter.this.mInputMethodAgent.notifySelectionChanged(someArgs.argi1, someArgs.argi2, someArgs.argi3, someArgs.argi4);
            } catch (RemoteException unused) {
                HiLog.error(this.subTag, "processSelectionChanged failed!", new Object[0]);
            }
            someArgs.recycle();
        }

        private void processUpdateCursorInfo(Message message) {
            if (!(message.obj instanceof CursorAnchorInfo)) {
                HiLog.error(this.subTag, "processUpdateCursorInfo: Incompatible Type!", new Object[0]);
                return;
            }
            CursorAnchorInfo cursorAnchorInfo = (CursorAnchorInfo) message.obj;
            if (cursorAnchorInfo == null) {
                HiLog.error(this.subTag, "processUpdateCursorInfo: cursor info is null", new Object[0]);
                return;
            }
            float insertionMarkerHorizontal = cursorAnchorInfo.getInsertionMarkerHorizontal();
            float insertionMarkerBottom = cursorAnchorInfo.getInsertionMarkerBottom();
            float insertionMarkerTop = cursorAnchorInfo.getInsertionMarkerTop();
            float[] fArr = new float[9];
            cursorAnchorInfo.getMatrix().getValues(fArr);
            try {
                InputMethodSessionAdapter.this.mInputMethodAgent.notifyCursorCoordinateChanged(insertionMarkerHorizontal, insertionMarkerBottom, insertionMarkerTop, fArr);
            } catch (RemoteException unused) {
                HiLog.error(this.subTag, "processUpdateCursorInfo failed!", new Object[0]);
            }
        }

        private void doSessionFinished() {
            InputMethodSessionAdapter.this.mInputMethodAgent = null;
            if (InputMethodSessionAdapter.this.mEventReceiver != null) {
                InputMethodSessionAdapter.this.mEventReceiver.dispose();
                InputMethodSessionAdapter.this.mEventReceiver = null;
            }
            if (InputMethodSessionAdapter.this.mInputChannel != null) {
                InputMethodSessionAdapter.this.mInputChannel.dispose();
                InputMethodSessionAdapter.this.mInputChannel = null;
            }
        }

        private RecommendationInfo[] convertToRecommendationInfos(CompletionInfo[] completionInfoArr) {
            if (completionInfoArr == null) {
                HiLog.warn(InputMethodSessionAdapter.LABEL, "there is no recommendation info", new Object[0]);
                return new RecommendationInfo[0];
            }
            RecommendationInfo[] recommendationInfoArr = new RecommendationInfo[completionInfoArr.length];
            for (int i = 0; i < completionInfoArr.length; i++) {
                recommendationInfoArr[i] = CompletionInfoAdapter.convertToRecommendationInfo(completionInfoArr[i]);
            }
            return recommendationInfoArr;
        }

        public void updateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(DO_UPDATE_CURSOR_INFO, cursorAnchorInfo));
        }

        public void updateExtractedText(int i, ExtractedText extractedText) {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(DO_UPDATE_EDITING_TEXT, i, 0, extractedText));
        }

        public void viewClicked(boolean z) {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(DO_VIEW_CLICKED, Integer.valueOf(z ? 1 : 0)));
        }

        public void updateCursor(Rect rect) {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(DO_UPDATE_CURSOR, rect));
        }

        public void notifyImeHidden() {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(110));
        }

        public void updateSelection(int i, int i2, int i3, int i4, int i5, int i6) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            obtain.argi2 = i2;
            obtain.argi3 = i3;
            obtain.argi4 = i4;
            obtain.argi5 = i5;
            obtain.argi6 = i6;
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(DO_SELECTION_CHANGED, obtain));
        }

        public void appPrivateCommand(String str, Bundle bundle) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = str;
            obtain.arg2 = bundle;
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(90, obtain));
        }

        public void finishSession() {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(100));
        }

        public void displayCompletions(CompletionInfo[] completionInfoArr) {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(DO_REPORT_COMPLETIONS, completionInfoArr));
        }

        public void toggleSoftInput(int i, int i2) {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(DO_SWITCH_SOFT_INPUT, i, i2));
        }
    }

    /* access modifiers changed from: package-private */
    public final class ImeInputEventReceiver extends InputEventReceiver implements InputMethodSession.EventCallback {
        private final SparseArray<InputEvent> mPendingEvents = new SparseArray<>();

        ImeInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent inputEvent) {
            Optional<MultimodalEvent> optional;
            if (InputMethodSessionAdapter.this.mInputMethodAgent == null) {
                finishInputEvent(inputEvent, false);
                return;
            }
            this.mPendingEvents.put(inputEvent.getSequenceNumber(), inputEvent);
            if (inputEvent instanceof KeyEvent) {
                optional = MultimodalEventFactory.createEvent((KeyEvent) inputEvent);
            } else if (inputEvent instanceof MotionEvent) {
                optional = MultimodalEventFactory.createEvent((MotionEvent) inputEvent);
            } else {
                HiLog.warn(InputMethodSessionAdapter.LABEL, "invalid event type", new Object[0]);
                finishInputEvent(inputEvent, false);
                return;
            }
            if (!optional.isPresent()) {
                HiLog.error(InputMethodSessionAdapter.LABEL, "invalid event", new Object[0]);
                finishInputEvent(inputEvent, false);
                return;
            }
            try {
                finishedEvent(inputEvent.getSequenceNumber(), InputMethodSessionAdapter.this.mInputMethodAgent.dispatchMultimodalEvent(optional.get()));
            } catch (RemoteException unused) {
                HiLog.error(InputMethodSessionAdapter.LABEL, "remote exception: handle event failed", new Object[0]);
                finishedEvent(inputEvent.getSequenceNumber(), false);
            }
        }

        @Override // android.view.inputmethod.InputMethodSession.EventCallback
        public void finishedEvent(int i, boolean z) {
            int indexOfKey = this.mPendingEvents.indexOfKey(i);
            if (indexOfKey >= 0) {
                this.mPendingEvents.removeAt(indexOfKey);
                finishInputEvent(this.mPendingEvents.valueAt(indexOfKey), z);
                return;
            }
            HiLog.warn(InputMethodSessionAdapter.LABEL, "This event does not exist.", new Object[0]);
        }
    }
}

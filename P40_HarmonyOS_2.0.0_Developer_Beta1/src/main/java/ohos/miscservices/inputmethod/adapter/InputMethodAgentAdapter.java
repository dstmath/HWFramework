package ohos.miscservices.inputmethod.adapter;

import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.InputMethodManager;
import java.util.Optional;
import java.util.function.Consumer;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.adapter.utils.AdaptUtil;
import ohos.miscservices.adapter.utils.ReflectUtil;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.rpc.RemoteException;

public class InputMethodAgentAdapter {
    private static final int SELECTION_INITIAL_INDEX = -1;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodAgentAdapter");
    private static volatile InputMethodAgentAdapter sInstance;
    private float mCursorHorizontal = 0.0f;
    private String mEditingTextContent;
    private String mPendingText = "";
    private int mSelectionEnd = -1;
    private int mSelectionStart = -1;

    private InputMethodAgentAdapter() {
    }

    public static InputMethodAgentAdapter getInstance() {
        if (sInstance == null) {
            synchronized (InputMethodAgentAdapter.class) {
                if (sInstance == null) {
                    sInstance = new InputMethodAgentAdapter();
                }
            }
        }
        return sInstance;
    }

    public void notifySelectionChanged(int i, int i2, int i3, int i4) throws RemoteException {
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (!inputMethodManager.isPresent()) {
            HiLog.error(TAG, "notifySelectionChanged failed: current InputMethodManager instance is null.", new Object[0]);
            return;
        }
        Optional<View> surfaceView = ReflectUtil.getSurfaceView();
        if (!surfaceView.isPresent()) {
            HiLog.error(TAG, "notifySelectionChanged failed because current surfaceView is null.", new Object[0]);
            return;
        }
        inputMethodManager.get().updateSelection(surfaceView.get(), i3, i4, -1, -1);
        this.mSelectionStart = i3;
        this.mSelectionEnd = i4;
        HiLog.debug(TAG, "InputMethodController#notifySelectionChanged works well, adaption successful.", new Object[0]);
    }

    public int[] getSelectionInfo() {
        return new int[]{this.mSelectionStart, this.mSelectionEnd};
    }

    public void sendRecommendationInfo(RecommendationInfo[] recommendationInfoArr) throws RemoteException {
        CompletionInfo[] completionInfoArr;
        if (recommendationInfoArr == null) {
            completionInfoArr = null;
        } else {
            int length = recommendationInfoArr.length;
            CompletionInfo[] completionInfoArr2 = new CompletionInfo[length];
            for (int i = 0; i < length; i++) {
                completionInfoArr2[i] = CompletionInfoAdapter.convertToCompletionInfo(recommendationInfoArr[i]);
            }
            completionInfoArr = completionInfoArr2;
        }
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (!inputMethodManager.isPresent()) {
            HiLog.error(TAG, "sendRecommendationInfo failed: current InputMethodManager instance is null.", new Object[0]);
            return;
        }
        Optional<View> surfaceView = ReflectUtil.getSurfaceView();
        if (!surfaceView.isPresent()) {
            HiLog.error(TAG, "sendRecommendationInfo failed because current surfaceView is null.", new Object[0]);
            return;
        }
        inputMethodManager.get().displayCompletions(surfaceView.get(), completionInfoArr);
        HiLog.debug(TAG, "sendRecommendationInfo down.", new Object[0]);
    }

    public void notifyEditingTextChanged(int i, EditingText editingText) throws RemoteException {
        if (editingText == null) {
            HiLog.error(TAG, "InputMethodController#notifyEditingTextChanged does not work well, editingText is null", new Object[0]);
            return;
        }
        HiLog.debug(TAG, " before convertToExtractedText()", new Object[0]);
        ExtractedText convertToExtractedText = ExtractedTextAdapter.convertToExtractedText(editingText);
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (!inputMethodManager.isPresent()) {
            HiLog.error(TAG, "notifyEditingTextChanged failed: current InputMethodManager instance is null.", new Object[0]);
            return;
        }
        Optional<View> surfaceView = ReflectUtil.getSurfaceView();
        if (!surfaceView.isPresent()) {
            HiLog.error(TAG, "notifyEditingTextChanged failed because current surfaceView is null.", new Object[0]);
            return;
        }
        inputMethodManager.get().updateExtractedText(surfaceView.get(), i, convertToExtractedText);
        this.mEditingTextContent = editingText.getTextContent();
    }

    public String getEditingTextContent() {
        return this.mEditingTextContent;
    }

    public float getCursorHorizontal() {
        return this.mCursorHorizontal;
    }

    public void notifyCursorCoordinateChanged(float f, float f2, float f3, float[] fArr) throws RemoteException {
        Optional<InputMethodManager> inputMethodManager = AdaptUtil.getInputMethodManager();
        if (!inputMethodManager.isPresent()) {
            HiLog.error(TAG, "notifyCursorCoordinateChanged failed: current InputMethodManager instance is null.", new Object[0]);
            return;
        }
        Optional<View> surfaceView = ReflectUtil.getSurfaceView();
        if (!surfaceView.isPresent()) {
            HiLog.error(TAG, "notifyCursorCoordinateChanged failed because current surfaceView is null.", new Object[0]);
            return;
        }
        Optional<CursorAnchorInfo> convertToCursorAnchorInfo = CursorContextInfoAdapter.convertToCursorAnchorInfo(f, f2, f3, fArr);
        CursorAnchorInfo cursorAnchorInfo = null;
        if (convertToCursorAnchorInfo.isPresent()) {
            cursorAnchorInfo = convertToCursorAnchorInfo.get();
        }
        inputMethodManager.get().updateCursorAnchorInfo(surfaceView.get(), cursorAnchorInfo);
        convertToCursorAnchorInfo.ifPresent(new Consumer() {
            /* class ohos.miscservices.inputmethod.adapter.$$Lambda$InputMethodAgentAdapter$SW09O5HBfQmMfgqutYoIGyb1UDc */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                InputMethodAgentAdapter.this.lambda$notifyCursorCoordinateChanged$0$InputMethodAgentAdapter((CursorAnchorInfo) obj);
            }
        });
    }

    public /* synthetic */ void lambda$notifyCursorCoordinateChanged$0$InputMethodAgentAdapter(CursorAnchorInfo cursorAnchorInfo) {
        this.mCursorHorizontal = cursorAnchorInfo.getInsertionMarkerHorizontal();
    }
}

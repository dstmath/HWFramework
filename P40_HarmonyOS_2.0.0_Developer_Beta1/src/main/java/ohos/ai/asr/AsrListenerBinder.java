package ohos.ai.asr;

import android.os.Bundle;
import ohos.ai.asr.service.AsrPluginListener;
import ohos.ai.asr.taskhandler.AsrCallbackTaskHandler;
import ohos.ai.engine.handler.TaskInfo;
import ohos.ai.engine.utils.HiAILog;
import ohos.utils.adapter.PacMapUtils;

public class AsrListenerBinder extends AsrPluginListener.Stub {
    private static final String TAG = AsrListenerBinder.class.getSimpleName();
    private AsrCallbackTaskHandler asrCallbackTaskHandler;

    public void setAsrCallbackTaskHandler(AsrCallbackTaskHandler asrCallbackTaskHandler2) {
        this.asrCallbackTaskHandler = asrCallbackTaskHandler2;
    }

    @Override // ohos.ai.asr.service.AsrPluginListener
    public void onInit(Bundle bundle) {
        HiAILog.info(TAG, "onInit");
        if (this.asrCallbackTaskHandler != null) {
            this.asrCallbackTaskHandler.start(new TaskInfo.Builder(1).setObject(PacMapUtils.convertFromBundle(bundle)).build());
        }
    }

    @Override // ohos.ai.asr.service.AsrPluginListener
    public void onRecordStart() {
        HiAILog.info(TAG, "onRecordStart");
        AsrCallbackTaskHandler asrCallbackTaskHandler2 = this.asrCallbackTaskHandler;
        if (asrCallbackTaskHandler2 != null) {
            asrCallbackTaskHandler2.start(new TaskInfo.Builder(2).build());
        }
    }

    @Override // ohos.ai.asr.service.AsrPluginListener
    public void onBeginningOfSpeech() {
        HiAILog.info(TAG, "onBeginningOfSpeech");
        AsrCallbackTaskHandler asrCallbackTaskHandler2 = this.asrCallbackTaskHandler;
        if (asrCallbackTaskHandler2 != null) {
            asrCallbackTaskHandler2.start(new TaskInfo.Builder(3).build());
        }
    }

    @Override // ohos.ai.asr.service.AsrPluginListener
    public void onRmsChanged(float f) {
        String str = TAG;
        HiAILog.info(str, "onRmsChanged " + f);
        AsrCallbackTaskHandler asrCallbackTaskHandler2 = this.asrCallbackTaskHandler;
        if (asrCallbackTaskHandler2 != null) {
            asrCallbackTaskHandler2.start(new TaskInfo.Builder(4).setObject(Float.valueOf(f)).build());
        }
    }

    @Override // ohos.ai.asr.service.AsrPluginListener
    public void onBufferReceived(byte[] bArr) {
        HiAILog.info(TAG, "onBufferReceived ");
        AsrCallbackTaskHandler asrCallbackTaskHandler2 = this.asrCallbackTaskHandler;
        if (asrCallbackTaskHandler2 != null) {
            asrCallbackTaskHandler2.start(new TaskInfo.Builder(5).setObject(bArr).build());
        }
    }

    @Override // ohos.ai.asr.service.AsrPluginListener
    public void onEndOfSpeech() {
        HiAILog.info(TAG, "onEndOfSpeech ");
        AsrCallbackTaskHandler asrCallbackTaskHandler2 = this.asrCallbackTaskHandler;
        if (asrCallbackTaskHandler2 != null) {
            asrCallbackTaskHandler2.start(new TaskInfo.Builder(6).build());
        }
    }

    @Override // ohos.ai.asr.service.AsrPluginListener
    public void onRecordEnd() {
        HiAILog.info(TAG, "onRecordEnd ");
        AsrCallbackTaskHandler asrCallbackTaskHandler2 = this.asrCallbackTaskHandler;
        if (asrCallbackTaskHandler2 != null) {
            asrCallbackTaskHandler2.start(new TaskInfo.Builder(7).build());
        }
    }

    @Override // ohos.ai.asr.service.AsrPluginListener
    public void onError(int i) {
        String str = TAG;
        HiAILog.info(str, "onError " + i);
        AsrCallbackTaskHandler asrCallbackTaskHandler2 = this.asrCallbackTaskHandler;
        if (asrCallbackTaskHandler2 != null) {
            asrCallbackTaskHandler2.start(new TaskInfo.Builder(8).setArgOne(i).build());
        }
    }

    @Override // ohos.ai.asr.service.AsrPluginListener
    public void onResults(Bundle bundle) {
        HiAILog.info(TAG, "onResults");
        if (this.asrCallbackTaskHandler != null) {
            this.asrCallbackTaskHandler.start(new TaskInfo.Builder(9).setObject(PacMapUtils.convertFromBundle(bundle)).build());
        }
    }

    @Override // ohos.ai.asr.service.AsrPluginListener
    public void onIntermediateResults(Bundle bundle) {
        HiAILog.info(TAG, "onIntermediateResults");
        if (this.asrCallbackTaskHandler != null) {
            this.asrCallbackTaskHandler.start(new TaskInfo.Builder(10).setObject(PacMapUtils.convertFromBundle(bundle)).build());
        }
    }

    @Override // ohos.ai.asr.service.AsrPluginListener
    public void onEnd() {
        HiAILog.info(TAG, "onEnd");
        AsrCallbackTaskHandler asrCallbackTaskHandler2 = this.asrCallbackTaskHandler;
        if (asrCallbackTaskHandler2 != null) {
            asrCallbackTaskHandler2.start(new TaskInfo.Builder(11).build());
        }
    }

    @Override // ohos.ai.asr.service.AsrPluginListener
    public void onEvent(int i, Bundle bundle) {
        HiAILog.info(TAG, "onEvent");
        if (this.asrCallbackTaskHandler != null) {
            this.asrCallbackTaskHandler.start(new TaskInfo.Builder(12).setObject(PacMapUtils.convertFromBundle(bundle)).setArgOne(i).build());
        }
    }

    @Override // ohos.ai.asr.service.AsrPluginListener
    public void onLexiconUpdated(String str, int i) {
        HiAILog.info(TAG, "onLexiconUpdated");
        AsrCallbackTaskHandler asrCallbackTaskHandler2 = this.asrCallbackTaskHandler;
        if (asrCallbackTaskHandler2 != null) {
            asrCallbackTaskHandler2.start(new TaskInfo.Builder(13).setObject(str).setArgOne(i).build());
        }
    }
}

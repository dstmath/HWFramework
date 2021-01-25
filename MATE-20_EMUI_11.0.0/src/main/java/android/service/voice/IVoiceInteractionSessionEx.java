package android.service.voice;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class IVoiceInteractionSessionEx {
    private IVoiceInteractionSession mVoiceInteractionSession;

    public IVoiceInteractionSession getIVoiceInteractionSession() {
        return this.mVoiceInteractionSession;
    }

    public void setIVoiceInteractionSession(IVoiceInteractionSession iVoiceInteractionSession) {
        this.mVoiceInteractionSession = iVoiceInteractionSession;
    }
}

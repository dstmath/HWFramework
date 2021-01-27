package com.huawei.android.internal.app;

import com.android.internal.app.IVoiceInteractor;

public class IVoiceInteractorEx {
    private IVoiceInteractor iVoiceInteractor;

    public IVoiceInteractor getVoiceInteractor() {
        return this.iVoiceInteractor;
    }

    public void setVoiceInteractor(IVoiceInteractor iVoiceInteractor2) {
        this.iVoiceInteractor = iVoiceInteractor2;
    }
}

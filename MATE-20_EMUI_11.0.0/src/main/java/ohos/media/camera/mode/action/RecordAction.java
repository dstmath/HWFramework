package ohos.media.camera.mode.action;

import ohos.media.camera.mode.impl.ActionDataCallbackImpl;
import ohos.media.camera.mode.impl.ActionStateCallbackImpl;

public interface RecordAction extends Action {
    void capture(String str, ActionStateCallbackImpl actionStateCallbackImpl);

    void capture(ActionDataCallbackImpl actionDataCallbackImpl, ActionStateCallbackImpl actionStateCallbackImpl, int i);

    void pause();

    void resume();

    void start(String str, ActionStateCallbackImpl actionStateCallbackImpl);

    void stop();
}

package ohos.aafwk.ability;

import ohos.aafwk.content.IntentParams;

public interface IAbilityContinuation {
    void onCompleteContinuation(int i);

    default void onRemoteTerminated() {
    }

    boolean onRestoreData(IntentParams intentParams);

    boolean onSaveData(IntentParams intentParams);

    boolean onStartContinuation();
}

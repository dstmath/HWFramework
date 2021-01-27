package ohos.account;

import ohos.annotation.SystemApi;

@SystemApi
public interface IOsAccountSubscriber {
    default void onAccountActivated(int i) {
    }

    default void onAccountActivating(int i) {
    }
}

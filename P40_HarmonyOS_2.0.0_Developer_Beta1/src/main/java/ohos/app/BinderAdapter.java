package ohos.app;

import android.os.Binder;
import ohos.aafwk.ability.LocalRemoteObject;

public class BinderAdapter extends Binder {
    private LocalRemoteObject localRemoteObject = null;

    public LocalRemoteObject getLocalRemoteObject() {
        return this.localRemoteObject;
    }

    public void setLocalRemoteObject(LocalRemoteObject localRemoteObject2) {
        this.localRemoteObject = localRemoteObject2;
    }
}

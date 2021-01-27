package ohos.event.commonevent;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.os.IBinder;
import java.util.Optional;
import ohos.aafwk.content.Intent;
import ohos.app.Context;
import ohos.bundle.ElementName;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public final class CommonEventRemoteAbilityHelper {
    private static final String SERVICE_SHELL_SUFFIX = "ShellService";

    static IRemoteObject getRemoteAbility(Context context, Intent intent, Object obj) throws RemoteException {
        IBinder peekService;
        if (!(obj instanceof BroadcastReceiver)) {
            return null;
        }
        BroadcastReceiver broadcastReceiver = (BroadcastReceiver) obj;
        Object hostContext = context.getHostContext();
        if (!(hostContext instanceof android.content.Context)) {
            return null;
        }
        android.content.Context context2 = (android.content.Context) hostContext;
        Optional<android.content.Intent> createAndroidIntent = createAndroidIntent(intent);
        if (!createAndroidIntent.isPresent() || (peekService = broadcastReceiver.peekService(context2, createAndroidIntent.get())) == null) {
            return null;
        }
        Optional translateToIRemoteObject = IPCAdapter.translateToIRemoteObject(peekService);
        if (!translateToIRemoteObject.isPresent()) {
            return null;
        }
        return (IRemoteObject) translateToIRemoteObject.get();
    }

    private static Optional<android.content.Intent> createAndroidIntent(Intent intent) {
        ElementName element = intent.getElement();
        if (element == null) {
            return Optional.empty();
        }
        String bundleName = element.getBundleName();
        String abilityName = element.getAbilityName();
        if (bundleName == null || abilityName == null) {
            return Optional.empty();
        }
        android.content.Intent intent2 = new android.content.Intent();
        intent2.setComponent(new ComponentName(bundleName, abilityName + "ShellService"));
        return Optional.of(intent2);
    }
}

package ohos.abilityshell.utils;

import android.content.Context;
import android.os.IBinder;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;

public class BinderConverter {
    private static final HiLogLabel SHELL_LABLE = new HiLogLabel(3, 218108160, "AbilityShell");

    private BinderConverter() {
    }

    public static Optional<IRemoteObject> getRemoteObjectFromContext(Context context) {
        if (context == null) {
            AppLog.e(SHELL_LABLE, "BinderConverter::getRemoteObjectFromContext param is invalid", new Object[0]);
            return Optional.empty();
        }
        try {
            Object invoke = context.getClass().getMethod("getActivityToken", new Class[0]).invoke(context, new Object[0]);
            if (invoke instanceof IBinder) {
                return IPCAdapter.translateToIRemoteObject(invoke);
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            AppLog.w(SHELL_LABLE, "failed get remote object from context: %{public}s", e.getMessage());
        }
        return Optional.empty();
    }
}

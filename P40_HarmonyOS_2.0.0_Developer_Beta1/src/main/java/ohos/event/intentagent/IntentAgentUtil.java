package ohos.event.intentagent;

import android.app.PendingIntent;
import android.content.IIntentSender;
import android.os.IBinder;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageParcel;

public class IntentAgentUtil {
    private static final String CLASS_NAME = "android.app.PendingIntent";
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.INTENTAGENT_DOMAIN, TAG);
    private static final String TAG = "IntentAgentMarshalling";

    public static boolean writeToParcel(Object obj, MessageParcel messageParcel) {
        if (messageParcel == null) {
            HiLog.warn(LABEL, "out is null.", new Object[0]);
            return false;
        }
        IRemoteObject convertToRemoteObject = convertToRemoteObject(obj);
        if (convertToRemoteObject == null) {
            if (!messageParcel.writeInt(-1)) {
                return false;
            }
            return true;
        } else if (messageParcel.writeInt(0) && messageParcel.writeRemoteObject(convertToRemoteObject)) {
            return true;
        } else {
            return false;
        }
    }

    public static Object readFromParcel(MessageParcel messageParcel) {
        if (messageParcel == null) {
            return null;
        }
        if (messageParcel.readInt() == 0) {
            return getFromParcel(messageParcel);
        }
        HiLog.warn(LABEL, "value is -1.", new Object[0]);
        return null;
    }

    public static void writeToParcelEx(Object obj, MessageParcel messageParcel) {
        if (messageParcel == null) {
            HiLog.warn(LABEL, "writeToParcelEx out is invalid.", new Object[0]);
        } else if (messageParcel.writeString(CLASS_NAME)) {
            messageParcel.writeRemoteObject(convertToRemoteObject(obj));
        }
    }

    private static IRemoteObject convertToRemoteObject(Object obj) {
        if (!(obj instanceof PendingIntent)) {
            HiLog.warn(LABEL, "object is invalid.", new Object[0]);
            return null;
        }
        IIntentSender target = ((PendingIntent) obj).getTarget();
        if (target == null) {
            HiLog.warn(LABEL, "aosp binder is invalid.", new Object[0]);
            return null;
        }
        Optional translateToIRemoteObject = IPCAdapter.translateToIRemoteObject(target.asBinder());
        if (translateToIRemoteObject.isPresent()) {
            return (IRemoteObject) translateToIRemoteObject.get();
        }
        return null;
    }

    private static PendingIntent getFromParcel(MessageParcel messageParcel) {
        IRemoteObject readRemoteObject = messageParcel.readRemoteObject();
        if (readRemoteObject == null) {
            HiLog.warn(LABEL, "getFromParcel remoteObject from parcel failed.", new Object[0]);
            return null;
        }
        Optional translateToIBinder = IPCAdapter.translateToIBinder(readRemoteObject);
        IBinder iBinder = (!translateToIBinder.isPresent() || !(translateToIBinder.get() instanceof IBinder)) ? null : (IBinder) translateToIBinder.get();
        if (iBinder == null) {
            HiLog.warn(LABEL, "getFromParcel convert remoteObject to binder failed.", new Object[0]);
            return null;
        }
        try {
            return (PendingIntent) PendingIntent.class.getDeclaredConstructor(IBinder.class, Object.class).newInstance(iBinder, null);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.warn(LABEL, "object is null.", new Object[0]);
            return null;
        }
    }
}

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
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.INTENTAGENT_DOMAIN, TAG);
    private static final String TAG = "IntentAgentMarshalling";

    static boolean writeToParcel(Object obj, MessageParcel messageParcel) {
        if (messageParcel == null) {
            HiLog.warn(LABEL, "out is null.", new Object[0]);
            return false;
        } else if (!(obj instanceof PendingIntent)) {
            HiLog.warn(LABEL, "object is invalid.", new Object[0]);
            return false;
        } else {
            IIntentSender target = ((PendingIntent) obj).getTarget();
            if (target == null) {
                HiLog.warn(LABEL, "target is invalid.", new Object[0]);
                return false;
            }
            IRemoteObject iRemoteObject = null;
            Optional translateToIRemoteObject = IPCAdapter.translateToIRemoteObject(target.asBinder());
            if (translateToIRemoteObject.isPresent()) {
                iRemoteObject = (IRemoteObject) translateToIRemoteObject.get();
            }
            if (iRemoteObject == null) {
                messageParcel.writeInt(-1);
                return true;
            }
            messageParcel.writeInt(0);
            messageParcel.writeRemoteObject(iRemoteObject);
            return true;
        }
    }

    static Object readFromParcel(MessageParcel messageParcel) {
        if (messageParcel == null) {
            return null;
        }
        if (messageParcel.readInt() != 0) {
            HiLog.warn(LABEL, "value is -1.", new Object[0]);
            return null;
        }
        IRemoteObject readRemoteObject = messageParcel.readRemoteObject();
        if (readRemoteObject == null) {
            HiLog.warn(LABEL, "read remoteObject from parcel failed.", new Object[0]);
            return null;
        }
        Optional translateToIBinder = IPCAdapter.translateToIBinder(readRemoteObject);
        IBinder iBinder = (!translateToIBinder.isPresent() || !(translateToIBinder.get() instanceof IBinder)) ? null : (IBinder) translateToIBinder.get();
        if (iBinder == null) {
            HiLog.warn(LABEL, "convert remoteObject to binder failed.", new Object[0]);
            return null;
        }
        try {
            return PendingIntent.class.getDeclaredConstructor(IBinder.class, Object.class).newInstance(iBinder, null);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.warn(LABEL, "object is null.", new Object[0]);
            return null;
        }
    }
}

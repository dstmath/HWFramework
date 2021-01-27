package com.huawei.ohos.foundation;

import android.os.IBinder;
import java.util.Optional;
import ohos.bundle.ElementName;
import ohos.distributedschedule.adapter.ElementNameAdapter;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public final class EmuiTransporter {
    private static final HiLogLabel DMS_LABEL = new HiLogLabel(3, 218109952, "DmsProxy_EmuiTransporter");
    private static final String INTERFACE_TOKEN = "com.huawei.harmonyos.interwork.IAbilityConnection";
    private static final int ON_ABILITY_CONNECT_DONE = 1;
    private static final int ON_ABILITY_DISCONNECT_DONE = 2;

    static void transactConnectToRemoteService(IRemoteObject iRemoteObject, ElementName elementName, IBinder iBinder, int i) {
        HiLog.debug(DMS_LABEL, "transactConnectToRemoteService is called", new Object[0]);
        if (iRemoteObject != null && elementName != null && iBinder != null) {
            Optional<IRemoteObject> convertToHarymonyRemote = convertToHarymonyRemote(iBinder);
            if (convertToHarymonyRemote.isPresent()) {
                MessageParcel obtain = MessageParcel.obtain();
                MessageParcel obtain2 = MessageParcel.obtain();
                try {
                    if (!writeConnectParcel(obtain, elementName, convertToHarymonyRemote.get(), i)) {
                        HiLog.warn(DMS_LABEL, "EmuiTransporter: connect message write failed", new Object[0]);
                        obtain.reclaim();
                        obtain2.reclaim();
                        return;
                    }
                    HiLog.info(DMS_LABEL, "transactConnectToRemoteService result %{public}b", new Object[]{Boolean.valueOf(iRemoteObject.sendRequest(1, obtain, obtain2, new MessageOption(1)))});
                    obtain.reclaim();
                    obtain2.reclaim();
                } catch (RemoteException unused) {
                    HiLog.error(DMS_LABEL, "onServiceConnected binder transact exception", new Object[0]);
                } catch (Throwable th) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    throw th;
                }
            }
        }
    }

    static void transactDisconnectToRemoteService(IRemoteObject iRemoteObject, ElementName elementName, int i) {
        HiLog.debug(DMS_LABEL, "transactDisconnectToRemoteService is called, resultCode=%{private}d", new Object[]{Integer.valueOf(i)});
        if (iRemoteObject != null && elementName != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!writeDisConnectParcel(obtain, elementName, i)) {
                    HiLog.warn(DMS_LABEL, "EmuiTransporter: disconnect message write failed", new Object[0]);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return;
                }
                HiLog.info(DMS_LABEL, "EmuiTransporter: transactDisconnectToRemoteService result %{public}b", new Object[]{Boolean.valueOf(iRemoteObject.sendRequest(2, obtain, obtain2, new MessageOption(1)))});
                obtain.reclaim();
                obtain2.reclaim();
            } catch (RemoteException unused) {
                HiLog.error(DMS_LABEL, "EmuiTransporter: onServiceConnected binder transact exception", new Object[0]);
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }

    static Optional<IRemoteObject> convertToHarymonyRemote(IBinder iBinder) {
        if (iBinder == null) {
            return Optional.empty();
        }
        return IPCAdapter.translateToIRemoteObject(iBinder);
    }

    static boolean writeConnectParcel(MessageParcel messageParcel, ElementName elementName, IRemoteObject iRemoteObject, int i) {
        if (messageParcel == null || elementName == null || iRemoteObject == null || !messageParcel.writeInterfaceToken(INTERFACE_TOKEN) || !new ElementNameAdapter(elementName).marshalling(messageParcel) || !messageParcel.writeRemoteObject(iRemoteObject) || !messageParcel.writeInt(i)) {
            return false;
        }
        return true;
    }

    static boolean writeDisConnectParcel(MessageParcel messageParcel, ElementName elementName, int i) {
        if (messageParcel == null || elementName == null || !messageParcel.writeInterfaceToken(INTERFACE_TOKEN) || !new ElementNameAdapter(elementName).marshalling(messageParcel) || !messageParcel.writeInt(i)) {
            return false;
        }
        return true;
    }
}

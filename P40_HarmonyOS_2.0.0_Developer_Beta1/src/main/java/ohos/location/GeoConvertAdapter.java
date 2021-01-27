package ohos.location;

import java.util.List;
import java.util.Locale;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.location.common.LBSLog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

public class GeoConvertAdapter implements IGeoConvertAdapter {
    private static final HiLogLabel LABEL = new HiLogLabel(3, LBSLog.LOCATOR_LOG_ID, "GeoConvertProxy");
    private static final String LOCATOR_DESCRIPTOR = "location.ILocator";
    private static final Object LOCK = new Object();
    private static final int MAX_RESULT = 10;
    private static final String NO_RESULT = "get no result";
    private static final int REPLY_NO_EXCEPTION = 0;
    private static final int TRANSACT_GET_FROM_COORDINATE = 12;
    private static final int TRANSACT_GET_FROM_LOCATION_NAME = 13;
    private static final int TRANSACT_IS_AVAILABLE = 11;
    private static volatile GeoConvertAdapter instance;
    private IRemoteObject mRemoteObject;

    private GeoConvertAdapter() {
        asObject();
    }

    public static GeoConvertAdapter getInstance() {
        GeoConvertAdapter geoConvertAdapter;
        synchronized (LOCK) {
            if (instance == null) {
                instance = new GeoConvertAdapter();
            }
            geoConvertAdapter = instance;
        }
        return geoConvertAdapter;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        IRemoteObject iRemoteObject = this.mRemoteObject;
        if (iRemoteObject != null) {
            return iRemoteObject;
        }
        this.mRemoteObject = SysAbilityManager.getSysAbility(SystemAbilityDefinition.LOCATION_LOCATOR_SA_ID);
        if (this.mRemoteObject == null) {
            HiLog.error(LABEL, "getSysAbility(%{public}d) failed.", Integer.valueOf((int) SystemAbilityDefinition.LOCATION_LOCATOR_SA_ID));
            return this.mRemoteObject;
        }
        HiLog.info(LABEL, "Get %{public}d completed.", Integer.valueOf((int) SystemAbilityDefinition.LOCATION_LOCATOR_SA_ID));
        return this.mRemoteObject;
    }

    @Override // ohos.location.IGeoConvertAdapter
    public boolean isGeoAvailable() {
        boolean z = false;
        HiLog.debug(LABEL, "calling isGeoAvailable", new Object[0]);
        if (this.mRemoteObject == null) {
            HiLog.error(LABEL, "can not remote to locator sa", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(LOCATOR_DESCRIPTOR);
        try {
            this.mRemoteObject.sendRequest(11, obtain, obtain2, messageOption);
            if (obtain2.readInt() != 0) {
                HiLog.error(LABEL, "have no result, cause some excepiton happened in lower service.", new Object[0]);
            } else if (obtain2.readInt() == 1) {
                z = true;
            }
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "isGeoAvailable: got RemoteException", new Object[0]);
        }
        obtain.reclaim();
        obtain2.reclaim();
        return z;
    }

    @Override // ohos.location.IGeoConvertAdapter
    public String getAddressFromLocation(double d, double d2, int i, Locale locale, List<GeoAddress> list) {
        HiLog.debug(LABEL, "calling getAddressFromLocation", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(LOCATOR_DESCRIPTOR);
        obtain.writeDouble(d);
        obtain.writeDouble(d2);
        obtain.writeInt(i);
        obtain.writeInt(1);
        obtain.writeString(locale.getLanguage());
        obtain.writeString(locale.getCountry());
        obtain.writeString(locale.getVariant());
        obtain.writeString("");
        String parseResult = parseResult(12, obtain, list);
        obtain.reclaim();
        return parseResult;
    }

    @Override // ohos.location.IGeoConvertAdapter
    public String getAddressFromLocationName(String str, int i, Locale locale, List<GeoAddress> list, double d, double d2, double d3, double d4) {
        HiLog.debug(LABEL, "calling getAddressFromLocation by boundary", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(LOCATOR_DESCRIPTOR);
        obtain.writeString(str);
        obtain.writeDouble(d);
        obtain.writeDouble(d2);
        obtain.writeDouble(d3);
        obtain.writeDouble(d4);
        obtain.writeInt(i);
        obtain.writeInt(1);
        obtain.writeString(locale.getLanguage());
        obtain.writeString(locale.getCountry());
        obtain.writeString(locale.getVariant());
        obtain.writeString("");
        String parseResult = parseResult(13, obtain, list);
        obtain.reclaim();
        return parseResult;
    }

    private String parseResult(int i, MessageParcel messageParcel, List<GeoAddress> list) {
        String str;
        if (this.mRemoteObject == null) {
            HiLog.error(LABEL, "can not remote to locator sa", new Object[0]);
            return NO_RESULT;
        }
        MessageParcel obtain = MessageParcel.obtain();
        try {
            this.mRemoteObject.sendRequest(i, messageParcel, obtain, new MessageOption());
            int readInt = obtain.readInt();
            str = obtain.readString();
            if (readInt != 0) {
                HiLog.error(LABEL, "have no result, cause some excepiton happened in lower service.", new Object[0]);
            } else {
                int readInt2 = obtain.readInt();
                if (readInt2 > 10) {
                    readInt2 = 10;
                }
                for (int i2 = 0; i2 < readInt2; i2++) {
                    GeoAddress geoAddress = new GeoAddress(Locale.getDefault());
                    obtain.readSequenceable(geoAddress);
                    list.add(geoAddress);
                }
            }
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "isGeoAvailable: got RemoteException", new Object[0]);
            str = NO_RESULT;
        }
        obtain.reclaim();
        return str;
    }
}

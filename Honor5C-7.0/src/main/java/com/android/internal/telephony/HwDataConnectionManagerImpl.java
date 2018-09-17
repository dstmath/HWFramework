package com.android.internal.telephony;

import android.content.Context;
import android.database.DatabaseUtils;
import android.net.IConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.net.TrafficStats;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.util.ArraySet;
import com.android.internal.telephony.dataconnection.AbstractDcTrackerBase;
import com.android.internal.telephony.dataconnection.AbstractDcTrackerBase.DcTrackerBaseReference;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.HwDcTrackerBaseReference;
import com.android.internal.telephony.intelligentdataswitch.IntelligentDataSwitch;
import java.util.HashMap;

public class HwDataConnectionManagerImpl implements HwDataConnectionManager {
    private static int CONNECTIVITY_SERVICE_NEED_SET_USER_DATA;
    private static HwDataConnectionManager mInstance;
    private static IntelligentDataSwitch mIntelligentDataSwitch;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwDataConnectionManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwDataConnectionManagerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwDataConnectionManagerImpl.<clinit>():void");
    }

    public DcTrackerBaseReference createHwDcTrackerBaseReference(AbstractDcTrackerBase dcTrackerBase) {
        return new HwDcTrackerBaseReference((DcTracker) dcTrackerBase);
    }

    public static HwDataConnectionManager getDefault() {
        return mInstance;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean needSetUserDataEnabled(boolean enabled) {
        boolean z = true;
        IConnectivityManager cm = Stub.asInterface(ServiceManager.getService("connectivity"));
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            IBinder connectivityServiceBinder = cm.asBinder();
            if (connectivityServiceBinder != null) {
                int i;
                data.writeInterfaceToken("android.net.IConnectivityManager");
                if (enabled) {
                    i = 1;
                } else {
                    i = 0;
                }
                data.writeInt(i);
                connectivityServiceBinder.transact(CONNECTIVITY_SERVICE_NEED_SET_USER_DATA + 1, data, reply, 0);
            }
            DatabaseUtils.readExceptionFromParcel(reply);
            int result = reply.readInt();
            Rlog.d("HwDataConnectionManager", "needSetUserDataEnabled result = " + result);
            if (result != 1) {
                z = false;
            }
            reply.recycle();
            data.recycle();
            return z;
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
            return true;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public void createIntelligentDataSwitch(Context context) {
        if (SystemProperties.getBoolean("ro.hwpp.autodds", false)) {
            mIntelligentDataSwitch = new IntelligentDataSwitch(context);
            if (mIntelligentDataSwitch == null) {
                Rlog.e("HwDataConnectionManager", "mIntelligentDataSwitch start error");
            }
        }
    }

    public long getThisModemMobileTxPackets(HashMap<String, Integer> mIfacePhoneHashMap, int phoneId) {
        long total = 0;
        for (String iface : getThisModemMobileIfaces(mIfacePhoneHashMap, phoneId)) {
            total += TrafficStats.getTxPackets(iface);
        }
        return total;
    }

    public long getThisModemMobileRxPackets(HashMap<String, Integer> mIfacePhoneHashMap, int phoneId) {
        long total = 0;
        for (String iface : getThisModemMobileIfaces(mIfacePhoneHashMap, phoneId)) {
            total += TrafficStats.getRxPackets(iface);
        }
        return total;
    }

    private String[] getThisModemMobileIfaces(HashMap<String, Integer> mIfacePhoneHashMap, int phoneId) {
        ArraySet<String> mobileIfaces = new ArraySet();
        for (String iface : TrafficStats.getMobileIfaces()) {
            if (mIfacePhoneHashMap.get(iface) == null || ((Integer) mIfacePhoneHashMap.get(iface)).equals(Integer.valueOf(phoneId))) {
                mobileIfaces.add(iface);
            }
        }
        return (String[]) mobileIfaces.toArray(new String[mobileIfaces.size()]);
    }
}

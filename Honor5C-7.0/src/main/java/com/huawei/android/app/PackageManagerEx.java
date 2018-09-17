package com.huawei.android.app;

import android.content.pm.IPackageManager;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.util.Singleton;
import java.util.ArrayList;
import java.util.List;

public class PackageManagerEx {
    private static final String IPACKAGE_MANAGER_DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final String TAG = "PackageManagerEx";
    private static final int TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED = 1008;
    private static final int TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP = 1009;
    private static final int TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST = 1007;
    private static final Singleton<IPackageManager> gDefault = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.app.PackageManagerEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.app.PackageManagerEx.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.app.PackageManagerEx.<clinit>():void");
    }

    public static List<String> getPreinstalledApkList() {
        List<String> list = new ArrayList();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST, data, reply, 0);
            reply.readException();
            reply.readStringList(list);
        } catch (Exception e) {
            Log.e(TAG, "failed to getPreinstalledApkList");
        } finally {
            reply.recycle();
            data.recycle();
        }
        return list;
    }

    private static IPackageManager getDefault() {
        return (IPackageManager) gDefault.get();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean checkGmsCoreUninstalled() {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED, data, reply, 0);
            reply.readException();
            res = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to checkGmsCoreUninstalled");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
        return res;
    }

    public static void deleteGmsCoreFromUninstalledDelapp() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP, data, reply, 0);
            reply.readException();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to deleteGmsCoreFromUninstalledDelapp");
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
}

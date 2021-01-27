package android.hsm;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Set;

public class MediaTransactWrapper {
    private static final int COLLECTION_INITIAL_SIZE = 5;
    private static final String TAG = "MediaTransactWrapper";
    private static SoftReference<IBinder> sBinder = null;

    private MediaTransactWrapper() {
    }

    public static void musicPlaying(int uid, int pid) {
        Parcel request = null;
        Parcel reply = null;
        try {
            request = Parcel.obtain();
            reply = Parcel.obtain();
            IBinder binder = getBinder();
            if (binder != null) {
                request.writeInterfaceToken(HsmTransactExt.INTERFACE_DESCRIPTOR);
                request.writeInt(0);
                request.writeInt(uid);
                request.writeInt(pid);
                binder.transact(102, request, reply, 0);
                reply.readException();
                reply.readInt();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "musicPlaying transact catch remote exception");
        } catch (Exception e2) {
            Log.e(TAG, "musicPlaying transact catch exception");
        } catch (Throwable th) {
            recycleParcel(null);
            recycleParcel(null);
            throw th;
        }
        recycleParcel(request);
        recycleParcel(reply);
    }

    public static void musicPausedOrStopped(int uid, int pid) {
        Parcel request = null;
        Parcel reply = null;
        try {
            request = Parcel.obtain();
            reply = Parcel.obtain();
            IBinder binder = getBinder();
            if (binder != null) {
                request.writeInterfaceToken(HsmTransactExt.INTERFACE_DESCRIPTOR);
                request.writeInt(1);
                request.writeInt(uid);
                request.writeInt(pid);
                binder.transact(102, request, reply, 0);
                reply.readException();
                reply.readInt();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "musicPausedOrStopped transact catch remote exception");
        } catch (Exception e2) {
            Log.e(TAG, "musicPausedOrStopped transact catch exception");
        } catch (Throwable th) {
            recycleParcel(null);
            recycleParcel(null);
            throw th;
        }
        recycleParcel(request);
        recycleParcel(reply);
    }

    public static Set<Integer> playingMusicUidSet() {
        String[] uidArray;
        Set<Integer> uidSet = new HashSet<>(5);
        String strUid = playingMusicUidStr();
        if (strUid != null && !strUid.isEmpty()) {
            for (String str : strUid.split("\\|")) {
                uidSet.add(Integer.valueOf(str));
            }
        }
        return uidSet;
    }

    private static String playingMusicUidStr() {
        Parcel request = null;
        Parcel reply = null;
        try {
            request = Parcel.obtain();
            reply = Parcel.obtain();
            IBinder binder = getBinder();
            if (binder != null) {
                request.writeInterfaceToken(HsmTransactExt.INTERFACE_DESCRIPTOR);
                binder.transact(103, request, reply, 0);
                reply.readException();
                String result = reply.readString();
                recycleParcel(request);
                recycleParcel(reply);
                return result;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "playingMusicUidStr transact catch remote exception");
        } catch (Exception e2) {
            Log.e(TAG, "playingMusicUidStr transact catch exception");
        } catch (Throwable th) {
            recycleParcel(null);
            recycleParcel(null);
            throw th;
        }
        recycleParcel(request);
        recycleParcel(reply);
        return null;
    }

    private static synchronized IBinder getBinder() {
        IBinder iBinder;
        synchronized (MediaTransactWrapper.class) {
            if (sBinder == null || sBinder.get() == null) {
                sBinder = new SoftReference<>(ServiceManager.getService(HsmTransactExt.SERVICE_NAME));
            }
            iBinder = sBinder.get();
        }
        return iBinder;
    }

    private static void recycleParcel(Parcel par) {
        if (par != null) {
            par.recycle();
        }
    }
}

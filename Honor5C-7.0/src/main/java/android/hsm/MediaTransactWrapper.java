package android.hsm;

import android.media.MediaFile;
import android.nfc.tech.Ndef;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Set;

public class MediaTransactWrapper {
    private static final boolean DEBUG = false;
    private static final String TAG = "MediaTransactWrapper";
    private static SoftReference<IBinder> mBinder;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hsm.MediaTransactWrapper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hsm.MediaTransactWrapper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hsm.MediaTransactWrapper.<clinit>():void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void musicPlaying(int uid, int pid) {
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            IBinder b = getBinder();
            if (b != null) {
                data.writeInterfaceToken(HsmTransactExt.INTERFACE_DESCRIPTOR);
                data.writeInt(0);
                data.writeInt(uid);
                data.writeInt(pid);
                b.transact(Ndef.TYPE_ICODE_SLI, data, reply, 0);
                reply.readException();
                if (1 == reply.readInt()) {
                }
            }
            recycleParcel(data);
            recycleParcel(reply);
        } catch (RemoteException e) {
            Log.e(TAG, "musicPlaying transact catch remote exception: " + e.toString());
        } catch (Exception e2) {
            Log.e(TAG, "musicPlaying transact catch exception: " + e2.toString());
            e2.printStackTrace();
        } catch (Throwable th) {
            recycleParcel(null);
            recycleParcel(null);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void musicPausedOrStopped(int uid, int pid) {
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            IBinder b = getBinder();
            if (b != null) {
                data.writeInterfaceToken(HsmTransactExt.INTERFACE_DESCRIPTOR);
                data.writeInt(1);
                data.writeInt(uid);
                data.writeInt(pid);
                b.transact(Ndef.TYPE_ICODE_SLI, data, reply, 0);
                reply.readException();
                if (1 == reply.readInt()) {
                }
            }
            recycleParcel(data);
            recycleParcel(reply);
        } catch (RemoteException e) {
            Log.e(TAG, "musicPausedOrStopped transact catch remote exception: " + e.toString());
        } catch (Exception e2) {
            Log.e(TAG, "musicPausedOrStopped transact catch exception: " + e2.toString());
            e2.printStackTrace();
        } catch (Throwable th) {
            recycleParcel(null);
            recycleParcel(null);
        }
    }

    public static Set<Integer> playingMusicUidSet() {
        Set<Integer> result = new HashSet();
        String strUids = playingMusicUidStr();
        if (!(strUids == null || strUids.isEmpty())) {
            String[] ids = strUids.split("\\|");
            if (ids != null) {
                for (String valueOf : ids) {
                    result.add(Integer.valueOf(valueOf));
                }
            }
        }
        return result;
    }

    private static String playingMusicUidStr() {
        Parcel data = null;
        Parcel reply = null;
        try {
            data = Parcel.obtain();
            reply = Parcel.obtain();
            IBinder b = getBinder();
            if (b != null) {
                data.writeInterfaceToken(HsmTransactExt.INTERFACE_DESCRIPTOR);
                b.transact(MediaFile.FILE_TYPE_XML, data, reply, 0);
                reply.readException();
                String result = reply.readString();
                return result;
            }
            recycleParcel(data);
            recycleParcel(reply);
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "musicPausedOrStopped transact catch remote exception: " + e.toString());
        } catch (Exception e2) {
            Log.e(TAG, "musicPausedOrStopped transact catch exception: " + e2.toString());
            e2.printStackTrace();
        } finally {
            recycleParcel(data);
            recycleParcel(reply);
        }
    }

    private static synchronized IBinder getBinder() {
        IBinder iBinder;
        synchronized (MediaTransactWrapper.class) {
            if (mBinder == null || mBinder.get() == null) {
                mBinder = new SoftReference(ServiceManager.getService(HsmTransactExt.SERVICE_NAME));
            }
            iBinder = (IBinder) mBinder.get();
        }
        return iBinder;
    }

    private static void recycleParcel(Parcel p) {
        if (p != null) {
            p.recycle();
        }
    }
}

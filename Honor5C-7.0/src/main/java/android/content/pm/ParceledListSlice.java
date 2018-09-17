package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.ClassLoaderCreator;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParceledListSlice<T extends Parcelable> implements Parcelable {
    public static final ClassLoaderCreator<ParceledListSlice> CREATOR = null;
    private static boolean DEBUG = false;
    private static final int MAX_IPC_SIZE = 65536;
    private static String TAG;
    private final List<T> mList;

    /* renamed from: android.content.pm.ParceledListSlice.2 */
    class AnonymousClass2 extends Binder {
        final /* synthetic */ int val$N;
        final /* synthetic */ int val$callFlags;
        final /* synthetic */ Class val$listElementClass;

        AnonymousClass2(int val$N, Class val$listElementClass, int val$callFlags) {
            this.val$N = val$N;
            this.val$listElementClass = val$listElementClass;
            this.val$callFlags = val$callFlags;
        }

        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1) {
                return super.onTransact(code, data, reply, flags);
            }
            int i = data.readInt();
            if (ParceledListSlice.DEBUG) {
                Log.d(ParceledListSlice.TAG, "Writing more @" + i + " of " + this.val$N);
            }
            while (i < this.val$N && reply.dataSize() < ParceledListSlice.MAX_IPC_SIZE) {
                reply.writeInt(1);
                Parcelable parcelable = (Parcelable) ParceledListSlice.this.mList.get(i);
                ParceledListSlice.verifySameType(this.val$listElementClass, parcelable.getClass());
                parcelable.writeToParcel(reply, this.val$callFlags);
                if (ParceledListSlice.DEBUG) {
                    Log.d(ParceledListSlice.TAG, "Wrote extra #" + i + ": " + ParceledListSlice.this.mList.get(i));
                }
                i++;
            }
            if (i < this.val$N) {
                if (ParceledListSlice.DEBUG) {
                    Log.d(ParceledListSlice.TAG, "Breaking @" + i + " of " + this.val$N);
                }
                reply.writeInt(0);
            }
            return true;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.ParceledListSlice.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.ParceledListSlice.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.ParceledListSlice.<clinit>():void");
    }

    public static <T extends Parcelable> ParceledListSlice<T> emptyList() {
        return new ParceledListSlice(Collections.emptyList());
    }

    public ParceledListSlice(List<T> list) {
        this.mList = list;
    }

    private ParceledListSlice(Parcel p, ClassLoader loader) {
        int N = p.readInt();
        this.mList = new ArrayList(N);
        if (DEBUG) {
            Log.d(TAG, "Retrieving " + N + " items");
        }
        if (N > 0) {
            T parcelable;
            Creator<?> creator = p.readParcelableCreator(loader);
            Class listElementClass = null;
            int i = 0;
            while (i < N && p.readInt() != 0) {
                parcelable = p.readCreator(creator, loader);
                if (listElementClass == null) {
                    listElementClass = parcelable.getClass();
                } else {
                    verifySameType(listElementClass, parcelable.getClass());
                }
                this.mList.add(parcelable);
                if (DEBUG) {
                    Log.d(TAG, "Read inline #" + i + ": " + this.mList.get(this.mList.size() - 1));
                }
                i++;
            }
            if (i < N) {
                IBinder retriever = p.readStrongBinder();
                while (i < N) {
                    if (DEBUG) {
                        Log.d(TAG, "Reading more @" + i + " of " + N + ": retriever=" + retriever);
                    }
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    data.writeInt(i);
                    try {
                        retriever.transact(1, data, reply, 0);
                        while (i < N && reply.readInt() != 0) {
                            parcelable = reply.readCreator(creator, loader);
                            verifySameType(listElementClass, parcelable.getClass());
                            this.mList.add(parcelable);
                            if (DEBUG) {
                                Log.d(TAG, "Read extra #" + i + ": " + this.mList.get(this.mList.size() - 1));
                            }
                            i++;
                        }
                        reply.recycle();
                        data.recycle();
                    } catch (RemoteException e) {
                        Log.w(TAG, "Failure retrieving array; only received " + i + " of " + N, e);
                        return;
                    }
                }
            }
        }
    }

    private static void verifySameType(Class<?> expected, Class<?> actual) {
        if (!actual.equals(expected)) {
            throw new IllegalArgumentException("Can't unparcel type " + actual.getName() + " in list of type " + expected.getName());
        }
    }

    public List<T> getList() {
        return this.mList;
    }

    public int describeContents() {
        int contents = 0;
        for (int i = 0; i < this.mList.size(); i++) {
            contents |= ((Parcelable) this.mList.get(i)).describeContents();
        }
        return contents;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int N = this.mList.size();
        int callFlags = flags;
        dest.writeInt(N);
        if (DEBUG) {
            Log.d(TAG, "Writing " + N + " items");
        }
        if (N > 0) {
            Class<?> listElementClass = ((Parcelable) this.mList.get(0)).getClass();
            dest.writeParcelableCreator((Parcelable) this.mList.get(0));
            int i = 0;
            while (i < N && dest.dataSize() < MAX_IPC_SIZE) {
                dest.writeInt(1);
                Parcelable parcelable = (Parcelable) this.mList.get(i);
                verifySameType(listElementClass, parcelable.getClass());
                parcelable.writeToParcel(dest, flags);
                if (DEBUG) {
                    Log.d(TAG, "Wrote inline #" + i + ": " + this.mList.get(i));
                }
                i++;
            }
            if (i < N) {
                dest.writeInt(0);
                Binder retriever = new AnonymousClass2(N, listElementClass, flags);
                if (DEBUG) {
                    Log.d(TAG, "Breaking @" + i + " of " + N + ": retriever=" + retriever);
                }
                dest.writeStrongBinder(retriever);
            }
        }
    }
}

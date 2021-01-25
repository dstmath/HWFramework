package android.content.pm;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public abstract class BaseParceledListSlice<T> implements Parcelable {
    private static boolean DEBUG = false;
    private static final int MAX_IPC_SIZE = 65536;
    private static String TAG = "ParceledListSlice";
    private int mInlineCountLimit = Integer.MAX_VALUE;
    private final List<T> mList;

    /* access modifiers changed from: protected */
    public abstract Parcelable.Creator<?> readParcelableCreator(Parcel parcel, ClassLoader classLoader);

    /* access modifiers changed from: protected */
    public abstract void writeElement(T t, Parcel parcel, int i);

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public abstract void writeParcelableCreator(T t, Parcel parcel);

    public BaseParceledListSlice(List<T> list) {
        this.mList = list;
    }

    BaseParceledListSlice(Parcel p, ClassLoader loader) {
        String str;
        String str2;
        int N = p.readInt();
        this.mList = new ArrayList(N);
        if (DEBUG) {
            Log.d(TAG, "Retrieving " + N + " items");
        }
        if (N > 0) {
            Parcelable.Creator<?> creator = readParcelableCreator(p, loader);
            int i = 0;
            Class<?> listElementClass = null;
            while (true) {
                str = ": ";
                if (i >= N) {
                    break;
                } else if (p.readInt() == 0) {
                    break;
                } else {
                    T parcelable = readCreator(creator, p, loader);
                    if (listElementClass == null) {
                        listElementClass = parcelable.getClass();
                    } else {
                        verifySameType(listElementClass, parcelable.getClass());
                    }
                    this.mList.add(parcelable);
                    if (DEBUG) {
                        String str3 = TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("Read inline #");
                        sb.append(i);
                        sb.append(str);
                        List<T> list = this.mList;
                        sb.append((Object) list.get(list.size() - 1));
                        Log.d(str3, sb.toString());
                    }
                    i++;
                }
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
                            T parcelable2 = readCreator(creator, reply, loader);
                            verifySameType(listElementClass, parcelable2.getClass());
                            this.mList.add(parcelable2);
                            if (DEBUG) {
                                String str4 = TAG;
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append("Read extra #");
                                sb2.append(i);
                                sb2.append(str);
                                List<T> list2 = this.mList;
                                str2 = str;
                                sb2.append((Object) list2.get(list2.size() - 1));
                                Log.d(str4, sb2.toString());
                            } else {
                                str2 = str;
                            }
                            i++;
                            str = str2;
                        }
                        reply.recycle();
                        data.recycle();
                        str = str;
                    } catch (RemoteException e) {
                        Log.w(TAG, "Failure retrieving array; only received " + i + " of " + N, e);
                        return;
                    }
                }
            }
        }
    }

    private T readCreator(Parcelable.Creator<?> creator, Parcel p, ClassLoader loader) {
        return creator instanceof Parcelable.ClassLoaderCreator ? (T) ((Parcelable.ClassLoaderCreator) creator).createFromParcel(p, loader) : (T) creator.createFromParcel(p);
    }

    /* access modifiers changed from: private */
    public static void verifySameType(Class<?> expected, Class<?> actual) {
        if (!actual.equals(expected)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Can't unparcel type ");
            sb.append(actual.getName());
            sb.append(" in list of type ");
            sb.append(expected == null ? null : expected.getName());
            throw new IllegalArgumentException(sb.toString());
        }
    }

    @UnsupportedAppUsage
    public List<T> getList() {
        return this.mList;
    }

    public void setInlineCountLimit(int maxCount) {
        this.mInlineCountLimit = maxCount;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, final int flags) {
        final int N = this.mList.size();
        dest.writeInt(N);
        if (DEBUG) {
            String str = TAG;
            Log.d(str, "Writing " + N + " items");
        }
        if (N > 0) {
            final Class<?> listElementClass = this.mList.get(0).getClass();
            writeParcelableCreator(this.mList.get(0), dest);
            int i = 0;
            while (i < N && i < this.mInlineCountLimit && dest.dataSize() < 65536) {
                dest.writeInt(1);
                T parcelable = this.mList.get(i);
                verifySameType(listElementClass, parcelable.getClass());
                writeElement(parcelable, dest, flags);
                if (DEBUG) {
                    String str2 = TAG;
                    Log.d(str2, "Wrote inline #" + i + ": " + ((Object) this.mList.get(i)));
                }
                i++;
            }
            if (i < N) {
                dest.writeInt(0);
                Binder retriever = new Binder() {
                    /* class android.content.pm.BaseParceledListSlice.AnonymousClass1 */

                    /* JADX DEBUG: Multi-variable search result rejected for r4v3, resolved type: android.content.pm.BaseParceledListSlice */
                    /* JADX WARN: Multi-variable type inference failed */
                    /* access modifiers changed from: protected */
                    @Override // android.os.Binder
                    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                        if (code != 1) {
                            return super.onTransact(code, data, reply, flags);
                        }
                        int i = data.readInt();
                        if (BaseParceledListSlice.DEBUG) {
                            String str = BaseParceledListSlice.TAG;
                            Log.d(str, "Writing more @" + i + " of " + N);
                        }
                        while (i < N && reply.dataSize() < 65536) {
                            reply.writeInt(1);
                            Object obj = BaseParceledListSlice.this.mList.get(i);
                            BaseParceledListSlice.verifySameType(listElementClass, obj.getClass());
                            BaseParceledListSlice.this.writeElement(obj, reply, flags);
                            if (BaseParceledListSlice.DEBUG) {
                                String str2 = BaseParceledListSlice.TAG;
                                Log.d(str2, "Wrote extra #" + i + ": " + BaseParceledListSlice.this.mList.get(i));
                            }
                            i++;
                        }
                        if (i < N) {
                            if (BaseParceledListSlice.DEBUG) {
                                String str3 = BaseParceledListSlice.TAG;
                                Log.d(str3, "Breaking @" + i + " of " + N);
                            }
                            reply.writeInt(0);
                        }
                        return true;
                    }
                };
                if (DEBUG) {
                    String str3 = TAG;
                    Log.d(str3, "Breaking @" + i + " of " + N + ": retriever=" + retriever);
                }
                dest.writeStrongBinder(retriever);
            }
        }
    }
}

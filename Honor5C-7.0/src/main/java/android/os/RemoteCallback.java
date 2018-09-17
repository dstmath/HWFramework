package android.os;

import android.os.IRemoteCallback.Stub;
import android.os.Parcelable.Creator;

public final class RemoteCallback implements Parcelable {
    public static final Creator<RemoteCallback> CREATOR = null;
    private final IRemoteCallback mCallback;
    private final Handler mHandler;
    private final OnResultListener mListener;

    public interface OnResultListener {
        void onResult(Bundle bundle);
    }

    /* renamed from: android.os.RemoteCallback.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ Bundle val$result;

        AnonymousClass3(Bundle val$result) {
            this.val$result = val$result;
        }

        public void run() {
            RemoteCallback.this.mListener.onResult(this.val$result);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.RemoteCallback.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.RemoteCallback.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.RemoteCallback.<clinit>():void");
    }

    public RemoteCallback(OnResultListener listener) {
        this(listener, null);
    }

    public RemoteCallback(OnResultListener listener, Handler handler) {
        if (listener == null) {
            throw new NullPointerException("listener cannot be null");
        }
        this.mListener = listener;
        this.mHandler = handler;
        this.mCallback = new Stub() {
            public void sendResult(Bundle data) {
                RemoteCallback.this.sendResult(data);
            }
        };
    }

    RemoteCallback(Parcel parcel) {
        this.mListener = null;
        this.mHandler = null;
        this.mCallback = Stub.asInterface(parcel.readStrongBinder());
    }

    public void sendResult(Bundle result) {
        if (this.mListener == null) {
            try {
                this.mCallback.sendResult(result);
            } catch (RemoteException e) {
            }
        } else if (this.mHandler != null) {
            this.mHandler.post(new AnonymousClass3(result));
        } else {
            this.mListener.onResult(result);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeStrongBinder(this.mCallback.asBinder());
    }
}

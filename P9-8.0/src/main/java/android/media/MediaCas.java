package android.media;

import android.media.IMediaCasService.Stub;
import android.media.MediaCasException.UnsupportedCasException;
import android.net.ProxyInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.util.EventLog;
import android.util.Log;
import android.util.Singleton;

public final class MediaCas implements AutoCloseable {
    private static final String TAG = "MediaCas";
    private static final Singleton<IMediaCasService> gDefault = new Singleton<IMediaCasService>() {
        protected IMediaCasService create() {
            return Stub.asInterface(ServiceManager.getService("media.cas"));
        }
    };
    private final ICasListener.Stub mBinder = new ICasListener.Stub() {
        public void onEvent(int event, int arg, byte[] data) throws RemoteException {
            MediaCas.this.mEventHandler.sendMessage(MediaCas.this.mEventHandler.obtainMessage(0, event, arg, data));
        }
    };
    private final ParcelableCasData mCasData = new ParcelableCasData();
    private EventHandler mEventHandler;
    private HandlerThread mHandlerThread;
    private ICas mICas;
    private EventListener mListener;

    private class EventHandler extends Handler {
        private static final int MSG_CAS_EVENT = 0;

        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                MediaCas.this.mListener.onEvent(MediaCas.this, msg.arg1, msg.arg2, (byte[]) msg.obj);
            }
        }
    }

    public interface EventListener {
        void onEvent(MediaCas mediaCas, int i, int i2, byte[] bArr);
    }

    static class ParcelableCasData implements Parcelable {
        public static final Creator<ParcelableCasData> CREATOR = new Creator<ParcelableCasData>() {
            public ParcelableCasData createFromParcel(Parcel in) {
                return new ParcelableCasData(in, null);
            }

            public ParcelableCasData[] newArray(int size) {
                return new ParcelableCasData[size];
            }
        };
        private byte[] mData;
        private int mLength;
        private int mOffset;

        /* synthetic */ ParcelableCasData(Parcel in, ParcelableCasData -this1) {
            this(in);
        }

        ParcelableCasData() {
            this.mData = null;
            this.mLength = 0;
            this.mOffset = 0;
        }

        private ParcelableCasData(Parcel in) {
            int i = 0;
            EventLog.writeEvent(1397638484, new Object[]{"b/73085795", Integer.valueOf(-1), ProxyInfo.LOCAL_EXCL_LIST});
            this.mData = in.createByteArray();
            this.mOffset = 0;
            if (this.mData != null) {
                i = this.mData.length;
            }
            this.mLength = i;
        }

        void set(byte[] data, int offset, int length) {
            this.mData = data;
            this.mOffset = offset;
            this.mLength = length;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByteArray(this.mData, this.mOffset, this.mLength);
        }
    }

    public static class PluginDescriptor {
        private final int mCASystemId;
        private final String mName;

        private PluginDescriptor() {
            this.mCASystemId = 65535;
            this.mName = null;
        }

        PluginDescriptor(int CA_system_id, String name) {
            this.mCASystemId = CA_system_id;
            this.mName = name;
        }

        public int getSystemId() {
            return this.mCASystemId;
        }

        public String getName() {
            return this.mName;
        }

        public String toString() {
            return "PluginDescriptor {" + this.mCASystemId + ", " + this.mName + "}";
        }
    }

    static class ParcelableCasPluginDescriptor extends PluginDescriptor implements Parcelable {
        public static final Creator<ParcelableCasPluginDescriptor> CREATOR = new Creator<ParcelableCasPluginDescriptor>() {
            public ParcelableCasPluginDescriptor createFromParcel(Parcel in) {
                return new ParcelableCasPluginDescriptor(in.readInt(), in.readString(), null);
            }

            public ParcelableCasPluginDescriptor[] newArray(int size) {
                return new ParcelableCasPluginDescriptor[size];
            }
        };

        /* synthetic */ ParcelableCasPluginDescriptor(int CA_system_id, String name, ParcelableCasPluginDescriptor -this2) {
            this(CA_system_id, name);
        }

        private ParcelableCasPluginDescriptor(int CA_system_id, String name) {
            super(CA_system_id, name);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            Log.w(MediaCas.TAG, "ParcelableCasPluginDescriptor.writeToParcel shouldn't be called!");
        }
    }

    public final class Session implements AutoCloseable {
        final byte[] mSessionId;

        Session(byte[] sessionId) {
            this.mSessionId = sessionId;
        }

        public void setPrivateData(byte[] data) throws MediaCasException {
            MediaCas.this.validateInternalStates();
            try {
                MediaCas.this.mICas.setSessionPrivateData(this.mSessionId, data);
            } catch (ServiceSpecificException e) {
                MediaCasException.throwExceptions(e);
            } catch (RemoteException e2) {
                MediaCas.this.cleanupAndRethrowIllegalState();
            }
        }

        public void processEcm(byte[] data, int offset, int length) throws MediaCasException {
            MediaCas.this.validateInternalStates();
            try {
                MediaCas.this.mCasData.set(data, offset, length);
                MediaCas.this.mICas.processEcm(this.mSessionId, MediaCas.this.mCasData);
            } catch (ServiceSpecificException e) {
                MediaCasException.throwExceptions(e);
            } catch (RemoteException e2) {
                MediaCas.this.cleanupAndRethrowIllegalState();
            }
        }

        public void processEcm(byte[] data) throws MediaCasException {
            processEcm(data, 0, data.length);
        }

        public void close() {
            MediaCas.this.validateInternalStates();
            try {
                MediaCas.this.mICas.closeSession(this.mSessionId);
            } catch (ServiceSpecificException e) {
                MediaCasStateException.throwExceptions(e);
            } catch (RemoteException e2) {
                MediaCas.this.cleanupAndRethrowIllegalState();
            }
        }
    }

    static IMediaCasService getService() {
        return (IMediaCasService) gDefault.get();
    }

    private void validateInternalStates() {
        if (this.mICas == null) {
            throw new IllegalStateException();
        }
    }

    private void cleanupAndRethrowIllegalState() {
        this.mICas = null;
        throw new IllegalStateException();
    }

    Session createFromSessionId(byte[] sessionId) {
        if (sessionId == null || sessionId.length == 0) {
            return null;
        }
        return new Session(sessionId);
    }

    public static boolean isSystemIdSupported(int CA_system_id) {
        IMediaCasService service = getService();
        if (service != null) {
            try {
                return service.isSystemIdSupported(CA_system_id);
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    public static PluginDescriptor[] enumeratePlugins() {
        IMediaCasService service = getService();
        if (service != null) {
            try {
                ParcelableCasPluginDescriptor[] descriptors = service.enumeratePlugins();
                if (descriptors.length == 0) {
                    return null;
                }
                PluginDescriptor[] results = new PluginDescriptor[descriptors.length];
                for (int i = 0; i < results.length; i++) {
                    results[i] = descriptors[i];
                }
                return results;
            } catch (RemoteException e) {
            }
        }
        return null;
    }

    public MediaCas(int CA_system_id) throws UnsupportedCasException {
        try {
            this.mICas = getService().createPlugin(CA_system_id, this.mBinder);
            if (this.mICas == null) {
                throw new UnsupportedCasException("Unsupported CA_system_id " + CA_system_id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create plugin: " + e);
            this.mICas = null;
            if (this.mICas == null) {
                throw new UnsupportedCasException("Unsupported CA_system_id " + CA_system_id);
            }
        } catch (Throwable th) {
            if (this.mICas == null) {
                UnsupportedCasException unsupportedCasException = new UnsupportedCasException("Unsupported CA_system_id " + CA_system_id);
            }
        }
    }

    IBinder getBinder() {
        validateInternalStates();
        return this.mICas.asBinder();
    }

    public void setEventListener(EventListener listener, Handler handler) {
        this.mListener = listener;
        if (this.mListener == null) {
            this.mEventHandler = null;
            return;
        }
        Looper looper = handler != null ? handler.getLooper() : null;
        if (looper == null) {
            looper = Looper.myLooper();
            if (looper == null) {
                looper = Looper.getMainLooper();
                if (looper == null) {
                    if (this.mHandlerThread == null || (this.mHandlerThread.isAlive() ^ 1) != 0) {
                        this.mHandlerThread = new HandlerThread("MediaCasEventThread", -2);
                        this.mHandlerThread.start();
                    }
                    looper = this.mHandlerThread.getLooper();
                }
            }
        }
        this.mEventHandler = new EventHandler(looper);
    }

    public void setPrivateData(byte[] data) throws MediaCasException {
        validateInternalStates();
        try {
            this.mICas.setPrivateData(data);
        } catch (ServiceSpecificException e) {
            MediaCasException.throwExceptions(e);
        } catch (RemoteException e2) {
            cleanupAndRethrowIllegalState();
        }
    }

    public Session openSession() throws MediaCasException {
        validateInternalStates();
        try {
            return createFromSessionId(this.mICas.openSession());
        } catch (ServiceSpecificException e) {
            MediaCasException.throwExceptions(e);
        } catch (RemoteException e2) {
            cleanupAndRethrowIllegalState();
        }
        return null;
    }

    public void processEmm(byte[] data, int offset, int length) throws MediaCasException {
        validateInternalStates();
        try {
            this.mCasData.set(data, offset, length);
            this.mICas.processEmm(this.mCasData);
        } catch (ServiceSpecificException e) {
            MediaCasException.throwExceptions(e);
        } catch (RemoteException e2) {
            cleanupAndRethrowIllegalState();
        }
    }

    public void processEmm(byte[] data) throws MediaCasException {
        processEmm(data, 0, data.length);
    }

    public void sendEvent(int event, int arg, byte[] data) throws MediaCasException {
        validateInternalStates();
        try {
            this.mICas.sendEvent(event, arg, data);
        } catch (ServiceSpecificException e) {
            MediaCasException.throwExceptions(e);
        } catch (RemoteException e2) {
            cleanupAndRethrowIllegalState();
        }
    }

    public void provision(String provisionString) throws MediaCasException {
        validateInternalStates();
        try {
            this.mICas.provision(provisionString);
        } catch (ServiceSpecificException e) {
            MediaCasException.throwExceptions(e);
        } catch (RemoteException e2) {
            cleanupAndRethrowIllegalState();
        }
    }

    public void refreshEntitlements(int refreshType, byte[] refreshData) throws MediaCasException {
        validateInternalStates();
        try {
            this.mICas.refreshEntitlements(refreshType, refreshData);
        } catch (ServiceSpecificException e) {
            MediaCasException.throwExceptions(e);
        } catch (RemoteException e2) {
            cleanupAndRethrowIllegalState();
        }
    }

    public void close() {
        if (this.mICas != null) {
            try {
                this.mICas.release();
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mICas = null;
            }
            this.mICas = null;
        }
    }

    protected void finalize() {
        close();
    }
}

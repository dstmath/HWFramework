package android.media;

import android.hardware.cas.V1_0.HidlCasPluginDescriptor;
import android.hardware.cas.V1_0.ICas;
import android.hardware.cas.V1_0.IMediaCasService;
import android.hardware.cas.V1_1.ICasListener;
import android.media.MediaCasException;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Singleton;
import java.util.ArrayList;

public final class MediaCas implements AutoCloseable {
    private static final String TAG = "MediaCas";
    private static final Singleton<IMediaCasService> sService = new Singleton<IMediaCasService>() {
        /* class android.media.MediaCas.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IMediaCasService create() {
            try {
                Log.d(MediaCas.TAG, "Tried to get cas@1.1 service");
                android.hardware.cas.V1_1.IMediaCasService serviceV11 = android.hardware.cas.V1_1.IMediaCasService.getService(true);
                if (serviceV11 != null) {
                    return serviceV11;
                }
                return null;
            } catch (Exception e) {
                try {
                    Log.d(MediaCas.TAG, "Tried to get cas@1.0 service");
                    return IMediaCasService.getService(true);
                } catch (Exception e2) {
                    Log.d(MediaCas.TAG, "Failed to get cas@1.0 service");
                    return null;
                }
            }
        }
    };
    private final ICasListener.Stub mBinder = new ICasListener.Stub() {
        /* class android.media.MediaCas.AnonymousClass2 */

        @Override // android.hardware.cas.V1_0.ICasListener
        public void onEvent(int event, int arg, ArrayList<Byte> data) throws RemoteException {
            MediaCas.this.mEventHandler.sendMessage(MediaCas.this.mEventHandler.obtainMessage(0, event, arg, data));
        }

        @Override // android.hardware.cas.V1_1.ICasListener
        public void onSessionEvent(ArrayList<Byte> sessionId, int event, int arg, ArrayList<Byte> data) throws RemoteException {
            Message msg = MediaCas.this.mEventHandler.obtainMessage();
            msg.what = 1;
            msg.arg1 = event;
            msg.arg2 = arg;
            Bundle bundle = new Bundle();
            bundle.putByteArray(TextToSpeech.Engine.KEY_PARAM_SESSION_ID, MediaCas.this.toBytes(sessionId));
            bundle.putByteArray("data", MediaCas.this.toBytes(data));
            msg.setData(bundle);
            MediaCas.this.mEventHandler.sendMessage(msg);
        }
    };
    private EventHandler mEventHandler;
    private HandlerThread mHandlerThread;
    private ICas mICas;
    private android.hardware.cas.V1_1.ICas mICasV11;
    private EventListener mListener;

    static IMediaCasService getService() {
        return sService.get();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void validateInternalStates() {
        if (this.mICas == null) {
            throw new IllegalStateException();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanupAndRethrowIllegalState() {
        this.mICas = null;
        this.mICasV11 = null;
        throw new IllegalStateException();
    }

    /* access modifiers changed from: private */
    public class EventHandler extends Handler {
        private static final String DATA_KEY = "data";
        private static final int MSG_CAS_EVENT = 0;
        private static final int MSG_CAS_SESSION_EVENT = 1;
        private static final String SESSION_KEY = "sessionId";

        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                MediaCas.this.mListener.onEvent(MediaCas.this, msg.arg1, msg.arg2, MediaCas.this.toBytes((ArrayList) msg.obj));
            } else if (msg.what == 1) {
                Bundle bundle = msg.getData();
                ArrayList<Byte> sessionId = MediaCas.this.toByteArray(bundle.getByteArray("sessionId"));
                EventListener eventListener = MediaCas.this.mListener;
                MediaCas mediaCas = MediaCas.this;
                eventListener.onSessionEvent(mediaCas, mediaCas.createFromSessionId(sessionId), msg.arg1, msg.arg2, bundle.getByteArray("data"));
            }
        }
    }

    public static class PluginDescriptor {
        private final int mCASystemId;
        private final String mName;

        private PluginDescriptor() {
            this.mCASystemId = 65535;
            this.mName = null;
        }

        PluginDescriptor(HidlCasPluginDescriptor descriptor) {
            this.mCASystemId = descriptor.caSystemId;
            this.mName = descriptor.name;
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ArrayList<Byte> toByteArray(byte[] data, int offset, int length) {
        ArrayList<Byte> byteArray = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            byteArray.add(Byte.valueOf(data[offset + i]));
        }
        return byteArray;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ArrayList<Byte> toByteArray(byte[] data) {
        if (data == null) {
            return new ArrayList<>();
        }
        return toByteArray(data, 0, data.length);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private byte[] toBytes(ArrayList<Byte> byteArray) {
        byte[] data = null;
        if (byteArray != null) {
            data = new byte[byteArray.size()];
            for (int i = 0; i < data.length; i++) {
                data[i] = byteArray.get(i).byteValue();
            }
        }
        return data;
    }

    public final class Session implements AutoCloseable {
        final ArrayList<Byte> mSessionId;

        Session(ArrayList<Byte> sessionId) {
            this.mSessionId = sessionId;
        }

        public boolean equals(Object obj) {
            if (obj instanceof Session) {
                return this.mSessionId.equals(((Session) obj).mSessionId);
            }
            return false;
        }

        public void setPrivateData(byte[] data) throws MediaCasException {
            MediaCas.this.validateInternalStates();
            try {
                MediaCasException.throwExceptionIfNeeded(MediaCas.this.mICas.setSessionPrivateData(this.mSessionId, MediaCas.this.toByteArray(data, 0, data.length)));
            } catch (RemoteException e) {
                MediaCas.this.cleanupAndRethrowIllegalState();
            }
        }

        public void processEcm(byte[] data, int offset, int length) throws MediaCasException {
            MediaCas.this.validateInternalStates();
            try {
                MediaCasException.throwExceptionIfNeeded(MediaCas.this.mICas.processEcm(this.mSessionId, MediaCas.this.toByteArray(data, offset, length)));
            } catch (RemoteException e) {
                MediaCas.this.cleanupAndRethrowIllegalState();
            }
        }

        public void processEcm(byte[] data) throws MediaCasException {
            processEcm(data, 0, data.length);
        }

        public void sendSessionEvent(int event, int arg, byte[] data) throws MediaCasException {
            MediaCas.this.validateInternalStates();
            if (MediaCas.this.mICasV11 != null) {
                try {
                    MediaCasException.throwExceptionIfNeeded(MediaCas.this.mICasV11.sendSessionEvent(this.mSessionId, event, arg, MediaCas.this.toByteArray(data)));
                } catch (RemoteException e) {
                    MediaCas.this.cleanupAndRethrowIllegalState();
                }
            } else {
                Log.d(MediaCas.TAG, "Send Session Event isn't supported by cas@1.0 interface");
                throw new MediaCasException.UnsupportedCasException("Send Session Event is not supported");
            }
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            MediaCas.this.validateInternalStates();
            try {
                MediaCasStateException.throwExceptionIfNeeded(MediaCas.this.mICas.closeSession(this.mSessionId));
            } catch (RemoteException e) {
                MediaCas.this.cleanupAndRethrowIllegalState();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Session createFromSessionId(ArrayList<Byte> sessionId) {
        if (sessionId == null || sessionId.size() == 0) {
            return null;
        }
        return new Session(sessionId);
    }

    public static boolean isSystemIdSupported(int CA_system_id) {
        IMediaCasService service = getService();
        if (service == null) {
            return false;
        }
        try {
            return service.isSystemIdSupported(CA_system_id);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static PluginDescriptor[] enumeratePlugins() {
        IMediaCasService service = getService();
        if (service != null) {
            try {
                ArrayList<HidlCasPluginDescriptor> descriptors = service.enumeratePlugins();
                if (descriptors.size() == 0) {
                    return null;
                }
                PluginDescriptor[] results = new PluginDescriptor[descriptors.size()];
                for (int i = 0; i < results.length; i++) {
                    results[i] = new PluginDescriptor(descriptors.get(i));
                }
                return results;
            } catch (RemoteException e) {
            }
        }
        return null;
    }

    public MediaCas(int CA_system_id) throws MediaCasException.UnsupportedCasException {
        try {
            IMediaCasService service = getService();
            android.hardware.cas.V1_1.IMediaCasService serviceV11 = android.hardware.cas.V1_1.IMediaCasService.castFrom((IHwInterface) service);
            if (serviceV11 == null) {
                Log.d(TAG, "Used cas@1_0 interface to create plugin");
                this.mICas = service.createPlugin(CA_system_id, this.mBinder);
            } else {
                Log.d(TAG, "Used cas@1.1 interface to create plugin");
                android.hardware.cas.V1_1.ICas createPluginExt = serviceV11.createPluginExt(CA_system_id, this.mBinder);
                this.mICasV11 = createPluginExt;
                this.mICas = createPluginExt;
            }
            if (this.mICas == null) {
                throw new MediaCasException.UnsupportedCasException("Unsupported CA_system_id " + CA_system_id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create plugin: " + e);
            this.mICas = null;
            if (this.mICas == null) {
                throw new MediaCasException.UnsupportedCasException("Unsupported CA_system_id " + CA_system_id);
            }
        } catch (Throwable th) {
            if (this.mICas == null) {
                throw new MediaCasException.UnsupportedCasException("Unsupported CA_system_id " + CA_system_id);
            }
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public IHwBinder getBinder() {
        validateInternalStates();
        return this.mICas.asBinder();
    }

    public interface EventListener {
        void onEvent(MediaCas mediaCas, int i, int i2, byte[] bArr);

        default void onSessionEvent(MediaCas mediaCas, Session session, int event, int arg, byte[] data) {
            Log.d(MediaCas.TAG, "Received MediaCas Session event");
        }
    }

    public void setEventListener(EventListener listener, Handler handler) {
        this.mListener = listener;
        Looper looper = null;
        if (this.mListener == null) {
            this.mEventHandler = null;
            return;
        }
        if (handler != null) {
            looper = handler.getLooper();
        }
        Looper looper2 = looper;
        if (looper2 == null) {
            Looper myLooper = Looper.myLooper();
            looper2 = myLooper;
            if (myLooper == null) {
                Looper mainLooper = Looper.getMainLooper();
                looper2 = mainLooper;
                if (mainLooper == null) {
                    HandlerThread handlerThread = this.mHandlerThread;
                    if (handlerThread == null || !handlerThread.isAlive()) {
                        this.mHandlerThread = new HandlerThread("MediaCasEventThread", -2);
                        this.mHandlerThread.start();
                    }
                    looper2 = this.mHandlerThread.getLooper();
                }
            }
        }
        this.mEventHandler = new EventHandler(looper2);
    }

    public void setPrivateData(byte[] data) throws MediaCasException {
        validateInternalStates();
        try {
            MediaCasException.throwExceptionIfNeeded(this.mICas.setPrivateData(toByteArray(data, 0, data.length)));
        } catch (RemoteException e) {
            cleanupAndRethrowIllegalState();
        }
    }

    private class OpenSessionCallback implements ICas.openSessionCallback {
        public Session mSession;
        public int mStatus;

        private OpenSessionCallback() {
        }

        @Override // android.hardware.cas.V1_0.ICas.openSessionCallback
        public void onValues(int status, ArrayList<Byte> sessionId) {
            this.mStatus = status;
            this.mSession = MediaCas.this.createFromSessionId(sessionId);
        }
    }

    public Session openSession() throws MediaCasException {
        validateInternalStates();
        try {
            OpenSessionCallback cb = new OpenSessionCallback();
            this.mICas.openSession(cb);
            MediaCasException.throwExceptionIfNeeded(cb.mStatus);
            return cb.mSession;
        } catch (RemoteException e) {
            cleanupAndRethrowIllegalState();
            return null;
        }
    }

    public void processEmm(byte[] data, int offset, int length) throws MediaCasException {
        validateInternalStates();
        try {
            MediaCasException.throwExceptionIfNeeded(this.mICas.processEmm(toByteArray(data, offset, length)));
        } catch (RemoteException e) {
            cleanupAndRethrowIllegalState();
        }
    }

    public void processEmm(byte[] data) throws MediaCasException {
        processEmm(data, 0, data.length);
    }

    public void sendEvent(int event, int arg, byte[] data) throws MediaCasException {
        validateInternalStates();
        try {
            MediaCasException.throwExceptionIfNeeded(this.mICas.sendEvent(event, arg, toByteArray(data)));
        } catch (RemoteException e) {
            cleanupAndRethrowIllegalState();
        }
    }

    public void provision(String provisionString) throws MediaCasException {
        validateInternalStates();
        try {
            MediaCasException.throwExceptionIfNeeded(this.mICas.provision(provisionString));
        } catch (RemoteException e) {
            cleanupAndRethrowIllegalState();
        }
    }

    public void refreshEntitlements(int refreshType, byte[] refreshData) throws MediaCasException {
        validateInternalStates();
        try {
            MediaCasException.throwExceptionIfNeeded(this.mICas.refreshEntitlements(refreshType, toByteArray(refreshData)));
        } catch (RemoteException e) {
            cleanupAndRethrowIllegalState();
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        ICas iCas = this.mICas;
        if (iCas != null) {
            try {
                iCas.release();
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mICas = null;
                throw th;
            }
            this.mICas = null;
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        close();
    }
}

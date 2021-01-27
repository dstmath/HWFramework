package com.android.server;

import android.content.Context;
import android.hardware.ISensorPrivacyListener;
import android.hardware.ISensorPrivacyManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.internal.util.function.pooled.PooledLambda;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class SensorPrivacyService extends SystemService {
    private static final String SENSOR_PRIVACY_XML_FILE = "sensor_privacy.xml";
    private static final String TAG = "SensorPrivacyService";
    private static final String XML_ATTRIBUTE_ENABLED = "enabled";
    private static final String XML_TAG_SENSOR_PRIVACY = "sensor-privacy";
    private final SensorPrivacyServiceImpl mSensorPrivacyServiceImpl;

    public SensorPrivacyService(Context context) {
        super(context);
        this.mSensorPrivacyServiceImpl = new SensorPrivacyServiceImpl(context);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.SensorPrivacyService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.SensorPrivacyService$SensorPrivacyServiceImpl, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("sensor_privacy", this.mSensorPrivacyServiceImpl);
    }

    /* access modifiers changed from: package-private */
    public class SensorPrivacyServiceImpl extends ISensorPrivacyManager.Stub {
        @GuardedBy({"mLock"})
        private final AtomicFile mAtomicFile;
        private final Context mContext;
        @GuardedBy({"mLock"})
        private boolean mEnabled;
        private final SensorPrivacyHandler mHandler;
        private final Object mLock = new Object();

        SensorPrivacyServiceImpl(Context context) {
            this.mContext = context;
            this.mHandler = new SensorPrivacyHandler(FgThread.get().getLooper(), this.mContext);
            this.mAtomicFile = new AtomicFile(new File(Environment.getDataSystemDirectory(), SensorPrivacyService.SENSOR_PRIVACY_XML_FILE));
            synchronized (this.mLock) {
                this.mEnabled = readPersistedSensorPrivacyEnabledLocked();
            }
        }

        public void setSensorPrivacy(boolean enable) {
            enforceSensorPrivacyPermission();
            synchronized (this.mLock) {
                this.mEnabled = enable;
                FileOutputStream outputStream = null;
                try {
                    XmlSerializer serializer = new FastXmlSerializer();
                    outputStream = this.mAtomicFile.startWrite();
                    serializer.setOutput(outputStream, StandardCharsets.UTF_8.name());
                    serializer.startDocument(null, true);
                    serializer.startTag(null, SensorPrivacyService.XML_TAG_SENSOR_PRIVACY);
                    serializer.attribute(null, SensorPrivacyService.XML_ATTRIBUTE_ENABLED, String.valueOf(enable));
                    serializer.endTag(null, SensorPrivacyService.XML_TAG_SENSOR_PRIVACY);
                    serializer.endDocument();
                    this.mAtomicFile.finishWrite(outputStream);
                } catch (IOException e) {
                    Log.e(SensorPrivacyService.TAG, "Caught an exception persisting the sensor privacy state: ", e);
                    this.mAtomicFile.failWrite(outputStream);
                }
            }
            this.mHandler.onSensorPrivacyChanged(enable);
        }

        private void enforceSensorPrivacyPermission() {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.MANAGE_SENSOR_PRIVACY") != 0) {
                throw new SecurityException("Changing sensor privacy requires the following permission: android.permission.MANAGE_SENSOR_PRIVACY");
            }
        }

        public boolean isSensorPrivacyEnabled() {
            boolean z;
            synchronized (this.mLock) {
                z = this.mEnabled;
            }
            return z;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0042, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0043, code lost:
            if (r0 != null) goto L_0x0045;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
            r0.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0049, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x004a, code lost:
            r1.addSuppressed(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x004d, code lost:
            throw r2;
         */
        private boolean readPersistedSensorPrivacyEnabledLocked() {
            if (!this.mAtomicFile.exists()) {
                return false;
            }
            FileInputStream inputStream = this.mAtomicFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, StandardCharsets.UTF_8.name());
            XmlUtils.beginDocument(parser, SensorPrivacyService.XML_TAG_SENSOR_PRIVACY);
            parser.next();
            parser.getName();
            boolean enabled = Boolean.valueOf(parser.getAttributeValue(null, SensorPrivacyService.XML_ATTRIBUTE_ENABLED)).booleanValue();
            if (inputStream == null) {
                return enabled;
            }
            try {
                inputStream.close();
                return enabled;
            } catch (IOException | XmlPullParserException e) {
                Log.e(SensorPrivacyService.TAG, "Caught an exception reading the state from storage: ", e);
                this.mAtomicFile.delete();
                return false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        public void persistSensorPrivacyState() {
            synchronized (this.mLock) {
                FileOutputStream outputStream = null;
                try {
                    XmlSerializer serializer = new FastXmlSerializer();
                    outputStream = this.mAtomicFile.startWrite();
                    serializer.setOutput(outputStream, StandardCharsets.UTF_8.name());
                    serializer.startDocument(null, true);
                    serializer.startTag(null, SensorPrivacyService.XML_TAG_SENSOR_PRIVACY);
                    serializer.attribute(null, SensorPrivacyService.XML_ATTRIBUTE_ENABLED, String.valueOf(this.mEnabled));
                    serializer.endTag(null, SensorPrivacyService.XML_TAG_SENSOR_PRIVACY);
                    serializer.endDocument();
                    this.mAtomicFile.finishWrite(outputStream);
                } catch (IOException e) {
                    Log.e(SensorPrivacyService.TAG, "Caught an exception persisting the sensor privacy state: ", e);
                    this.mAtomicFile.failWrite(outputStream);
                }
            }
        }

        public void addSensorPrivacyListener(ISensorPrivacyListener listener) {
            if (listener != null) {
                this.mHandler.addListener(listener);
                return;
            }
            throw new NullPointerException("listener cannot be null");
        }

        public void removeSensorPrivacyListener(ISensorPrivacyListener listener) {
            if (listener != null) {
                this.mHandler.removeListener(listener);
                return;
            }
            throw new NullPointerException("listener cannot be null");
        }
    }

    /* access modifiers changed from: private */
    public final class SensorPrivacyHandler extends Handler {
        private static final int MESSAGE_SENSOR_PRIVACY_CHANGED = 1;
        private final Context mContext;
        private final ArrayMap<ISensorPrivacyListener, DeathRecipient> mDeathRecipients = new ArrayMap<>();
        private final Object mListenerLock = new Object();
        @GuardedBy({"mListenerLock"})
        private final RemoteCallbackList<ISensorPrivacyListener> mListeners = new RemoteCallbackList<>();

        SensorPrivacyHandler(Looper looper, Context context) {
            super(looper);
            this.mContext = context;
        }

        public void onSensorPrivacyChanged(boolean enabled) {
            sendMessage(PooledLambda.obtainMessage($$Lambda$2rlj96lJ7chZcASbtixW5GQdw.INSTANCE, this, Boolean.valueOf(enabled)));
            sendMessage(PooledLambda.obtainMessage($$Lambda$SensorPrivacyService$SensorPrivacyHandler$ctW6BcqPnLm_33mG1WatsFwFT7w.INSTANCE, SensorPrivacyService.this.mSensorPrivacyServiceImpl));
        }

        public void addListener(ISensorPrivacyListener listener) {
            synchronized (this.mListenerLock) {
                this.mDeathRecipients.put(listener, new DeathRecipient(listener));
                this.mListeners.register(listener);
            }
        }

        public void removeListener(ISensorPrivacyListener listener) {
            synchronized (this.mListenerLock) {
                DeathRecipient deathRecipient = this.mDeathRecipients.remove(listener);
                if (deathRecipient != null) {
                    deathRecipient.destroy();
                }
                this.mListeners.unregister(listener);
            }
        }

        public void handleSensorPrivacyChanged(boolean enabled) {
            int count = this.mListeners.beginBroadcast();
            for (int i = 0; i < count; i++) {
                ISensorPrivacyListener listener = this.mListeners.getBroadcastItem(i);
                try {
                    listener.onSensorPrivacyChanged(enabled);
                } catch (RemoteException e) {
                    Log.e(SensorPrivacyService.TAG, "Caught an exception notifying listener " + listener + ": ", e);
                }
            }
            this.mListeners.finishBroadcast();
        }
    }

    /* access modifiers changed from: private */
    public final class DeathRecipient implements IBinder.DeathRecipient {
        private ISensorPrivacyListener mListener;

        DeathRecipient(ISensorPrivacyListener listener) {
            this.mListener = listener;
            try {
                this.mListener.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            SensorPrivacyService.this.mSensorPrivacyServiceImpl.removeSensorPrivacyListener(this.mListener);
        }

        public void destroy() {
            try {
                this.mListener.asBinder().unlinkToDeath(this, 0);
            } catch (NoSuchElementException e) {
            }
        }
    }
}

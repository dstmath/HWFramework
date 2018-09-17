package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class FingerprintsUserState {
    private static final String ATTR_DEVICE_ID = "deviceId";
    private static final String ATTR_FINGER_ID = "fingerId";
    private static final String ATTR_GROUP_ID = "groupId";
    private static final String ATTR_NAME = "name";
    private static final String FINGERPRINT_FILE = "settings_fingerprint.xml";
    private static final String TAG = "FingerprintState";
    private static final String TAG_FINGERPRINT = "fingerprint";
    private static final String TAG_FINGERPRINTS = "fingerprints";
    private final Context mCtx;
    private final File mFile;
    @GuardedBy("this")
    private final ArrayList<Fingerprint> mFingerprints;
    private final Runnable mWriteStateRunnable;

    public FingerprintsUserState(Context ctx, int userId) {
        this.mFingerprints = new ArrayList();
        this.mWriteStateRunnable = new Runnable() {
            public void run() {
                FingerprintsUserState.this.doWriteState();
            }
        };
        this.mFile = getFileForUser(userId);
        this.mCtx = ctx;
        synchronized (this) {
            readStateSyncLocked();
        }
    }

    public void addFingerprint(int fingerId, int groupId) {
        synchronized (this) {
            this.mFingerprints.add(new Fingerprint(getUniqueName(), groupId, fingerId, 0));
            scheduleWriteStateLocked();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeFingerprint(int fingerId) {
        synchronized (this) {
            int i = 0;
            while (true) {
                if (i >= this.mFingerprints.size()) {
                    break;
                } else if (((Fingerprint) this.mFingerprints.get(i)).getFingerId() == fingerId) {
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void renameFingerprint(int fingerId, CharSequence name) {
        synchronized (this) {
            int i = 0;
            while (true) {
                if (i >= this.mFingerprints.size()) {
                    break;
                } else if (((Fingerprint) this.mFingerprints.get(i)).getFingerId() == fingerId) {
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    public List<Fingerprint> getFingerprints() {
        List copy;
        synchronized (this) {
            copy = getCopy(this.mFingerprints);
        }
        return copy;
    }

    private String getUniqueName() {
        int guess = 1;
        while (true) {
            String name = this.mCtx.getString(17039851, new Object[]{Integer.valueOf(guess)});
            if (isUnique(name)) {
                return name;
            }
            guess++;
        }
    }

    private boolean isUnique(String name) {
        for (Fingerprint fp : this.mFingerprints) {
            if (fp.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    private static File getFileForUser(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), FINGERPRINT_FILE);
    }

    private void scheduleWriteStateLocked() {
        AsyncTask.execute(this.mWriteStateRunnable);
    }

    private ArrayList<Fingerprint> getCopy(ArrayList<Fingerprint> array) {
        ArrayList<Fingerprint> result = new ArrayList(array.size());
        for (int i = 0; i < array.size(); i++) {
            Fingerprint fp = (Fingerprint) array.get(i);
            result.add(new Fingerprint(fp.getName(), fp.getGroupId(), fp.getFingerId(), fp.getDeviceId()));
        }
        return result;
    }

    private void doWriteState() {
        AtomicFile destination = new AtomicFile(this.mFile);
        synchronized (this) {
            ArrayList<Fingerprint> fingerprints = getCopy(this.mFingerprints);
        }
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = destination.startWrite();
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(autoCloseable, "utf-8");
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.startTag(null, TAG_FINGERPRINTS);
            int count = fingerprints.size();
            for (int i = 0; i < count; i++) {
                Fingerprint fp = (Fingerprint) fingerprints.get(i);
                serializer.startTag(null, TAG_FINGERPRINT);
                serializer.attribute(null, ATTR_FINGER_ID, Integer.toString(fp.getFingerId()));
                serializer.attribute(null, ATTR_NAME, fp.getName().toString());
                serializer.attribute(null, ATTR_GROUP_ID, Integer.toString(fp.getGroupId()));
                serializer.attribute(null, ATTR_DEVICE_ID, Long.toString(fp.getDeviceId()));
                serializer.endTag(null, TAG_FINGERPRINT);
            }
            serializer.endTag(null, TAG_FINGERPRINTS);
            serializer.endDocument();
            destination.finishWrite(autoCloseable);
            IoUtils.closeQuietly(autoCloseable);
        } catch (Throwable th) {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    private void readStateSyncLocked() {
        if (this.mFile.exists()) {
            try {
                FileInputStream in = new FileInputStream(this.mFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(in, null);
                    parseStateLocked(parser);
                    IoUtils.closeQuietly(in);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed parsing settings file: " + this.mFile, e);
                } catch (Throwable th) {
                    IoUtils.closeQuietly(in);
                }
            } catch (FileNotFoundException e2) {
                Slog.i(TAG, "No fingerprint state");
            }
        }
    }

    private void parseStateLocked(XmlPullParser parser) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_FINGERPRINTS))) {
                parseFingerprintsLocked(parser);
            }
        }
    }

    private void parseFingerprintsLocked(XmlPullParser parser) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(TAG_FINGERPRINT))) {
                this.mFingerprints.add(new Fingerprint(parser.getAttributeValue(null, ATTR_NAME), Integer.parseInt(parser.getAttributeValue(null, ATTR_GROUP_ID)), Integer.parseInt(parser.getAttributeValue(null, ATTR_FINGER_ID)), (long) Integer.parseInt(parser.getAttributeValue(null, ATTR_DEVICE_ID))));
            }
        }
    }
}

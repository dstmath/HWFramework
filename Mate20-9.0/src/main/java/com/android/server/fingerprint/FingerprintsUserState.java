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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
    private static final String FINGERPRINT_FILE_UD = "settings_fingerprint_ud.xml";
    private static final String TAG = "FingerprintState";
    private static final String TAG_FINGERPRINT = "fingerprint";
    private static final String TAG_FINGERPRINTS = "fingerprints";
    private final Context mCtx;
    private final File mFile;
    @GuardedBy("this")
    private final ArrayList<Fingerprint> mFingerprints = new ArrayList<>();
    private final Runnable mWriteStateRunnable = new Runnable() {
        public void run() {
            FingerprintsUserState.this.doWriteState();
        }
    };

    public FingerprintsUserState(Context ctx, int userId) {
        this.mFile = getFileForUser(userId);
        this.mCtx = ctx;
        synchronized (this) {
            readStateSyncLocked();
        }
    }

    public FingerprintsUserState(Context ctx, int userId, int deviceIndex) {
        this.mFile = getFileForUser(userId, deviceIndex);
        this.mCtx = ctx;
        synchronized (this) {
            readStateSyncLocked();
        }
    }

    public void addFingerprint(int fingerId, int groupId) {
        synchronized (this) {
            ArrayList<Fingerprint> arrayList = this.mFingerprints;
            Fingerprint fingerprint = new Fingerprint(getUniqueName(), groupId, fingerId, 0);
            arrayList.add(fingerprint);
            scheduleWriteStateLocked();
        }
    }

    public boolean isFingerprintExist(int fingerId) {
        synchronized (this) {
            int size = this.mFingerprints.size();
            for (int i = 0; i < size; i++) {
                if (this.mFingerprints.get(i).getFingerId() == fingerId) {
                    return true;
                }
            }
            return false;
        }
    }

    public void removeFingerprint(int fingerId) {
        synchronized (this) {
            int i = 0;
            while (true) {
                if (i >= this.mFingerprints.size()) {
                    break;
                } else if (this.mFingerprints.get(i).getFingerId() == fingerId) {
                    this.mFingerprints.remove(i);
                    scheduleWriteStateLocked();
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    public void renameFingerprint(int fingerId, CharSequence name) {
        synchronized (this) {
            int i = 0;
            while (true) {
                if (i >= this.mFingerprints.size()) {
                    break;
                } else if (this.mFingerprints.get(i).getFingerId() == fingerId) {
                    Fingerprint old = this.mFingerprints.get(i);
                    ArrayList<Fingerprint> arrayList = this.mFingerprints;
                    Fingerprint fingerprint = new Fingerprint(name, old.getGroupId(), old.getFingerId(), old.getDeviceId());
                    arrayList.set(i, fingerprint);
                    scheduleWriteStateLocked();
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    public List<Fingerprint> getFingerprints() {
        ArrayList<Fingerprint> copy;
        synchronized (this) {
            copy = getCopy(this.mFingerprints);
        }
        return copy;
    }

    private String getUniqueName() {
        int guess = 1;
        while (true) {
            String name = this.mCtx.getString(17040080, new Object[]{Integer.valueOf(guess)});
            if (isUnique(name)) {
                return name;
            }
            guess++;
        }
    }

    private boolean isUnique(String name) {
        Iterator<Fingerprint> it = this.mFingerprints.iterator();
        while (it.hasNext()) {
            if (it.next().getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    private static File getFileForUser(int userId) {
        return new File(Environment.getUserSystemDirectory(userId), FINGERPRINT_FILE);
    }

    private static File getFileForUser(int userId, int deviceIndex) {
        return new File(Environment.getUserSystemDirectory(userId), deviceIndex > 0 ? FINGERPRINT_FILE_UD : FINGERPRINT_FILE);
    }

    private void scheduleWriteStateLocked() {
        AsyncTask.execute(this.mWriteStateRunnable);
    }

    private ArrayList<Fingerprint> getCopy(ArrayList<Fingerprint> array) {
        ArrayList<Fingerprint> result = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            Fingerprint fp = array.get(i);
            Fingerprint fingerprint = new Fingerprint(fp.getName(), fp.getGroupId(), fp.getFingerId(), fp.getDeviceId());
            result.add(fingerprint);
        }
        return result;
    }

    /* access modifiers changed from: private */
    public void doWriteState() {
        ArrayList<Fingerprint> fingerprints;
        AtomicFile destination = new AtomicFile(this.mFile);
        synchronized (this) {
            fingerprints = getCopy(this.mFingerprints);
        }
        try {
            FileOutputStream out = destination.startWrite();
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(out, "utf-8");
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument(null, true);
            serializer.startTag(null, TAG_FINGERPRINTS);
            int count = fingerprints.size();
            for (int i = 0; i < count; i++) {
                Fingerprint fp = fingerprints.get(i);
                serializer.startTag(null, TAG_FINGERPRINT);
                serializer.attribute(null, ATTR_FINGER_ID, Integer.toString(fp.getFingerId()));
                serializer.attribute(null, "name", fp.getName().toString());
                serializer.attribute(null, ATTR_GROUP_ID, Integer.toString(fp.getGroupId()));
                serializer.attribute(null, ATTR_DEVICE_ID, Long.toString(fp.getDeviceId()));
                serializer.endTag(null, TAG_FINGERPRINT);
            }
            serializer.endTag(null, TAG_FINGERPRINTS);
            serializer.endDocument();
            destination.finishWrite(out);
            IoUtils.closeQuietly(out);
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
            throw th;
        }
    }

    @GuardedBy("this")
    private void readStateSyncLocked() {
        if (this.mFile.exists()) {
            try {
                FileInputStream in = new FileInputStream(this.mFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(in, null);
                    parseStateLocked(parser);
                } catch (IOException | XmlPullParserException e) {
                    Slog.e(TAG, "Failed parsing settings fingerprint file");
                } catch (Throwable th) {
                    IoUtils.closeQuietly(in);
                    throw th;
                }
                IoUtils.closeQuietly(in);
            } catch (FileNotFoundException e2) {
                Slog.i(TAG, "No fingerprint state");
            }
        }
    }

    @GuardedBy("this")
    private void parseStateLocked(XmlPullParser parser) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
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

    @GuardedBy("this")
    private void parseFingerprintsLocked(XmlPullParser parser) throws IOException, XmlPullParserException {
        XmlPullParser xmlPullParser = parser;
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (type != 3 && type != 4) {
                    if (parser.getName().equals(TAG_FINGERPRINT)) {
                        String name = xmlPullParser.getAttributeValue(null, "name");
                        String groupId = xmlPullParser.getAttributeValue(null, ATTR_GROUP_ID);
                        String fingerId = xmlPullParser.getAttributeValue(null, ATTR_FINGER_ID);
                        String deviceId = xmlPullParser.getAttributeValue(null, ATTR_DEVICE_ID);
                        ArrayList<Fingerprint> arrayList = this.mFingerprints;
                        Fingerprint fingerprint = r6;
                        Fingerprint fingerprint2 = new Fingerprint(name, Integer.parseInt(groupId), Integer.parseInt(fingerId), (long) Integer.parseInt(deviceId));
                        arrayList.add(fingerprint);
                    }
                }
                xmlPullParser = parser;
            }
        }
    }
}

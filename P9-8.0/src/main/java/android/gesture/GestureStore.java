package android.gesture;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class GestureStore {
    private static final short FILE_FORMAT_VERSION = (short) 1;
    public static final int ORIENTATION_INVARIANT = 1;
    public static final int ORIENTATION_SENSITIVE = 2;
    static final int ORIENTATION_SENSITIVE_4 = 4;
    static final int ORIENTATION_SENSITIVE_8 = 8;
    private static final boolean PROFILE_LOADING_SAVING = false;
    public static final int SEQUENCE_INVARIANT = 1;
    public static final int SEQUENCE_SENSITIVE = 2;
    private boolean mChanged = false;
    private Learner mClassifier = new InstanceLearner();
    private final HashMap<String, ArrayList<Gesture>> mNamedGestures = new HashMap();
    private int mOrientationStyle = 2;
    private int mSequenceType = 2;

    public void setOrientationStyle(int style) {
        this.mOrientationStyle = style;
    }

    public int getOrientationStyle() {
        return this.mOrientationStyle;
    }

    public void setSequenceType(int type) {
        this.mSequenceType = type;
    }

    public int getSequenceType() {
        return this.mSequenceType;
    }

    public Set<String> getGestureEntries() {
        return this.mNamedGestures.keySet();
    }

    public ArrayList<Prediction> recognize(Gesture gesture) {
        return this.mClassifier.classify(this.mSequenceType, this.mOrientationStyle, Instance.createInstance(this.mSequenceType, this.mOrientationStyle, gesture, null).vector);
    }

    public void addGesture(String entryName, Gesture gesture) {
        if (entryName != null && entryName.length() != 0) {
            ArrayList<Gesture> gestures = (ArrayList) this.mNamedGestures.get(entryName);
            if (gestures == null) {
                gestures = new ArrayList();
                this.mNamedGestures.put(entryName, gestures);
            }
            gestures.add(gesture);
            this.mClassifier.addInstance(Instance.createInstance(this.mSequenceType, this.mOrientationStyle, gesture, entryName));
            this.mChanged = true;
        }
    }

    public void removeGesture(String entryName, Gesture gesture) {
        ArrayList<Gesture> gestures = (ArrayList) this.mNamedGestures.get(entryName);
        if (gestures != null) {
            gestures.remove(gesture);
            if (gestures.isEmpty()) {
                this.mNamedGestures.remove(entryName);
            }
            this.mClassifier.removeInstance(gesture.getID());
            this.mChanged = true;
        }
    }

    public void removeEntry(String entryName) {
        this.mNamedGestures.remove(entryName);
        this.mClassifier.removeInstances(entryName);
        this.mChanged = true;
    }

    public ArrayList<Gesture> getGestures(String entryName) {
        ArrayList<Gesture> gestures = (ArrayList) this.mNamedGestures.get(entryName);
        if (gestures != null) {
            return new ArrayList(gestures);
        }
        return null;
    }

    public boolean hasChanged() {
        return this.mChanged;
    }

    public void save(OutputStream stream) throws IOException {
        save(stream, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0069  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void save(OutputStream stream, boolean closeStream) throws IOException {
        Throwable th;
        Closeable out = null;
        try {
            HashMap<String, ArrayList<Gesture>> maps = this.mNamedGestures;
            if (!(stream instanceof BufferedOutputStream)) {
                stream = new BufferedOutputStream(stream, 32768);
            }
            DataOutputStream out2 = new DataOutputStream(stream);
            try {
                out2.writeShort(1);
                out2.writeInt(maps.size());
                for (Entry<String, ArrayList<Gesture>> entry : maps.entrySet()) {
                    String key = (String) entry.getKey();
                    ArrayList<Gesture> examples = (ArrayList) entry.getValue();
                    int count = examples.size();
                    out2.writeUTF(key);
                    out2.writeInt(count);
                    for (int i = 0; i < count; i++) {
                        ((Gesture) examples.get(i)).serialize(out2);
                    }
                }
                out2.flush();
                this.mChanged = false;
                if (closeStream) {
                    GestureUtils.closeStream(out2);
                }
            } catch (Throwable th2) {
                th = th2;
                Object out3 = out2;
                if (closeStream) {
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            if (closeStream) {
                GestureUtils.closeStream(out3);
            }
            throw th;
        }
    }

    public void load(InputStream stream) throws IOException {
        load(stream, false);
    }

    public void load(InputStream stream, boolean closeStream) throws IOException {
        Throwable th;
        Closeable closeable = null;
        try {
            if (!(stream instanceof BufferedInputStream)) {
                stream = new BufferedInputStream(stream, 32768);
            }
            DataInputStream in = new DataInputStream(stream);
            try {
                switch (in.readShort()) {
                    case (short) 1:
                        readFormatV1(in);
                        break;
                }
                if (closeStream) {
                    GestureUtils.closeStream(in);
                }
            } catch (Throwable th2) {
                th = th2;
                closeable = in;
            }
        } catch (Throwable th3) {
            th = th3;
            if (closeStream) {
                GestureUtils.closeStream(closeable);
            }
            throw th;
        }
    }

    private void readFormatV1(DataInputStream in) throws IOException {
        Learner classifier = this.mClassifier;
        HashMap<String, ArrayList<Gesture>> namedGestures = this.mNamedGestures;
        namedGestures.clear();
        int entriesCount = in.readInt();
        for (int i = 0; i < entriesCount; i++) {
            String name = in.readUTF();
            int gestureCount = in.readInt();
            ArrayList<Gesture> gestures = new ArrayList(gestureCount);
            for (int j = 0; j < gestureCount; j++) {
                Gesture gesture = Gesture.deserialize(in);
                gestures.add(gesture);
                classifier.addInstance(Instance.createInstance(this.mSequenceType, this.mOrientationStyle, gesture, name));
            }
            namedGestures.put(name, gestures);
        }
    }

    Learner getLearner() {
        return this.mClassifier;
    }
}

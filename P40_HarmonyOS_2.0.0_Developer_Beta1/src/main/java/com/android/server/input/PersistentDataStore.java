package com.android.server.input;

import android.hardware.input.TouchCalibration;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* access modifiers changed from: package-private */
public final class PersistentDataStore {
    static final String AUTO_LAYOUT_DESCRIPTOR = "keyboard_layout_auto";
    static final String TAG = "InputManager";
    private final AtomicFile mAtomicFile = new AtomicFile(new File("/data/system/input-manager-state.xml"), "input-state");
    private boolean mDirty;
    private final HashMap<String, InputDeviceState> mInputDevices = new HashMap<>();
    private boolean mLoaded;

    public void saveIfNeeded() {
        if (this.mDirty) {
            save();
            this.mDirty = false;
        }
    }

    public TouchCalibration getTouchCalibration(String inputDeviceDescriptor, int surfaceRotation) {
        InputDeviceState state = getInputDeviceState(inputDeviceDescriptor, false);
        if (state == null) {
            return TouchCalibration.IDENTITY;
        }
        TouchCalibration cal = state.getTouchCalibration(surfaceRotation);
        if (cal == null) {
            return TouchCalibration.IDENTITY;
        }
        return cal;
    }

    public boolean setTouchCalibration(String inputDeviceDescriptor, int surfaceRotation, TouchCalibration calibration) {
        if (!getInputDeviceState(inputDeviceDescriptor, true).setTouchCalibration(surfaceRotation, calibration)) {
            return false;
        }
        setDirty();
        return true;
    }

    public String getCurrentKeyboardLayout(String inputDeviceDescriptor) {
        InputDeviceState state = getInputDeviceState(inputDeviceDescriptor, false);
        if (state != null) {
            return state.getCurrentKeyboardLayout();
        }
        return null;
    }

    public boolean setCurrentKeyboardLayout(String inputDeviceDescriptor, String keyboardLayoutDescriptor) {
        if (!getInputDeviceState(inputDeviceDescriptor, true).setCurrentKeyboardLayout(keyboardLayoutDescriptor)) {
            return false;
        }
        setDirty();
        return true;
    }

    public String getAutoKeyboardLayout(String inputDeviceDescriptor) {
        InputDeviceState state = getInputDeviceState(inputDeviceDescriptor, false);
        if (state != null) {
            return state.getAutoKeyboardLayout();
        }
        return null;
    }

    public boolean setAutoKeyboardLayout(String inputDeviceDescriptor, String keyboardLayoutDescriptor) {
        if (!getInputDeviceState(inputDeviceDescriptor, true).setAutoKeyboardLayout(keyboardLayoutDescriptor)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean setHasAddedEnLayout(String inputDeviceDescriptor, boolean hasAdded) {
        if (!getInputDeviceState(inputDeviceDescriptor, true).setHasAddedEnLayout(hasAdded)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean hasAddedEnLayout(String inputDeviceDescriptor) {
        InputDeviceState state = getInputDeviceState(inputDeviceDescriptor, false);
        if (state == null) {
            return false;
        }
        return state.hasAddedEnLayout();
    }

    public String[] getKeyboardLayouts(String inputDeviceDescriptor) {
        InputDeviceState state = getInputDeviceState(inputDeviceDescriptor, false);
        if (state == null) {
            return (String[]) ArrayUtils.emptyArray(String.class);
        }
        return state.getKeyboardLayouts();
    }

    public boolean addKeyboardLayout(String inputDeviceDescriptor, String keyboardLayoutDescriptor) {
        if (!getInputDeviceState(inputDeviceDescriptor, true).addKeyboardLayout(keyboardLayoutDescriptor)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean removeKeyboardLayout(String inputDeviceDescriptor, String keyboardLayoutDescriptor) {
        if (!getInputDeviceState(inputDeviceDescriptor, true).removeKeyboardLayout(keyboardLayoutDescriptor)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean switchKeyboardLayout(String inputDeviceDescriptor, int direction) {
        InputDeviceState state = getInputDeviceState(inputDeviceDescriptor, false);
        if (state == null || !state.switchKeyboardLayout(direction)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean removeUninstalledKeyboardLayouts(Set<String> availableKeyboardLayouts) {
        boolean changed = false;
        for (InputDeviceState state : this.mInputDevices.values()) {
            if (state.removeUninstalledKeyboardLayouts(availableKeyboardLayouts)) {
                changed = true;
            }
        }
        if (!changed) {
            return false;
        }
        setDirty();
        return true;
    }

    private InputDeviceState getInputDeviceState(String inputDeviceDescriptor, boolean createIfAbsent) {
        loadIfNeeded();
        InputDeviceState state = this.mInputDevices.get(inputDeviceDescriptor);
        if (state != null || !createIfAbsent) {
            return state;
        }
        InputDeviceState state2 = new InputDeviceState();
        this.mInputDevices.put(inputDeviceDescriptor, state2);
        setDirty();
        return state2;
    }

    private void loadIfNeeded() {
        if (!this.mLoaded) {
            load();
            this.mLoaded = true;
        }
    }

    private void setDirty() {
        this.mDirty = true;
    }

    private void clearState() {
        this.mInputDevices.clear();
    }

    private void load() {
        clearState();
        try {
            InputStream is = this.mAtomicFile.openRead();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(new BufferedInputStream(is), StandardCharsets.UTF_8.name());
                loadFromXml(parser);
            } catch (IOException ex) {
                Slog.w(TAG, "Failed to load input manager persistent store data.", ex);
                clearState();
            } catch (XmlPullParserException ex2) {
                Slog.w(TAG, "Failed to load input manager persistent store data.", ex2);
                clearState();
            } finally {
                IoUtils.closeQuietly(is);
            }
        } catch (FileNotFoundException e) {
        }
    }

    private void save() {
        try {
            FileOutputStream os = this.mAtomicFile.startWrite();
            boolean success = false;
            try {
                XmlSerializer serializer = new FastXmlSerializer();
                serializer.setOutput(new BufferedOutputStream(os), StandardCharsets.UTF_8.name());
                saveToXml(serializer);
                serializer.flush();
                success = true;
                if (!success) {
                    this.mAtomicFile.failWrite(os);
                }
            } finally {
                if (success) {
                    this.mAtomicFile.finishWrite(os);
                } else {
                    this.mAtomicFile.failWrite(os);
                }
            }
        } catch (IOException ex) {
            Slog.w(TAG, "Failed to save input manager persistent store data.", ex);
        }
    }

    private void loadFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
        XmlUtils.beginDocument(parser, "input-manager-state");
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (parser.getName().equals("input-devices")) {
                loadInputDevicesFromXml(parser);
            }
        }
    }

    private void loadInputDevicesFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (parser.getName().equals("input-device")) {
                String descriptor = parser.getAttributeValue(null, "descriptor");
                if (descriptor == null) {
                    throw new XmlPullParserException("Missing descriptor attribute on input-device.");
                } else if (!this.mInputDevices.containsKey(descriptor)) {
                    InputDeviceState state = new InputDeviceState();
                    String enlayout = parser.getAttributeValue(null, "enlayout");
                    if (enlayout != null && "true".equals(enlayout)) {
                        state.setHasAddedEnLayout(true);
                    }
                    state.loadFromXml(parser);
                    this.mInputDevices.put(descriptor, state);
                } else {
                    throw new XmlPullParserException("Found duplicate input device.");
                }
            }
        }
    }

    private void saveToXml(XmlSerializer serializer) throws IOException {
        serializer.startDocument(null, true);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        serializer.startTag(null, "input-manager-state");
        serializer.startTag(null, "input-devices");
        for (Map.Entry<String, InputDeviceState> entry : this.mInputDevices.entrySet()) {
            InputDeviceState state = entry.getValue();
            serializer.startTag(null, "input-device");
            serializer.attribute(null, "descriptor", entry.getKey());
            if (state.hasAddedEnLayout()) {
                serializer.attribute(null, "enlayout", "true");
            }
            state.saveToXml(serializer);
            serializer.endTag(null, "input-device");
        }
        serializer.endTag(null, "input-devices");
        serializer.endTag(null, "input-manager-state");
        serializer.endDocument();
    }

    /* access modifiers changed from: private */
    public static final class InputDeviceState {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final String[] CALIBRATION_NAME = {"x_scale", "x_ymix", "x_offset", "y_xmix", "y_scale", "y_offset"};
        private String mAutoKeyboardLayout;
        private String mCurrentKeyboardLayout;
        private boolean mHasAddedEnLayout;
        private ArrayList<String> mKeyboardLayouts;
        private TouchCalibration[] mTouchCalibration;

        private InputDeviceState() {
            this.mTouchCalibration = new TouchCalibration[4];
            this.mHasAddedEnLayout = false;
            this.mKeyboardLayouts = new ArrayList<>();
        }

        public TouchCalibration getTouchCalibration(int surfaceRotation) {
            try {
                return this.mTouchCalibration[surfaceRotation];
            } catch (ArrayIndexOutOfBoundsException ex) {
                Slog.w(PersistentDataStore.TAG, "Cannot get touch calibration.", ex);
                return null;
            }
        }

        public boolean setTouchCalibration(int surfaceRotation, TouchCalibration calibration) {
            try {
                if (calibration.equals(this.mTouchCalibration[surfaceRotation])) {
                    return false;
                }
                this.mTouchCalibration[surfaceRotation] = calibration;
                return true;
            } catch (ArrayIndexOutOfBoundsException ex) {
                Slog.w(PersistentDataStore.TAG, "Cannot set touch calibration.", ex);
                return false;
            }
        }

        public String getCurrentKeyboardLayout() {
            return this.mCurrentKeyboardLayout;
        }

        public boolean setCurrentKeyboardLayout(String keyboardLayout) {
            if (Objects.equals(this.mCurrentKeyboardLayout, keyboardLayout)) {
                return false;
            }
            addKeyboardLayout(keyboardLayout);
            this.mCurrentKeyboardLayout = keyboardLayout;
            return true;
        }

        public String getAutoKeyboardLayout() {
            if (PersistentDataStore.AUTO_LAYOUT_DESCRIPTOR.equals(this.mCurrentKeyboardLayout)) {
                return this.mAutoKeyboardLayout;
            }
            return null;
        }

        public boolean setAutoKeyboardLayout(String keyboardLayout) {
            if (Objects.equals(this.mAutoKeyboardLayout, keyboardLayout)) {
                return false;
            }
            this.mAutoKeyboardLayout = keyboardLayout;
            return true;
        }

        public boolean hasAddedEnLayout() {
            return this.mHasAddedEnLayout;
        }

        public boolean setHasAddedEnLayout(boolean hasAdded) {
            if (this.mHasAddedEnLayout == hasAdded) {
                return false;
            }
            this.mHasAddedEnLayout = hasAdded;
            return true;
        }

        public String[] getKeyboardLayouts() {
            if (this.mKeyboardLayouts.isEmpty()) {
                return (String[]) ArrayUtils.emptyArray(String.class);
            }
            ArrayList<String> arrayList = this.mKeyboardLayouts;
            return (String[]) arrayList.toArray(new String[arrayList.size()]);
        }

        public boolean addKeyboardLayout(String keyboardLayout) {
            int index = Collections.binarySearch(this.mKeyboardLayouts, keyboardLayout);
            if (index >= 0) {
                return false;
            }
            this.mKeyboardLayouts.add((-index) - 1, keyboardLayout);
            if (this.mCurrentKeyboardLayout == null) {
                this.mCurrentKeyboardLayout = keyboardLayout;
            }
            return true;
        }

        public boolean removeKeyboardLayout(String keyboardLayout) {
            int index = Collections.binarySearch(this.mKeyboardLayouts, keyboardLayout);
            if (index < 0) {
                return false;
            }
            this.mKeyboardLayouts.remove(index);
            updateCurrentKeyboardLayoutIfRemoved(keyboardLayout, index);
            return true;
        }

        private void updateCurrentKeyboardLayoutIfRemoved(String removedKeyboardLayout, int removedIndex) {
            if (!Objects.equals(this.mCurrentKeyboardLayout, removedKeyboardLayout)) {
                return;
            }
            if (!this.mKeyboardLayouts.isEmpty()) {
                int index = removedIndex;
                if (index == this.mKeyboardLayouts.size()) {
                    index = 0;
                }
                this.mCurrentKeyboardLayout = this.mKeyboardLayouts.get(index);
                return;
            }
            this.mCurrentKeyboardLayout = null;
        }

        public boolean switchKeyboardLayout(int direction) {
            int index;
            int size = this.mKeyboardLayouts.size();
            if (size < 2) {
                return false;
            }
            int index2 = Collections.binarySearch(this.mKeyboardLayouts, this.mCurrentKeyboardLayout);
            if (direction > 0) {
                index = (index2 + 1) % size;
            } else {
                index = ((index2 + size) - 1) % size;
            }
            this.mCurrentKeyboardLayout = this.mKeyboardLayouts.get(index);
            return true;
        }

        public boolean removeUninstalledKeyboardLayouts(Set<String> availableKeyboardLayouts) {
            boolean changed = false;
            int i = this.mKeyboardLayouts.size();
            while (true) {
                int i2 = i - 1;
                if (i <= 0) {
                    return changed;
                }
                String keyboardLayout = this.mKeyboardLayouts.get(i2);
                if (!availableKeyboardLayouts.contains(keyboardLayout) && !PersistentDataStore.AUTO_LAYOUT_DESCRIPTOR.equals(keyboardLayout)) {
                    Slog.i(PersistentDataStore.TAG, "Removing uninstalled keyboard layout " + keyboardLayout);
                    this.mKeyboardLayouts.remove(i2);
                    updateCurrentKeyboardLayoutIfRemoved(keyboardLayout, i2);
                    changed = true;
                }
                i = i2;
            }
        }

        public void loadFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
            int outerDepth = parser.getDepth();
            while (XmlUtils.nextElementWithin(parser, outerDepth)) {
                if (parser.getName().equals("keyboard-layout")) {
                    String descriptor = parser.getAttributeValue(null, "descriptor");
                    if (descriptor != null) {
                        String current = parser.getAttributeValue(null, "current");
                        if (!this.mKeyboardLayouts.contains(descriptor)) {
                            this.mKeyboardLayouts.add(descriptor);
                            if (current != null && current.equals("true")) {
                                if (this.mCurrentKeyboardLayout == null) {
                                    this.mCurrentKeyboardLayout = descriptor;
                                } else {
                                    throw new XmlPullParserException("Found multiple current keyboard layouts.");
                                }
                            }
                        } else {
                            throw new XmlPullParserException("Found duplicate keyboard layout.");
                        }
                    } else {
                        throw new XmlPullParserException("Missing descriptor attribute on keyboard-layout.");
                    }
                } else if (parser.getName().equals("calibration")) {
                    String format = parser.getAttributeValue(null, "format");
                    String rotation = parser.getAttributeValue(null, "rotation");
                    int r = -1;
                    if (format == null) {
                        throw new XmlPullParserException("Missing format attribute on calibration.");
                    } else if (format.equals("affine")) {
                        if (rotation != null) {
                            try {
                                r = stringToSurfaceRotation(rotation);
                            } catch (IllegalArgumentException e) {
                                throw new XmlPullParserException("Unsupported rotation for calibration.");
                            }
                        }
                        float[] matrix = TouchCalibration.IDENTITY.getAffineTransform();
                        int depth = parser.getDepth();
                        while (XmlUtils.nextElementWithin(parser, depth)) {
                            String tag = parser.getName().toLowerCase();
                            String value = parser.nextText();
                            int i = 0;
                            while (true) {
                                if (i >= matrix.length) {
                                    break;
                                }
                                String[] strArr = CALIBRATION_NAME;
                                if (i >= strArr.length) {
                                    break;
                                } else if (tag.equals(strArr[i])) {
                                    matrix[i] = Float.parseFloat(value);
                                    break;
                                } else {
                                    i++;
                                }
                            }
                        }
                        if (r == -1) {
                            int r2 = 0;
                            while (true) {
                                TouchCalibration[] touchCalibrationArr = this.mTouchCalibration;
                                if (r2 >= touchCalibrationArr.length) {
                                    break;
                                }
                                touchCalibrationArr[r2] = new TouchCalibration(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
                                r2++;
                            }
                        } else {
                            this.mTouchCalibration[r] = new TouchCalibration(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
                        }
                    } else {
                        throw new XmlPullParserException("Unsupported format for calibration.");
                    }
                } else {
                    continue;
                }
            }
            Collections.sort(this.mKeyboardLayouts);
            if (this.mCurrentKeyboardLayout == null && !this.mKeyboardLayouts.isEmpty()) {
                this.mCurrentKeyboardLayout = this.mKeyboardLayouts.get(0);
            }
        }

        public void saveToXml(XmlSerializer serializer) throws IOException {
            Iterator<String> it = this.mKeyboardLayouts.iterator();
            while (it.hasNext()) {
                String layout = it.next();
                serializer.startTag(null, "keyboard-layout");
                serializer.attribute(null, "descriptor", layout);
                if (layout.equals(this.mCurrentKeyboardLayout)) {
                    serializer.attribute(null, "current", "true");
                }
                serializer.endTag(null, "keyboard-layout");
            }
            int i = 0;
            while (true) {
                TouchCalibration[] touchCalibrationArr = this.mTouchCalibration;
                if (i < touchCalibrationArr.length) {
                    if (touchCalibrationArr[i] != null) {
                        String rotation = surfaceRotationToString(i);
                        float[] transform = this.mTouchCalibration[i].getAffineTransform();
                        serializer.startTag(null, "calibration");
                        serializer.attribute(null, "format", "affine");
                        serializer.attribute(null, "rotation", rotation);
                        for (int j = 0; j < transform.length; j++) {
                            String[] strArr = CALIBRATION_NAME;
                            if (j >= strArr.length) {
                                break;
                            }
                            serializer.startTag(null, strArr[j]);
                            serializer.text(Float.toString(transform[j]));
                            serializer.endTag(null, CALIBRATION_NAME[j]);
                        }
                        serializer.endTag(null, "calibration");
                    }
                    i++;
                } else {
                    return;
                }
            }
        }

        private static String surfaceRotationToString(int surfaceRotation) {
            if (surfaceRotation == 0) {
                return "0";
            }
            if (surfaceRotation == 1) {
                return "90";
            }
            if (surfaceRotation == 2) {
                return "180";
            }
            if (surfaceRotation == 3) {
                return "270";
            }
            throw new IllegalArgumentException("Unsupported surface rotation value" + surfaceRotation);
        }

        private static int stringToSurfaceRotation(String s) {
            if ("0".equals(s)) {
                return 0;
            }
            if ("90".equals(s)) {
                return 1;
            }
            if ("180".equals(s)) {
                return 2;
            }
            if ("270".equals(s)) {
                return 3;
            }
            throw new IllegalArgumentException("Unsupported surface rotation string '" + s + "'");
        }
    }
}

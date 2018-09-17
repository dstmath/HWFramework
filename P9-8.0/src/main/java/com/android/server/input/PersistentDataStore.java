package com.android.server.input;

import android.hardware.input.TouchCalibration;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.inputmethod.InputMethodSubtypeHandle;
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
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import libcore.io.IoUtils;
import libcore.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

final class PersistentDataStore {
    static final String TAG = "InputManager";
    private final AtomicFile mAtomicFile = new AtomicFile(new File("/data/system/input-manager-state.xml"));
    private boolean mDirty;
    private final HashMap<String, InputDeviceState> mInputDevices = new HashMap();
    private boolean mLoaded;

    private static final class InputDeviceState {
        private static final String[] CALIBRATION_NAME = new String[]{"x_scale", "x_ymix", "x_offset", "y_xmix", "y_scale", "y_offset"};
        private String mCurrentKeyboardLayout;
        private ArrayMap<InputMethodSubtypeHandle, String> mKeyboardLayouts;
        private TouchCalibration[] mTouchCalibration;
        private List<String> mUnassociatedKeyboardLayouts;

        /* synthetic */ InputDeviceState(InputDeviceState -this0) {
            this();
        }

        private InputDeviceState() {
            this.mTouchCalibration = new TouchCalibration[4];
            this.mUnassociatedKeyboardLayouts = new ArrayList();
            this.mKeyboardLayouts = new ArrayMap();
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
            if (Objects.equal(this.mCurrentKeyboardLayout, keyboardLayout)) {
                return false;
            }
            addKeyboardLayout(keyboardLayout);
            this.mCurrentKeyboardLayout = keyboardLayout;
            return true;
        }

        public String[] getKeyboardLayouts() {
            if (this.mUnassociatedKeyboardLayouts.isEmpty()) {
                return (String[]) ArrayUtils.emptyArray(String.class);
            }
            return (String[]) this.mUnassociatedKeyboardLayouts.toArray(new String[this.mUnassociatedKeyboardLayouts.size()]);
        }

        public String getKeyboardLayout(InputMethodSubtypeHandle handle) {
            return (String) this.mKeyboardLayouts.get(handle);
        }

        public boolean setKeyboardLayout(InputMethodSubtypeHandle imeHandle, String keyboardLayout) {
            if (TextUtils.equals((String) this.mKeyboardLayouts.get(imeHandle), keyboardLayout)) {
                return false;
            }
            this.mKeyboardLayouts.put(imeHandle, keyboardLayout);
            return true;
        }

        public boolean addKeyboardLayout(String keyboardLayout) {
            int index = Collections.binarySearch(this.mUnassociatedKeyboardLayouts, keyboardLayout);
            if (index >= 0) {
                return false;
            }
            this.mUnassociatedKeyboardLayouts.add((-index) - 1, keyboardLayout);
            if (this.mCurrentKeyboardLayout == null) {
                this.mCurrentKeyboardLayout = keyboardLayout;
            }
            return true;
        }

        public boolean removeKeyboardLayout(String keyboardLayout) {
            int index = Collections.binarySearch(this.mUnassociatedKeyboardLayouts, keyboardLayout);
            if (index < 0) {
                return false;
            }
            this.mUnassociatedKeyboardLayouts.remove(index);
            updateCurrentKeyboardLayoutIfRemoved(keyboardLayout, index);
            return true;
        }

        private void updateCurrentKeyboardLayoutIfRemoved(String removedKeyboardLayout, int removedIndex) {
            if (!Objects.equal(this.mCurrentKeyboardLayout, removedKeyboardLayout)) {
                return;
            }
            if (this.mUnassociatedKeyboardLayouts.isEmpty()) {
                this.mCurrentKeyboardLayout = null;
                return;
            }
            int index = removedIndex;
            if (removedIndex == this.mUnassociatedKeyboardLayouts.size()) {
                index = 0;
            }
            this.mCurrentKeyboardLayout = (String) this.mUnassociatedKeyboardLayouts.get(index);
        }

        public boolean switchKeyboardLayout(InputMethodSubtypeHandle imeHandle) {
            String layout = (String) this.mKeyboardLayouts.get(imeHandle);
            if (TextUtils.equals(this.mCurrentKeyboardLayout, layout)) {
                return false;
            }
            this.mCurrentKeyboardLayout = layout;
            return true;
        }

        public boolean removeUninstalledKeyboardLayouts(Set<String> availableKeyboardLayouts) {
            boolean changed = false;
            int i = this.mUnassociatedKeyboardLayouts.size();
            while (true) {
                int i2 = i;
                i = i2 - 1;
                if (i2 <= 0) {
                    return changed;
                }
                String keyboardLayout = (String) this.mUnassociatedKeyboardLayouts.get(i);
                if (!availableKeyboardLayouts.contains(keyboardLayout)) {
                    Slog.i(PersistentDataStore.TAG, "Removing uninstalled keyboard layout " + keyboardLayout);
                    this.mUnassociatedKeyboardLayouts.remove(i);
                    updateCurrentKeyboardLayoutIfRemoved(keyboardLayout, i);
                    changed = true;
                }
            }
        }

        public void loadFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
            int outerDepth = parser.getDepth();
            while (XmlUtils.nextElementWithin(parser, outerDepth)) {
                if (parser.getName().equals("keyboard-layout")) {
                    String descriptor = parser.getAttributeValue(null, "descriptor");
                    if (descriptor == null) {
                        throw new XmlPullParserException("Missing descriptor attribute on keyboard-layout.");
                    }
                    String current = parser.getAttributeValue(null, "current");
                    if (current != null && current.equals("true")) {
                        if (this.mCurrentKeyboardLayout != null) {
                            throw new XmlPullParserException("Found multiple current keyboard layouts.");
                        }
                        this.mCurrentKeyboardLayout = descriptor;
                    }
                    String inputMethodId = parser.getAttributeValue(null, "input-method-id");
                    String inputMethodSubtypeId = parser.getAttributeValue(null, "input-method-subtype-id");
                    if ((inputMethodId == null && inputMethodSubtypeId != null) || (inputMethodId != null && inputMethodSubtypeId == null)) {
                        throw new XmlPullParserException("Found an incomplete input method description");
                    } else if (inputMethodSubtypeId != null) {
                        InputMethodSubtypeHandle handle = new InputMethodSubtypeHandle(inputMethodId, Integer.parseInt(inputMethodSubtypeId));
                        if (this.mKeyboardLayouts.containsKey(handle)) {
                            throw new XmlPullParserException("Found duplicate subtype to keyboard layout mapping: " + handle);
                        }
                        this.mKeyboardLayouts.put(handle, descriptor);
                    } else if (this.mUnassociatedKeyboardLayouts.contains(descriptor)) {
                        throw new XmlPullParserException("Found duplicate unassociated keyboard layout: " + descriptor);
                    } else {
                        this.mUnassociatedKeyboardLayouts.add(descriptor);
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
                            while (i < matrix.length && i < CALIBRATION_NAME.length) {
                                if (tag.equals(CALIBRATION_NAME[i])) {
                                    matrix[i] = Float.parseFloat(value);
                                    break;
                                }
                                i++;
                            }
                        }
                        if (r == -1) {
                            for (r = 0; r < this.mTouchCalibration.length; r++) {
                                this.mTouchCalibration[r] = new TouchCalibration(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
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
            Collections.sort(this.mUnassociatedKeyboardLayouts);
            if (this.mCurrentKeyboardLayout == null && (this.mUnassociatedKeyboardLayouts.isEmpty() ^ 1) != 0) {
                this.mCurrentKeyboardLayout = (String) this.mUnassociatedKeyboardLayouts.get(0);
            }
        }

        public void saveToXml(XmlSerializer serializer) throws IOException {
            String layout;
            int i;
            for (String layout2 : this.mUnassociatedKeyboardLayouts) {
                serializer.startTag(null, "keyboard-layout");
                serializer.attribute(null, "descriptor", layout2);
                serializer.endTag(null, "keyboard-layout");
            }
            int N = this.mKeyboardLayouts.size();
            for (i = 0; i < N; i++) {
                InputMethodSubtypeHandle handle = (InputMethodSubtypeHandle) this.mKeyboardLayouts.keyAt(i);
                layout2 = (String) this.mKeyboardLayouts.valueAt(i);
                serializer.startTag(null, "keyboard-layout");
                serializer.attribute(null, "descriptor", layout2);
                serializer.attribute(null, "input-method-id", handle.getInputMethodId());
                serializer.attribute(null, "input-method-subtype-id", Integer.toString(handle.getSubtypeId()));
                if (layout2.equals(this.mCurrentKeyboardLayout)) {
                    serializer.attribute(null, "current", "true");
                }
                serializer.endTag(null, "keyboard-layout");
            }
            for (i = 0; i < this.mTouchCalibration.length; i++) {
                if (this.mTouchCalibration[i] != null) {
                    String rotation = surfaceRotationToString(i);
                    float[] transform = this.mTouchCalibration[i].getAffineTransform();
                    serializer.startTag(null, "calibration");
                    serializer.attribute(null, "format", "affine");
                    serializer.attribute(null, "rotation", rotation);
                    int j = 0;
                    while (j < transform.length && j < CALIBRATION_NAME.length) {
                        serializer.startTag(null, CALIBRATION_NAME[j]);
                        serializer.text(Float.toString(transform[j]));
                        serializer.endTag(null, CALIBRATION_NAME[j]);
                        j++;
                    }
                    serializer.endTag(null, "calibration");
                }
            }
        }

        private void dump(PrintWriter pw, String prefix) {
            pw.println(prefix + "CurrentKeyboardLayout=" + this.mCurrentKeyboardLayout);
            pw.println(prefix + "UnassociatedKeyboardLayouts=" + this.mUnassociatedKeyboardLayouts);
            pw.println(prefix + "TouchCalibration=" + Arrays.toString(this.mTouchCalibration));
            pw.println(prefix + "Subtype to Layout Mappings:");
            int N = this.mKeyboardLayouts.size();
            if (N != 0) {
                for (int i = 0; i < N; i++) {
                    pw.println(prefix + "  " + this.mKeyboardLayouts.keyAt(i) + ": " + ((String) this.mKeyboardLayouts.valueAt(i)));
                }
                return;
            }
            pw.println(prefix + "  <none>");
        }

        private static String surfaceRotationToString(int surfaceRotation) {
            switch (surfaceRotation) {
                case 0:
                    return "0";
                case 1:
                    return "90";
                case 2:
                    return "180";
                case 3:
                    return "270";
                default:
                    throw new IllegalArgumentException("Unsupported surface rotation value" + surfaceRotation);
            }
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

    public String[] getKeyboardLayouts(String inputDeviceDescriptor) {
        InputDeviceState state = getInputDeviceState(inputDeviceDescriptor, false);
        if (state == null) {
            return (String[]) ArrayUtils.emptyArray(String.class);
        }
        return state.getKeyboardLayouts();
    }

    public String getKeyboardLayout(String inputDeviceDescriptor, InputMethodSubtypeHandle imeHandle) {
        InputDeviceState state = getInputDeviceState(inputDeviceDescriptor, false);
        if (state == null) {
            return null;
        }
        return state.getKeyboardLayout(imeHandle);
    }

    public boolean setKeyboardLayout(String inputDeviceDescriptor, InputMethodSubtypeHandle imeHandle, String keyboardLayoutDescriptor) {
        if (!getInputDeviceState(inputDeviceDescriptor, true).setKeyboardLayout(imeHandle, keyboardLayoutDescriptor)) {
            return false;
        }
        setDirty();
        return true;
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

    public boolean switchKeyboardLayout(String inputDeviceDescriptor, InputMethodSubtypeHandle imeHandle) {
        InputDeviceState state = getInputDeviceState(inputDeviceDescriptor, false);
        if (state == null || !state.switchKeyboardLayout(imeHandle)) {
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
        InputDeviceState state = (InputDeviceState) this.mInputDevices.get(inputDeviceDescriptor);
        if (state != null || !createIfAbsent) {
            return state;
        }
        state = new InputDeviceState();
        this.mInputDevices.put(inputDeviceDescriptor, state);
        setDirty();
        return state;
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
        FileOutputStream os;
        try {
            os = this.mAtomicFile.startWrite();
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(new BufferedOutputStream(os), StandardCharsets.UTF_8.name());
            saveToXml(serializer);
            serializer.flush();
            if (true) {
                this.mAtomicFile.finishWrite(os);
            } else {
                this.mAtomicFile.failWrite(os);
            }
        } catch (IOException ex) {
            Slog.w(TAG, "Failed to save input manager persistent store data.", ex);
        } catch (Throwable th) {
            if (false) {
                this.mAtomicFile.finishWrite(os);
            } else {
                this.mAtomicFile.failWrite(os);
            }
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
                } else if (this.mInputDevices.containsKey(descriptor)) {
                    throw new XmlPullParserException("Found duplicate input device.");
                } else {
                    InputDeviceState state = new InputDeviceState();
                    state.loadFromXml(parser);
                    this.mInputDevices.put(descriptor, state);
                }
            }
        }
    }

    private void saveToXml(XmlSerializer serializer) throws IOException {
        serializer.startDocument(null, Boolean.valueOf(true));
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        serializer.startTag(null, "input-manager-state");
        serializer.startTag(null, "input-devices");
        for (Entry<String, InputDeviceState> entry : this.mInputDevices.entrySet()) {
            String descriptor = (String) entry.getKey();
            InputDeviceState state = (InputDeviceState) entry.getValue();
            serializer.startTag(null, "input-device");
            serializer.attribute(null, "descriptor", descriptor);
            state.saveToXml(serializer);
            serializer.endTag(null, "input-device");
        }
        serializer.endTag(null, "input-devices");
        serializer.endTag(null, "input-manager-state");
        serializer.endDocument();
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "PersistentDataStore");
        pw.println(prefix + "  mLoaded=" + this.mLoaded);
        pw.println(prefix + "  mDirty=" + this.mDirty);
        pw.println(prefix + "  InputDeviceStates:");
        int i = 0;
        for (Entry<String, InputDeviceState> entry : this.mInputDevices.entrySet()) {
            int i2 = i + 1;
            pw.println(prefix + "    " + i + ": " + ((String) entry.getKey()));
            ((InputDeviceState) entry.getValue()).dump(pw, prefix + "      ");
            i = i2;
        }
    }
}

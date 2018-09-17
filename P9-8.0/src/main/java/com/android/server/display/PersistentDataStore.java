package com.android.server.display;

import android.hardware.display.WifiDisplay;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
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
import java.util.HashMap;
import java.util.Map.Entry;
import libcore.io.IoUtils;
import libcore.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

final class PersistentDataStore {
    static final String TAG = "DisplayManager";
    private final AtomicFile mAtomicFile = new AtomicFile(new File("/data/system/display-manager-state.xml"));
    private boolean mDirty;
    private final HashMap<String, DisplayState> mDisplayStates = new HashMap();
    private boolean mLoaded;
    private ArrayList<WifiDisplay> mRememberedWifiDisplays = new ArrayList();

    private static final class DisplayState {
        private int mColorMode;

        /* synthetic */ DisplayState(DisplayState -this0) {
            this();
        }

        private DisplayState() {
        }

        public boolean setColorMode(int colorMode) {
            if (colorMode == this.mColorMode) {
                return false;
            }
            this.mColorMode = colorMode;
            return true;
        }

        public int getColorMode() {
            return this.mColorMode;
        }

        public void loadFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
            int outerDepth = parser.getDepth();
            while (XmlUtils.nextElementWithin(parser, outerDepth)) {
                if (parser.getName().equals("color-mode")) {
                    this.mColorMode = Integer.parseInt(parser.nextText());
                }
            }
        }

        public void saveToXml(XmlSerializer serializer) throws IOException {
            serializer.startTag(null, "color-mode");
            serializer.text(Integer.toString(this.mColorMode));
            serializer.endTag(null, "color-mode");
        }

        private void dump(PrintWriter pw, String prefix) {
            pw.println(prefix + "ColorMode=" + this.mColorMode);
        }
    }

    public void saveIfNeeded() {
        if (this.mDirty) {
            save();
            this.mDirty = false;
        }
    }

    public WifiDisplay getRememberedWifiDisplay(String deviceAddress) {
        loadIfNeeded();
        int index = findRememberedWifiDisplay(deviceAddress);
        if (index >= 0) {
            return (WifiDisplay) this.mRememberedWifiDisplays.get(index);
        }
        return null;
    }

    public WifiDisplay[] getRememberedWifiDisplays() {
        loadIfNeeded();
        return (WifiDisplay[]) this.mRememberedWifiDisplays.toArray(new WifiDisplay[this.mRememberedWifiDisplays.size()]);
    }

    public WifiDisplay applyWifiDisplayAlias(WifiDisplay display) {
        if (display != null) {
            loadIfNeeded();
            String alias = null;
            int index = findRememberedWifiDisplay(display.getDeviceAddress());
            if (index >= 0) {
                alias = ((WifiDisplay) this.mRememberedWifiDisplays.get(index)).getDeviceAlias();
            }
            if (!Objects.equal(display.getDeviceAlias(), alias)) {
                return new WifiDisplay(display.getDeviceAddress(), display.getDeviceName(), alias, display.isAvailable(), display.canConnect(), display.isRemembered());
            }
        }
        return display;
    }

    public WifiDisplay[] applyWifiDisplayAliases(WifiDisplay[] displays) {
        WifiDisplay[] results = displays;
        if (displays != null) {
            int count = displays.length;
            for (int i = 0; i < count; i++) {
                WifiDisplay result = applyWifiDisplayAlias(displays[i]);
                if (result != displays[i]) {
                    if (results == displays) {
                        results = new WifiDisplay[count];
                        System.arraycopy(displays, 0, results, 0, count);
                    }
                    results[i] = result;
                }
            }
        }
        return results;
    }

    public boolean rememberWifiDisplay(WifiDisplay display) {
        loadIfNeeded();
        int index = findRememberedWifiDisplay(display.getDeviceAddress());
        if (index < 0) {
            this.mRememberedWifiDisplays.add(display);
        } else if (((WifiDisplay) this.mRememberedWifiDisplays.get(index)).equals(display)) {
            return false;
        } else {
            this.mRememberedWifiDisplays.set(index, display);
        }
        setDirty();
        return true;
    }

    public boolean forgetWifiDisplay(String deviceAddress) {
        int index = findRememberedWifiDisplay(deviceAddress);
        if (index < 0) {
            return false;
        }
        this.mRememberedWifiDisplays.remove(index);
        setDirty();
        return true;
    }

    private int findRememberedWifiDisplay(String deviceAddress) {
        int count = this.mRememberedWifiDisplays.size();
        for (int i = 0; i < count; i++) {
            if (((WifiDisplay) this.mRememberedWifiDisplays.get(i)).getDeviceAddress().equals(deviceAddress)) {
                return i;
            }
        }
        return -1;
    }

    public int getColorMode(DisplayDevice device) {
        if (!device.hasStableUniqueId()) {
            return -1;
        }
        DisplayState state = getDisplayState(device.getUniqueId(), false);
        if (state == null) {
            return -1;
        }
        return state.getColorMode();
    }

    public boolean setColorMode(DisplayDevice device, int colorMode) {
        if (!device.hasStableUniqueId() || !getDisplayState(device.getUniqueId(), true).setColorMode(colorMode)) {
            return false;
        }
        setDirty();
        return true;
    }

    private DisplayState getDisplayState(String uniqueId, boolean createIfAbsent) {
        loadIfNeeded();
        DisplayState state = (DisplayState) this.mDisplayStates.get(uniqueId);
        if (state != null || !createIfAbsent) {
            return state;
        }
        state = new DisplayState();
        this.mDisplayStates.put(uniqueId, state);
        setDirty();
        return state;
    }

    public void loadIfNeeded() {
        if (!this.mLoaded) {
            load();
            this.mLoaded = true;
        }
    }

    private void setDirty() {
        this.mDirty = true;
    }

    private void clearState() {
        this.mRememberedWifiDisplays.clear();
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
                Slog.w(TAG, "Failed to load display manager persistent store data.", ex);
                clearState();
            } catch (XmlPullParserException ex2) {
                Slog.w(TAG, "Failed to load display manager persistent store data.", ex2);
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
            Slog.w(TAG, "Failed to save display manager persistent store data.", ex);
        } catch (Throwable th) {
            if (false) {
                this.mAtomicFile.finishWrite(os);
            } else {
                this.mAtomicFile.failWrite(os);
            }
        }
    }

    private void loadFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
        XmlUtils.beginDocument(parser, "display-manager-state");
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (parser.getName().equals("remembered-wifi-displays")) {
                loadRememberedWifiDisplaysFromXml(parser);
            }
            if (parser.getName().equals("display-states")) {
                loadDisplaysFromXml(parser);
            }
        }
    }

    private void loadRememberedWifiDisplaysFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (parser.getName().equals("wifi-display")) {
                String deviceAddress = parser.getAttributeValue(null, "deviceAddress");
                String deviceName = parser.getAttributeValue(null, "deviceName");
                String deviceAlias = parser.getAttributeValue(null, "deviceAlias");
                if (deviceAddress == null || deviceName == null) {
                    throw new XmlPullParserException("Missing deviceAddress or deviceName attribute on wifi-display.");
                } else if (findRememberedWifiDisplay(deviceAddress) >= 0) {
                    throw new XmlPullParserException("Found duplicate wifi display device address.");
                } else {
                    this.mRememberedWifiDisplays.add(new WifiDisplay(deviceAddress, deviceName, deviceAlias, false, false, false));
                }
            }
        }
    }

    private void loadDisplaysFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, outerDepth)) {
            if (parser.getName().equals("display")) {
                String uniqueId = parser.getAttributeValue(null, "unique-id");
                if (uniqueId == null) {
                    throw new XmlPullParserException("Missing unique-id attribute on display.");
                } else if (this.mDisplayStates.containsKey(uniqueId)) {
                    throw new XmlPullParserException("Found duplicate display.");
                } else {
                    DisplayState state = new DisplayState();
                    state.loadFromXml(parser);
                    this.mDisplayStates.put(uniqueId, state);
                }
            }
        }
    }

    private void saveToXml(XmlSerializer serializer) throws IOException {
        serializer.startDocument(null, Boolean.valueOf(true));
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        serializer.startTag(null, "display-manager-state");
        serializer.startTag(null, "remembered-wifi-displays");
        for (WifiDisplay display : this.mRememberedWifiDisplays) {
            serializer.startTag(null, "wifi-display");
            serializer.attribute(null, "deviceAddress", display.getDeviceAddress());
            serializer.attribute(null, "deviceName", display.getDeviceName());
            if (display.getDeviceAlias() != null) {
                serializer.attribute(null, "deviceAlias", display.getDeviceAlias());
            }
            serializer.endTag(null, "wifi-display");
        }
        serializer.endTag(null, "remembered-wifi-displays");
        serializer.startTag(null, "display-states");
        for (Entry<String, DisplayState> entry : this.mDisplayStates.entrySet()) {
            String uniqueId = (String) entry.getKey();
            DisplayState state = (DisplayState) entry.getValue();
            serializer.startTag(null, "display");
            serializer.attribute(null, "unique-id", uniqueId);
            state.saveToXml(serializer);
            serializer.endTag(null, "display");
        }
        serializer.endTag(null, "display-states");
        serializer.endTag(null, "display-manager-state");
        serializer.endDocument();
    }

    public void dump(PrintWriter pw) {
        int i;
        pw.println("PersistentDataStore");
        pw.println("  mLoaded=" + this.mLoaded);
        pw.println("  mDirty=" + this.mDirty);
        pw.println("  RememberedWifiDisplays:");
        int i2 = 0;
        for (WifiDisplay display : this.mRememberedWifiDisplays) {
            i = i2 + 1;
            pw.println("    " + i2 + ": " + display);
            i2 = i;
        }
        pw.println("  DisplayStates:");
        i2 = 0;
        for (Entry<String, DisplayState> entry : this.mDisplayStates.entrySet()) {
            i = i2 + 1;
            pw.println("    " + i2 + ": " + ((String) entry.getKey()));
            ((DisplayState) entry.getValue()).dump(pw, "      ");
            i2 = i;
        }
    }
}

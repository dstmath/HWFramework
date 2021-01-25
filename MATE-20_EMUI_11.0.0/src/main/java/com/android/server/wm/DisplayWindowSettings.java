package com.android.server.wm;

import android.os.Environment;
import android.provider.Settings;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import android.view.DisplayAddress;
import android.view.DisplayInfo;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* access modifiers changed from: package-private */
public class DisplayWindowSettings {
    private static final int IDENTIFIER_PORT = 1;
    private static final int IDENTIFIER_UNIQUE_ID = 0;
    private static final String TAG = "WindowManager";
    private final HashMap<String, Entry> mEntries;
    @DisplayIdentifierType
    private int mIdentifier;
    private final WindowManagerService mService;
    private final SettingPersister mStorage;

    @interface DisplayIdentifierType {
    }

    /* access modifiers changed from: package-private */
    public interface SettingPersister {
        void finishWrite(OutputStream outputStream, boolean z);

        InputStream openRead() throws IOException;

        OutputStream startWrite() throws IOException;
    }

    /* access modifiers changed from: private */
    public static class Entry {
        private int mFixedToUserRotation;
        private int mForcedDensity;
        private int mForcedHeight;
        private int mForcedScalingMode;
        private int mForcedWidth;
        private final String mName;
        private int mOverscanBottom;
        private int mOverscanLeft;
        private int mOverscanRight;
        private int mOverscanTop;
        private int mRemoveContentMode;
        private boolean mShouldShowIme;
        private boolean mShouldShowSystemDecors;
        private boolean mShouldShowWithInsecureKeyguard;
        private int mUserRotation;
        private int mUserRotationMode;
        private int mWindowingMode;

        private Entry(String name) {
            this.mWindowingMode = 0;
            this.mUserRotationMode = 0;
            this.mUserRotation = 0;
            this.mForcedScalingMode = 0;
            this.mRemoveContentMode = 0;
            this.mShouldShowWithInsecureKeyguard = false;
            this.mShouldShowSystemDecors = false;
            this.mShouldShowIme = false;
            this.mFixedToUserRotation = 0;
            this.mName = name;
        }

        private Entry(String name, Entry copyFrom) {
            this(name);
            this.mOverscanLeft = copyFrom.mOverscanLeft;
            this.mOverscanTop = copyFrom.mOverscanTop;
            this.mOverscanRight = copyFrom.mOverscanRight;
            this.mOverscanBottom = copyFrom.mOverscanBottom;
            this.mWindowingMode = copyFrom.mWindowingMode;
            this.mUserRotationMode = copyFrom.mUserRotationMode;
            this.mUserRotation = copyFrom.mUserRotation;
            this.mForcedWidth = copyFrom.mForcedWidth;
            this.mForcedHeight = copyFrom.mForcedHeight;
            this.mForcedDensity = copyFrom.mForcedDensity;
            this.mForcedScalingMode = copyFrom.mForcedScalingMode;
            this.mRemoveContentMode = copyFrom.mRemoveContentMode;
            this.mShouldShowWithInsecureKeyguard = copyFrom.mShouldShowWithInsecureKeyguard;
            this.mShouldShowSystemDecors = copyFrom.mShouldShowSystemDecors;
            this.mShouldShowIme = copyFrom.mShouldShowIme;
            this.mFixedToUserRotation = copyFrom.mFixedToUserRotation;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isEmpty() {
            return this.mOverscanLeft == 0 && this.mOverscanTop == 0 && this.mOverscanRight == 0 && this.mOverscanBottom == 0 && this.mWindowingMode == 0 && this.mUserRotationMode == 0 && this.mUserRotation == 0 && this.mForcedWidth == 0 && this.mForcedHeight == 0 && this.mForcedDensity == 0 && this.mForcedScalingMode == 0 && this.mRemoveContentMode == 0 && !this.mShouldShowWithInsecureKeyguard && !this.mShouldShowSystemDecors && !this.mShouldShowIme && this.mFixedToUserRotation == 0;
        }
    }

    DisplayWindowSettings(WindowManagerService service) {
        this(service, new AtomicFileStorage());
    }

    @VisibleForTesting
    DisplayWindowSettings(WindowManagerService service, SettingPersister storageImpl) {
        this.mEntries = new HashMap<>();
        this.mIdentifier = 0;
        this.mService = service;
        this.mStorage = storageImpl;
        readSettings();
    }

    private Entry getEntry(DisplayInfo displayInfo) {
        Entry entry = this.mEntries.get(getIdentifier(displayInfo));
        if (entry != null) {
            return entry;
        }
        Entry entry2 = this.mEntries.get(displayInfo.name);
        if (entry2 != null) {
            return updateIdentifierForEntry(entry2, displayInfo);
        }
        return null;
    }

    private Entry getOrCreateEntry(DisplayInfo displayInfo) {
        Entry entry = getEntry(displayInfo);
        return entry != null ? entry : new Entry(getIdentifier(displayInfo));
    }

    private Entry updateIdentifierForEntry(Entry entry, DisplayInfo displayInfo) {
        Entry newEntry = new Entry(getIdentifier(displayInfo), entry);
        removeEntry(displayInfo);
        this.mEntries.put(newEntry.mName, newEntry);
        return newEntry;
    }

    /* access modifiers changed from: package-private */
    public void setOverscanLocked(DisplayInfo displayInfo, int left, int top, int right, int bottom) {
        Entry entry = getOrCreateEntry(displayInfo);
        entry.mOverscanLeft = left;
        entry.mOverscanTop = top;
        entry.mOverscanRight = right;
        entry.mOverscanBottom = bottom;
        writeSettingsIfNeeded(entry, displayInfo);
    }

    /* access modifiers changed from: package-private */
    public void setUserRotation(DisplayContent displayContent, int rotationMode, int rotation) {
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        Entry entry = getOrCreateEntry(displayInfo);
        entry.mUserRotationMode = rotationMode;
        entry.mUserRotation = rotation;
        writeSettingsIfNeeded(entry, displayInfo);
    }

    /* access modifiers changed from: package-private */
    public void setForcedSize(DisplayContent displayContent, int width, int height) {
        String sizeString;
        if (displayContent.isDefaultDisplay) {
            if (width == 0 || height == 0) {
                sizeString = "";
            } else {
                sizeString = width + "," + height;
            }
            Settings.Global.putString(this.mService.mContext.getContentResolver(), "display_size_forced", sizeString);
            return;
        }
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        Entry entry = getOrCreateEntry(displayInfo);
        entry.mForcedWidth = width;
        entry.mForcedHeight = height;
        writeSettingsIfNeeded(entry, displayInfo);
    }

    /* access modifiers changed from: package-private */
    public void setForcedDensity(DisplayContent displayContent, int density, int userId) {
        if (displayContent.isDefaultDisplay) {
            Settings.Secure.putStringForUser(this.mService.mContext.getContentResolver(), "display_density_forced", density == 0 ? "" : Integer.toString(density), userId);
            return;
        }
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        Entry entry = getOrCreateEntry(displayInfo);
        entry.mForcedDensity = density;
        writeSettingsIfNeeded(entry, displayInfo);
    }

    /* access modifiers changed from: package-private */
    public void setForcedScalingMode(DisplayContent displayContent, int mode) {
        if (displayContent.isDefaultDisplay) {
            Settings.Global.putInt(this.mService.mContext.getContentResolver(), "display_scaling_force", mode);
            return;
        }
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        Entry entry = getOrCreateEntry(displayInfo);
        entry.mForcedScalingMode = mode;
        writeSettingsIfNeeded(entry, displayInfo);
    }

    /* access modifiers changed from: package-private */
    public void setFixedToUserRotation(DisplayContent displayContent, int fixedToUserRotation) {
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        Entry entry = getOrCreateEntry(displayInfo);
        entry.mFixedToUserRotation = fixedToUserRotation;
        writeSettingsIfNeeded(entry, displayInfo);
    }

    private int getWindowingModeLocked(Entry entry, int displayId) {
        int windowingMode;
        boolean forceDesktopMode = false;
        if (entry != null) {
            windowingMode = entry.mWindowingMode;
        } else {
            windowingMode = 0;
        }
        int windowingMode2 = 5;
        if (windowingMode == 5 && !this.mService.mSupportsFreeformWindowManagement) {
            return 1;
        }
        if (windowingMode != 0) {
            return windowingMode;
        }
        if (this.mService.mForceDesktopModeOnExternalDisplays && displayId != 0) {
            forceDesktopMode = true;
        }
        if (!this.mService.mSupportsFreeformWindowManagement || (!this.mService.mIsPc && !forceDesktopMode)) {
            windowingMode2 = 1;
        }
        return windowingMode2;
    }

    /* access modifiers changed from: package-private */
    public int getWindowingModeLocked(DisplayContent dc) {
        return getWindowingModeLocked(getEntry(dc.getDisplayInfo()), dc.getDisplayId());
    }

    /* access modifiers changed from: package-private */
    public void setWindowingModeLocked(DisplayContent dc, int mode) {
        DisplayInfo displayInfo = dc.getDisplayInfo();
        Entry entry = getOrCreateEntry(displayInfo);
        entry.mWindowingMode = mode;
        dc.setWindowingMode(mode);
        writeSettingsIfNeeded(entry, displayInfo);
    }

    /* access modifiers changed from: package-private */
    public int getRemoveContentModeLocked(DisplayContent dc) {
        Entry entry = getEntry(dc.getDisplayInfo());
        if (entry != null && entry.mRemoveContentMode != 0) {
            return entry.mRemoveContentMode;
        }
        if (dc.isPrivate()) {
            return 2;
        }
        return 1;
    }

    /* access modifiers changed from: package-private */
    public void setRemoveContentModeLocked(DisplayContent dc, int mode) {
        DisplayInfo displayInfo = dc.getDisplayInfo();
        Entry entry = getOrCreateEntry(displayInfo);
        entry.mRemoveContentMode = mode;
        writeSettingsIfNeeded(entry, displayInfo);
    }

    /* access modifiers changed from: package-private */
    public boolean shouldShowWithInsecureKeyguardLocked(DisplayContent dc) {
        Entry entry = getEntry(dc.getDisplayInfo());
        if (entry == null) {
            return false;
        }
        return entry.mShouldShowWithInsecureKeyguard;
    }

    /* access modifiers changed from: package-private */
    public void setShouldShowWithInsecureKeyguardLocked(DisplayContent dc, boolean shouldShow) {
        if (dc.isPrivate() || !shouldShow) {
            DisplayInfo displayInfo = dc.getDisplayInfo();
            Entry entry = getOrCreateEntry(displayInfo);
            entry.mShouldShowWithInsecureKeyguard = shouldShow;
            writeSettingsIfNeeded(entry, displayInfo);
            return;
        }
        Slog.e(TAG, "Public display can't be allowed to show content when locked");
    }

    /* access modifiers changed from: package-private */
    public boolean shouldShowSystemDecorsLocked(DisplayContent dc) {
        if (dc.getDisplayId() == 0) {
            return true;
        }
        Entry entry = getEntry(dc.getDisplayInfo());
        if (entry == null) {
            return false;
        }
        return entry.mShouldShowSystemDecors;
    }

    /* access modifiers changed from: package-private */
    public void setShouldShowSystemDecorsLocked(DisplayContent dc, boolean shouldShow) {
        if (dc.getDisplayId() != 0 || shouldShow) {
            DisplayInfo displayInfo = dc.getDisplayInfo();
            Entry entry = getOrCreateEntry(displayInfo);
            entry.mShouldShowSystemDecors = shouldShow;
            writeSettingsIfNeeded(entry, displayInfo);
            return;
        }
        Slog.e(TAG, "Default display should show system decors");
    }

    /* access modifiers changed from: package-private */
    public boolean shouldShowImeLocked(DisplayContent dc) {
        if (dc.getDisplayId() == 0) {
            return true;
        }
        Entry entry = getEntry(dc.getDisplayInfo());
        if (entry == null) {
            return false;
        }
        return entry.mShouldShowIme;
    }

    /* access modifiers changed from: package-private */
    public void setShouldShowImeLocked(DisplayContent dc, boolean shouldShow) {
        if (dc.getDisplayId() != 0 || shouldShow) {
            DisplayInfo displayInfo = dc.getDisplayInfo();
            Entry entry = getOrCreateEntry(displayInfo);
            entry.mShouldShowIme = shouldShow;
            writeSettingsIfNeeded(entry, displayInfo);
            return;
        }
        Slog.e(TAG, "Default display should show IME");
    }

    /* access modifiers changed from: package-private */
    public void applySettingsToDisplayLocked(DisplayContent dc) {
        DisplayInfo displayInfo = dc.getDisplayInfo();
        Entry entry = getOrCreateEntry(displayInfo);
        dc.setWindowingMode(getWindowingModeLocked(entry, dc.getDisplayId()));
        displayInfo.overscanLeft = entry.mOverscanLeft;
        displayInfo.overscanTop = entry.mOverscanTop;
        displayInfo.overscanRight = entry.mOverscanRight;
        displayInfo.overscanBottom = entry.mOverscanBottom;
        dc.getDisplayRotation().restoreSettings(entry.mUserRotationMode, entry.mUserRotation, entry.mFixedToUserRotation);
        if (entry.mForcedDensity != 0) {
            dc.mBaseDisplayDensity = entry.mForcedDensity;
        }
        if (!(entry.mForcedWidth == 0 || entry.mForcedHeight == 0)) {
            dc.updateBaseDisplayMetrics(entry.mForcedWidth, entry.mForcedHeight, dc.mBaseDisplayDensity);
        }
        boolean z = true;
        if (entry.mForcedScalingMode != 1) {
            z = false;
        }
        dc.mDisplayScalingDisabled = z;
    }

    /* access modifiers changed from: package-private */
    public boolean updateSettingsForDisplay(DisplayContent dc) {
        if (dc.getWindowingMode() == getWindowingModeLocked(dc)) {
            return false;
        }
        dc.setWindowingMode(getWindowingModeLocked(dc));
        return true;
    }

    private void readSettings() {
        InputStream stream;
        XmlPullParser parser;
        int type;
        try {
            stream = this.mStorage.openRead();
            try {
                parser = Xml.newPullParser();
                parser.setInput(stream, StandardCharsets.UTF_8.name());
            } catch (IllegalStateException e) {
                Slog.w(TAG, "Failed parsing " + e);
                if (0 == 0) {
                    this.mEntries.clear();
                }
                stream.close();
                return;
            } catch (NullPointerException e2) {
                Slog.w(TAG, "Failed parsing " + e2);
                if (0 == 0) {
                    this.mEntries.clear();
                }
                stream.close();
                return;
            } catch (NumberFormatException e3) {
                Slog.w(TAG, "Failed parsing " + e3);
                if (0 == 0) {
                    this.mEntries.clear();
                }
                stream.close();
                return;
            } catch (XmlPullParserException e4) {
                Slog.w(TAG, "Failed parsing " + e4);
                if (0 == 0) {
                    this.mEntries.clear();
                }
                stream.close();
                return;
            } catch (IOException e5) {
                Slog.w(TAG, "Failed parsing " + e5);
                if (0 == 0) {
                    this.mEntries.clear();
                }
                stream.close();
                return;
            } catch (IndexOutOfBoundsException e6) {
                Slog.w(TAG, "Failed parsing " + e6);
                if (0 == 0) {
                    this.mEntries.clear();
                }
                stream.close();
                return;
            } catch (Throwable th) {
                if (0 == 0) {
                    this.mEntries.clear();
                }
                try {
                    stream.close();
                } catch (IOException e7) {
                }
                throw th;
            }
        } catch (IOException e8) {
            Slog.i(TAG, "No existing display settings, starting empty");
            return;
        }
        while (true) {
            type = parser.next();
            if (type == 2 || type == 1) {
                break;
            }
        }
        if (type == 2) {
            int outerDepth = parser.getDepth();
            while (true) {
                int type2 = parser.next();
                if (type2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                    break;
                } else if (type2 != 3) {
                    if (type2 != 4) {
                        String tagName = parser.getName();
                        if (tagName.equals("display")) {
                            readDisplay(parser);
                        } else if (tagName.equals("config")) {
                            readConfig(parser);
                        } else {
                            Slog.w(TAG, "Unknown element under <display-settings>: " + parser.getName());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                }
            }
            if (1 == 0) {
                this.mEntries.clear();
            }
            try {
                stream.close();
            } catch (IOException e9) {
            }
        } else {
            throw new IllegalStateException("no start tag found");
        }
    }

    private int getIntAttribute(XmlPullParser parser, String name) {
        return getIntAttribute(parser, name, 0);
    }

    private int getIntAttribute(XmlPullParser parser, String name, int defaultValue) {
        try {
            String str = parser.getAttributeValue(null, name);
            return str != null ? Integer.parseInt(str) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean getBooleanAttribute(XmlPullParser parser, String name) {
        return getBooleanAttribute(parser, name, false);
    }

    private boolean getBooleanAttribute(XmlPullParser parser, String name, boolean defaultValue) {
        try {
            String str = parser.getAttributeValue(null, name);
            return str != null ? Boolean.parseBoolean(str) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void readDisplay(XmlPullParser parser) throws NumberFormatException, XmlPullParserException, IOException {
        String name = parser.getAttributeValue(null, "name");
        if (name != null) {
            Entry entry = new Entry(name);
            entry.mOverscanLeft = getIntAttribute(parser, "overscanLeft");
            entry.mOverscanTop = getIntAttribute(parser, "overscanTop");
            entry.mOverscanRight = getIntAttribute(parser, "overscanRight");
            entry.mOverscanBottom = getIntAttribute(parser, "overscanBottom");
            entry.mWindowingMode = getIntAttribute(parser, "windowingMode", 0);
            entry.mUserRotationMode = getIntAttribute(parser, "userRotationMode", 0);
            entry.mUserRotation = getIntAttribute(parser, "userRotation", 0);
            entry.mForcedWidth = getIntAttribute(parser, "forcedWidth");
            entry.mForcedHeight = getIntAttribute(parser, "forcedHeight");
            entry.mForcedDensity = getIntAttribute(parser, "forcedDensity");
            entry.mForcedScalingMode = getIntAttribute(parser, "forcedScalingMode", 0);
            entry.mRemoveContentMode = getIntAttribute(parser, "removeContentMode", 0);
            entry.mShouldShowWithInsecureKeyguard = getBooleanAttribute(parser, "shouldShowWithInsecureKeyguard");
            entry.mShouldShowSystemDecors = getBooleanAttribute(parser, "shouldShowSystemDecors");
            entry.mShouldShowIme = getBooleanAttribute(parser, "shouldShowIme");
            entry.mFixedToUserRotation = getIntAttribute(parser, "fixedToUserRotation");
            this.mEntries.put(name, entry);
        }
        XmlUtils.skipCurrentTag(parser);
    }

    private void readConfig(XmlPullParser parser) throws NumberFormatException, XmlPullParserException, IOException {
        this.mIdentifier = getIntAttribute(parser, "identifier");
        XmlUtils.skipCurrentTag(parser);
    }

    private void writeSettingsIfNeeded(Entry changedEntry, DisplayInfo displayInfo) {
        if (!changedEntry.isEmpty() || removeEntry(displayInfo)) {
            this.mEntries.put(getIdentifier(displayInfo), changedEntry);
            writeSettings();
        }
    }

    private void writeSettings() {
        try {
            OutputStream stream = this.mStorage.startWrite();
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(stream, StandardCharsets.UTF_8.name());
                out.startDocument(null, true);
                out.startTag(null, "display-settings");
                out.startTag(null, "config");
                out.attribute(null, "identifier", Integer.toString(this.mIdentifier));
                out.endTag(null, "config");
                for (Entry entry : this.mEntries.values()) {
                    out.startTag(null, "display");
                    out.attribute(null, "name", entry.mName);
                    if (entry.mOverscanLeft != 0) {
                        out.attribute(null, "overscanLeft", Integer.toString(entry.mOverscanLeft));
                    }
                    if (entry.mOverscanTop != 0) {
                        out.attribute(null, "overscanTop", Integer.toString(entry.mOverscanTop));
                    }
                    if (entry.mOverscanRight != 0) {
                        out.attribute(null, "overscanRight", Integer.toString(entry.mOverscanRight));
                    }
                    if (entry.mOverscanBottom != 0) {
                        out.attribute(null, "overscanBottom", Integer.toString(entry.mOverscanBottom));
                    }
                    if (entry.mWindowingMode != 0) {
                        out.attribute(null, "windowingMode", Integer.toString(entry.mWindowingMode));
                    }
                    if (entry.mUserRotationMode != 0) {
                        out.attribute(null, "userRotationMode", Integer.toString(entry.mUserRotationMode));
                    }
                    if (entry.mUserRotation != 0) {
                        out.attribute(null, "userRotation", Integer.toString(entry.mUserRotation));
                    }
                    if (!(entry.mForcedWidth == 0 || entry.mForcedHeight == 0)) {
                        out.attribute(null, "forcedWidth", Integer.toString(entry.mForcedWidth));
                        out.attribute(null, "forcedHeight", Integer.toString(entry.mForcedHeight));
                    }
                    if (entry.mForcedDensity != 0) {
                        out.attribute(null, "forcedDensity", Integer.toString(entry.mForcedDensity));
                    }
                    if (entry.mForcedScalingMode != 0) {
                        out.attribute(null, "forcedScalingMode", Integer.toString(entry.mForcedScalingMode));
                    }
                    if (entry.mRemoveContentMode != 0) {
                        out.attribute(null, "removeContentMode", Integer.toString(entry.mRemoveContentMode));
                    }
                    if (entry.mShouldShowWithInsecureKeyguard) {
                        out.attribute(null, "shouldShowWithInsecureKeyguard", Boolean.toString(entry.mShouldShowWithInsecureKeyguard));
                    }
                    if (entry.mShouldShowSystemDecors) {
                        out.attribute(null, "shouldShowSystemDecors", Boolean.toString(entry.mShouldShowSystemDecors));
                    }
                    if (entry.mShouldShowIme) {
                        out.attribute(null, "shouldShowIme", Boolean.toString(entry.mShouldShowIme));
                    }
                    if (entry.mFixedToUserRotation != 0) {
                        out.attribute(null, "fixedToUserRotation", Integer.toString(entry.mFixedToUserRotation));
                    }
                    out.endTag(null, "display");
                }
                out.endTag(null, "display-settings");
                out.endDocument();
                this.mStorage.finishWrite(stream, true);
            } catch (IOException e) {
                Slog.w(TAG, "Failed to write display window settings.", e);
                this.mStorage.finishWrite(stream, false);
            }
        } catch (IOException e2) {
            Slog.w(TAG, "Failed to write display settings: " + e2);
        }
    }

    private boolean removeEntry(DisplayInfo displayInfo) {
        boolean z = true;
        boolean removed = (this.mEntries.remove(getIdentifier(displayInfo)) != null) | (this.mEntries.remove(displayInfo.uniqueId) != null);
        if (this.mEntries.remove(displayInfo.name) == null) {
            z = false;
        }
        return removed | z;
    }

    private String getIdentifier(DisplayInfo displayInfo) {
        if (this.mIdentifier != 1 || displayInfo.address == null || !(displayInfo.address instanceof DisplayAddress.Physical)) {
            return displayInfo.uniqueId;
        }
        return "port:" + ((int) displayInfo.address.getPort());
    }

    private static class AtomicFileStorage implements SettingPersister {
        private final AtomicFile mAtomicFile = new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), "display_settings.xml"), "wm-displays");

        AtomicFileStorage() {
        }

        @Override // com.android.server.wm.DisplayWindowSettings.SettingPersister
        public InputStream openRead() throws FileNotFoundException {
            return this.mAtomicFile.openRead();
        }

        @Override // com.android.server.wm.DisplayWindowSettings.SettingPersister
        public OutputStream startWrite() throws IOException {
            return this.mAtomicFile.startWrite();
        }

        @Override // com.android.server.wm.DisplayWindowSettings.SettingPersister
        public void finishWrite(OutputStream os, boolean success) {
            if (os instanceof FileOutputStream) {
                FileOutputStream fos = (FileOutputStream) os;
                if (success) {
                    this.mAtomicFile.finishWrite(fos);
                } else {
                    this.mAtomicFile.failWrite(fos);
                }
            } else {
                throw new IllegalArgumentException("Unexpected OutputStream as argument: " + os);
            }
        }
    }
}

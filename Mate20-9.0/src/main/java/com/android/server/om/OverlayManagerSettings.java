package com.android.server.om;

import android.content.om.OverlayInfo;
import android.hwtheme.HwThemeManager;
import android.util.ArrayMap;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.XmlUtils;
import com.android.server.om.OverlayManagerSettings;
import com.android.server.pm.PackageManagerService;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

final class OverlayManagerSettings {
    public static final String FWK_DARK_OVERLAY_TAG = "com.android.frameworkhwext.overlay.dark";
    public static final String FWK_DARK_TAG = "com.android.frameworkhwext.dark";
    public static final String FWK_EMULATION_NARROW_TAG = "com.android.internal.display.cutout.emulation.narrow";
    public static final String FWK_EMULATION_TALL_TAG = "com.android.internal.display.cutout.emulation.tall";
    public static final String FWK_EMULATION_WIDE_TAG = "com.android.internal.display.cutout.emulation.wide";
    public static final String FWK_HONOR_TAG = "com.android.frameworkhwext.honor";
    public static final String FWK_NOVA_TAG = "com.android.frameworkhwext.nova";
    private final ArrayList<SettingsItem> mItems = new ArrayList<>();

    static final class BadKeyException extends RuntimeException {
        BadKeyException(String packageName, int userId) {
            super("Bad key mPackageName=" + packageName + " mUserId=" + userId);
        }
    }

    private static final class Serializer {
        private static final String ATTR_BASE_CODE_PATH = "baseCodePath";
        private static final String ATTR_CATEGORY = "category";
        private static final String ATTR_IS_ENABLED = "isEnabled";
        private static final String ATTR_IS_STATIC = "isStatic";
        private static final String ATTR_PACKAGE_NAME = "packageName";
        private static final String ATTR_PRIORITY = "priority";
        private static final String ATTR_STATE = "state";
        private static final String ATTR_TARGET_PACKAGE_NAME = "targetPackageName";
        private static final String ATTR_USER_ID = "userId";
        private static final String ATTR_VERSION = "version";
        private static final int CURRENT_VERSION = 3;
        private static final String TAG_ITEM = "item";
        private static final String TAG_OVERLAYS = "overlays";

        private Serializer() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0057, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x005b, code lost:
            if (r1 != null) goto L_0x005d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
            r0.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0061, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x0062, code lost:
            r1.addSuppressed(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x0066, code lost:
            r0.close();
         */
        public static void restore(ArrayList<SettingsItem> table, InputStream is) throws IOException, XmlPullParserException {
            InputStreamReader reader = new InputStreamReader(is);
            table.clear();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(reader);
            XmlUtils.beginDocument(parser, TAG_OVERLAYS);
            int version = XmlUtils.readIntAttribute(parser, ATTR_VERSION);
            if (version != 3) {
                upgrade(version);
            }
            int depth = parser.getDepth();
            while (XmlUtils.nextElementWithin(parser, depth)) {
                String name = parser.getName();
                char c = 65535;
                if (name.hashCode() == 3242771) {
                    if (name.equals("item")) {
                        c = 0;
                    }
                }
                if (c == 0) {
                    table.add(restoreRow(parser, depth + 1));
                }
            }
            reader.close();
            return;
            throw th;
        }

        private static void upgrade(int oldVersion) throws XmlPullParserException {
            switch (oldVersion) {
                case 0:
                case 1:
                case 2:
                    throw new XmlPullParserException("old version " + oldVersion + "; ignoring");
                default:
                    throw new XmlPullParserException("unrecognized version " + oldVersion);
            }
        }

        private static SettingsItem restoreRow(XmlPullParser parser, int depth) throws IOException {
            XmlPullParser xmlPullParser = parser;
            SettingsItem settingsItem = new SettingsItem(XmlUtils.readStringAttribute(xmlPullParser, "packageName"), XmlUtils.readIntAttribute(xmlPullParser, ATTR_USER_ID), XmlUtils.readStringAttribute(xmlPullParser, ATTR_TARGET_PACKAGE_NAME), XmlUtils.readStringAttribute(xmlPullParser, ATTR_BASE_CODE_PATH), XmlUtils.readIntAttribute(xmlPullParser, "state"), XmlUtils.readBooleanAttribute(xmlPullParser, ATTR_IS_ENABLED), XmlUtils.readBooleanAttribute(xmlPullParser, ATTR_IS_STATIC), XmlUtils.readIntAttribute(xmlPullParser, ATTR_PRIORITY), XmlUtils.readStringAttribute(xmlPullParser, ATTR_CATEGORY));
            return settingsItem;
        }

        public static void persist(ArrayList<SettingsItem> table, OutputStream os) throws IOException, XmlPullParserException {
            FastXmlSerializer xml = new FastXmlSerializer();
            xml.setOutput(os, "utf-8");
            xml.startDocument(null, true);
            xml.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            xml.startTag(null, TAG_OVERLAYS);
            XmlUtils.writeIntAttribute(xml, ATTR_VERSION, 3);
            int N = table.size();
            for (int i = 0; i < N; i++) {
                persistRow(xml, table.get(i));
            }
            xml.endTag(null, TAG_OVERLAYS);
            xml.endDocument();
        }

        private static void persistRow(FastXmlSerializer xml, SettingsItem item) throws IOException {
            xml.startTag(null, "item");
            XmlUtils.writeStringAttribute(xml, "packageName", item.mPackageName);
            XmlUtils.writeIntAttribute(xml, ATTR_USER_ID, item.mUserId);
            XmlUtils.writeStringAttribute(xml, ATTR_TARGET_PACKAGE_NAME, item.mTargetPackageName);
            XmlUtils.writeStringAttribute(xml, ATTR_BASE_CODE_PATH, item.mBaseCodePath);
            XmlUtils.writeIntAttribute(xml, "state", item.mState);
            XmlUtils.writeBooleanAttribute(xml, ATTR_IS_ENABLED, item.mIsEnabled);
            XmlUtils.writeBooleanAttribute(xml, ATTR_IS_STATIC, item.mIsStatic);
            XmlUtils.writeIntAttribute(xml, ATTR_PRIORITY, item.mPriority);
            XmlUtils.writeStringAttribute(xml, ATTR_CATEGORY, item.mCategory);
            xml.endTag(null, "item");
        }
    }

    private static final class SettingsItem {
        /* access modifiers changed from: private */
        public String mBaseCodePath;
        private OverlayInfo mCache;
        /* access modifiers changed from: private */
        public String mCategory;
        /* access modifiers changed from: private */
        public boolean mIsEnabled;
        /* access modifiers changed from: private */
        public boolean mIsStatic;
        /* access modifiers changed from: private */
        public final String mPackageName;
        /* access modifiers changed from: private */
        public int mPriority;
        /* access modifiers changed from: private */
        public int mState;
        /* access modifiers changed from: private */
        public final String mTargetPackageName;
        /* access modifiers changed from: private */
        public final int mUserId;

        SettingsItem(String packageName, int userId, String targetPackageName, String baseCodePath, int state, boolean isEnabled, boolean isStatic, int priority, String category) {
            this.mPackageName = packageName;
            this.mUserId = userId;
            this.mTargetPackageName = targetPackageName;
            this.mBaseCodePath = baseCodePath;
            this.mState = state;
            boolean enable = isEnabled;
            if (OverlayManagerSettings.FWK_HONOR_TAG.equals(packageName) && HwThemeManager.isHonorProduct() && !HwThemeManager.isDeepDarkTheme(userId)) {
                enable = true;
            }
            if (OverlayManagerSettings.FWK_NOVA_TAG.equals(packageName) && HwThemeManager.isNovaProduct() && !HwThemeManager.isDeepDarkTheme(userId)) {
                enable = true;
            }
            if (OverlayManagerSettings.FWK_DARK_TAG.equals(packageName) && HwThemeManager.isDeepDarkTheme(userId)) {
                enable = true;
            }
            this.mIsEnabled = enable || isStatic;
            this.mCategory = category;
            this.mCache = null;
            this.mIsStatic = isStatic;
            this.mPriority = priority;
        }

        SettingsItem(String packageName, int userId, String targetPackageName, String baseCodePath, boolean isStatic, int priority, String category) {
            this(packageName, userId, targetPackageName, baseCodePath, -1, false, isStatic, priority, category);
        }

        /* access modifiers changed from: private */
        public String getTargetPackageName() {
            return this.mTargetPackageName;
        }

        /* access modifiers changed from: private */
        public int getUserId() {
            return this.mUserId;
        }

        /* access modifiers changed from: private */
        public String getBaseCodePath() {
            return this.mBaseCodePath;
        }

        /* access modifiers changed from: private */
        public boolean setBaseCodePath(String path) {
            if (this.mBaseCodePath.equals(path)) {
                return false;
            }
            this.mBaseCodePath = path;
            invalidateCache();
            return true;
        }

        /* access modifiers changed from: private */
        public int getState() {
            return this.mState;
        }

        /* access modifiers changed from: private */
        public boolean setState(int state) {
            if (this.mState == state) {
                return false;
            }
            this.mState = state;
            invalidateCache();
            return true;
        }

        /* access modifiers changed from: private */
        public boolean isEnabled() {
            return this.mIsEnabled;
        }

        /* access modifiers changed from: private */
        public boolean setEnabled(boolean enable) {
            if (this.mIsStatic || this.mIsEnabled == enable) {
                return false;
            }
            this.mIsEnabled = enable;
            invalidateCache();
            return true;
        }

        /* access modifiers changed from: private */
        public boolean setCategory(String category) {
            if (Objects.equals(this.mCategory, category)) {
                return false;
            }
            this.mCategory = category.intern();
            invalidateCache();
            return true;
        }

        /* access modifiers changed from: private */
        public OverlayInfo getOverlayInfo() {
            if (this.mCache == null) {
                OverlayInfo overlayInfo = new OverlayInfo(this.mPackageName, this.mTargetPackageName, this.mCategory, this.mBaseCodePath, this.mState, this.mUserId, this.mPriority, this.mIsStatic);
                this.mCache = overlayInfo;
            }
            return this.mCache;
        }

        private void invalidateCache() {
            this.mCache = null;
        }

        /* access modifiers changed from: private */
        public boolean isStatic() {
            return this.mIsStatic;
        }

        private int getPriority() {
            return this.mPriority;
        }
    }

    /* renamed from: lambda$bXuJGR0fITXNwGnQfQHv9KS-XgY  reason: not valid java name */
    public static /* synthetic */ ArrayMap m10lambda$bXuJGR0fITXNwGnQfQHv9KSXgY() {
        return new ArrayMap();
    }

    OverlayManagerSettings() {
    }

    /* access modifiers changed from: package-private */
    public void init(String packageName, int userId, String targetPackageName, String baseCodePath, boolean isStatic, int priority, String overlayCategory) {
        int i;
        remove(packageName, userId);
        SettingsItem item = new SettingsItem(packageName, userId, targetPackageName, baseCodePath, isStatic, priority, overlayCategory);
        if (isStatic) {
            boolean unused = item.setEnabled(true);
            int i2 = this.mItems.size() - 1;
            while (true) {
                i = i2;
                if (i < 0) {
                    int i3 = priority;
                    break;
                }
                SettingsItem parentItem = this.mItems.get(i);
                if (!parentItem.mIsStatic) {
                    int i4 = priority;
                } else if (parentItem.mPriority <= priority) {
                    break;
                }
                i2 = i - 1;
            }
            int pos = i + 1;
            if (pos == this.mItems.size()) {
                this.mItems.add(item);
            } else {
                this.mItems.add(pos, item);
            }
        } else {
            int i5 = priority;
            this.mItems.add(item);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean remove(String packageName, int userId) {
        int idx = select(packageName, userId);
        if (idx < 0) {
            return false;
        }
        this.mItems.remove(idx);
        return true;
    }

    /* access modifiers changed from: package-private */
    public OverlayInfo getOverlayInfo(String packageName, int userId) throws BadKeyException {
        int idx = select(packageName, userId);
        if (idx >= 0) {
            return this.mItems.get(idx).getOverlayInfo();
        }
        throw new BadKeyException(packageName, userId);
    }

    /* access modifiers changed from: package-private */
    public boolean setBaseCodePath(String packageName, int userId, String path) throws BadKeyException {
        int idx = select(packageName, userId);
        if (idx >= 0) {
            return this.mItems.get(idx).setBaseCodePath(path);
        }
        throw new BadKeyException(packageName, userId);
    }

    /* access modifiers changed from: package-private */
    public boolean setCategory(String packageName, int userId, String category) throws BadKeyException {
        int idx = select(packageName, userId);
        if (idx >= 0) {
            return this.mItems.get(idx).setCategory(category);
        }
        throw new BadKeyException(packageName, userId);
    }

    /* access modifiers changed from: package-private */
    public boolean getEnabled(String packageName, int userId) throws BadKeyException {
        int idx = select(packageName, userId);
        if (idx >= 0) {
            return this.mItems.get(idx).isEnabled();
        }
        throw new BadKeyException(packageName, userId);
    }

    /* access modifiers changed from: package-private */
    public boolean setEnabled(String packageName, int userId, boolean enable) throws BadKeyException {
        int idx = select(packageName, userId);
        if (idx >= 0) {
            return this.mItems.get(idx).setEnabled(enable);
        }
        throw new BadKeyException(packageName, userId);
    }

    /* access modifiers changed from: package-private */
    public int getState(String packageName, int userId) throws BadKeyException {
        int idx = select(packageName, userId);
        if (idx >= 0) {
            return this.mItems.get(idx).getState();
        }
        throw new BadKeyException(packageName, userId);
    }

    /* access modifiers changed from: package-private */
    public boolean setState(String packageName, int userId, int state) throws BadKeyException {
        int idx = select(packageName, userId);
        if (idx >= 0) {
            return this.mItems.get(idx).setState(state);
        }
        throw new BadKeyException(packageName, userId);
    }

    /* access modifiers changed from: package-private */
    public List<OverlayInfo> getOverlaysForTarget(String targetPackageName, int userId) {
        return (List) selectWhereTarget(targetPackageName, userId).filter($$Lambda$OverlayManagerSettings$ATr0DZmWpSWdKD0COw4t2qSDRk.INSTANCE).map($$Lambda$OverlayManagerSettings$WYtPK6Ebqjgxm8_8Cotijv_z_8.INSTANCE).collect(Collectors.toList());
    }

    static /* synthetic */ boolean lambda$getOverlaysForTarget$0(SettingsItem i) {
        return !i.isStatic() || !PackageManagerService.PLATFORM_PACKAGE_NAME.equals(i.getTargetPackageName());
    }

    /* access modifiers changed from: package-private */
    public ArrayMap<String, List<OverlayInfo>> getOverlaysForUser(int userId) {
        return (ArrayMap) selectWhereUser(userId).filter($$Lambda$OverlayManagerSettings$IkswmT9ZZJXmNAztGRVrD3hODMw.INSTANCE).map($$Lambda$OverlayManagerSettings$jZUujzDxrP0hpAqUxnqEfbnQc.INSTANCE).collect(Collectors.groupingBy($$Lambda$OverlayManagerSettings$sx0Nyvq91kCH_A4Ctf09G_0u9M.INSTANCE, $$Lambda$OverlayManagerSettings$bXuJGR0fITXNwGnQfQHv9KSXgY.INSTANCE, Collectors.toList()));
    }

    static /* synthetic */ boolean lambda$getOverlaysForUser$2(SettingsItem i) {
        return !i.isStatic() || !PackageManagerService.PLATFORM_PACKAGE_NAME.equals(i.getTargetPackageName());
    }

    /* access modifiers changed from: package-private */
    public int[] getUsers() {
        return this.mItems.stream().mapToInt($$Lambda$OverlayManagerSettings$vXm2C4y9QF5yYZNimBLr6woI.INSTANCE).distinct().toArray();
    }

    /* access modifiers changed from: package-private */
    public boolean removeUser(int userId) {
        boolean removed = false;
        int i = 0;
        while (i < this.mItems.size()) {
            if (this.mItems.get(i).getUserId() == userId) {
                this.mItems.remove(i);
                removed = true;
                i--;
            }
            i++;
        }
        return removed;
    }

    /* access modifiers changed from: package-private */
    public boolean setPriority(String packageName, String newParentPackageName, int userId) {
        boolean z = false;
        if (packageName.equals(newParentPackageName)) {
            return false;
        }
        int moveIdx = select(packageName, userId);
        if (moveIdx < 0) {
            return false;
        }
        int parentIdx = select(newParentPackageName, userId);
        if (parentIdx < 0) {
            return false;
        }
        SettingsItem itemToMove = this.mItems.get(moveIdx);
        if (!itemToMove.getTargetPackageName().equals(this.mItems.get(parentIdx).getTargetPackageName())) {
            return false;
        }
        this.mItems.remove(moveIdx);
        int newParentIdx = select(newParentPackageName, userId) + 1;
        this.mItems.add(newParentIdx, itemToMove);
        if (moveIdx != newParentIdx) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean setLowestPriority(String packageName, int userId) {
        int idx = select(packageName, userId);
        if (idx <= 0) {
            return false;
        }
        SettingsItem item = this.mItems.get(idx);
        this.mItems.remove(item);
        this.mItems.add(0, item);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setHighestPriority(String packageName, int userId) {
        int idx = select(packageName, userId);
        if (idx < 0 || idx == this.mItems.size() - 1) {
            return false;
        }
        this.mItems.remove(idx);
        this.mItems.add(this.mItems.get(idx));
        return true;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter p) {
        IndentingPrintWriter pw = new IndentingPrintWriter(p, "  ");
        pw.println("Settings");
        pw.increaseIndent();
        if (this.mItems.isEmpty()) {
            pw.println("<none>");
            return;
        }
        int N = this.mItems.size();
        for (int i = 0; i < N; i++) {
            SettingsItem item = this.mItems.get(i);
            pw.println(item.mPackageName + ":" + item.getUserId() + " {");
            pw.increaseIndent();
            pw.print("mPackageName.......: ");
            pw.println(item.mPackageName);
            pw.print("mUserId............: ");
            pw.println(item.getUserId());
            pw.print("mTargetPackageName.: ");
            pw.println(item.getTargetPackageName());
            pw.print("mBaseCodePath......: ");
            pw.println(item.getBaseCodePath());
            pw.print("mState.............: ");
            pw.println(OverlayInfo.stateToString(item.getState()));
            pw.print("mIsEnabled.........: ");
            pw.println(item.isEnabled());
            pw.print("mIsStatic..........: ");
            pw.println(item.isStatic());
            pw.print("mPriority..........: ");
            pw.println(item.mPriority);
            pw.print("mCategory..........: ");
            pw.println(item.mCategory);
            pw.decreaseIndent();
            pw.println("}");
        }
    }

    /* access modifiers changed from: package-private */
    public void restore(InputStream is) throws IOException, XmlPullParserException {
        Serializer.restore(this.mItems, is);
    }

    /* access modifiers changed from: package-private */
    public void persist(OutputStream os) throws IOException, XmlPullParserException {
        Serializer.persist(this.mItems, os);
    }

    private int select(String packageName, int userId) {
        int N = this.mItems.size();
        for (int i = 0; i < N; i++) {
            SettingsItem item = this.mItems.get(i);
            if (item.mUserId == userId && item.mPackageName.equals(packageName)) {
                return i;
            }
        }
        return -1;
    }

    static /* synthetic */ boolean lambda$selectWhereUser$6(int userId, SettingsItem item) {
        return item.mUserId == userId;
    }

    private Stream<SettingsItem> selectWhereUser(int userId) {
        return this.mItems.stream().filter(new Predicate(userId) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return OverlayManagerSettings.lambda$selectWhereUser$6(this.f$0, (OverlayManagerSettings.SettingsItem) obj);
            }
        });
    }

    private Stream<SettingsItem> selectWhereTarget(String targetPackageName, int userId) {
        return selectWhereUser(userId).filter(new Predicate(targetPackageName) {
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return ((OverlayManagerSettings.SettingsItem) obj).getTargetPackageName().equals(this.f$0);
            }
        });
    }
}

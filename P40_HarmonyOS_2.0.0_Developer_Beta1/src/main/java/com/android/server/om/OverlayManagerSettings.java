package com.android.server.om;

import android.content.om.OverlayInfo;
import android.hwtheme.HwThemeManager;
import android.util.ArrayMap;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* access modifiers changed from: package-private */
public final class OverlayManagerSettings {
    public static final String FWK_EMULATION_NARROW_TAG = "com.android.internal.display.cutout.emulation.narrow";
    public static final String FWK_EMULATION_TALL_TAG = "com.android.internal.display.cutout.emulation.tall";
    public static final String FWK_EMULATION_WIDE_TAG = "com.android.internal.display.cutout.emulation.wide";
    public static final String FWK_HONOR_TAG = "com.huawei.frameworkhwext.honor";
    private final ArrayList<SettingsItem> mItems = new ArrayList<>();

    OverlayManagerSettings() {
    }

    /* access modifiers changed from: package-private */
    public void init(String packageName, int userId, String targetPackageName, String targetOverlayableName, String baseCodePath, boolean isStatic, int priority, String overlayCategory) {
        remove(packageName, userId);
        SettingsItem item = new SettingsItem(packageName, userId, targetPackageName, targetOverlayableName, baseCodePath, isStatic, priority, overlayCategory);
        if (isStatic) {
            item.setEnabled(true);
            int i = this.mItems.size() - 1;
            while (true) {
                if (i < 0) {
                    break;
                }
                SettingsItem parentItem = this.mItems.get(i);
                if (parentItem.mIsStatic) {
                    if (parentItem.mPriority <= priority) {
                        break;
                    }
                }
                i--;
            }
            int pos = i + 1;
            if (pos == this.mItems.size()) {
                this.mItems.add(item);
            } else {
                this.mItems.add(pos, item);
            }
        } else {
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
        return (ArrayMap) selectWhereUser(userId).filter($$Lambda$OverlayManagerSettings$IkswmT9ZZJXmNAztGRVrD3hODMw.INSTANCE).map($$Lambda$OverlayManagerSettings$jZUujzDxrP0hpAqUxnqEfbnQc.INSTANCE).collect(Collectors.groupingBy($$Lambda$OverlayManagerSettings$sx0Nyvq91kCH_A4Ctf09G_0u9M.INSTANCE, $$Lambda$bXuJGR0fITXNwGnQfQHv9KSXgY.INSTANCE, Collectors.toList()));
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
        int moveIdx;
        int parentIdx;
        if (packageName.equals(newParentPackageName) || (moveIdx = select(packageName, userId)) < 0 || (parentIdx = select(newParentPackageName, userId)) < 0) {
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
            return true;
        }
        return false;
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
    public void dump(PrintWriter p, DumpState dumpState) {
        Stream<SettingsItem> items = this.mItems.stream();
        if (dumpState.getUserId() != -1) {
            items = items.filter(new Predicate() {
                /* class com.android.server.om.$$Lambda$OverlayManagerSettings$n3zAcJx5VlITl9U9fQatqN2KJyA */

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return OverlayManagerSettings.lambda$dump$6(DumpState.this, (OverlayManagerSettings.SettingsItem) obj);
                }
            });
        }
        if (dumpState.getPackageName() != null) {
            items = items.filter(new Predicate() {
                /* class com.android.server.om.$$Lambda$OverlayManagerSettings$leWA95COTthWNYtDKcdKVChlcc */

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return ((OverlayManagerSettings.SettingsItem) obj).mPackageName.equals(DumpState.this.getPackageName());
                }
            });
        }
        IndentingPrintWriter pw = new IndentingPrintWriter(p, "  ");
        if (dumpState.getField() != null) {
            items.forEach(new Consumer(pw, dumpState) {
                /* class com.android.server.om.$$Lambda$OverlayManagerSettings$BKNCDt6MBH2RSKr2mbIUnL_dIvA */
                private final /* synthetic */ IndentingPrintWriter f$1;
                private final /* synthetic */ DumpState f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    OverlayManagerSettings.this.lambda$dump$8$OverlayManagerSettings(this.f$1, this.f$2, (OverlayManagerSettings.SettingsItem) obj);
                }
            });
        } else {
            items.forEach(new Consumer(pw) {
                /* class com.android.server.om.$$Lambda$OverlayManagerSettings$Xr3l7ivgTflBmPTqf9hbG3i0H_I */
                private final /* synthetic */ IndentingPrintWriter f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    OverlayManagerSettings.this.lambda$dump$9$OverlayManagerSettings(this.f$1, (OverlayManagerSettings.SettingsItem) obj);
                }
            });
        }
    }

    static /* synthetic */ boolean lambda$dump$6(DumpState dumpState, SettingsItem item) {
        return item.mUserId == dumpState.getUserId();
    }

    public /* synthetic */ void lambda$dump$8$OverlayManagerSettings(IndentingPrintWriter pw, DumpState dumpState, SettingsItem item) {
        dumpSettingsItemField(pw, item, dumpState.getField());
    }

    /* access modifiers changed from: private */
    /* renamed from: dumpSettingsItem */
    public void lambda$dump$9$OverlayManagerSettings(IndentingPrintWriter pw, SettingsItem item) {
        pw.println(item.mPackageName + ":" + item.getUserId() + " {");
        pw.increaseIndent();
        StringBuilder sb = new StringBuilder();
        sb.append("mPackageName...........: ");
        sb.append(item.mPackageName);
        pw.println(sb.toString());
        pw.println("mUserId................: " + item.getUserId());
        pw.println("mTargetPackageName.....: " + item.getTargetPackageName());
        pw.println("mTargetOverlayableName.: " + item.getTargetOverlayableName());
        pw.println("mBaseCodePath..........: " + item.getBaseCodePath());
        pw.println("mState.................: " + OverlayInfo.stateToString(item.getState()));
        pw.println("mIsEnabled.............: " + item.isEnabled());
        pw.println("mIsStatic..............: " + item.isStatic());
        pw.println("mPriority..............: " + item.mPriority);
        pw.println("mCategory..............: " + item.mCategory);
        pw.decreaseIndent();
        pw.println("}");
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void dumpSettingsItemField(IndentingPrintWriter pw, SettingsItem item, String field) {
        char c;
        switch (field.hashCode()) {
            case -1750736508:
                if (field.equals("targetoverlayablename")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1248283232:
                if (field.equals("targetpackagename")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1165461084:
                if (field.equals("priority")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -836029914:
                if (field.equals("userid")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 50511102:
                if (field.equals("category")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 109757585:
                if (field.equals("state")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 440941271:
                if (field.equals("isenabled")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 697685016:
                if (field.equals("isstatic")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 909712337:
                if (field.equals("packagename")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1693907299:
                if (field.equals("basecodepath")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                pw.println(item.mPackageName);
                return;
            case 1:
                pw.println(item.mUserId);
                return;
            case 2:
                pw.println(item.mTargetPackageName);
                return;
            case 3:
                pw.println(item.mTargetOverlayableName);
                return;
            case 4:
                pw.println(item.mBaseCodePath);
                return;
            case 5:
                pw.println(OverlayInfo.stateToString(item.mState));
                return;
            case 6:
                pw.println(item.mIsEnabled);
                return;
            case 7:
                pw.println(item.mIsStatic);
                return;
            case '\b':
                pw.println(item.mPriority);
                return;
            case '\t':
                pw.println(item.mCategory);
                return;
            default:
                return;
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

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static final class Serializer {
        private static final String ATTR_BASE_CODE_PATH = "baseCodePath";
        private static final String ATTR_CATEGORY = "category";
        private static final String ATTR_IS_ENABLED = "isEnabled";
        private static final String ATTR_IS_STATIC = "isStatic";
        private static final String ATTR_PACKAGE_NAME = "packageName";
        private static final String ATTR_PRIORITY = "priority";
        private static final String ATTR_STATE = "state";
        private static final String ATTR_TARGET_OVERLAYABLE_NAME = "targetOverlayableName";
        private static final String ATTR_TARGET_PACKAGE_NAME = "targetPackageName";
        private static final String ATTR_USER_ID = "userId";
        private static final String ATTR_VERSION = "version";
        @VisibleForTesting
        static final int CURRENT_VERSION = 3;
        private static final String TAG_ITEM = "item";
        private static final String TAG_OVERLAYS = "overlays";

        Serializer() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0058, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
            r0.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x005d, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x005e, code lost:
            r1.addSuppressed(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x0061, code lost:
            throw r2;
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
                if (name.hashCode() == 3242771 && name.equals("item")) {
                    c = 0;
                }
                if (c == 0) {
                    table.add(restoreRow(parser, depth + 1));
                }
            }
            reader.close();
        }

        private static void upgrade(int oldVersion) throws XmlPullParserException {
            if (oldVersion == 0 || oldVersion == 1 || oldVersion == 2) {
                throw new XmlPullParserException("old version " + oldVersion + "; ignoring");
            }
            throw new XmlPullParserException("unrecognized version " + oldVersion);
        }

        private static SettingsItem restoreRow(XmlPullParser parser, int depth) throws IOException {
            return new SettingsItem(XmlUtils.readStringAttribute(parser, ATTR_PACKAGE_NAME), XmlUtils.readIntAttribute(parser, ATTR_USER_ID), XmlUtils.readStringAttribute(parser, ATTR_TARGET_PACKAGE_NAME), XmlUtils.readStringAttribute(parser, ATTR_TARGET_OVERLAYABLE_NAME), XmlUtils.readStringAttribute(parser, ATTR_BASE_CODE_PATH), XmlUtils.readIntAttribute(parser, ATTR_STATE), XmlUtils.readBooleanAttribute(parser, ATTR_IS_ENABLED), XmlUtils.readBooleanAttribute(parser, ATTR_IS_STATIC), XmlUtils.readIntAttribute(parser, ATTR_PRIORITY), XmlUtils.readStringAttribute(parser, ATTR_CATEGORY));
        }

        public static void persist(ArrayList<SettingsItem> table, OutputStream os) throws IOException, XmlPullParserException {
            FastXmlSerializer xml = new FastXmlSerializer();
            xml.setOutput(os, "utf-8");
            xml.startDocument((String) null, true);
            xml.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            xml.startTag((String) null, TAG_OVERLAYS);
            XmlUtils.writeIntAttribute(xml, ATTR_VERSION, 3);
            int n = table.size();
            for (int i = 0; i < n; i++) {
                persistRow(xml, table.get(i));
            }
            xml.endTag((String) null, TAG_OVERLAYS);
            xml.endDocument();
        }

        private static void persistRow(FastXmlSerializer xml, SettingsItem item) throws IOException {
            xml.startTag((String) null, "item");
            XmlUtils.writeStringAttribute(xml, ATTR_PACKAGE_NAME, item.mPackageName);
            XmlUtils.writeIntAttribute(xml, ATTR_USER_ID, item.mUserId);
            XmlUtils.writeStringAttribute(xml, ATTR_TARGET_PACKAGE_NAME, item.mTargetPackageName);
            XmlUtils.writeStringAttribute(xml, ATTR_TARGET_OVERLAYABLE_NAME, item.mTargetOverlayableName);
            XmlUtils.writeStringAttribute(xml, ATTR_BASE_CODE_PATH, item.mBaseCodePath);
            XmlUtils.writeIntAttribute(xml, ATTR_STATE, item.mState);
            XmlUtils.writeBooleanAttribute(xml, ATTR_IS_ENABLED, item.mIsEnabled);
            XmlUtils.writeBooleanAttribute(xml, ATTR_IS_STATIC, item.mIsStatic);
            XmlUtils.writeIntAttribute(xml, ATTR_PRIORITY, item.mPriority);
            XmlUtils.writeStringAttribute(xml, ATTR_CATEGORY, item.mCategory);
            xml.endTag((String) null, "item");
        }
    }

    /* access modifiers changed from: private */
    public static final class SettingsItem {
        private String mBaseCodePath;
        private OverlayInfo mCache;
        private String mCategory;
        private boolean mIsEnabled;
        private boolean mIsStatic;
        private final String mPackageName;
        private int mPriority;
        private int mState;
        private final String mTargetOverlayableName;
        private final String mTargetPackageName;
        private final int mUserId;

        SettingsItem(String packageName, int userId, String targetPackageName, String targetOverlayableName, String baseCodePath, int state, boolean isEnabled, boolean isStatic, int priority, String category) {
            this.mPackageName = packageName;
            this.mUserId = userId;
            this.mTargetPackageName = targetPackageName;
            this.mTargetOverlayableName = targetOverlayableName;
            this.mBaseCodePath = baseCodePath;
            this.mState = state;
            boolean enable = isEnabled;
            if (OverlayManagerSettings.FWK_HONOR_TAG.equals(packageName) && HwThemeManager.isHonorProduct()) {
                enable = true;
            }
            this.mIsEnabled = enable || isStatic;
            this.mCategory = category;
            this.mCache = null;
            this.mIsStatic = isStatic;
            this.mPriority = priority;
        }

        SettingsItem(String packageName, int userId, String targetPackageName, String targetOverlayableName, String baseCodePath, boolean isStatic, int priority, String category) {
            this(packageName, userId, targetPackageName, targetOverlayableName, baseCodePath, -1, false, isStatic, priority, category);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getTargetPackageName() {
            return this.mTargetPackageName;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getTargetOverlayableName() {
            return this.mTargetOverlayableName;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        public int getUserId() {
            return this.mUserId;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getBaseCodePath() {
            return this.mBaseCodePath;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean setBaseCodePath(String path) {
            if (this.mBaseCodePath.equals(path)) {
                return false;
            }
            this.mBaseCodePath = path;
            invalidateCache();
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getState() {
            return this.mState;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean setState(int state) {
            if (this.mState == state) {
                return false;
            }
            this.mState = state;
            invalidateCache();
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isEnabled() {
            return this.mIsEnabled;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean setEnabled(boolean enable) {
            if (this.mIsStatic || this.mIsEnabled == enable) {
                return false;
            }
            this.mIsEnabled = enable;
            invalidateCache();
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean setCategory(String category) {
            if (Objects.equals(this.mCategory, category)) {
                return false;
            }
            this.mCategory = category == null ? null : category.intern();
            invalidateCache();
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        public OverlayInfo getOverlayInfo() {
            if (this.mCache == null) {
                this.mCache = new OverlayInfo(this.mPackageName, this.mTargetPackageName, this.mTargetOverlayableName, this.mCategory, this.mBaseCodePath, this.mState, this.mUserId, this.mPriority, this.mIsStatic);
            }
            return this.mCache;
        }

        private void invalidateCache() {
            this.mCache = null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isStatic() {
            return this.mIsStatic;
        }

        private int getPriority() {
            return this.mPriority;
        }
    }

    private int select(String packageName, int userId) {
        int n = this.mItems.size();
        for (int i = 0; i < n; i++) {
            SettingsItem item = this.mItems.get(i);
            if (item.mUserId == userId && item.mPackageName.equals(packageName)) {
                return i;
            }
        }
        return -1;
    }

    static /* synthetic */ boolean lambda$selectWhereUser$10(int userId, SettingsItem item) {
        return item.mUserId == userId;
    }

    private Stream<SettingsItem> selectWhereUser(int userId) {
        return this.mItems.stream().filter(new Predicate(userId) {
            /* class com.android.server.om.$$Lambda$OverlayManagerSettings$Fjt465P6G89HQZERZFsOEjMbtXI */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return OverlayManagerSettings.lambda$selectWhereUser$10(this.f$0, (OverlayManagerSettings.SettingsItem) obj);
            }
        });
    }

    private Stream<SettingsItem> selectWhereTarget(String targetPackageName, int userId) {
        return selectWhereUser(userId).filter(new Predicate(targetPackageName) {
            /* class com.android.server.om.$$Lambda$OverlayManagerSettings$L_Sj43p2Txm_KHwT0lseBTVzh8 */
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ((OverlayManagerSettings.SettingsItem) obj).getTargetPackageName().equals(this.f$0);
            }
        });
    }

    static final class BadKeyException extends RuntimeException {
        BadKeyException(String packageName, int userId) {
            super("Bad key mPackageName=" + packageName + " mUserId=" + userId);
        }
    }
}

package com.android.server.devicepolicy;

import android.content.ComponentName;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class TransferOwnershipMetadataManager {
    static final String ADMIN_TYPE_DEVICE_OWNER = "device-owner";
    static final String ADMIN_TYPE_PROFILE_OWNER = "profile-owner";
    public static final String OWNER_TRANSFER_METADATA_XML = "owner-transfer-metadata.xml";
    private static final String TAG = TransferOwnershipMetadataManager.class.getName();
    @VisibleForTesting
    static final String TAG_ADMIN_TYPE = "admin-type";
    @VisibleForTesting
    static final String TAG_SOURCE_COMPONENT = "source-component";
    @VisibleForTesting
    static final String TAG_TARGET_COMPONENT = "target-component";
    @VisibleForTesting
    static final String TAG_USER_ID = "user-id";
    private final Injector mInjector;

    @VisibleForTesting
    static class Injector {
        Injector() {
        }

        public File getOwnerTransferMetadataDir() {
            return Environment.getDataSystemDirectory();
        }
    }

    static class Metadata {
        final String adminType;
        final ComponentName sourceComponent;
        final ComponentName targetComponent;
        final int userId;

        Metadata(ComponentName sourceComponent2, ComponentName targetComponent2, int userId2, String adminType2) {
            this.sourceComponent = sourceComponent2;
            this.targetComponent = targetComponent2;
            Preconditions.checkNotNull(sourceComponent2);
            Preconditions.checkNotNull(targetComponent2);
            Preconditions.checkStringNotEmpty(adminType2);
            this.userId = userId2;
            this.adminType = adminType2;
        }

        Metadata(String flatSourceComponent, String flatTargetComponent, int userId2, String adminType2) {
            this(unflattenComponentUnchecked(flatSourceComponent), unflattenComponentUnchecked(flatTargetComponent), userId2, adminType2);
        }

        private static ComponentName unflattenComponentUnchecked(String flatComponent) {
            Preconditions.checkNotNull(flatComponent);
            return ComponentName.unflattenFromString(flatComponent);
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof Metadata)) {
                return false;
            }
            Metadata params = (Metadata) obj;
            if (this.userId == params.userId && this.sourceComponent.equals(params.sourceComponent) && this.targetComponent.equals(params.targetComponent) && TextUtils.equals(this.adminType, params.adminType)) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (31 * ((31 * ((31 * ((31 * 1) + this.userId)) + this.sourceComponent.hashCode())) + this.targetComponent.hashCode())) + this.adminType.hashCode();
        }
    }

    TransferOwnershipMetadataManager() {
        this(new Injector());
    }

    @VisibleForTesting
    TransferOwnershipMetadataManager(Injector injector) {
        this.mInjector = injector;
    }

    /* access modifiers changed from: package-private */
    public boolean saveMetadataFile(Metadata params) {
        File transferOwnershipMetadataFile = new File(this.mInjector.getOwnerTransferMetadataDir(), OWNER_TRANSFER_METADATA_XML);
        AtomicFile atomicFile = new AtomicFile(transferOwnershipMetadataFile);
        FileOutputStream stream = null;
        try {
            stream = atomicFile.startWrite();
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(stream, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, true);
            insertSimpleTag(serializer, TAG_USER_ID, Integer.toString(params.userId));
            insertSimpleTag(serializer, TAG_SOURCE_COMPONENT, params.sourceComponent.flattenToString());
            insertSimpleTag(serializer, TAG_TARGET_COMPONENT, params.targetComponent.flattenToString());
            insertSimpleTag(serializer, TAG_ADMIN_TYPE, params.adminType);
            serializer.endDocument();
            atomicFile.finishWrite(stream);
            return true;
        } catch (IOException e) {
            Slog.e(TAG, "Caught exception while trying to save Owner Transfer Params to file " + transferOwnershipMetadataFile, e);
            transferOwnershipMetadataFile.delete();
            atomicFile.failWrite(stream);
            return false;
        }
    }

    private void insertSimpleTag(XmlSerializer serializer, String tagName, String value) throws IOException {
        serializer.startTag(null, tagName);
        serializer.text(value);
        serializer.endTag(null, tagName);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0040, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0041, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0045, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0046, code lost:
        r6 = r4;
        r4 = r3;
        r3 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0058, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0059, code lost:
        android.util.Slog.e(TAG, "Caught exception while trying to load the owner transfer params from file " + r0, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006f, code lost:
        return null;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0058 A[ExcHandler: IOException | IllegalArgumentException | XmlPullParserException (r1v4 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:4:0x002c] */
    public Metadata loadMetadataFile() {
        FileInputStream stream;
        Throwable th;
        Throwable th2;
        File transferOwnershipMetadataFile = new File(this.mInjector.getOwnerTransferMetadataDir(), OWNER_TRANSFER_METADATA_XML);
        if (!transferOwnershipMetadataFile.exists()) {
            return null;
        }
        Slog.d(TAG, "Loading TransferOwnershipMetadataManager from " + transferOwnershipMetadataFile);
        try {
            stream = new FileInputStream(transferOwnershipMetadataFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            Metadata parseMetadataFile = parseMetadataFile(parser);
            stream.close();
            return parseMetadataFile;
        } catch (IOException | IllegalArgumentException | XmlPullParserException e) {
        } catch (Throwable th3) {
            th.addSuppressed(th3);
        }
        throw th2;
        if (th != null) {
            stream.close();
        } else {
            stream.close();
        }
        throw th2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0064, code lost:
        if (r8.equals(TAG_TARGET_COMPONENT) != false) goto L_0x0068;
     */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x006c  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0074  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x007c  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0084  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0008 A[SYNTHETIC] */
    private Metadata parseMetadataFile(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int userId = 0;
        String adminComponent = null;
        String targetComponent = null;
        String adminType = null;
        while (true) {
            int next = parser.next();
            int type = next;
            char c = 1;
            if (next != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (!(type == 3 || type == 4)) {
                    String name = parser.getName();
                    int hashCode = name.hashCode();
                    if (hashCode != -337219647) {
                        if (hashCode == -147180963) {
                            if (name.equals(TAG_USER_ID)) {
                                c = 0;
                                switch (c) {
                                    case 0:
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        break;
                                }
                            }
                        } else if (hashCode == 281362891) {
                            if (name.equals(TAG_SOURCE_COMPONENT)) {
                                c = 2;
                                switch (c) {
                                    case 0:
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        break;
                                }
                            }
                        } else if (hashCode == 641951480 && name.equals(TAG_ADMIN_TYPE)) {
                            c = 3;
                            switch (c) {
                                case 0:
                                    parser.next();
                                    userId = Integer.parseInt(parser.getText());
                                    break;
                                case 1:
                                    parser.next();
                                    targetComponent = parser.getText();
                                    break;
                                case 2:
                                    parser.next();
                                    adminComponent = parser.getText();
                                    break;
                                case 3:
                                    parser.next();
                                    adminType = parser.getText();
                                    break;
                            }
                        }
                    }
                    c = 65535;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            }
        }
        return new Metadata(adminComponent, targetComponent, userId, adminType);
    }

    /* access modifiers changed from: package-private */
    public void deleteMetadataFile() {
        new File(this.mInjector.getOwnerTransferMetadataDir(), OWNER_TRANSFER_METADATA_XML).delete();
    }

    /* access modifiers changed from: package-private */
    public boolean metadataFileExists() {
        return new File(this.mInjector.getOwnerTransferMetadataDir(), OWNER_TRANSFER_METADATA_XML).exists();
    }
}

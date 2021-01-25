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

/* access modifiers changed from: package-private */
public class TransferOwnershipMetadataManager {
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
            String str = TAG;
            Slog.e(str, "Caught exception while trying to save Owner Transfer Params to file " + transferOwnershipMetadataFile, e);
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
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0042, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0047, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0048, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004b, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004c, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004d, code lost:
        r3 = com.android.server.devicepolicy.TransferOwnershipMetadataManager.TAG;
        android.util.Slog.e(r3, "Caught exception while trying to load the owner transfer params from file " + r0, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0063, code lost:
        return null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x004c A[ExcHandler: IOException | IllegalArgumentException | XmlPullParserException (r1v5 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:6:0x003c] */
    public Metadata loadMetadataFile() {
        File transferOwnershipMetadataFile = new File(this.mInjector.getOwnerTransferMetadataDir(), OWNER_TRANSFER_METADATA_XML);
        if (!transferOwnershipMetadataFile.exists()) {
            return null;
        }
        String str = TAG;
        Slog.d(str, "Loading TransferOwnershipMetadataManager from " + transferOwnershipMetadataFile);
        FileInputStream stream = new FileInputStream(transferOwnershipMetadataFile);
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, null);
        Metadata parseMetadataFile = parseMetadataFile(parser);
        try {
            stream.close();
            return parseMetadataFile;
        } catch (IOException | IllegalArgumentException | XmlPullParserException e) {
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private Metadata parseMetadataFile(XmlPullParser parser) throws XmlPullParserException, IOException {
        boolean z;
        int outerDepth = parser.getDepth();
        int userId = 0;
        String adminComponent = null;
        String targetComponent = null;
        String adminType = null;
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (!(type == 3 || type == 4)) {
                    String name = parser.getName();
                    switch (name.hashCode()) {
                        case -337219647:
                            if (name.equals(TAG_TARGET_COMPONENT)) {
                                z = true;
                                break;
                            }
                            z = true;
                            break;
                        case -147180963:
                            if (name.equals(TAG_USER_ID)) {
                                z = false;
                                break;
                            }
                            z = true;
                            break;
                        case 281362891:
                            if (name.equals(TAG_SOURCE_COMPONENT)) {
                                z = true;
                                break;
                            }
                            z = true;
                            break;
                        case 641951480:
                            if (name.equals(TAG_ADMIN_TYPE)) {
                                z = true;
                                break;
                            }
                            z = true;
                            break;
                        default:
                            z = true;
                            break;
                    }
                    if (!z) {
                        parser.next();
                        try {
                            userId = Integer.parseInt(parser.getText());
                        } catch (NumberFormatException e) {
                            Slog.e(TAG, "NumberFormatException userId");
                        }
                    } else if (z) {
                        parser.next();
                        targetComponent = parser.getText();
                    } else if (z) {
                        parser.next();
                        adminComponent = parser.getText();
                    } else if (z) {
                        parser.next();
                        adminType = parser.getText();
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

    /* access modifiers changed from: package-private */
    public static class Metadata {
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
            if (!(obj instanceof Metadata)) {
                return false;
            }
            Metadata params = (Metadata) obj;
            if (this.userId != params.userId || !this.sourceComponent.equals(params.sourceComponent) || !this.targetComponent.equals(params.targetComponent) || !TextUtils.equals(this.adminType, params.adminType)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return (((((((1 * 31) + this.userId) * 31) + this.sourceComponent.hashCode()) * 31) + this.targetComponent.hashCode()) * 31) + this.adminType.hashCode();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class Injector {
        Injector() {
        }

        public File getOwnerTransferMetadataDir() {
            return Environment.getDataSystemDirectory();
        }
    }
}

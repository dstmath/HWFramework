package android.os.storage;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import android.provider.DocumentsContract;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.DebugUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Menu;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import java.io.CharArrayWriter;
import java.io.File;
import java.util.Comparator;
import java.util.Objects;

public class VolumeInfo implements Parcelable {
    public static final String ACTION_VOLUME_STATE_CHANGED = "android.os.storage.action.VOLUME_STATE_CHANGED";
    public static final Parcelable.Creator<VolumeInfo> CREATOR = new Parcelable.Creator<VolumeInfo>() {
        public VolumeInfo createFromParcel(Parcel in) {
            return new VolumeInfo(in);
        }

        public VolumeInfo[] newArray(int size) {
            return new VolumeInfo[size];
        }
    };
    private static final String DOCUMENT_AUTHORITY = "com.android.externalstorage.documents";
    private static final String DOCUMENT_ROOT_PRIMARY_EMULATED = "primary";
    public static final String EXTRA_VOLUME_ID = "android.os.storage.extra.VOLUME_ID";
    public static final String EXTRA_VOLUME_STATE = "android.os.storage.extra.VOLUME_STATE";
    public static final String ID_EMULATED_INTERNAL = "emulated";
    public static final String ID_PRIVATE_INTERNAL = "private";
    public static final int MOUNT_FLAG_FULLWRITE = 128;
    public static final int MOUNT_FLAG_PRIMARY = 1;
    public static final int MOUNT_FLAG_RO = 64;
    public static final int MOUNT_FLAG_VISIBLE = 2;
    public static final int STATE_BAD_REMOVAL = 8;
    public static final int STATE_BAD_SD = 25;
    public static final int STATE_CHECKING = 1;
    public static final int STATE_EJECTING = 5;
    public static final int STATE_FILESYSTEM_ERROR = 26;
    public static final int STATE_FORMATTING = 4;
    public static final int STATE_MOUNTED = 2;
    public static final int STATE_MOUNTED_READ_ONLY = 3;
    public static final int STATE_REMOVED = 7;
    public static final int STATE_UNMOUNTABLE = 6;
    public static final int STATE_UNMOUNTED = 0;
    public static final int STATE_VOLUME_LOWSPEED_SD = 24;
    public static final int STATE_VOLUME_LOWSPEED_SPEC_SD = 27;
    public static final int STATE_VOLUME_READERROR = 21;
    public static final int STATE_VOLUME_RO = 23;
    public static final int STATE_VOLUME_WRITEERROR = 22;
    private static final String TAG = "VolumeInfo";
    public static final int TYPE_ASEC = 3;
    public static final int TYPE_EMULATED = 2;
    public static final int TYPE_OBB = 4;
    public static final int TYPE_PRIVATE = 1;
    public static final int TYPE_PUBLIC = 0;
    private static final Comparator<VolumeInfo> sDescriptionComparator = new Comparator<VolumeInfo>() {
        public int compare(VolumeInfo lhs, VolumeInfo rhs) {
            if (VolumeInfo.ID_PRIVATE_INTERNAL.equals(lhs.getId())) {
                return -1;
            }
            if (lhs.getDescription() == null) {
                return 1;
            }
            if (rhs.getDescription() == null) {
                return -1;
            }
            return lhs.getDescription().compareTo(rhs.getDescription());
        }
    };
    private static ArrayMap<String, String> sEnvironmentToBroadcast = new ArrayMap<>();
    private static SparseIntArray sStateToDescrip = new SparseIntArray();
    private static SparseArray<String> sStateToEnvironment = new SparseArray<>();
    public int blockedUserId = -1;
    public final DiskInfo disk;
    public String fsLabel;
    public String fsType;
    public String fsUuid;
    public final String id;
    public String internalPath;
    public int mountFlags = 0;
    public int mountUserId = -1;
    public final String partGuid;
    public String path;
    public int state = 0;
    public final int type;

    static {
        sStateToEnvironment.put(0, Environment.MEDIA_UNMOUNTED);
        sStateToEnvironment.put(1, Environment.MEDIA_CHECKING);
        sStateToEnvironment.put(2, Environment.MEDIA_MOUNTED);
        sStateToEnvironment.put(3, Environment.MEDIA_MOUNTED_READ_ONLY);
        sStateToEnvironment.put(4, Environment.MEDIA_UNMOUNTED);
        sStateToEnvironment.put(5, Environment.MEDIA_EJECTING);
        sStateToEnvironment.put(6, Environment.MEDIA_UNMOUNTABLE);
        sStateToEnvironment.put(7, Environment.MEDIA_REMOVED);
        sStateToEnvironment.put(8, Environment.MEDIA_BAD_REMOVAL);
        sEnvironmentToBroadcast.put(Environment.MEDIA_UNMOUNTED, "android.intent.action.MEDIA_UNMOUNTED");
        sEnvironmentToBroadcast.put(Environment.MEDIA_CHECKING, "android.intent.action.MEDIA_CHECKING");
        sEnvironmentToBroadcast.put(Environment.MEDIA_MOUNTED, "android.intent.action.MEDIA_MOUNTED");
        sEnvironmentToBroadcast.put(Environment.MEDIA_MOUNTED_READ_ONLY, "android.intent.action.MEDIA_MOUNTED");
        sEnvironmentToBroadcast.put(Environment.MEDIA_EJECTING, "android.intent.action.MEDIA_EJECT");
        sEnvironmentToBroadcast.put(Environment.MEDIA_UNMOUNTABLE, "android.intent.action.MEDIA_UNMOUNTABLE");
        sEnvironmentToBroadcast.put(Environment.MEDIA_REMOVED, "android.intent.action.MEDIA_REMOVED");
        sEnvironmentToBroadcast.put(Environment.MEDIA_BAD_REMOVAL, "android.intent.action.MEDIA_BAD_REMOVAL");
        sStateToDescrip.put(0, 17040037);
        sStateToDescrip.put(1, 17040029);
        sStateToDescrip.put(2, 17040033);
        sStateToDescrip.put(3, 17040034);
        sStateToDescrip.put(4, 17040031);
        sStateToDescrip.put(5, 17040030);
        sStateToDescrip.put(6, 17040036);
        sStateToDescrip.put(7, 17040035);
        sStateToDescrip.put(8, 17040028);
    }

    public VolumeInfo(String id2, int type2, DiskInfo disk2, String partGuid2) {
        this.id = (String) Preconditions.checkNotNull(id2);
        this.type = type2;
        this.disk = disk2;
        this.partGuid = partGuid2;
    }

    public VolumeInfo(Parcel parcel) {
        this.id = parcel.readString();
        this.type = parcel.readInt();
        if (parcel.readInt() != 0) {
            this.disk = DiskInfo.CREATOR.createFromParcel(parcel);
        } else {
            this.disk = null;
        }
        this.partGuid = parcel.readString();
        this.mountFlags = parcel.readInt();
        this.mountUserId = parcel.readInt();
        this.blockedUserId = parcel.readInt();
        this.state = parcel.readInt();
        this.fsType = parcel.readString();
        this.fsUuid = parcel.readString();
        this.fsLabel = parcel.readString();
        this.path = parcel.readString();
        this.internalPath = parcel.readString();
    }

    public static String getEnvironmentForState(int state2) {
        String envState = sStateToEnvironment.get(state2);
        if (envState != null) {
            return envState;
        }
        return "unknown";
    }

    public static String getBroadcastForEnvironment(String envState) {
        return sEnvironmentToBroadcast.get(envState);
    }

    public static String getBroadcastForState(int state2) {
        return getBroadcastForEnvironment(getEnvironmentForState(state2));
    }

    public static Comparator<VolumeInfo> getDescriptionComparator() {
        return sDescriptionComparator;
    }

    public String getId() {
        return this.id;
    }

    public DiskInfo getDisk() {
        return this.disk;
    }

    public String getDiskId() {
        if (this.disk != null) {
            return this.disk.id;
        }
        return null;
    }

    public int getType() {
        return this.type;
    }

    public int getState() {
        return this.state;
    }

    public int getStateDescription() {
        return sStateToDescrip.get(this.state, 0);
    }

    public String getFsUuid() {
        return this.fsUuid;
    }

    public int getMountUserId() {
        return this.mountUserId;
    }

    public int getBlockedUserId() {
        return this.blockedUserId;
    }

    public String getDescription() {
        if (ID_PRIVATE_INTERNAL.equals(this.id) || ID_EMULATED_INTERNAL.equals(this.id)) {
            return Resources.getSystem().getString(17041215);
        }
        if (!TextUtils.isEmpty(this.fsLabel)) {
            return this.fsLabel;
        }
        return null;
    }

    public boolean isMountedReadable() {
        return this.state == 2 || this.state == 3;
    }

    public boolean isMountedWritable() {
        return this.state == 2;
    }

    public boolean isPrimary() {
        return (this.mountFlags & 1) != 0;
    }

    public boolean isPrimaryPhysical() {
        return isPrimary() && getType() == 0;
    }

    public boolean isVisible() {
        return (this.mountFlags & 2) != 0;
    }

    public boolean isVisibleForUser(int userId) {
        if (this.type == 0 && this.blockedUserId == userId) {
            return false;
        }
        if (this.type == 0 && this.mountUserId == userId) {
            return isVisible();
        }
        if (this.type == 2) {
            return isVisible();
        }
        return false;
    }

    public boolean isVisibleForRead(int userId) {
        return isVisibleForUser(userId);
    }

    public boolean isVisibleForWrite(int userId) {
        return isVisibleForUser(userId);
    }

    public File getPath() {
        if (this.path != null) {
            return new File(this.path);
        }
        return null;
    }

    public File getInternalPath() {
        if (this.internalPath != null) {
            return new File(this.internalPath);
        }
        return null;
    }

    public File getPathForUser(int userId) {
        if (this.path == null) {
            return null;
        }
        if (this.type == 0) {
            return new File(this.path);
        }
        if (this.type == 2) {
            return new File(this.path, Integer.toString(userId));
        }
        return null;
    }

    public File getInternalPathForUser(int userId) {
        if (this.type != 0) {
            return getPathForUser(userId);
        }
        if (this.path == null) {
            return new File("/dev/null");
        }
        return new File(this.path.replace("/storage/", "/mnt/media_rw/"));
    }

    public StorageVolume buildStorageVolume(Context context, int userId, boolean reportUnmounted) {
        String derivedFsUuid;
        long maxFileSize;
        boolean emulated;
        boolean removable;
        Context context2 = context;
        int i = userId;
        StorageManager storage = (StorageManager) context2.getSystemService(StorageManager.class);
        String envState = reportUnmounted ? Environment.MEDIA_UNMOUNTED : getEnvironmentForState(this.state);
        File userPath = getPathForUser(i);
        if (userPath == null) {
            userPath = new File("/dev/null");
            Slog.i(TAG, "storage volume is /dev/null ");
        }
        File internalPath2 = getInternalPathForUser(i);
        if (internalPath2 == null) {
            internalPath2 = new File("/dev/null");
        }
        File internalPath3 = internalPath2;
        String description = null;
        String derivedFsUuid2 = this.fsUuid;
        long maxFileSize2 = 0;
        if (this.type == 2) {
            VolumeInfo privateVol = storage.findPrivateForEmulated(this);
            if (privateVol != null) {
                description = storage.getBestVolumeDescription(privateVol);
                derivedFsUuid2 = privateVol.fsUuid;
            }
            if (ID_EMULATED_INTERNAL.equals(this.id)) {
                removable = false;
            } else {
                removable = true;
            }
            boolean z = removable;
            derivedFsUuid = derivedFsUuid2;
            maxFileSize = 0;
            emulated = true;
        } else if (this.type == 0) {
            description = storage.getBestVolumeDescription(this);
            if ("vfat".equals(this.fsType)) {
                maxFileSize2 = 4294967295L;
            }
            derivedFsUuid = derivedFsUuid2;
            maxFileSize = maxFileSize2;
            emulated = false;
            removable = true;
        } else {
            throw new IllegalStateException("Unexpected volume type " + this.type);
        }
        if (description == null) {
            description = context2.getString(SubscriptionManager.DEFAULT_NAME_RES);
        }
        StorageVolume storageVolume = new StorageVolume(this.id, userPath, internalPath3, description, isPrimary(), removable, emulated, false, maxFileSize, new UserHandle(i), derivedFsUuid, envState);
        return storageVolume;
    }

    public static int buildStableMtpStorageId(String fsUuid2) {
        if (TextUtils.isEmpty(fsUuid2)) {
            return 0;
        }
        int hash = 0;
        for (int i = 0; i < fsUuid2.length(); i++) {
            hash = (31 * hash) + fsUuid2.charAt(i);
        }
        int hash2 = ((hash << 16) ^ hash) & Menu.CATEGORY_MASK;
        if (hash2 == 0) {
            hash2 = 131072;
        }
        if (hash2 == 65536) {
            hash2 = 131072;
        }
        if (hash2 == -65536) {
            hash2 = -131072;
        }
        return hash2 | 1;
    }

    public Intent buildBrowseIntent() {
        return buildBrowseIntentForUser(UserHandle.myUserId());
    }

    public Intent buildBrowseIntentForUser(int userId) {
        Uri uri;
        if (this.type == 0 && this.mountUserId == userId) {
            uri = DocumentsContract.buildRootUri("com.android.externalstorage.documents", this.fsUuid);
        } else if (this.type != 2 || !isPrimary()) {
            return null;
        } else {
            uri = DocumentsContract.buildRootUri("com.android.externalstorage.documents", DOCUMENT_ROOT_PRIMARY_EMULATED);
        }
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(uri, DocumentsContract.Root.MIME_TYPE_ITEM);
        intent.putExtra(DocumentsContract.EXTRA_SHOW_ADVANCED, isPrimary());
        return intent;
    }

    public String toString() {
        CharArrayWriter writer = new CharArrayWriter();
        dump(new IndentingPrintWriter(writer, "    ", 80));
        return writer.toString();
    }

    public void dump(IndentingPrintWriter pw) {
        pw.println("VolumeInfo{" + this.id + "}:");
        pw.increaseIndent();
        pw.printPair("type", DebugUtils.valueToString(getClass(), "TYPE_", this.type));
        pw.printPair("diskId", getDiskId());
        pw.printPair("partGuid", this.partGuid);
        pw.printPair("mountFlags", DebugUtils.flagsToString(getClass(), "MOUNT_FLAG_", this.mountFlags));
        pw.printPair("mountUserId", Integer.valueOf(this.mountUserId));
        pw.printPair("blockedUserId", Integer.valueOf(this.blockedUserId));
        pw.printPair("state", DebugUtils.valueToString(getClass(), "STATE_", this.state));
        pw.println();
        pw.printPair("fsType", this.fsType);
        pw.printPair("fsUuid", this.fsUuid);
        pw.printPair("fsLabel", this.fsLabel);
        pw.println();
        pw.printPair("path", this.path);
        pw.printPair("internalPath", this.internalPath);
        pw.decreaseIndent();
        pw.println();
    }

    public VolumeInfo clone() {
        Parcel temp = Parcel.obtain();
        try {
            writeToParcel(temp, 0);
            temp.setDataPosition(0);
            return CREATOR.createFromParcel(temp);
        } finally {
            temp.recycle();
        }
    }

    public boolean equals(Object o) {
        if (o instanceof VolumeInfo) {
            return Objects.equals(this.id, ((VolumeInfo) o).id);
        }
        return false;
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.id);
        parcel.writeInt(this.type);
        if (this.disk != null) {
            parcel.writeInt(1);
            this.disk.writeToParcel(parcel, flags);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeString(this.partGuid);
        parcel.writeInt(this.mountFlags);
        parcel.writeInt(this.mountUserId);
        parcel.writeInt(this.blockedUserId);
        parcel.writeInt(this.state);
        parcel.writeString(this.fsType);
        parcel.writeString(this.fsUuid);
        parcel.writeString(this.fsLabel);
        parcel.writeString(this.path);
        parcel.writeString(this.internalPath);
    }
}

package ohos.miscservices.download;

import java.util.HashMap;
import java.util.Map;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Environment;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

public class DownloadConfig implements Sequenceable {
    public static final int NETWORK_MOBILE = 1;
    public static final int NETWORK_WIFI = 2;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "DownloadConfig");
    private String description;
    private boolean downloadInCharging;
    private boolean downloadInIdle;
    private Uri downloadStoragePath;
    private Map<String, String> httpHeaders;
    private boolean meteredAllowed;
    private int networkRestriction;
    private String remoteDeviceId;
    private boolean roamingAllowed;
    private boolean showNotification;
    private Uri targetUri;
    private String title;

    /* access modifiers changed from: package-private */
    public void setRemoteDeviceId(String str) {
        this.remoteDeviceId = str;
    }

    /* access modifiers changed from: package-private */
    public Uri getDownloadUri() {
        Uri uri = this.targetUri;
        return uri != null ? Uri.parse(uri.toString()) : uri;
    }

    /* access modifiers changed from: package-private */
    public Uri getDownloadStoragePath() {
        Uri uri = this.downloadStoragePath;
        return uri != null ? Uri.parse(uri.toString()) : uri;
    }

    /* access modifiers changed from: package-private */
    public Map<String, String> getHttpHeaders() {
        HashMap hashMap = new HashMap();
        hashMap.putAll(this.httpHeaders);
        return hashMap;
    }

    /* access modifiers changed from: package-private */
    public String getTitle() {
        return this.title;
    }

    /* access modifiers changed from: package-private */
    public String getDescription() {
        return this.description;
    }

    /* access modifiers changed from: package-private */
    public int getNetworkRestriction() {
        return this.networkRestriction;
    }

    /* access modifiers changed from: package-private */
    public boolean isRoamingAllowed() {
        return this.roamingAllowed;
    }

    /* access modifiers changed from: package-private */
    public boolean isMeteredAllowed() {
        return this.meteredAllowed;
    }

    /* access modifiers changed from: package-private */
    public boolean isRequiresCharging() {
        return this.downloadInCharging;
    }

    /* access modifiers changed from: package-private */
    public boolean isShowNotify() {
        return this.showNotification;
    }

    /* access modifiers changed from: package-private */
    public boolean isDownloadInIdle() {
        return this.downloadInIdle;
    }

    public String getRemoteDeviceId() {
        return this.remoteDeviceId;
    }

    private DownloadConfig(Builder builder) {
        this.roamingAllowed = true;
        this.meteredAllowed = true;
        this.targetUri = builder.targetUri;
        this.downloadStoragePath = builder.downloadStoragePath;
        this.httpHeaders = new HashMap();
        for (Map.Entry<String, Object> entry : builder.httpHeaders.getAll().entrySet()) {
            this.httpHeaders.put(entry.getKey(), entry.getValue().toString());
        }
        this.title = builder.title;
        this.description = builder.description;
        this.networkRestriction = builder.networkRestriction;
        this.showNotification = builder.showNotification;
        this.downloadInIdle = builder.downloadInIdle;
        this.downloadInCharging = builder.downloadInCharging;
        this.roamingAllowed = builder.roamingAllowed;
        this.meteredAllowed = builder.meteredAllowed;
        this.remoteDeviceId = builder.remoteDeviceId;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            HiLog.error(TAG, "wrong input", new Object[0]);
            return false;
        }
        parcel.writeSequenceable(this.targetUri);
        parcel.writeSequenceable(this.downloadStoragePath);
        parcel.writeMap(this.httpHeaders);
        if (!parcel.writeString(this.title)) {
            HiLog.error(TAG, "write title wrong", new Object[0]);
            return false;
        } else if (!parcel.writeString(this.description)) {
            HiLog.error(TAG, "write description wrong", new Object[0]);
            return false;
        } else if (!parcel.writeInt(this.networkRestriction)) {
            HiLog.error(TAG, "write network restriction wrong", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.roamingAllowed)) {
            HiLog.error(TAG, "write roaming allowed wrong", new Object[0]);
            return false;
        } else if (!parcel.writeBoolean(this.meteredAllowed)) {
            HiLog.error(TAG, "write metered allowed wrong", new Object[0]);
            return false;
        } else if (parcel.writeString(this.remoteDeviceId)) {
            return true;
        } else {
            HiLog.error(TAG, "write remote device Id wrong", new Object[0]);
            return false;
        }
    }

    public static DownloadConfig readFromParcel(Parcel parcel, Context context) {
        if (parcel == null || context == null) {
            HiLog.error(TAG, "wrong input", new Object[0]);
            return null;
        }
        Uri readFromParcel = Uri.readFromParcel(parcel);
        Builder builder = new Builder(context, readFromParcel);
        String uri = readFromParcel.toString();
        builder.setPath(Environment.DIRECTORY_DOWNLOAD, uri.length() > 0 ? uri.substring(uri.lastIndexOf("/") + 1) : "");
        String[] readStringArray = parcel.readStringArray();
        String[] readStringArray2 = parcel.readStringArray();
        for (int i = 0; i < readStringArray.length; i++) {
            if (readStringArray2.length > i) {
                builder.addHttpheader(readStringArray[i], readStringArray2[i]);
            }
        }
        builder.setTitle(parcel.readString()).setDescription(parcel.readString()).setNetworkRestriction(parcel.readInt()).enableRoaming(parcel.readBoolean()).enableMetered(parcel.readBoolean()).setRemoteDeviceId(parcel.readString());
        return builder.build();
    }

    /* JADX DEBUG: Type inference failed for r0v2. Raw type applied. Possible types: java.util.Map<?, ?>, java.util.Map<java.lang.String, java.lang.String> */
    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.targetUri = Uri.readFromParcel(parcel);
        this.downloadStoragePath = Uri.readFromParcel(parcel);
        this.httpHeaders = parcel.readMap();
        this.title = parcel.readString();
        this.description = parcel.readString();
        this.networkRestriction = parcel.readInt();
        this.roamingAllowed = parcel.readBoolean();
        this.meteredAllowed = parcel.readBoolean();
        this.remoteDeviceId = parcel.readString();
        return true;
    }

    public static class Builder {
        private Context context;
        private String description;
        private boolean downloadInCharging;
        private boolean downloadInIdle;
        private Uri downloadStoragePath;
        private PacMap httpHeaders;
        private boolean meteredAllowed = true;
        private int networkRestriction;
        private String remoteDeviceId;
        private boolean roamingAllowed = true;
        private boolean showNotification;
        private Uri targetUri;
        private String title;

        public Builder(Context context2, Uri uri) {
            if (context2 != null) {
                this.context = context2;
                setDownloadUri(uri);
                this.downloadStoragePath = getDefaultDownloadStoragePath(uri);
                this.httpHeaders = new PacMap();
                this.title = "";
                this.description = "";
                this.networkRestriction = -1;
                this.downloadInCharging = false;
                this.roamingAllowed = true;
                this.meteredAllowed = true;
                this.showNotification = true;
                this.downloadInIdle = false;
                return;
            }
            throw new IllegalArgumentException("context cannot be null");
        }

        private Uri getDefaultDownloadStoragePath(Uri uri) {
            String uri2 = uri.toString();
            return DownloadUtils.createDownloadPathInPrivateDir(this.context, Environment.DIRECTORY_DOWNLOAD, uri2.length() > 0 ? uri2.substring(uri2.lastIndexOf("/") + 1) : "");
        }

        private void setDownloadUri(Uri uri) {
            if (uri != null) {
                String scheme = uri.getScheme();
                if (scheme == null) {
                    throw new IllegalArgumentException("Can't find scheme Only support http/https uris");
                } else if (scheme.equals("http") || scheme.equals("https")) {
                    this.targetUri = uri;
                } else {
                    throw new IllegalArgumentException("Must be http/https uris");
                }
            } else {
                throw new NullPointerException();
            }
        }

        /* access modifiers changed from: package-private */
        public Builder setDownloadPathInAppPrivateDir(Context context2, String str, String str2) {
            this.downloadStoragePath = DownloadUtils.createDownloadPathInPrivateDir(context2, str, str2);
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setDownloadPathInPublicDir(String str, String str2) {
            this.downloadStoragePath = DownloadUtils.createDownloadPathInPublicDir(str, str2);
            return this;
        }

        public Builder setPath(String str, String str2) {
            if (str == null) {
                return setDownloadPathInAppPrivateDir(this.context, "", str2);
            }
            return setDownloadPathInPublicDir(str, str2);
        }

        public Builder addHttpheader(String str, String str2) {
            if (str == null) {
                throw new NullPointerException("http header cannot be null");
            } else if (!str.contains(":")) {
                if (str2 == null) {
                    str2 = "";
                }
                this.httpHeaders.putObjectValue(str, str2);
                return this;
            } else {
                throw new IllegalArgumentException("http header may not contain ':'");
            }
        }

        public Builder setTitle(String str) {
            this.title = str;
            return this;
        }

        public Builder setDescription(String str) {
            this.description = str;
            return this;
        }

        public Builder setNetworkRestriction(int i) {
            this.networkRestriction = i;
            return this;
        }

        public Builder enableRoaming(boolean z) {
            this.roamingAllowed = z;
            return this;
        }

        public Builder enableMetered(boolean z) {
            this.meteredAllowed = z;
            return this;
        }

        public Builder setRemoteDeviceId(String str) {
            this.remoteDeviceId = str;
            return this;
        }

        public DownloadConfig build() {
            return new DownloadConfig(this);
        }
    }
}

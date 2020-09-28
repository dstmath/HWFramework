package android.media.tv;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.HdmiUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.xmlpull.v1.XmlPullParserException;

public final class TvInputInfo implements Parcelable {
    public static final Parcelable.Creator<TvInputInfo> CREATOR = new Parcelable.Creator<TvInputInfo>() {
        /* class android.media.tv.TvInputInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TvInputInfo createFromParcel(Parcel in) {
            return new TvInputInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public TvInputInfo[] newArray(int size) {
            return new TvInputInfo[size];
        }
    };
    private static final boolean DEBUG = false;
    public static final String EXTRA_INPUT_ID = "android.media.tv.extra.INPUT_ID";
    private static final String TAG = "TvInputInfo";
    public static final int TYPE_COMPONENT = 1004;
    public static final int TYPE_COMPOSITE = 1001;
    public static final int TYPE_DISPLAY_PORT = 1008;
    public static final int TYPE_DVI = 1006;
    public static final int TYPE_HDMI = 1007;
    public static final int TYPE_OTHER = 1000;
    public static final int TYPE_SCART = 1003;
    public static final int TYPE_SVIDEO = 1002;
    public static final int TYPE_TUNER = 0;
    public static final int TYPE_VGA = 1005;
    private final boolean mCanRecord;
    private final Bundle mExtras;
    private final int mHdmiConnectionRelativePosition;
    private final HdmiDeviceInfo mHdmiDeviceInfo;
    private final Icon mIcon;
    private final Icon mIconDisconnected;
    private final Icon mIconStandby;
    private Uri mIconUri;
    private final String mId;
    private final boolean mIsConnectedToHdmiSwitch;
    private final boolean mIsHardwareInput;
    private final CharSequence mLabel;
    private final int mLabelResId;
    private final String mParentId;
    private final ResolveInfo mService;
    private final String mSetupActivity;
    private final int mTunerCount;
    private final int mType;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    @SystemApi
    @Deprecated
    public static TvInputInfo createTvInputInfo(Context context, ResolveInfo service, HdmiDeviceInfo hdmiDeviceInfo, String parentId, String label, Uri iconUri) throws XmlPullParserException, IOException {
        TvInputInfo info = new Builder(context, service).setHdmiDeviceInfo(hdmiDeviceInfo).setParentId(parentId).setLabel(label).build();
        info.mIconUri = iconUri;
        return info;
    }

    @SystemApi
    @Deprecated
    public static TvInputInfo createTvInputInfo(Context context, ResolveInfo service, HdmiDeviceInfo hdmiDeviceInfo, String parentId, int labelRes, Icon icon) throws XmlPullParserException, IOException {
        return new Builder(context, service).setHdmiDeviceInfo(hdmiDeviceInfo).setParentId(parentId).setLabel(labelRes).setIcon(icon).build();
    }

    @SystemApi
    @Deprecated
    public static TvInputInfo createTvInputInfo(Context context, ResolveInfo service, TvInputHardwareInfo hardwareInfo, String label, Uri iconUri) throws XmlPullParserException, IOException {
        TvInputInfo info = new Builder(context, service).setTvInputHardwareInfo(hardwareInfo).setLabel(label).build();
        info.mIconUri = iconUri;
        return info;
    }

    @SystemApi
    @Deprecated
    public static TvInputInfo createTvInputInfo(Context context, ResolveInfo service, TvInputHardwareInfo hardwareInfo, int labelRes, Icon icon) throws XmlPullParserException, IOException {
        return new Builder(context, service).setTvInputHardwareInfo(hardwareInfo).setLabel(labelRes).setIcon(icon).build();
    }

    private TvInputInfo(ResolveInfo service, String id, int type, boolean isHardwareInput, CharSequence label, int labelResId, Icon icon, Icon iconStandby, Icon iconDisconnected, String setupActivity, boolean canRecord, int tunerCount, HdmiDeviceInfo hdmiDeviceInfo, boolean isConnectedToHdmiSwitch, int hdmiConnectionRelativePosition, String parentId, Bundle extras) {
        this.mService = service;
        this.mId = id;
        this.mType = type;
        this.mIsHardwareInput = isHardwareInput;
        this.mLabel = label;
        this.mLabelResId = labelResId;
        this.mIcon = icon;
        this.mIconStandby = iconStandby;
        this.mIconDisconnected = iconDisconnected;
        this.mSetupActivity = setupActivity;
        this.mCanRecord = canRecord;
        this.mTunerCount = tunerCount;
        this.mHdmiDeviceInfo = hdmiDeviceInfo;
        this.mIsConnectedToHdmiSwitch = isConnectedToHdmiSwitch;
        this.mHdmiConnectionRelativePosition = hdmiConnectionRelativePosition;
        this.mParentId = parentId;
        this.mExtras = extras;
    }

    public String getId() {
        return this.mId;
    }

    public String getParentId() {
        return this.mParentId;
    }

    public ServiceInfo getServiceInfo() {
        return this.mService.serviceInfo;
    }

    @UnsupportedAppUsage
    public ComponentName getComponent() {
        return new ComponentName(this.mService.serviceInfo.packageName, this.mService.serviceInfo.name);
    }

    public Intent createSetupIntent() {
        if (TextUtils.isEmpty(this.mSetupActivity)) {
            return null;
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(this.mService.serviceInfo.packageName, this.mSetupActivity);
        intent.putExtra(EXTRA_INPUT_ID, getId());
        return intent;
    }

    @Deprecated
    public Intent createSettingsIntent() {
        return null;
    }

    public int getType() {
        return this.mType;
    }

    public int getTunerCount() {
        return this.mTunerCount;
    }

    public boolean canRecord() {
        return this.mCanRecord;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    @SystemApi
    public HdmiDeviceInfo getHdmiDeviceInfo() {
        if (this.mType == 1007) {
            return this.mHdmiDeviceInfo;
        }
        return null;
    }

    public boolean isPassthroughInput() {
        return this.mType != 0;
    }

    @SystemApi
    public boolean isHardwareInput() {
        return this.mIsHardwareInput;
    }

    @SystemApi
    public boolean isConnectedToHdmiSwitch() {
        return this.mIsConnectedToHdmiSwitch;
    }

    public int getHdmiConnectionRelativePosition() {
        return this.mHdmiConnectionRelativePosition;
    }

    public boolean isHidden(Context context) {
        return TvInputSettings.isHidden(context, this.mId, UserHandle.myUserId());
    }

    public CharSequence loadLabel(Context context) {
        if (this.mLabelResId != 0) {
            return context.getPackageManager().getText(this.mService.serviceInfo.packageName, this.mLabelResId, null);
        }
        if (!TextUtils.isEmpty(this.mLabel)) {
            return this.mLabel;
        }
        return this.mService.loadLabel(context.getPackageManager());
    }

    public CharSequence loadCustomLabel(Context context) {
        return TvInputSettings.getCustomLabel(context, this.mId, UserHandle.myUserId());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002d, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002e, code lost:
        if (r0 != null) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0030, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0033, code lost:
        throw r2;
     */
    public Drawable loadIcon(Context context) {
        Icon icon = this.mIcon;
        if (icon != null) {
            return icon.loadDrawable(context);
        }
        if (this.mIconUri != null) {
            try {
                InputStream is = context.getContentResolver().openInputStream(this.mIconUri);
                Drawable drawable = Drawable.createFromStream(is, null);
                if (drawable != null) {
                    if (is != null) {
                        $closeResource(null, is);
                    }
                    return drawable;
                } else if (is != null) {
                    $closeResource(null, is);
                }
            } catch (IOException e) {
                Log.w(TAG, "Loading the default icon due to a failure");
            }
        }
        return loadServiceIcon(context);
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    @SystemApi
    public Drawable loadIcon(Context context, int state) {
        if (state == 0) {
            return loadIcon(context);
        }
        if (state == 1) {
            Icon icon = this.mIconStandby;
            if (icon != null) {
                return icon.loadDrawable(context);
            }
            return null;
        } else if (state == 2) {
            Icon icon2 = this.mIconDisconnected;
            if (icon2 != null) {
                return icon2.loadDrawable(context);
            }
            return null;
        } else {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int hashCode() {
        return this.mId.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TvInputInfo)) {
            return false;
        }
        TvInputInfo obj = (TvInputInfo) o;
        if (!Objects.equals(this.mService, obj.mService) || !TextUtils.equals(this.mId, obj.mId) || this.mType != obj.mType || this.mIsHardwareInput != obj.mIsHardwareInput || !TextUtils.equals(this.mLabel, obj.mLabel) || !Objects.equals(this.mIconUri, obj.mIconUri) || this.mLabelResId != obj.mLabelResId || !Objects.equals(this.mIcon, obj.mIcon) || !Objects.equals(this.mIconStandby, obj.mIconStandby) || !Objects.equals(this.mIconDisconnected, obj.mIconDisconnected) || !TextUtils.equals(this.mSetupActivity, obj.mSetupActivity) || this.mCanRecord != obj.mCanRecord || this.mTunerCount != obj.mTunerCount || !Objects.equals(this.mHdmiDeviceInfo, obj.mHdmiDeviceInfo) || this.mIsConnectedToHdmiSwitch != obj.mIsConnectedToHdmiSwitch || this.mHdmiConnectionRelativePosition != obj.mHdmiConnectionRelativePosition || !TextUtils.equals(this.mParentId, obj.mParentId) || !Objects.equals(this.mExtras, obj.mExtras)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "TvInputInfo{id=" + this.mId + ", pkg=" + this.mService.serviceInfo.packageName + ", service=" + this.mService.serviceInfo.name + "}";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        this.mService.writeToParcel(dest, flags);
        dest.writeString(this.mId);
        dest.writeInt(this.mType);
        dest.writeByte(this.mIsHardwareInput ? (byte) 1 : 0);
        TextUtils.writeToParcel(this.mLabel, dest, flags);
        dest.writeParcelable(this.mIconUri, flags);
        dest.writeInt(this.mLabelResId);
        dest.writeParcelable(this.mIcon, flags);
        dest.writeParcelable(this.mIconStandby, flags);
        dest.writeParcelable(this.mIconDisconnected, flags);
        dest.writeString(this.mSetupActivity);
        dest.writeByte(this.mCanRecord ? (byte) 1 : 0);
        dest.writeInt(this.mTunerCount);
        dest.writeParcelable(this.mHdmiDeviceInfo, flags);
        dest.writeByte(this.mIsConnectedToHdmiSwitch ? (byte) 1 : 0);
        dest.writeInt(this.mHdmiConnectionRelativePosition);
        dest.writeString(this.mParentId);
        dest.writeBundle(this.mExtras);
    }

    private Drawable loadServiceIcon(Context context) {
        if (this.mService.serviceInfo.icon == 0 && this.mService.serviceInfo.applicationInfo.icon == 0) {
            return null;
        }
        return this.mService.serviceInfo.loadIcon(context.getPackageManager());
    }

    private TvInputInfo(Parcel in) {
        this.mService = ResolveInfo.CREATOR.createFromParcel(in);
        this.mId = in.readString();
        this.mType = in.readInt();
        boolean z = false;
        this.mIsHardwareInput = in.readByte() == 1;
        this.mLabel = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mIconUri = (Uri) in.readParcelable(null);
        this.mLabelResId = in.readInt();
        this.mIcon = (Icon) in.readParcelable(null);
        this.mIconStandby = (Icon) in.readParcelable(null);
        this.mIconDisconnected = (Icon) in.readParcelable(null);
        this.mSetupActivity = in.readString();
        this.mCanRecord = in.readByte() == 1;
        this.mTunerCount = in.readInt();
        this.mHdmiDeviceInfo = (HdmiDeviceInfo) in.readParcelable(null);
        this.mIsConnectedToHdmiSwitch = in.readByte() == 1 ? true : z;
        this.mHdmiConnectionRelativePosition = in.readInt();
        this.mParentId = in.readString();
        this.mExtras = in.readBundle();
    }

    public static final class Builder {
        private static final String DELIMITER_INFO_IN_ID = "/";
        private static final int LENGTH_HDMI_DEVICE_ID = 2;
        private static final int LENGTH_HDMI_PHYSICAL_ADDRESS = 4;
        private static final String PREFIX_HARDWARE_DEVICE = "HW";
        private static final String PREFIX_HDMI_DEVICE = "HDMI";
        private static final String XML_START_TAG_NAME = "tv-input";
        private static final SparseIntArray sHardwareTypeToTvInputType = new SparseIntArray();
        private Boolean mCanRecord;
        private final Context mContext;
        private Bundle mExtras;
        private HdmiDeviceInfo mHdmiDeviceInfo;
        private Icon mIcon;
        private Icon mIconDisconnected;
        private Icon mIconStandby;
        private CharSequence mLabel;
        private int mLabelResId;
        private String mParentId;
        private final ResolveInfo mResolveInfo;
        private String mSetupActivity;
        private Integer mTunerCount;
        private TvInputHardwareInfo mTvInputHardwareInfo;

        static {
            sHardwareTypeToTvInputType.put(1, 1000);
            sHardwareTypeToTvInputType.put(2, 0);
            sHardwareTypeToTvInputType.put(3, 1001);
            sHardwareTypeToTvInputType.put(4, 1002);
            sHardwareTypeToTvInputType.put(5, 1003);
            sHardwareTypeToTvInputType.put(6, 1004);
            sHardwareTypeToTvInputType.put(7, 1005);
            sHardwareTypeToTvInputType.put(8, 1006);
            sHardwareTypeToTvInputType.put(9, 1007);
            sHardwareTypeToTvInputType.put(10, 1008);
        }

        public Builder(Context context, ComponentName component) {
            if (context != null) {
                this.mResolveInfo = context.getPackageManager().resolveService(new Intent(TvInputService.SERVICE_INTERFACE).setComponent(component), 132);
                if (this.mResolveInfo != null) {
                    this.mContext = context;
                    return;
                }
                throw new IllegalArgumentException("Invalid component. Can't find the service.");
            }
            throw new IllegalArgumentException("context cannot be null.");
        }

        public Builder(Context context, ResolveInfo resolveInfo) {
            if (context == null) {
                throw new IllegalArgumentException("context cannot be null");
            } else if (resolveInfo != null) {
                this.mContext = context;
                this.mResolveInfo = resolveInfo;
            } else {
                throw new IllegalArgumentException("resolveInfo cannot be null");
            }
        }

        @SystemApi
        public Builder setIcon(Icon icon) {
            this.mIcon = icon;
            return this;
        }

        @SystemApi
        public Builder setIcon(Icon icon, int state) {
            if (state == 0) {
                this.mIcon = icon;
            } else if (state == 1) {
                this.mIconStandby = icon;
            } else if (state == 2) {
                this.mIconDisconnected = icon;
            } else {
                throw new IllegalArgumentException("Unknown state: " + state);
            }
            return this;
        }

        @SystemApi
        public Builder setLabel(CharSequence label) {
            if (this.mLabelResId == 0) {
                this.mLabel = label;
                return this;
            }
            throw new IllegalStateException("Resource ID for label is already set.");
        }

        @SystemApi
        public Builder setLabel(int resId) {
            if (this.mLabel == null) {
                this.mLabelResId = resId;
                return this;
            }
            throw new IllegalStateException("Label text is already set.");
        }

        @SystemApi
        public Builder setHdmiDeviceInfo(HdmiDeviceInfo hdmiDeviceInfo) {
            if (this.mTvInputHardwareInfo != null) {
                Log.w(TvInputInfo.TAG, "TvInputHardwareInfo will not be used to build this TvInputInfo");
                this.mTvInputHardwareInfo = null;
            }
            this.mHdmiDeviceInfo = hdmiDeviceInfo;
            return this;
        }

        @SystemApi
        public Builder setParentId(String parentId) {
            this.mParentId = parentId;
            return this;
        }

        @SystemApi
        public Builder setTvInputHardwareInfo(TvInputHardwareInfo tvInputHardwareInfo) {
            if (this.mHdmiDeviceInfo != null) {
                Log.w(TvInputInfo.TAG, "mHdmiDeviceInfo will not be used to build this TvInputInfo");
                this.mHdmiDeviceInfo = null;
            }
            this.mTvInputHardwareInfo = tvInputHardwareInfo;
            return this;
        }

        public Builder setTunerCount(int tunerCount) {
            this.mTunerCount = Integer.valueOf(tunerCount);
            return this;
        }

        public Builder setCanRecord(boolean canRecord) {
            this.mCanRecord = Boolean.valueOf(canRecord);
            return this;
        }

        public Builder setExtras(Bundle extras) {
            this.mExtras = extras;
            return this;
        }

        public TvInputInfo build() {
            int type;
            String id;
            ComponentName componentName = new ComponentName(this.mResolveInfo.serviceInfo.packageName, this.mResolveInfo.serviceInfo.name);
            boolean isHardwareInput = false;
            boolean isConnectedToHdmiSwitch = false;
            int hdmiConnectionRelativePosition = 0;
            HdmiDeviceInfo hdmiDeviceInfo = this.mHdmiDeviceInfo;
            int i = 0;
            if (hdmiDeviceInfo != null) {
                id = generateInputId(componentName, hdmiDeviceInfo);
                type = 1007;
                isHardwareInput = true;
                hdmiConnectionRelativePosition = getRelativePosition(this.mContext, this.mHdmiDeviceInfo);
                boolean z = true;
                if (hdmiConnectionRelativePosition == 1) {
                    z = false;
                }
                isConnectedToHdmiSwitch = z;
            } else {
                TvInputHardwareInfo tvInputHardwareInfo = this.mTvInputHardwareInfo;
                if (tvInputHardwareInfo != null) {
                    id = generateInputId(componentName, tvInputHardwareInfo);
                    type = sHardwareTypeToTvInputType.get(this.mTvInputHardwareInfo.getType(), 0);
                    isHardwareInput = true;
                } else {
                    id = generateInputId(componentName);
                    type = 0;
                }
            }
            parseServiceMetadata(type);
            ResolveInfo resolveInfo = this.mResolveInfo;
            CharSequence charSequence = this.mLabel;
            int i2 = this.mLabelResId;
            Icon icon = this.mIcon;
            Icon icon2 = this.mIconStandby;
            Icon icon3 = this.mIconDisconnected;
            String str = this.mSetupActivity;
            Boolean bool = this.mCanRecord;
            boolean booleanValue = bool == null ? false : bool.booleanValue();
            Integer num = this.mTunerCount;
            if (num != null) {
                i = num.intValue();
            }
            return new TvInputInfo(resolveInfo, id, type, isHardwareInput, charSequence, i2, icon, icon2, icon3, str, booleanValue, i, this.mHdmiDeviceInfo, isConnectedToHdmiSwitch, hdmiConnectionRelativePosition, this.mParentId, this.mExtras);
        }

        private static String generateInputId(ComponentName name) {
            return name.flattenToShortString();
        }

        private static String generateInputId(ComponentName name, HdmiDeviceInfo hdmiDeviceInfo) {
            return name.flattenToShortString() + String.format(Locale.ENGLISH, "/HDMI%04X%02X", Integer.valueOf(hdmiDeviceInfo.getPhysicalAddress()), Integer.valueOf(hdmiDeviceInfo.getId()));
        }

        private static String generateInputId(ComponentName name, TvInputHardwareInfo tvInputHardwareInfo) {
            return name.flattenToShortString() + DELIMITER_INFO_IN_ID + PREFIX_HARDWARE_DEVICE + tvInputHardwareInfo.getDeviceId();
        }

        private static int getRelativePosition(Context context, HdmiDeviceInfo info) {
            HdmiControlManager hcm = (HdmiControlManager) context.getSystemService(Context.HDMI_CONTROL_SERVICE);
            if (hcm == null) {
                return 0;
            }
            return HdmiUtils.getHdmiAddressRelativePosition(info.getPhysicalAddress(), hcm.getPhysicalAddress());
        }

        /* JADX WARNING: Code restructure failed: missing block: B:30:0x009f, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a0, code lost:
            if (r2 != null) goto L_0x00a2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
            r2.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a6, code lost:
            r5 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a7, code lost:
            r3.addSuppressed(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x00aa, code lost:
            throw r4;
         */
        private void parseServiceMetadata(int inputType) {
            ServiceInfo si = this.mResolveInfo.serviceInfo;
            PackageManager pm = this.mContext.getPackageManager();
            try {
                XmlResourceParser parser = si.loadXmlMetaData(pm, TvInputService.SERVICE_META_DATA);
                if (parser != null) {
                    Resources res = pm.getResourcesForApplication(si.applicationInfo);
                    AttributeSet attrs = Xml.asAttributeSet(parser);
                    while (true) {
                        int type = parser.next();
                        if (type == 1 || type == 2) {
                        }
                    }
                    if (XML_START_TAG_NAME.equals(parser.getName())) {
                        TypedArray sa = res.obtainAttributes(attrs, R.styleable.TvInputService);
                        this.mSetupActivity = sa.getString(1);
                        if (this.mCanRecord == null) {
                            this.mCanRecord = Boolean.valueOf(sa.getBoolean(2, false));
                        }
                        if (this.mTunerCount == null && inputType == 0) {
                            this.mTunerCount = Integer.valueOf(sa.getInt(3, 1));
                        }
                        sa.recycle();
                        try {
                            parser.close();
                        } catch (IOException | XmlPullParserException e) {
                            throw new IllegalStateException("Failed reading meta-data for " + si.packageName, e);
                        }
                    } else {
                        throw new IllegalStateException("Meta-data does not start with tv-input tag for " + si.name);
                    }
                } else {
                    throw new IllegalStateException("No android.media.tv.input meta-data found for " + si.name);
                }
            } catch (PackageManager.NameNotFoundException e2) {
                throw new IllegalStateException("No resources found for " + si.packageName, e2);
            }
        }
    }

    @SystemApi
    public static final class TvInputSettings {
        private static final String CUSTOM_NAME_SEPARATOR = ",";
        private static final String TV_INPUT_SEPARATOR = ":";

        private TvInputSettings() {
        }

        /* access modifiers changed from: private */
        public static boolean isHidden(Context context, String inputId, int userId) {
            return getHiddenTvInputIds(context, userId).contains(inputId);
        }

        /* access modifiers changed from: private */
        public static String getCustomLabel(Context context, String inputId, int userId) {
            return getCustomLabels(context, userId).get(inputId);
        }

        @SystemApi
        public static Set<String> getHiddenTvInputIds(Context context, int userId) {
            String hiddenIdsString = Settings.Secure.getStringForUser(context.getContentResolver(), Settings.Secure.TV_INPUT_HIDDEN_INPUTS, userId);
            Set<String> set = new HashSet<>();
            if (TextUtils.isEmpty(hiddenIdsString)) {
                return set;
            }
            for (String id : hiddenIdsString.split(":")) {
                set.add(Uri.decode(id));
            }
            return set;
        }

        @SystemApi
        public static Map<String, String> getCustomLabels(Context context, int userId) {
            String labelsString = Settings.Secure.getStringForUser(context.getContentResolver(), Settings.Secure.TV_INPUT_CUSTOM_LABELS, userId);
            Map<String, String> map = new HashMap<>();
            if (TextUtils.isEmpty(labelsString)) {
                return map;
            }
            for (String pairString : labelsString.split(":")) {
                String[] pair = pairString.split(",");
                map.put(Uri.decode(pair[0]), Uri.decode(pair[1]));
            }
            return map;
        }

        @SystemApi
        public static void putHiddenTvInputs(Context context, Set<String> hiddenInputIds, int userId) {
            StringBuilder builder = new StringBuilder();
            boolean firstItem = true;
            for (String inputId : hiddenInputIds) {
                ensureValidField(inputId);
                if (firstItem) {
                    firstItem = false;
                } else {
                    builder.append(":");
                }
                builder.append(Uri.encode(inputId));
            }
            Settings.Secure.putStringForUser(context.getContentResolver(), Settings.Secure.TV_INPUT_HIDDEN_INPUTS, builder.toString(), userId);
            TvInputManager tm = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
            for (String inputId2 : hiddenInputIds) {
                TvInputInfo info = tm.getTvInputInfo(inputId2);
                if (info != null) {
                    tm.updateTvInputInfo(info);
                }
            }
        }

        @SystemApi
        public static void putCustomLabels(Context context, Map<String, String> customLabels, int userId) {
            StringBuilder builder = new StringBuilder();
            boolean firstItem = true;
            for (Map.Entry<String, String> entry : customLabels.entrySet()) {
                ensureValidField(entry.getKey());
                ensureValidField(entry.getValue());
                if (firstItem) {
                    firstItem = false;
                } else {
                    builder.append(":");
                }
                builder.append(Uri.encode(entry.getKey()));
                builder.append(",");
                builder.append(Uri.encode(entry.getValue()));
            }
            Settings.Secure.putStringForUser(context.getContentResolver(), Settings.Secure.TV_INPUT_CUSTOM_LABELS, builder.toString(), userId);
            TvInputManager tm = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
            for (String inputId : customLabels.keySet()) {
                TvInputInfo info = tm.getTvInputInfo(inputId);
                if (info != null) {
                    tm.updateTvInputInfo(info);
                }
            }
        }

        private static void ensureValidField(String value) {
            if (TextUtils.isEmpty(value)) {
                throw new IllegalArgumentException(value + " should not empty ");
            }
        }
    }
}

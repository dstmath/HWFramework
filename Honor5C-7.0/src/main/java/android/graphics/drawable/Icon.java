package android.graphics.drawable;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.service.notification.ZenModeConfig;
import android.service.voice.VoiceInteractionSession;
import android.text.TextUtils;
import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public final class Icon implements Parcelable {
    public static final Creator<Icon> CREATOR = null;
    static final Mode DEFAULT_TINT_MODE = null;
    public static final int MIN_ASHMEM_ICON_SIZE = 131072;
    private static final String TAG = "Icon";
    public static final int TYPE_BITMAP = 1;
    public static final int TYPE_DATA = 3;
    public static final int TYPE_RESOURCE = 2;
    public static final int TYPE_URI = 4;
    private static final int VERSION_STREAM_SERIALIZER = 1;
    private int mInt1;
    private int mInt2;
    private Object mObj1;
    private String mString1;
    private ColorStateList mTintList;
    private Mode mTintMode;
    private final int mType;

    private class LoadDrawableTask implements Runnable {
        final Context mContext;
        final Message mMessage;

        /* renamed from: android.graphics.drawable.Icon.LoadDrawableTask.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ OnDrawableLoadedListener val$listener;

            AnonymousClass1(OnDrawableLoadedListener val$listener) {
                this.val$listener = val$listener;
            }

            public void run() {
                this.val$listener.onDrawableLoaded((Drawable) LoadDrawableTask.this.mMessage.obj);
            }
        }

        public LoadDrawableTask(Context context, Handler handler, OnDrawableLoadedListener listener) {
            this.mContext = context;
            this.mMessage = Message.obtain(handler, new AnonymousClass1(listener));
        }

        public LoadDrawableTask(Context context, Message message) {
            this.mContext = context;
            this.mMessage = message;
        }

        public void run() {
            this.mMessage.obj = Icon.this.loadDrawable(this.mContext);
            this.mMessage.sendToTarget();
        }

        public void runAsync() {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(this);
        }
    }

    public interface OnDrawableLoadedListener {
        void onDrawableLoaded(Drawable drawable);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.drawable.Icon.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.drawable.Icon.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.Icon.<clinit>():void");
    }

    public int getType() {
        return this.mType;
    }

    public Bitmap getBitmap() {
        if (this.mType == VERSION_STREAM_SERIALIZER) {
            return (Bitmap) this.mObj1;
        }
        throw new IllegalStateException("called getBitmap() on " + this);
    }

    private void setBitmap(Bitmap b) {
        this.mObj1 = b;
    }

    public int getDataLength() {
        if (this.mType != TYPE_DATA) {
            throw new IllegalStateException("called getDataLength() on " + this);
        }
        int i;
        synchronized (this) {
            i = this.mInt1;
        }
        return i;
    }

    public int getDataOffset() {
        if (this.mType != TYPE_DATA) {
            throw new IllegalStateException("called getDataOffset() on " + this);
        }
        int i;
        synchronized (this) {
            i = this.mInt2;
        }
        return i;
    }

    public byte[] getDataBytes() {
        if (this.mType != TYPE_DATA) {
            throw new IllegalStateException("called getDataBytes() on " + this);
        }
        byte[] bArr;
        synchronized (this) {
            bArr = (byte[]) this.mObj1;
        }
        return bArr;
    }

    public Resources getResources() {
        if (this.mType == TYPE_RESOURCE) {
            return (Resources) this.mObj1;
        }
        throw new IllegalStateException("called getResources() on " + this);
    }

    public String getResPackage() {
        if (this.mType == TYPE_RESOURCE) {
            return this.mString1;
        }
        throw new IllegalStateException("called getResPackage() on " + this);
    }

    public int getResId() {
        if (this.mType == TYPE_RESOURCE) {
            return this.mInt1;
        }
        throw new IllegalStateException("called getResId() on " + this);
    }

    public String getUriString() {
        if (this.mType == TYPE_URI) {
            return this.mString1;
        }
        throw new IllegalStateException("called getUriString() on " + this);
    }

    public Uri getUri() {
        return Uri.parse(getUriString());
    }

    private static final String typeToString(int x) {
        switch (x) {
            case VERSION_STREAM_SERIALIZER /*1*/:
                return "BITMAP";
            case TYPE_RESOURCE /*2*/:
                return "RESOURCE";
            case TYPE_DATA /*3*/:
                return "DATA";
            case TYPE_URI /*4*/:
                return "URI";
            default:
                return "UNKNOWN";
        }
    }

    public void loadDrawableAsync(Context context, Message andThen) {
        if (andThen.getTarget() == null) {
            throw new IllegalArgumentException("callback message must have a target handler");
        }
        new LoadDrawableTask(context, andThen).runAsync();
    }

    public void loadDrawableAsync(Context context, OnDrawableLoadedListener listener, Handler handler) {
        new LoadDrawableTask(context, handler, listener).runAsync();
    }

    public Drawable loadDrawable(Context context) {
        Drawable result = loadDrawableInner(context);
        if (!(result == null || (this.mTintList == null && this.mTintMode == DEFAULT_TINT_MODE))) {
            result.mutate();
            result.setTintList(this.mTintList);
            result.setTintMode(this.mTintMode);
        }
        return result;
    }

    private Drawable loadDrawableInner(Context context) {
        String str;
        Object[] objArr;
        switch (this.mType) {
            case VERSION_STREAM_SERIALIZER /*1*/:
                return new BitmapDrawable(context.getResources(), getBitmap());
            case TYPE_RESOURCE /*2*/:
                if (getResources() == null) {
                    String resPackage = getResPackage();
                    if (TextUtils.isEmpty(resPackage)) {
                        resPackage = context.getPackageName();
                    }
                    if (!ZenModeConfig.SYSTEM_AUTHORITY.equals(resPackage)) {
                        PackageManager pm = context.getPackageManager();
                        try {
                            ApplicationInfo ai = pm.getApplicationInfo(resPackage, Process.PROC_OUT_LONG);
                            if (ai != null) {
                                this.mObj1 = pm.getResourcesForApplication(ai);
                            }
                        } catch (NameNotFoundException e) {
                            str = TAG;
                            objArr = new Object[TYPE_RESOURCE];
                            objArr[0] = resPackage;
                            objArr[VERSION_STREAM_SERIALIZER] = this;
                            Log.e(str, String.format("Unable to find pkg=%s for icon %s", objArr), e);
                            break;
                        }
                    }
                    this.mObj1 = Resources.getSystem();
                }
                try {
                    return getResources().getDrawable(getResId(), context.getTheme());
                } catch (RuntimeException e2) {
                    str = TAG;
                    objArr = new Object[TYPE_RESOURCE];
                    objArr[0] = Integer.valueOf(getResId());
                    objArr[VERSION_STREAM_SERIALIZER] = getResPackage();
                    Log.e(str, String.format("Unable to load resource 0x%08x from pkg=%s", objArr), e2);
                    break;
                }
                break;
            case TYPE_DATA /*3*/:
                return new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(getDataBytes(), getDataOffset(), getDataLength()));
            case TYPE_URI /*4*/:
                Uri uri = getUri();
                String scheme = uri.getScheme();
                InputStream is = null;
                if (VoiceInteractionSession.KEY_CONTENT.equals(scheme) || WifiManager.EXTRA_PASSPOINT_ICON_FILE.equals(scheme)) {
                    try {
                        is = context.getContentResolver().openInputStream(uri);
                    } catch (Exception e3) {
                        Log.w(TAG, "Unable to load image from URI: " + uri, e3);
                    }
                } else {
                    try {
                        is = new FileInputStream(new File(this.mString1));
                    } catch (FileNotFoundException e4) {
                        Log.w(TAG, "Unable to load image from path: " + uri, e4);
                    }
                }
                if (is != null) {
                    return new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(is));
                }
                break;
        }
        return null;
    }

    public Drawable loadDrawableAsUser(Context context, int userId) {
        if (this.mType == TYPE_RESOURCE) {
            String resPackage = getResPackage();
            if (TextUtils.isEmpty(resPackage)) {
                resPackage = context.getPackageName();
            }
            if (getResources() == null && !getResPackage().equals(ZenModeConfig.SYSTEM_AUTHORITY)) {
                try {
                    this.mObj1 = context.getPackageManager().getResourcesForApplicationAsUser(resPackage, userId);
                } catch (NameNotFoundException e) {
                    String str = TAG;
                    Object[] objArr = new Object[TYPE_RESOURCE];
                    objArr[0] = getResPackage();
                    objArr[VERSION_STREAM_SERIALIZER] = Integer.valueOf(userId);
                    Log.e(str, String.format("Unable to find pkg=%s user=%d", objArr), e);
                }
            }
        }
        return loadDrawable(context);
    }

    public void convertToAshmem() {
        if (this.mType == VERSION_STREAM_SERIALIZER && getBitmap().isMutable() && getBitmap().getAllocationByteCount() >= MIN_ASHMEM_ICON_SIZE) {
            setBitmap(getBitmap().createAshmemBitmap());
        }
    }

    public void writeToStream(OutputStream stream) throws IOException {
        DataOutputStream dataStream = new DataOutputStream(stream);
        dataStream.writeInt(VERSION_STREAM_SERIALIZER);
        dataStream.writeByte(this.mType);
        switch (this.mType) {
            case VERSION_STREAM_SERIALIZER /*1*/:
                getBitmap().compress(CompressFormat.PNG, 100, dataStream);
            case TYPE_RESOURCE /*2*/:
                dataStream.writeUTF(getResPackage());
                dataStream.writeInt(getResId());
            case TYPE_DATA /*3*/:
                dataStream.writeInt(getDataLength());
                dataStream.write(getDataBytes(), getDataOffset(), getDataLength());
            case TYPE_URI /*4*/:
                dataStream.writeUTF(getUriString());
            default:
        }
    }

    private Icon(int mType) {
        this.mTintMode = DEFAULT_TINT_MODE;
        this.mType = mType;
    }

    public static Icon createFromStream(InputStream stream) throws IOException {
        DataInputStream inputStream = new DataInputStream(stream);
        if (inputStream.readInt() >= VERSION_STREAM_SERIALIZER) {
            switch (inputStream.readByte()) {
                case VERSION_STREAM_SERIALIZER /*1*/:
                    return createWithBitmap(BitmapFactory.decodeStream(inputStream));
                case TYPE_RESOURCE /*2*/:
                    return createWithResource(inputStream.readUTF(), inputStream.readInt());
                case TYPE_DATA /*3*/:
                    int length = inputStream.readInt();
                    byte[] data = new byte[length];
                    inputStream.read(data, 0, length);
                    return createWithData(data, 0, length);
                case TYPE_URI /*4*/:
                    return createWithContentUri(inputStream.readUTF());
            }
        }
        return null;
    }

    public boolean sameAs(Icon otherIcon) {
        boolean z = true;
        boolean z2 = false;
        if (otherIcon == this) {
            return true;
        }
        if (this.mType != otherIcon.getType()) {
            return false;
        }
        switch (this.mType) {
            case VERSION_STREAM_SERIALIZER /*1*/:
                if (getBitmap() != otherIcon.getBitmap()) {
                    z = false;
                }
                return z;
            case TYPE_RESOURCE /*2*/:
                if (getResId() == otherIcon.getResId()) {
                    z2 = Objects.equals(getResPackage(), otherIcon.getResPackage());
                }
                return z2;
            case TYPE_DATA /*3*/:
                if (getDataLength() != otherIcon.getDataLength() || getDataOffset() != otherIcon.getDataOffset()) {
                    z = false;
                } else if (getDataBytes() != otherIcon.getDataBytes()) {
                    z = false;
                }
                return z;
            case TYPE_URI /*4*/:
                return Objects.equals(getUriString(), otherIcon.getUriString());
            default:
                return false;
        }
    }

    public static Icon createWithResource(Context context, int resId) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        Icon rep = new Icon((int) TYPE_RESOURCE);
        rep.mInt1 = resId;
        rep.mString1 = context.getPackageName();
        return rep;
    }

    public static Icon createWithResource(Resources res, int resId) {
        if (res == null) {
            throw new IllegalArgumentException("Resource must not be null.");
        }
        Icon rep = new Icon((int) TYPE_RESOURCE);
        rep.mInt1 = resId;
        rep.mString1 = res.getResourcePackageName(resId);
        return rep;
    }

    public static Icon createWithResource(String resPackage, int resId) {
        if (resPackage == null) {
            throw new IllegalArgumentException("Resource package name must not be null.");
        }
        Icon rep = new Icon((int) TYPE_RESOURCE);
        rep.mInt1 = resId;
        rep.mString1 = resPackage;
        return rep;
    }

    public static Icon createWithBitmap(Bitmap bits) {
        if (bits == null) {
            throw new IllegalArgumentException("Bitmap must not be null.");
        }
        Icon rep = new Icon((int) VERSION_STREAM_SERIALIZER);
        rep.setBitmap(bits);
        return rep;
    }

    public static Icon createWithData(byte[] data, int offset, int length) {
        if (data == null) {
            throw new IllegalArgumentException("Data must not be null.");
        }
        Icon rep = new Icon((int) TYPE_DATA);
        rep.mObj1 = data;
        rep.mInt1 = length;
        rep.mInt2 = offset;
        return rep;
    }

    public static Icon createWithContentUri(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException("Uri must not be null.");
        }
        Icon rep = new Icon((int) TYPE_URI);
        rep.mString1 = uri;
        return rep;
    }

    public static Icon createWithContentUri(Uri uri) {
        if (uri == null) {
            throw new IllegalArgumentException("Uri must not be null.");
        }
        Icon rep = new Icon((int) TYPE_URI);
        rep.mString1 = uri.toString();
        return rep;
    }

    public Icon setTint(int tint) {
        return setTintList(ColorStateList.valueOf(tint));
    }

    public Icon setTintList(ColorStateList tintList) {
        this.mTintList = tintList;
        return this;
    }

    public Icon setTintMode(Mode mode) {
        this.mTintMode = mode;
        return this;
    }

    public boolean hasTint() {
        return (this.mTintList == null && this.mTintMode == DEFAULT_TINT_MODE) ? false : true;
    }

    public static Icon createWithFilePath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null.");
        }
        Icon rep = new Icon((int) TYPE_URI);
        rep.mString1 = path;
        return rep;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Icon(typ=").append(typeToString(this.mType));
        switch (this.mType) {
            case VERSION_STREAM_SERIALIZER /*1*/:
                sb.append(" size=").append(getBitmap().getWidth()).append("x").append(getBitmap().getHeight());
                break;
            case TYPE_RESOURCE /*2*/:
                StringBuilder append = sb.append(" pkg=").append(getResPackage()).append(" id=");
                Object[] objArr = new Object[VERSION_STREAM_SERIALIZER];
                objArr[0] = Integer.valueOf(getResId());
                append.append(String.format("0x%08x", objArr));
                break;
            case TYPE_DATA /*3*/:
                sb.append(" len=").append(getDataLength());
                if (getDataOffset() != 0) {
                    sb.append(" off=").append(getDataOffset());
                    break;
                }
                break;
            case TYPE_URI /*4*/:
                sb.append(" uri=").append(getUriString());
                break;
        }
        if (this.mTintList != null) {
            sb.append(" tint=");
            String sep = ProxyInfo.LOCAL_EXCL_LIST;
            int[] colors = this.mTintList.getColors();
            int length = colors.length;
            for (int i = 0; i < length; i += VERSION_STREAM_SERIALIZER) {
                int c = colors[i];
                Object[] objArr2 = new Object[TYPE_RESOURCE];
                objArr2[0] = sep;
                objArr2[VERSION_STREAM_SERIALIZER] = Integer.valueOf(c);
                sb.append(String.format("%s0x%08x", objArr2));
                sep = "|";
            }
        }
        if (this.mTintMode != DEFAULT_TINT_MODE) {
            sb.append(" mode=").append(this.mTintMode);
        }
        sb.append(")");
        return sb.toString();
    }

    public int describeContents() {
        if (this.mType == VERSION_STREAM_SERIALIZER || this.mType == TYPE_DATA) {
            return VERSION_STREAM_SERIALIZER;
        }
        return 0;
    }

    private Icon(Parcel in) {
        this(in.readInt());
        switch (this.mType) {
            case VERSION_STREAM_SERIALIZER /*1*/:
                this.mObj1 = (Bitmap) Bitmap.CREATOR.createFromParcel(in);
                break;
            case TYPE_RESOURCE /*2*/:
                String pkg = in.readString();
                int resId = in.readInt();
                this.mString1 = pkg;
                this.mInt1 = resId;
                break;
            case TYPE_DATA /*3*/:
                int len = in.readInt();
                byte[] a = in.readBlob();
                if (len == a.length) {
                    this.mInt1 = len;
                    this.mObj1 = a;
                    break;
                }
                throw new RuntimeException("internal unparceling error: blob length (" + a.length + ") != expected length (" + len + ")");
            case TYPE_URI /*4*/:
                this.mString1 = in.readString();
                break;
            default:
                throw new RuntimeException("invalid " + getClass().getSimpleName() + " type in parcel: " + this.mType);
        }
        if (in.readInt() == VERSION_STREAM_SERIALIZER) {
            this.mTintList = (ColorStateList) ColorStateList.CREATOR.createFromParcel(in);
        }
        this.mTintMode = PorterDuff.intToMode(in.readInt());
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType);
        switch (this.mType) {
            case VERSION_STREAM_SERIALIZER /*1*/:
                Bitmap bits = getBitmap();
                getBitmap().writeToParcel(dest, flags);
                break;
            case TYPE_RESOURCE /*2*/:
                dest.writeString(getResPackage());
                dest.writeInt(getResId());
                break;
            case TYPE_DATA /*3*/:
                dest.writeInt(getDataLength());
                dest.writeBlob(getDataBytes(), getDataOffset(), getDataLength());
                break;
            case TYPE_URI /*4*/:
                dest.writeString(getUriString());
                break;
        }
        if (this.mTintList == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(VERSION_STREAM_SERIALIZER);
            this.mTintList.writeToParcel(dest, flags);
        }
        dest.writeInt(PorterDuff.modeToInt(this.mTintMode));
    }
}

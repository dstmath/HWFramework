package android.hardware.camera2.params;

import android.graphics.Color;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.hardware.camera2.utils.SurfaceUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PowerManager;
import android.util.Size;
import android.view.Surface;
import com.android.internal.util.Preconditions;

public final class OutputConfiguration implements Parcelable {
    public static final Creator<OutputConfiguration> CREATOR = null;
    public static final int ROTATION_0 = 0;
    public static final int ROTATION_180 = 2;
    public static final int ROTATION_270 = 3;
    public static final int ROTATION_90 = 1;
    public static final int SURFACE_GROUP_ID_NONE = -1;
    private static final String TAG = "OutputConfiguration";
    private final int mConfiguredDataspace;
    private final int mConfiguredFormat;
    private final int mConfiguredGenerationId;
    private final Size mConfiguredSize;
    private final int mRotation;
    private final Surface mSurface;
    private int mSurfaceGroupId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.camera2.params.OutputConfiguration.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.camera2.params.OutputConfiguration.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.params.OutputConfiguration.<clinit>():void");
    }

    public OutputConfiguration(Surface surface) {
        this((int) SURFACE_GROUP_ID_NONE, surface, (int) ROTATION_0);
    }

    public OutputConfiguration(int surfaceGroupId, Surface surface) {
        this(surfaceGroupId, surface, (int) ROTATION_0);
    }

    public OutputConfiguration(Surface surface, int rotation) {
        this((int) SURFACE_GROUP_ID_NONE, surface, rotation);
    }

    public OutputConfiguration(int surfaceGroupId, Surface surface, int rotation) {
        Preconditions.checkNotNull(surface, "Surface must not be null");
        Preconditions.checkArgumentInRange(rotation, ROTATION_0, ROTATION_270, "Rotation constant");
        this.mSurfaceGroupId = surfaceGroupId;
        this.mSurface = surface;
        this.mRotation = rotation;
        this.mConfiguredSize = SurfaceUtils.getSurfaceSize(surface);
        this.mConfiguredFormat = SurfaceUtils.getSurfaceFormat(surface);
        this.mConfiguredDataspace = SurfaceUtils.getSurfaceDataspace(surface);
        this.mConfiguredGenerationId = surface.getGenerationId();
    }

    public OutputConfiguration(OutputConfiguration other) {
        if (other == null) {
            throw new IllegalArgumentException("OutputConfiguration shouldn't be null");
        }
        this.mSurface = other.mSurface;
        this.mRotation = other.mRotation;
        this.mSurfaceGroupId = other.mSurfaceGroupId;
        this.mConfiguredDataspace = other.mConfiguredDataspace;
        this.mConfiguredFormat = other.mConfiguredFormat;
        this.mConfiguredSize = other.mConfiguredSize;
        this.mConfiguredGenerationId = other.mConfiguredGenerationId;
    }

    public OutputConfiguration(Surface surface, int rotation, int dualMode) {
        Preconditions.checkNotNull(surface, "Surface must not be null");
        Preconditions.checkArgumentInRange(rotation, ROTATION_0, ROTATION_270, "Rotation constant");
        this.mSurface = surface;
        this.mRotation = (PowerManager.WAKE_LOCK_LEVEL_MASK & rotation) | ((dualMode << 16) & Color.RED);
        this.mConfiguredSize = SurfaceUtils.getSurfaceSize(surface);
        this.mConfiguredFormat = SurfaceUtils.getSurfaceFormat(surface);
        this.mConfiguredDataspace = SurfaceUtils.getSurfaceDataspace(surface);
        this.mConfiguredGenerationId = this.mSurface.getGenerationId();
    }

    private OutputConfiguration(Parcel source) {
        int rotation = source.readInt();
        int surfaceSetId = source.readInt();
        Surface surface = (Surface) Surface.CREATOR.createFromParcel(source);
        Preconditions.checkNotNull(surface, "Surface must not be null");
        Preconditions.checkArgumentInRange(rotation, ROTATION_0, ROTATION_270, "Rotation constant");
        this.mSurfaceGroupId = surfaceSetId;
        this.mSurface = surface;
        this.mRotation = rotation;
        this.mConfiguredSize = SurfaceUtils.getSurfaceSize(this.mSurface);
        this.mConfiguredFormat = SurfaceUtils.getSurfaceFormat(this.mSurface);
        this.mConfiguredDataspace = SurfaceUtils.getSurfaceDataspace(this.mSurface);
        this.mConfiguredGenerationId = this.mSurface.getGenerationId();
    }

    public Surface getSurface() {
        return this.mSurface;
    }

    public int getRotation() {
        return this.mRotation;
    }

    public int getSurfaceGroupId() {
        return this.mSurfaceGroupId;
    }

    public int describeContents() {
        return ROTATION_0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (dest == null) {
            throw new IllegalArgumentException("dest must not be null");
        }
        dest.writeInt(this.mRotation);
        dest.writeInt(this.mSurfaceGroupId);
        this.mSurface.writeToParcel(dest, flags);
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OutputConfiguration)) {
            return false;
        }
        OutputConfiguration other = (OutputConfiguration) obj;
        if (this.mRotation != other.mRotation || this.mSurface != other.mSurface || this.mConfiguredGenerationId != other.mConfiguredGenerationId || !this.mConfiguredSize.equals(other.mConfiguredSize) || this.mConfiguredFormat != other.mConfiguredFormat || this.mConfiguredDataspace != other.mConfiguredDataspace) {
            z = false;
        } else if (this.mSurfaceGroupId != other.mSurfaceGroupId) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCode(this.mRotation, this.mSurface.hashCode(), this.mConfiguredGenerationId, this.mConfiguredSize.hashCode(), this.mConfiguredFormat, this.mConfiguredDataspace, this.mSurfaceGroupId);
    }
}

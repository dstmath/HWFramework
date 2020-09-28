package android.content;

import android.app.ActivityThread;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArraySet;
import android.util.Log;
import android.view.contentcapture.ContentCaptureManager;
import com.android.internal.annotations.VisibleForTesting;
import java.io.PrintWriter;

public final class ContentCaptureOptions implements Parcelable {
    public static final Parcelable.Creator<ContentCaptureOptions> CREATOR = new Parcelable.Creator<ContentCaptureOptions>() {
        /* class android.content.ContentCaptureOptions.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ContentCaptureOptions createFromParcel(Parcel parcel) {
            boolean lite = parcel.readBoolean();
            int loggingLevel = parcel.readInt();
            if (lite) {
                return new ContentCaptureOptions(loggingLevel);
            }
            return new ContentCaptureOptions(loggingLevel, parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readArraySet(null));
        }

        @Override // android.os.Parcelable.Creator
        public ContentCaptureOptions[] newArray(int size) {
            return new ContentCaptureOptions[size];
        }
    };
    private static final String TAG = ContentCaptureOptions.class.getSimpleName();
    public final int idleFlushingFrequencyMs;
    public final boolean lite;
    public final int logHistorySize;
    public final int loggingLevel;
    public final int maxBufferSize;
    public final int textChangeFlushingFrequencyMs;
    public final ArraySet<ComponentName> whitelistedComponents;

    public ContentCaptureOptions(int loggingLevel2) {
        this(true, loggingLevel2, 0, 0, 0, 0, null);
    }

    public ContentCaptureOptions(int loggingLevel2, int maxBufferSize2, int idleFlushingFrequencyMs2, int textChangeFlushingFrequencyMs2, int logHistorySize2, ArraySet<ComponentName> whitelistedComponents2) {
        this(false, loggingLevel2, maxBufferSize2, idleFlushingFrequencyMs2, textChangeFlushingFrequencyMs2, logHistorySize2, whitelistedComponents2);
    }

    @VisibleForTesting
    public ContentCaptureOptions(ArraySet<ComponentName> whitelistedComponents2) {
        this(2, 100, 5000, 1000, 10, whitelistedComponents2);
    }

    private ContentCaptureOptions(boolean lite2, int loggingLevel2, int maxBufferSize2, int idleFlushingFrequencyMs2, int textChangeFlushingFrequencyMs2, int logHistorySize2, ArraySet<ComponentName> whitelistedComponents2) {
        this.lite = lite2;
        this.loggingLevel = loggingLevel2;
        this.maxBufferSize = maxBufferSize2;
        this.idleFlushingFrequencyMs = idleFlushingFrequencyMs2;
        this.textChangeFlushingFrequencyMs = textChangeFlushingFrequencyMs2;
        this.logHistorySize = logHistorySize2;
        this.whitelistedComponents = whitelistedComponents2;
    }

    public static ContentCaptureOptions forWhitelistingItself() {
        ActivityThread at = ActivityThread.currentActivityThread();
        if (at != null) {
            String packageName = at.getApplication().getPackageName();
            if ("android.contentcaptureservice.cts".equals(packageName)) {
                ContentCaptureOptions options = new ContentCaptureOptions((ArraySet<ComponentName>) null);
                String str = TAG;
                Log.i(str, "forWhitelistingItself(" + packageName + "): " + options);
                return options;
            }
            String str2 = TAG;
            Log.e(str2, "forWhitelistingItself(): called by " + packageName);
            throw new SecurityException("Thou shall not pass!");
        }
        throw new IllegalStateException("No ActivityThread");
    }

    @VisibleForTesting
    public boolean isWhitelisted(Context context) {
        if (this.whitelistedComponents == null) {
            return true;
        }
        ContentCaptureManager.ContentCaptureClient client = context.getContentCaptureClient();
        if (client != null) {
            return this.whitelistedComponents.contains(client.contentCaptureClientGetComponentName());
        }
        String str = TAG;
        Log.w(str, "isWhitelisted(): no ContentCaptureClient on " + context);
        return false;
    }

    public String toString() {
        if (this.lite) {
            return "ContentCaptureOptions [loggingLevel=" + this.loggingLevel + " (lite)]";
        }
        StringBuilder string = new StringBuilder("ContentCaptureOptions [");
        string.append("loggingLevel=");
        string.append(this.loggingLevel);
        string.append(", maxBufferSize=");
        string.append(this.maxBufferSize);
        string.append(", idleFlushingFrequencyMs=");
        string.append(this.idleFlushingFrequencyMs);
        string.append(", textChangeFlushingFrequencyMs=");
        string.append(this.textChangeFlushingFrequencyMs);
        string.append(", logHistorySize=");
        string.append(this.logHistorySize);
        if (this.whitelistedComponents != null) {
            string.append(", whitelisted=");
            string.append(this.whitelistedComponents);
        }
        string.append(']');
        return string.toString();
    }

    public void dumpShort(PrintWriter pw) {
        pw.print("logLvl=");
        pw.print(this.loggingLevel);
        if (this.lite) {
            pw.print(", lite");
            return;
        }
        pw.print(", bufferSize=");
        pw.print(this.maxBufferSize);
        pw.print(", idle=");
        pw.print(this.idleFlushingFrequencyMs);
        pw.print(", textIdle=");
        pw.print(this.textChangeFlushingFrequencyMs);
        pw.print(", logSize=");
        pw.print(this.logHistorySize);
        if (this.whitelistedComponents != null) {
            pw.print(", whitelisted=");
            pw.print(this.whitelistedComponents);
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeBoolean(this.lite);
        parcel.writeInt(this.loggingLevel);
        if (!this.lite) {
            parcel.writeInt(this.maxBufferSize);
            parcel.writeInt(this.idleFlushingFrequencyMs);
            parcel.writeInt(this.textChangeFlushingFrequencyMs);
            parcel.writeInt(this.logHistorySize);
            parcel.writeArraySet(this.whitelistedComponents);
        }
    }
}

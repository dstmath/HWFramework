package android.service.notification;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class NotificationRankingUpdate implements Parcelable {
    public static final Creator<NotificationRankingUpdate> CREATOR = null;
    private final int[] mImportance;
    private final Bundle mImportanceExplanation;
    private final String[] mInterceptedKeys;
    private final String[] mKeys;
    private final Bundle mOverrideGroupKeys;
    private final Bundle mSuppressedVisualEffects;
    private final Bundle mVisibilityOverrides;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.service.notification.NotificationRankingUpdate.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.service.notification.NotificationRankingUpdate.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.service.notification.NotificationRankingUpdate.<clinit>():void");
    }

    public NotificationRankingUpdate(String[] keys, String[] interceptedKeys, Bundle visibilityOverrides, Bundle suppressedVisualEffects, int[] importance, Bundle explanation, Bundle overrideGroupKeys) {
        this.mKeys = keys;
        this.mInterceptedKeys = interceptedKeys;
        this.mVisibilityOverrides = visibilityOverrides;
        this.mSuppressedVisualEffects = suppressedVisualEffects;
        this.mImportance = importance;
        this.mImportanceExplanation = explanation;
        this.mOverrideGroupKeys = overrideGroupKeys;
    }

    public NotificationRankingUpdate(Parcel in) {
        this.mKeys = in.readStringArray();
        this.mInterceptedKeys = in.readStringArray();
        this.mVisibilityOverrides = in.readBundle();
        this.mSuppressedVisualEffects = in.readBundle();
        this.mImportance = new int[this.mKeys.length];
        in.readIntArray(this.mImportance);
        this.mImportanceExplanation = in.readBundle();
        this.mOverrideGroupKeys = in.readBundle();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStringArray(this.mKeys);
        out.writeStringArray(this.mInterceptedKeys);
        out.writeBundle(this.mVisibilityOverrides);
        out.writeBundle(this.mSuppressedVisualEffects);
        out.writeIntArray(this.mImportance);
        out.writeBundle(this.mImportanceExplanation);
        out.writeBundle(this.mOverrideGroupKeys);
    }

    public String[] getOrderedKeys() {
        return this.mKeys;
    }

    public String[] getInterceptedKeys() {
        return this.mInterceptedKeys;
    }

    public Bundle getVisibilityOverrides() {
        return this.mVisibilityOverrides;
    }

    public Bundle getSuppressedVisualEffects() {
        return this.mSuppressedVisualEffects;
    }

    public int[] getImportance() {
        return this.mImportance;
    }

    public Bundle getImportanceExplanation() {
        return this.mImportanceExplanation;
    }

    public Bundle getOverrideGroupKeys() {
        return this.mOverrideGroupKeys;
    }
}

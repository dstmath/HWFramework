package android.service.chooser;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.speech.tts.TextToSpeech.Engine;

public final class ChooserTarget implements Parcelable {
    public static final Creator<ChooserTarget> CREATOR = null;
    private static final String TAG = "ChooserTarget";
    private ComponentName mComponentName;
    private Icon mIcon;
    private Bundle mIntentExtras;
    private float mScore;
    private CharSequence mTitle;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.service.chooser.ChooserTarget.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.service.chooser.ChooserTarget.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.chooser.ChooserTarget.<clinit>():void");
    }

    public ChooserTarget(CharSequence title, Icon icon, float score, ComponentName componentName, Bundle intentExtras) {
        this.mTitle = title;
        this.mIcon = icon;
        if (score > Engine.DEFAULT_VOLUME || score < 0.0f) {
            throw new IllegalArgumentException("Score " + score + " out of range; " + "must be between 0.0f and 1.0f");
        }
        this.mScore = score;
        this.mComponentName = componentName;
        this.mIntentExtras = intentExtras;
    }

    ChooserTarget(Parcel in) {
        this.mTitle = in.readCharSequence();
        if (in.readInt() != 0) {
            this.mIcon = (Icon) Icon.CREATOR.createFromParcel(in);
        } else {
            this.mIcon = null;
        }
        this.mScore = in.readFloat();
        this.mComponentName = ComponentName.readFromParcel(in);
        this.mIntentExtras = in.readBundle();
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public float getScore() {
        return this.mScore;
    }

    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    public Bundle getIntentExtras() {
        return this.mIntentExtras;
    }

    public String toString() {
        return "ChooserTarget{" + this.mComponentName + ", " + this.mIntentExtras + ", '" + this.mTitle + "', " + this.mScore + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeCharSequence(this.mTitle);
        if (this.mIcon != null) {
            dest.writeInt(1);
            this.mIcon.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        dest.writeFloat(this.mScore);
        ComponentName.writeToParcel(this.mComponentName, dest);
        dest.writeBundle(this.mIntentExtras);
    }
}

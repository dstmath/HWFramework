package android.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KeyCharacteristics implements Parcelable {
    public static final Creator<KeyCharacteristics> CREATOR = null;
    public KeymasterArguments hwEnforced;
    public KeymasterArguments swEnforced;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.security.keymaster.KeyCharacteristics.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.security.keymaster.KeyCharacteristics.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.security.keymaster.KeyCharacteristics.<clinit>():void");
    }

    protected KeyCharacteristics(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        this.swEnforced.writeToParcel(out, flags);
        this.hwEnforced.writeToParcel(out, flags);
    }

    public void readFromParcel(Parcel in) {
        this.swEnforced = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(in);
        this.hwEnforced = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(in);
    }

    public Integer getEnum(int tag) {
        if (this.hwEnforced.containsTag(tag)) {
            return Integer.valueOf(this.hwEnforced.getEnum(tag, -1));
        }
        if (this.swEnforced.containsTag(tag)) {
            return Integer.valueOf(this.swEnforced.getEnum(tag, -1));
        }
        return null;
    }

    public List<Integer> getEnums(int tag) {
        List<Integer> result = new ArrayList();
        result.addAll(this.hwEnforced.getEnums(tag));
        result.addAll(this.swEnforced.getEnums(tag));
        return result;
    }

    public long getUnsignedInt(int tag, long defaultValue) {
        if (this.hwEnforced.containsTag(tag)) {
            return this.hwEnforced.getUnsignedInt(tag, defaultValue);
        }
        return this.swEnforced.getUnsignedInt(tag, defaultValue);
    }

    public List<BigInteger> getUnsignedLongs(int tag) {
        List<BigInteger> result = new ArrayList();
        result.addAll(this.hwEnforced.getUnsignedLongs(tag));
        result.addAll(this.swEnforced.getUnsignedLongs(tag));
        return result;
    }

    public Date getDate(int tag) {
        Date result = this.swEnforced.getDate(tag, null);
        if (result != null) {
            return result;
        }
        return this.hwEnforced.getDate(tag, null);
    }

    public boolean getBoolean(int tag) {
        if (this.hwEnforced.containsTag(tag)) {
            return this.hwEnforced.getBoolean(tag);
        }
        return this.swEnforced.getBoolean(tag);
    }
}

package huawei.android.pfw;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class HwPFWStartupControlScope implements Parcelable {
    public static final Creator<HwPFWStartupControlScope> CREATOR = null;
    private List<String> mSystemBlackPackages;
    private List<String> mThirdWhitePackages;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.pfw.HwPFWStartupControlScope.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.pfw.HwPFWStartupControlScope.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.pfw.HwPFWStartupControlScope.<clinit>():void");
    }

    public HwPFWStartupControlScope() {
        this.mSystemBlackPackages = new ArrayList();
        this.mThirdWhitePackages = new ArrayList();
    }

    public void setScope(List<String> systemBlack, List<String> thirdWhite) {
        clear();
        this.mSystemBlackPackages.addAll(systemBlack);
        this.mThirdWhitePackages.addAll(thirdWhite);
    }

    public void copyOutScope(List<String> systemBlack, List<String> thirdWhite) {
        systemBlack.addAll(this.mSystemBlackPackages);
        thirdWhite.addAll(this.mThirdWhitePackages);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.mSystemBlackPackages);
        dest.writeStringList(this.mThirdWhitePackages);
    }

    private HwPFWStartupControlScope(Parcel source) {
        this.mSystemBlackPackages = new ArrayList();
        this.mThirdWhitePackages = new ArrayList();
        source.readStringList(this.mSystemBlackPackages);
        source.readStringList(this.mThirdWhitePackages);
    }

    private void clear() {
        this.mSystemBlackPackages.clear();
        this.mThirdWhitePackages.clear();
    }
}

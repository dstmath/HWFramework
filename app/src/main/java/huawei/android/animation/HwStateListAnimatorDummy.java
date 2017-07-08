package huawei.android.animation;

import android.animation.Animator;
import android.content.res.ConstantState;

public class HwStateListAnimatorDummy implements HwStateListAnimator {
    private static volatile HwStateListAnimator sHwStateListAnimator;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.animation.HwStateListAnimatorDummy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.animation.HwStateListAnimatorDummy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.animation.HwStateListAnimatorDummy.<clinit>():void");
    }

    private HwStateListAnimatorDummy() {
    }

    public static HwStateListAnimator getDefault() {
        if (sHwStateListAnimator == null) {
            sHwStateListAnimator = new HwStateListAnimatorDummy();
        }
        return sHwStateListAnimator;
    }

    public void addState(int[] specs, Animator animator) {
    }

    public Animator getRunningAnimator() {
        return null;
    }

    public Object getTarget() {
        return null;
    }

    public void setTarget(Object object) {
    }

    public void setState(int[] state) {
    }

    public void jumpToCurrentState() {
    }

    public int getChangingConfigurations() {
        return 0;
    }

    public void setChangingConfigurations(int configs) {
    }

    public void appendChangingConfigurations(int configs) {
    }

    public ConstantState<HwStateListAnimator> createConstantState() {
        return null;
    }

    public void setMode(int mode) {
    }

    public void setAnimatorEnable(boolean enable) {
    }
}

package tmsdk.common.module.numbermarker;

import tmsdkobf.cd;
import tmsdkobf.de;

/* compiled from: Unknown */
public class NumberMarkEntity {
    public static int CLIENT_LOGIC_BLACK_LIST;
    public static int CLIENT_LOGIC_MAX;
    public static int CLIENT_LOGIC_MIN;
    public static int TAG_TYPE_CHEAT;
    public static int TAG_TYPE_CORRECT_YELLOW;
    public static int TAG_TYPE_HOUSE_AGT;
    public static int TAG_TYPE_INSURANCE;
    public static int TAG_TYPE_MAX;
    public static int TAG_TYPE_NONE;
    public static int TAG_TYPE_OTHER;
    public static int TAG_TYPE_SALES;
    public static int TAG_TYPE_SELF_TAG;
    public static int TEL_TYPE_MISS_CALL;
    public static int TEL_TYPE_RING_ONE_SOUND;
    public static int TEL_TYPE_USER_CANCEL;
    public static int TEL_TYPE_USER_HANG_UP;
    public static int USER_ACTION_IMPEACH;
    public int calltime;
    public int clientlogic;
    public int localTagType;
    public String originName;
    public String phonenum;
    public int scene;
    public int tagtype;
    public int talktime;
    public int teltype;
    public String userDefineName;
    public int useraction;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.numbermarker.NumberMarkEntity.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.numbermarker.NumberMarkEntity.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.numbermarker.NumberMarkEntity.<clinit>():void");
    }

    public NumberMarkEntity() {
        this.phonenum = "";
        this.useraction = USER_ACTION_IMPEACH;
        this.teltype = de.ih.value();
        this.talktime = 0;
        this.calltime = 0;
        this.clientlogic = CLIENT_LOGIC_MIN;
        this.tagtype = 0;
        this.localTagType = 0;
        this.scene = 0;
    }

    public cd toTelReport() {
        cd cdVar = new cd();
        cdVar.ej = this.phonenum;
        cdVar.eV = this.useraction;
        cdVar.eW = this.teltype;
        cdVar.eX = this.talktime;
        cdVar.eY = this.calltime;
        cdVar.eZ = this.clientlogic;
        cdVar.tagType = this.tagtype;
        cdVar.userDefineName = this.userDefineName;
        cdVar.localTagType = this.localTagType;
        cdVar.originName = this.originName;
        cdVar.scene = this.scene;
        return cdVar;
    }
}

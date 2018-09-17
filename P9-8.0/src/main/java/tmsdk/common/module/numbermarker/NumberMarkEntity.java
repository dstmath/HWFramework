package tmsdk.common.module.numbermarker;

import tmsdkobf.cr;
import tmsdkobf.ef;

public class NumberMarkEntity {
    public static final int CLIENT_LOGIC_MIN = 0;
    public static int TAG_TYPE_CORRECT_YELLOW = 10056;
    public static int TAG_TYPE_MAX = 30056;
    public static int TAG_TYPE_NONE = 0;
    public static int TAG_TYPE_OTHER = 50;
    public static int TAG_TYPE_SELF_TAG = 10055;
    public static int TEL_TYPE_MISS_CALL = 3;
    public static int TEL_TYPE_RING_ONE_SOUND = 1;
    public static int TEL_TYPE_USER_CANCEL = 2;
    public static int TEL_TYPE_USER_HANG_UP = 4;
    public static final int USER_ACTION_IMPEACH = 11;
    public int calltime = 0;
    public int clientlogic = 0;
    public int localTagType = 0;
    public String originName;
    public String phonenum = "";
    public int scene = 0;
    public int tagtype = 0;
    public int talktime = 0;
    public int teltype = ef.jZ.value();
    public String userDefineName;
    public int useraction = 11;

    public cr toTelReport() {
        cr crVar = new cr();
        crVar.fe = this.phonenum;
        crVar.fP = this.useraction;
        crVar.fQ = this.teltype;
        crVar.fR = this.talktime;
        crVar.fS = this.calltime;
        crVar.fT = this.clientlogic;
        crVar.tagType = this.tagtype;
        crVar.userDefineName = this.userDefineName;
        crVar.localTagType = this.localTagType;
        crVar.originName = this.originName;
        crVar.scene = this.scene;
        return crVar;
    }
}

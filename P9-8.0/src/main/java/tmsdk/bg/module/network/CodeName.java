package tmsdk.bg.module.network;

public final class CodeName {
    public String mCode;
    public String mName;

    public CodeName() {
        this.mCode = "";
        this.mName = "";
    }

    public CodeName(String str, String str2) {
        this.mCode = str;
        this.mName = str2;
    }

    public CodeName(CodeName codeName) {
        this.mCode = codeName.mCode;
        this.mName = codeName.mName;
    }
}

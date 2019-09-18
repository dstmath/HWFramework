package android.system;

import libcore.util.Objects;

public final class StructPasswd {
    public final String pw_dir;
    public final int pw_gid;
    public final String pw_name;
    public final String pw_shell;
    public final int pw_uid;

    public StructPasswd(String pw_name2, int pw_uid2, int pw_gid2, String pw_dir2, String pw_shell2) {
        this.pw_name = pw_name2;
        this.pw_uid = pw_uid2;
        this.pw_gid = pw_gid2;
        this.pw_dir = pw_dir2;
        this.pw_shell = pw_shell2;
    }

    public String toString() {
        return Objects.toString(this);
    }
}

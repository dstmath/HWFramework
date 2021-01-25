package huawei.android.security.secai.hook;

public enum HookStatus {
    DEFAULT(0),
    LOAD_LIBRARY_ERROR(1),
    LOCATE_INTERPRETER_ERROR(2),
    HOOK_FAILURE(3),
    HOOK_SUCCESS(4);
    
    private int code;

    private HookStatus(int code2) {
        this.code = code2;
    }
}

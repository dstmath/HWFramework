package huawei.android.security.secai.hookcase.entity;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

public class HookEntity {
    private Method mBackupMethod;
    private Method mHookMethod;
    private Member mTargetMethod;

    public HookEntity() {
    }

    public HookEntity(Member target, Method hook, Method backup) {
        this.mTargetMethod = target;
        this.mHookMethod = hook;
        this.mBackupMethod = backup;
    }

    public Member getTargetMethod() {
        return this.mTargetMethod;
    }

    public void setTargetMethod(Member targetMethod) {
        this.mTargetMethod = targetMethod;
    }

    public Method getHookMethod() {
        return this.mHookMethod;
    }

    public void setHookMethod(Method hookMethod) {
        this.mHookMethod = hookMethod;
    }

    public Method getBackupMethod() {
        return this.mBackupMethod;
    }

    public void setBackupMethod(Method backupMethod) {
        this.mBackupMethod = backupMethod;
    }
}

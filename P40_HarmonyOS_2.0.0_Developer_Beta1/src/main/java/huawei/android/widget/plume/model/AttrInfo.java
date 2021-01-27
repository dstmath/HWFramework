package huawei.android.widget.plume.model;

public class AttrInfo {
    public static final int ATTR_FLAG_LAYOUT_PARAMS = 2;
    public static final int ATTR_FLAG_VIEW = 1;
    private Class[] mClassArgs;
    private int mFlag;
    private Object mHost;
    private String mMethodName;
    private Object[] mObjectArgs;

    public AttrInfo(String methodName, Object host, Class[] classArgs, Object[] objectArgs, int flag) {
        this.mMethodName = methodName;
        this.mHost = host;
        this.mClassArgs = classArgs;
        this.mObjectArgs = objectArgs;
        this.mFlag = flag;
    }

    public String getMethodName() {
        return this.mMethodName;
    }

    public void setMethodName(String methodName) {
        this.mMethodName = methodName;
    }

    public Object getHost() {
        return this.mHost;
    }

    public void setHost(Object host) {
        this.mHost = host;
    }

    public Class[] getClassArgs() {
        return this.mClassArgs;
    }

    public void setClassArgs(Class[] classArgs) {
        this.mClassArgs = classArgs;
    }

    public Object[] getObjectArgs() {
        return this.mObjectArgs;
    }

    public void setObjectArgs(Object[] objectArgs) {
        this.mObjectArgs = objectArgs;
    }

    public int getFlag() {
        return this.mFlag;
    }

    public void setFlag(int flag) {
        this.mFlag = flag;
    }
}

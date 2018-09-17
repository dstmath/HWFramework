package tmsdk.common.module.aresengine;

import java.util.ArrayList;

public final class FilterResult {
    public boolean isBlocked;
    public TelephonyEntity mData;
    public final ArrayList<Runnable> mDotos = new ArrayList();
    public int mFilterfiled;
    public Object[] mParams;
    public int mState;
}

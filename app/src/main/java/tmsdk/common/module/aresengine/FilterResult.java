package tmsdk.common.module.aresengine;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class FilterResult {
    public boolean isBlocked;
    public TelephonyEntity mData;
    public final ArrayList<Runnable> mDotos;
    public int mFilterfiled;
    public Object[] mParams;
    public int mState;

    public FilterResult() {
        this.mDotos = new ArrayList();
    }
}

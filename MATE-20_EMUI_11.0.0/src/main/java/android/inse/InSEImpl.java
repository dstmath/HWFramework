package android.inse;

import com.huawei.android.util.NoExtAPIException;

public class InSEImpl {
    private InSEImpl() {
    }

    public static InSEImpl getInstance() {
        throw new NoExtAPIException("method not supported.");
    }

    public int inSE_PowerOnDelayed(int time, int id) {
        throw new NoExtAPIException("method not supported.");
    }
}

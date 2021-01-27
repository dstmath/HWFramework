package ohos.agp.components;

import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.ComponentObserverHandler.Observer;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class ComponentObserverHandler<E extends Observer> {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_VIEW");
    protected final List<E> mObservers = new ArrayList();

    public interface Observer {
    }

    public void addObserver(E e) {
        this.mObservers.add(e);
    }

    public void removeObserver(E e) {
        this.mObservers.remove(e);
    }

    public int getObserversCount() {
        return this.mObservers.size();
    }

    public void onChange(int[] iArr) {
        if (this.mObservers.size() == 0) {
            HiLog.error(TAG, "mObservers is null, or size is 0.", new Object[0]);
        }
    }
}

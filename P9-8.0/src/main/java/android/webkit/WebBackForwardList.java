package android.webkit;

import java.io.Serializable;

public abstract class WebBackForwardList implements Cloneable, Serializable {
    protected abstract WebBackForwardList clone();

    public abstract int getCurrentIndex();

    public abstract WebHistoryItem getCurrentItem();

    public abstract WebHistoryItem getItemAtIndex(int i);

    public abstract int getSize();
}

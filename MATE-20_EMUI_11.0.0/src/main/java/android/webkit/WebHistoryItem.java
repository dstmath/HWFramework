package android.webkit;

import android.annotation.SystemApi;
import android.graphics.Bitmap;

public abstract class WebHistoryItem implements Cloneable {
    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public abstract WebHistoryItem clone();

    public abstract Bitmap getFavicon();

    @SystemApi
    @Deprecated
    public abstract int getId();

    public abstract String getOriginalUrl();

    public abstract String getTitle();

    public abstract String getUrl();
}

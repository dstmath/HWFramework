package android.media.update;

import android.os.Bundle;

public interface MediaBrowser2Provider extends MediaController2Provider {
    void getChildren_impl(String str, int i, int i2, Bundle bundle);

    void getItem_impl(String str);

    void getLibraryRoot_impl(Bundle bundle);

    void getSearchResult_impl(String str, int i, int i2, Bundle bundle);

    void search_impl(String str, Bundle bundle);

    void subscribe_impl(String str, Bundle bundle);

    void unsubscribe_impl(String str);
}

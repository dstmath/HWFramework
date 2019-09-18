package android.media.update;

import android.media.MediaSession2;
import android.os.Bundle;

public interface MediaLibraryService2Provider extends MediaSessionService2Provider {

    public interface LibraryRootProvider {
        Bundle getExtras_impl();

        String getRootId_impl();
    }

    public interface MediaLibrarySessionProvider extends MediaSession2Provider {
        void notifyChildrenChanged_impl(MediaSession2.ControllerInfo controllerInfo, String str, int i, Bundle bundle);

        void notifyChildrenChanged_impl(String str, int i, Bundle bundle);

        void notifySearchResultChanged_impl(MediaSession2.ControllerInfo controllerInfo, String str, int i, Bundle bundle);
    }
}

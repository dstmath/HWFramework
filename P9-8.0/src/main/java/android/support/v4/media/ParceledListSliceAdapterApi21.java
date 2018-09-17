package android.support.v4.media;

import android.media.browse.MediaBrowser.MediaItem;
import android.support.annotation.RequiresApi;
import java.lang.reflect.Constructor;
import java.util.List;

@RequiresApi(21)
class ParceledListSliceAdapterApi21 {
    private static Constructor sConstructor;

    ParceledListSliceAdapterApi21() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:2:0x0016 A:{Splitter: B:0:0x0000, ExcHandler: java.lang.ClassNotFoundException (r0_0 'e' java.lang.ReflectiveOperationException)} */
    /* JADX WARNING: Missing block: B:2:0x0016, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:3:0x0017, code:
            r0.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static {
        try {
            sConstructor = Class.forName("android.content.pm.ParceledListSlice").getConstructor(new Class[]{List.class});
        } catch (ReflectiveOperationException e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000e A:{Splitter: B:1:0x0001, ExcHandler: java.lang.InstantiationException (r0_0 'e' java.lang.ReflectiveOperationException)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x000e A:{Splitter: B:1:0x0001, ExcHandler: java.lang.InstantiationException (r0_0 'e' java.lang.ReflectiveOperationException)} */
    /* JADX WARNING: Missing block: B:3:0x000e, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:4:0x000f, code:
            r0.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static Object newInstance(List<MediaItem> itemList) {
        Object result = null;
        try {
            return sConstructor.newInstance(new Object[]{itemList});
        } catch (ReflectiveOperationException e) {
        }
    }
}

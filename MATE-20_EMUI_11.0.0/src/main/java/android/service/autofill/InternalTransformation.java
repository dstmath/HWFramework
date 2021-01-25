package android.service.autofill;

import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;
import android.view.autofill.Helper;
import android.widget.RemoteViews;
import java.util.ArrayList;

public abstract class InternalTransformation implements Transformation, Parcelable {
    private static final String TAG = "InternalTransformation";

    /* access modifiers changed from: package-private */
    public abstract void apply(ValueFinder valueFinder, RemoteViews remoteViews, int i) throws Exception;

    public static boolean batchApply(ValueFinder finder, RemoteViews template, ArrayList<Pair<Integer, InternalTransformation>> transformations) {
        int size = transformations.size();
        if (Helper.sDebug) {
            Log.d(TAG, "getPresentation(): applying " + size + " transformations");
        }
        for (int i = 0; i < size; i++) {
            Pair<Integer, InternalTransformation> pair = transformations.get(i);
            int id = pair.first.intValue();
            InternalTransformation transformation = pair.second;
            if (Helper.sDebug) {
                Log.d(TAG, "#" + i + ": " + transformation);
            }
            try {
                transformation.apply(finder, template, id);
            } catch (Exception e) {
                Log.e(TAG, "Could not apply transformation " + transformation + ": " + e.getClass());
                return false;
            }
        }
        return true;
    }
}

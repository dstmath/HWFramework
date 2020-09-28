package android.app.prediction;

import android.annotation.SystemApi;
import android.content.Context;
import com.android.internal.util.Preconditions;

@SystemApi
public final class AppPredictionManager {
    private final Context mContext;

    public AppPredictionManager(Context context) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
    }

    public AppPredictor createAppPredictionSession(AppPredictionContext predictionContext) {
        return new AppPredictor(this.mContext, predictionContext);
    }
}

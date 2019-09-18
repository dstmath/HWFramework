package android.service.settings.suggestions;

import android.annotation.SystemApi;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.service.settings.suggestions.ISuggestionService;
import java.util.List;

@SystemApi
public abstract class SuggestionService extends Service {
    private static final boolean DEBUG = false;
    private static final String TAG = "SuggestionService";

    public abstract List<Suggestion> onGetSuggestions();

    public abstract void onSuggestionDismissed(Suggestion suggestion);

    public abstract void onSuggestionLaunched(Suggestion suggestion);

    public IBinder onBind(Intent intent) {
        return new ISuggestionService.Stub() {
            public List<Suggestion> getSuggestions() {
                return SuggestionService.this.onGetSuggestions();
            }

            public void dismissSuggestion(Suggestion suggestion) {
                SuggestionService.this.onSuggestionDismissed(suggestion);
            }

            public void launchSuggestion(Suggestion suggestion) {
                SuggestionService.this.onSuggestionLaunched(suggestion);
            }
        };
    }
}

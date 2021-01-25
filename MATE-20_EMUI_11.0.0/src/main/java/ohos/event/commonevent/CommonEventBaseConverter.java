package ohos.event.commonevent;

import android.content.Intent;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import ohos.aafwk.content.IntentFilter;
import ohos.aafwk.content.IntentParams;

public class CommonEventBaseConverter {
    private static final String INTENT_PARAM_KEY_SCHEME = "scheme";

    public Optional<Intent> convertIntentToAospIntent(ohos.aafwk.content.Intent intent) {
        if (intent == null) {
            return Optional.empty();
        }
        Optional<Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, null);
        if (!createAndroidIntent.isPresent()) {
            return Optional.empty();
        }
        Optional<String> convertToAndroidAction = ActionMapper.convertToAndroidAction(intent.getAction());
        Intent intent2 = createAndroidIntent.get();
        Objects.requireNonNull(intent2);
        convertToAndroidAction.ifPresent(new Consumer(intent2) {
            /* class ohos.event.commonevent.$$Lambda$CommonEventBaseConverter$12V_ZTaCS7y0hOFDPX5jkA20ES8 */
            private final /* synthetic */ Intent f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                Intent unused = this.f$0.setAction((String) obj);
            }
        });
        return createAndroidIntent;
    }

    public Optional<ohos.aafwk.content.Intent> convertAospIntentToIntent(Intent intent) {
        if (intent == null) {
            return Optional.empty();
        }
        Optional<ohos.aafwk.content.Intent> createZidaneIntent = IntentConverter.createZidaneIntent(intent, null);
        if (!createZidaneIntent.isPresent()) {
            return Optional.empty();
        }
        Optional<String> convertToZidaneAction = ActionMapper.convertToZidaneAction(intent.getAction());
        ohos.aafwk.content.Intent intent2 = createZidaneIntent.get();
        Objects.requireNonNull(intent2);
        convertToZidaneAction.ifPresent(new Consumer() {
            /* class ohos.event.commonevent.$$Lambda$Tme3yMgwaotnSjh_pFBObRubKiw */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ohos.aafwk.content.Intent.this.setAction((String) obj);
            }
        });
        return createZidaneIntent;
    }

    public void convertIntentFilterToAospIntentFilter(IntentFilter intentFilter, android.content.IntentFilter intentFilter2) {
        if (!(intentFilter == null || intentFilter2 == null)) {
            int countActions = intentFilter.countActions();
            for (int i = 0; i < countActions; i++) {
                ActionMapper.convertToAndroidAction(intentFilter.getAction(i)).ifPresent(new Consumer(intentFilter2) {
                    /* class ohos.event.commonevent.$$Lambda$CommonEventBaseConverter$YDLdMjzncCg76qxA7jE0Ke8eVxE */
                    private final /* synthetic */ android.content.IntentFilter f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        this.f$0.addAction((String) obj);
                    }
                });
            }
            int countEntities = intentFilter.countEntities();
            for (int i2 = 0; i2 < countEntities; i2++) {
                intentFilter2.addCategory(intentFilter.getEntity(i2));
            }
            IntentParams intentParams = intentFilter.getIntentParams();
            if (intentParams != null) {
                Object param = intentParams.getParam(INTENT_PARAM_KEY_SCHEME);
                if (param instanceof String[]) {
                    for (String str : (String[]) param) {
                        intentFilter2.addDataScheme(str);
                    }
                }
            }
            int countSchemes = intentFilter.countSchemes();
            for (int i3 = 0; i3 < countSchemes; i3++) {
                intentFilter2.addDataScheme(intentFilter.getScheme(i3));
            }
        }
    }
}

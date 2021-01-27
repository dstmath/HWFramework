package ohos.abilityshell;

import android.content.Context;
import android.view.View;

public interface IAbilityShell {
    ClassLoader getSystemClassLoader();

    Context getSystemContext();

    void setSystemView(View view);
}

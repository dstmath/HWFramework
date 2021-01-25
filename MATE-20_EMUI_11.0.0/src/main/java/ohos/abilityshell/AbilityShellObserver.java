package ohos.abilityshell;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import ohos.aafwk.ability.IDataAbilityObserver;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;

public class AbilityShellObserver extends ContentObserver {
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private IDataAbilityObserver dataObserver;

    public AbilityShellObserver(Handler handler, IDataAbilityObserver iDataAbilityObserver) {
        super(handler);
        this.dataObserver = iDataAbilityObserver;
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean z) {
        AppLog.d(SHELL_LABEL, "AbilityShellObserver::onChange called", new Object[0]);
        this.dataObserver.onChange();
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean z, Uri uri) {
        onChange(z);
    }
}

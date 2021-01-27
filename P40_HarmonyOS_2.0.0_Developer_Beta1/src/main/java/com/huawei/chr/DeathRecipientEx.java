package com.huawei.chr;

import android.os.IHwBinder;

public class DeathRecipientEx {
    private IHwBinder.DeathRecipient deathRecipient = new IHwBinder.DeathRecipient() {
        /* class com.huawei.chr.DeathRecipientEx.AnonymousClass1 */

        public void serviceDied(long cookie) {
            DeathRecipientEx.this.serviceDied(cookie);
        }
    };

    public void serviceDied(long cookie) {
    }

    /* access modifiers changed from: package-private */
    public IHwBinder.DeathRecipient getDeathRecipient() {
        return this.deathRecipient;
    }
}

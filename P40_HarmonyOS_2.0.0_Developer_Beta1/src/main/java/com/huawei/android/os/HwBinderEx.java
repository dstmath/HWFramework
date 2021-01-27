package com.huawei.android.os;

import android.os.IHwBinder;

public class HwBinderEx {

    private static class DeathRecipientBridge implements IHwBinder.DeathRecipient {
        private DeathRecipientEx mDeathRecipientEx;

        private DeathRecipientBridge() {
        }

        public void setDeathRecipientEx(DeathRecipientEx deathRecipientEx) {
            this.mDeathRecipientEx = deathRecipientEx;
        }

        public void serviceDied(long cookie) {
            DeathRecipientEx deathRecipientEx = this.mDeathRecipientEx;
            if (deathRecipientEx != null) {
                deathRecipientEx.serviceDied(cookie);
            }
        }
    }

    public static class DeathRecipientEx {
        private DeathRecipientBridge mBridge = new DeathRecipientBridge();

        public DeathRecipientEx() {
            this.mBridge.setDeathRecipientEx(this);
        }

        public IHwBinder.DeathRecipient getDeathRecipient() {
            return this.mBridge;
        }

        public void serviceDied(long cookie) {
        }
    }
}

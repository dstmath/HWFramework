package com.android.server.connectivity;

import com.android.server.connectivity.usbp2p.UsbP2pManager;

public class HwTetheringEx implements IHwTetheringEx {

    private static final class Singleton {
        private static final IHwTetheringEx INSTANCE = new HwTetheringEx();

        private Singleton() {
        }
    }

    private HwTetheringEx() {
    }

    public static IHwTetheringEx getDefault() {
        return Singleton.INSTANCE;
    }

    public boolean isConflictWithUsbP2p(int actionType) {
        return UsbP2pManager.getInstance().isConflictWithUsbP2p(actionType);
    }
}

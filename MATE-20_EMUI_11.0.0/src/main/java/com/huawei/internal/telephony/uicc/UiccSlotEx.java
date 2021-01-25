package com.huawei.internal.telephony.uicc;

import android.content.Context;
import com.android.internal.telephony.uicc.UiccSlot;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class UiccSlotEx {
    private UiccCardExt mUiccCardExt;
    private UiccSlot mUiccSlot;

    public UiccSlotEx() {
        this.mUiccCardExt = new UiccCardExt();
    }

    public UiccSlotEx(Context c, boolean isActive) {
        this();
        this.mUiccSlot = new UiccSlot(c, isActive);
    }

    public UiccSlot getUiccSlot() {
        return this.mUiccSlot;
    }

    public void setUiccSlot(UiccSlot uiccSlot) {
        this.mUiccSlot = uiccSlot;
    }

    public void update(CommandsInterfaceEx ci, IccCardStatusExt ics, int phoneId, int slotIndex) {
        UiccSlot uiccSlot = this.mUiccSlot;
        if (uiccSlot != null) {
            uiccSlot.update(ci.getCommandsInterface(), ics.getIccCardStatus(), phoneId, slotIndex);
            this.mUiccCardExt.setUiccCard(this.mUiccSlot.getUiccCard());
        }
    }

    public UiccCardExt getUiccCard() {
        return this.mUiccCardExt;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        UiccSlot uiccSlot = this.mUiccSlot;
        if (uiccSlot != null) {
            uiccSlot.dump(fd, pw, args);
        }
    }
}

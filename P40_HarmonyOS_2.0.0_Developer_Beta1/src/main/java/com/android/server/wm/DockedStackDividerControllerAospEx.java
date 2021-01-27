package com.android.server.wm;

public class DockedStackDividerControllerAospEx {
    private DockedStackDividerController mDockedStackDividerController;

    public void setDockedStackDividerController(DockedStackDividerController dockedStackDividerController) {
        this.mDockedStackDividerController = dockedStackDividerController;
    }

    public void adjustBoundsForSingleHand() {
        this.mDockedStackDividerController.adjustBoundsForSingleHand();
    }
}

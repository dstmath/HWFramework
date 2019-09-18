package com.android.internal.app;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MicroAlertController extends AlertController {
    public MicroAlertController(Context context, DialogInterface di, Window window) {
        super(context, di, window);
    }

    /* access modifiers changed from: protected */
    public void setupContent(ViewGroup contentPanel) {
        this.mScrollView = (ScrollView) this.mWindow.findViewById(16909281);
        this.mMessageView = (TextView) contentPanel.findViewById(16908299);
        if (this.mMessageView != null) {
            if (this.mMessage != null) {
                this.mMessageView.setText(this.mMessage);
            } else {
                this.mMessageView.setVisibility(8);
                contentPanel.removeView(this.mMessageView);
                if (this.mListView != null) {
                    View topPanel = this.mScrollView.findViewById(16909457);
                    ((ViewGroup) topPanel.getParent()).removeView(topPanel);
                    FrameLayout.LayoutParams topParams = new FrameLayout.LayoutParams(topPanel.getLayoutParams());
                    topParams.gravity = 48;
                    topPanel.setLayoutParams(topParams);
                    View buttonPanel = this.mScrollView.findViewById(16908786);
                    ((ViewGroup) buttonPanel.getParent()).removeView(buttonPanel);
                    FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(buttonPanel.getLayoutParams());
                    buttonParams.gravity = 80;
                    buttonPanel.setLayoutParams(buttonParams);
                    ViewGroup scrollParent = (ViewGroup) this.mScrollView.getParent();
                    scrollParent.removeViewAt(scrollParent.indexOfChild(this.mScrollView));
                    scrollParent.addView(this.mListView, new ViewGroup.LayoutParams(-1, -1));
                    scrollParent.addView(topPanel);
                    scrollParent.addView(buttonPanel);
                } else {
                    contentPanel.setVisibility(8);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setupTitle(ViewGroup topPanel) {
        super.setupTitle(topPanel);
        if (topPanel.getVisibility() == 8) {
            topPanel.setVisibility(4);
        }
    }

    /* access modifiers changed from: protected */
    public void setupButtons(ViewGroup buttonPanel) {
        super.setupButtons(buttonPanel);
        if (buttonPanel.getVisibility() == 8) {
            buttonPanel.setVisibility(4);
        }
    }
}

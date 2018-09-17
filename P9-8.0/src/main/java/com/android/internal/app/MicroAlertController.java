package com.android.internal.app;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.internal.R;

public class MicroAlertController extends AlertController {
    public MicroAlertController(Context context, DialogInterface di, Window window) {
        super(context, di, window);
    }

    protected void setupContent(ViewGroup contentPanel) {
        this.mScrollView = (ScrollView) this.mWindow.findViewById(R.id.scrollView);
        this.mMessageView = (TextView) contentPanel.findViewById(R.id.message);
        if (this.mMessageView != null) {
            if (this.mMessage != null) {
                this.mMessageView.setText(this.mMessage);
            } else {
                this.mMessageView.setVisibility(8);
                contentPanel.removeView(this.mMessageView);
                if (this.mListView != null) {
                    View topPanel = this.mScrollView.findViewById(R.id.topPanel);
                    ((ViewGroup) topPanel.getParent()).removeView(topPanel);
                    LayoutParams topParams = new LayoutParams(topPanel.getLayoutParams());
                    topParams.gravity = 48;
                    topPanel.-wrap18(topParams);
                    View buttonPanel = this.mScrollView.findViewById(R.id.buttonPanel);
                    ((ViewGroup) buttonPanel.getParent()).removeView(buttonPanel);
                    LayoutParams buttonParams = new LayoutParams(buttonPanel.getLayoutParams());
                    buttonParams.gravity = 80;
                    buttonPanel.-wrap18(buttonParams);
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

    protected void setupTitle(ViewGroup topPanel) {
        super.setupTitle(topPanel);
        if (topPanel.getVisibility() == 8) {
            topPanel.setVisibility(4);
        }
    }

    protected void setupButtons(ViewGroup buttonPanel) {
        super.setupButtons(buttonPanel);
        if (buttonPanel.getVisibility() == 8) {
            buttonPanel.setVisibility(4);
        }
    }
}

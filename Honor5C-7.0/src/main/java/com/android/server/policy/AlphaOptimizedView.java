package com.android.server.policy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class AlphaOptimizedView extends View {
    public AlphaOptimizedView(Context context) {
        super(context);
    }

    public AlphaOptimizedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlphaOptimizedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AlphaOptimizedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}

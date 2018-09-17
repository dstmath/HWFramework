package com.android.internal.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.provider.Settings.System;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import com.android.internal.R;

public class PlatLogoActivity extends Activity {
    public static final boolean FINISH = false;
    public static final boolean REVEAL_THE_NAME = false;
    PathInterpolator mInterpolator = new PathInterpolator(0.0f, 0.0f, 0.5f, 1.0f);
    int mKeyCount;
    FrameLayout mLayout;
    int mTapCount;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLayout = new FrameLayout(this);
        setContentView(this.mLayout);
    }

    public void onAttachedToWindow() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float dp = dm.density;
        int size = (int) (Math.min((float) Math.min(dm.widthPixels, dm.heightPixels), 600.0f * dp) - (100.0f * dp));
        final ImageView im = new ImageView(this);
        int pad = (int) (40.0f * dp);
        im.setPadding(pad, pad, pad, pad);
        im.setTranslationZ(20.0f);
        im.setScaleX(0.5f);
        im.setScaleY(0.5f);
        im.setAlpha(0.0f);
        im.setBackground(new RippleDrawable(ColorStateList.valueOf(-1), getDrawable(R.drawable.platlogo), null));
        im.setClickable(true);
        im.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ImageView imageView = im;
                final ImageView imageView2 = im;
                imageView.setOnLongClickListener(new OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        if (PlatLogoActivity.this.mTapCount < 5) {
                            return false;
                        }
                        ContentResolver cr = PlatLogoActivity.this.getContentResolver();
                        if (System.getLong(cr, System.EGG_MODE, 0) == 0) {
                            try {
                                System.putLong(cr, System.EGG_MODE, System.currentTimeMillis());
                            } catch (RuntimeException e) {
                                Log.e("PlatLogoActivity", "Can't write settings", e);
                            }
                        }
                        imageView2.post(new Runnable() {
                            public void run() {
                                try {
                                    PlatLogoActivity.this.startActivity(new Intent("android.intent.action.MAIN").setFlags(276856832).addCategory("com.android.internal.category.PLATLOGO"));
                                } catch (ActivityNotFoundException e) {
                                    Log.e("PlatLogoActivity", "No more eggs.");
                                }
                            }
                        });
                        return true;
                    }
                });
                PlatLogoActivity platLogoActivity = PlatLogoActivity.this;
                platLogoActivity.mTapCount++;
            }
        });
        im.setFocusable(true);
        im.requestFocus();
        im.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == 4 || event.getAction() != 0) {
                    return false;
                }
                PlatLogoActivity platLogoActivity = PlatLogoActivity.this;
                platLogoActivity.mKeyCount++;
                if (PlatLogoActivity.this.mKeyCount > 2) {
                    if (PlatLogoActivity.this.mTapCount > 5) {
                        im.-wrap11();
                    } else {
                        im.performClick();
                    }
                }
                return true;
            }
        });
        this.mLayout.addView((View) im, new LayoutParams(size, size, 17));
        im.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setInterpolator(this.mInterpolator).setDuration(500).setStartDelay(800).start();
    }
}

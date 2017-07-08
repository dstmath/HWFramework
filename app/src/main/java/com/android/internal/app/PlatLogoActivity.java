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
import android.view.WindowManager.LayoutParams;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.internal.R;

public class PlatLogoActivity extends Activity {
    public static final boolean FINISH = false;
    public static final boolean REVEAL_THE_NAME = false;
    PathInterpolator mInterpolator;
    int mKeyCount;
    FrameLayout mLayout;
    int mTapCount;

    /* renamed from: com.android.internal.app.PlatLogoActivity.1 */
    class AnonymousClass1 implements OnClickListener {
        final /* synthetic */ ImageView val$im;

        /* renamed from: com.android.internal.app.PlatLogoActivity.1.1 */
        class AnonymousClass1 implements OnLongClickListener {
            final /* synthetic */ ImageView val$im;

            AnonymousClass1(ImageView val$im) {
                this.val$im = val$im;
            }

            public boolean onLongClick(View v) {
                if (PlatLogoActivity.this.mTapCount < 5) {
                    return false;
                }
                ContentResolver cr = PlatLogoActivity.this.getContentResolver();
                if (System.getLong(cr, "egg_mode", 0) == 0) {
                    try {
                        System.putLong(cr, "egg_mode", System.currentTimeMillis());
                    } catch (RuntimeException e) {
                        Log.e("PlatLogoActivity", "Can't write settings", e);
                    }
                }
                this.val$im.post(new Runnable() {
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
        }

        AnonymousClass1(ImageView val$im) {
            this.val$im = val$im;
        }

        public void onClick(View v) {
            this.val$im.setOnLongClickListener(new AnonymousClass1(this.val$im));
            PlatLogoActivity platLogoActivity = PlatLogoActivity.this;
            platLogoActivity.mTapCount++;
        }
    }

    /* renamed from: com.android.internal.app.PlatLogoActivity.2 */
    class AnonymousClass2 implements OnKeyListener {
        final /* synthetic */ ImageView val$im;

        AnonymousClass2(ImageView val$im) {
            this.val$im = val$im;
        }

        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == 4 || event.getAction() != 0) {
                return false;
            }
            PlatLogoActivity platLogoActivity = PlatLogoActivity.this;
            platLogoActivity.mKeyCount++;
            if (PlatLogoActivity.this.mKeyCount > 2) {
                if (PlatLogoActivity.this.mTapCount > 5) {
                    this.val$im.performLongClick();
                } else {
                    this.val$im.performClick();
                }
            }
            return true;
        }
    }

    public PlatLogoActivity() {
        this.mInterpolator = new PathInterpolator(0.0f, 0.0f, 0.5f, LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLayout = new FrameLayout(this);
        setContentView(this.mLayout);
    }

    public void onAttachedToWindow() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float dp = dm.density;
        int size = (int) (Math.min((float) Math.min(dm.widthPixels, dm.heightPixels), 600.0f * dp) - (100.0f * dp));
        ImageView im = new ImageView(this);
        int pad = (int) (40.0f * dp);
        im.setPadding(pad, pad, pad, pad);
        im.setTranslationZ(20.0f);
        im.setScaleX(0.5f);
        im.setScaleY(0.5f);
        im.setAlpha(0.0f);
        im.setBackground(new RippleDrawable(ColorStateList.valueOf(-1), getDrawable(R.drawable.platlogo), null));
        im.setClickable(true);
        im.setOnClickListener(new AnonymousClass1(im));
        im.setFocusable(true);
        im.requestFocus();
        im.setOnKeyListener(new AnonymousClass2(im));
        this.mLayout.addView((View) im, new FrameLayout.LayoutParams(size, size, 17));
        im.animate().scaleX(LayoutParams.BRIGHTNESS_OVERRIDE_FULL).scaleY(LayoutParams.BRIGHTNESS_OVERRIDE_FULL).alpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL).setInterpolator(this.mInterpolator).setDuration(500).setStartDelay(800).start();
    }
}

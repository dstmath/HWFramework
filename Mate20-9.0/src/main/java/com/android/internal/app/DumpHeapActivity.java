package com.android.internal.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DebugUtils;
import android.util.Slog;

public class DumpHeapActivity extends Activity {
    public static final String ACTION_DELETE_DUMPHEAP = "com.android.server.am.DELETE_DUMPHEAP";
    public static final String EXTRA_DELAY_DELETE = "delay_delete";
    public static final Uri JAVA_URI = Uri.parse("content://com.android.server.heapdump/java");
    public static final String KEY_DIRECT_LAUNCH = "direct_launch";
    public static final String KEY_PROCESS = "process";
    public static final String KEY_SIZE = "size";
    AlertDialog mDialog;
    boolean mHandled = false;
    String mProcess;
    long mSize;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mProcess = getIntent().getStringExtra(KEY_PROCESS);
        this.mSize = getIntent().getLongExtra(KEY_SIZE, 0);
        String directLaunch = getIntent().getStringExtra(KEY_DIRECT_LAUNCH);
        if (directLaunch != null) {
            Intent intent = new Intent("android.app.action.REPORT_HEAP_LIMIT");
            intent.setPackage(directLaunch);
            ClipData clip = ClipData.newUri(getContentResolver(), "Heap Dump", JAVA_URI);
            intent.setClipData(clip);
            intent.addFlags(1);
            intent.setType(clip.getDescription().getMimeType(0));
            intent.putExtra("android.intent.extra.STREAM", JAVA_URI);
            try {
                startActivity(intent);
                scheduleDelete();
                this.mHandled = true;
                finish();
                return;
            } catch (ActivityNotFoundException e) {
                Slog.i("DumpHeapActivity", "Unable to direct launch to " + directLaunch + ": " + e.getMessage());
            }
        }
        AlertDialog.Builder b = new AlertDialog.Builder(this, 16974394);
        b.setTitle(17039962);
        b.setMessage(getString(17039961, new Object[]{this.mProcess, DebugUtils.sizeValueToString(this.mSize, null)}));
        b.setNegativeButton(17039360, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DumpHeapActivity.this.mHandled = true;
                DumpHeapActivity.this.sendBroadcast(new Intent(DumpHeapActivity.ACTION_DELETE_DUMPHEAP));
                DumpHeapActivity.this.finish();
            }
        });
        b.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DumpHeapActivity.this.mHandled = true;
                DumpHeapActivity.this.scheduleDelete();
                Intent intent = new Intent("android.intent.action.SEND");
                ClipData clip = ClipData.newUri(DumpHeapActivity.this.getContentResolver(), "Heap Dump", DumpHeapActivity.JAVA_URI);
                intent.setClipData(clip);
                intent.addFlags(1);
                intent.setType(clip.getDescription().getMimeType(0));
                intent.putExtra("android.intent.extra.STREAM", DumpHeapActivity.JAVA_URI);
                DumpHeapActivity.this.startActivity(Intent.createChooser(intent, DumpHeapActivity.this.getText(17039962)));
                DumpHeapActivity.this.finish();
            }
        });
        this.mDialog = b.show();
    }

    /* access modifiers changed from: package-private */
    public void scheduleDelete() {
        Intent broadcast = new Intent(ACTION_DELETE_DUMPHEAP);
        broadcast.putExtra(EXTRA_DELAY_DELETE, true);
        sendBroadcast(broadcast);
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        if (!isChangingConfigurations() && !this.mHandled) {
            sendBroadcast(new Intent(ACTION_DELETE_DUMPHEAP));
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        this.mDialog.dismiss();
    }
}

package com.android.internal.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DebugUtils;
import android.util.Slog;
import com.android.internal.R;

public class DumpHeapActivity extends Activity {
    public static final String ACTION_DELETE_DUMPHEAP = "com.android.server.am.DELETE_DUMPHEAP";
    public static final String EXTRA_DELAY_DELETE = "delay_delete";
    public static final Uri JAVA_URI = null;
    public static final String KEY_DIRECT_LAUNCH = "direct_launch";
    public static final String KEY_PROCESS = "process";
    public static final String KEY_SIZE = "size";
    AlertDialog mDialog;
    boolean mHandled;
    String mProcess;
    long mSize;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.DumpHeapActivity.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.app.DumpHeapActivity.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.DumpHeapActivity.<clinit>():void");
    }

    public DumpHeapActivity() {
        this.mHandled = false;
    }

    protected void onCreate(Bundle savedInstanceState) {
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
        Builder b = new Builder(this, R.style.Theme_Material_Light_Dialog_Alert);
        b.setTitle(R.string.dump_heap_title);
        b.setMessage(getString(R.string.dump_heap_text, new Object[]{this.mProcess, DebugUtils.sizeValueToString(this.mSize, null)}));
        b.setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DumpHeapActivity.this.mHandled = true;
                DumpHeapActivity.this.sendBroadcast(new Intent(DumpHeapActivity.ACTION_DELETE_DUMPHEAP));
                DumpHeapActivity.this.finish();
            }
        });
        b.setPositiveButton(R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DumpHeapActivity.this.mHandled = true;
                DumpHeapActivity.this.scheduleDelete();
                Intent intent = new Intent("android.intent.action.SEND");
                ClipData clip = ClipData.newUri(DumpHeapActivity.this.getContentResolver(), "Heap Dump", DumpHeapActivity.JAVA_URI);
                intent.setClipData(clip);
                intent.addFlags(1);
                intent.setType(clip.getDescription().getMimeType(0));
                intent.putExtra("android.intent.extra.STREAM", DumpHeapActivity.JAVA_URI);
                DumpHeapActivity.this.startActivity(Intent.createChooser(intent, DumpHeapActivity.this.getText(R.string.dump_heap_title)));
                DumpHeapActivity.this.finish();
            }
        });
        this.mDialog = b.show();
    }

    void scheduleDelete() {
        Intent broadcast = new Intent(ACTION_DELETE_DUMPHEAP);
        broadcast.putExtra(EXTRA_DELAY_DELETE, true);
        sendBroadcast(broadcast);
    }

    protected void onStop() {
        super.onStop();
        if (!isChangingConfigurations() && !this.mHandled) {
            sendBroadcast(new Intent(ACTION_DELETE_DUMPHEAP));
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mDialog.dismiss();
    }
}

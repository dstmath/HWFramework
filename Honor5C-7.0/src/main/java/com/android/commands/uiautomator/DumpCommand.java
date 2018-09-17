package com.android.commands.uiautomator;

import android.app.UiAutomation;
import android.graphics.Point;
import android.hardware.display.DisplayManagerGlobal;
import android.view.Display;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.commands.uiautomator.Launcher.Command;
import com.android.uiautomator.core.AccessibilityNodeInfoDumper;
import com.android.uiautomator.core.UiAutomationShellWrapper;
import java.io.File;
import java.util.concurrent.TimeoutException;

public class DumpCommand extends Command {
    private static final File DEFAULT_DUMP_FILE = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.commands.uiautomator.DumpCommand.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.commands.uiautomator.DumpCommand.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.uiautomator.DumpCommand.<clinit>():void");
    }

    public DumpCommand() {
        super("dump");
    }

    public String shortHelp() {
        return "creates an XML dump of current UI hierarchy";
    }

    public String detailedOptions() {
        return "    dump [--verbose][file]\n      [--compressed]: dumps compressed layout information.\n      [file]: the location where the dumped XML should be stored, default is\n      " + DEFAULT_DUMP_FILE.getAbsolutePath() + "\n";
    }

    public void run(String[] args) {
        File dumpFile = DEFAULT_DUMP_FILE;
        boolean verboseMode = true;
        for (String arg : args) {
            if (arg.equals("--compressed")) {
                verboseMode = false;
            } else if (!arg.startsWith("-")) {
                dumpFile = new File(arg);
            }
        }
        UiAutomationShellWrapper automationWrapper = new UiAutomationShellWrapper();
        automationWrapper.connect();
        if (verboseMode) {
            automationWrapper.setCompressedLayoutHierarchy(false);
        } else {
            automationWrapper.setCompressedLayoutHierarchy(true);
        }
        try {
            UiAutomation uiAutomation = automationWrapper.getUiAutomation();
            uiAutomation.waitForIdle(1000, 10000);
            AccessibilityNodeInfo info = uiAutomation.getRootInActiveWindow();
            if (info == null) {
                System.err.println("ERROR: null root node returned by UiTestAutomationBridge.");
                return;
            }
            Display display = DisplayManagerGlobal.getInstance().getRealDisplay(0);
            int rotation = display.getRotation();
            Point size = new Point();
            display.getSize(size);
            AccessibilityNodeInfoDumper.dumpWindowToFile(info, dumpFile, rotation, size.x, size.y);
            automationWrapper.disconnect();
            System.out.println(String.format("UI hierchary dumped to: %s", new Object[]{dumpFile.getAbsolutePath()}));
        } catch (TimeoutException e) {
            System.err.println("ERROR: could not get idle state.");
        } finally {
            automationWrapper.disconnect();
        }
    }
}

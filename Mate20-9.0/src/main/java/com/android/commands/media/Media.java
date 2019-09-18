package com.android.commands.media;

import android.app.ActivityManager;
import android.content.pm.ParceledListSlice;
import android.media.MediaMetadata;
import android.media.session.ISessionController;
import android.media.session.ISessionControllerCallback;
import android.media.session.ISessionManager;
import android.media.session.ParcelableVolumeInfo;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.AndroidException;
import android.view.KeyEvent;
import com.android.internal.os.BaseCommand;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Iterator;

public class Media extends BaseCommand {
    private static final String PACKAGE_NAME = "";
    private ISessionManager mSessionService;

    class ControllerMonitor extends ISessionControllerCallback.Stub {
        /* access modifiers changed from: private */
        public final ISessionController mController;

        public ControllerMonitor(ISessionController controller) {
            this.mController = controller;
        }

        public void onSessionDestroyed() {
            System.out.println("onSessionDestroyed. Enter q to quit.");
        }

        public void onEvent(String event, Bundle extras) {
            PrintStream printStream = System.out;
            printStream.println("onSessionEvent event=" + event + ", extras=" + extras);
        }

        public void onPlaybackStateChanged(PlaybackState state) {
            PrintStream printStream = System.out;
            printStream.println("onPlaybackStateChanged " + state);
        }

        public void onMetadataChanged(MediaMetadata metadata) {
            String mmString;
            if (metadata == null) {
                mmString = null;
            } else {
                mmString = "title=" + metadata.getDescription();
            }
            System.out.println("onMetadataChanged " + mmString);
        }

        public void onQueueChanged(ParceledListSlice queue) throws RemoteException {
            String str;
            PrintStream printStream = System.out;
            StringBuilder sb = new StringBuilder();
            sb.append("onQueueChanged, ");
            if (queue == null) {
                str = "null queue";
            } else {
                str = " size=" + queue.getList().size();
            }
            sb.append(str);
            printStream.println(sb.toString());
        }

        public void onQueueTitleChanged(CharSequence title) throws RemoteException {
            PrintStream printStream = System.out;
            printStream.println("onQueueTitleChange " + title);
        }

        public void onExtrasChanged(Bundle extras) throws RemoteException {
            PrintStream printStream = System.out;
            printStream.println("onExtrasChanged " + extras);
        }

        public void onVolumeInfoChanged(ParcelableVolumeInfo info) throws RemoteException {
            PrintStream printStream = System.out;
            printStream.println("onVolumeInfoChanged " + info);
        }

        /* access modifiers changed from: package-private */
        public void printUsageMessage() {
            try {
                PrintStream printStream = System.out;
                printStream.println("V2Monitoring session " + this.mController.getTag() + "...  available commands: play, pause, next, previous");
            } catch (RemoteException e) {
                System.out.println("Error trying to monitor session!");
            }
            System.out.println("(q)uit: finish monitoring");
        }

        /* access modifiers changed from: package-private */
        public void run() throws RemoteException {
            printUsageMessage();
            HandlerThread cbThread = new HandlerThread("MediaCb") {
                /* access modifiers changed from: protected */
                public void onLooperPrepared() {
                    try {
                        ControllerMonitor.this.mController.registerCallbackListener(Media.PACKAGE_NAME, ControllerMonitor.this);
                    } catch (RemoteException e) {
                        System.out.println("Error registering monitor callback");
                    }
                }
            };
            cbThread.start();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    String readLine = in.readLine();
                    String line = readLine;
                    if (readLine == null) {
                        break;
                    }
                    boolean addNewline = true;
                    if (line.length() <= 0) {
                        addNewline = false;
                    } else if ("q".equals(line)) {
                        break;
                    } else if ("quit".equals(line)) {
                        break;
                    } else if ("play".equals(line)) {
                        dispatchKeyCode(126);
                    } else if ("pause".equals(line)) {
                        dispatchKeyCode(127);
                    } else if ("next".equals(line)) {
                        dispatchKeyCode(87);
                    } else if ("previous".equals(line)) {
                        dispatchKeyCode(88);
                    } else {
                        PrintStream printStream = System.out;
                        printStream.println("Invalid command: " + line);
                    }
                    synchronized (this) {
                        if (addNewline) {
                            System.out.println(Media.PACKAGE_NAME);
                        }
                        printUsageMessage();
                    }
                }
                cbThread.getLooper().quit();
                try {
                    this.mController.unregisterCallbackListener(this);
                } catch (Exception e) {
                }
            } catch (IOException e2) {
                try {
                    e2.printStackTrace();
                } finally {
                    cbThread.getLooper().quit();
                    try {
                        this.mController.unregisterCallbackListener(this);
                    } catch (Exception e3) {
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }

        private void dispatchKeyCode(int keyCode) {
            long now = SystemClock.uptimeMillis();
            long j = now;
            long j2 = now;
            int i = keyCode;
            KeyEvent down = new KeyEvent(j, j2, 0, i, 0, 0, -1, 0, 0, 257);
            long j3 = now;
            KeyEvent down2 = down;
            KeyEvent keyEvent = new KeyEvent(j, j2, 1, i, 0, 0, -1, 0, 0, 257);
            KeyEvent up = keyEvent;
            try {
                this.mController.sendMediaButton(Media.PACKAGE_NAME, null, false, down2);
                this.mController.sendMediaButton(Media.PACKAGE_NAME, null, false, up);
                int i2 = keyCode;
            } catch (RemoteException e) {
                PrintStream printStream = System.out;
                printStream.println("Failed to dispatch " + keyCode);
            }
        }
    }

    public static void main(String[] args) {
        new Media().run(args);
    }

    public void onShowUsage(PrintStream out) {
        out.println("usage: media [subcommand] [options]\n       media dispatch KEY\n       media list-sessions\n       media monitor <tag>\n       media volume [options]\n\nmedia dispatch: dispatch a media key to the system.\n                KEY may be: play, pause, play-pause, mute, headsethook,\n                stop, next, previous, rewind, record, fast-forword.\nmedia list-sessions: print a list of the current sessions.\nmedia monitor: monitor updates to the specified session.\n                       Use the tag from list-sessions.\nmedia volume:  " + VolumeCtrl.USAGE);
    }

    public void onRun() throws Exception {
        this.mSessionService = ISessionManager.Stub.asInterface(ServiceManager.checkService("media_session"));
        if (this.mSessionService != null) {
            String op = nextArgRequired();
            if (op.equals("dispatch")) {
                runDispatch();
            } else if (op.equals("list-sessions")) {
                runListSessions();
            } else if (op.equals("monitor")) {
                runMonitor();
            } else if (op.equals("volume")) {
                runVolume();
            } else {
                showError("Error: unknown command '" + op + "'");
                return;
            }
            return;
        }
        System.err.println("Error type 2");
        throw new AndroidException("Can't connect to media session service; is the system running?");
    }

    private void sendMediaKey(KeyEvent event) {
        try {
            this.mSessionService.dispatchMediaKeyEvent(PACKAGE_NAME, false, event, false);
        } catch (RemoteException e) {
        }
    }

    private void runMonitor() throws Exception {
        String id = nextArgRequired();
        if (id == null) {
            showError("Error: must include a session id");
            return;
        }
        boolean success = false;
        try {
            Iterator<IBinder> it = this.mSessionService.getSessions(null, ActivityManager.getCurrentUser()).iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ISessionController controller = ISessionController.Stub.asInterface(it.next());
                if (controller != null) {
                    try {
                        if (id.equals(controller.getTag())) {
                            new ControllerMonitor(controller).run();
                            success = true;
                            break;
                        }
                    } catch (RemoteException e) {
                    }
                }
            }
        } catch (Exception e2) {
            PrintStream printStream = System.out;
            printStream.println("***Error monitoring session*** " + e2.getMessage());
        }
        if (!success) {
            PrintStream printStream2 = System.out;
            printStream2.println("No session found with id " + id);
        }
    }

    private void runDispatch() throws Exception {
        int keycode;
        String cmd = nextArgRequired();
        if ("play".equals(cmd)) {
            keycode = 126;
        } else if ("pause".equals(cmd)) {
            keycode = 127;
        } else if ("play-pause".equals(cmd)) {
            keycode = 85;
        } else if ("mute".equals(cmd)) {
            keycode = 91;
        } else if ("headsethook".equals(cmd)) {
            keycode = 79;
        } else if ("stop".equals(cmd)) {
            keycode = 86;
        } else if ("next".equals(cmd)) {
            keycode = 87;
        } else if ("previous".equals(cmd)) {
            keycode = 88;
        } else if ("rewind".equals(cmd)) {
            keycode = 89;
        } else if ("record".equals(cmd)) {
            keycode = 130;
        } else if ("fast-forward".equals(cmd)) {
            keycode = 90;
        } else {
            showError("Error: unknown dispatch code '" + cmd + "'");
            return;
        }
        int keycode2 = keycode;
        long now = SystemClock.uptimeMillis();
        KeyEvent keyEvent = new KeyEvent(now, now, 0, keycode2, 0, 0, -1, 0, 0, 257);
        sendMediaKey(keyEvent);
        KeyEvent keyEvent2 = new KeyEvent(now, now, 1, keycode2, 0, 0, -1, 0, 0, 257);
        sendMediaKey(keyEvent2);
    }

    private void runListSessions() {
        System.out.println("Sessions:");
        try {
            for (IBinder session : this.mSessionService.getSessions(null, ActivityManager.getCurrentUser())) {
                ISessionController controller = ISessionController.Stub.asInterface(session);
                if (controller != null) {
                    try {
                        PrintStream printStream = System.out;
                        printStream.println("  tag=" + controller.getTag() + ", package=" + controller.getPackageName());
                    } catch (RemoteException e) {
                    }
                }
            }
        } catch (Exception e2) {
            System.out.println("***Error listing sessions***");
        }
    }

    private void runVolume() throws Exception {
        VolumeCtrl.run(this);
    }
}

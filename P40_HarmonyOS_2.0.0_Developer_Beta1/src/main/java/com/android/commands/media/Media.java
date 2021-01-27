package com.android.commands.media;

import android.app.ActivityThread;
import android.media.MediaMetadata;
import android.media.session.ISessionManager;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
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
import java.util.List;

public class Media extends BaseCommand {
    private static final String PACKAGE_NAME = "";
    private static MediaSessionManager sMediaSessionManager;
    private static ActivityThread sThread;
    private ISessionManager mSessionService;

    public static void main(String[] args) {
        new Media().run(args);
    }

    public void onShowUsage(PrintStream out) {
        out.println("usage: media [subcommand] [options]\n       media dispatch KEY\n       media list-sessions\n       media monitor <tag>\n       media volume [options]\n\nmedia dispatch: dispatch a media key to the system.\n                KEY may be: play, pause, play-pause, mute, headsethook,\n                stop, next, previous, rewind, record, fast-forword.\nmedia list-sessions: print a list of the current sessions.\nmedia monitor: monitor updates to the specified session.\n                       Use the tag from list-sessions.\nmedia volume:  " + VolumeCtrl.USAGE);
    }

    public void onRun() throws Exception {
        if (sThread == null) {
            Looper.prepareMainLooper();
            sThread = ActivityThread.systemMain();
            sMediaSessionManager = (MediaSessionManager) sThread.getSystemContext().getSystemService("media_session");
        }
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
            }
        } else {
            System.err.println("Error type 2");
            throw new AndroidException("Can't connect to media session service; is the system running?");
        }
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
            Iterator<MediaController> it = sMediaSessionManager.getActiveSessions(null).iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                MediaController controller = it.next();
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
        long now = SystemClock.uptimeMillis();
        sendMediaKey(new KeyEvent(now, now, 0, keycode, 0, 0, -1, 0, 0, 257));
        sendMediaKey(new KeyEvent(now, now, 1, keycode, 0, 0, -1, 0, 0, 257));
    }

    /* access modifiers changed from: package-private */
    public class ControllerCallback extends MediaController.Callback {
        ControllerCallback() {
        }

        @Override // android.media.session.MediaController.Callback
        public void onSessionDestroyed() {
            System.out.println("onSessionDestroyed. Enter q to quit.");
        }

        @Override // android.media.session.MediaController.Callback
        public void onSessionEvent(String event, Bundle extras) {
            PrintStream printStream = System.out;
            printStream.println("onSessionEvent event=" + event + ", extras=" + extras);
        }

        @Override // android.media.session.MediaController.Callback
        public void onPlaybackStateChanged(PlaybackState state) {
            PrintStream printStream = System.out;
            printStream.println("onPlaybackStateChanged " + state);
        }

        @Override // android.media.session.MediaController.Callback
        public void onMetadataChanged(MediaMetadata metadata) {
            String mmString;
            if (metadata == null) {
                mmString = null;
            } else {
                mmString = "title=" + metadata.getDescription();
            }
            System.out.println("onMetadataChanged " + mmString);
        }

        @Override // android.media.session.MediaController.Callback
        public void onQueueChanged(List<MediaSession.QueueItem> queue) {
            String str;
            PrintStream printStream = System.out;
            StringBuilder sb = new StringBuilder();
            sb.append("onQueueChanged, ");
            if (queue == null) {
                str = "null queue";
            } else {
                str = " size=" + queue.size();
            }
            sb.append(str);
            printStream.println(sb.toString());
        }

        @Override // android.media.session.MediaController.Callback
        public void onQueueTitleChanged(CharSequence title) {
            PrintStream printStream = System.out;
            printStream.println("onQueueTitleChange " + ((Object) title));
        }

        @Override // android.media.session.MediaController.Callback
        public void onExtrasChanged(Bundle extras) {
            PrintStream printStream = System.out;
            printStream.println("onExtrasChanged " + extras);
        }

        @Override // android.media.session.MediaController.Callback
        public void onAudioInfoChanged(MediaController.PlaybackInfo info) {
            PrintStream printStream = System.out;
            printStream.println("onAudioInfoChanged " + info);
        }
    }

    /* access modifiers changed from: private */
    public class ControllerMonitor {
        private final MediaController mController;
        private final ControllerCallback mControllerCallback;

        ControllerMonitor(MediaController controller) {
            this.mController = controller;
            this.mControllerCallback = new ControllerCallback();
        }

        /* access modifiers changed from: package-private */
        public void printUsageMessage() {
            try {
                PrintStream printStream = System.out;
                printStream.println("V2Monitoring session " + this.mController.getTag() + "...  available commands: play, pause, next, previous");
            } catch (RuntimeException e) {
                System.out.println("Error trying to monitor session!");
            }
            System.out.println("(q)uit: finish monitoring");
        }

        /* JADX INFO: finally extract failed */
        /* access modifiers changed from: package-private */
        public void run() throws RemoteException {
            printUsageMessage();
            HandlerThread cbThread = new HandlerThread("MediaCb") {
                /* class com.android.commands.media.Media.ControllerMonitor.AnonymousClass1 */

                /* access modifiers changed from: protected */
                @Override // android.os.HandlerThread
                public void onLooperPrepared() {
                    try {
                        ControllerMonitor.this.mController.registerCallback(ControllerMonitor.this.mControllerCallback);
                    } catch (RuntimeException e) {
                        System.out.println("Error registering monitor callback");
                    }
                }
            };
            cbThread.start();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
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
                    this.mController.unregisterCallback(this.mControllerCallback);
                } catch (Exception e) {
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                cbThread.getLooper().quit();
                this.mController.unregisterCallback(this.mControllerCallback);
            } catch (Throwable th) {
                cbThread.getLooper().quit();
                try {
                    this.mController.unregisterCallback(this.mControllerCallback);
                } catch (Exception e3) {
                }
                throw th;
            }
        }

        private void dispatchKeyCode(int keyCode) {
            long now = SystemClock.uptimeMillis();
            KeyEvent down = new KeyEvent(now, now, 0, keyCode, 0, 0, -1, 0, 0, 257);
            KeyEvent up = new KeyEvent(now, now, 1, keyCode, 0, 0, -1, 0, 0, 257);
            try {
                this.mController.dispatchMediaButtonEvent(down);
                this.mController.dispatchMediaButtonEvent(up);
            } catch (RuntimeException e) {
                PrintStream printStream = System.out;
                printStream.println("Failed to dispatch " + keyCode);
            }
        }
    }

    private void runListSessions() {
        System.out.println("Sessions:");
        try {
            for (MediaController controller : sMediaSessionManager.getActiveSessions(null)) {
                if (controller != null) {
                    try {
                        PrintStream printStream = System.out;
                        printStream.println("  tag=" + controller.getTag() + ", package=" + controller.getPackageName());
                    } catch (RuntimeException e) {
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

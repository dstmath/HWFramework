package com.android.internal.os;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;
import android.util.Log;
import com.android.internal.os.Zygote.MethodAndArgsCaller;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;

class ZygoteServer {
    private static final String ANDROID_SOCKET_PREFIX = "ANDROID_SOCKET_";
    public static final String TAG = "ZygoteServer";
    private LocalServerSocket mServerSocket;

    ZygoteServer() {
    }

    void registerServerSocket(String socketName) {
        if (this.mServerSocket == null) {
            String fullSocketName = ANDROID_SOCKET_PREFIX + socketName;
            try {
                int fileDesc = Integer.parseInt(System.getenv(fullSocketName));
                try {
                    FileDescriptor fd = new FileDescriptor();
                    fd.setInt$(fileDesc);
                    this.mServerSocket = new LocalServerSocket(fd);
                } catch (IOException ex) {
                    throw new RuntimeException("Error binding to local socket '" + fileDesc + "'", ex);
                }
            } catch (RuntimeException ex2) {
                throw new RuntimeException(fullSocketName + " unset or invalid", ex2);
            }
        }
    }

    private ZygoteConnection acceptCommandPeer(String abiList) {
        try {
            return createNewConnection(this.mServerSocket.accept(), abiList);
        } catch (IOException ex) {
            throw new RuntimeException("IOException during accept()", ex);
        }
    }

    protected ZygoteConnection createNewConnection(LocalSocket socket, String abiList) throws IOException {
        return new ZygoteConnection(socket, abiList);
    }

    void closeServerSocket() {
        try {
            if (this.mServerSocket != null) {
                FileDescriptor fd = this.mServerSocket.getFileDescriptor();
                this.mServerSocket.close();
                if (fd != null) {
                    Os.close(fd);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "Zygote:  error closing sockets", ex);
        } catch (ErrnoException ex2) {
            Log.e(TAG, "Zygote:  error closing descriptor", ex2);
        }
        this.mServerSocket = null;
    }

    FileDescriptor getServerSocketFileDescriptor() {
        return this.mServerSocket.getFileDescriptor();
    }

    void runSelectLoop(String abiList) throws MethodAndArgsCaller {
        ArrayList<FileDescriptor> fds = new ArrayList();
        ArrayList<ZygoteConnection> peers = new ArrayList();
        fds.add(this.mServerSocket.getFileDescriptor());
        peers.add(null);
        while (true) {
            int i;
            StructPollfd[] pollFds = new StructPollfd[fds.size()];
            for (i = 0; i < pollFds.length; i++) {
                pollFds[i] = new StructPollfd();
                pollFds[i].fd = (FileDescriptor) fds.get(i);
                pollFds[i].events = (short) OsConstants.POLLIN;
            }
            try {
                Os.poll(pollFds, -1);
                for (i = pollFds.length - 1; i >= 0; i--) {
                    if ((pollFds[i].revents & OsConstants.POLLIN) != 0) {
                        if (i == 0) {
                            ZygoteConnection newPeer = acceptCommandPeer(abiList);
                            peers.add(newPeer);
                            fds.add(newPeer.getFileDesciptor());
                        } else if (((ZygoteConnection) peers.get(i)).runOnce(this)) {
                            peers.remove(i);
                            fds.remove(i);
                        }
                    }
                }
            } catch (ErrnoException ex) {
                throw new RuntimeException("poll failed", ex);
            }
        }
    }
}

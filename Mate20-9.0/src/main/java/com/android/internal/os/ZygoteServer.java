package com.android.internal.os;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;
import android.util.Log;
import android.util.Slog;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;

class ZygoteServer {
    private static final String ANDROID_SOCKET_PREFIX = "ANDROID_SOCKET_";
    public static final String TAG = "ZygoteServer";
    private boolean mCloseSocketFd;
    private boolean mIsForkChild;
    private LocalServerSocket mServerSocket;

    ZygoteServer() {
    }

    /* access modifiers changed from: package-private */
    public void setForkChild() {
        this.mIsForkChild = true;
    }

    /* access modifiers changed from: package-private */
    public void registerServerSocketFromEnv(String socketName) {
        if (this.mServerSocket == null) {
            try {
                int fileDesc = Integer.parseInt(System.getenv(ANDROID_SOCKET_PREFIX + socketName));
                try {
                    FileDescriptor fd = new FileDescriptor();
                    fd.setInt$(fileDesc);
                    this.mServerSocket = new LocalServerSocket(fd);
                    this.mCloseSocketFd = true;
                } catch (IOException ex) {
                    throw new RuntimeException("Error binding to local socket '" + fileDesc + "'", ex);
                }
            } catch (RuntimeException ex2) {
                throw new RuntimeException(fullSocketName + " unset or invalid", ex2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void registerServerSocketAtAbstractName(String socketName) {
        if (this.mServerSocket == null) {
            try {
                this.mServerSocket = new LocalServerSocket(socketName);
                this.mCloseSocketFd = false;
            } catch (IOException ex) {
                throw new RuntimeException("Error binding to abstract socket '" + socketName + "'", ex);
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

    /* access modifiers changed from: protected */
    public ZygoteConnection createNewConnection(LocalSocket socket, String abiList) throws IOException {
        return new ZygoteConnection(socket, abiList);
    }

    /* access modifiers changed from: package-private */
    public void closeServerSocket() {
        try {
            if (this.mServerSocket != null) {
                FileDescriptor fd = this.mServerSocket.getFileDescriptor();
                this.mServerSocket.close();
                if (fd != null && this.mCloseSocketFd) {
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

    /* access modifiers changed from: package-private */
    public FileDescriptor getServerSocketFileDescriptor() {
        return this.mServerSocket.getFileDescriptor();
    }

    /* access modifiers changed from: package-private */
    public Runnable runSelectLoop(String abiList) {
        ArrayList<FileDescriptor> fds = new ArrayList<>();
        ArrayList<ZygoteConnection> peers = new ArrayList<>();
        fds.add(this.mServerSocket.getFileDescriptor());
        peers.add(null);
        while (true) {
            StructPollfd[] pollFds = new StructPollfd[fds.size()];
            for (int i = 0; i < pollFds.length; i++) {
                pollFds[i] = new StructPollfd();
                pollFds[i].fd = fds.get(i);
                pollFds[i].events = (short) OsConstants.POLLIN;
            }
            try {
                Os.poll(pollFds, -1);
                int i2 = pollFds.length - 1;
                while (true) {
                    if (i2 >= 0) {
                        if ((pollFds[i2].revents & OsConstants.POLLIN) != 0) {
                            if (i2 == 0) {
                                ZygoteConnection newPeer = acceptCommandPeer(abiList);
                                peers.add(newPeer);
                                fds.add(newPeer.getFileDesciptor());
                            } else {
                                try {
                                    ZygoteConnection connection = peers.get(i2);
                                    Runnable command = connection.processOneCommand(this);
                                    if (this.mIsForkChild) {
                                        if (command != null) {
                                            this.mIsForkChild = false;
                                            return command;
                                        }
                                        throw new IllegalStateException("command == null");
                                    } else if (command == null) {
                                        if (connection.isClosedByPeer()) {
                                            connection.closeSocket();
                                            peers.remove(i2);
                                            fds.remove(i2);
                                        }
                                        this.mIsForkChild = false;
                                    } else {
                                        throw new IllegalStateException("command != null");
                                    }
                                } catch (Exception e) {
                                    if (!this.mIsForkChild) {
                                        Slog.e(TAG, "Exception executing zygote command: ", e);
                                        peers.remove(i2).closeSocket();
                                        fds.remove(i2);
                                    } else {
                                        Log.e(TAG, "Caught post-fork exception in child process.", e);
                                        throw e;
                                    }
                                } catch (Throwable th) {
                                    this.mIsForkChild = false;
                                    throw th;
                                }
                            }
                        }
                        i2--;
                    }
                }
            } catch (ErrnoException ex) {
                throw new RuntimeException("poll failed", ex);
            }
        }
    }
}

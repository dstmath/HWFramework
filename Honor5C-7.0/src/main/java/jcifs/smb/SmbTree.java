package jcifs.smb;

import jcifs.util.Encdec;
import jcifs.util.LogStream;

class SmbTree {
    private static int tree_conn_counter;
    int connectionState;
    boolean inDfs;
    boolean inDomainDfs;
    String service;
    String service0;
    SmbSession session;
    String share;
    int tid;
    int tree_num;

    SmbTree(SmbSession session, String share, String service) {
        this.service = "?????";
        this.session = session;
        this.share = share.toUpperCase();
        if (!(service == null || service.startsWith("??"))) {
            this.service = service;
        }
        this.service0 = this.service;
        this.connectionState = 0;
    }

    boolean matches(String share, String service) {
        return this.share.equalsIgnoreCase(share) && (service == null || service.startsWith("??") || this.service.equalsIgnoreCase(service));
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SmbTree)) {
            return false;
        }
        SmbTree tree = (SmbTree) obj;
        return matches(tree.share, tree.service);
    }

    void send(ServerMessageBlock request, ServerMessageBlock response) throws SmbException {
        synchronized (this.session.transport()) {
            if (response != null) {
                response.received = false;
            }
            treeConnect(request, response);
            if (request == null || (response != null && response.received)) {
                return;
            }
            if (!this.service.equals("A:")) {
                switch (request.command) {
                    case (byte) 37:
                    case (byte) 50:
                        switch (((SmbComTransaction) request).subCommand & 255) {
                            case SmbFile.FILE_NO_SHARE /*0*/:
                            case SmbFile.TYPE_NAMED_PIPE /*16*/:
                            case 35:
                            case 38:
                            case 83:
                            case 84:
                            case 104:
                            case 215:
                                break;
                            default:
                                throw new SmbException("Invalid operation for " + this.service + " service");
                        }
                    case (byte) -94:
                    case Encdec.TIME_1904_SEC_32LE /*4*/:
                    case (byte) 45:
                    case (byte) 46:
                    case (byte) 47:
                    case (byte) 113:
                        request.tid = this.tid;
                        if (this.inDfs && !this.service.equals("IPC") && request.path != null && request.path.length() > 0) {
                            request.flags2 = SmbConstants.FLAGS2_RESOLVE_PATHS_IN_DFS;
                            request.path = '\\' + this.session.transport().tconHostName + '\\' + this.share + request.path;
                        }
                        this.session.send(request, response);
                    default:
                        throw new SmbException("Invalid operation for " + this.service + " service" + request);
                }
            }
            request.tid = this.tid;
            request.flags2 = SmbConstants.FLAGS2_RESOLVE_PATHS_IN_DFS;
            request.path = '\\' + this.session.transport().tconHostName + '\\' + this.share + request.path;
            try {
                this.session.send(request, response);
            } catch (SmbException se) {
                if (se.getNtStatus() == NtStatus.NT_STATUS_NETWORK_NAME_DELETED) {
                    treeDisconnect(true);
                }
                throw se;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void treeConnect(ServerMessageBlock andx, ServerMessageBlock andxResponse) throws SmbException {
        synchronized (this.session.transport()) {
            while (true) {
                if (this.connectionState == 0) {
                    break;
                } else if (this.connectionState == 2 || this.connectionState == 3) {
                } else {
                    try {
                        this.session.transport.wait();
                    } catch (SmbException se) {
                        treeDisconnect(true);
                        this.connectionState = 0;
                        throw se;
                    } catch (Throwable ie) {
                        throw new SmbException(ie.getMessage(), ie);
                    }
                }
            }
        }
    }

    void treeDisconnect(boolean inError) {
        synchronized (this.session.transport()) {
            if (this.connectionState != 2) {
                return;
            }
            this.connectionState = 3;
            if (!(inError || this.tid == 0)) {
                try {
                    send(new SmbComTreeDisconnect(), null);
                } catch (SmbException se) {
                    r1 = this.session.transport;
                    LogStream logStream = SmbTransport.log;
                    if (LogStream.level > 1) {
                        SmbTransport smbTransport;
                        smbTransport = this.session.transport;
                        se.printStackTrace(SmbTransport.log);
                    }
                }
            }
            this.inDfs = false;
            this.inDomainDfs = false;
            this.connectionState = 0;
            this.session.transport.notifyAll();
        }
    }

    public String toString() {
        return "SmbTree[share=" + this.share + ",service=" + this.service + ",tid=" + this.tid + ",inDfs=" + this.inDfs + ",inDomainDfs=" + this.inDomainDfs + ",connectionState=" + this.connectionState + "]";
    }
}

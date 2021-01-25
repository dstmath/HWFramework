package jcifs.smb;

import jcifs.util.LogStream;

/* access modifiers changed from: package-private */
public class SmbTree {
    private static int tree_conn_counter;
    int connectionState;
    boolean inDfs;
    boolean inDomainDfs;
    String service = "?????";
    String service0;
    SmbSession session;
    String share;
    int tid;
    int tree_num;

    SmbTree(SmbSession session2, String share2, String service2) {
        this.session = session2;
        this.share = share2.toUpperCase();
        if (service2 != null && !service2.startsWith("??")) {
            this.service = service2;
        }
        this.service0 = this.service;
        this.connectionState = 0;
    }

    /* access modifiers changed from: package-private */
    public boolean matches(String share2, String service2) {
        return this.share.equalsIgnoreCase(share2) && (service2 == null || service2.startsWith("??") || this.service.equalsIgnoreCase(service2));
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SmbTree)) {
            return false;
        }
        SmbTree tree = (SmbTree) obj;
        return matches(tree.share, tree.service);
    }

    /* access modifiers changed from: package-private */
    public void send(ServerMessageBlock request, ServerMessageBlock response) throws SmbException {
        synchronized (this.session.transport()) {
            if (response != null) {
                response.received = false;
            }
            treeConnect(request, response);
            if (request != null && (response == null || !response.received)) {
                if (!this.service.equals("A:")) {
                    switch (request.command) {
                        case -94:
                        case 4:
                        case 45:
                        case 46:
                        case 47:
                        case 113:
                            break;
                        default:
                            throw new SmbException("Invalid operation for " + this.service + " service" + request);
                        case 37:
                        case 50:
                            switch (((SmbComTransaction) request).subCommand & 255) {
                                case 0:
                                case 16:
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
                    }
                }
                request.tid = this.tid;
                if (this.inDfs && !this.service.equals("IPC") && request.path != null && request.path.length() > 0) {
                    request.flags2 = 4096;
                    request.path = '\\' + this.session.transport().tconHostName + '\\' + this.share + request.path;
                }
                try {
                    this.session.send(request, response);
                } catch (SmbException se) {
                    if (se.getNtStatus() == -1073741623) {
                        treeDisconnect(true);
                    }
                    throw se;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void treeConnect(ServerMessageBlock andx, ServerMessageBlock andxResponse) throws SmbException {
        synchronized (this.session.transport()) {
            while (this.connectionState != 0) {
                if (this.connectionState != 2 && this.connectionState != 3) {
                    try {
                        this.session.transport.wait();
                    } catch (InterruptedException ie) {
                        throw new SmbException(ie.getMessage(), ie);
                    }
                } else {
                    return;
                }
            }
            this.connectionState = 1;
            try {
                this.session.transport.connect();
                String unc = "\\\\" + this.session.transport.tconHostName + '\\' + this.share;
                this.service = this.service0;
                SmbTransport smbTransport = this.session.transport;
                LogStream logStream = SmbTransport.log;
                if (LogStream.level >= 4) {
                    SmbTransport smbTransport2 = this.session.transport;
                    SmbTransport.log.println("treeConnect: unc=" + unc + ",service=" + this.service);
                }
                SmbComTreeConnectAndXResponse response = new SmbComTreeConnectAndXResponse(andxResponse);
                this.session.send(new SmbComTreeConnectAndX(this.session, unc, this.service, andx), response);
                this.tid = response.tid;
                this.service = response.service;
                this.inDfs = response.shareIsInDfs;
                int i = tree_conn_counter;
                tree_conn_counter = i + 1;
                this.tree_num = i;
                this.connectionState = 2;
            } catch (SmbException se) {
                treeDisconnect(true);
                this.connectionState = 0;
                throw se;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void treeDisconnect(boolean inError) {
        synchronized (this.session.transport()) {
            if (this.connectionState == 2) {
                this.connectionState = 3;
                if (!inError && this.tid != 0) {
                    try {
                        send(new SmbComTreeDisconnect(), null);
                    } catch (SmbException se) {
                        SmbTransport smbTransport = this.session.transport;
                        LogStream logStream = SmbTransport.log;
                        if (LogStream.level > 1) {
                            SmbTransport smbTransport2 = this.session.transport;
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
    }

    public String toString() {
        return "SmbTree[share=" + this.share + ",service=" + this.service + ",tid=" + this.tid + ",inDfs=" + this.inDfs + ",inDomainDfs=" + this.inDomainDfs + ",connectionState=" + this.connectionState + "]";
    }
}

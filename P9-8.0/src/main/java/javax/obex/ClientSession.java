package javax.obex;

import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class ClientSession extends ObexSession {
    private static final String TAG = "ClientSession";
    private byte[] mConnectionId = null;
    private final InputStream mInput;
    private final boolean mLocalSrmSupported;
    private int mMaxTxPacketSize = 255;
    private boolean mObexConnected;
    private boolean mOpen;
    private final OutputStream mOutput;
    private boolean mRequestActive;
    private final ObexTransport mTransport;

    public ClientSession(ObexTransport trans) throws IOException {
        this.mInput = trans.openInputStream();
        this.mOutput = trans.openOutputStream();
        this.mOpen = true;
        this.mRequestActive = false;
        this.mLocalSrmSupported = trans.isSrmSupported();
        this.mTransport = trans;
    }

    public ClientSession(ObexTransport trans, boolean supportsSrm) throws IOException {
        this.mInput = trans.openInputStream();
        this.mOutput = trans.openOutputStream();
        this.mOpen = true;
        this.mRequestActive = false;
        this.mLocalSrmSupported = supportsSrm;
        this.mTransport = trans;
    }

    public HeaderSet connect(HeaderSet header) throws IOException {
        ensureOpen();
        if (this.mObexConnected) {
            throw new IOException("Already connected to server");
        }
        setRequestActive();
        int totalLength = 4;
        byte[] head = null;
        if (header != null) {
            if (header.nonce != null) {
                this.mChallengeDigest = new byte[16];
                System.arraycopy(header.nonce, 0, this.mChallengeDigest, 0, 16);
            }
            head = ObexHelper.createHeader(header, false);
            totalLength = head.length + 4;
        }
        byte[] requestPacket = new byte[totalLength];
        int maxRxPacketSize = ObexHelper.getMaxRxPacketSize(this.mTransport);
        requestPacket[0] = (byte) 16;
        requestPacket[1] = (byte) 0;
        requestPacket[2] = (byte) (maxRxPacketSize >> 8);
        requestPacket[3] = (byte) (maxRxPacketSize & 255);
        if (head != null) {
            System.arraycopy(head, 0, requestPacket, 4, head.length);
        }
        if (requestPacket.length + 3 > ObexHelper.MAX_PACKET_SIZE_INT) {
            throw new IOException("Packet size exceeds max packet size for connect");
        }
        HeaderSet returnHeaderSet = new HeaderSet();
        sendRequest(128, requestPacket, returnHeaderSet, null, false);
        if (returnHeaderSet.responseCode == ResponseCodes.OBEX_HTTP_OK) {
            this.mObexConnected = true;
        }
        setRequestInactive();
        return returnHeaderSet;
    }

    public Operation get(HeaderSet header) throws IOException {
        if (this.mObexConnected) {
            HeaderSet head;
            setRequestActive();
            ensureOpen();
            if (header == null) {
                head = new HeaderSet();
            } else {
                head = header;
                if (header.nonce != null) {
                    this.mChallengeDigest = new byte[16];
                    System.arraycopy(header.nonce, 0, this.mChallengeDigest, 0, 16);
                }
            }
            if (this.mConnectionId != null) {
                head.mConnectionID = new byte[4];
                System.arraycopy(this.mConnectionId, 0, head.mConnectionID, 0, 4);
            }
            if (this.mLocalSrmSupported) {
                head.setHeader(HeaderSet.SINGLE_RESPONSE_MODE, Byte.valueOf((byte) 1));
            }
            return new ClientOperation(this.mMaxTxPacketSize, this, head, true);
        }
        throw new IOException("Not connected to the server");
    }

    public void setConnectionID(long id) {
        if (id < 0 || id > 4294967295L) {
            throw new IllegalArgumentException("Connection ID is not in a valid range");
        }
        this.mConnectionId = ObexHelper.convertToByteArray(id);
    }

    public HeaderSet delete(HeaderSet header) throws IOException {
        Operation op = put(header);
        op.getResponseCode();
        HeaderSet returnValue = op.getReceivedHeader();
        op.close();
        return returnValue;
    }

    public HeaderSet disconnect(HeaderSet header) throws IOException {
        if (this.mObexConnected) {
            setRequestActive();
            ensureOpen();
            byte[] head = null;
            if (header != null) {
                if (header.nonce != null) {
                    this.mChallengeDigest = new byte[16];
                    System.arraycopy(header.nonce, 0, this.mChallengeDigest, 0, 16);
                }
                if (this.mConnectionId != null) {
                    header.mConnectionID = new byte[4];
                    System.arraycopy(this.mConnectionId, 0, header.mConnectionID, 0, 4);
                }
                head = ObexHelper.createHeader(header, false);
                if (head.length + 3 > this.mMaxTxPacketSize) {
                    throw new IOException("Packet size exceeds max packet size");
                }
            } else if (this.mConnectionId != null) {
                head = new byte[5];
                head[0] = (byte) -53;
                System.arraycopy(this.mConnectionId, 0, head, 1, 4);
            }
            HeaderSet returnHeaderSet = new HeaderSet();
            sendRequest(ObexHelper.OBEX_OPCODE_DISCONNECT, head, returnHeaderSet, null, false);
            synchronized (this) {
                this.mObexConnected = false;
                setRequestInactive();
            }
            return returnHeaderSet;
        }
        throw new IOException("Not connected to the server");
    }

    public long getConnectionID() {
        if (this.mConnectionId == null) {
            return -1;
        }
        return ObexHelper.convertToLong(this.mConnectionId);
    }

    public Operation put(HeaderSet header) throws IOException {
        if (this.mObexConnected) {
            HeaderSet head;
            setRequestActive();
            ensureOpen();
            if (header == null) {
                head = new HeaderSet();
            } else {
                head = header;
                if (header.nonce != null) {
                    this.mChallengeDigest = new byte[16];
                    System.arraycopy(header.nonce, 0, this.mChallengeDigest, 0, 16);
                }
            }
            if (this.mConnectionId != null) {
                head.mConnectionID = new byte[4];
                System.arraycopy(this.mConnectionId, 0, head.mConnectionID, 0, 4);
            }
            if (this.mLocalSrmSupported) {
                head.setHeader(HeaderSet.SINGLE_RESPONSE_MODE, Byte.valueOf((byte) 1));
            }
            return new ClientOperation(this.mMaxTxPacketSize, this, head, false);
        }
        throw new IOException("Not connected to the server");
    }

    public void setAuthenticator(Authenticator auth) throws IOException {
        if (auth == null) {
            throw new IOException("Authenticator may not be null");
        }
        this.mAuthenticator = auth;
    }

    public HeaderSet setPath(HeaderSet header, boolean backup, boolean create) throws IOException {
        if (this.mObexConnected) {
            HeaderSet headset;
            setRequestActive();
            ensureOpen();
            if (header == null) {
                headset = new HeaderSet();
            } else {
                headset = header;
                if (header.nonce != null) {
                    this.mChallengeDigest = new byte[16];
                    System.arraycopy(header.nonce, 0, this.mChallengeDigest, 0, 16);
                }
            }
            if (headset.nonce != null) {
                this.mChallengeDigest = new byte[16];
                System.arraycopy(headset.nonce, 0, this.mChallengeDigest, 0, 16);
            }
            if (this.mConnectionId != null) {
                headset.mConnectionID = new byte[4];
                System.arraycopy(this.mConnectionId, 0, headset.mConnectionID, 0, 4);
            }
            byte[] head = ObexHelper.createHeader(headset, false);
            int totalLength = head.length + 2;
            if (totalLength > this.mMaxTxPacketSize) {
                throw new IOException("Packet size exceeds max packet size");
            }
            int flags = 0;
            if (backup) {
                flags = 1;
            }
            if (!create) {
                flags |= 2;
            }
            byte[] packet = new byte[totalLength];
            packet[0] = (byte) flags;
            packet[1] = (byte) 0;
            if (headset != null) {
                System.arraycopy(head, 0, packet, 2, head.length);
            }
            HeaderSet returnHeaderSet = new HeaderSet();
            sendRequest(ObexHelper.OBEX_OPCODE_SETPATH, packet, returnHeaderSet, null, false);
            setRequestInactive();
            return returnHeaderSet;
        }
        throw new IOException("Not connected to the server");
    }

    public synchronized void ensureOpen() throws IOException {
        if (!this.mOpen) {
            throw new IOException("Connection closed");
        }
    }

    synchronized void setRequestInactive() {
        this.mRequestActive = false;
    }

    private synchronized void setRequestActive() throws IOException {
        if (this.mRequestActive) {
            throw new IOException("OBEX request is already being performed");
        }
        this.mRequestActive = true;
    }

    public boolean sendRequest(int opCode, byte[] head, HeaderSet header, PrivateInputStream privateInput, boolean srmActive) throws IOException {
        if (head == null || head.length + 3 <= ObexHelper.MAX_PACKET_SIZE_INT) {
            boolean skipSend = false;
            boolean skipReceive = false;
            if (srmActive) {
                if (opCode == 2) {
                    skipReceive = true;
                } else if (opCode == 3) {
                    skipReceive = true;
                } else if (opCode == 131) {
                    skipSend = true;
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write((byte) opCode);
            if (head == null) {
                out.write(0);
                out.write(3);
            } else {
                out.write((byte) ((head.length + 3) >> 8));
                out.write((byte) (head.length + 3));
                out.write(head);
            }
            if (!skipSend) {
                this.mOutput.write(out.toByteArray());
                this.mOutput.flush();
            }
            if (!skipReceive) {
                header.responseCode = this.mInput.read();
                int length = (this.mInput.read() << 8) | this.mInput.read();
                if (length > ObexHelper.getMaxRxPacketSize(this.mTransport)) {
                    throw new IOException("Packet received exceeds packet size limit");
                } else if (length > 3) {
                    byte[] data;
                    int bytesReceived;
                    if (opCode == 128) {
                        int version = this.mInput.read();
                        int flags = this.mInput.read();
                        this.mMaxTxPacketSize = (this.mInput.read() << 8) + this.mInput.read();
                        if (this.mMaxTxPacketSize > ObexHelper.MAX_CLIENT_PACKET_SIZE) {
                            this.mMaxTxPacketSize = ObexHelper.MAX_CLIENT_PACKET_SIZE;
                        }
                        if (this.mMaxTxPacketSize > ObexHelper.getMaxTxPacketSize(this.mTransport)) {
                            Log.w(TAG, "An OBEX packet size of " + this.mMaxTxPacketSize + "was" + " requested. Transport only allows: " + ObexHelper.getMaxTxPacketSize(this.mTransport) + " Lowering limit to this value.");
                            this.mMaxTxPacketSize = ObexHelper.getMaxTxPacketSize(this.mTransport);
                        }
                        if (length <= 7) {
                            return true;
                        }
                        data = new byte[(length - 7)];
                        bytesReceived = this.mInput.read(data);
                        while (bytesReceived != length - 7) {
                            bytesReceived += this.mInput.read(data, bytesReceived, data.length - bytesReceived);
                        }
                    } else {
                        data = new byte[(length - 3)];
                        bytesReceived = this.mInput.read(data);
                        while (bytesReceived != length - 3) {
                            bytesReceived += this.mInput.read(data, bytesReceived, data.length - bytesReceived);
                        }
                        if (opCode == 255) {
                            return true;
                        }
                    }
                    byte[] body = ObexHelper.updateHeaderSet(header, data);
                    if (!(privateInput == null || body == null)) {
                        privateInput.writeBytes(body, 1);
                    }
                    if (header.mConnectionID != null) {
                        this.mConnectionId = new byte[4];
                        System.arraycopy(header.mConnectionID, 0, this.mConnectionId, 0, 4);
                    }
                    if (header.mAuthResp != null) {
                        if (!handleAuthResp(header.mAuthResp)) {
                            setRequestInactive();
                            throw new IOException("Authentication Failed");
                        }
                    }
                    if (header.responseCode == ResponseCodes.OBEX_HTTP_UNAUTHORIZED && header.mAuthChall != null && handleAuthChall(header)) {
                        out.write(78);
                        out.write((byte) ((header.mAuthResp.length + 3) >> 8));
                        out.write((byte) (header.mAuthResp.length + 3));
                        out.write(header.mAuthResp);
                        header.mAuthChall = null;
                        header.mAuthResp = null;
                        byte[] sendHeaders = new byte[(out.size() - 3)];
                        System.arraycopy(out.toByteArray(), 3, sendHeaders, 0, sendHeaders.length);
                        return sendRequest(opCode, sendHeaders, header, privateInput, false);
                    }
                }
            }
            return true;
        }
        throw new IOException("header too large ");
    }

    public void close() throws IOException {
        this.mOpen = false;
        this.mInput.close();
        this.mOutput.close();
    }

    public boolean isSrmSupported() {
        return this.mLocalSrmSupported;
    }
}

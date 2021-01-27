package javax.obex;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class ServerSession extends ObexSession implements Runnable {
    private static final String TAG = "Obex ServerSession";
    private static final boolean V = false;
    private boolean mClosed;
    private InputStream mInput = this.mTransport.openInputStream();
    private ServerRequestHandler mListener;
    private int mMaxPacketLength;
    private OutputStream mOutput = this.mTransport.openOutputStream();
    private Thread mProcessThread;
    private ObexTransport mTransport;

    public ServerSession(ObexTransport trans, ServerRequestHandler handler, Authenticator auth) throws IOException {
        this.mAuthenticator = auth;
        this.mTransport = trans;
        this.mListener = handler;
        this.mMaxPacketLength = 256;
        this.mClosed = false;
        this.mProcessThread = new Thread(this);
        this.mProcessThread.start();
    }

    @Override // java.lang.Runnable
    public void run() {
        boolean done = false;
        while (!done) {
            try {
                if (!this.mClosed) {
                    int requestType = this.mInput.read();
                    if (requestType == -1) {
                        done = true;
                    } else if (requestType == 133) {
                        handleSetPathRequest();
                    } else if (requestType != 255) {
                        if (requestType != 2) {
                            if (requestType != 3) {
                                switch (requestType) {
                                    case 128:
                                        handleConnectRequest();
                                        break;
                                    case ObexHelper.OBEX_OPCODE_DISCONNECT /* 129 */:
                                        handleDisconnectRequest();
                                        break;
                                    case ObexHelper.OBEX_OPCODE_PUT_FINAL /* 130 */:
                                        break;
                                    case ObexHelper.OBEX_OPCODE_GET_FINAL /* 131 */:
                                        break;
                                    default:
                                        int length = (this.mInput.read() << 8) + this.mInput.read();
                                        for (int i = 3; i < length; i++) {
                                            this.mInput.read();
                                        }
                                        sendResponse(ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED, null);
                                        break;
                                }
                            }
                            handleGetRequest(requestType);
                        }
                        handlePutRequest(requestType);
                    } else {
                        handleAbortRequest();
                    }
                } else {
                    close();
                }
            } catch (NullPointerException e) {
                Log.d(TAG, "Exception occured - ignoring", e);
            } catch (Exception e2) {
                Log.d(TAG, "Exception occured - ignoring", e2);
            }
        }
        close();
    }

    private void handleAbortRequest() throws IOException {
        int code;
        HeaderSet request = new HeaderSet();
        HeaderSet reply = new HeaderSet();
        int length = (this.mInput.read() << 8) + this.mInput.read();
        if (length > ObexHelper.getMaxRxPacketSize(this.mTransport)) {
            code = ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE;
        } else {
            for (int i = 3; i < length; i++) {
                this.mInput.read();
            }
            int code2 = this.mListener.onAbort(request, reply);
            Log.v(TAG, "onAbort request handler return value- " + code2);
            code = validateResponseCode(code2);
        }
        sendResponse(code, null);
    }

    private void handlePutRequest(int type) throws IOException {
        int response;
        ServerOperation op = new ServerOperation(this, this.mInput, type, this.mMaxPacketLength, this.mListener);
        try {
            if (!op.finalBitSet || op.isValidBody()) {
                response = validateResponseCode(this.mListener.onPut(op));
            } else {
                response = validateResponseCode(this.mListener.onDelete(op.requestHeader, op.replyHeader));
            }
            if (response != 160 && !op.isAborted) {
                op.sendReply(response);
            } else if (!op.isAborted) {
                while (!op.finalBitSet) {
                    op.sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
                }
                op.sendReply(response);
            }
        } catch (Exception e) {
            if (!op.isAborted) {
                sendResponse(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR, null);
            }
        }
    }

    private void handleGetRequest(int type) throws IOException {
        ServerOperation op = new ServerOperation(this, this.mInput, type, this.mMaxPacketLength, this.mListener);
        try {
            int response = validateResponseCode(this.mListener.onGet(op));
            if (!op.isAborted) {
                op.sendReply(response);
            }
        } catch (Exception e) {
            sendResponse(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR, null);
        }
    }

    public void sendResponse(int code, byte[] header) throws IOException {
        byte[] data;
        OutputStream op = this.mOutput;
        if (op != null) {
            if (header != null) {
                int totalLength = 3 + header.length;
                data = new byte[totalLength];
                data[0] = (byte) code;
                data[1] = (byte) (totalLength >> 8);
                data[2] = (byte) totalLength;
                System.arraycopy(header, 0, data, 3, header.length);
            } else {
                data = new byte[]{(byte) code, 0, (byte) 3};
            }
            op.write(data);
            op.flush();
        }
    }

    /* JADX INFO: Multiple debug info for r9v2 byte[]: [D('code' int), D('replyData' byte[])] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0117  */
    private void handleSetPathRequest() throws IOException {
        int code;
        int code2;
        boolean backup;
        byte[] bArr;
        int totalLength = 3;
        byte[] head = null;
        int code3 = -1;
        HeaderSet request = new HeaderSet();
        HeaderSet reply = new HeaderSet();
        int length = (this.mInput.read() << 8) + this.mInput.read();
        int flags = this.mInput.read();
        this.mInput.read();
        if (length > ObexHelper.getMaxRxPacketSize(this.mTransport)) {
            code = ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE;
            totalLength = 3;
        } else {
            if (length > 5) {
                byte[] headers = new byte[(length - 5)];
                int bytesReceived = this.mInput.read(headers);
                while (bytesReceived != headers.length) {
                    bytesReceived += this.mInput.read(headers, bytesReceived, headers.length - bytesReceived);
                }
                ObexHelper.updateHeaderSet(request, headers);
                if (this.mListener.getConnectionId() == -1 || request.mConnectionID == null) {
                    this.mListener.setConnectionId(1);
                } else {
                    this.mListener.setConnectionId(ObexHelper.convertToLong(request.mConnectionID));
                }
                if (request.mAuthResp != null) {
                    if (!handleAuthResp(request.mAuthResp)) {
                        code3 = ResponseCodes.OBEX_HTTP_UNAUTHORIZED;
                        this.mListener.onAuthenticationFailure(ObexHelper.getTagValue((byte) 1, request.mAuthResp));
                    }
                    request.mAuthResp = null;
                    code2 = code3;
                    if (code2 == 193) {
                        if (request.mAuthChall != null) {
                            handleAuthChall(request);
                            reply.mAuthResp = new byte[request.mAuthResp.length];
                            System.arraycopy(request.mAuthResp, 0, reply.mAuthResp, 0, reply.mAuthResp.length);
                            request.mAuthChall = null;
                            request.mAuthResp = null;
                        }
                        boolean create = true;
                        if ((flags & 1) != 0) {
                            backup = true;
                        } else {
                            backup = false;
                        }
                        if ((flags & 2) != 0) {
                            create = false;
                        }
                        try {
                            code = validateResponseCode(this.mListener.onSetPath(request, reply, backup, create));
                            if (reply.nonce != null) {
                                this.mChallengeDigest = new byte[16];
                                System.arraycopy(reply.nonce, 0, this.mChallengeDigest, 0, 16);
                                bArr = null;
                            } else {
                                bArr = null;
                                this.mChallengeDigest = null;
                            }
                            long id = this.mListener.getConnectionId();
                            if (id == -1) {
                                reply.mConnectionID = bArr;
                            } else {
                                reply.mConnectionID = ObexHelper.convertToByteArray(id);
                            }
                            head = ObexHelper.createHeader(reply, false);
                            totalLength = 3 + head.length;
                            if (totalLength > this.mMaxPacketLength) {
                                totalLength = 3;
                                head = null;
                                code = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
                            }
                        } catch (Exception e) {
                            sendResponse(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR, null);
                            return;
                        }
                    } else {
                        code = code2;
                    }
                }
            }
            code2 = -1;
            if (code2 == 193) {
            }
        }
        byte[] replyData = new byte[totalLength];
        replyData[0] = (byte) code;
        replyData[1] = (byte) (totalLength >> 8);
        replyData[2] = (byte) totalLength;
        if (head != null) {
            System.arraycopy(head, 0, replyData, 3, head.length);
        }
        this.mOutput.write(replyData);
        this.mOutput.flush();
    }

    private void handleDisconnectRequest() throws IOException {
        int code;
        byte[] replyData;
        int code2;
        int code3 = ResponseCodes.OBEX_HTTP_OK;
        int totalLength = 3;
        byte[] head = null;
        HeaderSet request = new HeaderSet();
        HeaderSet reply = new HeaderSet();
        int length = (this.mInput.read() << 8) + this.mInput.read();
        if (length > ObexHelper.getMaxRxPacketSize(this.mTransport)) {
            code = ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE;
            totalLength = 3;
        } else {
            if (length > 3) {
                byte[] headers = new byte[(length - 3)];
                int bytesReceived = this.mInput.read(headers);
                while (bytesReceived != headers.length) {
                    bytesReceived += this.mInput.read(headers, bytesReceived, headers.length - bytesReceived);
                }
                ObexHelper.updateHeaderSet(request, headers);
            }
            if (this.mListener.getConnectionId() == -1 || request.mConnectionID == null) {
                this.mListener.setConnectionId(1);
            } else {
                this.mListener.setConnectionId(ObexHelper.convertToLong(request.mConnectionID));
            }
            if (request.mAuthResp != null) {
                if (!handleAuthResp(request.mAuthResp)) {
                    code3 = ResponseCodes.OBEX_HTTP_UNAUTHORIZED;
                    this.mListener.onAuthenticationFailure(ObexHelper.getTagValue((byte) 1, request.mAuthResp));
                }
                request.mAuthResp = null;
                code2 = code3;
            } else {
                code2 = 160;
            }
            if (code2 != 193) {
                if (request.mAuthChall != null) {
                    handleAuthChall(request);
                    request.mAuthChall = null;
                }
                try {
                    this.mListener.onDisconnect(request, reply);
                    long id = this.mListener.getConnectionId();
                    if (id == -1) {
                        reply.mConnectionID = null;
                    } else {
                        reply.mConnectionID = ObexHelper.convertToByteArray(id);
                    }
                    head = ObexHelper.createHeader(reply, false);
                    totalLength = 3 + head.length;
                    if (totalLength > this.mMaxPacketLength) {
                        totalLength = 3;
                        head = null;
                        code = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
                    } else {
                        code = code2;
                    }
                } catch (Exception e) {
                    sendResponse(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR, null);
                    return;
                }
            } else {
                code = code2;
            }
        }
        if (head != null) {
            replyData = new byte[(head.length + 3)];
        } else {
            replyData = new byte[3];
        }
        replyData[0] = (byte) code;
        replyData[1] = (byte) (totalLength >> 8);
        replyData[2] = (byte) totalLength;
        if (head != null) {
            System.arraycopy(head, 0, replyData, 3, head.length);
        }
        this.mOutput.write(replyData);
        this.mOutput.flush();
    }

    private void handleConnectRequest() throws IOException {
        int code;
        int code2;
        int totalLength = 7;
        byte[] head = null;
        int code3 = -1;
        HeaderSet request = new HeaderSet();
        HeaderSet reply = new HeaderSet();
        int packetLength = (this.mInput.read() << 8) + this.mInput.read();
        this.mInput.read();
        this.mInput.read();
        this.mMaxPacketLength = this.mInput.read();
        this.mMaxPacketLength = (this.mMaxPacketLength << 8) + this.mInput.read();
        if (this.mMaxPacketLength > 65534) {
            this.mMaxPacketLength = ObexHelper.MAX_PACKET_SIZE_INT;
        }
        if (this.mMaxPacketLength > ObexHelper.getMaxTxPacketSize(this.mTransport)) {
            Log.w(TAG, "Requested MaxObexPacketSize " + this.mMaxPacketLength + " is larger than the max size supported by the transport: " + ObexHelper.getMaxTxPacketSize(this.mTransport) + " Reducing to this size.");
            this.mMaxPacketLength = ObexHelper.getMaxTxPacketSize(this.mTransport);
        }
        if (packetLength > ObexHelper.getMaxRxPacketSize(this.mTransport)) {
            code = ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE;
            totalLength = 7;
        } else {
            if (packetLength > 7) {
                byte[] headers = new byte[(packetLength - 7)];
                int bytesReceived = this.mInput.read(headers);
                while (bytesReceived != headers.length) {
                    bytesReceived += this.mInput.read(headers, bytesReceived, headers.length - bytesReceived);
                }
                ObexHelper.updateHeaderSet(request, headers);
            }
            if (this.mListener.getConnectionId() == -1 || request.mConnectionID == null) {
                this.mListener.setConnectionId(1);
            } else {
                this.mListener.setConnectionId(ObexHelper.convertToLong(request.mConnectionID));
            }
            if (request.mAuthResp != null) {
                if (!handleAuthResp(request.mAuthResp)) {
                    code3 = ResponseCodes.OBEX_HTTP_UNAUTHORIZED;
                    this.mListener.onAuthenticationFailure(ObexHelper.getTagValue((byte) 1, request.mAuthResp));
                }
                request.mAuthResp = null;
                code2 = code3;
            } else {
                code2 = -1;
            }
            if (code2 != 193) {
                if (request.mAuthChall != null) {
                    handleAuthChall(request);
                    reply.mAuthResp = new byte[request.mAuthResp.length];
                    System.arraycopy(request.mAuthResp, 0, reply.mAuthResp, 0, reply.mAuthResp.length);
                    request.mAuthChall = null;
                    request.mAuthResp = null;
                }
                try {
                    int code4 = validateResponseCode(this.mListener.onConnect(request, reply));
                    if (reply.nonce != null) {
                        this.mChallengeDigest = new byte[16];
                        System.arraycopy(reply.nonce, 0, this.mChallengeDigest, 0, 16);
                    } else {
                        this.mChallengeDigest = null;
                    }
                    long id = this.mListener.getConnectionId();
                    if (id == -1) {
                        reply.mConnectionID = null;
                    } else {
                        reply.mConnectionID = ObexHelper.convertToByteArray(id);
                    }
                    head = ObexHelper.createHeader(reply, false);
                    totalLength = 7 + head.length;
                    if (totalLength > this.mMaxPacketLength) {
                        totalLength = 7;
                        code = 208;
                        head = null;
                    } else {
                        code = code4;
                    }
                } catch (Exception e) {
                    totalLength = 7;
                    head = null;
                    code = 208;
                }
            } else {
                code = code2;
            }
        }
        byte[] length = ObexHelper.convertToByteArray((long) totalLength);
        byte[] sendData = new byte[totalLength];
        int maxRxLength = ObexHelper.getMaxRxPacketSize(this.mTransport);
        if (maxRxLength > this.mMaxPacketLength) {
            maxRxLength = this.mMaxPacketLength;
        }
        sendData[0] = (byte) code;
        sendData[1] = length[2];
        sendData[2] = length[3];
        sendData[3] = 16;
        sendData[4] = 0;
        sendData[5] = (byte) (maxRxLength >> 8);
        sendData[6] = (byte) (maxRxLength & 255);
        if (head != null) {
            System.arraycopy(head, 0, sendData, 7, head.length);
        }
        this.mOutput.write(sendData);
        this.mOutput.flush();
    }

    public synchronized void close() {
        if (this.mListener != null) {
            this.mListener.onClose();
        }
        try {
            this.mClosed = true;
            if (this.mInput != null) {
                this.mInput.close();
            }
            if (this.mOutput != null) {
                this.mOutput.close();
            }
            if (this.mTransport != null) {
                this.mTransport.close();
            }
        } catch (Exception e) {
        }
        this.mTransport = null;
        this.mInput = null;
        this.mOutput = null;
        this.mListener = null;
    }

    private int validateResponseCode(int code) {
        if (code >= 160 && code <= 166) {
            return code;
        }
        if (code >= 176 && code <= 181) {
            return code;
        }
        if (code >= 192 && code <= 207) {
            return code;
        }
        if (code >= 208 && code <= 213) {
            return code;
        }
        if (code < 224 || code > 225) {
            return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }
        return code;
    }

    public ObexTransport getTransport() {
        return this.mTransport;
    }
}

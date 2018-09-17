package javax.obex;

import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class ServerOperation implements Operation, BaseStream {
    private static final String TAG = "ServerOperation";
    private static final boolean V = false;
    public boolean finalBitSet;
    public boolean isAborted = false;
    private boolean mClosed;
    private String mExceptionString;
    private boolean mGetOperation;
    private boolean mHasBody;
    private InputStream mInput;
    private ServerRequestHandler mListener;
    private int mMaxPacketLength;
    private ServerSession mParent;
    private PrivateInputStream mPrivateInput;
    private PrivateOutputStream mPrivateOutput;
    private boolean mPrivateOutputOpen;
    private boolean mRequestFinished;
    private int mResponseSize;
    private boolean mSendBodyHeader = true;
    private boolean mSrmActive = false;
    private boolean mSrmEnabled = false;
    private boolean mSrmLocalWait = false;
    private boolean mSrmResponseSent = false;
    private boolean mSrmWaitingForRemote = true;
    private ObexTransport mTransport;
    public HeaderSet replyHeader;
    public HeaderSet requestHeader;

    public ServerOperation(ServerSession p, InputStream in, int request, int maxSize, ServerRequestHandler listen) throws IOException {
        this.mParent = p;
        this.mInput = in;
        this.mMaxPacketLength = maxSize;
        this.mClosed = false;
        this.requestHeader = new HeaderSet();
        this.replyHeader = new HeaderSet();
        this.mPrivateInput = new PrivateInputStream(this);
        this.mResponseSize = 3;
        this.mListener = listen;
        this.mRequestFinished = false;
        this.mPrivateOutputOpen = false;
        this.mHasBody = false;
        this.mTransport = p.getTransport();
        if (request == 2 || request == ObexHelper.OBEX_OPCODE_PUT_FINAL) {
            this.mGetOperation = false;
            if ((request & 128) == 0) {
                this.finalBitSet = false;
            } else {
                this.finalBitSet = true;
                this.mRequestFinished = true;
            }
        } else if (request == 3 || request == ObexHelper.OBEX_OPCODE_GET_FINAL) {
            this.mGetOperation = true;
            this.finalBitSet = false;
            if (request == ObexHelper.OBEX_OPCODE_GET_FINAL) {
                this.mRequestFinished = true;
            }
        } else {
            throw new IOException("ServerOperation can not handle such request");
        }
        ObexPacket packet = ObexPacket.read(request, this.mInput);
        if (packet.mLength > ObexHelper.getMaxRxPacketSize(this.mTransport)) {
            this.mParent.sendResponse(ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE, null);
            throw new IOException("Packet received was too large. Length: " + packet.mLength + " maxLength: " + ObexHelper.getMaxRxPacketSize(this.mTransport));
        }
        if (packet.mLength > 3) {
            if (!handleObexPacket(packet)) {
                return;
            }
            if (!this.mHasBody && (this.mSrmEnabled ^ 1) != 0) {
                while (!this.mGetOperation && (this.finalBitSet ^ 1) != 0) {
                    sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
                    if (this.mPrivateInput.available() > 0) {
                        break;
                    }
                }
            }
        }
        while (!this.mSrmEnabled && (this.mGetOperation ^ 1) != 0 && (this.finalBitSet ^ 1) != 0 && this.mPrivateInput.available() == 0) {
            sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
            if (this.mPrivateInput.available() > 0) {
                break;
            }
        }
        while (this.mGetOperation && (this.mRequestFinished ^ 1) != 0) {
            sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
        }
    }

    private boolean handleObexPacket(ObexPacket packet) throws IOException {
        byte[] body = updateRequestHeaders(packet);
        if (body != null) {
            this.mHasBody = true;
        }
        if (this.mListener.getConnectionId() == -1 || this.requestHeader.mConnectionID == null) {
            this.mListener.setConnectionId(1);
        } else {
            this.mListener.setConnectionId(ObexHelper.convertToLong(this.requestHeader.mConnectionID));
        }
        if (this.requestHeader.mAuthResp != null) {
            if (this.mParent.handleAuthResp(this.requestHeader.mAuthResp)) {
                this.requestHeader.mAuthResp = null;
            } else {
                this.mExceptionString = "Authentication Failed";
                this.mParent.sendResponse(ResponseCodes.OBEX_HTTP_UNAUTHORIZED, null);
                this.mClosed = true;
                this.requestHeader.mAuthResp = null;
                return false;
            }
        }
        if (this.requestHeader.mAuthChall != null) {
            this.mParent.handleAuthChall(this.requestHeader);
            this.replyHeader.mAuthResp = new byte[this.requestHeader.mAuthResp.length];
            System.arraycopy(this.requestHeader.mAuthResp, 0, this.replyHeader.mAuthResp, 0, this.replyHeader.mAuthResp.length);
            this.requestHeader.mAuthResp = null;
            this.requestHeader.mAuthChall = null;
        }
        if (body != null) {
            this.mPrivateInput.writeBytes(body, 1);
        }
        return true;
    }

    private byte[] updateRequestHeaders(ObexPacket packet) throws IOException {
        byte[] body = null;
        if (packet.mPayload != null) {
            body = ObexHelper.updateHeaderSet(this.requestHeader, packet.mPayload);
        }
        Byte srmMode = (Byte) this.requestHeader.getHeader(HeaderSet.SINGLE_RESPONSE_MODE);
        if (this.mTransport.isSrmSupported() && srmMode != null && srmMode.byteValue() == (byte) 1) {
            this.mSrmEnabled = true;
        }
        checkForSrmWait(packet.mHeaderId);
        if (!this.mSrmWaitingForRemote && this.mSrmEnabled) {
            this.mSrmActive = true;
        }
        return body;
    }

    private void checkForSrmWait(int headerId) {
        if (!this.mSrmEnabled) {
            return;
        }
        if (headerId == 3 || headerId == ObexHelper.OBEX_OPCODE_GET_FINAL || headerId == 2) {
            try {
                this.mSrmWaitingForRemote = false;
                Byte srmp = (Byte) this.requestHeader.getHeader(HeaderSet.SINGLE_RESPONSE_MODE_PARAMETER);
                if (srmp != null && srmp.byteValue() == (byte) 1) {
                    this.mSrmWaitingForRemote = true;
                    this.requestHeader.setHeader(HeaderSet.SINGLE_RESPONSE_MODE_PARAMETER, null);
                }
            } catch (IOException e) {
            }
        }
    }

    public boolean isValidBody() {
        return this.mHasBody;
    }

    public synchronized boolean continueOperation(boolean sendEmpty, boolean inStream) throws IOException {
        if (this.mGetOperation) {
            sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
            return true;
        } else if (this.finalBitSet) {
            return false;
        } else {
            if (sendEmpty) {
                sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
                return true;
            } else if (this.mResponseSize <= 3 && this.mPrivateOutput.size() <= 0) {
                return false;
            } else {
                sendReply(ResponseCodes.OBEX_HTTP_CONTINUE);
                return true;
            }
        }
    }

    public synchronized boolean sendReply(int type) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean skipSend = false;
        boolean skipReceive = false;
        boolean srmRespSendPending = false;
        long id = this.mListener.getConnectionId();
        if (id == -1) {
            this.replyHeader.mConnectionID = null;
        } else {
            this.replyHeader.mConnectionID = ObexHelper.convertToByteArray(id);
        }
        if (this.mSrmEnabled && (this.mSrmResponseSent ^ 1) != 0) {
            this.replyHeader.setHeader(HeaderSet.SINGLE_RESPONSE_MODE, Byte.valueOf((byte) 1));
            srmRespSendPending = true;
        }
        if (this.mSrmEnabled && (this.mGetOperation ^ 1) != 0 && this.mSrmLocalWait) {
            this.replyHeader.setHeader(HeaderSet.SINGLE_RESPONSE_MODE, Byte.valueOf((byte) 1));
        }
        byte[] headerArray = ObexHelper.createHeader(this.replyHeader, true);
        int bodyLength = -1;
        int orginalBodyLength = -1;
        if (this.mPrivateOutput != null) {
            bodyLength = this.mPrivateOutput.size();
            orginalBodyLength = bodyLength;
        }
        if (headerArray.length + 3 > this.mMaxPacketLength) {
            int end = 0;
            int start = 0;
            while (end != headerArray.length) {
                end = ObexHelper.findHeaderEnd(headerArray, start, this.mMaxPacketLength - 3);
                if (end == -1) {
                    this.mClosed = true;
                    if (this.mPrivateInput != null) {
                        this.mPrivateInput.close();
                    }
                    if (this.mPrivateOutput != null) {
                        this.mPrivateOutput.close();
                    }
                    this.mParent.sendResponse(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR, null);
                    throw new IOException("OBEX Packet exceeds max packet size");
                }
                byte[] sendHeader = new byte[(end - start)];
                System.arraycopy(headerArray, start, sendHeader, 0, sendHeader.length);
                this.mParent.sendResponse(type, sendHeader);
                start = end;
            }
            if (bodyLength > 0) {
                return true;
            }
            return false;
        }
        out.write(headerArray);
        if (this.mGetOperation && type == 160) {
            this.finalBitSet = true;
        }
        if (this.mSrmActive) {
            if (!this.mGetOperation && type == 144 && this.mSrmResponseSent) {
                skipSend = true;
            } else if (this.mGetOperation && !this.mRequestFinished && this.mSrmResponseSent) {
                skipSend = true;
            } else if (this.mGetOperation && this.mRequestFinished) {
                skipReceive = true;
            }
        }
        if (srmRespSendPending) {
            this.mSrmResponseSent = true;
        }
        if ((this.finalBitSet || headerArray.length < this.mMaxPacketLength - 20) && bodyLength > 0) {
            if (bodyLength > (this.mMaxPacketLength - headerArray.length) - 6) {
                bodyLength = (this.mMaxPacketLength - headerArray.length) - 6;
            }
            byte[] body = this.mPrivateOutput.readBytes(bodyLength);
            if (this.finalBitSet || this.mPrivateOutput.isClosed()) {
                if (this.mSendBodyHeader) {
                    out.write(73);
                    bodyLength += 3;
                    out.write((byte) (bodyLength >> 8));
                    out.write((byte) bodyLength);
                    out.write(body);
                }
            } else if (this.mSendBodyHeader) {
                out.write(72);
                bodyLength += 3;
                out.write((byte) (bodyLength >> 8));
                out.write((byte) bodyLength);
                out.write(body);
            }
        }
        if (this.finalBitSet && type == 160 && orginalBodyLength <= 0 && this.mSendBodyHeader) {
            out.write(73);
            out.write((byte) 0);
            out.write(3);
        }
        if (!skipSend) {
            this.mResponseSize = 3;
            this.mParent.sendResponse(type, out.toByteArray());
        }
        if (type != 144) {
            return false;
        }
        if (this.mGetOperation && skipReceive) {
            checkSrmRemoteAbort();
        } else {
            ObexPacket packet = ObexPacket.read(this.mInput);
            int headerId = packet.mHeaderId;
            if (headerId == 2 || headerId == 130 || headerId == 3 || headerId == 131) {
                if (headerId == 130) {
                    this.finalBitSet = true;
                } else if (headerId == 131) {
                    this.mRequestFinished = true;
                }
                if (packet.mLength > ObexHelper.getMaxRxPacketSize(this.mTransport)) {
                    this.mParent.sendResponse(ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE, null);
                    throw new IOException("Packet received was too large");
                } else if ((packet.mLength > 3 || (this.mSrmEnabled && packet.mLength == 3)) && !handleObexPacket(packet)) {
                    return false;
                }
            } else if (headerId == 255) {
                handleRemoteAbort();
            } else {
                this.mParent.sendResponse(192, null);
                this.mClosed = true;
                this.mExceptionString = "Bad Request Received";
                throw new IOException("Bad Request Received");
            }
        }
        return true;
    }

    private void checkSrmRemoteAbort() throws IOException {
        if (this.mInput.available() > 0) {
            ObexPacket packet = ObexPacket.read(this.mInput);
            if (packet.mHeaderId == 255) {
                handleRemoteAbort();
            } else {
                Log.w(TAG, "Received unexpected request from client - discarding...\n   headerId: " + packet.mHeaderId + " length: " + packet.mLength);
            }
        }
    }

    private void handleRemoteAbort() throws IOException {
        this.mParent.sendResponse(ResponseCodes.OBEX_HTTP_OK, null);
        this.mClosed = true;
        this.isAborted = true;
        this.mExceptionString = "Abort Received";
        throw new IOException("Abort Received");
    }

    public void abort() throws IOException {
        throw new IOException("Called from a server");
    }

    public HeaderSet getReceivedHeader() throws IOException {
        ensureOpen();
        return this.requestHeader;
    }

    public void sendHeaders(HeaderSet headers) throws IOException {
        ensureOpen();
        if (headers == null) {
            throw new IOException("Headers may not be null");
        }
        int[] headerList = headers.getHeaderList();
        if (headerList != null) {
            for (int i = 0; i < headerList.length; i++) {
                this.replyHeader.setHeader(headerList[i], headers.getHeader(headerList[i]));
            }
        }
    }

    public int getResponseCode() throws IOException {
        throw new IOException("Called from a server");
    }

    public String getEncoding() {
        return null;
    }

    public String getType() {
        try {
            return (String) this.requestHeader.getHeader(66);
        } catch (IOException e) {
            return null;
        }
    }

    public long getLength() {
        try {
            Long temp = (Long) this.requestHeader.getHeader(195);
            if (temp == null) {
                return -1;
            }
            return temp.longValue();
        } catch (IOException e) {
            return -1;
        }
    }

    public int getMaxPacketSize() {
        return (this.mMaxPacketLength - 6) - getHeaderLength();
    }

    public int getHeaderLength() {
        long id = this.mListener.getConnectionId();
        if (id == -1) {
            this.replyHeader.mConnectionID = null;
        } else {
            this.replyHeader.mConnectionID = ObexHelper.convertToByteArray(id);
        }
        return ObexHelper.createHeader(this.replyHeader, false).length;
    }

    public InputStream openInputStream() throws IOException {
        ensureOpen();
        return this.mPrivateInput;
    }

    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }

    public OutputStream openOutputStream() throws IOException {
        ensureOpen();
        if (this.mPrivateOutputOpen) {
            throw new IOException("no more input streams available, stream already opened");
        } else if (this.mRequestFinished) {
            if (this.mPrivateOutput == null) {
                this.mPrivateOutput = new PrivateOutputStream(this, getMaxPacketSize());
            }
            this.mPrivateOutputOpen = true;
            return this.mPrivateOutput;
        } else {
            throw new IOException("no  output streams available ,request not finished");
        }
    }

    public DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream(openOutputStream());
    }

    public void close() throws IOException {
        ensureOpen();
        this.mClosed = true;
    }

    public void ensureOpen() throws IOException {
        if (this.mExceptionString != null) {
            throw new IOException(this.mExceptionString);
        } else if (this.mClosed) {
            throw new IOException("Operation has already ended");
        }
    }

    public void ensureNotDone() throws IOException {
    }

    public void streamClosed(boolean inStream) throws IOException {
    }

    public void noBodyHeader() {
        this.mSendBodyHeader = false;
    }
}

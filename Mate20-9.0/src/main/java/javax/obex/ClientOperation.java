package javax.obex;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class ClientOperation implements Operation, BaseStream {
    private static final String TAG = "ClientOperation";
    private static final boolean V = false;
    private boolean mEndOfBodySent;
    private String mExceptionMessage;
    private boolean mGetFinalFlag;
    private boolean mGetOperation;
    private boolean mInputOpen;
    private int mMaxPacketSize;
    private boolean mOperationDone;
    private ClientSession mParent;
    private PrivateInputStream mPrivateInput;
    private boolean mPrivateInputOpen;
    private PrivateOutputStream mPrivateOutput;
    private boolean mPrivateOutputOpen;
    private HeaderSet mReplyHeader;
    private HeaderSet mRequestHeader;
    private boolean mSendBodyHeader = true;
    private boolean mSrmActive = false;
    private boolean mSrmEnabled = false;
    private boolean mSrmWaitingForRemote = true;

    public ClientOperation(int maxSize, ClientSession p, HeaderSet header, boolean type) throws IOException {
        this.mParent = p;
        this.mEndOfBodySent = false;
        this.mInputOpen = true;
        this.mOperationDone = false;
        this.mMaxPacketSize = maxSize;
        this.mGetOperation = type;
        this.mGetFinalFlag = false;
        this.mPrivateInputOpen = false;
        this.mPrivateOutputOpen = false;
        this.mPrivateInput = null;
        this.mPrivateOutput = null;
        this.mReplyHeader = new HeaderSet();
        this.mRequestHeader = new HeaderSet();
        int[] headerList = header.getHeaderList();
        if (headerList != null) {
            for (int i = 0; i < headerList.length; i++) {
                this.mRequestHeader.setHeader(headerList[i], header.getHeader(headerList[i]));
            }
        }
        if (header.mAuthChall != null) {
            this.mRequestHeader.mAuthChall = new byte[header.mAuthChall.length];
            System.arraycopy(header.mAuthChall, 0, this.mRequestHeader.mAuthChall, 0, header.mAuthChall.length);
        }
        if (header.mAuthResp != null) {
            this.mRequestHeader.mAuthResp = new byte[header.mAuthResp.length];
            System.arraycopy(header.mAuthResp, 0, this.mRequestHeader.mAuthResp, 0, header.mAuthResp.length);
        }
        if (header.mConnectionID != null) {
            this.mRequestHeader.mConnectionID = new byte[4];
            System.arraycopy(header.mConnectionID, 0, this.mRequestHeader.mConnectionID, 0, 4);
        }
    }

    public void setGetFinalFlag(boolean flag) {
        this.mGetFinalFlag = flag;
    }

    public synchronized void abort() throws IOException {
        ensureOpen();
        if (this.mOperationDone) {
            if (this.mReplyHeader.responseCode != 144) {
                throw new IOException("Operation has already ended");
            }
        }
        this.mExceptionMessage = "Operation aborted";
        if (!this.mOperationDone && this.mReplyHeader.responseCode == 144) {
            this.mOperationDone = true;
            this.mParent.sendRequest(255, null, this.mReplyHeader, null, false);
            if (this.mReplyHeader.responseCode == 160) {
                this.mExceptionMessage = null;
            } else {
                throw new IOException("Invalid response code from server");
            }
        }
        close();
    }

    public synchronized int getResponseCode() throws IOException {
        if (this.mReplyHeader.responseCode == -1 || this.mReplyHeader.responseCode == 144) {
            validateConnection();
        }
        return this.mReplyHeader.responseCode;
    }

    public String getEncoding() {
        return null;
    }

    public String getType() {
        try {
            return (String) this.mReplyHeader.getHeader(66);
        } catch (IOException e) {
            return null;
        }
    }

    public long getLength() {
        try {
            Long temp = (Long) this.mReplyHeader.getHeader(195);
            if (temp == null) {
                return -1;
            }
            return temp.longValue();
        } catch (IOException e) {
            return -1;
        }
    }

    public InputStream openInputStream() throws IOException {
        ensureOpen();
        if (!this.mPrivateInputOpen) {
            if (this.mGetOperation) {
                validateConnection();
            } else if (this.mPrivateInput == null) {
                this.mPrivateInput = new PrivateInputStream(this);
            }
            this.mPrivateInputOpen = true;
            return this.mPrivateInput;
        }
        throw new IOException("no more input streams available");
    }

    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }

    public OutputStream openOutputStream() throws IOException {
        ensureOpen();
        ensureNotDone();
        if (!this.mPrivateOutputOpen) {
            if (this.mPrivateOutput == null) {
                this.mPrivateOutput = new PrivateOutputStream(this, getMaxPacketSize());
            }
            this.mPrivateOutputOpen = true;
            return this.mPrivateOutput;
        }
        throw new IOException("no more output streams available");
    }

    public int getMaxPacketSize() {
        return (this.mMaxPacketSize - 6) - getHeaderLength();
    }

    public int getHeaderLength() {
        return ObexHelper.createHeader(this.mRequestHeader, false).length;
    }

    public DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream(openOutputStream());
    }

    public void close() throws IOException {
        this.mInputOpen = false;
        this.mPrivateInputOpen = false;
        this.mPrivateOutputOpen = false;
        this.mParent.setRequestInactive();
    }

    public HeaderSet getReceivedHeader() throws IOException {
        ensureOpen();
        return this.mReplyHeader;
    }

    public void sendHeaders(HeaderSet headers) throws IOException {
        ensureOpen();
        if (this.mOperationDone) {
            throw new IOException("Operation has already exchanged all data");
        } else if (headers != null) {
            int[] headerList = headers.getHeaderList();
            if (headerList != null) {
                for (int i = 0; i < headerList.length; i++) {
                    this.mRequestHeader.setHeader(headerList[i], headers.getHeader(headerList[i]));
                }
            }
        } else {
            throw new IOException("Headers may not be null");
        }
    }

    public void ensureNotDone() throws IOException {
        if (this.mOperationDone) {
            throw new IOException("Operation has completed");
        }
    }

    public void ensureOpen() throws IOException {
        this.mParent.ensureOpen();
        if (this.mExceptionMessage != null) {
            throw new IOException(this.mExceptionMessage);
        } else if (!this.mInputOpen) {
            throw new IOException("Operation has already ended");
        }
    }

    private void validateConnection() throws IOException {
        ensureOpen();
        if (this.mPrivateInput == null || this.mReplyHeader.responseCode == -1) {
            startProcessing();
        }
    }

    private boolean sendRequest(int opCode) throws IOException {
        int opCode2;
        boolean returnValue = false;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bodyLength = -1;
        byte[] headerArray = ObexHelper.createHeader(this.mRequestHeader, true);
        if (this.mPrivateOutput != null) {
            bodyLength = this.mPrivateOutput.size();
        }
        if (headerArray.length + 3 + 3 > this.mMaxPacketSize) {
            int end = 0;
            int start = 0;
            while (end != headerArray.length) {
                end = ObexHelper.findHeaderEnd(headerArray, start, this.mMaxPacketSize - 3);
                if (end == -1) {
                    this.mOperationDone = true;
                    abort();
                    this.mExceptionMessage = "Header larger then can be sent in a packet";
                    this.mInputOpen = false;
                    if (this.mPrivateInput != null) {
                        this.mPrivateInput.close();
                    }
                    if (this.mPrivateOutput != null) {
                        this.mPrivateOutput.close();
                    }
                    throw new IOException("OBEX Packet exceeds max packet size");
                }
                byte[] sendHeader = new byte[(end - start)];
                System.arraycopy(headerArray, start, sendHeader, 0, sendHeader.length);
                if (!this.mParent.sendRequest(opCode, sendHeader, this.mReplyHeader, this.mPrivateInput, false) || this.mReplyHeader.responseCode != 144) {
                    return false;
                }
                start = end;
            }
            checkForSrm();
            if (bodyLength > 0) {
                return true;
            }
            return false;
        }
        if (this.mSendBodyHeader == 0) {
            opCode2 = opCode | 128;
        } else {
            opCode2 = opCode;
        }
        out.write(headerArray);
        if (bodyLength > 0) {
            if (bodyLength > (this.mMaxPacketSize - headerArray.length) - 6) {
                returnValue = true;
                bodyLength = (this.mMaxPacketSize - headerArray.length) - 6;
            }
            byte[] body = this.mPrivateOutput.readBytes(bodyLength);
            if (!this.mPrivateOutput.isClosed() || returnValue || this.mEndOfBodySent || (opCode2 & 128) == 0) {
                out.write(72);
            } else {
                out.write(73);
                this.mEndOfBodySent = true;
            }
            bodyLength += 3;
            out.write((byte) (bodyLength >> 8));
            out.write((byte) bodyLength);
            if (body != null) {
                out.write(body);
            }
        }
        if (this.mPrivateOutputOpen && bodyLength <= 0 && !this.mEndOfBodySent) {
            if ((opCode2 & 128) == 0) {
                out.write(72);
            } else {
                out.write(73);
                this.mEndOfBodySent = true;
            }
            out.write((byte) (3 >> 8));
            out.write((byte) 3);
        }
        if (out.size() == 0) {
            if (!this.mParent.sendRequest(opCode2, null, this.mReplyHeader, this.mPrivateInput, this.mSrmActive)) {
                return false;
            }
            checkForSrm();
            return returnValue;
        }
        if (out.size() > 0) {
            if (!this.mParent.sendRequest(opCode2, out.toByteArray(), this.mReplyHeader, this.mPrivateInput, this.mSrmActive)) {
                return false;
            }
        }
        checkForSrm();
        if (this.mPrivateOutput != null && this.mPrivateOutput.size() > 0) {
            returnValue = true;
        }
        return returnValue;
    }

    private void checkForSrm() throws IOException {
        Byte srmMode = (Byte) this.mReplyHeader.getHeader(HeaderSet.SINGLE_RESPONSE_MODE);
        if (this.mParent.isSrmSupported() && srmMode != null && srmMode.byteValue() == 1) {
            this.mSrmEnabled = true;
        }
        if (this.mSrmEnabled) {
            this.mSrmWaitingForRemote = false;
            Byte srmp = (Byte) this.mReplyHeader.getHeader(HeaderSet.SINGLE_RESPONSE_MODE_PARAMETER);
            if (srmp != null && srmp.byteValue() == 1) {
                this.mSrmWaitingForRemote = true;
                this.mReplyHeader.setHeader(HeaderSet.SINGLE_RESPONSE_MODE_PARAMETER, null);
            }
        }
        if (!this.mSrmWaitingForRemote && this.mSrmEnabled) {
            this.mSrmActive = true;
        }
    }

    private synchronized void startProcessing() throws IOException {
        if (this.mPrivateInput == null) {
            this.mPrivateInput = new PrivateInputStream(this);
        }
        boolean more = true;
        if (!this.mGetOperation) {
            if (!this.mOperationDone) {
                this.mReplyHeader.responseCode = ResponseCodes.OBEX_HTTP_CONTINUE;
                while (more && this.mReplyHeader.responseCode == 144) {
                    more = sendRequest(2);
                }
            }
            if (this.mReplyHeader.responseCode == 144) {
                this.mParent.sendRequest(ObexHelper.OBEX_OPCODE_PUT_FINAL, null, this.mReplyHeader, this.mPrivateInput, this.mSrmActive);
            }
            if (this.mReplyHeader.responseCode != 144) {
                this.mOperationDone = true;
            }
        } else if (!this.mOperationDone) {
            if (!this.mGetFinalFlag) {
                this.mReplyHeader.responseCode = ResponseCodes.OBEX_HTTP_CONTINUE;
                while (more && this.mReplyHeader.responseCode == 144) {
                    more = sendRequest(3);
                }
                if (this.mReplyHeader.responseCode == 144) {
                    this.mParent.sendRequest(ObexHelper.OBEX_OPCODE_GET_FINAL, null, this.mReplyHeader, this.mPrivateInput, this.mSrmActive);
                }
                if (this.mReplyHeader.responseCode != 144) {
                    this.mOperationDone = true;
                } else {
                    checkForSrm();
                }
            } else if (!sendRequest(ObexHelper.OBEX_OPCODE_GET_FINAL)) {
                this.mOperationDone = true;
            } else {
                throw new IOException("FINAL_GET forced, data didn't fit into one packet");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002a, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0053, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0080, code lost:
        return false;
     */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:29:0x0054=Splitter:B:29:0x0054, B:50:0x0079=Splitter:B:50:0x0079} */
    public synchronized boolean continueOperation(boolean sendEmpty, boolean inStream) throws IOException {
        if (!this.mGetOperation) {
            if (!inStream) {
                if (!this.mOperationDone) {
                    if (this.mReplyHeader.responseCode == -1) {
                        this.mReplyHeader.responseCode = ResponseCodes.OBEX_HTTP_CONTINUE;
                    }
                    sendRequest(2);
                    return true;
                }
            }
            if (inStream) {
                if (!this.mOperationDone) {
                    return false;
                }
            }
            if (this.mOperationDone) {
                return false;
            }
        } else if (!inStream || this.mOperationDone) {
            if (!inStream) {
                if (!this.mOperationDone) {
                    if (this.mPrivateInput == null) {
                        this.mPrivateInput = new PrivateInputStream(this);
                    }
                    if (!this.mGetFinalFlag) {
                        sendRequest(3);
                    } else {
                        sendRequest(ObexHelper.OBEX_OPCODE_GET_FINAL);
                    }
                    if (this.mReplyHeader.responseCode != 144) {
                        this.mOperationDone = true;
                    }
                }
            }
            if (this.mOperationDone) {
                return false;
            }
        } else {
            this.mParent.sendRequest(ObexHelper.OBEX_OPCODE_GET_FINAL, null, this.mReplyHeader, this.mPrivateInput, this.mSrmActive);
            if (this.mReplyHeader.responseCode != 144) {
                this.mOperationDone = true;
            } else {
                checkForSrm();
            }
        }
    }

    public void streamClosed(boolean inStream) throws IOException {
        if (!this.mGetOperation) {
            if (!inStream && !this.mOperationDone) {
                boolean more = true;
                if (this.mPrivateOutput != null && this.mPrivateOutput.size() <= 0 && ObexHelper.createHeader(this.mRequestHeader, false).length <= 0) {
                    more = false;
                }
                if (this.mReplyHeader.responseCode == -1) {
                    this.mReplyHeader.responseCode = ResponseCodes.OBEX_HTTP_CONTINUE;
                }
                while (more && this.mReplyHeader.responseCode == 144) {
                    more = sendRequest(2);
                }
                while (this.mReplyHeader.responseCode == 144) {
                    sendRequest(ObexHelper.OBEX_OPCODE_PUT_FINAL);
                }
                this.mOperationDone = true;
            } else if (inStream && this.mOperationDone) {
                this.mOperationDone = true;
            }
        } else if (inStream && !this.mOperationDone) {
            if (this.mReplyHeader.responseCode == -1) {
                this.mReplyHeader.responseCode = ResponseCodes.OBEX_HTTP_CONTINUE;
            }
            while (this.mReplyHeader.responseCode == 144 && !this.mOperationDone) {
                if (!sendRequest(ObexHelper.OBEX_OPCODE_GET_FINAL)) {
                    break;
                }
            }
            while (this.mReplyHeader.responseCode == 144 && !this.mOperationDone) {
                this.mParent.sendRequest(ObexHelper.OBEX_OPCODE_GET_FINAL, null, this.mReplyHeader, this.mPrivateInput, false);
            }
            this.mOperationDone = true;
        } else if (!inStream && !this.mOperationDone) {
            boolean more2 = true;
            if (this.mPrivateOutput != null && this.mPrivateOutput.size() <= 0 && ObexHelper.createHeader(this.mRequestHeader, false).length <= 0) {
                more2 = false;
            }
            if (this.mPrivateInput == null) {
                this.mPrivateInput = new PrivateInputStream(this);
            }
            if (this.mPrivateOutput != null && this.mPrivateOutput.size() <= 0) {
                more2 = false;
            }
            this.mReplyHeader.responseCode = ResponseCodes.OBEX_HTTP_CONTINUE;
            while (more2 && this.mReplyHeader.responseCode == 144) {
                more2 = sendRequest(3);
            }
            sendRequest(ObexHelper.OBEX_OPCODE_GET_FINAL);
            if (this.mReplyHeader.responseCode != 144) {
                this.mOperationDone = true;
            }
        }
    }

    public void noBodyHeader() {
        this.mSendBodyHeader = false;
    }
}

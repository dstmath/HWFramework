package android_maps_conflict_avoidance.com.google.googlenav.datarequest;

import android_maps_conflict_avoidance.com.google.common.Clock;
import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.Log;
import android_maps_conflict_avoidance.com.google.common.Log.LogSaver;
import android_maps_conflict_avoidance.com.google.common.StaticUtil;
import android_maps_conflict_avoidance.com.google.common.io.GoogleHttpConnection;
import android_maps_conflict_avoidance.com.google.common.io.HttpConnectionFactory;
import android_maps_conflict_avoidance.com.google.common.io.PersistentStore;
import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBuf;
import android_maps_conflict_avoidance.com.google.common.util.text.TextUtil;
import android_maps_conflict_avoidance.com.google.googlenav.GmmSettings;
import android_maps_conflict_avoidance.com.google.googlenav.proto.GmmMessageTypes;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

public class DataRequestDispatcher {
    public static final int MAX_WORKER_THREAD_COUNT = 4;
    private static volatile DataRequestDispatcher instance;
    private static int requestId = 0;
    protected volatile boolean active = false;
    protected int bytesReceived;
    protected int bytesSent;
    private final Clock clock;
    protected HttpConnectionFactory connectionFactory;
    protected long cookie;
    protected final boolean debug;
    protected final DispatcherServer defaultServer;
    protected final String distributionChannel;
    private long errorRetryTime = 0;
    private long firstConnectionErrorTime = Long.MIN_VALUE;
    protected String globalSpecialUrlArguments;
    private volatile long lastActiveTime = Long.MIN_VALUE;
    private Throwable lastException;
    private long lastExceptionTime;
    private volatile long lastSuccessTime = Long.MIN_VALUE;
    private final Vector listeners = new Vector();
    private long maxNetworkErrorRetryTimeout = 300000;
    protected volatile boolean mockLostDataConnection;
    private volatile boolean networkErrorMode;
    private volatile int networkSpeedBytesPerSecond = -1;
    protected final String platformID;
    protected final ProtoBuf properties;
    protected volatile String serverAddress;
    protected final String softwareVersion;
    private volatile int suspendCount;
    protected Vector thirdPartyServers = new Vector();
    private final Object threadDispatchLock = new Object();
    protected ConnectionWarmUpManager warmUpManager;
    private volatile int workerForegroundThreadCount = 0;
    private volatile int workerSubmissionThreadCount = 0;
    private volatile int workerThreadCount = 0;

    private class CookieDataRequest extends BaseDataRequest {
        private CookieDataRequest() {
        }

        public int getRequestType() {
            return 15;
        }

        public boolean isImmediate() {
            return false;
        }

        public void writeRequestData(DataOutput dos) throws IOException {
        }

        public boolean readResponseData(DataInput dis) throws IOException {
            DataRequestDispatcher.this.cookie = dis.readLong();
            DataRequestDispatcher.saveCookie(DataRequestDispatcher.this.cookie);
            return true;
        }
    }

    public static final class DataRequestEventUploader implements LogSaver {
        private final DataRequestDispatcher drd;

        private DataRequestEventUploader(DataRequestDispatcher drd) {
            this.drd = drd;
        }

        public Object uploadEventLog(boolean immediate, Object waitObject, byte[] logBytes) {
            if (logBytes != null && logBytes.length > 2) {
                SimpleDataRequest edr = new SimpleDataRequest(10, logBytes, immediate, false, waitObject);
                DataRequestDispatcher drd = DataRequestDispatcher.getInstance();
                if (drd == null) {
                    return null;
                }
                drd.addDataRequest(edr);
            }
            return null;
        }
    }

    public class DispatcherServer implements Runnable {
        protected final byte headerFlag;
        protected volatile String serverAddress;
        protected Vector serverRequests = new Vector();
        protected final Vector supportedDataRequests;
        private Vector tempRequests;

        protected boolean canHandle(int protocolId) {
            return this.supportedDataRequests.isEmpty() || this.supportedDataRequests.contains(new Integer(protocolId));
        }

        public DispatcherServer(String address, Vector protocolList, byte headerFlag) {
            this.serverAddress = address;
            this.supportedDataRequests = protocolList;
            this.headerFlag = (byte) headerFlag;
        }

        public void addDataRequest(DataRequest dataRequest) {
            this.serverRequests.addElement(dataRequest);
            if (dataRequest.isImmediate() && !DataRequestDispatcher.this.isSuspended()) {
                activate();
            }
        }

        protected Vector dequeuePendingRequests() {
            Vector pendingServerRequests;
            synchronized (this) {
                pendingServerRequests = this.serverRequests;
                this.serverRequests = new Vector();
            }
            return pendingServerRequests;
        }

        protected synchronized void activate() {
            if (DataRequestDispatcher.this.canDispatchNow()) {
                synchronized (DataRequestDispatcher.this.threadDispatchLock) {
                    this.tempRequests = dequeuePendingRequests();
                    if (this.tempRequests != null) {
                        DataRequestDispatcher.this.workerThreadCount = DataRequestDispatcher.this.workerThreadCount + 1;
                        if (DataRequestDispatcher.containsForegroundRequest(this.tempRequests)) {
                            DataRequestDispatcher.this.workerForegroundThreadCount = DataRequestDispatcher.this.workerForegroundThreadCount + 1;
                        }
                        if (DataRequestDispatcher.containsSubmissionRequest(this.tempRequests)) {
                            DataRequestDispatcher.this.workerSubmissionThreadCount = DataRequestDispatcher.this.workerSubmissionThreadCount + 1;
                        }
                        new Thread(this, "DataRequestDispatcher").start();
                        while (this.tempRequests != null) {
                            try {
                                DataRequestDispatcher.this.threadDispatchLock.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            }
        }

        protected void activateIfNeeded() {
            if (checkNeedToActivate()) {
                activate();
            }
        }

        private boolean checkNeedToActivate() {
            if (DataRequestDispatcher.this.isSuspended()) {
                return false;
            }
            synchronized (this.serverRequests) {
                for (int i = 0; i < this.serverRequests.size(); i++) {
                    if (((DataRequest) this.serverRequests.elementAt(i)).isImmediate()) {
                        return true;
                    }
                }
                return false;
            }
        }

        public void run() {
            Vector requests;
            synchronized (DataRequestDispatcher.this.threadDispatchLock) {
                requests = this.tempRequests;
                this.tempRequests = null;
                DataRequestDispatcher.this.lastActiveTime = DataRequestDispatcher.this.clock.relativeTimeMillis();
                DataRequestDispatcher.this.threadDispatchLock.notifyAll();
            }
            boolean containsForegroundRequest = DataRequestDispatcher.containsForegroundRequest(requests);
            boolean containsSubmissionRequest = DataRequestDispatcher.containsSubmissionRequest(requests);
            while (DataRequestDispatcher.this.active) {
                try {
                    if (requests.size() <= 0) {
                        break;
                    }
                    synchronized (this) {
                        if ((DataRequestDispatcher.this.errorRetryTime <= 0 ? 1 : null) == null) {
                            try {
                                wait(DataRequestDispatcher.this.errorRetryTime);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                    DataRequestDispatcher.this.serviceRequests(requests, this);
                    DataRequestDispatcher.this.connectionFactory.registerNetworkSuccess(false);
                    DataRequestDispatcher.this.clearNetworkError();
                    DataRequestDispatcher.this.lastSuccessTime = DataRequestDispatcher.this.clock.relativeTimeMillis();
                } catch (Exception e2) {
                    networkAccessDenied(e2);
                } catch (IOException e3) {
                    DataRequestDispatcher.this.handleError(3, e3);
                } catch (Exception e4) {
                    DataRequestDispatcher.this.handleError(5, e4);
                    Log.logThrowable("REQUEST", e4);
                } catch (Throwable e5) {
                    StaticUtil.handleOutOfMemory();
                    DataRequestDispatcher.this.handleError(5, e5);
                } catch (Throwable th) {
                    synchronized (DataRequestDispatcher.this.threadDispatchLock) {
                        DataRequestDispatcher.this.workerThreadCount = DataRequestDispatcher.this.workerThreadCount - 1;
                        if (containsForegroundRequest) {
                            DataRequestDispatcher.this.workerForegroundThreadCount = DataRequestDispatcher.this.workerForegroundThreadCount - 1;
                        }
                        if (containsSubmissionRequest) {
                            DataRequestDispatcher.this.workerSubmissionThreadCount = DataRequestDispatcher.this.workerSubmissionThreadCount - 1;
                        }
                        activateIfNeeded();
                    }
                }
            }
            synchronized (DataRequestDispatcher.this.threadDispatchLock) {
                DataRequestDispatcher.this.workerThreadCount = DataRequestDispatcher.this.workerThreadCount - 1;
                if (containsForegroundRequest) {
                    DataRequestDispatcher.this.workerForegroundThreadCount = DataRequestDispatcher.this.workerForegroundThreadCount - 1;
                }
                if (containsSubmissionRequest) {
                    DataRequestDispatcher.this.workerSubmissionThreadCount = DataRequestDispatcher.this.workerSubmissionThreadCount - 1;
                }
            }
            activateIfNeeded();
        }

        private void networkAccessDenied(Exception e) {
            Log.logQuietThrowable("REQUEST", e);
            DataRequestDispatcher.this.stop();
            DataRequestDispatcher.this.maybeNotifyNetworkError(0);
        }

        public void start() {
            activateIfNeeded();
        }
    }

    public static synchronized DataRequestDispatcher createInstance(String serverAddress, String platformID, String softwareVersion, String distributionChannel, boolean debug) {
        DataRequestDispatcher dataRequestDispatcher;
        synchronized (DataRequestDispatcher.class) {
            if (instance == null) {
                instance = new DataRequestDispatcher(serverAddress, platformID, softwareVersion, distributionChannel, debug);
                Log.setLogSaver(new DataRequestEventUploader());
                dataRequestDispatcher = instance;
            } else {
                throw new RuntimeException("Attempting to create multiple DataRequestDispatchers");
            }
        }
        return dataRequestDispatcher;
    }

    public static DataRequestDispatcher getInstance() {
        return instance;
    }

    protected DataRequestDispatcher(String serverAddress, String platformID, String softwareVersion, String distributionChannel, boolean debug) {
        if (serverAddress == null || serverAddress.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.serverAddress = serverAddress;
        this.softwareVersion = softwareVersion;
        this.platformID = platformID;
        this.distributionChannel = distributionChannel;
        this.debug = debug;
        this.connectionFactory = Config.getInstance().getConnectionFactory();
        this.clock = Config.getInstance().getClock();
        this.warmUpManager = new ConnectionWarmUpManager(this, this.clock);
        this.bytesSent = 0;
        this.bytesReceived = 0;
        this.defaultServer = new DispatcherServer(this.serverAddress, new Vector(), (byte) 0);
        this.cookie = loadOrRequestCookie();
        this.properties = new ProtoBuf(GmmMessageTypes.CLIENT_PROPERTIES_REQUEST_PROTO);
    }

    public synchronized boolean isSuspended() {
        boolean z = false;
        synchronized (this) {
            if (this.suspendCount > 0) {
                z = true;
            }
        }
        return z;
    }

    protected long loadOrRequestCookie() {
        DataInput dis = StaticUtil.readPreferenceAsDataInput("SessionID");
        if (dis != null) {
            try {
                return dis.readLong();
            } catch (IOException e) {
                Config.getInstance().getPersistentStore().setPreference("SessionID", null);
            }
        }
        addDataRequest(new CookieDataRequest());
        return 0;
    }

    public synchronized void addDataRequestListener(DataRequestListener listenerData) {
        if (!this.listeners.contains(listenerData)) {
            this.listeners.addElement(listenerData);
        }
    }

    public synchronized void removeDataRequestListener(DataRequestListener listenerData) {
        this.listeners.removeElement(listenerData);
    }

    protected synchronized DataRequestListener[] snapshotListeners() {
        DataRequestListener[] listenersArray;
        listenersArray = new DataRequestListener[this.listeners.size()];
        this.listeners.copyInto(listenersArray);
        return listenersArray;
    }

    protected void notifyComplete(DataRequest dataRequest) {
        DataRequestListener[] listenersArray = snapshotListeners();
        for (DataRequestListener onComplete : listenersArray) {
            onComplete.onComplete(dataRequest);
        }
    }

    protected void notifyNetworkError(int errorCode, boolean networkEverWorked, String debugMessage) {
        DataRequestListener[] listenersArray = snapshotListeners();
        for (DataRequestListener onNetworkError : listenersArray) {
            onNetworkError.onNetworkError(errorCode, networkEverWorked, debugMessage);
        }
    }

    protected final void maybeNotifyNetworkError(int errorCode) {
        boolean notifyListeners = false;
        synchronized (this) {
            if (!this.networkErrorMode) {
                Log.logToScreen("DRD: in Error Mode");
                this.networkErrorMode = true;
                this.firstConnectionErrorTime = Long.MIN_VALUE;
                notifyListeners = true;
            }
        }
        boolean networkEverWorked = this.connectionFactory.getNetworkWorked();
        if (notifyListeners) {
            notifyNetworkError(errorCode, networkEverWorked, null);
        }
    }

    public void addDataRequest(DataRequest dataRequest) {
        if (this.mockLostDataConnection) {
            notifyNetworkError(5, true, null);
        }
        for (int i = 0; i < this.thirdPartyServers.size(); i++) {
            DispatcherServer tps = (DispatcherServer) this.thirdPartyServers.elementAt(i);
            if (tps.canHandle(dataRequest.getRequestType())) {
                tps.addDataRequest(dataRequest);
                return;
            }
        }
        this.defaultServer.addDataRequest(dataRequest);
    }

    public synchronized boolean canDispatchNow() {
        boolean z = false;
        synchronized (this) {
            if (this.active) {
                if (this.workerThreadCount < MAX_WORKER_THREAD_COUNT && (this.connectionFactory.getNetworkWorkedThisSession() || this.workerThreadCount == 0)) {
                    z = true;
                }
            }
        }
        return z;
    }

    public void stop() {
        this.active = false;
    }

    public void start() {
        this.active = true;
        for (int i = 0; i < this.thirdPartyServers.size(); i++) {
            ((DispatcherServer) this.thirdPartyServers.elementAt(i)).start();
        }
        this.defaultServer.start();
    }

    private synchronized void clearNetworkError() {
        this.firstConnectionErrorTime = Long.MIN_VALUE;
        this.networkErrorMode = false;
        this.errorRetryTime = 0;
    }

    private void handleError(int code, Throwable t) {
        Object obj = 1;
        boolean call = false;
        synchronized (this) {
            this.lastException = t;
            this.lastExceptionTime = System.currentTimeMillis();
            if (t != null && GmmSettings.isDebugBuild()) {
                t.printStackTrace();
            }
            this.connectionFactory.notifyFailure();
            if (this.networkErrorMode) {
                Object obj2;
                if (this.errorRetryTime >= 2000) {
                    obj2 = 1;
                } else {
                    obj2 = null;
                }
                if (obj2 == null) {
                    this.errorRetryTime = 2000;
                } else {
                    this.errorRetryTime = (this.errorRetryTime * 5) / 4;
                }
                if (this.errorRetryTime > this.maxNetworkErrorRetryTimeout) {
                    obj = null;
                }
                if (obj == null) {
                    this.errorRetryTime = this.maxNetworkErrorRetryTimeout;
                }
            } else {
                this.errorRetryTime = 200;
                if (this.firstConnectionErrorTime == Long.MIN_VALUE) {
                    this.firstConnectionErrorTime = this.clock.relativeTimeMillis();
                } else {
                    if (this.firstConnectionErrorTime + 15000 < this.clock.relativeTimeMillis()) {
                        obj = null;
                    }
                    if (obj == null) {
                        call = true;
                    }
                }
            }
        }
        if (call) {
            if (code == 3 && this.connectionFactory.usingMDS() && !this.connectionFactory.getNetworkWorked()) {
                code = 4;
            }
            maybeNotifyNetworkError(code);
        }
    }

    protected void serviceRequests(Vector requests, DispatcherServer dispatcherServer) throws IOException, SecurityException {
        int i;
        GoogleHttpConnection hc = null;
        DataOutputStream os = null;
        DataInputStream is = null;
        this.warmUpManager.onStartServiceRequests(requests);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String addToUrl = urlArguments(requests);
        generateRequest(requests, baos, dispatcherServer);
        StringBuffer stringBuffer = new StringBuffer("DRD");
        StringBuffer append = stringBuffer.append("(");
        int i2 = requestId;
        requestId = i2 + 1;
        append.append(i2).append("): ");
        for (i = 0; i < requests.size(); i++) {
            stringBuffer.append(((DataRequest) requests.elementAt(i)).getRequestType());
            if (i != requests.size() - 1) {
                stringBuffer.append("|");
            }
        }
        byte[] sendData = baos.toByteArray();
        DataRequest dataRequest;
        int index;
        try {
            long startTime = this.clock.relativeTimeMillis();
            hc = this.connectionFactory.createConnection(dispatcherServer.serverAddress + addToUrl, true);
            hc.setConnectionProperty("Content-Type", "application/binary");
            hc.setConnectionProperty("Content-Length", "" + sendData.length);
            os = hc.openDataOutputStream();
            os.write(sendData);
            this.bytesSent += sendData.length;
            is = hc.openDataInputStream();
            int responseCode = hc.getResponseCode();
            String contentType = hc.getContentType();
            long firstByteTime = this.clock.relativeTimeMillis() - startTime;
            stringBuffer.append(", ");
            if ((firstByteTime >= 1000 ? 1 : null) == null) {
                stringBuffer.append("<1s");
            } else {
                stringBuffer.append(firstByteTime / 1000).append("s");
            }
            if (responseCode == 501) {
                maybeNotifyNetworkError(2);
                Log.logToScreen(stringBuffer.toString());
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e2) {
                    }
                }
                if (hc != null) {
                    try {
                        hc.close();
                    } catch (IOException e3) {
                    }
                }
                for (i = 0; i < requests.size(); i++) {
                    dataRequest = (DataRequest) requests.elementAt(i);
                    if (!dataRequest.retryOnFailure()) {
                        requests.removeElement(dataRequest);
                    }
                }
            } else if (responseCode != 200) {
                Log.logToScreen("Bad Response Code " + responseCode + " " + stringBuffer.toString());
                if (responseCode == 500) {
                    stringBuffer = new StringBuffer("Server 500 for request types: ");
                    for (index = 0; index < requests.size(); index++) {
                        dataRequest = (DataRequest) requests.elementAt(index);
                        dataRequest.onServerFailure();
                        stringBuffer.append(dataRequest.getRequestType());
                        if (index != requests.size() - 1) {
                            stringBuffer.append(',');
                        }
                    }
                    if (this.debug) {
                        notifyNetworkError(7, this.connectionFactory.getNetworkWorked(), stringBuffer.toString());
                    }
                }
                throw new IOException("Bad HTTP response code: " + responseCode);
            } else if ("application/binary".equals(contentType)) {
                int responseLength = (int) hc.getLength();
                this.bytesReceived += responseLength;
                if (is.readUnsignedShort() == 23) {
                    index = 0;
                    while (index < requests.size()) {
                        dataRequest = (DataRequest) requests.elementAt(index);
                        processDataRequest(is, dataRequest, dispatcherServer);
                        index++;
                    }
                    int elapsedTime = (int) (this.clock.relativeTimeMillis() - startTime);
                    Log.addEvent((short) 22, "fb", "" + firstByteTime);
                    Log.addEvent((short) 22, "lb", "" + elapsedTime);
                    this.warmUpManager.onFinishServiceRequests(requests, startTime, (int) firstByteTime, elapsedTime);
                    if (responseLength >= 8192) {
                        if ((((long) elapsedTime) > 60000 ? 1 : null) == null) {
                            this.networkSpeedBytesPerSecond = (responseLength * 1000) / elapsedTime;
                        }
                    }
                    stringBuffer.append(", ");
                    if (responseLength >= 1000) {
                        stringBuffer.append(responseLength / 1000).append("kb");
                    } else {
                        stringBuffer.append("<1kb");
                    }
                    requests.removeAllElements();
                    Log.logToScreen(stringBuffer.toString());
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e4) {
                        }
                    }
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e5) {
                        }
                    }
                    if (hc != null) {
                        try {
                            hc.close();
                        } catch (IOException e6) {
                        }
                    }
                    for (i = 0; i < requests.size(); i++) {
                        dataRequest = (DataRequest) requests.elementAt(i);
                        if (!dataRequest.retryOnFailure()) {
                            requests.removeElement(dataRequest);
                        }
                    }
                    return;
                }
                maybeNotifyNetworkError(1);
                Log.logToScreen(stringBuffer.toString());
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e7) {
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e8) {
                    }
                }
                if (hc != null) {
                    try {
                        hc.close();
                    } catch (IOException e9) {
                    }
                }
                for (i = 0; i < requests.size(); i++) {
                    dataRequest = (DataRequest) requests.elementAt(i);
                    if (!dataRequest.retryOnFailure()) {
                        requests.removeElement(dataRequest);
                    }
                }
            } else {
                Log.logToScreen("Bad HTTP content type: " + contentType + " " + stringBuffer.toString());
                throw new IOException("Bad HTTP content type: " + contentType);
            }
        } catch (IOException e10) {
            Log.logToScreen("IOException: " + dataRequest.getRequestType());
            if (this.debug) {
                System.err.println("IOException processing: " + dataRequest.getRequestType());
                e10.printStackTrace();
            }
            if (e10 instanceof EOFException) {
                dataRequest.onServerFailure();
                if (this.debug) {
                    notifyNetworkError(7, this.connectionFactory.getNetworkWorked(), "No server support for data request: " + dataRequest.getRequestType());
                }
            }
            for (i = 0; i < index; i++) {
                requests.removeElementAt(0);
            }
            throw e10;
        } catch (RuntimeException e11) {
            Log.logToScreen("RuntimeException: " + dataRequest.getRequestType());
            if (this.debug) {
                System.err.println("RuntimeException processing: " + dataRequest.getRequestType());
                e11.printStackTrace();
            }
            throw e11;
        } catch (Throwable th) {
            Log.logToScreen(stringBuffer.toString());
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e12) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e13) {
                }
            }
            if (hc != null) {
                try {
                    hc.close();
                } catch (IOException e14) {
                }
            }
            for (i = 0; i < requests.size(); i++) {
                dataRequest = (DataRequest) requests.elementAt(i);
                if (!dataRequest.retryOnFailure()) {
                    requests.removeElement(dataRequest);
                }
            }
        }
    }

    protected void processDataRequest(DataInput is, DataRequest dataRequest, DispatcherServer dispatcherServer) throws IOException {
        int requestType = is.readUnsignedByte();
        if (requestType != dataRequest.getRequestType()) {
            throw new IOException("RT: " + requestType + " != " + dataRequest.getRequestType());
        } else if (!dataRequest.readResponseData(is)) {
            dispatcherServer.serverRequests.insertElementAt(dataRequest, 0);
        } else if (dataRequest != this && !dataRequest.isCancelled()) {
            notifyComplete(dataRequest);
        }
    }

    public void generateRequest(Vector requests, OutputStream outputStream, DispatcherServer dispatcherServer) throws IOException {
        DataOutputStream out = new DataOutputStream(outputStream);
        addClientPropertiesRequest(requests, dispatcherServer);
        if (dispatcherServer.headerFlag == (byte) 0) {
            out.writeShort(23);
            out.writeLong(this.cookie);
            out.writeUTF(Config.getLocale());
            out.writeUTF(this.platformID);
            out.writeUTF(this.softwareVersion);
            out.writeUTF(this.distributionChannel);
        } else if (dispatcherServer.headerFlag == (byte) 1) {
            out.writeShort(23);
            out.writeLong(this.cookie);
            out.writeUTF("");
            out.writeUTF("");
            out.writeUTF("");
            out.writeUTF("");
        }
        for (int i = 0; i < requests.size(); i++) {
            DataRequest dataRequest = (DataRequest) requests.elementAt(i);
            out.writeByte(dataRequest.getRequestType());
            dataRequest.writeRequestData(out);
        }
        out.flush();
    }

    private void addClientPropertiesRequest(Vector requests, DispatcherServer dispatcherServer) {
        if (dispatcherServer.canHandle(62)) {
            ClientPropertiesRequest clientProperties = new ClientPropertiesRequest(this.properties);
            if (requests.size() <= 0) {
                requests.insertElementAt(clientProperties, 0);
            } else if (((DataRequest) requests.elementAt(0)) instanceof ClientPropertiesRequest) {
                requests.setElementAt(clientProperties, 0);
            } else {
                requests.insertElementAt(clientProperties, 0);
            }
        }
    }

    public final void addSimpleRequest(int requestType, byte[] data, boolean immediate, boolean foreground) {
        addDataRequest(new SimpleDataRequest(requestType, data, immediate, foreground));
    }

    protected static boolean containsForegroundRequest(Vector requests) {
        for (int i = 0; i < requests.size(); i++) {
            if (((DataRequest) requests.elementAt(i)).isForeground()) {
                return true;
            }
        }
        return false;
    }

    protected static boolean containsSubmissionRequest(Vector requests) {
        for (int i = 0; i < requests.size(); i++) {
            if (((DataRequest) requests.elementAt(i)).isSubmission()) {
                return true;
            }
        }
        return false;
    }

    static void saveCookie(long cookie) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new DataOutputStream(baos).writeLong(cookie);
            PersistentStore store = Config.getInstance().getPersistentStore();
            store.setPreference("SessionID", baos.toByteArray());
            store.savePreferences();
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    protected String urlArguments(Vector requests) {
        StringBuffer addToUrl = new StringBuffer();
        String separator = "?";
        if (!TextUtil.isEmpty(this.globalSpecialUrlArguments)) {
            addToUrl.append(separator);
            addToUrl.append(this.globalSpecialUrlArguments);
            separator = "&";
        }
        for (int i = 0; i < requests.size(); i++) {
            DataRequest request = (DataRequest) requests.elementAt(i);
            if (request instanceof NeedsSpecialUrl) {
                String param = ((NeedsSpecialUrl) request).getParams();
                if (!TextUtil.isEmpty(param)) {
                    addToUrl.append(separator);
                    addToUrl.append(param);
                    separator = "&";
                }
            }
        }
        String addString = addToUrl.toString();
        boolean isEmpty = TextUtil.isEmpty(addString);
        return addString;
    }

    public void setAndroidMapKey(String mapKey) {
        this.properties.setString(17, mapKey);
    }

    public void setAndroidSignature(String signature) {
        this.properties.setString(18, signature);
    }

    public void setAndroidLoggingId2(String androidLoggingId2) {
        this.properties.setString(19, androidLoggingId2);
    }

    public void setApplicationName(String applicationName) {
        this.properties.setString(5, applicationName);
    }

    public void resetConnectionFactory() {
        this.connectionFactory = Config.getInstance().getConnectionFactory();
    }
}

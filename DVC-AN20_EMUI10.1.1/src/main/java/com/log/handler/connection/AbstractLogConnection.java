package com.log.handler.connection;

import com.log.handler.LogHandlerUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public abstract class AbstractLogConnection extends Observable implements ILogConnection {
    private static final String TAG = "LogHandler/AbstractLogConnection";
    private Map<String, String> mCommandAndResponsMap = new HashMap();
    protected String mServerName = "";

    @Override // com.log.handler.connection.ILogConnection
    public abstract boolean connect();

    @Override // com.log.handler.connection.ILogConnection
    public abstract boolean isConnection();

    /* access modifiers changed from: protected */
    public abstract boolean sendDataToServer(String str);

    public AbstractLogConnection(String serverName) {
        this.mServerName = serverName;
    }

    @Override // com.log.handler.connection.ILogConnection
    public synchronized boolean sendToServer(String data) {
        LogHandlerUtils.logi(TAG, "sendToServer() mServerName = " + this.mServerName + " data = " + data);
        this.mCommandAndResponsMap.put(data, "");
        if (isConnection() || connect()) {
            return sendDataToServer(data);
        }
        LogHandlerUtils.logw(TAG, "Service is not connect & re-connect failed!");
        return false;
    }

    /* access modifiers changed from: protected */
    public synchronized void setResponseFromServer(String serverData) {
        LogHandlerUtils.logi(TAG, "setResponseFromServer() mServerName = " + this.mServerName + " serverData = " + serverData);
        String commandData = serverData;
        Iterator<String> it = this.mCommandAndResponsMap.keySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            String keyCommand = it.next();
            if (serverData.startsWith(keyCommand + ",")) {
                commandData = keyCommand;
                break;
            }
        }
        this.mCommandAndResponsMap.put(commandData, serverData);
        setChanged();
        notifyObservers(serverData);
    }

    @Override // com.log.handler.connection.ILogConnection
    public synchronized String getResponseFromServer(String sendData) {
        return this.mCommandAndResponsMap.get(sendData);
    }

    @Override // com.log.handler.connection.ILogConnection
    public void addServerObserver(Observer observer) {
        addObserver(observer);
    }

    @Override // com.log.handler.connection.ILogConnection
    public void deleteServerObserver(Observer observer) {
        deleteObserver(observer);
    }

    @Override // com.log.handler.connection.ILogConnection
    public synchronized void disConnect() {
        this.mCommandAndResponsMap.clear();
    }
}

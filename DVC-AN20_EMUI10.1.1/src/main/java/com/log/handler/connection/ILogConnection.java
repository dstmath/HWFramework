package com.log.handler.connection;

import java.util.Observer;

public interface ILogConnection {
    void addServerObserver(Observer observer);

    boolean connect();

    void deleteServerObserver(Observer observer);

    void disConnect();

    String getResponseFromServer(String str);

    boolean isConnection();

    boolean sendToServer(String str);
}

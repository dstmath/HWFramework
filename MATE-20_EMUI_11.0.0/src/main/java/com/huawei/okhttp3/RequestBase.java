package com.huawei.okhttp3;

import com.huawei.okhttp3.Request;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class RequestBase {
    ArrayList<InetAddress> additionalInetAddresses = null;
    Headers headers;
    boolean isConcurrentConnectEnabled = false;

    RequestBase(BuilderBase builder) {
        if (builder != null) {
            this.isConcurrentConnectEnabled = builder.isConcurrentConnectEnabled;
            this.additionalInetAddresses = builder.additionalInetAddresses;
        }
    }

    public boolean concurrentConnectEnabled() {
        return this.isConcurrentConnectEnabled;
    }

    public ArrayList<InetAddress> additionalInetAddresses() {
        return this.additionalInetAddresses;
    }

    public boolean isCreateConnectionRequest() {
        return this.headers.get("Http2ConnectionIndex") != null;
    }

    public static class BuilderBase<T extends Request.Builder> {
        ArrayList<InetAddress> additionalInetAddresses = new ArrayList<>();
        boolean isConcurrentConnectEnabled = false;

        public BuilderBase() {
        }

        BuilderBase(RequestBase request) {
            if (request != null) {
                this.isConcurrentConnectEnabled = request.isConcurrentConnectEnabled;
                this.additionalInetAddresses = request.additionalInetAddresses;
            }
        }

        public T concurrentConnectEnabled(boolean isConcurrentConnectEnabled2) {
            this.isConcurrentConnectEnabled = isConcurrentConnectEnabled2;
            return (T) ((Request.Builder) this);
        }

        public T additionalIpAddresses(ArrayList<String> additionalIpAddresses) throws UnknownHostException {
            if (additionalIpAddresses != null) {
                Iterator<String> it = additionalIpAddresses.iterator();
                while (it.hasNext()) {
                    addIpAddress(it.next());
                }
                return (T) ((Request.Builder) this);
            }
            throw new IllegalArgumentException("additionalIpAddresses is null");
        }

        public T addIpAddress(String ipAddress) throws UnknownHostException {
            if (ipAddress != null) {
                InetAddress[] inetAddresses = InetAddress.getAllByName(ipAddress);
                if (inetAddresses != null) {
                    for (InetAddress inetAddress : inetAddresses) {
                        this.additionalInetAddresses.add(inetAddress);
                    }
                }
                return (T) ((Request.Builder) this);
            }
            throw new IllegalArgumentException("IP address is null");
        }
    }
}

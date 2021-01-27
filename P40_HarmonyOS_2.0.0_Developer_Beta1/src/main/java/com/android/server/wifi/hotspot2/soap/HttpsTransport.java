package com.android.server.wifi.hotspot2.soap;

import android.net.Network;
import java.io.IOException;
import java.net.URL;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.ServiceConnection;

public class HttpsTransport extends HttpTransportSE {
    private Network mNetwork;
    private ServiceConnection mServiceConnection;
    private URL mUrl;

    private HttpsTransport(Network network, URL url) {
        super(url.toString());
        this.mNetwork = network;
        this.mUrl = url;
    }

    public static HttpsTransport createInstance(Network network, URL url) {
        return new HttpsTransport(network, url);
    }

    @Override // org.ksoap2.transport.HttpTransportSE, org.ksoap2.transport.Transport
    public ServiceConnection getServiceConnection() throws IOException {
        if (this.mServiceConnection == null) {
            this.mServiceConnection = new HttpsServiceConnection(this.mNetwork, this.mUrl);
        }
        return this.mServiceConnection;
    }
}

package android.net.http;

public class LoggingEventHandler implements EventHandler {
    public void requestSent() {
        HttpLog.v("LoggingEventHandler:requestSent()");
    }

    @Override // android.net.http.EventHandler
    public void status(int major_version, int minor_version, int code, String reason_phrase) {
    }

    @Override // android.net.http.EventHandler
    public void headers(Headers headers) {
    }

    public void locationChanged(String newLocation, boolean permanent) {
    }

    @Override // android.net.http.EventHandler
    public void data(byte[] data, int len) {
    }

    @Override // android.net.http.EventHandler
    public void endData() {
    }

    @Override // android.net.http.EventHandler
    public void certificate(SslCertificate certificate) {
    }

    @Override // android.net.http.EventHandler
    public void error(int id, String description) {
    }

    @Override // android.net.http.EventHandler
    public boolean handleSslErrorRequest(SslError error) {
        return false;
    }
}

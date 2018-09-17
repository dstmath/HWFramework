package android.webkit;

public class WebMessage {
    private String mData;
    private WebMessagePort[] mPorts;

    public WebMessage(String data) {
        this.mData = data;
    }

    public WebMessage(String data, WebMessagePort[] ports) {
        this.mData = data;
        this.mPorts = ports;
    }

    public String getData() {
        return this.mData;
    }

    public WebMessagePort[] getPorts() {
        return this.mPorts;
    }
}

package tmsdk.bg.module.network;

public class ProfileInfo {
    public int brand = -1;
    public String carry = "";
    public int city = -1;
    public String imsi = "";
    public int province = -1;

    public String toString() {
        return "imsi:[" + this.imsi + "]province:[" + this.province + "]city:[" + this.city + "]carry:[" + this.carry + "]brand:[" + this.brand + "]";
    }
}

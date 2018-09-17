package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.d;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class fm {
    protected HashMap<String, HashMap<String, byte[]>> mu = new HashMap();
    protected HashMap<String, Object> mv = new HashMap();
    private HashMap<String, Object> mw = new HashMap();
    protected String mx = "GBK";
    JceInputStream my = new JceInputStream();

    fm() {
    }

    public void B(String encodeName) {
        this.mx = encodeName;
    }

    public void k() {
        this.mw.clear();
    }

    public <T> void put(String name, T t) {
        if (name == null) {
            throw new IllegalArgumentException("put key can not is null");
        } else if (t == null) {
            throw new IllegalArgumentException("put value can not is null");
        } else if (t instanceof Set) {
            throw new IllegalArgumentException("can not support Set");
        } else {
            JceOutputStream _out = new JceOutputStream();
            _out.setServerEncoding(this.mx);
            _out.write((Object) t, 0);
            byte[] _sBuffer = d.a(_out.getByteBuffer());
            HashMap<String, byte[]> pair = new HashMap(1);
            ArrayList<String> listType = new ArrayList(1);
            a(listType, t);
            pair.put(fk.f(listType), _sBuffer);
            this.mw.remove(name);
            this.mu.put(name, pair);
        }
    }

    private void a(ArrayList<String> listTpye, Object o) {
        if (o.getClass().isArray()) {
            if (!o.getClass().getComponentType().toString().equals("byte")) {
                throw new IllegalArgumentException("only byte[] is supported");
            } else if (Array.getLength(o) <= 0) {
                listTpye.add("Array");
                listTpye.add("?");
            } else {
                listTpye.add("java.util.List");
                a(listTpye, Array.get(o, 0));
            }
        } else if (o instanceof Array) {
            throw new IllegalArgumentException("can not support Array, please use List");
        } else if (o instanceof List) {
            listTpye.add("java.util.List");
            List list = (List) o;
            if (list.size() <= 0) {
                listTpye.add("?");
            } else {
                a(listTpye, list.get(0));
            }
        } else if (o instanceof Map) {
            listTpye.add("java.util.Map");
            Map map = (Map) o;
            if (map.size() <= 0) {
                listTpye.add("?");
                listTpye.add("?");
                return;
            }
            Object key = map.keySet().iterator().next();
            Object value = map.get(key);
            listTpye.add(key.getClass().getName());
            a(listTpye, value);
        } else {
            listTpye.add(o.getClass().getName());
        }
    }

    public byte[] l() {
        JceOutputStream _os = new JceOutputStream(0);
        _os.setServerEncoding(this.mx);
        _os.write(this.mu, 0);
        return d.a(_os.getByteBuffer());
    }

    public void b(byte[] buffer) {
        this.my.wrap(buffer);
        this.my.setServerEncoding(this.mx);
        HashMap<String, HashMap<String, byte[]>> _tempdata = new HashMap(1);
        HashMap<String, byte[]> h = new HashMap(1);
        h.put("", new byte[0]);
        _tempdata.put("", h);
        this.mu = this.my.readMap(_tempdata, 0, false);
    }
}

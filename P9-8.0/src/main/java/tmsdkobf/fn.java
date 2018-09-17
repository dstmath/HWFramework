package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.d;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class fn extends fm {
    private HashMap<String, Object> mw = new HashMap();
    JceInputStream my = new JceInputStream();
    protected HashMap<String, byte[]> mz = null;

    public void m() {
        this.mz = new HashMap();
    }

    public void k() {
        this.mw.clear();
    }

    public <T> void put(String name, T t) {
        if (this.mz == null) {
            super.put(name, t);
        } else if (name == null) {
            throw new IllegalArgumentException("put key can not is null");
        } else if (t == null) {
            throw new IllegalArgumentException("put value can not is null");
        } else if (t instanceof Set) {
            throw new IllegalArgumentException("can not support Set");
        } else {
            JceOutputStream _out = new JceOutputStream();
            _out.setServerEncoding(this.mx);
            _out.write((Object) t, 0);
            this.mz.put(name, d.a(_out.getByteBuffer()));
        }
    }

    public <T> T a(String name, T proxy) throws fl {
        Object o;
        if (this.mz == null) {
            if (!this.mu.containsKey(name)) {
                return null;
            }
            if (this.mw.containsKey(name)) {
                return this.mw.get(name);
            }
            byte[] data = new byte[0];
            Iterator it = ((HashMap) this.mu.get(name)).entrySet().iterator();
            if (it.hasNext()) {
                Entry<String, byte[]> e = (Entry) it.next();
                String className = (String) e.getKey();
                data = (byte[]) e.getValue();
            }
            try {
                this.my.wrap(data);
                this.my.setServerEncoding(this.mx);
                o = this.my.read((Object) proxy, 0, true);
                b(name, o);
                return o;
            } catch (Exception ex) {
                throw new fl(ex);
            }
        } else if (!this.mz.containsKey(name)) {
            return null;
        } else {
            if (this.mw.containsKey(name)) {
                return this.mw.get(name);
            }
            try {
                o = a((byte[]) this.mz.get(name), (Object) proxy);
                if (o != null) {
                    b(name, o);
                }
                return o;
            } catch (Exception ex2) {
                throw new fl(ex2);
            }
        }
    }

    private Object a(byte[] data, Object proxy) {
        this.my.wrap(data);
        this.my.setServerEncoding(this.mx);
        return this.my.read(proxy, 0, true);
    }

    private void b(String name, Object o) {
        this.mw.put(name, o);
    }

    public byte[] l() {
        if (this.mz == null) {
            return super.l();
        }
        JceOutputStream _os = new JceOutputStream(0);
        _os.setServerEncoding(this.mx);
        _os.write(this.mz, 0);
        return d.a(_os.getByteBuffer());
    }

    public void b(byte[] buffer) {
        try {
            super.b(buffer);
        } catch (Exception e) {
            this.my.wrap(buffer);
            this.my.setServerEncoding(this.mx);
            HashMap<String, byte[]> _tempdata = new HashMap(1);
            _tempdata.put("", new byte[0]);
            this.mz = this.my.readMap(_tempdata, 0, false);
        }
    }
}

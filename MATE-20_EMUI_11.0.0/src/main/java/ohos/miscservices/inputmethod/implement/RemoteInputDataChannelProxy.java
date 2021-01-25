package ohos.miscservices.inputmethod.implement;

import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.RichContent;
import ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel;
import ohos.multimodalinput.event.KeyEvent;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public class RemoteInputDataChannelProxy implements IRemoteInputDataChannel {
    private static final int COMMAND_CLOSE = 12;
    private static final int COMMAND_DELETE_BACKWARD = 3;
    private static final int COMMAND_DELETE_FORWARD = 4;
    private static final int COMMAND_GET_BACKWARD = 6;
    private static final int COMMAND_GET_FORWARD = 5;
    private static final int COMMAND_INSERT_RICH_CONTENT = 2;
    private static final int COMMAND_INSERT_TEXT = 1;
    private static final int COMMAND_MARK_TEXT = 7;
    private static final int COMMAND_REPLACE_MARKED_TEXT = 9;
    private static final int COMMAND_SELECT_TEXT = 15;
    private static final int COMMAND_SEND_CUSTOMIZED_DATA = 13;
    private static final int COMMAND_SEND_KEY_EVENT = 11;
    private static final int COMMAND_SEND_KEY_FUNCTION = 14;
    private static final int COMMAND_SUBSCRIBE_EDITING_TEXT = 10;
    private static final int COMMAND_UNMARK_TEXT = 8;
    private static final int ERR_OK = 0;
    private final IRemoteObject remote;

    public RemoteInputDataChannelProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public boolean insertText(String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeString(str);
        try {
            boolean z = true;
            this.remote.sendRequest(1, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public boolean insertRichContent(RichContent richContent) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        richContent.marshalling(obtain);
        try {
            this.remote.sendRequest(2, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                boolean z = true;
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public boolean deleteBackward(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeInt(i);
        try {
            this.remote.sendRequest(3, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                boolean z = true;
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public boolean deleteForward(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeInt(i);
        try {
            this.remote.sendRequest(4, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                boolean z = true;
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public String getForward(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeInt(i);
        try {
            this.remote.sendRequest(5, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                String readString = obtain2.readString();
                obtain.reclaim();
                obtain2.reclaim();
                return readString;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public String getBackward(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeInt(i);
        try {
            this.remote.sendRequest(6, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                String readString = obtain2.readString();
                obtain.reclaim();
                obtain2.reclaim();
                return readString;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public boolean markText(int i, int i2) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeInt(i);
        obtain.writeInt(i2);
        try {
            this.remote.sendRequest(7, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                boolean z = true;
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public boolean unmarkText() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            this.remote.sendRequest(8, obtain, obtain2, new MessageOption(0));
            if (obtain2.readInt() == 0) {
                boolean z = true;
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public boolean replaceMarkedText(String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeString(str);
        try {
            this.remote.sendRequest(9, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                boolean z = true;
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public EditingText subscribeEditingText(EditingCapability editingCapability) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        editingCapability.marshalling(obtain);
        try {
            this.remote.sendRequest(10, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                EditingText editingText = new EditingText();
                editingText.unmarshalling(obtain2);
                obtain.reclaim();
                obtain2.reclaim();
                return editingText;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public void close() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            this.remote.sendRequest(12, obtain, obtain2, new MessageOption(0));
            if (obtain2.readInt() == 0) {
                obtain.reclaim();
                obtain2.reclaim();
                return;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public boolean sendCustomizedData(String str, PacMap pacMap) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeString(str);
        pacMap.marshalling(obtain);
        try {
            this.remote.sendRequest(13, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                boolean z = true;
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public boolean sendKeyEvent(KeyEvent keyEvent) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        keyEvent.marshalling(obtain);
        try {
            this.remote.sendRequest(11, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                boolean z = true;
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public boolean sendKeyFunction(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeInt(i);
        try {
            this.remote.sendRequest(14, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                boolean z = true;
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel
    public boolean selectText(int i, int i2) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeInt(i);
        obtain.writeInt(i2);
        try {
            this.remote.sendRequest(15, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                boolean z = true;
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }
}

/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\develop\\workspace\\eclipse\\JiDou_Asr\\src\\com\\sogou\\speech\\wakeup\\wakeupservice\\IWakeupCallback.aidl
 */
package com.sogou.speech.wakeup.wakeupservice;

public interface IWakeupCallback extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    abstract class Stub extends android.os.Binder implements com.sogou.speech.wakeup.wakeupservice.IWakeupCallback {
        private static final java.lang.String DESCRIPTOR = "com.sogou.speech.wakeup.wakeupservice.IWakeupCallback";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an com.sogou.speech.wakeup.wakeupservice.IWakeupCallback interface,
         * generating a proxy if needed.
         */
        public static com.sogou.speech.wakeup.wakeupservice.IWakeupCallback asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof com.sogou.speech.wakeup.wakeupservice.IWakeupCallback))) {
                return ((com.sogou.speech.wakeup.wakeupservice.IWakeupCallback) iin);
            }
            return new com.sogou.speech.wakeup.wakeupservice.IWakeupCallback.Stub.Proxy(obj);
        }

        @Override
        public android.os.IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data,
                                  android.os.Parcel reply, int flags) throws android.os.RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_onResult: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    this.onResult(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_onBeginOfSpeech: {
                    data.enforceInterface(DESCRIPTOR);
                    this.onBeginOfSpeech();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_onError: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    int _arg1;
                    _arg1 = data.readInt();
                    this.onError(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements com.sogou.speech.wakeup.wakeupservice.IWakeupCallback {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            @Override
            public android.os.IBinder asBinder() {
                return mRemote;
            }

            public java.lang.String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            public void onResult(java.lang.String result) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(result);
                    mRemote.transact(Stub.TRANSACTION_onResult, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onBeginOfSpeech() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_onBeginOfSpeech, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void onError(java.lang.String errorMsg, int errorCode) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(errorMsg);
                    _data.writeInt(errorCode);
                    mRemote.transact(Stub.TRANSACTION_onError, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        static final int TRANSACTION_onResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_onBeginOfSpeech = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_onError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    }

    public void onResult(java.lang.String result) throws android.os.RemoteException;

    public void onBeginOfSpeech() throws android.os.RemoteException;

    public void onError(java.lang.String errorMsg, int errorCode) throws android.os.RemoteException;
}

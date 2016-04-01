/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\develop\\workspace\\eclipse\\JiDou_Asr\\src\\com\\sogou\\speech\\wakeup\\wakeupservice\\IWakeupService.aidl
 */
package com.sogou.speech.wakeup.wakeupservice;
public interface IWakeupService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.sogou.speech.wakeup.wakeupservice.IWakeupService
{
private static final java.lang.String DESCRIPTOR = "com.sogou.speech.wakeup.wakeupservice.IWakeupService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.sogou.speech.wakeup.wakeupservice.IWakeupService interface,
 * generating a proxy if needed.
 */
public static com.sogou.speech.wakeup.wakeupservice.IWakeupService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.sogou.speech.wakeup.wakeupservice.IWakeupService))) {
return ((com.sogou.speech.wakeup.wakeupservice.IWakeupService)iin);
}
return new com.sogou.speech.wakeup.wakeupservice.IWakeupService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_setLogLevel:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.setLogLevel(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_initWakupArd:
{
data.enforceInterface(DESCRIPTOR);
com.sogou.speech.wakeup.wakeupservice.IWakeupCallback _arg0;
_arg0 = com.sogou.speech.wakeup.wakeupservice.IWakeupCallback.Stub.asInterface(data.readStrongBinder());
this.initWakupArd(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_saveRawDataToDisk:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.saveRawDataToDisk(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_setErrorLogPath:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.setErrorLogPath(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_startListening:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<java.lang.String> _arg0;
_arg0 = data.createStringArrayList();
this.startListening(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_stopListening:
{
data.enforceInterface(DESCRIPTOR);
this.stopListening();
reply.writeNoException();
return true;
}
case TRANSACTION_destroy:
{
data.enforceInterface(DESCRIPTOR);
this.destroy();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.sogou.speech.wakeup.wakeupservice.IWakeupService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void setLogLevel(int level) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(level);
mRemote.transact(Stub.TRANSACTION_setLogLevel, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void initWakupArd(com.sogou.speech.wakeup.wakeupservice.IWakeupCallback callBack) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_initWakupArd, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void saveRawDataToDisk(java.lang.String filePath, java.lang.String word) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(filePath);
_data.writeString(word);
mRemote.transact(Stub.TRANSACTION_saveRawDataToDisk, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setErrorLogPath(java.lang.String path) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(path);
mRemote.transact(Stub.TRANSACTION_setErrorLogPath, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void startListening(java.util.List<java.lang.String> words) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStringList(words);
mRemote.transact(Stub.TRANSACTION_startListening, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void stopListening() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stopListening, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void destroy() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_destroy, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_setLogLevel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_initWakupArd = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_saveRawDataToDisk = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_setErrorLogPath = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_startListening = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_stopListening = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_destroy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
}
public void setLogLevel(int level) throws android.os.RemoteException;
public void initWakupArd(com.sogou.speech.wakeup.wakeupservice.IWakeupCallback callBack) throws android.os.RemoteException;
public void saveRawDataToDisk(java.lang.String filePath, java.lang.String word) throws android.os.RemoteException;
public void setErrorLogPath(java.lang.String path) throws android.os.RemoteException;
public void startListening(java.util.List<java.lang.String> words) throws android.os.RemoteException;
public void stopListening() throws android.os.RemoteException;
public void destroy() throws android.os.RemoteException;
}

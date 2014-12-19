
package com.eagle.socket;

import android.app.Activity;
import android.content.Context;
import android.database.DatabaseUtils;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.eagle.socket.DataObject.Type;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;

public class SocketDemo extends Activity
{
    private static final String TAG = SocketDemo.class.getSimpleName();

    private static final String SOCKET_PATH = "eagel_serial";

    private Context mContext;
    private EditText mClientEditText;
    private EditText mServerEditText;
    private volatile LocalServerSocket mLocalServerSocket;
    private volatile InputStream mServerInputStream;
    private volatile OutputStream mServerOutputStream;
    private volatile InputStream mClientInputStream;
    private volatile OutputStream mClientOutputStream;
    private volatile LocalSocket mClientSocket;
    private char[] mClientBuf = new char[1024];
    private char[] mServerBuf = new char[1024];
    private CustomAdapter mClientAdapter;
    private CustomAdapter mServerAdapter;
    private String mClient;
    private String mServer;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mContext = this;
        mClient = this.getString(R.string.client);
        mServer = this.getString(R.string.server);
        initViews();
        startSocketService();
    }

    private void initViews() {
        mClientEditText = (EditText) findViewById(R.id.client_input);
        mServerEditText = (EditText) findViewById(R.id.server_input);
        ListView clientlist = (ListView) findViewById(R.id.client_list);
        mClientAdapter = new CustomAdapter(mContext);
        clientlist.setAdapter(mClientAdapter);
        ListView serverList = (ListView) findViewById(R.id.server_list);
        mServerAdapter = new CustomAdapter(mContext);
        serverList.setAdapter(mServerAdapter);
    }

    private void startSocketService() {
        new ServerThread().start();
        new ClientThread().start();
    }

    private void stopSocketService() {
        logD(TAG, "socket disconnected!");
        disconnect();
        closeLockSocketService();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onClientClear(View view) {
        mClientAdapter.clearAll();
    }

    public void onServerClear(View view) {
        mServerAdapter.clearAll();
    }

    public void onClientSend(View view) {
        CharSequence data = mClientEditText.getText();
        if (TextUtils.isEmpty(data)) {
            return;
        }
        clientSendData(data.toString().getBytes());
    }

    public void onServerSend(View view) {
        CharSequence data = mServerEditText.getText();
        if (TextUtils.isEmpty(data)) {
            return;
        }
        serverSendData(data.toString().getBytes());
    }

    @Override
    protected void onDestroy() {
        stopSocketService();
        super.onDestroy();
    }

    private void logD(String tag, String msg) {
        Log.d(tag, msg);
    }

    private static final int EVENT_CLINENT_SEND_DATA = 1;
    private static final int EVENT_CLIENT_RECEIVED_DATA = 2;
    private static final int EVENT_SERVER_SEND_DATA = 3;
    private static final int EVENT_SERVER_RECEIVED_DATA = 4;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_CLINENT_SEND_DATA:
                    if (msg.obj != null) {
                        doClientSendData((byte[]) msg.obj);
                    }
                    break;
                case EVENT_CLIENT_RECEIVED_DATA:
                    if (msg.obj != null) {
                        doClientReceivedData((char[]) msg.obj);
                    }
                    break;
                case EVENT_SERVER_SEND_DATA:
                    if (msg.obj != null) {
                        doServerSendData((byte[]) msg.obj);
                    }
                    break;
                case EVENT_SERVER_RECEIVED_DATA:
                    if (msg.obj != null) {
                        doServerReceivedData((char[]) msg.obj);
                    }
                    break;
            }
        }
    };

    private void doClientSendData(byte[] data) {
        mClientAdapter.add(new DataObject(Type.LOCAL, mClient, new String(data)));
    }

    private void doClientReceivedData(char[] data) {
        mClientAdapter.add(new DataObject(Type.OTHER, mServer, new String(data)));
    }

    private void doServerSendData(byte[] data) {
        mServerAdapter.add(new DataObject(Type.LOCAL, mServer, new String(data)));
    }

    private void doServerReceivedData(char[] data) {
        mServerAdapter.add(new DataObject(Type.OTHER, mClient, new String(data)));
    }

    // Client******************SocketLocal****************
    private class ClientThread extends Thread {
        @Override
        public void run() {
            try {
                mClientSocket = new LocalSocket();
                LocalSocketAddress address = new LocalSocketAddress(SOCKET_PATH);
                mClientSocket.connect(address);
                mClientInputStream = mClientSocket.getInputStream();
                mClientOutputStream = mClientSocket.getOutputStream();
                InputStreamReader isr = new InputStreamReader(mClientInputStream);
                for (;;) {
                    int count = isr.read(mClientBuf);
                    if (count > 0) {
                        char data[] = new char[count];
                        System.arraycopy(mClientBuf, 0, data, 0, count);
                        mHandler.obtainMessage(EVENT_CLIENT_RECEIVED_DATA, data).sendToTarget();
                    }
                }
            } catch (IOException e) {
                disconnect();
            }
        }
    }

    private void disconnect() {
        try {
            if (mClientSocket != null) {
                mClientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mClientSocket = null;
        }
        try {
            if (mClientInputStream != null) {
                mClientInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mClientInputStream = null;
        }
        try {
            if (mClientOutputStream != null) {
                mClientOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mClientOutputStream = null;
        }
    }

    /*
     * client send message to server
     */
    private void clientSendData(byte[] data) {
        if (mClientOutputStream != null) {
            try {
                mClientOutputStream.write(data);
                mClientOutputStream.flush();
                mHandler.obtainMessage(EVENT_CLINENT_SEND_DATA, data).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // server******************SocketLocalService****************
    private class ServerThread extends Thread {

        @Override
        public void run() {
            try {
                mLocalServerSocket = new LocalServerSocket(SOCKET_PATH);
                for (;;) {
                    LocalSocket socket = mLocalServerSocket.accept();
                    if (socket == null) {
                        continue;
                    } else {
                        logD(TAG, "local socket connted local socket service successful!");
                        mServerInputStream = socket.getInputStream();
                        mServerOutputStream = socket.getOutputStream();
                        InputStreamReader isr = new InputStreamReader(mServerInputStream);
                        for (;;) {
                            int count = isr.read(mServerBuf);
                            if (count > 0) {
                                char[] data = Arrays.copyOf(mServerBuf, count);
                                mHandler.obtainMessage(EVENT_SERVER_RECEIVED_DATA, data)
                                        .sendToTarget();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                closeLockSocketService();
            }
        }
    }

    /*
     * server send message to client
     */
    private void serverSendData(byte[] data) {
        if (mServerOutputStream != null) {
            try {
                mServerOutputStream.write(data);
                mServerOutputStream.flush();
                mHandler.obtainMessage(EVENT_SERVER_SEND_DATA, data).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeLockSocketService() {
        try {
            if (mLocalServerSocket != null) {
                mLocalServerSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mLocalServerSocket = null;
        }
        try {
            if (mServerInputStream != null) {
                mServerInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mServerInputStream = null;
        }
        try {
            if (mServerOutputStream != null) {
                mServerOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mServerOutputStream = null;
        }
    }
}

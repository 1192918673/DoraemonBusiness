package com.geeknewbee.doraemon.input.bluetooth;

import android.os.Handler;
import android.util.Log;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class SocketService {

    public static final int PORT = 9000;
    public static final String TAG = SocketService.class.getSimpleName();
    private final Handler mHandler;
    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;
    private SocketReader socketReader;

    public SocketService(Handler mHandler) {
        this.mHandler = mHandler;
        socketReader = new SocketReader();
    }

    public void start() {
        stop();
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    public void stop() {
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread.interrupt();
            acceptThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread.interrupt();
            connectedThread = null;
        }
    }

    public boolean write(String data) {
        if (connectedThread != null) {
            connectedThread.write(data.getBytes());
            return true;
        } else return false;
    }

    private class AcceptThread extends Thread {
        // The local server socket
        ServerSocket serverSocket = null;

        public AcceptThread() {
            // Create a new listening server socket
            try {
                serverSocket = new ServerSocket(PORT);
                serverSocket.setReuseAddress(true);
            } catch (IOException e) {
                Log.e(TAG, "Socket listen() failed", e);
            }
        }

        public void run() {
            LogUtils.d(TAG, "Socket BEGIN mAcceptThread" + this);

            while (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    Socket client = serverSocket.accept();
                    connectClient(client);
                } catch (Exception e) {
                    Log.e(TAG, "Socket accept() failed", e);
                    break;
                }
            }
            Log.i(TAG, "END mAcceptThread, socket");
        }

        public void cancel() {
            LogUtils.d(TAG, "Socket cancel " + this);
            try {
                if (serverSocket != null)
                    serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket close() of server failed", e);
            }
        }
    }

    private void connectClient(Socket client) {
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        socketReader.clearData();
        connectedThread = new ConnectedThread(client);
        connectedThread.start();
    }

    private class ConnectedThread extends Thread {
        private final Socket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean isExit;

        public ConnectedThread(Socket socket) {
            LogUtils.d(TAG, "create ConnectedThread: ");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024 * 20];
            int bytes;

            while (!isExit) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if (bytes == -1) {
                        Log.e(TAG, "disconnected");
                        cancel();//断开连接
                        break;
                    }
                    byte[] result = socketReader.readData2(Arrays.copyOfRange(buffer, 0, bytes));
                    if (result != null)
                        mHandler.obtainMessage(Constants.MESSAGE_SOCKET_CONTROL, result.length, -1, result)
                                .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    cancel();//断开连接
                    break;
                }
            }
            Log.i(TAG, "END mConnectedThread");
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            isExit = true;
            try {
                mmInStream.close();
                mmOutStream.close();
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

}

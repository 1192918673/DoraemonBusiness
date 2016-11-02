package com.geeknewbee.doraemon.input.bluetooth;

import android.os.Handler;
import android.util.Log;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemonsdk.utils.BytesUtils;
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
    public static final int DATA_LENGTH = 4;
    private final Handler mHandler;
    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;

    public SocketService(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void start() {
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
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
            byte[] buffer;
            //读取到的bytes length
            int resultBytes = 0;
            //需要读取的bytes length
            int readBytes = 0;
            //每次读取的最大长度
            int maxBufferSize = 1024;
            //是否是读取数据包的开头包
            boolean isFirstPackage = true;
            //是否是读取数据的最后的一个包
            //读取数据的需要的总次数
            int readDataCount = 1;
            //当前读取数据的次数
            int currentReadCount = 0;
            //数据长度
            int dataLength = 0;
            //读取到的数据
            byte[] data = null;
            //每次实际读取的个数
            int read = 0;

            int prefixLength = Constants.COMMAND_ROBOT_PREFIX_FOR_SOCKET.length();
            int suffixLength = Constants.COMMAND_ROBOT_SUFFIX_FOR_SOCKET.length();

            while (!isExit) {
                try {
                    if (isFirstPackage) {
                        LogUtils.d(TAG, "isFirstPackage");
                        //读取数据包的开头标示和数据长度的byte[]
                        resultBytes = 0;
                        readBytes = prefixLength + DATA_LENGTH;
                        buffer = new byte[readBytes];
                        while (resultBytes < readBytes) {
                            read = mmInStream.read(buffer, resultBytes, readBytes - resultBytes);
                            if (read == -1) {
                                Log.e(TAG, "disconnected");
                                cancel();//断开连接
                                return;
                            }
                            resultBytes += read;
                        }

                        String prefixDataStr = new String(buffer, 0, prefixLength);
                        if (!prefixDataStr.equals(Constants.COMMAND_ROBOT_PREFIX_FOR_SOCKET))
                            //不满足通讯规则直接抛掉 (没有头)
                            continue;

                        //计算数据的长度
                        dataLength = BytesUtils.bytes2int(Arrays.copyOfRange(buffer, prefixLength, readBytes));
                        readBytes = dataLength + suffixLength;//设置下次要读取的数据长度
                        isFirstPackage = false;
                        if (readBytes > maxBufferSize) {
                            //如果需要读取的数据长度大于最大的buffer size，需要分次读取
                            readDataCount = (readBytes % maxBufferSize == 0 ? readBytes / maxBufferSize : (readBytes / maxBufferSize + 1));
                        }
                    } else if (currentReadCount == readDataCount - 1) {
                        resultBytes = 0;
                        //最后一次读取数据域
                        readBytes = (dataLength + suffixLength) - maxBufferSize * (currentReadCount);
                        buffer = new byte[readBytes];
                        while (resultBytes < readBytes) {
                            read = mmInStream.read(buffer, resultBytes, readBytes - resultBytes);
                            if (read == -1) {
                                Log.e(TAG, "disconnected");
                                cancel();//断开连接
                                return;
                            }
                            resultBytes += read;
                        }

                        String suffixDataStr = new String(buffer, readBytes - suffixLength, suffixLength);
                        if (!suffixDataStr.equals(Constants.COMMAND_ROBOT_SUFFIX_FOR_SOCKET)) {
                            //不满足通讯规则直接抛掉 (没有尾)
                            isFirstPackage = true;
                            dataLength = 0;
                            currentReadCount = 0;
                            readDataCount = 1;
                            data = null;
                            continue;
                        }

                        //获取中间的数据
                        byte[] result = Arrays.copyOfRange(buffer, 0, readBytes - suffixLength);
                        data = BytesUtils.concat(data, result);
                        mHandler.obtainMessage(Constants.MESSAGE_SOCKET_CONTROL, data.length, -1, data)
                                .sendToTarget();
                        isFirstPackage = true;
                        dataLength = 0;
                        currentReadCount = 0;
                        readDataCount = 1;
                        data = null;
                    } else {
                        //读取中间的数据
                        resultBytes = 0;
                        readBytes = maxBufferSize;
                        buffer = new byte[readBytes];
                        while (resultBytes < readBytes) {
                            read = mmInStream.read(buffer, resultBytes, readBytes - resultBytes);
                            if (read == -1) {
                                Log.e(TAG, "disconnected");
                                cancel();//断开连接
                                return;
                            }
                            resultBytes += read;
                        }

                        currentReadCount++;
                        data = BytesUtils.concat(data, buffer);
                    }
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

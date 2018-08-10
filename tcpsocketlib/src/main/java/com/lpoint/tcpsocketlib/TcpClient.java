package com.lpoint.tcpsocketlib;

import android.util.Log;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 季白
 *         create time 2018/8/10.
 */

public class TcpClient {
    public static final String TAG = "TcpClient";
    /**
     * 连接的服务器IP地址
     */
    private String serverIP = "";
    /**
     * 连接的socket服务端口
     */
    private int serverPort = -1;
    private PrintWriter pw;
    private InputStream is;
    private DataInputStream dis;
    private boolean isRun = false;
    private byte buff[] = new byte[4096];
    private String rcvMsg;
    private Socket socket;
    private int rcvLen;
    /**
     * socket的timeout时间，默认为0
     */
    private int soTimeOut = 0;
    private Runnable msgRun;
    private Runnable connRun;
    private Runnable sendMsgRun;
    /**
     * socket接受消息的编码格式，默认"utf-8"
     */
    private String charsetName = "utf-8";
    /**
     * TCP连接的状态监听
     */
    private TcpSocketListener tcpSocketListener;
    private int numberOfCores = Runtime.getRuntime().availableProcessors();
    /**
     * 额外线程空状态生存时间
     */
    private int keepAliveTime = 1;
    private BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
    private boolean needReConn = false;
    private int reConnTime = 10;
    ExecutorService executorService = new ThreadPoolExecutor(numberOfCores,
            numberOfCores * 2, keepAliveTime, TimeUnit.SECONDS, taskQueue);

    public TcpClient(String serverIp, int port) {
        this.serverIP = serverIp;
        this.serverPort = port;
        tcpSocketListener = new TcpSocketListener() {
            @Override
            public void onConnException(Exception e) {
                Log.e(TAG, e.toString());
            }

            @Override
            public void onMessage(String s) {
                Log.i(TAG, s);
            }

            @Override
            public void onListenerException(Exception e) {
                Log.e(TAG, e.toString());
            }

            @Override
            public void onSendMsgSuccess(String msg) {

            }

            @Override
            public void onSendMsgException(Exception e) {
                Log.e(TAG, e.toString());
            }

            @Override
            public void onCloseException(Exception e) {
                Log.e(TAG, e.toString());
            }
        };
    }

    /**
     * 设置连接的socket服务器IP
     * @param serverIP
     */
    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    /**
     * 设置连接的socket服务端口
     * @param serverPort
     */

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * 设置soTimeOut
     * @param soTimeOut
     */
    public void setSoTimeOut(int soTimeOut) {
        this.soTimeOut = soTimeOut;
    }

    /**
     * 设置socket接收字符的格式，默认"utf-8"
     * @param charsetName
     */
    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    /**
     * 设置是否需要断开后重新连接
     * @param needReConn
     */
    public void setNeedReConn(boolean needReConn) {
        this.needReConn = needReConn;
    }

    /**
     * 设置socket断开后重新连接间隔时间
     *
     * @param reConnTime 重连间隔时间 单位 秒
     */
    public void setReConnTime(int reConnTime) {
        this.reConnTime = reConnTime;
    }

    /**
     * 设置socket的各种状态的回掉
     * @param tcpSocketListener
     */
    public void setTcpSocketListener(TcpSocketListener tcpSocketListener) {
        this.tcpSocketListener = tcpSocketListener;
    }

    public void startConn() {
        connRun = new Runnable() {
            @Override
            public void run() {
                if (isRun) {
                    tcpSocketListener.onConnException(new Exception("已经有一个socket连接了"));
                    return;
                }
                try {
                    socket = new Socket(serverIP, serverPort);
                    socket.setSoTimeout(soTimeOut);
                    pw = new PrintWriter(socket.getOutputStream(), true);
                    is = socket.getInputStream();
                    dis = new DataInputStream(is);
                    startListen();
                } catch (Exception e) {
                    tcpSocketListener.onConnException(e);
                    closeTcpSocket();
                }
            }
        };
        executorService.execute(connRun);
    }

    private void startListen() {
        if (isRun) {
            tcpSocketListener.onListenerException(new Exception("当前消息监听尚未停止，无法执行startListen()"));
            return;
        }
        if (socket == null || pw == null || is == null) {
            tcpSocketListener.onListenerException(new Exception("socket初始化失败，无法执行startListen()"));
            return;
        }
        isRun = true;
        msgRun = new Runnable() {
            @Override
            public void run() {
                while (isRun) {
                    try {
                        rcvLen = dis.read(buff);
                        rcvMsg = new String(buff, 0, rcvLen, charsetName);
                        tcpSocketListener.onMessage(rcvMsg);
                    } catch (Exception e) {
                        tcpSocketListener.onListenerException(e);
                        closeTcpSocket();
                    }
                }
            }
        };
        executorService.execute(msgRun);
    }

    public void sendMsg(final String msg) {
        sendMsgRun = new Runnable() {
            @Override
            public void run() {
                try {
                    pw.println(msg);
                    pw.flush();
                    tcpSocketListener.onSendMsgSuccess(msg);
                } catch (Exception e) {
                    tcpSocketListener.onSendMsgException(e);
                }
            }
        };
        executorService.execute(sendMsgRun);
    }

    public void closeTcpSocket() {
        try {
            isRun = false;
            pw.close();
            is.close();
            dis.close();
            socket.close();
        } catch (Exception e) {
            tcpSocketListener.onCloseException(e);
        } finally {
            if (needReConn && !isRun) {
                try {
                    Thread.sleep(reConnTime * 1000);
                    startConn();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }
}

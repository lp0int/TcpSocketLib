package com.lpoint.tcpsocketlib;

/**
 * @author 季白
 *         create time 2018/8/10.
 */

public interface TcpSocketListener {
    /**
     * 发起TCP连接时报出的异常
     *
     * @param e
     */
    void onConnException(Exception e);

    /**
     * 当TCP通道收到消息时执行此回调
     * 需要注意此回掉会在异步线程中执行，如果需要更新UI则需要runOnUiThread
     *
     * @param s
     */
    void onMessage(String s);

    /**
     * 当TCP消息监听时遇到异常，从这里抛出
     *
     * @param e
     */
    void onListenerException(Exception e);

    /**
     * 当sendMsg()方法成功执行完毕后，执行此方法
     * @param s
     */
    void onSendMsgSuccess(String s);

    /**
     * 发送消息时遇到异常，从这里抛出
     * @param e
     */
    void onSendMsgException(Exception e);

    /**
     * 当TCP连接断开时遇到异常，从这里抛出
     *
     * @param e
     */
    void onCloseException(Exception e);
}
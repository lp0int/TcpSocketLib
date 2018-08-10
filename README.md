#TcpSocketLib

在Android开发中，经常会需要由服务端主动推送一些消息给客户端，最常见的做法就是用Tcp Socket保持长链接。为了简化这个过程，做了一个简单的依赖库来实现TCP Socket的client功能。

# Doc
## 引入依赖
Step 1.先配置你的project的build.gradle

```
allprojects {
    repositories {
        ···
        maven { url 'https://jitpack.io' }
        ···
    }
}
```
Step 2.为你的项目添加依赖
```
dependencies {
    ···
    implementation 'com.github.lp0int:TcpSocketLib:1.0'
    ···
}
```
## 开始使用
```
    TcpClient tcpClient = new TcpClient(serverIp, serverPort);
    tcpClient.startConn();
```
以上代码就是创建一个TCP连接最简单的代码了，创建一个TcpClient的实例，然后startConn()，就完成啦最简单的Tcp连接啦。Of course,这是不够的。所以提供了一些方法来实现更丰满的功能。

## API
### 监听Socket的各个状态以及消息的接受
这里提供了一个简单的listener类，来负责监听Tcp的各个状态
```
    TcpSocketListener tcpSocketListener = new TcpSocketListener() {
        /**
         * 发起TCP连接时报出的异常
         */
        @Override
        public void onConnException(Exception e) {}

        /**
         * 当TCP通道收到消息时执行此回调
         */
        @Override
        public void onMessage(String s) {}

        /**
         * 当TCP消息监听时遇到异常，从这里抛出
         */
        @Override
        public void onListenerException(Exception e) {}

        /**
         * 当sendMsg()方法成功执行完毕后，执行此方法
         */
        @Override
        public void onSendMsgSuccess(String s) {}

        /**
         * 发送消息时遇到异常，从这里抛出
         */
        @Override
        public void onSendMsgException(Exception e) {}

        /**
         * 当TCP连接断开时遇到异常，从这里抛出
         */
        @Override
        public void onCloseException(Exception e) {}
    }; 
    tcpClient.setTcpSocketListener(tcpSocketListener)
```
就是这样，通过setTcpSocketListener()方法，添加一个监听，就可以很方便的完成socket通讯了。**要注意的是，这些方法是异步执行的，可能会引发线程问题需要自己去处理**

### 发送消息
```
tcpClient.sendMsg(final String msg)
```

### 关闭连接
```
tcpClient.closeTcpSocket()
```

这里提供了链接断开（不管异常还是主动）后，自动重连的方法
### 设置是否需要断开后重新连接
```
    tcpClient.setNeedReConn(boolean needReConn)
```

### 设置socket断开后重新连接间隔时间 单位 秒
```
    tcpClient.setReConnTime(int reConnTime)
```

### 设置socket接收字符的格式，默认"utf-8"，非特殊需要不用修改
```
    tcpClient.setCharsetName(String charsetName)
```

### 设置soTimeOut,默认为0，不建议修改
```
    tcpClient.setSoTimeOut(int soTimeOut)
```

### 设置连接的socket服务器IP
```
    tcpClient.setServerIP(String serverIP)
```

### 设置连接的socket服务端口
```
    tcpClient.setServerPort(int serverPort)
```


>gayhub地址：https://github.com/lp0int/TcpSocketLib </br>自用lib，欢迎提交Issues和star

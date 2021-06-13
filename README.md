# MyRpcDemo
一步一步学习如何自己写一个RPC框架

本项目参考来源：[参考项目](https://github.com/CN-GuoZiyang/My-RPC-Framework)  

### 版本V1.0
思路：
1. 用socket进行进行通信
2. 采用jdk原生序列化
3. 客户端用基于接口的代理模式
目录介绍：
- rpc-api:定义的接口

说明：因为客户端采用基于接口的代理模式；客户端只能拿到某个接口的代理类，然后你通过调用某个代理Service.fun()，其实它会被拦截到invoke()方法中，然后我就知道你的fun()是哪个方法，拿到这个方法的method对象，以及method对象里面的各个参数对象，和参数对象的类型，以此构造rpcRequest，你看，通过这种代理模式，就不需要自己手动去构建rcpReqesut对象，不然的话你调用不同的服务还得自己手动传参构造rpcRequest；然后具体的传输rpcRequest由RpcClient来做。
- rpc-common: 客户端和服务器端都用到的RpcRequest, RpcResponse实体类、枚举类
- rpc-core: 框架的核心代码；
包括RpcClient和RpcServer类，也就是你会用这个框架中类
- rpc-client: 模拟客户端发起rpc请求
- rpc-server: 模拟服务器端，也就是在某个端口注册一个服务而已

关于代理模式的一些介绍：[代理模式](https://winterliu1020.github.io/winterliu-notes/1-Java%20%E5%9F%BA%E7%A1%80/Java%E4%B8%AD%E7%9A%84%E4%BB%A3%E7%90%86%E7%B1%BBProxy%E5%92%8CInvocationHandler.html)

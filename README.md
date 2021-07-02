# MyRpcDemo
一步一步学习如何自己写一个RPC框架

本项目参考来源：[参考项目](https://github.com/CN-GuoZiyang/My-RPC-Framework)  

### 版本v1.0
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

### 版本v1.1
1. 框架中增加ServiceRegister模块
2. 首先测试服务器端需要将服务添加到register注册表，然后服务端绑定这个register对象
3. 然后服务器端指定端口开启服务；当服务器端接收到一个socket，就把它放到RequestHandlerThread线程池
4. RequestHandlerThread(socket, handler, register)
5. RequestHandler.handler(rpcRequest, service)

### 版本v2.0
1. 用Netty进行客户端和服务器端通信
2. 把RpcServer、RpcClient抽象成接口，原来的就是socket实现：SocketServer, RpcClient；现在加入Netty实现
3. 对RpcClientProxy进行抽象，原来的RpcClientProxy默认用的是socketClient，现在也把不同的client传入RpcClientProxy的构造函数，为不同的client实现代理。
4. 之前用的是原生序列化，这里实现通用序列化接口，可以实现不同的序列化，首次加入JSON序列化，但是注意反序列化的时候rpcRequest中Object类型数组会反序列化失败，原因在于JSON序列化只是简单保存字符串，会丢失类型信息，所以用JSON序列化的时候需要利用rpcRequest中参数类型数组辅助反序列化。
5. 把DefaultServiceRegister类中两个存储注册服务的属性改成static类型，这样就可以让注册表全局只有一份；也就不用让RpcServer与某一个具体的serviceRegister对象进行绑定

这一版本中主要加入netty进行底层通信，一些知识点：
1. AttributeMap<AttributeKey, Attribute>，每一个channelHandlerContext以及channel都各自绑定了自己的一个AttributeMap，各个channelHandlerContext不能访问到其它channelHandlerContext的AttributeMap，但是所有的channelHandlerContext都能访问到所在channel的AttributeMap。

### 版本v3.0
版本简介：这个版本中引入了注册中心，这里使用Nacos，同时增加了Kryo序列化方式。  

目录介绍：  

rpc-common模块：
- Entity 
- Enumeration  
- Exception
- uitl

rpc-core模块：
- codec 用于netty中编解码
- handler 所有通信方式公用的类，调用handler(rpcRequest)来反射执行，并返回结果
- registry 远程注册中心
- provider 本地注册表
- serializer 序列化方式
- transport 通信方式。

#### 关于为什么要引入远程注册中心Nacos?
在版本2中都是将服务实现类直接放到ServiceProvider对象中，ServiceProvider是放在服务器端的，相当于服务器端本地注册表，ServiceProvider中两个属性：  
``` java
private static final ConcurrentHashMap<String, Object> serviceMap = new ConcurrentHashMap<>(); // 接口名，service；因为同一个service可能实现了很多接口（多个接口可能注册到同一个服务实现类上）
private static final Set<String> registerService = ConcurrentHashMap.newKeySet(); // 放所有服务实现类的名字；一个线程安全的set；只是起到一个防止重复添加同一个服务实现类
```
通过这个ServiceProvider类我做到了将**接口注册到服务**上，这样我之后就可以通过接口名获取到具体的服务实现类，但是这就让我们局限在一个客户端只能请求一个服务器端上的服务，如果这个服务器端上这个请求的服务挂了，那么客户端就无法获取远程过程调用的结果；  

所以引入注册中心的概念，服务器端如果要发布服务，都需要将（key:接口名，value:接口对应的这项服务发布的host,port）注册到远程注册中心，然后客户端则通过接口名这个key去获取服务所在的host, port；那这样我就可以提供同一个服务的多个(host, port)注册到远程注册中心，那客户端通过接口名去获取服务所在的地址时，在注册中心还可以设置**负载均衡**策略，返回对应该key的所有服务地址中负载最低的那一个。  

注意点：远程注册中心只存储了<**接口名**， inetSocketAddress>; 具体的服务实现实例还是存在服务器端上的ServiceProvider对象中serviceMap属性。  

以上就是为什么要引入远程注册中心。

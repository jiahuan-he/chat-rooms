# chat-room
## 1. Introduction
Multiple user chat room implemented using different languages and middlewares
#### Golang: [Socket](/Go-socket-chat-room), [RPC](/Go-RPC-chat-room)
#### Java: [Socket](/Java-socket-chat-room), [RMI](/Java-RMI-chat-room), [CORBA](/Java-CORBA-chat-room/src)
#### Node.js: [RESTful](/Node-REST-chat-room)

## 2. Interface (same for all implementations)
- **/list** list all chatrooms
- **/create [room names]...** create one or more chat rooms
- **/join [room names]...** join one or more chat rooms (you can receive messages from joined rooms)
- **/switch [room name]** switch to one chat room (you can speak only in your current room)
- **/leave [room names]...** leave one or more chat rooms


## Demo(Golang implementation)

1. connect new user
## <img src="/GIFs/1-connect.gif" >

2. create rooms
## <img src="/GIFs/2-create-room.gif" >

3. join room
## <img src="/GIFs/3-join-r1.gif">
## <img src="/GIFs/4-join-r1.gif">

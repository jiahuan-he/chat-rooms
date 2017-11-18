package main

import (
	"net/rpc"
	"net"
	"log"
	"net/http"
	"errors"
	"fmt"
	"../shared"
	"strings"
	"sort"
)

//////////////////////// Server
type Server struct{
	chatRooms map[string]*ChatRoom
	clients map[string]*Client
}

func NewServer() *Server{

	chatRooms := map[string]*ChatRoom{}
	clients := map[string]*Client{}
	s := &Server{chatRooms:chatRooms, clients:clients}
	defaultName := "default-room"
	defaultRoom := NewChatRoom(defaultName)
	chatRooms[defaultName] = defaultRoom
	return s
}

func (server *Server) _addUser(client string)  {
	newClient := NewClient(client)
	server.clients[client] = newClient
}

func (server *Server) _joinUserToRoom(client string, room string){
	c := server.clients[client]
	r := server.chatRooms[room]

	r.clients[client] = c
	c.joinedRooms[room] = r
}

func (server *Server) _switchUserToRoom(client string, room string){
	c := server.clients[client]
	r := server.chatRooms[room]
	c.currentRoom = r
}

func (server *Server) SwitchRoom(args shared.SwitchRoom, room *string) error{
	c := server.clients[args.Client]
	if _, ok := server.chatRooms[args.Room]; ok{
		if rJoined, ok := c.joinedRooms[args.Room] ;ok {
			c.currentRoom = rJoined
			c._appendMessage("System => Success: You have switched to room "+ args.Room);
		} else {
			c._appendMessage("System => Error: You have to join "+ args.Room+" first");
			return errors.New("You've not joined room "+args.Room)
		}
	} else {
		c._appendMessage("System => Error: Room "+ args.Room +" doesn't exist");
		return errors.New("The room" +args.Room+" doesn't exist")
	}
	return nil
}

func (server *Server) CreateRoom (arg shared.CreateRoom, roomName *string) error{
	if _, ok := server.chatRooms[arg.Room]; ok{
		return errors.New("The room" +arg.Room+" already exists")
	} else {
		newRoom := NewChatRoom(arg.Room)
		server.chatRooms[arg.Room] = newRoom
		*roomName = arg.Room
		c := server.clients[arg.Client]
		c._appendMessage("SYSTEM => Success: Created room: " + arg.Room)
	}
	return nil
}


func (server *Server) JoinRoom (args shared.JoinRoom, roomName *string) error{
	c := server.clients[args.Client]
	if room, ok := server.chatRooms[args.Room]; ok{
		if _, ok := c.joinedRooms[args.Room] ;ok {
			return errors.New("You've already joined room "+args.Room)
		} else {
			c.joinedRooms[args.Room] = room
			room.clients[args.Client] = c
			c._appendMessage("SYSTEM => Success: joined room: " + args.Room)
			*roomName = args.Room
		}
	} else {
		return errors.New("Room "+args.Room+" doesn't exist")
	}
	return nil
}


func (server *Server) LeaveRoom (args shared.LeaveRoom, roomName *string) error{
	c := server.clients[args.Client]
	if room, ok := server.chatRooms[args.Room]; ok{
		if _, ok := c.joinedRooms[args.Room] ;ok {
			delete(c.joinedRooms, args.Room)
			delete(room.clients, args.Client)
			c._appendMessage("SYSTEM => Success: Left room: " + args.Room)
			*roomName = args.Room
			if c.currentRoom.roomName == args.Room {
				c._appendMessage("SYSTEM => Note: You just left the current room, switching to default room ... ")
				var temp  *string
				server.SwitchRoom(shared.SwitchRoom{Room:"default-room", Client:c.clientName}, temp)
			}


		} else {
			return errors.New("You've not joined room "+args.Room)
		}
	} else {
		return errors.New("Room "+args.Room+" doesn't exist")
	}
	return nil
}



func (server *Server) Connect(client string, isSuccessful *bool) error{
	if server.clients[client] == nil{
		defer fmt.Println("New connection: User: "+ client)
		defaultRoom := "default-room"
		server._addUser(client)
		server._joinUserToRoom(client, defaultRoom)
		server._switchUserToRoom(client, defaultRoom)
		for _, c := range server.chatRooms[defaultRoom].clients{
			c._appendMessage("SYSTEM => Welcome new user: "+client+" join default-room")
		}
		*isSuccessful = true
		return nil;
	} else {
		return errors.New("Name "+client+" is already taken")
	}
}

func (server *Server) Speak(arg shared.Message, messageBack *string) error{
	if strings.TrimSpace(strings.TrimRight(arg.Message, "\n")) == "" {
		return nil
	}
	client:= server.clients[arg.Client]
	room := client.currentRoom
	m := "("+room.roomName+") "+client.clientName+" => "+arg.Message
	for _, c := range room.clients{
		c._appendMessage(m)
		*messageBack = arg.Message
	}
	return nil
}

func (server *Server) Retrieve(client string, mq *[]string) error  {

	if server.clients[client].messageQueue == nil {
		mq = nil
	} else {
		for _, str := range *server.clients[client].messageQueue{
			*mq = append((*mq), str)
		}
		server.clients[client].messageQueue = nil
		server.clients[client].messageQueue = &[]string{}
	}
	return nil
}


func (server *Server) ListRoom(client string, isSuccessful *bool) error  {
	c := server.clients[client]
	roomNames := []string{}
	for _, room := range server.chatRooms{
		roomNames = append(roomNames, room.roomName)
	}

	sort.Strings(roomNames)

	for _, room := range roomNames{
		var message string
		if  c.currentRoom.roomName == room{
			message = "SYSTEM => (Current) (Joined) "+ room
		} else if _, ok := server.chatRooms[room].clients[client]; ok {
			message = "SYSTEM =>           (Joined) "+room
		} else {
			message = "SYSTEM =>                    "+room
		}
		c._appendMessage(message)
	}
	return nil
}



// END Server


//////////////////////// ChatRoom
type ChatRoom struct {
	roomName string
	clients map[string]*Client
	history []string
}

func NewChatRoom(name string) *ChatRoom {
	return &ChatRoom{roomName:name, clients:map[string]*Client{}}
}

// END ChatRoom



//////////////////////// Client
type Client struct {
	clientName string
	joinedRooms map[string]*ChatRoom
	currentRoom *ChatRoom
	messageQueue *[]string
}

func NewClient(name string) *Client{
	return &Client{clientName:name, joinedRooms: map[string]*ChatRoom{}, messageQueue: &[]string{}}
}

func (client *Client) _appendMessage(message string)  {
	*(client.messageQueue) = append((*(client.messageQueue)), message)
}

// END Client


func main() {
	server := NewServer()
	rpc.Register(server)
	rpc.HandleHTTP()

	listener, e := net.Listen("tcp", ":8088")
	if e != nil {
		log.Fatal("listen error:", e)
	}
	http.Serve(listener, nil)
}
package main

import (
	"net/rpc"
	"net"
	"log"
	"net/http"
	"errors"
	"fmt"
	"../shared"
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

func (server *Server) AddUser(client string)  {
	newClient := NewClient(client)
	server.clients[client] = newClient
}

func (server *Server) JoinUserToRoom(client string, room string){
	c := server.clients[client]
	r := server.chatRooms[room]

	r.clients[client] = c
	c.joinedRooms[room] = r
}

func (server *Server) SwitchUserToRoom(client string, room string){
	c := server.clients[client]
	r := server.chatRooms[room]

	c.currentRoom = r
}

func (server *Server) Connect(client string, isSuccessful *bool) error{
	if server.clients[client] == nil{
		defer fmt.Println("New connection: User: "+ client)
		defaultRoom := "default-room"
		server.AddUser(client)
		server.JoinUserToRoom(client, defaultRoom)
		server.SwitchUserToRoom(client, defaultRoom)

		*isSuccessful = true
		return nil;
	} else {
		return errors.New("this name is already taken")
	}
}


func (server *Server) Speak(arg shared.Message, messageBack *string) error{
	client:= server.clients[arg.Client]
	room := client.currentRoom


	for _, c := range room.clients{
		*(c.messageQueue) = append((*(c.messageQueue)), arg.Message)
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
	//*server.clients[client].messageQueue.Messages = (*(server.clients[client].messageQueue.Messages))[:0]
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
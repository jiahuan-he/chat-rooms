package main

import (
	"bufio"
	"net"
	"fmt"
	"strconv"
	"strings"
	"sync"
	"sort"
)

type ChatRoom struct{
	name string
	connectedSockets []*ServerClient
	history []string
}

type SafeMap struct {
	m map[string]ChatRoom
	mux sync.Mutex
}

func main() {
	ln, _ := net.Listen("tcp", ":8088")
	allRooms := SafeMap{}
	allRooms.m = map[string]ChatRoom{"default-room":ChatRoom{name:"default-room"}}
	defaultRoom := allRooms.m["default-room"]

	for {
		newConn,_ := ln.Accept()
		newClient := ServerClient{}
		newClient.currentRoom = &defaultRoom
		newClient.joinedRooms = append(newClient.joinedRooms, &defaultRoom)
		newClient.conn = &newConn
		newClient.currentRoom.connectedSockets = append(newClient.currentRoom.connectedSockets, &newClient)
		go newClient.newListener(allRooms)
	}
}

type ServerClient struct {
	name        string
	joinedRooms []*ChatRoom
	conn        *net.Conn
	currentRoom *ChatRoom
}

func (client *ServerClient) newListener(allRooms SafeMap)  {
	client.send("SYSTEM => Please enter your name: ")
	name, _ := bufio.NewReader(*client.conn).ReadString('\n')
	client.name = strings.TrimSuffix(name, "\n")
	client.currentRoom.broadcast("Welcome "+client.name+" joining " + client.currentRoom.name, nil)

	for {
		message, err := bufio.NewReader(*client.conn).ReadString('\n')
		message = strings.TrimRight(strings.TrimSpace(message), "\n")
		command := strings.Fields(message)
		if err != nil {
			break
		}
		if len(command) == 0 || command == nil{
			continue
		}
		switch command[0] {
		case "/create":
			for _,roomName := range command[1:] {
				if _, ok:=allRooms.m[roomName]; ok{
					(*client).send("SYSTEM => Error: "+roomName+" already exists")
					continue
				}
				allRooms.m[roomName] = ChatRoom{name:roomName}
				fmt.Println("created: " + roomName)
				(*client).send("SYSTEM => Success: "+roomName+" created")
			}
			println("rooms now: " +strconv.Itoa(len(allRooms.m)))

		case "/leave":

		case "/join":
		case "/list":
			keys := make([]string, len(allRooms.m))
			i := 0
			for k:= range allRooms.m{
				keys[i] = k
				i++
			}
			sort.Strings(keys)
			for _, k:= range keys{
				client.send("SYSTEM => " + k)
			}


		case "/switch":
		case "/rename":
		default:
			client.currentRoom.broadcast("("+client.currentRoom.name + ") " + client.name + " => "+ message, client)
			client.send("("+client.currentRoom.name + ") me" + " => "+ message)
		}
	}
}

func (client *ServerClient) send(message string) {
	fmt.Println("sending: " + message)
	(*client.conn).Write([]byte(message + "\n"))
}

func (chatRoom *ChatRoom) broadcast(message string, selfClient *ServerClient){
	for _,client := range chatRoom.connectedSockets{
		if client == selfClient{
			continue
		}
		client.send(message)
	}
}

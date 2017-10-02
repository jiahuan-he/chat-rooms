package main

import (
	"bufio"
	"net"
	"fmt"
	"strconv"
	"strings"
	//"time"
	//"sync"
)

type ChatRoom struct{
	name string
	connectedSockets []*ServerClient
	history []string
}



func main() {
	ln, _ := net.Listen("tcp", ":8088")
	allRooms := map[string]ChatRoom{"default-room":ChatRoom{name:"default-room"}}
	//allRoomsMap := sync.Map{}

	defaultRoom := allRooms["default-room"]

	for {
		newConn,_ := ln.Accept()
		newClient := ServerClient{}
		newClient.currentRoom = &defaultRoom
		newClient.joinedRooms = append(newClient.joinedRooms, &defaultRoom)
		newClient.conn = &newConn
		newClient.currentRoom.connectedSockets = append(newClient.currentRoom.connectedSockets, &newClient)
		fmt.Println("log: current user number: "+ strconv.Itoa(len(defaultRoom.connectedSockets)))
		go newClient.newListener(allRooms)
	}
}

type ServerClient struct {
	name        string
	joinedRooms []*ChatRoom
	conn        *net.Conn
	currentRoom *ChatRoom
}

func (client *ServerClient) newListener(allRooms map[string]ChatRoom)  {
	//Prompt user to enter their name
	client.send("SYSTEM => Please enter your name: ")
	name, _ := bufio.NewReader(*client.conn).ReadString('\n')
	client.name = strings.TrimSuffix(name, "\n")
	client.currentRoom.broadcast("Welcome "+client.name+" joining " + client.currentRoom.name, nil)

	for {
		message, err := bufio.NewReader(*client.conn).ReadString('\n')
		message = strings.TrimRight(strings.TrimSpace(message), "\n")
		command := strings.Fields(message)
		fmt.Print("Message Received:", string(message))
		if err != nil{
			break
		}
		switch command[0] {
		case "/create":
			//c:= make(chan ChatRoom)
			for _,roomName := range command[1:] {
				//TODO solve concurrency problem!!
				if _, ok:=allRooms[roomName]; ok{
					client.send("SYSTEM => Error: "+roomName+" already exists")
					break
				}
				fmt.Println(roomName)

				allRooms[roomName] = ChatRoom{name:roomName}
				// A workaround
				//time.Sleep(100*time.Millisecond)
				client.send("SYSTEM => Success: "+roomName+" created")
			}

		case "/leave":

		case "/join":
		case "/list":
		case "/switch":
		case "/rename":
		default:
			client.currentRoom.broadcast("("+client.currentRoom.name + ") " + client.name + " => "+ message, client)
			client.send("("+client.currentRoom.name + ") me" + " => "+ message)
		}
	}
}

func (clent *ServerClient) send(message string) {
	(*clent.conn).Write([]byte(message + "\n"))
}

func (chatRoom *ChatRoom) broadcast(message string, selfClient *ServerClient){
	for _,client := range chatRoom.connectedSockets{

		//fmt.Println(client.name)
		//fmt.Println("other: "+client.name)
		//if(selfClient != nil){
		//	fmt.Println("self: "+selfClient.name)
		//}
		if client == selfClient{
			continue
		}
		client.send(message)
	}
}

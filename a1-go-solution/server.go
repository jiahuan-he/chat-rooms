package main

import (
	"bufio"
	"net"
	"fmt"
	"strconv"
	"strings"
)

type ChatRoom struct{
	name string
	connectedSockets []*ServerClient
	history []string
}

func main() {
	ln, _ := net.Listen("tcp", ":8088")
	allRooms := map[string]ChatRoom{"default-room":ChatRoom{name:"default-room"}}
	defaultRoom := allRooms["default-room"]

	for {
		newConn,_ := ln.Accept()
		newClient := ServerClient{}
		newClient.currentRoom = &defaultRoom
		newClient.joinedRooms = append(newClient.joinedRooms, &defaultRoom)
		newClient.conn = &newConn
		newClient.currentRoom.connectedSockets = append(newClient.currentRoom.connectedSockets, &newClient)
		fmt.Println("log: current user number: "+ strconv.Itoa(len(defaultRoom.connectedSockets)))
		go newClient.newListener()
	}
}

type ServerClient struct {
	name        string
	joinedRooms []*ChatRoom
	conn        *net.Conn
	currentRoom *ChatRoom
}

func (client *ServerClient) newListener()  {
	//Prompt user to enter their name
	client.send("SYSTEM => Please enter your name: ")
	name, _ := bufio.NewReader(*client.conn).ReadString('\n')
	client.name = strings.TrimSuffix(name, "\n")
	client.currentRoom.broadcast("Welcome "+client.name+" joining " + client.currentRoom.name, nil)
	for {
		message, err := bufio.NewReader(*client.conn).ReadString('\n')
		if err != nil{
			break
		}
		// output message received
		fmt.Print("Message Received:", string(message))
		client.currentRoom.broadcast("("+client.currentRoom.name + ") " + client.name + " => "+ message, client)
		client.send("("+client.currentRoom.name + ") me" + " => "+ message)
	}
}

func (clent *ServerClient) send(message string) {
	(*clent.conn).Write([]byte(message + "\n"))
}

func (chatRoom ChatRoom) broadcast(message string, selfClient *ServerClient){
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





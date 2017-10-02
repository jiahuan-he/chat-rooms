package main

import (
	"bufio"
	"net"
	"fmt"
)

type ChatRoom struct{
	name string
	connectedSockets []ServerClient
	history []string
}

func main() {
	ln, _ := net.Listen("tcp", ":8088")
	allRooms := map[string]ChatRoom{"default-room":ChatRoom{name:"default-room"}}
	defaultRoom := allRooms["default-room"]

	for {
		newConn,_ := ln.Accept()
		newClient := ServerClient{}
		newClient.currentRoom = defaultRoom
		newClient.joinedRooms = append(newClient.joinedRooms, defaultRoom)
		newClient.conn = newConn
		newClient.currentRoom.connectedSockets = append(newClient.currentRoom.connectedSockets, newClient)
		fmt.Println(len(defaultRoom.connectedSockets))
		go newClient.newListener()
	}
}

type ServerClient struct {
	name        string
	joinedRooms []ChatRoom
	conn        net.Conn
	currentRoom ChatRoom
}

func (client ServerClient) newListener()  {
	// will listen for message to process ending in newline (\n)
	for {
		message, _ := bufio.NewReader(client.conn).ReadString('\n')
		// output message received
		fmt.Print("Message Received:", string(message))
		client.currentRoom.broadcast(message)
	}
}

func (clent ServerClient) send(message string) {
	clent.conn.Write([]byte(message + "\n"))
}

func (chatRoom ChatRoom) broadcast(message string){
	for index,client := range chatRoom.connectedSockets{
		fmt.Println(index)
		client.send(message)
	}
}




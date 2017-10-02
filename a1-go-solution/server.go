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
	chatRooms := map[string]ChatRoom{"default-room":ChatRoom{name:"default-room"}}
	defaultRoom := chatRooms["default-room"]

	for {
		newConn,_ := ln.Accept()
		newClient := ServerClient{}
		newClient.currentRoom = defaultRoom
		defaultRoom.connectedSockets = append(defaultRoom.connectedSockets, newClient)
		go newClient.newListener(newConn)
	}
}

type ServerClient struct {
	name        string
	chatRooms   []ChatRoom
	conn        net.Conn
	currentRoom ChatRoom
}


func (client ServerClient) newListener(conn net.Conn)  {
	// will listen for message to process ending in newline (\n)
	for {
		message, _ := bufio.NewReader(conn).ReadString('\n')
		// output message received
		fmt.Print("Message Received:", string(message))
		// sample process for string received
		//newmessage := message
		//send new string back to client
		//for _, client:= range  client.currentRoom.connectedSockets{
		//	client.conn.Write([]byte(newmessage + "\n"))
		//}
	}
}


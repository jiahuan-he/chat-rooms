package main

import (
	"bufio"
	"net"
	"fmt"
	//"strconv"
	"strings"
	"sync"
	"sort"
	//"io/ioutil"
)

type ChatRoom struct{
	name string
	connectedSockets *SafeSocketsMap
	history []string
}

type SafeSocketsMap struct {
	m map[string]*ServerClient
	mux sync.Mutex
}

type SafeRoomMap struct {
	m map[string]*ChatRoom
	mux sync.Mutex
}

func main() {
	ln, _ := net.Listen("tcp", ":8088")
	allRooms := &SafeRoomMap{}
	allRooms.m = map[string]*ChatRoom{}
    defaultRoom := &ChatRoom{name:"default-room", connectedSockets: &SafeSocketsMap{}}
    defaultRoom.connectedSockets.m = make(map[string]*ServerClient)
    allRooms.m[defaultRoom.name] = defaultRoom

	for {
		newConn,_ := ln.Accept()
		newClient := &ServerClient{joinedRooms:&SafeRoomMap{m: map[string]*ChatRoom{}}}
		newClient.conn = &newConn
		go newClient.newListener(allRooms)
	}
}

type ServerClient struct {
	name        string
	joinedRooms *SafeRoomMap
	conn        *net.Conn
	currentRoom *ChatRoom
}

func (client *ServerClient) newListener(allRooms *SafeRoomMap)  {
	//client.send("SYSTEM => Please enter your name: ")
	//var lastname string

	for {
		//if  _, ok := allRooms.m["default-room"].connectedSockets.m["test"]; ok{
		//	fmt.Println("existing name: "+allRooms.m["default-room"].connectedSockets.m["test"].name)
		//	fooRoom := allRooms.m["default-room"]
		//	fooSockets := fooRoom.connectedSockets
		//	fooClients := fooSockets.m
		//	fooClient := fooClients["test"]
		//	fmt.Println(fooClient)
		//}

		client.send("SYSTEM => Please enter your name: ")
		// Bug fixed here: process name here at the beginning
		name, err := bufio.NewReader(*client.conn).ReadString('\n')
		if err != nil{
			println(err)
			continue
		}
		name = strings.TrimSpace(strings.TrimSuffix(name, "\n"))
		duplicatedName := false
		for _, v := range allRooms.m{
			if _, ok := v.connectedSockets.m[name]; ok{
				duplicatedName = true
				client.send("SYSTEM => The name your entered already exists")
				break
			}
		}
		if duplicatedName{
			continue
		} else {
			client.name = name
			allRooms.m["default-room"] .connectedSockets.m[client.name] = client
			client.joinedRooms.m["default-room"] = allRooms.m["default-room"]
			client.currentRoom = allRooms.m["default-room"]
			break
		}
	}

	client.currentRoom.broadcast("SYSTEM => Welcome "+client.name+" joining " + client.currentRoom.name, nil)

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
				allRooms.m[roomName] = &ChatRoom{name:roomName, connectedSockets: &SafeSocketsMap{m:map[string]*ServerClient{}}}
				fmt.Println("created: " + roomName)
				(*client).send("SYSTEM => Success: "+roomName+" created")
			}

		case "/leave":
			for _, roomName := range command[1:]  {
				if room,ok := allRooms.m[roomName]; ok{
					//hasSocket := false
					if _, ok := room.connectedSockets.m[client.name]; ok {
						delete(room.connectedSockets.m, client.name)
                        delete(client.joinedRooms.m, roomName)
					} else {
						client.send("SYSTEM => Error: You are not in room: "+ roomName)
					}
					//for i := 0; i < len(room.connectedSockets); i++ {
					//	if room.connectedSockets[i] == client{
					//		hasSocket = true
					//		room.connectedSockets = append(room.connectedSockets[:i],room.connectedSockets[i+1:]...)
					//		for k:= 0; k<len(client.joinedRooms);k++ {
					//			if client.joinedRooms[i] == room{
                     //               client.joinedRooms = append(client.joinedRooms[:k], client.joinedRooms[k+1:]...)
					//			}
					//			if client.currentRoom == room{
					//				client.currentRoom = nil
					//			}
					//		}
					//	}
					//	if !hasSocket {
					//		client.send("SYSTEM => You have not join "+ roomName)
					//	}
					//}
				} else {
					(*client).send("SYSTEM => Error: "+roomName+" doesn't exist")
				}
			}

		case "/join":
            for _, roomName := range command[1:] {
				if room, ok := allRooms.m[roomName]; ok {
					if _, ok := room.connectedSockets.m[client.name]; ok {
						client.send("SYSTEM => You are already in room: " + roomName)
						continue
					}
					room.connectedSockets.m[client.name] = client
					client.joinedRooms.m[roomName] = allRooms.m[roomName]
					client.send("SYSTEM => Success: joined " + roomName)
				} else {
					client.send("SYSTEM => Error: " + roomName + " doesn't exist")
				}

				////alreadlyJoined := false
				//if room, ok:= allRooms.m[roomName]; ok{
				//	room.connectedSockets = append(room.connectedSockets, client)
				//	client.joinedRooms = append(client.joinedRooms, room)
				//	room.broadcast("SYSTEM => Welcome "+client.name+" joining " + room.name, nil)
				//} else {
				//	client.send("SYSTEM => Error: "+roomName+" doesn't exist")
				//}
			}
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
	for _,client := range chatRoom.connectedSockets.m{
		if client == selfClient{
			continue
		}
		client.send(message)
	}
}

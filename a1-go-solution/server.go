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
				client.send("SYSTEM => Success: "+roomName+" created")
			}

		case "/leave":
			for _, roomName := range command[1:]  {
				if room,ok := allRooms.m[roomName]; ok{
					if _, ok := room.connectedSockets.m[client.name]; ok {
						delete(room.connectedSockets.m, client.name)
                        delete(client.joinedRooms.m, roomName)

						if client.currentRoom.name == roomName {
							client.currentRoom = nil
							client.send("SYSTEM => Note: You just left your current room, please switch to another room to send messages")
						}
					} else {
						client.send("SYSTEM => Error: You are not in room: "+ roomName)
					}
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
				_, joined:= client.joinedRooms.m[k]
				current := k == client.currentRoom.name
				if current&&joined {

					client.send("SYSTEM => (current) (joined) "+k)
				} else if joined{
					client.send("SYSTEM =>           (joined) "+ k)
				} else {
					client.send("SYSTEM =>                    " + k)
				}


			}

		case "/switch":
			if len(command) != 2{
				client.send("SYSTEM => Error: Wrong number of arguments")
				break
			}
			roomName := command[1]
			if v,ok := allRooms.m[roomName]; ok {
				if client.currentRoom.name == v.name {
					client.send("SYSTEM => Error: Your current room is already this room")
					break
				}
				if _, ok := client.joinedRooms.m[roomName]; ok{
					client.currentRoom = v
					client.send("SYSTEM => Success: You switched to room: "+roomName)
					break
				}
				client.send("SYSTEM => Error: You have to join room: "+roomName+"before you can switch to it")


			}

		case "/rename":
		default:
			if client.currentRoom != nil{
				client.currentRoom.broadcast("("+client.currentRoom.name + ") " + client.name + " => "+ message, client)
				client.send("("+client.currentRoom.name + ") me" + " => "+ message)
			} else {
				client.send("SYSTEM => Error: your current room is empty")
			}


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

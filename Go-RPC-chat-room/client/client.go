package main

import (
	"net/rpc"
	"log"
	"fmt"
	"bufio"
	"os"
	"../shared"
	"time"
	"strings"
)

func main() {
	client, err := rpc.DialHTTP("tcp",  "localhost:8088")
	if err != nil {
		log.Fatal("dialing:", err)
	}

	reader := bufio.NewReader(os.Stdin)
	fmt.Println("System => Please enter your name: ")
	name, _ := reader.ReadString('\n')
	name = shared.StripString(name)
	var connSuccess *bool
	err = client.Call("Server.Connect", name, &connSuccess)
	go Retrieve(client, name)
	if err == nil{
		if  *connSuccess{
			for {
				//	 read in input from stdin
				m , err := reader.ReadString('\n')
				m = shared.StripString(m)
				var messageBack *string
				if err == nil{
					input := strings.Split(m, " ")

					// Crete room
					if strings.HasPrefix(m, "/create") {
						if len(input) > 1{
							for i := 1; i < len(input); i++ {
								room := input[i]
								arg := shared.CreateRoom{Client:name, Room:room}
								var roomBack *string;
								err = client.Call("Server.CreateRoom", arg, &roomBack)
								if err != nil{
									fmt.Println(err)
								}
							}
						} else {
							fmt.Println("Wrong number of parameters")
						}
					} else if strings.HasPrefix(m, "/list") {

						var isSuccessful *bool
						err = client.Call("Server.ListRoom", name, &isSuccessful)
						if err != nil {
							fmt.Println(err)
						}

					} else if strings.HasPrefix(m, "/join") {
						if len(input) > 1{
							for i := 1; i < len(input); i++ {
								room := input[i]
								var roomBack *string;
								arg := shared.JoinRoom{Client:name, Room:room}
								err = client.Call("Server.JoinRoom", arg, &roomBack)
								if err != nil{
									fmt.Println(err)
								}
							}
						} else {
							fmt.Println("Wrong number of parameters")
						}
					} else if strings.HasPrefix(m, "/leave") {
						if len(input) > 1{
							for i := 1; i < len(input); i++ {
								room := input[i]
								var roomBack *string;
								arg := shared.LeaveRoom{Client:name, Room:room}
								err = client.Call("Server.LeaveRoom", arg, &roomBack)
								if err != nil{
									fmt.Println(err)
								}
							}
						} else {
							fmt.Println("Wrong number of parameters")
						}
					} else if strings.HasPrefix(m, "/switch") {
						if len(input) == 2{
								room := input[1]
								var roomBack *string;
								arg := shared.SwitchRoom{Client:name, Room:room}
								err = client.Call("Server.SwitchRoom", arg, &roomBack)
								if err != nil{
									fmt.Println(err)
								}
						} else {
							fmt.Println("Wrong number of parameters")
						}
					} else {
						arg := shared.Message{Message:m, Client:name}
						client.Call("Server.Speak", arg,  &messageBack)
					}
				} else {
					fmt.Println(err)
					return
				}
			}
		}
	} else {
		fmt.Println(err)
	}
}

func Retrieve(client *rpc.Client, clientName string){
	for {
		var mq *[]string
		client.Call("Server.Retrieve", clientName, &mq)
		if mq != nil && len(*mq) > 0{
			for _, m := range *mq{
				if strings.Trim(strings.TrimSpace(m), "\n") != "" {
					fmt.Println(m)
				}
			}
		}
		time.Sleep(time.Millisecond * 500)
	}
}


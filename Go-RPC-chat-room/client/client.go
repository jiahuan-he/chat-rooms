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
	name = strings.TrimRight(name, "\n")
	var connSuccess *bool
	err = client.Call("Server.Connect", name, &connSuccess)
	go Retrieve(client, name)
	if err == nil{
		if  *connSuccess{
			for {
				//	 read in input from stdin
				m , err := reader.ReadString('\n')
				var messageBack *string
				if err == nil{
					arg := shared.Message{Message:m, Client:name}
					client.Call("Server.Speak", arg,  &messageBack)
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
					fmt.Print(m)
				}
			}
		}
		time.Sleep(time.Millisecond * 500)
	}
}


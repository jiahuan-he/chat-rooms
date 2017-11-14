package main

import (
	"net/rpc"
	"log"
	"fmt"
	"bufio"
	"os"
)

func main() {
	client, err := rpc.DialHTTP("tcp",  "localhost:8088")
	if err != nil {
		log.Fatal("dialing:", err)
	}

	reader := bufio.NewReader(os.Stdin)
	fmt.Print("System => Please enter your name: ")
	name, _ := reader.ReadString('\n')

	var connSuccess *bool
	err = client.Call("Server.Connect", name, &connSuccess)
	if err == nil{
		if  *connSuccess{
			for {
				//	 read in input from stdin
				_ , err := reader.ReadString('\n')
				if err != nil{
					return
				}
			}
		}
	} else {
		fmt.Println(err)
	}


}

package main

import (
	"net/rpc"
	"log"
	"fmt"
)

func main() {
	client, err := rpc.DialHTTP("tcp",  "localhost:8088")
	if err != nil {
		log.Fatal("dialing:", err)
	}

	var reply *bool
	client.Call("Server.Hello", "Geoff", &reply)

	if err != nil {
		log.Fatal("arith error:", err)
	}
	fmt.Printf("%t", *reply)

}

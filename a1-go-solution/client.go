package main

import "net"
import "fmt"
import "bufio"
import (
	"os"
)

// connect to this ServerClient



func main() {
	conn, _ := net.Dial("tcp", "localhost:8088")

	go listen(conn)
	for {
	//	 read in input from stdin
		reader := bufio.NewReader(os.Stdin)
		text, _ := reader.ReadString('\n')
		// send to ServerClient
		fmt.Fprintf(conn, text)
	}
}

func listen(conn net.Conn) {
	for{
		// listen for reply
		message, _ := bufio.NewReader(conn).ReadString('\n')
		fmt.Print("Message from server: "+message)
	}
}
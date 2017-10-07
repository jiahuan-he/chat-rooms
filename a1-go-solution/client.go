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
		text, err := reader.ReadString('\n')
		if err != nil{
			return
		}
		// send to ServerClient
		fmt.Fprintf(conn, text)
	}
}

func listen(conn net.Conn) {
	reader := bufio.NewReader(conn)
	for{
		// listen for reply
		// Bug fixed here.
		message, err := reader.ReadString('\n')
		if(err != nil){
			break
		}
		fmt.Print(message)
	}
}
package main

import "net"
import "fmt"
import "bufio"
import (
	"os"
	"strings"
	"github.com/fatih/color"
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

		if strings.HasPrefix(message, "SYSTEM => Success:"){
			color.Green(message)
		} else if strings.HasPrefix(message, "SYSTEM => Error:"){
			color.Red(message)
		} else if strings.HasPrefix(message, "SYSTEM => ") {
			color.Yellow(message)
		} else {
			fmt.Print(message)
		}
	}
}
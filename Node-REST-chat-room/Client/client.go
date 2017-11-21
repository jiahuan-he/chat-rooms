package main

import (
	"fmt"
	"bufio"
	"os"
	"strings"
	"time"
)

type User struct {
	name string
}


func StripString(str string)  string{
	return strings.TrimSpace(strings.TrimRight(str, "\n"))
}

func main()  {
	url_client := "http://localhost:3000/client"
	url_message := "http://localhost:3000/message"
	url_room := "http://localhost:3000/room"
	reader := bufio.NewReader(os.Stdin)
	fmt.Println("System => Please enter your name: ")
	clientName, _ := reader.ReadString('\n')
	clientName = StripString(clientName)
	p := []Param{Param{key: "clientName", value: clientName}}
	Post(url_client, p)
	go Retrieve(clientName)
	for {


		//	 read in input from stdin
		m , err := reader.ReadString('\n')
		m = StripString(m)
		params := []Param{}
		//p := []Param{Param{key: "clientName", value: clientName}, Param{key: "message", value: m}}
		//err := Post(urlMessage, p)
		//if err != nil {
		//	return
		//}
		CLIENT_NAME := "clientName"
		ROOM_NAME := "roomName"


		if err == nil{
			input := strings.Split(m, " ")

			// Create room
			if strings.HasPrefix(m, "/create") {
				if len(input) > 1{
					for i := 1; i < len(input); i++ {
						room := input[i]
						params = append(params, Param{key: CLIENT_NAME, value: clientName})
						params = append(params, Param{key: ROOM_NAME, value: room})
						err := Post(url_room, params)
						if err != nil{
							fmt.Println(err)
						}
					}
				} else {
					fmt.Println("Wrong number of parameters")
				}
			} else if strings.HasPrefix(m, "/list") {
				params = append(params, Param{key:CLIENT_NAME, value:clientName})
				_, err :=Get(url_room, params)
				if err != nil {
					fmt.Println(err)
				}

			} else if strings.HasPrefix(m, "/join") {
				if len(input) > 1{
					for i := 1; i < len(input); i++ {
						room := input[i]
						params = append(params, Param{key:CLIENT_NAME, value: clientName})
						params = append(params, Param{key:ROOM_NAME, value: room})

						// TODO join room
						//if err != nil{
						//	fmt.Println(err)
						//}
					}
				} else {
					fmt.Println("Wrong number of parameters")
				}
			} else if strings.HasPrefix(m, "/leave") {
				if len(input) > 1{
					for i := 1; i < len(input); i++ {
						room := input[i]
						params = append(params, Param{key:CLIENT_NAME, value: clientName})
						params = append(params, Param{key:ROOM_NAME, value: room})
						// TODO leave room
						//if err != nil{
						//	fmt.Println(err)
						//}
					}
				} else {
					fmt.Println("Wrong number of parameters")
				}
			} else if strings.HasPrefix(m, "/switch") {
				if len(input) == 2{
					room := input[1]
					params = append(params, Param{key:CLIENT_NAME, value: clientName})
					params = append(params, Param{key:ROOM_NAME, value: room})
					// TODO switch room
					//if err != nil{
					//	fmt.Println(err)
					//}
				} else {
					fmt.Println("Wrong number of parameters")
				}
			} else {
				params = append(params, Param{key: "clientName", value: clientName}, Param{key: "message", value: m})
				err := Post(url_message, params)
				if err != nil {
					return
				}
			}
		} else {
			fmt.Println(err)
			return
		}
	}
}

type Param struct{
	key string
	value string
}


func Retrieve(clientName string){
	url_message := "http://localhost:3000/message"
	for {
		param := []Param{Param{key:"clientName", value: clientName}}
		messages, err := Get(url_message, param)
		if err != nil{
			 fmt.Println(err)
		}

		messages = strings.TrimSpace(StripString(messages))


		if(messages!= "[]"){
			for _, m := range ParseStrToArr(messages){
				fmt.Println(m)
			}
		}
		time.Sleep(time.Millisecond * 500)
	}
}

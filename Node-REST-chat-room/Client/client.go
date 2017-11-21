package main

import (
	"net/http"
	"fmt"
	"bytes"

	"bufio"
	"os"
	"strings"
	"time"
	"log"
	"encoding/json"
	"io/ioutil"
)

type User struct {
	name string
}


func StripString(str string)  string{
	return strings.TrimSpace(strings.TrimRight(str, "\n"))
}

func main()  {
	URL := "http://localhost:3000/client"
	urlMessage := "http://localhost:3000/message"
	reader := bufio.NewReader(os.Stdin)
	fmt.Println("System => Please enter your name: ")
	clientName, _ := reader.ReadString('\n')
	clientName = StripString(clientName)
	p := []Param{Param{key: "clientName", value: clientName}}
	Post(URL, p)
	go Retrieve(clientName)
	for {


		//	 read in input from stdin
		m , _ := reader.ReadString('\n')
		m = StripString(m)
		p := []Param{Param{key: "clientName", value: clientName}, Param{key: "message", value: m}}
		err := Post(urlMessage, p)
		if err != nil {
			return
		}
		//
		//if err == nil{
		//	input := strings.Split(m, " ")
		//
		//	// Create room
		//	if strings.HasPrefix(m, "/create") {
		//		if len(input) > 1{
		//			for i := 1; i < len(input); i++ {
		//				room := input[i]
		//				arg := shared.CreateRoom{Client:name, Room:room}
		//				var roomBack *string;
		//				err = client.Call("Server.CreateRoom", arg, &roomBack)
		//				if err != nil{
		//					fmt.Println(err)
		//				}
		//			}
		//		} else {
		//			fmt.Println("Wrong number of parameters")
		//		}
		//	} else if strings.HasPrefix(m, "/list") {
		//
		//		var isSuccessful *bool
		//		err = client.Call("Server.ListRoom", name, &isSuccessful)
		//		if err != nil {
		//			fmt.Println(err)
		//		}
		//
		//	} else if strings.HasPrefix(m, "/join") {
		//		if len(input) > 1{
		//			for i := 1; i < len(input); i++ {
		//				room := input[i]
		//				var roomBack *string;
		//				arg := shared.JoinRoom{Client:name, Room:room}
		//				err = client.Call("Server.JoinRoom", arg, &roomBack)
		//				if err != nil{
		//					fmt.Println(err)
		//				}
		//			}
		//		} else {
		//			fmt.Println("Wrong number of parameters")
		//		}
		//	} else if strings.HasPrefix(m, "/leave") {
		//		if len(input) > 1{
		//			for i := 1; i < len(input); i++ {
		//				room := input[i]
		//				var roomBack *string;
		//				arg := shared.LeaveRoom{Client:name, Room:room}
		//				err = client.Call("Server.LeaveRoom", arg, &roomBack)
		//				if err != nil{
		//					fmt.Println(err)
		//				}
		//			}
		//		} else {
		//			fmt.Println("Wrong number of parameters")
		//		}
		//	} else if strings.HasPrefix(m, "/switch") {
		//		if len(input) == 2{
		//			room := input[1]
		//			var roomBack *string;
		//			arg := shared.SwitchRoom{Client:name, Room:room}
		//			err = client.Call("Server.SwitchRoom", arg, &roomBack)
		//			if err != nil{
		//				fmt.Println(err)
		//			}
		//		} else {
		//			fmt.Println("Wrong number of parameters")
		//		}
		//	} else {
		//		arg := shared.Message{Message:m, Client:name}
		//		client.Call("Server.Speak", arg,  &messageBack)
		//	}
		//} else {
		//	fmt.Println(err)
		//	return
		//}
	}
}

type Param struct{
	key string
	value string
}

func Post(url string, params []Param) error {
	values := map[string]string{}
	for _, p := range params{
		values[p.key] = p.value
	}
	jsonValue, _ := json.Marshal(values)
	resp, err := http.Post(url, "application/json", bytes.NewBuffer(jsonValue))
	if err != nil{
		fmt.Println(err)
		return err
	}
	resp.Body.Close()
	return nil
}

func Get(url string, params []Param) (string, error){
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		log.Print(err)
		os.Exit(1)
	}

	q := req.URL.Query()
	for _, p := range params{
		q.Add(p.key, p.value)
	}

	req.URL.RawQuery = q.Encode()
	res, err := http.Get(req.URL.String())
	if err != nil{
		return "", err
	}

	body, err := ioutil.ReadAll(res.Body)
	if err != nil{
		return "", err
	}
	messages := string(body)
	messages = strings.TrimSpace(StripString(messages))
	return messages, nil
}



func Retrieve(clientName string){
	URL := "http://localhost:3000/message"
	for {
		param := []Param{Param{key:"clientName", value: clientName}}
		messages, err := Get(URL, param)
		if err != nil{
			 fmt.Println(err)
		}

		messages = strings.TrimSpace(StripString(messages))


		if(messages!= "[]"){
			//fmt.Println(messages)
			for _, m := range ParseStrToArr(messages){
				fmt.Println(m)
			}
		}
		time.Sleep(time.Millisecond * 500)
	}
}

func ParseStrToArr(str string) []string{
	trimed:= strings.TrimLeft(strings.TrimRight(str, `"]`), `["`)
	return strings.Split(trimed, `","`)
}
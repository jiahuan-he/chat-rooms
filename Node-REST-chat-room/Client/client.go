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
		m , _ := reader.ReadString('\n')
		m = StripString(m)
		p := []Param{Param{key: "clientName", value: clientName}, Param{key: "message", value: m}}
		err := Post(urlMessage, p)
		if err != nil {
			return
		}
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



func Retrieve(clientName string){
	URL := "http://localhost:3000/message"
	for {
		req, err := http.NewRequest("GET", URL, nil)
		if err != nil {
			log.Print(err)
			os.Exit(1)
		}

		q := req.URL.Query()
		q.Add("clientName", clientName)
		req.URL.RawQuery = q.Encode()
		res, err := http.Get(req.URL.String())
		if err != nil{
			fmt.Println(err)
			return
		}
		body, err := ioutil.ReadAll(res.Body)
		if err != nil{
			return
		}

		messages := string(body)
		messages = strings.TrimSpace(StripString(messages))

		if(messages!= "[]"){
			fmt.Println(string(body))
		}
		time.Sleep(time.Millisecond * 500)
	}
}
package main

import (
	"strings"
	"encoding/json"
	"net/http"
	"bytes"
	"fmt"
	"log"
	"os"
	"io/ioutil"
)

func ParseStrToArr(str string) []string{
	trimed:= strings.TrimLeft(strings.TrimRight(str, `"]`), `["`)
	return strings.Split(trimed, `","`)
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


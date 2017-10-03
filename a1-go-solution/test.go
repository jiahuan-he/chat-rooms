package main

import (
	"fmt"
	//"time"
	//"strconv"
)

func main()  {

	dict := map[int]string{0:"0"}

	go func() {
		command := []string{"a", "b", "c", "d"}
		for k,v:= range command{
			dict[k] = v
		}
	}()

	fmt.Println(dict)
	//time.Sleep(time.Second)
}
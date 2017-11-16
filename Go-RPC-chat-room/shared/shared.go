package shared

import "strings"

type Message struct {
	Client string
	Message string
	Room string
}

type SwitchRoom struct {
	Client string
	Room string
}

type JoinRoom struct {
	Client string
	Room string
}


type LeaveRoom struct {
	Client string
	Room string
}


type CreateRoom struct {
	Client string
	Room string
}

func StripString(str string)  string{
	return strings.TrimSpace(strings.TrimRight(str, "\n"))
}
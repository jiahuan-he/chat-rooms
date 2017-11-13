package main

import (
	"net/rpc"
	"net"
	"log"
	"net/http"
)


type Server struct{}

func (server *Server) Connect(client string, isSuccessful *bool) error{
	return nil;
}

func (server *Server) Hello(words string, isSuccessful *bool) error {
	*isSuccessful = true
	return nil
}

//
//
//type Server struct{}
//
//func (server *Server) Connect(client string, isSuccessful *bool) error{
//	return nil;
//}
//
//func (server *Server) Hello(words string, isSuccessful *bool) error {
//	*isSuccessful = true
//	return nil
//}

func main() {
	server := new (Server)
	rpc.Register(server)
	rpc.HandleHTTP()

	listener, e := net.Listen("tcp", ":8088")
	if e != nil {
		log.Fatal("listen error:", e)
	}
	http.Serve(listener, nil)
}
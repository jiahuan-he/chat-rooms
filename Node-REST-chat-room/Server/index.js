const ChatRoom = require("./model/chatRoom")
const Client = require("./model/client")


module.exports = class Server{

    constructor(){
        this.defaultName = "default-room"
        this.clients = {}
        this.chatRooms = {[this.defaultName] : new ChatRoom(this.defaultName)}
   
    }

    connect (clientName) {        

        if(this.clients[clientName]){            
            console.log("this name exists")
            return false
        } else {
            const defaultRoom = this.chatRooms[this.defaultName]
            this.chatRooms[this.defaultName] = defaultRoom

            const newClient = new Client(clientName)
            this.clients[clientName] = newClient
            
            this.chatRooms[this.defaultName].clients[clientName] = newClient
            newClient.currentRoom = defaultRoom
            newClient.joinedRooms[this.defaultName] = defaultRoom

            Object.keys(defaultRoom.clients).map((client) => {
                this.clients[client].messageQueue.push("SYSTEM => Welcome new user: "+ clientName)    
            });            
            return true
        }    
    }

    speak(clientName, message){
        const client = this.clients[clientName]
        const room = client.currentRoom
        if(room){
            message = "("+room.roomName+") "+client.clientName+" => "+message
            Object.keys(room.clients).map((client) => {
                this.clients[client].messageQueue.push(message)    
            }); 
        } else {
            message = "SYSTEM => Note: You current room is empty, please join and switch to a room to speak"
            client.messageQueue.push(message)
        }
    }

    retrieve(clientName){        
        const messages = this.clients[clientName].messageQueue.map( (m) => {
            return m
        })
        this.clients[clientName].messageQueue = []
        return messages
    }

    createRoom(clientName, roomName){
        // If the room already exists
        let message = ""
        if(this.chatRooms[roomName]){
            message = "SYSTEM => Error: Room: "+ roomName + "already exists"
        } else { // If the room does not exist
            this.chatRooms[roomName] = new ChatRoom(roomName)
            message = "SYSTEM => Success: create room: "+ roomName
        }
        this.clients[clientName].messageQueue.push(message)
    }

    listRoom(clientName){
        const client = this.clients[clientName]
        let rooms = Object.keys(this.chatRooms)
        rooms = rooms.sort()
        rooms.map( (room) => {
            let message = ""
            if  (client.currentRoom && client.currentRoom.roomName === room){
                message = "SYSTEM => (Current) (Joined) "+ room
            } else if (this.chatRooms[room].clients[clientName]){
                message = "SYSTEM =>           (Joined) "+room
            } else {
                message = "SYSTEM =>                    "+room
            }
            client.messageQueue.push(message)            
        })
    }

    joinRoom(clientName, roomName){
        let message = ""
        const client = this.clients[clientName]
        if (this.chatRooms[roomName]){
            if (client.joinedRooms[roomName]){
                message = "SYSTEM => Error: You've already joined room: "+ roomName
            } else {
                const room = this.chatRooms[roomName]
                client.joinedRooms[roomName] = room
                room.clients[clientName] = client
                message = "SYSTEM => Success: joined room: " + roomName
            }
        } else {
            message = "SYSTEM => Error: Room: "+ roomName + " doesn't exist"
        }
        client.messageQueue.push(message)            
    }

    switchRoom(clientName, roomName){
        let message = ""
        const client = this.clients[clientName]
        if (this.chatRooms[roomName]){
            // success
            if(client.joinedRooms[roomName]){
                const room = this.chatRooms[roomName]
                client.currentRoom = room
                message = ("SYSTEM => Success: You Switched to room "+ roomName);
            } else {
                // exists but not joined
                message = ("SYSTEM => Error: You have to join "+ roomName +" first");
            }
        } else { 
            // Non-exist
            message = "SYSTEM => Error: Room: "+ roomName + " doesn't exist"
        }
        client.messageQueue.push(message)     
    }

    leaveRoom(clientName, roomName){
        let message = ""
        const client = this.clients[clientName]
        const room = this.chatRooms[roomName]
        if(room){
            const joinedRoom = client.joinedRooms[roomName]
            if(joinedRoom){
                delete client.joinedRooms[roomName]
                delete joinedRoom.clients[clientName]
                client.messageQueue.push("SYSTEM => Success: You left room "+ roomName)
                if(client.currentRoom.roomName === roomName){
                    client.messageQueue.push("SYSTEM => Note: You left current room, please join a room to speak")
                    delete client.currentRoom
                }
            } else {
                client.messageQueue.push("SYSTEM => Error: You have to join "+ roomName +" first")
            }
        } else {
            client.messageQueue.push("SYSTEM => Error: Room: "+ roomName + " doesn't exist")
        }
    }
}
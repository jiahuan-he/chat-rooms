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
}
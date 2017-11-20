module.exports = class Client{
    constructor(name){
        this.clientName = name
        this.currentRoom = null
        this.joinedRooms = {}
        this.messageQueue = []
    }
}
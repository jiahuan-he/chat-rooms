module.exports = class ChatRoom{
    constructor(roomName){
        this.roomName = roomName
        this.clients = {}
        this.history = []
    }
}
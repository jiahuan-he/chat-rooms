const express = require("express")
const bodyParser = require('body-parser');
const Server = require("./Server")
const app = express()

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

const srv = new Server()
app.post('/client', (req, res) => {
        
        const clientName = req.body.clientName
        console.log("post /client")
        console.log(clientName)
        console.log()
        if (srv.connect(clientName)){
            res.sendStatus(200)
        } else {
            res.sendStatus(409)
        }
    }
)

// Client speaks
app.post('/message', (req, res) => {
    console.log('post /message')
    
    const clientName = req.body.clientName
    const message = req.body.message
    console.log(clientName)
    console.log(message)
    console.log()
    res.sendStatus(200)
    srv.speak(clientName, message)
})

// Client Retrive, content-type: 
app.get('/message', (req, res) => {
    const clientName = req.query.clientName
    const messages = srv.retrieve(clientName)
    res.json(messages)
})

// Client List rooms
app.post('/room', (req, res) => {
    const clientName = req.body.clientName
    const roomName = req.body.roomName
    const method = req.body.method
    console.log('post /room')
    console.log(clientName)
    console.log(roomName)
    console.log()
    switch (method){
        case "join":
            srv.joinRoom(clientName, roomName)
            break
        case "switch":
            srv.switchRoom(clientName, roomName)
            break
        case "leave":
            srv.leaveRoom(clientName, roomName)
            break
        default: 
            srv.createRoom(clientName, roomName)
    }
    
    res.sendStatus(200)
})


// Client List rooms
app.get('/room', (req, res) => {
    console.log("get /room")
    const clientName = req.query.clientName
    console.log(clientName)
    console.log()
    srv.listRoom(clientName)
    res.sendStatus(200)
})

app.listen(3000, () => console.log('Example app listening on port 3000!'))
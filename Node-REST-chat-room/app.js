const express = require("express")
const bodyParser = require('body-parser');
const Server = require("./Server")
const app = express()

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

const srv = new Server()
app.post('/client', (req, res) => {
        
        const clientName = req.body.clientName
        console.log(req.body.clientName)
        if (srv.connect(clientName)){
            res.sendStatus(200)
        } else {
            res.sendStatus(409)
        }
    }
)

// Client speaks
app.post('/message', (req, res) => {
    const clientName = req.body.clientName
    const message = req.body.message
    res.sendStatus(200)
    srv.speak(clientName, message)
})

// Client Retrive, content-type: 
app.get('/message', (req, res) => {
    const clientName = req.query.clientName
    const messages = srv.retrieve(clientName)
    res.json(messages)
})

app.listen(3000, () => console.log('Example app listening on port 3000!'))
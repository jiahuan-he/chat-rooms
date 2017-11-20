const express = require("express")
const bodyParser = require('body-parser');
const Server = require("./Server")
const app = express()

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

app.post('/client', (req, res) => {
const srv = new Server()
        const clientName = req.body.name
        console.log(req.body)
        if (srv.connect(clientName)){
            res.sendStatus(200)
        } else {
            res.sendStatus(404)
        }
    }
)

app.get('/', (req, res) => {
    console.log("/get")
    res.send("hello world")
})

app.listen(3000, () => console.log('Example app listening on port 3000!'))
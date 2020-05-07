const superAgent = require('superagent')
const querystring = require('querystring');

const agent = superAgent.agent()

async function login() {
    try {
        await new Promise(resolve => agent.post('http://localhost:8080/api/session')
            .send(JSON.stringify({
                username: "user",
                password: "user"
            })).end(function (err, res) {
                resolve(res.text)
            }))

        let start = Date.now()
        let queries = []
        let purchases = []
        for (let i = 0; i < 1000; i++) {
            queries.push(query())
            // purchases.push(purchase())
        }
        let a = await Promise.all(queries)
        // let a = await Promise.all(purchases)
        // console.log(a)
        console.log(Date.now()-start)
    } catch (e) {
        console.log(e)
    }
}

async function query() {
    return new Promise(resolve => agent
        .get(trainRoute('广州', '襄阳'))
        .end((err, res) => resolve(res.text)))
}

async function purchase() {
    return new Promise(resolve => agent
        .post('http://localhost:8080/api/ticket')
        .send(JSON.stringify({
            "train": 10159,
            "seat": 1,
            "from": 836,
            "to": 42,
            "passenger": 2
        })).end((err, res) => resolve(res.text)))
}

function trainRoute() {
    return 'http://localhost:8080/api/train/from/' + querystring.escape(arguments[0]) + '/to/' + querystring.escape(arguments[1]) + '/date/2020-05-06'
}

login().then()

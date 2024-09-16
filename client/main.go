package main

import (
    "github.com/gorilla/websocket"
    "log"
    "net"
    "flag"
    "sync"
)



type Client struct {
    url string
}


func (c *Client) handleConn(local net.Conn) {
    remote, _, err := websocket.DefaultDialer.Dial(c.url, nil)
    if err != nil {
        log.Print(err)
        return
    }
    defer remote.Close()

    var wg sync.WaitGroup
    wg.Add(2)

    go func() {
        defer wg.Done()
        buf := make([]byte, 1024)
        for { 
            remoteWriter, err := remote.NextWriter(websocket.BinaryMessage)
            if err != nil {
                break
            }
            n, err := local.Read(buf)
            if err != nil {
                break
            }
            if n > 0 {
                remoteWriter.Write(buf[0:n])
            }
            remoteWriter.Close()
        }
        remote.Close()
    }()

    go func() {
        defer wg.Done()
        buf := make([]byte, 1024)
        for {
            _, remoteReader, err := remote.NextReader()
            if err != nil {
                break
            }
            n, err := remoteReader.Read(buf)
            if err != nil {
                break
            }
            if n > 0 {
                local.Write(buf[0:n])
            }
        }
        local.Close()
    }()

    wg.Wait()
}


func (c *Client) Run(listen *string) {
    ln, err := net.Listen("tcp", *listen)
    if err != nil {
        log.Fatal(err)
    }
    log.Printf("Listening on %s", *listen)
    for {
        conn, err := ln.Accept()
        if err != nil {
            continue
        }
        go c.handleConn(conn)
    }
}


func main() {
    urlPtr := flag.String("url", "",  "injected WS endpoint URL")
    listenPtr := flag.String("listen", ":8888", "listen at")
    flag.Parse()
    if len(*urlPtr) == 0 {
        flag.PrintDefaults()
        return
    }
    c := &Client{
        url: *urlPtr,
    }
    c.Run(listenPtr)
}


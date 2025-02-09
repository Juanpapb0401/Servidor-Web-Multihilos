package org.example;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);

        System.out.println("Server waiting for connection...");
        Socket socket = serverSocket.accept();
        System.out.println("Connection Accepted");

        socket.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())
        );

        bufferedWriter.write("HTTP/1.0 200 OK\r\n");
        bufferedWriter.write("Content-Type: text/html\r\n");
        bufferedWriter.write("Content-Length: 10\\r\\n");
        bufferedWriter.write("\r\n");
        bufferedWriter.write("<html><head></head><body>Hola Mundo</body></html>");
        bufferedWriter.flush();

    }
}
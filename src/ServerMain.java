package src;


import src.Controller.Repository;
import src.Controller.SocketClientHandler;
import src.Controller.Thread.EchoThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    private static final int PORT_MAIN = 8080;
    private static final int PORT_SUB = 8888;
    private ServerSocket serverSocket;
    private ServerSocket echoSocket;
    private EchoThread echoThread;

    private void run() {
        try {
            serverSocket = new ServerSocket(PORT_MAIN);
            echoSocket = new ServerSocket(PORT_SUB);
            System.out.println("<< SERVER RUNNING ON " + "[ HOST:" + serverSocket.getInetAddress() + " / PORT:" + PORT_MAIN + " / PORT(Echo):"+ PORT_SUB +" ] >>");
            while (true) {
                Socket socket = serverSocket.accept();
                Socket checkSocket = echoSocket.accept();
                SocketClientHandler socketClientHandler = new SocketClientHandler(socket);
                SocketClientHandler aliveCheck = new SocketClientHandler(checkSocket);

                System.out.println("CONNECTION FOUND    --->     " + socket.getInetAddress() + " : " + socket.getPort());

                echoThread = new EchoThread(aliveCheck);
                Repository.getInstance().connectionHandler(socketClientHandler, true);

                echoThread.start();
                socketClientHandler.start();

                if (socket.isClosed()){
                    System.out.println("[ EchoThread ("+socket.getPort()+") ] was terminated.");
                    checkSocket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                    System.out.println("<< SERVER SHUT DOWN >>");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("!! SERVER ON ERROR !!");
                }
            }
        }
    }

    public static void main(String[] args) {
        ServerMain serverMain = new ServerMain();
        Repository.getInstance();
        serverMain.run();
    }
}
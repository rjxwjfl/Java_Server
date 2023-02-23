package src.Controller.Thread;


import src.Controller.Repository;
import src.Controller.SocketClientHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EchoThread extends Thread {

    private static final long TIMEOUT = 30000;
    private final SocketClientHandler socket;
    private OutputStream out;
    private InputStream in;

    public EchoThread(SocketClientHandler socketClientHandler) {
        this.socket = socketClientHandler;
    }

    @Override
    public void run() {
        try {
            out = socket.getSocket().getOutputStream();
            in = socket.getSocket().getInputStream();
            while (true) {
                System.out.println("Check that the Client is alive.");
                byte[] buffer = new byte[1024];
                out.write("ALIVE\n".getBytes());
                out.flush();
                int n = in.read(buffer);
                Thread.sleep(TIMEOUT);
                if (n == -1 | !"ALIVE".equals(new String(buffer, 0, n).trim())){
                    Repository.getInstance().connectionHandler(socket, false);
                    System.out.println("CONNECTION TERMINATED   >>  " +
                            socket.getSocket().getInetAddress() +" : "+ socket.getSocket().getPort());
                    return;
                }
            }
        } catch (IOException | InterruptedException e) {
            Repository.getInstance().connectionHandler(socket, false);
            System.out.println("CONNECTION TERMINATED   >>  " +
                    socket.getSocket().getInetAddress() +" : "+ socket.getSocket().getPort() + "\ncause, " + e);
        } finally {
            if (!socket.getSocket().isClosed()){
                try {
                    socket.getSocket().close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

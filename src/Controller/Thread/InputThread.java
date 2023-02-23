package src.Controller.Thread;


import src.Controller.Thread.Interface.InputThreadListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

public class InputThread extends Thread {
    private InputStream inputStream;
    private final InputThreadListener listener;

    public InputThread(InputStream inputStream, InputThreadListener listener) {
        this.listener = listener;
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("Input thread is running...");
                byte[] buffer = new byte[1024];
                int a = inputStream.read(buffer);
                listener.onInput(new String(buffer, 0, a).trim());
            }
        } catch (SocketException e) {
            System.out.println("!! ERROR !!\nDETAILS : " + e);
            listener.onConnectionLost();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (StringIndexOutOfBoundsException e){
            listener.onConnectionLost();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }
}

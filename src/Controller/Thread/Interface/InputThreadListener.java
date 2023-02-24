package Controller.Thread.Interface;

public interface InputThreadListener {
    void onInput(String msg);

    void onConnectionLost();
}

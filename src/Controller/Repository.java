package src.Controller;

import src.Controller.Thread.Interface.ChangeNotifier;
import src.Model.ChatRoomModel;
import src.Model.UserModel;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

public class Repository implements ChangeNotifier {
    private static Map<Integer, SocketClientHandler> connectedSocketOnServer = new HashMap<>();
    private static Map<String, ChatRoomModel> chatRoomList = new HashMap<>(); //
    private static Repository instance = new Repository();

    private Repository() {
    }

    public static Repository getInstance() {
        if (instance == null) {
            instance = new Repository();
        }
        return instance;
    }

    public boolean lobbyManager(ChatRoomModel chat, boolean isCreate) {
        if (isCreate) {
            synchronized (chatRoomList) {
                for (Map.Entry<String, ChatRoomModel> mp : chatRoomList.entrySet()) {
                    if (mp.getKey().equals(chat.getTitle())) {
                        return false;
                        // Redundancy Check
                    }
                }
                chatRoomList.put(chat.getTitle(), chat);
            }
        } else {
            chatRoomList.remove(chat.getTitle());
        }
        changeNotify(101);
        return true;
    }


    public void chatManager(String title, UserModel user, boolean isJoining) {
        ChatRoomModel chat = chatRoomList.get(title);
        if (chat == null) return;
        List<UserModel> entry = chat.getEntry();
        if (entry == null) return;


        synchronized (entry) {
            if (isJoining) {
                entry.add(user);
                changeNotifierDtl(201, user, entry);
            } else {
                entry.remove(user);
                changeNotifierDtl(202, user, entry);
                if (chat.getHost() == user) {
                    lobbyManager(chat, false);
                    changeNotifier(301, entry);
                }
            }
        }
    }


    public List<UserModel> getChatEntry(String title) {
        if (getChatInfo(title) == null){
            return null;
        }
        List<UserModel> entry = getChatInfo(title).getEntry();
        return entry;
    }

    public ChatRoomModel getChatInfo(String title) {
        ChatRoomModel chat = chatRoomList.get(title);
        return chat;
    }

    public Map<Integer, SocketClientHandler> getConnectedSocketOnServer() {
        return connectedSocketOnServer;
    }


    public void connectionHandler(SocketClientHandler sch, boolean isAlive) {
        synchronized (connectedSocketOnServer) {
            if (!isAlive) {
                connectedSocketOnServer.remove(sch);

            } else {
                connectedSocketOnServer.putIfAbsent(sch.getSocket().getPort(), sch);
            }
        }
    }

    @Override
    public void changeNotify(int code) { // Notify All Socket
        for (Map.Entry<Integer, SocketClientHandler> sc : connectedSocketOnServer.entrySet()) {
            sc.getValue().changeNotify(code);
        }
    }

    @Override
    public void changeNotifier(int code, List<UserModel> listener) {
        if (listener.isEmpty()) {
            return;
        }
        synchronized (listener) {
            for (UserModel u : listener) {
                connectedSocketOnServer.get(u.getPORT()).changeNotify(code);
                System.out.println(u);
            }
        }
    }

    @Override
    public void changeNotifierDtl(int code, UserModel user, List<UserModel> listener) {
        if (listener.isEmpty()) {
            return;
        }
        for (UserModel u : listener) {
            connectedSocketOnServer.get(u.getPORT()).changeNotifierTg(code, user);
        }
    }

    @Override
    public void changeNotifierTg(int code, UserModel user) {

    }

    public List<String> listRefresher() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, ChatRoomModel> map : chatRoomList.entrySet()) {
            list.add(map.getValue().getTitle());
        }
        return list;
    }

}

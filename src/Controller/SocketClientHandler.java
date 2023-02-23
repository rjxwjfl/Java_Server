package src.Controller;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import src.Controller.Thread.InputThread;
import src.Controller.Thread.Interface.ChangeNotifier;
import src.Controller.Thread.Interface.InputThreadListener;
import src.Controller.Thread.Interface.OutputThreadListener;
import src.Controller.Thread.OutputThread;
import src.Model.DataTransferObject.ReqDto;
import src.Model.DataTransferObject.RespDto;
import src.Model.ChatRoomModel;
import src.Model.MFCmodel;
import src.Model.MTCmodel;
import src.Model.UserModel;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocketClientHandler implements InputThreadListener, OutputThreadListener, ChangeNotifier {
    private Socket socket;
    private UserModel user;
    private Gson gson;
    private ReqDto<?> reqDto;
    private String currentTitle;
    private List<UserModel> entry;
    private Map<String, ChatRoomModel> myChatList;
    private PrintWriter pw;

    private InputThread in;
    private OutputThread out;
    private Repository rep;


    public SocketClientHandler(Socket socket) throws IOException {
        super();
        this.socket = socket;
        this.gson = new Gson();
        this.user = new UserModel(null, socket.getPort());

        rep = Repository.getInstance();
        pw = new PrintWriter(socket.getOutputStream());
        out = new OutputThread(this);
        in = new InputThread(socket.getInputStream(), this);
        myChatList = new HashMap<>();
    }

    public void start() {
        in.start();
        out.start();
    }

    public void stop() {
        rep.connectionHandler(this, false);
        in.interrupt();
        out.interrupt();
        out.removePrintWriter();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestHandler(String req) {
        reqDto = gson.fromJson(req, ReqDto.class);
        int code = reqDto.getTransferCode();
        Object body = reqDto.getBody();

        switch (code) {
            case 1:
            case 2:
                userConnectionManager(code, body);
                break;
            default:
                userChatConnectionManager(code, body);
                break;
        }
    }

    @Override
    public void onConnectionLost() {
        System.out.println("Session was forcibly terminated.\n" + socket.getInetAddress());
        if (currentTitle != null) {
            rep.chatManager(currentTitle, user, false);
        }
        stop();
    }

    @Override
    public void onInput(String msg) {
        requestHandler(msg);
    }

    @Override
    public void onOutput(RespDto<?> respDto) {
        System.out.println("[[ RESPONSE ]]  ::  " + respDto);
        out.addPrintWriter(pw);
        String response = gson.toJson(respDto);
        out.toOutputThread(response);
    }

    private void userConnectionManager(int code, Object body) {
        if (user.getNickName() == null) {
            user = new UserModel((String) body, socket.getPort());
            System.out.println(user);
        }
        RespDto<?> connectionResp;
        switch (code) {
            case 1: // Connection success
                connectionResp = new RespDto<>(2, user);
                onOutput(connectionResp);
                changeNotify(101);
                break;
            case 2: // User disconnect
                if (rep.getChatInfo(currentTitle).getHost() == user) {
                    rep.lobbyManager(rep.getChatInfo(currentTitle), false);
                }
                onConnectionLost();
                break;
        }
    }

    private void userChatConnectionManager(int code, Object body) {
        if (user == null) {
            return;
        }
        RespDto<?> conActResp;
        switch (code) {
            case 3: // Entered chat
                currentTitle = (String) body;
                if (rep.getChatInfo(currentTitle) == null) {
                    conActResp = new RespDto<>(11, "That chatroom doesn't exist.");
                    onOutput(conActResp);
                    break;
                }
                rep.chatManager(currentTitle, user, true);
                myChatList.put(currentTitle, rep.getChatInfo(currentTitle));
                entry = rep.getChatEntry(currentTitle);
                conActResp = new RespDto<>(code, getStringNick(entry).size());
                onOutput(conActResp);
                break;
            case 4: // Left chat
                currentTitle = (String) body;
                rep.chatManager(currentTitle, user, false);
                currentTitle = null;
                conActResp = new RespDto<>(code, rep.listRefresher());
                onOutput(conActResp);
                break;
            case 5: // Create chat
                currentTitle = (String) body;
                entry = new ArrayList<>();
                entry.add(user);
                ChatRoomModel model = new ChatRoomModel(currentTitle, user, entry);
                if (rep.lobbyManager(model, true)) {
                    myChatList.put(currentTitle, rep.getChatInfo(currentTitle));
                    conActResp = new RespDto<>(code, getStringNick(myChatList.get(currentTitle).getEntry()));
                    System.out.println("\nA new chat was created.\n");
                } else {
                    conActResp = new RespDto<>(10, "\nA Chatroom with that title already exists.");
                }
                rep.changeNotify(101);
                onOutput(conActResp);
                break;
            case 6:
                LinkedTreeMap<?, ?> map = (LinkedTreeMap<?, ?>) body;
                UserModel sender = new UserModel((String) ((LinkedTreeMap<?, ?>) map.get("sender")).get("nickName"), ((Double) ((LinkedTreeMap<?, ?>) map.get("sender")).get("PORT")).intValue());
                MFCmodel msg = new MFCmodel(sender, (String) map.get("content"), (boolean) map.get("isWhisper"), null);
                messageOperator(msg, myChatList.get(currentTitle));
                break;
            case 401:
                entry = rep.getChatEntry(currentTitle);
                conActResp = new RespDto<>(code, entry == null ? 0 : getStringNick(entry).size());
                System.out.println("OUT : " + conActResp);
                onOutput(conActResp);
                break;
        }
    }

    public void messageOperator(MFCmodel msg, ChatRoomModel currentChat) {
        List<UserModel> entries = currentChat.getEntry();
        MTCmodel re = new MTCmodel(msg.getSender(), msg.getContent());
        RespDto<MTCmodel> msgOperation = new RespDto<>(7, re);
        String cvtMsg = gson.toJson(msgOperation);
        PrintWriter pw;
        try {
            for (UserModel us : entries) {
                if (us == user) {
                    continue;
                }
                pw = new PrintWriter(rep.getConnectedSocketOnServer().get(us.getPORT()).socket.getOutputStream());
                out.addPrintWriter(pw);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.toOutputThread(cvtMsg);
    }


    /*
     * ********************* State Change Code *********************
     *
     * 101 : A new chat has been added. / A chat has been removed.
     * 201, 202 : A new user entered this chat. / A user left this chat.
     * 301 : The host has left.
     * 401 : Check an entry state.
     *
     * *************************************************************
     */
    @Override
    public void changeNotify(int code) {
        RespDto<?> stateNotifier;
        switch (code) {
            case 101:
            case 301:
                stateNotifier = new RespDto<>(code, rep.listRefresher());
                onOutput(stateNotifier);
                break;
        }
    }

    @Override
    public void changeNotifierTg(int code, UserModel user) {
        RespDto<?> stateNotifier = new RespDto<>(code, user.getNickName());
        onOutput(stateNotifier);
    }

    public List<String> getStringNick(List<UserModel> user) {
        List<String> nicks = new ArrayList<>();
        user.forEach(userModel -> nicks.add(userModel.getNickName()));
        System.out.println("GET STRING NICK ---> " + nicks);
        return nicks;
    }


    public Socket getSocket() {
        return socket;
    }

    @Override
    public void changeNotifier(int code, List<UserModel> listener) {

    }

    @Override
    public void changeNotifierDtl(int code, UserModel user, List<UserModel> listener) {

    }
}


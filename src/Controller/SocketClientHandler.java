package Controller;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import Controller.Thread.InputThread;
import Controller.Thread.Interface.ChangeNotifier;
import Controller.Thread.Interface.InputThreadListener;
import Controller.Thread.Interface.OutputThreadListener;
import Controller.Thread.OutputThread;
import Model.DataTransferObject.ReqDto;
import Model.DataTransferObject.RespDto;
import Model.ChatRoomModel;
import Model.MFCmodel;
import Model.MTCmodel;
import Model.UserModel;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocketClientHandler implements InputThreadListener, OutputThreadListener, ChangeNotifier {
    private final Socket socket;
    private UserModel user;
    private final Gson gson;
    private String currentTitle;
    private final Map<String, ChatRoomModel> myChatList;
    private final PrintWriter pw;

    private final InputThread in;
    private final OutputThread out;
    private final Repository rep;


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
        ReqDto<?> reqDto = gson.fromJson(req, ReqDto.class);
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
                List<UserModel> entry = rep.getChatEntry(currentTitle);
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
        return nicks;
    }


    public Socket getSocket() {
        return socket;
    }

    @Override
    public void changeNotifier(int code, List<UserModel> listener) {}

    @Override
    public void changeNotifierDtl(int code, UserModel user, List<UserModel> listener) {}
}


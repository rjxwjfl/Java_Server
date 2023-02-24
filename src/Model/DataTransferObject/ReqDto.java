package Model.DataTransferObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ReqDto<T> {
    private int transferCode;
    private T body;

    /*
     * <*----------------Server Communication Code----------------*>
     *
     * 1 : Send a connection request to the server.
     * 2 : null
     * 3 : Send a request to enter the chat room to the server.
     * 4 : Send a chat room exit notification to the server.
     * 5 : Send a request to create a new chat room to the server.
     * 6 : Send a new message forwarding request to the server.
     *
     * ********************* State Change Code *********************
     *
     * 101 : A new chat has been added. / A chat has been removed.
     * 201 : A new user entered this chat.
     * 202 : A user left this chat.
     * 301 : The host has left.
     * 401 : Check an entry state.
     *
     * *************************************************************
     */
}
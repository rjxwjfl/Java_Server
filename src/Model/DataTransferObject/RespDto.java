package Model.DataTransferObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RespDto<T> {
    private int transferCode;
    private T body;


    /*
     * <*----------------Server Communication Code----------------*>
     *
     * 1 : Send a notification to the client that connection with a server is complete.
     * 2 : null
     * 3 : Send a response to enter the chat room to the client.
     * 4 : Send a notification to the client that the chat room has been left.
     * 5 : Send a response to create a new chat room to the client.
     * 7 : Send a new message to the client.
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

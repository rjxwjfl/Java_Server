package src.Model.DataTransferObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ReqDto<T> {
    private int transferCode;
    private T body;
}

/*

* CODE
    1 - CONNECT
    2 - DISCONNECT
    3 - JOINING CHAT
    4 - LEAVE CHAT // master left    or  member left
    5 - SEND MESSAGE
    6 - CREATE CHAT

*/
package src.Model.DataTransferObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RespDto<T> {
    private int transferCode;
    private T body;
}

/*

* CODE
    1 - CONNECTION SUCCESS
    2 - DISCONNECTED
    3 - ENTERED CHAT
    4 - LEFT CHAT // master left    or  member left
    5 - SENDING SUCCESS
    6 - CREATE CHAT
    7 - SEND WHISPER

*/


package src.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class ChatRoomModel {
    String title;
    UserModel host;
    List<UserModel> entry;
}

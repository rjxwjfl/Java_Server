package src.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class MFCmodel {
    private UserModel sender;
    private String content;
    private boolean isWhisper;
    private List<UserModel> receivers;
}

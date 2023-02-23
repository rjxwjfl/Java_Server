package src.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MTCmodel {
    private UserModel sender;
    private String contents;
}

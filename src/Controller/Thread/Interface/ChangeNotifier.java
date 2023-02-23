package src.Controller.Thread.Interface;

import src.Model.UserModel;

import java.util.List;

public interface ChangeNotifier {
    void changeNotify(int code);

    void changeNotifier(int code, List<UserModel> listener);

    void changeNotifierDtl(int code, UserModel user, List<UserModel> listener);

    void changeNotifierTg(int code, UserModel user);
}

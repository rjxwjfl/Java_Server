package src.Controller.Thread.Interface;


import src.Model.DataTransferObject.RespDto;

public interface OutputThreadListener {
    void onOutput(RespDto<?> respDto);

}

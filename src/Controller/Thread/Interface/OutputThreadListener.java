package Controller.Thread.Interface;


import Model.DataTransferObject.RespDto;

public interface OutputThreadListener {
    void onOutput(RespDto<?> respDto);

}

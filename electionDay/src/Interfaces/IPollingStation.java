package Interfaces;

public interface IPollingStation {
    boolean enterPollingStation(int voterId);
    int callNextVoter();
    // void exitPollingStation(int voterId);

    boolean waitIdValidation(int voterId);
    void sendSignal(int voterId, boolean response);

    void openPollingStation();
    void closePollingStation();

    boolean isOpen();
    // boolean stillVotersInQueue();

    int numberVotersInQueue();
}
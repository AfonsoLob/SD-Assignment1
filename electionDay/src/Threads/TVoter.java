package Threads;
import Interfaces.IExitPoll;
import Interfaces.IPollingStation;
import Interfaces.IVotingBooth;
import Logging.Logger;


public class TVoter implements Runnable {
    private int voterId;
    private final IPollingStation pollingStation;
    private final IVotingBooth votingBooth;
    private final IExitPoll exitPoll;
    private final Logger logger;
    private boolean myVote;

    private TVoter(int id, IPollingStation pollingStation, IVotingBooth votingBooth, IExitPoll exitPoll, Logger logger) {
        this.voterId = id;
        // this.name = "Voter-" + id;
        this.pollingStation = pollingStation;
        this.votingBooth = votingBooth;
        this.exitPoll = exitPoll;
        this.logger = logger;
        // this.myVote = -1;
    }
    
    public static TVoter getInstance(int id, IPollingStation pollingStation, IVotingBooth votingBooth, IExitPoll exitPoll, Logger logger) {
        return new TVoter(id, pollingStation, votingBooth, exitPoll, logger);
    }

    @Override
    public void run() {
        do {
            // Try to enter the polling station
            pollingStation.enterPollingStation(voterId);
            logger.voterEnteringQueue(voterId);

            // Validate ID
            int response = 0;
            while (response == 0) {
                // Wait for ID validation
                response = pollingStation.waitIdValidation(voterId);
            }

            if (response == 1) {
                // Cast vote
                System.out.println("Voter " + voterId + " ID validation correct!");
                
                if (Math.random() < 0.4) {
                    System.out.println(voterId + " voted for candidate A");
                    votingBooth.voteA();
                    logger.voterInBooth(voterId, true); // vote A
                    this.myVote = true; // 1 for A
                } else {
                    System.out.println(voterId + " voted for candidate b");
                    votingBooth.voteB();
                    logger.voterInBooth(voterId, false); // vote B
                    this.myVote = false; // 0 for B
                }
                // Exit polling station 
                exitPoll.exitPollingStation(voterId, myVote);

            } else{
                System.out.println("Voter " + voterId + " ID validation incorrect!");
                logger.voterExiting(voterId, false);
            }

            // Reborn with probability or exit
            if (Math.random() < 0.5) {
                int newId = voterId + 1000; // Simple way to create new ID
                System.out.println("Voter " + voterId + " reborn as " + newId);
                voterId = newId;
            } else {
                System.out.println("Voter " + voterId + " reborn with same ID");
            }
                // Wait before trying again
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }

        } while ((pollingStation.isOpen()));
        System.out.println("Voter " + voterId + " terminated");
    }
}
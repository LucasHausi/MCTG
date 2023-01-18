package org.mtcg.game;

import org.mtcg.user.User;
import org.mtcg.util.Pair;

import java.util.concurrent.BlockingQueue;

public class QueueHandler implements Runnable{
    private final BlockingQueue<User> bQPlayers;
    private final BlockingQueue<Pair<User,User>> bQGameResults;
    private final Game game = new Game();

    public QueueHandler(BlockingQueue<User> bQPlayers, BlockingQueue<Pair<User,User>> bQGameResults) {
        this.bQPlayers = bQPlayers;
        this.bQGameResults = bQGameResults;
    }

    @Override
    public void run() {
        while(true){
            try {
                User tempUser1 = (User) bQPlayers.take();
                User tempUser2 = (User) bQPlayers.take();
                User winner = game.startGame(tempUser2, tempUser1);
                System.out.println("Handler finished game");
                bQGameResults.add(new Pair<>(tempUser1, winner));
                bQGameResults.add(new Pair<>(tempUser2, winner));
               // notifyAll();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }
    }
}

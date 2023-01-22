package org.mtcg.game;

import org.mtcg.user.User;
import org.mtcg.util.Pair;
import org.mtcg.util.Tripple;

import java.util.concurrent.BlockingQueue;

public class QueueHandler implements Runnable{
    private final BlockingQueue<User> bQPlayers;
    private final BlockingQueue<Tripple<User,User, BattleLog>> bQGameResults;
    private final Game game = new Game();

    public QueueHandler(BlockingQueue<User> bQPlayers, BlockingQueue<Tripple<User,User, BattleLog>> bQGameResults) {
        this.bQPlayers = bQPlayers;
        this.bQGameResults = bQGameResults;
    }

    @Override
    public void run() {
        while(true){
            try {
                User tempUser1 = (User) bQPlayers.take();
                User tempUser2 = (User) bQPlayers.take();
                BattleLog battleLog = new BattleLog();
                User winner = game.startGame(tempUser2, tempUser1, battleLog);
                System.out.println("Handler finished game");
                battleLog.print();
                bQGameResults.add(new Tripple<>(tempUser1, winner,battleLog));
                bQGameResults.add(new Tripple<>(tempUser2, winner,battleLog));
               // notifyAll();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }
    }
}

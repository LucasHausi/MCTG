package org.mtcg.game;

import org.mtcg.cards.Card;
import org.mtcg.user.User;

import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;

public class Game {

    public void startGame()
    {
        printIntro();
        User player1 = new User("PlayerA","pwd1234");
        User player2 = new User("PlayerB","pwd1234");

        for(int i=1; i <= 100;i++)
        {
            System.out.println(("Round "+i));
            Card c1 = player1.getCardToAttack();
            Card c2 = player2.getCardToAttack();
            Card winner = c1.attack(c2);
            if(c1 == winner)
            {
                System.out.println(c1+" defeats "+c2);
                if(!player2.removeCard(c2))
                {
                    System.out.println(player1.getUsername().concat(" wins the game!"));
                    exit(0);
                }
                else {
                    player1.addCardToDeck(c2);
                }
            }
            else{
                System.out.println(c2+" defeats "+c1);
                if(!player1.removeCard(c1))
                {
                    System.out.println(player2.getUsername().concat(" wins the game!"));
                    exit(0);
                }
                else {
                    player2.addCardToDeck(c1);
                }
            }

            //get random cards from player perhaps make in-class function
            //compare their damage and also their fears and elements
            //exchange cards by trading function provide source and destination
            //check if both decks are >= 0
            //end game or start next round
        }
    }

    void printIntro()
    {
        System.out.println("" +
                "$$\\      $$\\                                $$\\                                                          $$\\           \n" +
                "$$$\\    $$$ |                               $$ |                                                         $$ |          \n" +
                "$$$$\\  $$$$ | $$$$$$\\  $$$$$$$\\   $$$$$$$\\$$$$$$\\   $$$$$$\\   $$$$$$\\   $$$$$$$\\$$$$$$\\   $$$$$$\\   $$$$$$$ | $$$$$$$\\ \n" +
                "$$\\$$\\$$ $$ |$$  __$$\\ $$  __$$\\ $$  _____\\_$$  _| $$  __$$\\ $$  __$$\\ $$  _____\\____$$\\ $$  __$$\\ $$  __$$ |$$  _____|\n" +
                "$$ \\$$$  $$ |$$ /  $$ |$$ |  $$ |\\$$$$$$\\   $$ |   $$$$$$$$ |$$ |  \\__|$$ /     $$$$$$$ |$$ |  \\__|$$ /  $$ |\\$$$$$$\\  \n" +
                "$$ |\\$  /$$ |$$ |  $$ |$$ |  $$ | \\____$$\\  $$ |$$\\$$   ____|$$ |      $$ |    $$  __$$ |$$ |      $$ |  $$ | \\____$$\\ \n" +
                "$$ | \\_/ $$ |\\$$$$$$  |$$ |  $$ |$$$$$$$  | \\$$$$  \\$$$$$$$\\ $$ |      \\$$$$$$$\\$$$$$$$ |$$ |      \\$$$$$$$ |$$$$$$$  |\n" +
                "\\__|     \\__| \\______/ \\__|  \\__|\\_______/   \\____/ \\_______|\\__|       \\_______\\_______|\\__|       \\_______|\\_______/ \n");

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}

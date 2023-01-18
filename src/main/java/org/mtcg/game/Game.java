package org.mtcg.game;

import org.mtcg.cards.Card;
import org.mtcg.user.Deck;
import org.mtcg.user.User;
import org.mtcg.util.Pair;

import java.util.concurrent.TimeUnit;

public class Game {
    public User startGame(User player1, User player2)
    {
        Deck deck1 = player1.getDeck();
        Deck deck2 = player2.getDeck();
        for(int i=1; i <= 100;i++)
        {
            System.out.println(("Round "+i));
            Card c1 = deck1.getCardToAttack();
            Card c2 = deck2.getCardToAttack();

            Card winner;
            if(i%2==0)
            {
                System.out.println(player1.getUsername()+" attacks "+ player2.getUsername());
                winner = c1.attack(c2);
            }
            else{
                System.out.println(player2.getUsername()+" attacks "+ player1.getUsername());
                winner = c2.attack(c1);
            }

            if(c1 == winner)
            {
                System.out.println(c1+" defeats "+c2);
                if(!deck2.removeCardFromDeck(c2))
                {
                    System.out.println(player1.getUsername().concat(" wins the game!"));
                    player1.win();
                    player2.loose();
                    clearDecks(deck2,deck1);
                    return player1;
                }
                else {
                    player1.addCardToDeck(c2);
                }
            }
            else if(c2 == winner){
                System.out.println(c2+" defeats "+c1);
                if(!player1.removeCardFromDeck(c1))
                {
                    System.out.println(player2.getUsername().concat(" wins the game!"));
                    player2.win();
                    player1.loose();
                    clearDecks(deck2,deck1);
                    return player2;
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
        System.out.println("It's a draw!");
        clearDecks(deck2,deck1);
        return null;
    }
    void clearDecks(Deck deck1, Deck deck2){
        deck1.clearDeck();
        deck2.clearDeck();
    }
}

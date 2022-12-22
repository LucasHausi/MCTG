package org.mtcg.User;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.mtcg.Cards.Card;

import java.util.*;

public class User {
    private String username;
    private String password;
    private int coins;
    private Deck deck;

    private Stack cardStack;

    @JsonCreator
    public User(@JsonProperty("Username") String username, @JsonProperty("Password")  String password) {
        this.username = username;
        this.password = password;
        this.coins = 20;
        this.cardStack = new Stack();
        this.deck = new Deck();
        //this.chooseDeck();
    }

    public String getPassword() {
        return password;
    }

    public void copyCardsToDeck(int[] cardIndexes){
        for(int index : cardIndexes)
        {
            Card c = this.cardStack.getCard(index);
            this.deck.addCard(c);
        }
        for (int i=0; i < cardIndexes.length; i++)
        {
            Card c = this.cardStack.getCard(cardIndexes[i]-i);
            this.cardStack.removeCard(c);
        }
    }

    public void chooseDeck()
    {
        boolean finished = false;
        System.out.println(this.username.concat(" please choose the cards for your deck: "));
        this.cardStack.printStack();

        while(!finished) {
            Scanner myScan = new Scanner(System.in);
            System.out.print("Your selection: ");
            String input = myScan.nextLine();

            if(this.assertInput(input))
            {
                finished = true;
                this.processDeckSelection(input);
            }
        }
    }
    void processDeckSelection(String input)
    {
        String[] splitInput = input.split("\\s");
        int[] cardIndexes = Arrays.stream(splitInput).mapToInt(Integer::parseInt).toArray();
        this.copyCardsToDeck(cardIndexes);
    }
    boolean assertInput(String input)
    {
        String[] splitInput = input.split("\\s");

        //check for if enough cards are selected
        if(splitInput.length < 4)
        {
            System.out.println("You did not provide enough Cards try again");
            return false;
        }
        //check if the input is all numbers
        for(String splitElement : splitInput)
        {
            try{
                Integer.parseInt(splitElement);
            }catch (NumberFormatException ex1)
            {
                System.out.println(splitElement.concat(" is not a Number"));
                return false;
            }
        }
        //check if the numbers are in the correct selection range
        for(String splitElement : splitInput)
        {
            int intElement = Integer.parseInt(splitElement);
            if(intElement >= this.cardStack.getStackSize() || intElement < 0){
                System.out.println(("The provided number ").concat(splitElement).concat(" does not correlate to a card"));
                return false;
            }
        }
        //check if there are duplicates
        //Sets do not allow duplicate entries, so check if adding to one is possible
        Set<String> set = new HashSet<String>();
        for(String splitElement : splitInput){
            if(set.add(splitElement)== false){
                System.out.println("You can choose a card only one time");
                return false;
            }
        }
        return true;
    }

    public Card getCardToAttack()
    {
        Random rand = new Random();
        int randIndex = rand.nextInt(0,this.deck.getDeckSize());
        Card c = this.deck.getCard(randIndex);
        return c;
    }
    public boolean removeCard(Card c){
        return this.deck.removeCard(c);
    }
    public void addCardToDeck(Card c){
        this.deck.addCard(c);
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // DEV Functions:
    public void printDeckSize()
    {
        System.out.println(this.username+" Deck Size: "+this.deck.getDeckSize());
    }
}

package org.mtcg.User;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.mtcg.Cards.Card;
import org.mtcg.Cards.Package;

import java.util.*;

public class User {
    private String username;
    private String password;
    private int coins;
    private Deck deck;
    private Stack cardStack;
    private String nickname;
    private String bio;
    private String image;

    @JsonCreator
    public User(@JsonProperty("Username") String username, @JsonProperty("Password") String password) {
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


    void processDeckSelection(String input) {
        String[] splitInput = input.split("\\s");
        int[] cardIndexes = Arrays.stream(splitInput).mapToInt(Integer::parseInt).toArray();
        this.copyCardsToDeck(cardIndexes);
    }

    boolean assertInput(String input) {
        String[] splitInput = input.split("\\s");

        //check for if enough cards are selected
        if (splitInput.length < 4) {
            System.out.println("You did not provide enough Cards try again");
            return false;
        }
        //check if the input is all numbers
        for (String splitElement : splitInput) {
            try {
                Integer.parseInt(splitElement);
            } catch (NumberFormatException ex1) {
                System.out.println(splitElement.concat(" is not a Number"));
                return false;
            }
        }
        //check if the numbers are in the correct selection range
        for (String splitElement : splitInput) {
            int intElement = Integer.parseInt(splitElement);
            if (intElement >= this.cardStack.getStackSize() || intElement < 0) {
                System.out.println(("The provided number ").concat(splitElement).concat(" does not correlate to a card"));
                return false;
            }
        }
        //check if there are duplicates
        //Sets do not allow duplicate entries, so check if adding to one is possible
        Set<String> set = new HashSet<>();
        for (String splitElement : splitInput) {
            if (set.add(splitElement) == false) {
                System.out.println("You can choose a card only one time");
                return false;
            }
        }
        return true;
    }

    public Card getCardToAttack() {
        Random rand = new Random();
        int randIndex = rand.nextInt(0, this.deck.getDeckSize());
        return this.deck.getCard(randIndex);
    }

    public boolean removeCard(Card c) {
        return this.deck.removeCard(c);
    }

    public void addCardToDeck(Card c) {
        this.deck.addCard(c);
    }

    public String getUsername() {
        return username;
    }

    public void setDeck(List<UUID> cardIDs) {
        for (UUID id : cardIDs) {
            Card c = this.cardStack.getCard(id);
            if (c != null) {
                this.deck.addCard(c);
                this.cardStack.removeCard(c);
            }

        }
    }

    public void acquirePackage(Package p) {
        boolean errWhenAddingCards = false;
        //variable to store the amount of successful purchased cards
        int succBought = 0;
        //add all Cards to Stack
        for (Card c : p.getCards()) {
            //check if the card could be added to the stack
            if (this.cardStack.addCard(c)) {
                succBought++;
            } else {
                errWhenAddingCards = true;
            }
        }

        if (errWhenAddingCards) {
            System.out.println(5 - succBought + " cards could not be added because the id was taken already");
        } else {
            System.out.println("The user " + username + " successfully acquired a package");
        }
        this.coins -= succBought;
    }

    public boolean hasEnoughMoney() {
        if (this.coins >= 5) {
            return true;
        } else {
            return false;
        }
    }

    //simplify this methods call somehow??
    public void printStack() {
        this.cardStack.printStack();
    }

    public void printDeck() {
        System.out.println("This is " + username + "'s deck: ");
        this.deck.printDeck("normal");
    }
    public void printDeckPlain(){
        System.out.println("Plain deck view");
        this.deck.printDeck("plain");
    }
    public void printUserData(){
        System.out.println("Name: "+ this.username+"\nBio: "+this.bio+"\nImage: "+this.image);
    }
    public void setUserData(String name, String bio, String image){
        this.nickname=name;
        this.bio=bio;
        this.image=image;
    }

    // DEV Functions:
    public void printDeckSize() {
        System.out.println(this.username + " Deck Size: " + this.deck.getDeckSize());
    }

    public void copyCardsToDeck(int[] cardIndexes) {
        for (int index : cardIndexes) {
            Card c = this.cardStack.getCard(index);
            this.deck.addCard(c);
        }
        for (int i = 0; i < cardIndexes.length; i++) {
            Card c = this.cardStack.getCard(cardIndexes[i] - i);
            this.cardStack.removeCard(c);
        }
    }

    public void chooseDeck() {
        boolean finished = false;
        System.out.println(this.username.concat(" please choose the cards for your deck: "));
        this.cardStack.printStack();

        while (!finished) {
            Scanner myScan = new Scanner(System.in);
            System.out.print("Your selection: ");
            String input = myScan.nextLine();

            if (this.assertInput(input)) {
                finished = true;
                this.processDeckSelection(input);
            }
        }
    }
}

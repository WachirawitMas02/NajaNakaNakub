/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modifiers;

import GameModel.Hand;
import GameModel.PokerHandType;

// A Hero is the player's chosen character for the run: a single persistent
// passive ability picked once at the start of a new game.
public class Hero implements Modifier {

    // A small functional hook so HeroFactory can define many distinct
    // passives (e.g. "bonus mult if hand contains an Ace") without subclassing.
    @FunctionalInterface
    public interface HeroEffect {
        double apply(double currentMult, Hand hand, PokerHandType handType);
    }

    private final String name;
    private final String title;
    private final String description;
    private final HeroEffect effect;
    private final String IMGpath;

    public Hero(String name, String title, String description, HeroEffect effect) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.effect = effect;
        this.IMGpath = "/Assets/Heroes/" + name.toLowerCase().replace(" ", "_")+ ".png";
    }

    @Override
    public String getName() {
        return name + " the " + title;
    }

    @Override
    public String getDescription() {
        return description;
    }
    
    public String getTitle(){
        return title;
    }
    public String getNName(){
        return name;
    }

    @Override
    public double modifyMult(double currentMult, Hand hand, PokerHandType handType) {
        return effect.apply(currentMult, hand, handType);
    }
    
    public String getimgpath(){
        return IMGpath;
    }

    @Override
    public String toString() {
        return getName();
    }
}

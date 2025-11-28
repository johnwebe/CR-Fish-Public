package com.clashroyale;

import java.util.ArrayList;
import java.util.Collections;

public class Battles{
    private int[] time;
    private String type;
    private long crowns;
    private long kingTowerHealth;
    private long princessTowersHitPoints[] = new long[2];
    private double elixirLeaked;
    //opponent level
    private int level;
    private ArrayList<Cards> cardArray = new ArrayList<>();
    private Battles opponentBattle;
    private String name; 
    private String tag;
    private long[][] opponentRankings;
    private double pressure;
    public Battles(){
        type = "";
        crowns = 0L;
        kingTowerHealth = 0L;
        princessTowersHitPoints[0] = 0L;
        princessTowersHitPoints[1] = 0L;
        elixirLeaked = 0;
        level = 0;        
    }
    
    public void setBattles(String type, long crowns, long kingTowerHealth, long princessTowersHitPoints[], double elixirLeaked, String time){
        this.type = type;
        this.crowns = crowns;
        this.kingTowerHealth = kingTowerHealth;
        this.princessTowersHitPoints[0] = princessTowersHitPoints[0];
        this.princessTowersHitPoints[1] = princessTowersHitPoints[1];
        this.elixirLeaked = elixirLeaked;
        this.time = timeConversion(time);

    }
    public void addCards(String name, long level, long evolutionLevel, String rarity){
        Cards card = new Cards(name, level, evolutionLevel, rarity);
        cardArray.add(card);
    }
    public void opponentStats(Battles opponent, String tag, String name){
        opponentBattle = opponent;
        opponentBattle.setName(name);
        opponentBattle.setTag(tag);
    }
    public int[] timeConversion(String time) {
        return new int[] {Integer.parseInt(time.substring(0,4)), Integer.parseInt(time.substring(4,6)),
        Integer.parseInt(time.substring(6,8)), Integer.parseInt(time.substring(9,11)), Integer.parseInt(time.substring(11,13))};
    }
    public int[] returnTime(){return time;}
    public String returnName(){return name;}
    public String returnTag(){return tag;}
    public Battles getOpponentBattle(){return opponentBattle;}
    public long[][] getOpponentRanking(){return opponentRankings;}
    public double getPressure(){return pressure;}
    public int getLevel(){return level;}
    public String getType(){return type;}
    public ArrayList<Cards> getCardsList(){return cardArray;}
    public double[] getDeckMR(){
        //median range
        double[] deckMR = new double[2];
        ArrayList<Double> mode = new ArrayList<>();
        for (Cards i : cardArray){
            deckMR[0] += i.getCardLevel();
            mode.add((double)i.getCardLevel());
        }
        Collections.sort(mode);
        deckMR[0] = deckMR[0] / 8;
        deckMR[1] = mode.get(7) - mode.get(0);
        return deckMR;
    }
    public long returnCrowns(){return crowns;}
    public void setName(String name){this.name = name;}
    public void setTag(String tag){this.tag = tag;}
    public void setOpponentRanking(long[][] opponentRankings){
        this.opponentRankings = opponentRankings;
        level = (int) opponentRankings[4][0];
    }
    public void setPressure(double p){pressure = p;}
    public String toString(){
        return type + " crowns " + crowns + " king tower " + kingTowerHealth + " leaked " + elixirLeaked + " princess1 " + princessTowersHitPoints[0] + " princess2 " + princessTowersHitPoints[1];
    }
    public String opponentToString(){
        return name + " " + tag +  " " + toString();
    }

    public class Cards{
        private String cardName;
        private long cardLevel;
        private long evolutionLevel;
        private String rarity;

        public Cards(){
            cardName = " ";
            cardLevel = 0L;
            evolutionLevel = 0L;
            rarity = " ";
        }

        public Cards(String cardName, long cardLevel, long evolutionLevel, String rarity){
            this.cardName = cardName;
            this.cardLevel = convertToStandardLevel(rarity, cardLevel);
            this.evolutionLevel = evolutionLevel;
            this.rarity = rarity;
        }

        public String toString(){
            return cardName + " " + cardLevel + " " + evolutionLevel + " " + rarity;
        }

        public long getCardLevel(){return cardLevel;}
        public long getCardEvolutionLevel(){return evolutionLevel;}
        
        public long convertToStandardLevel(String rar, long cardLevel){
            if (rar.equals("rare")){
                return cardLevel + 2L;
            } else if (rar.equals("epic")){
                return cardLevel + 5L;
            } else if (rar.equals("legendary")){
                return cardLevel + 8L;
            } else if (rar.equals("champion")){
                return cardLevel + 10L;
            }
            return cardLevel;
        }
    }
}

package com.clashroyale;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import static java.util.Map.entry;

import org.json.simple.JSONArray;

import com.opencsv.exceptions.CsvValidationException;

 
public class ClanDatabase {
    
    /* There is 3 sections
     * 1. Constructor/setters/toString
     * 2. Methods that relate to sending or compiling the data
     * 3. Player class
     */
    
    // Section 1
    private ArrayList<Player> playerDatabase = new ArrayList<Player>();

    public ClanDatabase() throws Exception{}
    public ClanDatabase(JSONArray warMembers, JSONArray allMembers) throws Exception{
        //allMembers have the correct people in a clan. Use that to check for people that aren't in the clan. warMembers have the incorrect information
        Iterator curMemsItr = allMembers.iterator(); 
        while(curMemsItr.hasNext()){
            Map member = (Map) curMemsItr.next();
            String name = (String) member.get("name");
            String role = (String) member.get("role");
            String tempTag = (String) member.get("tag");
            String tag = tempTag.substring(1);
            int exp = Math.toIntExact( (long) member.get("expLevel"));
            int fame = 0;
            int decksUsed = 0;
            Player player = new Player(tag, name, role, exp, fame, decksUsed);
            playerDatabase.add(player);
        }
        
    }
    
    public void setPlayerFame() throws CsvValidationException, IOException{
        //This portion right here takes past fame from a CSV and sets the players fame going back 10 weeks
        //This can probably be done in a much faster way but i'm too lazy to set it up. Runtime isn't a problem rn
        CSVHandler warHistoryCSV = new CSVHandler();
        //warHistoryCSV.printCSV();
        //size of the csv should be 142 (for the CSV that uses /war/analysis)
        ArrayList<String> warHistoryArray = new ArrayList<String>(warHistoryCSV.getCSV());
        ArrayList<String> currentFameArray = new ArrayList<String>(warHistoryCSV.getCurrentCSV());
        for(int i = 0; i < playerDatabase.size();i++){
            for(String j: warHistoryArray){
                if(j.contains(playerDatabase.get(i).getTag())){
                    //infoarray length should be 51 
                    String[] infoArray = j.split("[\\s]");
                    //the fame starts at index 11 and skips every 4 steps
                    int[] fameArray = new int[11];
                    fameArray[0] = 0;
                    int k = 1;
                    int m = 11;
                    while(k < 11){
                        fameArray[k] = Integer.parseInt(infoArray[m]);
                        //System.out.println(m);
                        k++;
                        m+=4;
                    }
                    //System.out.println(infoArray[0] + " "+ infoArray[1] +" "+ infoArray[2] + " "+ infoArray[8] + " " +infoArray[11] + " " +infoArray[15] + " " +infoArray[19]);
                    playerDatabase.get(i).setFame(fameArray);
                }
            }
        }
        //updates current fame
        for (int i = 0; i < playerDatabase.size();i++){
            for (String j: currentFameArray){
                if(j.contains(playerDatabase.get(i).getTag())){
                    String[] infoArray = j.split("[\\s]");
                    //System.out.println(infoArray[1]);
                    playerDatabase.get(i).setCurrentFame(Integer.parseInt(infoArray[1]));
                }
            }
        }
    }

    public void setPlayerRanking(APIHandler handler, ExecutorService childThreads) throws Exception{

        // for(Player i: playerDatabase){
        //     i.setRanking(handler.returnPlayerRankings(i.getTag())); 
        // }
        CompletableFuture<Void> future0 = CompletableFuture.runAsync(()-> {
            PlayerRankingThread thread0 = new PlayerRankingThread(handler, playerDatabase.subList(0, playerDatabase.size()/4));
            thread0.run();
        }, childThreads);
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(()-> {
            PlayerRankingThread thread1 = new PlayerRankingThread(handler, playerDatabase.subList(playerDatabase.size()/4, 2*playerDatabase.size()/4));
            thread1.run();
        }, childThreads);
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(()-> {
            PlayerRankingThread thread2 = new PlayerRankingThread(handler, playerDatabase.subList(2*playerDatabase.size()/4, 3*playerDatabase.size()/4));
            thread2.run();
        }, childThreads);
        CompletableFuture<Void> future3 = CompletableFuture.runAsync(()-> {
            PlayerRankingThread thread3 = new PlayerRankingThread(handler, playerDatabase.subList(3*playerDatabase.size()/4, playerDatabase.size()));
            thread3.run();
        }, childThreads);
        
        CompletableFuture.allOf(future0,future1,future2,future3).join();

    }

    public void getPlayerWarHistory(APIHandler handler, ExecutorService childThreads) throws Exception{
        // for(Player i: playerDatabase){
        //     //System.out.println(i.getName());
        //     handler.warOpponentsArray(i.getTag(), i);
        //     //i.battlesToString();
        // }
        CompletableFuture<Void> future0 = CompletableFuture.runAsync(()-> {
            WarHistoryThread thread0 = new WarHistoryThread(handler, playerDatabase.subList(0, playerDatabase.size()/4));
            thread0.run();
        }, childThreads);
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(()-> {
            WarHistoryThread thread1 = new WarHistoryThread(handler, playerDatabase.subList(playerDatabase.size()/4, 2*playerDatabase.size()/4));
            thread1.run();
        }, childThreads);
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(()-> {
            WarHistoryThread thread2 = new WarHistoryThread(handler, playerDatabase.subList(2*playerDatabase.size()/4, 3*playerDatabase.size()/4));
            thread2.run();
        }, childThreads);
        CompletableFuture<Void> future3 = CompletableFuture.runAsync(()-> {
            WarHistoryThread thread3 = new WarHistoryThread(handler, playerDatabase.subList(3*playerDatabase.size()/4, playerDatabase.size()));
            thread3.run();
        }, childThreads);

        CompletableFuture.allOf(future0,future1,future2,future3).join();
        
    }

    public void getOpponentRankings(APIHandler handler, ExecutorService childThreads) throws Exception {
        
        CompletableFuture<Void> future0 = CompletableFuture.runAsync(()-> {    
            OpponentRankingThread thread0 = new OpponentRankingThread(handler, playerDatabase.subList(0, playerDatabase.size()/5));
            try {
                thread0.run();
            } catch (Exception e) {
            }
        }, childThreads);
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(()-> {
            OpponentRankingThread thread1 = new OpponentRankingThread(handler, playerDatabase.subList(playerDatabase.size()/5, 2* playerDatabase.size()/5));
            try {
                thread1.run();
            } catch (Exception e) {
            }
        }, childThreads);
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(()-> {
            OpponentRankingThread thread2 = new OpponentRankingThread(handler, playerDatabase.subList(2*playerDatabase.size()/5, 3*playerDatabase.size()/5));
            try {
                thread2.run();
            } catch (Exception e) {
            }
        }, childThreads);
        CompletableFuture<Void> future3 = CompletableFuture.runAsync(()-> {
            OpponentRankingThread thread3 = new OpponentRankingThread(handler, playerDatabase.subList(3*playerDatabase.size()/5, 4 * playerDatabase.size()/5));
            try {
                thread3.run();
            } catch (Exception e) {
            }
        }, childThreads);
        CompletableFuture<Void> future4 = CompletableFuture.runAsync(()-> {    
            OpponentRankingThread thread0 = new OpponentRankingThread(handler, playerDatabase.subList(4*playerDatabase.size()/5, playerDatabase.size()));
            try {
                thread0.run();
            } catch (Exception e) {
            }
        }, childThreads);
        

        CompletableFuture.allOf(future0,future1,future2,future3,future4).join();
 
    }
    /* Pressure is based on 3 things
     * 1. Crowns earned
     * 2. Level Disparity
     * 3. Skill Disparity
     * 2 and 3 are independent variables whilst crowns is dependent. 
     * Levels have the most impact on gameplay. Levels can be broken down into 2 categories; card and king tower level.
     * Card levels have the most impact on gameplay with the king tower having a big impact too, but you would have a better chance of winning a game with higher level cards.
     * First thing is that we need to check the level disparty between decks. We need the mean and range of the cards between the decks. 
     * Second thing is that we need to check the level disparty between towers. 
     * Onto skill disparty. There are 2 kinds of rankings, trophy road and POL rankings. Trophy rankings have less of an impact on actual performance vs POL rankings
     * Trophy Disparty: < 100 doesn't make much a difference. < 200 A bit of a difference.  < 300-500 There is a good skill difference.  > 600 Big Skill difference
     * Another thing to consider is the trophy levels in relation to POL. 9k is the equivilant of a champion-grand champ in POL. If the player <= champion, then focus on the trophy count.
     * If a person is >= grand Champ, then focus on the POL score. We are looking at these rankings to then be judged by ACS (actual skill score)
     * ACS is determined by the highest ranking a person has in either POL or Trophy road, tounament finishes (if the player has any), and if they completed grand, classic, or 20 win challenges. 
     * Overall score is determined by the amount of crowns earned. More crowns earned means that the battle wasn't that hard. Most amount you can earn is 3. Least amount is 1 (assuming no draw)
     */
    public void calculatePressure(Player player){     
        for(Battles[] i : player.getBattles()){
            //This stores the pressure per battle. We will take the avg of the pressure to give us a total pressure
            ArrayList<Integer> pressure = new ArrayList<>();
            //kind of explains itself
            double kingLevelDisparity = (double) (expToKingLevel(player.getLevel()) - expToKingLevel(i[0].getOpponentBattle().getLevel()));
            //{Current arena, UC Score}, {last arena, UC Score}, {best arena, UC Score}
            double polDisparity[][] = {{player.getRanking()[0][0] - i[0].getOpponentBattle().getOpponentRanking()[0][0], player.getRanking()[0][1] - i[0].getOpponentBattle().getOpponentRanking()[0][1]},
            {player.getRanking()[1][0] - i[0].getOpponentBattle().getOpponentRanking()[1][0],player.getRanking()[1][1] - i[0].getOpponentBattle().getOpponentRanking()[1][1]},
            {player.getRanking()[2][0] - i[0].getOpponentBattle().getOpponentRanking()[2][0],player.getRanking()[2][1] - i[0].getOpponentBattle().getOpponentRanking()[2][1]}};
            //Section on trophies
            double trophyDisparity = 0;
            //if player is above 10k but the opponent is below 10k, adjust player trophies
            //if both are above 10k, then adjust for both
            //if opponent is above 10k but the player is below, adjust opponent trophies
            //if both are below 10k, no adjustment is needed. 
            if (player.getRanking()[3][0] >= 10000 && i[0].getOpponentBattle().getOpponentRanking()[3][0] < 10000) {
                trophyDisparity = (((player.getRanking()[3][0] - 10000) / 150) + player.getRanking()[3][0]) - i[0].getOpponentBattle().getOpponentRanking()[3][0];
            } else if (player.getRanking()[3][0] >= 10000 && i[0].getOpponentBattle().getOpponentRanking()[3][0] >= 10000) {
                trophyDisparity = (((player.getRanking()[3][0] - 10000) / 150) + player.getRanking()[3][0]) - (((i[0].getOpponentBattle().getOpponentRanking()[3][0] - 10000) / 150) + i[0].getOpponentBattle().getOpponentRanking()[3][0]);
            } else if (player.getRanking()[3][0] < 10000 && i[0].getOpponentBattle().getOpponentRanking()[3][0] >= 10000) {
                trophyDisparity = player.getRanking()[3][0] - (((i[0].getOpponentBattle().getOpponentRanking()[3][0] - 10000) / 150) + i[0].getOpponentBattle().getOpponentRanking()[3][0]);
            } else {
                trophyDisparity = player.getRanking()[3][0] - i[0].getOpponentBattle().getOpponentRanking()[3][0];
            } 
            //Per battle pressure
            double deckLevelMultiplier[] = new double[i.length];
            double crownsEarned[][] = new double[i.length][2];
            for(int j = 0; j < i.length; j++){
                //this will have at first a value of one, one means it is a fair match, then the deck level disparity, crowns, trophy, king level, and then pol ranking can have weights that can multiply the overall weight
                crownsEarned[j][0] = i[j].returnCrowns();
                crownsEarned[j][1] = i[j].getOpponentBattle().returnCrowns();
                //First index is mean and second is the range
                //mean should probably have more weight than the range
                double[] deckLevelDisparity = new double[]{i[j].getOpponentBattle().getDeckMR()[0] - i[j].getDeckMR()[0], i[j].getOpponentBattle().getDeckMR()[1] - i[j].getDeckMR()[1]};
                if (deckLevelDisparity[0] >= 3) {
                    deckLevelMultiplier[j] = 3;
                } else if (deckLevelDisparity[0] < -3){
                    deckLevelMultiplier[j] = 0.1;
                } else if (deckLevelDisparity[0] == 0){
                    deckLevelMultiplier[j] = 1;
                } else if (deckLevelDisparity[0] > 0) {
                    deckLevelMultiplier[j] = (1/4*deckLevelDisparity[0]*deckLevelDisparity[0])+1;
                } else if (deckLevelDisparity[0] < 0) {
                    deckLevelMultiplier[j] = (-1/9*deckLevelDisparity[0]*deckLevelDisparity[0]) + 1;
                }
                

            }
            

        }
        
    }
    //find a function that can do this better i'm too lazy for this rn 
    public int expToKingLevel(int exp) {
        if (exp <= 4){
            return exp;
        } else if (exp <= 6) {
            return 4;
        } else if (exp <= 9) {
            return 5;
        } else if (exp <= 13) {
            return 6;
        } else if (exp <= 17) {
            return 7;
        } else if (exp <= 21) {
            return 8;
        } else if (exp <= 25) {
            return 9;
        } else if (exp <= 29) {
            return 10;
        } else if (exp <= 33) {
            return 11;
        } else if (exp <= 37) {
            return 12;
        } else if (exp <= 41) {
            return 13;
        } else if (exp <= 53) {
            return 14;
        }
        return 15;
    }
  
    public String toString(){
        String str = "";
        System.out.println("Size of database: "+playerDatabase.size());
        for(Player player : playerDatabase){
            str += player + "\n";
        }
        return str;
    }

    public ArrayList<Player> returnPlayerDatabase(APIHandler handler, ExecutorService childThreads){

        return playerDatabase;
    }

    //Section 2


    //needs to be reworked
    public ArrayList<Player> rankPlayersByFame(){
        ArrayList<Player> rankPlayerFame = new ArrayList<>(playerDatabase);
        Collections.sort(rankPlayerFame, new SortByFame());
        //while(top15.size() > 50) { top15.remove(top15.size() - 1); }
        
        return rankPlayerFame;
    }
  

    //needs to be reworked
    public ArrayList<Player> getFourWeekAverage(){
        ArrayList<Player> temp = new ArrayList<>(playerDatabase);
        Collections.sort(temp, new SortByFourWeekAverage()); 
        ArrayList<Player> rankFourWeekFame = new ArrayList<>();
        for(Player player: temp){
            if(onFourWeekList(player)){
                rankFourWeekFame.add(player);
            }
        }
        return rankFourWeekFame;
    }
    //rework
    public ArrayList<Player> getLazyPlayers(){
        ArrayList<Player> lazy = new ArrayList<Player>();
        for(Player player : playerDatabase){
            if(player.getDecksUsed() < 4) { lazy.add(player); }
        }
        Collections.sort(lazy, new SortByDecksUsed());
        return lazy;
    }
    
    public boolean onFourWeekList(Player player){
        boolean areThey = true;
        int count = 0;
        int[] fameArray = player.getFameArray();
        for(int i = 0; i < 4; i++){
            if(fameArray[i] == 0){
                System.out.println("Player " + player.getName() + " has a fame of " + fameArray[i]);
                count++;
            }
        }
        if(count >= 2){
            System.out.println("Player " + player.getName() + " was removed");
            return false;
        }
        return areThey;
    }

    
   // Section 3
   public class Player{
        //there is more attibutes that need to be added
        private String name;
        private int expLevel; 
        private int[] fame = {0,0,0,0,0,0,0,0,0,0,0};
        private int decksUsed;
        private String role;
        private String tag;
        private int strike = 0;
        //[0] = Current rating; [1] = Best rating; [2] = Last season rating; [3] = Trophies
        private long ranking[][];
        private ArrayList<Battles[]> battleHistory = new ArrayList<>();
        private double pressure = 0;
        

        public Player(String t, String n, String r, int exp, int f, int d){
            name = n;
            role = r;
            expLevel = exp; 
            fame[0] = f;
            decksUsed = d;
            tag = t;
        }

        public void setPlayer(String n, String r, int exp, int f, int d){
            name = n;
            role = r;
            expLevel = exp; 
            fame[0] = f;
            decksUsed = d;
            fame[0] = f;
        }
        //Get Section. Please leave a space after the last get method
        public String getName() { return name; }
        public String getRole() { return role; }
        public int getFame() { return fame[0]; }
        public int getDecksUsed(){ return decksUsed; }
        public int getStrikes() { return strike; }
        public int getFameAvg(){ return (fame[0] + fame[1]+ fame[2] + fame[3]) / 4; }
        public int get3DayFameAvg(){ return ((fame[0])+ (fame[1])  + fame[2]) / 3; }
        public String getTag() { return tag; }
        public int[] getFameArray(){ return new int[] {fame[0], fame[1] , fame[2] , fame[3]}; }
        public long getBestRanking(){return ranking[1][2];}
        public long[][] getRanking(){return ranking;}
        public ArrayList<Battles[]> getBattles() {return battleHistory;}
        public void battlesToString(){
            for(Battles[] i: battleHistory){
                for(Battles j: i){
                    System.out.println(j.toString());
                }
            }
        }
        public double getPressure() {return pressure;}
        public int getLevel(){return expLevel;}
        

        //Set section
        public void setFame(int[] f){ fame = f; }
        public void setCurrentFame(int f) {fame[0] = f;}
        public void addStrike() { strike++; }
        public void clearStrikes(){ strike = 0;}
        public void setRanking(long[][] r) { ranking = r;}
        public void setPressure(double p) {pressure = p;}

        //Add section
        public void addBattle(Battles[] battle){battleHistory.add(battle);}
        

        public String toString(){
            //return tag + " " + name + " " + role + " " + "expLevel: " + expLevel + " " + "fame1: " + fame[0] + " "+ "fame2: " + fame[1] + "  decks: " + decksUsed;
            return tag + " " + name + " "+ "fame1: " + fame[0] + " "+ " fame2: " + fame[1] + " fame3: " + fame[2] + " "+ " fame4: " + fame[3] + " fame5: " + fame[4] + " "+ " fame6: " + fame[5] + " fame7: " + fame[6] + " "+ " fame8: " + fame[7] + " fame9: " + fame[8] + " fame10: " + fame[9] + " fame11: "+ fame[10];
            
        }

        
  
    }
    class SortByFame implements Comparator<Player>{
        public int compare(Player a, Player b){
            return b.fame[0] - a.fame[0];
        }
    }

    class SortByExp implements Comparator<Player>{
        public int compare(Player a, Player b){
            return b.expLevel - a.expLevel;
        }
    }
    //added
    class SortByDecksUsed implements Comparator<Player>{
        public int compare(Player a, Player b){
            return b.decksUsed - a.decksUsed;
        }
    }
    //also added
    class SortByFourWeekAverage implements Comparator<Player>{
        public int compare(Player a, Player b){
            return b.getFameAvg() - a.getFameAvg();
        }
    }
    public class WarHistoryThread implements Runnable{
        private APIHandler handler;
        private List<Player> players;
        public WarHistoryThread(APIHandler handler, List<Player> players){
            this.handler = handler;
            this.players = players;
        }
        @Override
        public void run(){
            long startTime = System.nanoTime();
            for(Player i : players){
                //System.out.println(i.getName());
                try {
                    handler.warOpponentsArray(i.getTag(), i);
                } catch (Exception e) {
                }
                //i.battlesToString();
            }
            long endTime   = System.nanoTime();
            long totalTime = endTime - startTime;
            System.out.println(totalTime / Math.pow(10, 9) + " seconds in war history thread");
            
        }
    }
    public class PlayerRankingThread implements Runnable{
        private APIHandler handler;
        private List<Player> players;
        public PlayerRankingThread(APIHandler handler, List<Player> players){
            this.handler = handler;
            this.players = players;
        }
        @Override
        public void run(){
            long startTime = System.nanoTime();
            for(Player i : players){
                try {
                    i.setRanking(handler.returnPlayerRankings(i.getTag()));
                } catch (Exception e) {
                }
            }
            long endTime   = System.nanoTime();
            long totalTime = endTime - startTime;
            System.out.println(totalTime / Math.pow(10, 9) + " seconds in playerRanking Thread");
        }
    }
    public class OpponentRankingThread {
        private APIHandler handler;
        private List<Player> players;
        public OpponentRankingThread(APIHandler handler, List<Player> players){
            this.handler = handler;
            this.players = players;
        }
        public void run() throws Exception{
            long startTime = System.nanoTime();
            //debugging purposes only
            // try{
            // for(Player i : players){
            //     if(i.getName().equals("depression")){
            //         System.out.println(i.getName() + " size of array " + i.getBattles().size());
            //         for(Battles[] j : i.getBattles()){  
            //         System.out.println(j[0]);
            //         //System.out.println(j[0].getOpponentBattle().returnTag() + " type " + j[0].getOpponentBattle().getType());           
            //         //j[0].getOpponentBattle().setOpponentRanking(handler.returnPlayerRankings(j[0].getOpponentBattle().returnTag()));
            //     }
            //     }   
            // }
            // }catch(Exception e){
            //     System.out.println(e);
            // }
            ForkJoinPool innerPool = new ForkJoinPool(4);
            players.parallelStream().forEach(players -> {
                innerPool.submit(() ->
                    players.getBattles().parallelStream().forEach(battles -> {
                        try {
                            battles[0].getOpponentBattle().setOpponentRanking(handler.returnPlayerRankings(battles[0].getOpponentBattle().returnTag()));
                        } catch (Exception e) {
                        }
                })).join();
                }
            );
            innerPool.close();
            long endTime   = System.nanoTime();
            long totalTime = endTime - startTime;
            System.out.println(totalTime / Math.pow(10, 9) + " seconds in opponentRanking Thread");
        }
    }
    public class CalculatePressureThread{

    } 
}

    
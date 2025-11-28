package com.clashroyale;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.clashroyale.ClanDatabase.Player;
 

public class APIHandler {
    // private final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    // private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    // private final String TOKENS_DIRECTORY_PATH = "tokens";
    
    //private Sheets sheetsService;
    
  

    
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
            //this version will not work without your own key
    

    public APIHandler() throws Exception{  
       
        
    }

    //keep this for now as a temp solution but rn i don't want to rework this. 
    AbstractMap.SimpleEntry<JSONArray, JSONArray> sendGet() throws Exception {
        long startTime = System.nanoTime();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://api.clashroyale.com/v1/clans/%23GCG2RJP0/currentriverrace"))
                .headers("typ", "JWT", "alg", "HS512", "kid", "28a318f7-0000-a1eb-7fa1-2c7433c6cca5", "authorization", token)
                .build();
        //we need this to check for duplicate members
        HttpRequest request2 = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://api.clashroyale.com/v1/clans/%23GCG2RJP0/members"))
                .headers("typ", "JWT", "alg", "HS512", "kid", "28a318f7-0000-a1eb-7fa1-2c7433c6cca5", "authorization", token)
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response2 = httpClient.send(request2, HttpResponse.BodyHandlers.ofString());
        System.out.println("Current River Race response (not used anymore)"+ response.statusCode());
        System.out.println("Clan memebers response (still being used)"+response2.statusCode());
        //only needed to check wtf is going on
        //System.out.println(response2.body());


        JSONParser parser = new JSONParser();
        JSONObject json1 = (JSONObject) parser.parse(response.body());
        JSONObject json2 = (JSONObject) parser.parse(response2.body());
        JSONObject clan = (JSONObject) json1.get("clan");
        JSONArray warMembers = (JSONArray) clan.get("participants");
        JSONArray allMembers = (JSONArray) json2.get("items");
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        System.out.println(totalTime / Math.pow(10, 9) + " seconds to run API handler");
        return new AbstractMap.SimpleEntry<>(warMembers, allMembers);
    }
    //We need a function that gives information about the players
    
    public long[][] returnPlayerRankings(String playerTag) throws Exception{
        
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://api.clashroyale.com/v1/players/%23"+playerTag))
                .headers("typ", "JWT", "alg", "HS512", "kid", "28a318f7-0000-a1eb-7fa1-2c7433c6cca5", "authorization", token)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());        
        JSONParser parser = new JSONParser();
        JSONObject json1 = (JSONObject) parser.parse(response.body());
        long[] expLevel = {(long) json1.get("expLevel")};
        //System.out.println(json1.get("currentPathOfLegendSeasonResult"));
        //there's got to be a way to do this faster there is just no way that this is what i have to do
        JSONObject json2 = (JSONObject) json1.get("currentPathOfLegendSeasonResult");
        long[] currentPOL = {0L, (long)json2.get("leagueNumber"), (long)json2.get("trophies")};
        if(json2.get("rank") != null){
                currentPOL[0] = (long)json2.get("rank");
        }
        json2 = (JSONObject) json1.get("lastPathOfLegendSeasonResult");
        long[] pastPOL = {0L,(long)json2.get("leagueNumber"), (long)json2.get("trophies")};
        if(json2.get("rank") != null){
                pastPOL[0] = (long)json2.get("rank");
        }
        json2 = (JSONObject) json1.get("bestPathOfLegendSeasonResult");
        long[] bestPOL = {0L,(long)json2.get("leagueNumber"), (long)json2.get("trophies")};
        if(json2.get("rank") != null){
                bestPOL[0] = (long)json2.get("rank");
        }
        long[] trophies = {(long)json1.get("trophies"), (long)json1.get("bestTrophies")};
        
        
        return new long[][]{currentPOL, bestPOL, pastPOL, trophies, expLevel};
        
    }

    public void warOpponentsArray(String tag, Player player) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://api.clashroyale.com/v1/players/%23"+tag+"/battlelog"))
                .headers("typ", "JWT", "alg", "HS512", "kid", "28a318f7-0000-a1eb-7fa1-2c7433c6cca5", "authorization", token)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());        
        JSONParser parser = new JSONParser();
        //if it returns jsonobject, then response is probably 404
        JSONArray jsonArray = (JSONArray) parser.parse(response.body());
        
        
        //System.out.println(jsonArray.get("message"));
        Iterator iterator = jsonArray.iterator();
        
        while(iterator.hasNext()){
                Map map = (Map) iterator.next();
                String battleType = (String) map.get("type");
                //System.out.println(battleType);
                
                if(battleType.equals("riverRacePvP")){
                        //System.out.println("hello");
                        //System.out.println(battleType);
                        Battles battle = new Battles();
                        String time = (String) map.get("battleTime");
                        JSONArray jsonArray2 = (JSONArray) map.get("team");
                        JSONObject jsonObject = (JSONObject) jsonArray2.get(0);
                        try{
                                
                                long crowns = (long) jsonObject.get("crowns");
                                long kingTowerHealth = 0L;
                                if (jsonObject.get("kingTowerHitPoints") != null){
                                        kingTowerHealth = (long) jsonObject.get("kingTowerHitPoints");
                                }
                                JSONArray towersHealth = (JSONArray) jsonObject.get("princessTowersHitPoints");
                                long princessTowersHitPoints[] = {0L,0L};    
                                if (towersHealth != null){
                                        for(int i = 0; i < towersHealth.size();i++){
                                                if(towersHealth.get(i) != null){
                                                        princessTowersHitPoints[i] = (long)towersHealth.get(i); 
                                                } 
                                        } 
                                }
                                double elixirLeaked = (double)jsonObject.get("elixirLeaked");
                                battle.setBattles(battleType, crowns, kingTowerHealth, princessTowersHitPoints, elixirLeaked,time);
                                jsonArray2 = (JSONArray) jsonObject.get("cards");
                                for (int i = 0; i < jsonArray2.size(); i++){
                                        jsonObject = (JSONObject) jsonArray2.get(i);
                                        String name = (String) jsonObject.get("name");
                                        long level = (long) jsonObject.get("level");
                                        long evolutionLevel = 0L;
                                        if(jsonObject.get("evolutionLevel") != null){
                                                evolutionLevel = (long) jsonObject.get("evolutionLevel");
                                        }
                                        String rarity = (String) jsonObject.get("rarity");
                                        battle.addCards(name, level, evolutionLevel, rarity);
                                }
                        }catch(Exception e){
                        //System.out.println(e);
                        }
                        jsonArray2 = (JSONArray) map.get("opponent");
                        jsonObject = (JSONObject) jsonArray2.get(0);
                        try{
                                Battles opponent = new Battles();
                                String opponentName = (String) jsonObject.get("name");
                                String opponentTag = (String) jsonObject.get("tag");
                                opponentTag = opponentTag.substring(1);
                                //long[][] opponentRankings = returnPlayerRankings(tag);
                                long crowns = (long) jsonObject.get("crowns");
                                long kingTowerHealth = 0L;
                                        if (jsonObject.get("kingTowerHitPoints") != null){
                                                kingTowerHealth = (long) jsonObject.get("kingTowerHitPoints");
                                        }
                                JSONArray towersHealth = (JSONArray) jsonObject.get("princessTowersHitPoints");
                                long princessTowersHitPoints[] = {0L,0L};    
                                if (towersHealth != null){
                                        for(int i = 0; i < towersHealth.size();i++){
                                                if(towersHealth.get(i) != null){
                                                        princessTowersHitPoints[i] = (long)towersHealth.get(i); 
                                                } 
                                        } 
                                }
                                double elixirLeaked = (double)jsonObject.get("elixirLeaked");
                                opponent.setBattles(battleType, crowns, kingTowerHealth, princessTowersHitPoints, elixirLeaked,time);
                                jsonArray2 = (JSONArray) jsonObject.get("cards");
                                for (int i = 0; i < jsonArray2.size(); i++){
                                        jsonObject = (JSONObject) jsonArray2.get(i);
                                        String name = (String) jsonObject.get("name");
                                        long level = (long) jsonObject.get("level");
                                        long evolutionLevel = 0L;
                                        if(jsonObject.get("evolutionLevel") != null){
                                                evolutionLevel = (long) jsonObject.get("evolutionLevel");
                                        }
                                        String rarity = (String) jsonObject.get("rarity");
                                        opponent.addCards(name, level, evolutionLevel, rarity);
                                }
                                battle.opponentStats(opponent, opponentTag, opponentName);
                                //battle.setOpponentRanking(opponentRankings);
                                
                        }catch(Exception e){
                                System.out.println(e);
                        }
                        player.addBattle(new Battles[] {battle});         
                }
                else if(battleType.equals("riverRaceDuel") || battleType.equals("riverRaceDuelColosseum")){
                        String time = (String) map.get("battleTime");
                        try{
                        //System.out.println(player.getName());
                        JSONArray jsonArray2 = (JSONArray) map.get("team");
                        JSONObject jsonObject = (JSONObject) jsonArray2.get(0);
                        jsonArray2 = (JSONArray) jsonObject.get("rounds");
                        Battles[] battles = new Battles[jsonArray2.size()];
                        for (int i = 0; i < jsonArray2.size();i++) {
                                battles[i] = new Battles();
                        }
                        //System.out.println(jsonArray2.size());
                        //something about this is sus. don't forget about the index out of bounds shit
                        
                        for(int i = 0; i < jsonArray2.size();i++){
                                
                                jsonObject = (JSONObject) jsonArray2.get(i);
                                long crowns = (long) jsonObject.get("crowns");
                                long kingTowerHealth = 0L;
                                if (jsonObject.get("kingTowerHitPoints") != null){
                                        kingTowerHealth = (long) jsonObject.get("kingTowerHitPoints");
                                }
                                JSONArray towersHealth = (JSONArray)jsonObject.get("princessTowersHitPoints");
                                long princessTowersHitPoints[] = {0L, 0L};
                                if (towersHealth != null){
                                        for(int j = 0; j < towersHealth.size();j++){
                                                if(towersHealth.get(j) != null){
                                                        princessTowersHitPoints[j] = (long)towersHealth.get(j); 
                                                } 
                                        } 
                                }
                                double elixirLeaked = (double) jsonObject.get("elixirLeaked");
                                battles[i].setBattles(battleType, crowns, kingTowerHealth, princessTowersHitPoints, elixirLeaked,time);
                                JSONArray jsonCardArray = (JSONArray) jsonObject.get("cards");
                                for(int j = 0; j < jsonCardArray.size(); j++){
                                        jsonObject = (JSONObject) jsonCardArray.get(j);
                                        String cardName = (String)jsonObject.get("name");
                                        long level = (long) jsonObject.get("level");
                                        long evolutionLevel = 0L;
                                        if(jsonObject.get("evolutionLevel") != null){
                                                evolutionLevel = (long) jsonObject.get("evolutionLevel");
                                        }
                                        String rarity = (String) jsonObject.get("rarity");
                                        battles[i].addCards(cardName, level, evolutionLevel, rarity);
                                }

                        }
                        jsonArray2 = (JSONArray) map.get("opponent");
                        jsonObject = (JSONObject) jsonArray2.get(0);
                        String opponentName = (String) jsonObject.get("name");
                        String opponentTag = (String) jsonObject.get("tag");
                        opponentTag = opponentTag.substring(1);
                        //System.out.println(opponentTag);
                        jsonArray2 = (JSONArray) jsonObject.get("rounds");
                        for(int i = 0; i < jsonArray2.size();i++){
                                jsonObject = (JSONObject) jsonArray2.get(i);
                                Battles opponentBattles = new Battles();
                                long crowns = (long) jsonObject.get("crowns");
                                long kingTowerHealth = 0L;
                                if (jsonObject.get("kingTowerHitPoints") != null){
                                        kingTowerHealth = (long) jsonObject.get("kingTowerHitPoints");
                                }
                                JSONArray towersHealth = (JSONArray)jsonObject.get("princessTowersHitPoints");
                                long princessTowersHitPoints[] = {0L, 0L};
                                if (towersHealth != null){
                                        for(int j = 0; j < towersHealth.size();j++){
                                                if(towersHealth.get(j) != null){
                                                        princessTowersHitPoints[j] = (long)towersHealth.get(j); 
                                                } 
                                        } 
                                }
                                double elixirLeaked = (double) jsonObject.get("elixirLeaked");
                                opponentBattles.setBattles(battleType, crowns, kingTowerHealth, princessTowersHitPoints, elixirLeaked,time);
                                JSONArray jsonCardArray = (JSONArray) jsonObject.get("cards");
                                for(int j = 0; j < jsonCardArray.size(); j++){
                                        jsonObject = (JSONObject) jsonCardArray.get(j);
                                        String cardName = (String)jsonObject.get("name");
                                        long level = (long) jsonObject.get("level");
                                        //check null later
                                        long evolutionLevel = 0L;
                                        if(jsonObject.get("evolutionLevel") != null){
                                                evolutionLevel = (long) jsonObject.get("evolutionLevel");
                                        }
                                        String rarity = (String) jsonObject.get("rarity");
                                        opponentBattles.addCards(cardName, level, evolutionLevel, rarity);
                                }
                                battles[i].opponentStats(opponentBattles, opponentTag, opponentName);
                        }
                        player.addBattle(battles);
                        //System.out.println(battles[0].getOpponentBattle().opponentToString() + "\n" + battles[1].getOpponentBattle().opponentToString());
                }catch(Exception e){
                        System.out.println(e);
                }
                }

        }
    }

}

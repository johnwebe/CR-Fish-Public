package com.clashroyale;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import com.clashroyale.ClanDatabase.Player;

public class Main {
    
    public static void main(String[] args) throws Exception, InterruptedException{
    
    long startTime = System.nanoTime();
    APIHandler apiHandler = new APIHandler();
    AbstractMap.SimpleEntry<JSONArray,JSONArray> json = apiHandler.sendGet();
    ClanDatabase database = new ClanDatabase(json.getKey(),json.getValue());
    ExecutorService mainThreads = Executors.newFixedThreadPool(10);
    //initialization
    CompletableFuture<Void> task0 = CompletableFuture.runAsync(() -> {
      try {
        database.getPlayerWarHistory(apiHandler, mainThreads);
      } catch (Exception e) {
      }}, mainThreads);
    CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
      try {
        database.setPlayerRanking(apiHandler, mainThreads);
      } catch (Exception e) {
      }}, mainThreads);
    CompletableFuture.allOf(task0,task1).join();
    //ends a set of futures
    database.getOpponentRankings(apiHandler, mainThreads);
    
    mainThreads.shutdown();
    try {
      mainThreads.awaitTermination(20, TimeUnit.SECONDS);
    } catch (Exception e) {
    }


    long endTime   = System.nanoTime();
    long totalTime = endTime - startTime;
    System.out.println(totalTime / Math.pow(10, 9) + " seconds");
     
  }
  
}
    


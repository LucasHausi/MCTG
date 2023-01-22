package org.mtcg.game;

public class BattleLog {
    private String log = "";

    public String getLog() {
        return log;
    }

    public void print(){
        System.out.println("-----------------------------------------");
        System.out.println(log);
        System.out.println("-----------------------------------------");
    }
    public void addToLog(String temp){
        this.log += temp;
    }
}

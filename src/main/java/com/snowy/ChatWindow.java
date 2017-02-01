/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.snowy;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Snowy
 */
public class ChatWindow extends Panel{
    private data d;
    private UsersList ul;
    private TabSheet tabMain, tabChats;
    private HashMap<Integer,Chat> ht = new HashMap<>();
    public ChatWindow(data d){
        this.d = d;
         ul= new UsersList(d);
        tabMain = new TabSheet();
        tabChats = new TabSheet();
        //tabChats.addTab();
        //tabChats.
        ht.put(0, new Chat(0,d));
        tabChats.addTab(ht.get(0),"Main Lobby");
        tabChats.setSizeFull();
        tabMain.addTab(tabChats,"Chat");
        tabMain.addTab(ul,"Users");
       tabMain.setSizeFull();
        
        //tabChats.setImmediate(true);
        this.setContent(tabMain);
    }
    public void updateChats(){
        for(Integer i:ht.keySet()){
           ht.get(i).update();
           //figure out how to remove inactive chats
        }
    }
    public void addChat(int id){
        if(!ht.containsKey(id) && d.isGameActive(id)==0){
            ht.put(id, new Chat(id,d));
            tabChats.addTab(ht.get(id), "Game "+(ht.size()-1));
        }
         //Logger.getLogger(ChatWindow.class.getName()).info(id+" | "+ht.containsKey(id));
        
    }
    public ArrayList<Integer> getChatIds(){
        ArrayList<Integer> arr = new ArrayList<>();
        ht.forEach((k,v)->{
            arr.add(k);
        });
        return arr;
    }
    public UsersList getUsersList(){
        return ul;
    }
}

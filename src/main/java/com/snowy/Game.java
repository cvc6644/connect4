/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.snowy;

import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Snowy
 */
public class Game extends VerticalLayout{
    private JavaScript js = JavaScript.getCurrent();
    private int gameId,p1,p2,currentTurn;
    private data d;
    private boolean hasUpdate =false;
    private boolean selfGame,notDisp=false;
    public boolean rendered =false;
    private ArrayList<ArrayList<Integer>> gameBoard;
    private int jj =0;
    public Game(int id, data d){
        
        Notification.show("Game/Chat Created", Notification.Type.TRAY_NOTIFICATION);
        this.d = d;
        gameId = id;
        
       CheckForUpdate();
       selfGame = p1==p2;
       this.setId("GameForMe");
       js.addFunction("sendMove", (e)->{
           int clickPos = (int) e.getNumber(0);
           for(int i = 0;i<e.getArray(1).length();i++){
               
               int circle =(int)e.getArray(1).getArray(i).getNumber(0);
               int center = (int)e.getArray(1).getArray(i).getNumber(1);
               if(clickPos<=(center+30) && clickPos >=(center-30)){
                   //Logger.getLogger(Game.class.getName()).info("Played "+circle);
                   boolean win = d.move(circle,(currentTurn == p1?p2:p1),this.gameId);
                   if(win==true){
                       /*Notification nn = new Notification("Game Over You Win","",Notification.Type.WARNING_MESSAGE);
                       nn.setDelayMsec(5000);
                       nn.show(Page.getCurrent());*/
                       
                       js.execute("if(document.getElementById('GameForMe')!=null && "
                + "document.getElementById('GameForMe').hasChildNodes() &&"
                + "document.getElementById('GameForMe').lastChild.hasChildNodes()){"
                + "var bb = document.getElementsByTagName('svg')[0];"
                + "bb.parentNode.removeChild(bb);"
                + "}");
                       VerticalLayout winLayout = new VerticalLayout();
                       Label winMessage = new Label("<h1>Congratulations You Win!</h1>",ContentMode.HTML);
                       Label winCloseMessage = new Label("<h3>Game and Chat Tabs will stay open till next Login</h3>",ContentMode.HTML);
                       Notification.show("Game Won", Notification.Type.TRAY_NOTIFICATION);
                       winLayout.addComponent(winMessage);
                       winLayout.addComponent(winCloseMessage);
                       winLayout.setSizeFull();
                       winLayout.setMargin(true);
                       winLayout.setSpacing(false);
                       winLayout.setComponentAlignment(winMessage, Alignment.MIDDLE_CENTER);
                       
                       winLayout.setComponentAlignment(winCloseMessage, Alignment.MIDDLE_CENTER);
                       this.addComponent(winLayout);
                       this.setComponentAlignment(winLayout,Alignment.MIDDLE_CENTER);
                       notDisp=true;
                       //Logger.getLogger(Game.class.getName()).info("go win");
                   }
                   currentTurn = currentTurn == p1?p2:p1;
                   rendered=false;
                   break;
               }
           }
           
        });
       
       /*this.addComponent(new Button("c",e->{
           create();
           //js.execute("hello();");
       }));*/
       
       this.setSizeFull();
      
    }
    public int getGameId(){
        return gameId;
    }
    public void CheckForUpdate(){
        if(d.isGameActive(gameId)==0){
            ArrayList<ArrayList<Integer>> tempGameBoard = d.getGameBoard(gameId);
            if(!tempGameBoard.equals(gameBoard)){
                hasUpdate =true;
                gameBoard = tempGameBoard;
                p1 = d.getPlayerOne(gameId);
                p2 = d.getPlayerTwo(gameId);
                currentTurn = d.getLastTurn(gameId)==0? p1: d.getLastTurn(gameId);
            }
        //Logger.getLogger(Game.class.getName()).info(hasUpdate+"|"+this.isConnectorEnabled()+"|"+rendered+"|"+jj++);
            if(this.isConnectorEnabled()&& hasUpdate ){//&& !rendered){
                Update();
                hasUpdate =false;
            //Logger.getLogger(Game.class.getName()).info("has update");
                rendered =true;
            }else if(this.isConnectorEnabled() && !rendered){
                Update();
            //Logger.getLogger(Game.class.getName()).info("rendering");
                this.rendered =true;
            }else if(!this.isConnectorEnabled() && rendered){
                rendered =false;
            //Logger.getLogger(Game.class.getName()).info("mark as need to render");
            }
        }else{
            //Logger.getLogger(Game.class.getName()).info("go lose");
            if(this.isConnectorEnabled() && notDisp==false){
                //Logger.getLogger(Game.class.getName()).info("go lose " +((d.getLastTurn(gameId)==p1?p2:p1))+" | "+d.getUserIdFromToken());
                if((d.getLastTurn(gameId)==p1?p2:p1)!=d.getUserIdFromToken()){
                    /*Notification nn = new Notification("Game Over You Lose","",Notification.Type.WARNING_MESSAGE);
                    nn.setDelayMsec(5000);
                    nn.show(Page.getCurrent());*/
                    js.execute("if(document.getElementById('GameForMe')!=null && "
                + "document.getElementById('GameForMe').hasChildNodes() &&"
                + "document.getElementById('GameForMe').lastChild.hasChildNodes()){"
                + "var bb = document.getElementsByTagName('svg')[0];"
                + "bb.parentNode.removeChild(bb);"
                + "}");
                    VerticalLayout loseLayout = new VerticalLayout();
                       Label loseMessage = new Label("<h1>Unfortunately You Have Lost</h1>",ContentMode.HTML);
                       Label loseCloseMessage = new Label("<h3>Game and Chat Tabs will stay open till next Login</h3>",ContentMode.HTML);
                       loseLayout.addComponent(loseMessage);
                       loseLayout.addComponent(loseCloseMessage);
                       loseLayout.setSizeFull();
                       loseLayout.setComponentAlignment(loseMessage, Alignment.MIDDLE_CENTER);
                       
                       Notification.show("Game Lost", Notification.Type.TRAY_NOTIFICATION);
                       loseLayout.setComponentAlignment(loseCloseMessage, Alignment.MIDDLE_CENTER);
                       loseLayout.setMargin(true);
                       loseLayout.setSpacing(false);
                       this.addComponent(loseLayout);
                       this.setComponentAlignment(loseLayout,Alignment.MIDDLE_CENTER);
                    //Logger.getLogger(Game.class.getName()).info("go lose");
                    notDisp = true;
                }
                
            }
            //n.
        }
    }
    
    public void Update(){
       js.execute("if(document.getElementById('GameForMe')!=null && "
                + "document.getElementById('GameForMe').hasChildNodes() &&"
                + "document.getElementById('GameForMe').lastChild.hasChildNodes()){"
                + "var bb = document.getElementsByTagName('svg')[0];"
                + "bb.parentNode.removeChild(bb);"
                + "}");
        String s = "var cursorX;var cursorY;"+
        "var ss = document.getElementById(\"GameForMe\").lastChild;"
                +
        "var svgns = \"http://www.w3.org/2000/svg\";"+
                
        "var svg = document.createElementNS(svgns,\"svg\");\n" +
                "svg.setAttribute('xmlns',svgns);"+
                
                "svg.setAttributeNS(null,'version','1.1');"+
                "svg.setAttributeNS(null,'width','"+this.getWidth()+this.getWidthUnits().getSymbol()+"');"+
                "svg.setAttributeNS(null,'height','"+this.getHeight()+this.getHeightUnits().getSymbol()+"');"+
                "var rectMain = document.createElementNS(svgns,'rect');"
                + "rectMain.setAttributeNS(null,'width','98%');"
                + "rectMain.setAttributeNS(null,'x','1%');"
                + "rectMain.setAttributeNS(null,'y','14%');"
                + "rectMain.setAttributeNS(null,'height','86%');"
                + "rectMain.setAttributeNS(null,'fill','blue');"
                //+ "rectMain.setAttribute('mousedown','imClickable()');"
                + "svg.appendChild(rectMain);";
        for(int i =0 ;i<7;i++){//colloms
            for(int xx =0;xx<7;xx++){//rows
                
                        if(xx==0 && this.currentTurn == d.getUserIdFromToken()){
                            s+="var circ"+i+xx+"=document.createElementNS(svgns,'circle');"
                        + "circ"+i+xx+".setAttributeNS(null,'r','30px');"
                        + "circ"+i+xx+".setAttributeNS(null,'cy',(7+"+(xx*14.3)+")+'%');"
                         + "circ"+i+xx+".setAttributeNS(null,'cx',(8+"+(i*14)+")+'%');";
                            s+= "circ"+i+xx+".setAttributeNS(null,'stroke','grey');"
                                    + "circ"+i+xx+".setAttributeNS(null,'stroke-width','3px');"
                                    + "circ"+i+xx+".setAttributeNS(null,'stroke-dasharray','10,5');"
                                    + "circ"+i+xx+".setAttributeNS(null,'fill','white');"
                                    + "circ"+i+xx+".setAttributeNS(null,'class','inputters');"
                                    + "";
                            s+= "svg.appendChild(circ"+i+xx+");";
                        }else if(xx!=0){
                            s+="var circ"+i+xx+"=document.createElementNS(svgns,'circle');"
                        + "circ"+i+xx+".setAttributeNS(null,'r','30px');"
                        + "circ"+i+xx+".setAttributeNS(null,'cy',(7+"+(xx*14.3)+")+'%');"
                         + "circ"+i+xx+".setAttributeNS(null,'cx',(8+"+(i*14)+")+'%');";
                            switch(gameBoard.get(xx-1).get(i)){
                                case 1:
                                    s+= "circ"+i+xx+".setAttributeNS(null,'fill','red');";
                                    //Logger.getLogger(Game.class.getName()).info(i+"|"+xx+"    red");
                                    break;
                                case 2:
                                    s+= "circ"+i+xx+".setAttributeNS(null,'fill','yellow');";
                                    //Logger.getLogger(Game.class.getName()).info(i+"|"+xx+"    yellow");
                                    break;
                                case 0:
                                    
                                    s+= "circ"+i+xx+".setAttributeNS(null,'fill','white');";
                                    //Logger.getLogger(Game.class.getName()).info(i+"|"+xx+"    white");
                                    break;
                            }
                            //Logger.getLogger(Game.class.getName()).info(i+"|"+xx+"    "+gameBoard.get(i).get(xx));
                            s+= "svg.appendChild(circ"+i+xx+");";
                        }
                        
                        
            }
        }
        
        if(this.currentTurn == d.getUserIdFromToken()){
            
            s+=""
                    + "var rectPlay = document.createElementNS(svgns,'rect');"
                    + "rectPlay.setAttributeNS(null,'width','100%');"
                    + "rectPlay.setAttributeNS(null,'height','14%');"
                    + "rectPlay.setAttributeNS(null,'opacity','0');"
                    + "rectPlay.onmouseenter = function(e){"
                    + ""
                    + "};"
                    + "rectPlay.onmouseout = function(e){"
                    + ""
                    + "};"
                    + "rectPlay.onmousemove = function(e){"
                    + ""
                    + "clientX = e.layerX;"
                    + "clientY = e.layerY;"
                    + "};"
                    + "rectPlay.onclick= function(e){"
                    + "var inputters =document.getElementsByClassName('inputters');"
                    + "var arr=new Array();"
                    + "for(var i =0;i<inputters.length;i++){"
                    + "var test = parseInt(inputters[i].getAttributeNS(null,'cx'));"
                    + "var parentTest = inputters[i].parentNode.getBBox().width;"
                    + "var pixelTest = parentTest*(test/100);"
                    + "arr.push(new Array(i,pixelTest));"
                    
                    
                    
                    + "}"
                    + "sendMove(clientX,arr);"
                    + "rectPlay.onclick=null;"
                    + "};"
        
                    + "svg.appendChild(rectPlay);";
        }
        
        s+="ss.appendChild(svg);";
        
        js.execute(s);
        
        
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.snowy;

import com.vaadin.server.Page;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.JdbcTemplate;

//import org.springframework.jdbc.support.rowset.SqlRowSet;

//import com.mysql.jdbc
/**
 *
 * @author snowyowl
 */
public class data {
    //JdbcTemplate template;
    //private SqlRowSet srs;
    /*static SimpleJDBCConnectionPool connectionPool;
    static{
        try{
            connectionPool = new SimpleJDBCConnectionPool(
            "com.mysql.jdbc.Driver",
            "jdbc:mysql//localhost:3306/webappdev","webappdev","password",2,2);
              
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static ResultSet createMySQLContainer(){
        TableQuery query = null;
        ResultSet temp = null;
        try{
            Connection c = connectionPool.reserveConnection();
            Statement statement = c.createStatement();
            ResultSet r = statement.executeQuery("select * from tempuser");
            temp = r;
            
            connectionPool.releaseConnection(c);
            
        }catch(SQLException e){
            e.printStackTrace();
        }
        return temp;
    }
    */
    private Connection con;
    public data(){
        con = connect();
    }
    public Connection connect(){
        Connection c = null;
        try {
            /*Properties p = new Properties();
            p.put("user", "webappdev");
            p.put("password","password");
            p.put("port", 3306);
            //p.put("useSSL", false);
            Class.forName("com.mysql.cj.jdbc.Driver");
            //DriverManager.registerDriver();
            c = DriverManager.getConnection("jdbc:mysql://localhost:3306/webappdev?useSSL=false",p);
            */
            c = WebappdevApplication.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        return c;
        
        
    }
    //public data(){
      //  con = connect();
    //}
    public String genToken(String username,String password){
        
        if(login(username,password)){
            
            String unameToke=username;
            for(int i=username.length();i<40;i++){
                unameToke+="0";
            }
            unameToke+="00000";
            //uname 45
            //ip = 15 111.111.111.111
            String tok = "";
            String address = Page.getCurrent().getWebBrowser().getAddress();
            String addLat="";
            if(address.indexOf('.')!=-1){
                for(String i : address.split("\\.")){
                    if(i.length()<3){
                        for(int x = i.length();x<3;x++){
                            i= "0"+i;
                        }
                        addLat+=i;
                    }else{
                        addLat+=i;
                    }
                    addLat+=".";
                }
                addLat =addLat.substring(0, addLat.length()-1);
            }else{
                addLat= address;
            }
            int zz =0;
            int bb =0;
            for(int b =0;b<60;b++){
                if(b%4==0){
                    tok+=addLat.charAt(bb);
                    bb++;
                }else{
                    tok+=unameToke.charAt(zz);
                    zz++;
                }
            }
            tok+=System.currentTimeMillis() / 1000L;
            String hash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(tok);
            hash+= org.apache.commons.codec.digest.DigestUtils.sha256Hex(hash)+hash;
            VaadinSession.getCurrent().setAttribute("token", hash);
            
            try {
                //return hash;
                PreparedStatement ps = con.prepareStatement("update users set token = ?, active =1 where Username =?");
                ps.setString(1, VaadinSession.getCurrent().getCsrfToken());
                
                //ps.setString(1, "balls");
                //ps.setString(2, "true");
                ps.setString(2, username);
                ps.executeUpdate();
                
                //ps.close();
                //rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
            }
            return hash;
        }else{
            return "false";
        }
        
    }
  
    
    public boolean login(String username,String password){
        
        //Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, con.toString());
        boolean b = false;
        try {
            PreparedStatement s =con.prepareStatement("select Username,Password from users where Username = ?");
            s.setString(1, username);
            ResultSet rs = s.executeQuery();
            rs.last();
            //srs = template.queryForRowSet("select Username,Password from tempuser where Username = ?", username);
            //srs.last();
            if(rs.getRow() >= 1){
                b = PasswordHash.verifyPassword(password.trim(), rs.getString("Password").trim());
                
            }else{
                b= false;
            }
            //s.close();
            //rs.close();
        } catch (PasswordHash.InvalidHashException | SQLException | PasswordHash.CannotPerformOperationException ex) {
            b=false;
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return b;
    }
    
    public String createUser(String username,String password, String email){
        
        String responce="";
        try {
            PreparedStatement ps = con.prepareStatement("select * from users where Username =?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            rs.last();
            if(rs.getRow() == 0){
                
                ps = con.prepareStatement("select * from users where Email=?");
                ps.setString(1, email);
                rs = ps.executeQuery();
                rs.last();
                if(rs.getRow() == 0){
                    ps = con.prepareStatement("insert into users (Username,Password,Email,timeoutTime) values (?,?,?,0)");
                    ps.setString(1, username);
                    ps.setString(2, PasswordHash.createHash(password));
                    ps.setString(3, email);
                    if(ps.executeUpdate()==1){
                        return "Creation Sucess";
                    }else{
                        return "Creation Failure";
                    }
                }else{
                    responce = "Email Exists";
                }
            }else{
               responce = "Username Exists"; 
            }
            //ps.close();
            //rs.close();
        } catch (SQLException | PasswordHash.CannotPerformOperationException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return responce;
    }
    /*public void closeConnection(){
        try {
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/
    public void logout(String csrfToken) {
        try {
            PreparedStatement ps = con.prepareStatement("update users set active = 0 where token = ?");
            //ps.setString(1,"false");
            ps.setString(1, csrfToken);
            ps.executeUpdate();
            //ps.close();
            //con.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public HashMap<String,Integer> retrieveActiveUsers() {
        
        HashMap<String,Integer> hm = new HashMap<>();
        try {
            PreparedStatement ps = con.prepareStatement("select Username, user_id from users where active = ?");
            ps.setInt(1, 1);
            ResultSet rs =ps.executeQuery();
            rs.last();
            if(rs.getRow()>=1){
                rs.beforeFirst();
                while(rs.next()){
                    
                    
                    hm.put(rs.getString("Username"),rs.getInt("user_id"));
                    //Logger.getLogger(data.class.getName()).info(hm.get(rs.getString("Username")).toString());
                }
            }
                //rs.first();
            //while(rs.next()){
                //hm.put( rs.getInt("user_id"),rs.getString("Username"));
                
            //}
            //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hm;
    }
    public String getUsernameFromToken(){
        String s ="";
        try {
            PreparedStatement ss = con.prepareStatement("select Username from users where token = ?");
            ss.setString(1, VaadinSession.getCurrent().getCsrfToken());
            ResultSet rs = ss.executeQuery();
            rs.last();
            if(rs.getRow()>0){
                s = rs.getString("Username");
            }     
            //ss.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }
    public void sendMessage(int id, String value) {
        try {
            PreparedStatement ps = con.prepareStatement("insert into chat(GameId,Message,sender,Timestamp) values(?,?,?,current_timestamp)");
            ps.setInt(1, id);
            ps.setString(2, value);
            //ps.setDate(3, format);\
            ps.setString(3,getUsernameFromToken());
            ps.executeUpdate();
            //ps.setT
            //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public java.sql.Timestamp getCurrentSqlTimestamp() {
        java.sql.Timestamp dd =null;
        try {
            ResultSet rs = con.prepareStatement("select current_timestamp() as timestamp").executeQuery();
            rs.last();
            dd = rs.getTimestamp("timestamp");
           //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dd;
    }
    public int lastMessageIdFromCurrent(int gameId){
        int i=0;
        try {
            PreparedStatement ps = con.prepareStatement("select max(Message_id) as final from chat where GameId=?");
            ps.setInt(1, gameId);
            ResultSet rs = ps.executeQuery();
            rs.last();
            i=rs.getInt("final");
            //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return i;
    }

    
    public HashMap<Integer,String> chatsSince(int id, int lastMessageid) {
        HashMap<Integer,String> hm = new HashMap<>();
        try {
            PreparedStatement ps=con.prepareStatement("select Timestamp,Message,sender,Message_id from chat where GameId =? and Message_id > ?");
            ps.setInt(1, id);
            ps.setInt(2, lastMessageid);
            ResultSet rs = ps.executeQuery();
            rs.beforeFirst();
            while(rs.next()){
                LocalDateTime t =rs.getTimestamp("Timestamp").toLocalDateTime();
                String time = String.format("%02d:%02d", t.getHour(),t.getMinute());
                hm.put(rs.getInt("Message_id"),time+" "+rs.getString("sender")+": "+rs.getString("Message")+"\n");
            }
            //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hm;
    }

    public boolean sendChallenge(int id) {
        boolean ss =false;
        try {
            PreparedStatement ps = con.prepareStatement("insert into gamerequest(requestor,requested)values ((select user_id from users where Username=?),?)");
            
            ps.setString(1, this.getUsernameFromToken());
            ps.setInt(2, id);
            if(ps.executeUpdate()>0){
                ss=true;
            }
            //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ss;
    }
    public String getUsernameFromId(int id){
       String s ="";
        try {
            PreparedStatement ps = con.prepareStatement("select Username from users where user_id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            rs.last();
            s=rs.getString("Username");
            //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }
    public ArrayList<ArrayList<String>> retriveChallenges() {
        ArrayList<ArrayList<String>> res = new ArrayList<> ();
        try {
            
            String uname =this.getUsernameFromToken();
            PreparedStatement ps  =con.prepareStatement("select requestor,requested,timestamp,accepted,RequestId from gamerequest where requestor = (select user_id from users where Username=?) or requested = (select user_id from users where Username=?) and timestamp >subdate(current_timestamp, interval 2 hour)");
            ps.setString(1, uname);
            ps.setString(2, uname);
            //PreparedStatement pss =con.prepareStatement("select Username from users where user_id=?");
            
            ResultSet rs = ps.executeQuery();
            rs.last();
            if(rs.getRow()>0){
                rs.beforeFirst();
                while(rs.next()){
                   
                   ArrayList<String> al = new ArrayList<>();
                   al.add(this.getUsernameFromId(rs.getInt("requestor")));
                   al.add(this.getUsernameFromId(rs.getInt("requested")));
                   al.add(String.valueOf(rs.getTimestamp("timestamp").getTime()));
                   al.add(rs.getInt("accepted")+"");
                   //al.add(rs.getInt("gameCreated")+"");
                   al.add(rs.getInt("RequestId")+"");
                   res.add(al);
                }
            }
            //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }
    public boolean gameCreated(int requestId){
        boolean f =false;
        try {
            PreparedStatement ps = con.prepareStatement("Select GameId from game where fromRequest =?");
            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();
            rs.last();
            f= rs.getRow()>0;
            //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return f;
    }
    public int getGameIdfromRequest(int requestId){
        int i=-1;
        try {
            PreparedStatement ps = con.prepareStatement("select GameId from gamerequest where RequestId=?");
            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();
            rs.last();
            i = rs.getInt("GameId");
            //rs.close();
            //ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return i;
    }
    public int startGame(String requestor, String requested, int requestId) {
        int i =0;
        try {
            PreparedStatement pss = con.prepareStatement("insert into game(p1Id,p2Id,LastMoveBy,fromRequest,LastKnownState,MoveOrder) values((select user_id from users where Username = ?),(select user_id from users where Username=?),?,?,?,?)");
            if(new Random().nextInt(100)>50){
                pss.setString(1,requestor);
                pss.setString(2, requested);
                pss.setInt(3, this.getUserIdFromUsername(requestor));
            }else{
                pss.setString(1, requested);
                pss.setString(2,requestor);
                pss.setInt(3, this.getUserIdFromUsername(requested));
            }
            pss.setInt(4, requestId);
            String emptyBoard = "";
            for(int b =0; b<42;b++){
                emptyBoard+=0;
            }
            pss.setString(5, emptyBoard);
            pss.setString(6,emptyBoard);
                    
            if(this.gameCreated(requestId)==false){
                //Logger.getLogger(data.class.getName()).info("susposedly "+this.gameCreated(requestId));
                if(pss.executeUpdate()>0){
                    //Logger.getLogger(data.class.getName()).info("susposedly true");
                    pss = con.prepareStatement("select GameId from game where fromRequest =?");
                    pss.setInt(1, requestId);
                    ResultSet rs = pss.executeQuery();
                    rs.last();
                    //Logger.getLogger(data.class.getName()).info("susposedly "+rs.getRow());
                    i = rs.getInt("GameId");
                    //Logger.getLogger(data.class.getName()).info("susposedly "+i);
                    PreparedStatement ps = con.prepareStatement("select GameId from gamerequest where RequestId=?");
                    ps.setInt(1, requestId);
                    rs = ps.executeQuery();
                    rs.last();
                    if(rs.getInt("GameId")==0){
                        ps = con.prepareStatement("update gamerequest set GameId =? where RequestId=?");
                        ps.setInt(1,i);
                        ps.setInt(2, requestId);
                        ps.executeUpdate();
                    }
                    //ps.close();
                    //rs.close();
                //con.close();
                }else{
                    Logger.getLogger(data.class.getName()).info("susposedly false");
                }
            }else{
                pss = con.prepareStatement("select GameId from gamerequest where RequestId=?");
                pss.setInt(1, requestId);
                ResultSet rs = pss.executeQuery();
                rs.last();
                i = rs.getInt("GameId");
                //rs.close();
            }
            //pss.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return i;
    }
    public void resetActiveIfNot(){
        try {
            PreparedStatement ps = con.prepareStatement("select active from users where Username =?");
            ps.setString(1, this.getUsernameFromToken());
            ResultSet rs = ps.executeQuery();
            rs.last();
            if(rs.getInt("active")!=1){
                ps = con.prepareStatement("update users set active =? where Username=?");
                ps.setInt(1, 1);
                ps.setString(2,this.getUsernameFromToken());
                ps.executeUpdate();
            }
            //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void acceptRefuse(int requestId,boolean acceptal){
        try {
            PreparedStatement ps = con.prepareStatement("update gamerequest set accepted =? where RequestId =?");
            ps.setInt(2, requestId);
            if(acceptal ==true){
                ps.setInt(1, 1);
            }else{
                ps.setInt(1, 2);
            }
            ps.executeUpdate();
            //ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<ArrayList<Integer>> getGameBoard(int id) {
        /*
         1  2  3  4  5  6  7
       1 01|02|03|04|05|06|07
       2 08|09|10|11|12|13|14
       3 15|16|17|18|19|20|21
       4 22|23|24|25|26|27|28
       5 29|30|31|32|33|34|35
       6 36|37|38|39|40|41|42
        */
        ArrayList<ArrayList<Integer>> returns = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement("Select LastKnownState from game where GameId=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            rs.last();
            String boardString = rs.getString("LastKnownState");
            
            char[] boardCharArray =boardString.toCharArray();
            
            for(int i =0; i<boardCharArray.length;i+=7){
                ArrayList<Integer> in = new ArrayList<>();
                for(int x =0; x<7;x++){
                   in.add( Integer.parseInt(String.valueOf(boardCharArray[i+x])));
                }
                returns.add(in);
            }
            
            //rs.close();
            //ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returns;
    }

    public int getPlayerOne(int gameId) {
        int xs =0;
        try {
            PreparedStatement ps = con.prepareStatement("select p1Id from game where GameId=?");
            ps.setInt(1, gameId);
            ResultSet rs = ps.executeQuery();
            rs.last();
            xs = rs.getInt("p1Id");
            //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return xs;
    }
    public int getPlayerTwo(int gameId) {
        int xs =0;
        try {
            PreparedStatement ps = con.prepareStatement("select p2Id from game where GameId=?");
            ps.setInt(1, gameId);
            ResultSet rs = ps.executeQuery();
            rs.last();
            xs = rs.getInt("p2Id");
            //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return xs;
    }
    public int getLastTurn(int gameId) {
        int xs =0;
        try {
            PreparedStatement ps = con.prepareStatement("select LastMoveBy from game where GameId=?");
            ps.setInt(1, gameId);
            ResultSet rs = ps.executeQuery();
            rs.last();
            xs = rs.getInt("LastMoveBy");
            //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return xs;
    }

    public ArrayList<Integer> getRecientGames() {
        ArrayList<Integer> kk = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement("select GameId from game where (p1Id=(select user_id from users where Username =?) or p2Id =(select user_id from users where Username =?)) and active =0");
            ps.setString(1, this.getUsernameFromToken());
            ps.setString(2, this.getUsernameFromToken());
            ResultSet rs = ps.executeQuery();
            rs.beforeFirst();
            while(rs.next()){
                kk.add(rs.getInt("GameId"));
            }
            //rs.close();
            //ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return kk;
    }

    public int isGameActive(int id) {
        int xs =0;
        try {
            PreparedStatement ps = con.prepareStatement("select active from game where GameId=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            rs.last();
            xs = rs.getInt("active");
            //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return xs;
    }
    public int getNumberOfTurns(int gameId){
         int i =0;
        try {
           //doesnt work
           
            PreparedStatement ps = con.prepareStatement("Select MoveOrder from game where GameId=?");
            ps.setInt(1, gameId);
            ResultSet rs = ps.executeQuery();
            rs.last();
            String boardString = rs.getString("MoveOrder");
            
            char[] boardCharArray =boardString.toCharArray();
            java.util.Arrays.sort(boardCharArray);
            //Logger.getLogger(data.class.getName()).info(boardCharArray[41]+"");
            i=Integer.parseInt(String.valueOf(boardCharArray[41]));
            //Logger.getLogger(data.class.getName()).info(i+"");
            //ps.close();
            //rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return i;
    }
    public ArrayList<ArrayList<Integer>> getMoveOrder(int gameId){
         ArrayList<ArrayList<Integer>> returns = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement("Select MoveOrder from game where GameId=?");
            ps.setInt(1, gameId);
            ResultSet rs = ps.executeQuery();
            rs.last();
            String boardString = rs.getString("MoveOrder");
            
            char[] boardCharArray =boardString.toCharArray();
            
            for(int i =0; i<boardCharArray.length;i+=7){
                ArrayList<Integer> in = new ArrayList<>();
                for(int x =0; x<7;x++){
                   in.add( Integer.parseInt(String.valueOf(boardCharArray[i+x])));
                }
                returns.add(in);
            }
            
            //rs.close();
            //ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returns;
    }
    public int getUserIdFromUsername(String username) {
        int i =0;
        try {
            String s = username;
            
            PreparedStatement ps = con.prepareStatement("select user_id from users where Username =?");
            ps.setString(1, s);
            ResultSet rs = ps.executeQuery();
            rs.last();
            i = rs.getInt("user_id");
            //rs.close();
            //ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return i;
    }
    public int getUserIdFromToken() {
        int i =0;
        try {
            String s = this.getUsernameFromToken();
            
            PreparedStatement ps = con.prepareStatement("select user_id from users where Username =?");
            ps.setString(1, s);
            ResultSet rs = ps.executeQuery();
            rs.last();
            i = rs.getInt("user_id");
            //rs.close();
            //ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return i;
    }
    public void setGameOver(int gameId){
        try {
            PreparedStatement ps = con.prepareStatement("update game set active =1 where GameId=?");
            ps.setInt(1, gameId);
            ps.executeUpdate();
            //ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public boolean move(int circle, int nextPlayer, int gameId) {
        boolean b = false;
        
        try {
            PreparedStatement ps = con.prepareStatement("update game set LastMoveBy =?,LastKnownState =? where GameId=?");
            ps.setInt(1, nextPlayer);
            int currentPlayer = (this.getPlayerOne(gameId)==nextPlayer?2:1);
            int moveX=0;
                    int moveY = circle;
            ArrayList<ArrayList<Integer>> list = this.getGameBoard(gameId);
            for(int i=list.size()-1;i>0;i--){
                if(list.get(i).get(circle)==0){
                    list.get(i).set(circle, (this.getPlayerOne(gameId)==nextPlayer?2:1));
                    moveX = i;
                    break;
                }
            }
            String LastMoveBy="";
            for(ArrayList<Integer>e :list){
                for(Integer i : e){
                    
                    LastMoveBy +=i;
                }
            }
            ps.setString(2, LastMoveBy);
            String MoveOrder="";
            ArrayList<ArrayList<Integer>> ks = this.getMoveOrder(gameId);
            
            ks.get(moveX).set(moveY, (this.getNumberOfTurns(gameId)+1));
            for(ArrayList<Integer>e :ks){
                for(Integer i : e){
                    
                    MoveOrder +=i;
                }
            }
            //Logger.getLogger(Game.class.getName()).info(MoveOrder+" | "+MoveOrder.length());
            
            
            ps.setInt(3, gameId);
            int i =ps.executeUpdate();
            //ps.close();
            //change this
            //if(LastMoveBy.contains(currentPlayer+""+currentPlayer+""+currentPlayer+""+currentPlayer)){
            String check = currentPlayer+""+currentPlayer+""+currentPlayer+""+currentPlayer;
            if(LastMoveBy.substring(0, 6).contains(check) || 
                    LastMoveBy.substring(7, 13).contains(check)||
                    LastMoveBy.substring(14, 20).contains(check)||
                    LastMoveBy.substring(21, 27).contains(check)||
                    LastMoveBy.substring(28, 34).contains(check)||
                    LastMoveBy.substring(35, 41).contains(check)){    //Logger.getLogger(data.class.getName()).info("winner " +currentPlayer);
                
                setGameOver(gameId);
                return true;
            }else if(moveX <5 && moveX>0){
                if(moveY>0 && moveY<6){
                    if(moveX<=2 && (list.get(moveX+1).get(moveY)==currentPlayer && list.get(moveX+2).get(moveY)==currentPlayer && list.get(moveX+3).get(moveY)==currentPlayer)){
                        setGameOver(gameId);
                        return true;
                    }else{
                        if(moveX>=3){
                            if(moveY==3 ){
                                if((list.get(moveX-1).get(moveY-1)==currentPlayer && list.get(moveX-2).get(moveY-2)==currentPlayer && list.get(moveX-3).get(moveY-3)==currentPlayer)||
                                    (list.get(moveX-1).get(moveY+1)==currentPlayer &&list.get(moveX-2).get(moveY+2)==currentPlayer  &&list.get(moveX-3).get(moveY+3)==currentPlayer)){
                                    setGameOver(gameId);
                                    return true;
                                }
                            }else if(moveY<3){
                                if((list.get(moveX-1).get(moveY-1)==currentPlayer && list.get(moveX-2).get(moveY-2)==currentPlayer && list.get(moveX-3).get(moveY-3)==currentPlayer)){
                                    setGameOver(gameId);
                                    return true;
                                }
                            }else if(moveY>3 ){
                                if((list.get(moveX-1).get(moveY+1)==currentPlayer &&list.get(moveX-2).get(moveY+2)==currentPlayer  &&list.get(moveX-3).get(moveY+3)==currentPlayer)){
                                    setGameOver(gameId);
                                    return true;
                                }
                            }
                        }else{
                            if(moveY==3 ){
                                if((list.get(moveX+1).get(moveY-1)==currentPlayer && list.get(moveX+2).get(moveY-2)==currentPlayer && list.get(moveX+3).get(moveY-3)==currentPlayer)||
                                    (list.get(moveX+1).get(moveY+1)==currentPlayer &&list.get(moveX+2).get(moveY+2)==currentPlayer  &&list.get(moveX+3).get(moveY+3)==currentPlayer)){
                                    setGameOver(gameId);
                                    return true;
                                }
                            }else if(moveY>3){
                                if((list.get(moveX+1).get(moveY-1)==currentPlayer && list.get(moveX+2).get(moveY-2)==currentPlayer && list.get(moveX+3).get(moveY-3)==currentPlayer)){
                                    setGameOver(gameId);
                                    return true;
                                }
                            }else if(moveY<3 ){
                                if((list.get(moveX+1).get(moveY+1)==currentPlayer &&list.get(moveX+2).get(moveY+2)==currentPlayer  &&list.get(moveX+3).get(moveY+3)==currentPlayer)){
                                    setGameOver(gameId);
                                    return true;
                                }
                            }
                        }
                    }
                    
                }else if(moveY==0){
                    if(moveX<=2 &&(list.get(moveX+1).get(moveY)==currentPlayer && list.get(moveX+2).get(moveY)==currentPlayer && list.get(moveX+3).get(moveY)==currentPlayer)){
                        setGameOver(gameId);
                        return true;
                    }else if(moveX>=3){
                                if((list.get(moveX-1).get(moveY+1)==currentPlayer && list.get(moveX-2).get(moveY+2)==currentPlayer && list.get(moveX-3).get(moveY+3)==currentPlayer)){
                                    setGameOver(gameId);
                                    return true;
                                }
                    }else{
                        if((list.get(moveX+1).get(moveY+1)==currentPlayer && list.get(moveX+2).get(moveY+2)==currentPlayer && list.get(moveX+3).get(moveY+3)==currentPlayer)){
                                    setGameOver(gameId);
                                    return true;
                                }
                    }
                }else if(moveY==6){
                    if(moveX<=2 && (list.get(moveX+1).get(moveY)==currentPlayer && list.get(moveX+2).get(moveY)==currentPlayer && list.get(moveX+3).get(moveY)==currentPlayer)){
                        setGameOver(gameId);
                        return true;
                    }else if(moveX>=3){
                                if((list.get(moveX-1).get(moveY-1)==currentPlayer && list.get(moveX-2).get(moveY-2)==currentPlayer && list.get(moveX-3).get(moveY-3)==currentPlayer)){
                                    setGameOver(gameId);
                                    return true;
                                }
                    }else{
                        if((list.get(moveX+1).get(moveY-1)==currentPlayer && list.get(moveX+2).get(moveY-2)==currentPlayer && list.get(moveX+3).get(moveY-3)==currentPlayer)){
                                    setGameOver(gameId);
                                    return true;
                                }
                    }
                }
            }else if(moveX ==5){
                //only check diag
                if(moveY>0 && moveY<6){
                    if(moveY==3){
                        if((list.get(moveX-1).get(moveY-1)==currentPlayer && list.get(moveX-2).get(moveY-2)==currentPlayer && list.get(moveX-3).get(moveY-3)==currentPlayer)||
                                (list.get(moveX-1).get(moveY+1)==currentPlayer &&list.get(moveX-2).get(moveY+2)==currentPlayer  &&list.get(moveX-3).get(moveY+3)==currentPlayer)){
                            setGameOver(gameId);
                            return true;
                        }
                    }else if(moveY<3){
                        if((list.get(moveX-1).get(moveY-1)==currentPlayer && list.get(moveX-2).get(moveY-2)==currentPlayer && list.get(moveX-3).get(moveY-3)==currentPlayer)){
                            setGameOver(gameId);
                            return true;
                        }
                    }else if(moveY>3){
                        if((list.get(moveX-1).get(moveY+1)==currentPlayer &&list.get(moveX-2).get(moveY+2)==currentPlayer  &&list.get(moveX-3).get(moveY+3)==currentPlayer)){
                            setGameOver(gameId);
                            return true;
                        }
                    }
                }
                else if(moveY==0){
                    if(list.get(moveX-1).get(moveY+1)==currentPlayer && list.get(moveX-2).get(moveY+2)==currentPlayer && list.get(moveX-3).get(moveY+3)==currentPlayer ){
                        setGameOver(gameId);
                        return true;
                    }
                }else if(moveY==6){
                    if(list.get(moveX-1).get(moveY-1)==currentPlayer && list.get(moveX-2).get(moveY-2)==currentPlayer && list.get(moveX-3).get(moveY-3)==currentPlayer ){
                        setGameOver(gameId);
                        return true;
                    }
                }
            }else if(moveX==0){
                //down or diag
                if((list.get(moveX+1).get(moveY)==currentPlayer && list.get(moveX+2).get(moveY)==currentPlayer && list.get(moveX+3).get(moveY)==currentPlayer)){
                        setGameOver(gameId);
                        return true;
                }else{
                    if(moveY==3 ){
                                if((list.get(moveX+1).get(moveY-1)==currentPlayer && list.get(moveX+2).get(moveY-2)==currentPlayer && list.get(moveX+3).get(moveY-3)==currentPlayer)||
                                    (list.get(moveX+1).get(moveY+1)==currentPlayer &&list.get(moveX+2).get(moveY+2)==currentPlayer  &&list.get(moveX+3).get(moveY+3)==currentPlayer)){
                                    setGameOver(gameId);
                                    return true;
                                }
                            }else if(moveY<3){
                                if((list.get(moveX+1).get(moveY-1)==currentPlayer && list.get(moveX+2).get(moveY-2)==currentPlayer && list.get(moveX+3).get(moveY-3)==currentPlayer)){
                                    setGameOver(gameId);
                                    return true;
                                }
                            }else if(moveY>3 ){
                                if((list.get(moveX+1).get(moveY+1)==currentPlayer &&list.get(moveX+2).get(moveY+2)==currentPlayer  &&list.get(moveX+3).get(moveY+3)==currentPlayer)){
                                    setGameOver(gameId);
                                    return true;
                                }
                            }
                }
            }
            
            
            
            
            
            
        } catch (SQLException ex) {
            Logger.getLogger(data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return b;
    }
}

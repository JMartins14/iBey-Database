/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverrmi;


import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author João
 */
public class RmiServer extends UnicastRemoteObject implements ConnectionRMI{
    static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    static final String DATABASE_URL ="jdbc:oracle:thin:@127.0.0.1:1521:xe";
    static final String USERNAME = "bd";
    static final String PASSWORD = "bd";
	//static final int RMIPORT = 6001;0
	static Connection conn = null;
	static Statement stmt = null;
        static Savepoint sp = null;
    public RmiServer() throws RemoteException{
       
        super();
    }
    @Override
    public synchronized boolean test(){
        return true;
    }
    @Override

    //Efetua login do user
    public synchronized Cliente makelogin(String[] dados){
        try {
            String query;
            String user;
            String pass;
            if(dados[2].replaceAll("\\s","").equalsIgnoreCase("username") && dados[4].replaceAll("\\s","").equalsIgnoreCase("password"))
                stmt = conn.createStatement();
            query = "SELECT nome, password "+"FROM Cliente "+"WHERE nome LIKE ?";
            try(PreparedStatement ps=conn.prepareStatement(query)){
                ps.setString(1,dados[3].replaceAll("\\s",""));
                ResultSet res = ps.executeQuery();        
                while(res.next()){
                    user = res.getString("nome");
                    pass = res.getString("password");
                    if(user.equalsIgnoreCase(dados[3].replaceAll("\\s","")) && pass.equalsIgnoreCase(dados[5].replaceAll("\\s",""))){
                            System.out.println("User is successfully logged in.");
                            query = "UPDATE Cliente "
                                +"SET ativo = ? "
                                +"WHERE NOME LIKE ? and PASSWORD like ?";
                        PreparedStatement ps2 = conn.prepareStatement(query);
                        ps2.setInt(1,1);
                        ps2.setString(2,user);
                        ps2.setString(3, pass);
                        ps2.executeQuery();
                        conn.commit();
                        return new Cliente(dados[3].replaceAll("\\s",""),dados[5].replaceAll("\\s",""));
                    }
                }
                res.close();
            }catch(SQLException e){
                e.printStackTrace();
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    //efetua registo do user
    public synchronized boolean makeregister(String[] dados){
        String query;
        if(dados[2].replaceAll("\\s","").equalsIgnoreCase("username") && dados[4].replaceAll("\\s","").equalsIgnoreCase("password")){
            try {
                conn.setAutoCommit(false);
                stmt = conn.createStatement();
                query= "SELECT NOME,PASSWORD FROM CLIENTE";
                ResultSet res = stmt.executeQuery(query);
                while(res.next()){
                    if(dados[3].replaceAll("\\s","").equals(res.getString("NOME"))){
                        System.out.println("Register failure");
                        return false;
                    }                        
                }
                res.close();
                stmt = conn.createStatement();
                sp = conn.setSavepoint();
                query = "INSERT INTO Cliente (nome, password,ativo) "	+"VALUES (?,?,0)";
                try(PreparedStatement ps=conn.prepareStatement(query)){
                    ps.setString(1,dados[3].replaceAll("\\s",""));
                    ps.setString(2, dados[5].replaceAll("\\s",""));
                    ps.executeUpdate();	
                    conn.commit();
                    System.out.println("Register success");
                    return true;
                }catch(SQLException e){
                    e.printStackTrace();
                }

            } catch (SQLException ex) {
                Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Register failure");
        return false;
    }
    //Lista as notificações de um cliente
    public ArrayList<String> listNotifications(Cliente cliente){
        String query,query2;
        ArrayList<ArrayList<String>> message = new ArrayList<>();
        ArrayList<String> aux;
        ArrayList<String> stringfinal = new ArrayList<>();
        try {
            stmt = conn.createStatement();
            sp = conn.setSavepoint();
            query = "SELECT ID,TEXT,TYPE,CLIENTENVIOU "
                    +"FROM NOTIFICATION "
                    +"WHERE CLIENTNAME LIKE ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, cliente.getUsername());
            ResultSet res = ps.executeQuery();
            while(res.next()){
                aux = new ArrayList<>();
                aux.add(res.getString("TYPE"));
                aux.add(String.valueOf(res.getInt("ID")));
                aux.add(res.getString("CLIENTENVIOU"));
                aux.add(res.getString("TEXT"));
                message.add(aux);
                query2 = "DELETE FROM NOTIFICATION "
                        +"WHERE ID LIKE ?";
                PreparedStatement ps2 = conn.prepareStatement(query2);
                ps2.setString(1,String.valueOf(res.getInt("ID")));
                ps2.executeQuery();
                conn.commit();
            }
           res.close();
        } catch (SQLException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(int i =0 ;i<message.size();i++){
            if(message.get(i).get(0).equalsIgnoreCase("notification_message")){
                stringfinal.add("type: notification_message, id:"+message.get(i).get(1) + ",user: "+ message.get(i).get(2) +",text:"+message.get(i).get(3));
 
            }
            else{
                stringfinal.add("type: notification_bid, id: "+message.get(i).get(1)+", user: "+message.get(i).get(2)+", amount: "+message.get(i).get(3));
            }
        }
        return stringfinal;
    }
    //Cria um leilão
    public synchronized boolean create_auction(String[] dados,Cliente cliente){
        if(dados.length>=11){
            Leilao novoleilao = null;
            String code,title,description,deadline,amount;
            long longcode;
                code = dados[2].replaceAll("\\s","");
            try{
                longcode = Long.parseLong(dados[3].replaceAll("\\s",""));
            }catch(Exception e){
                return false;
            }
            String query;
            title = dados[4].replaceAll("\\s","");
            description = dados[6].replaceAll("\\s","");
            deadline = dados[8].replaceAll("\\s","");
            amount = dados[10].replaceAll("\\s","");

            String[] aux = dados[9].split(" ");
            if(aux[0].equals("")){
                aux[0] = aux[1];
                aux[1] = aux[2].replace("-",":");
            }
            else{
                aux[0] = aux[0];
                aux[1] = aux[1].replace("-",":");
            }
            SimpleDateFormat dateformat2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String strdate2 = String.format("%s %s:00",aux[0],aux[1]);
            java.sql.Date sqlDate;
            try{
                java.util.Date newdate = (java.util.Date) dateformat2.parse(strdate2);
                sqlDate = new java.sql.Date(newdate.getTime());
            }
            catch(ParseException es){
                es.printStackTrace();
                return false;
            }
            int id,maxid=0;
            if(code.equalsIgnoreCase("code") && title.equalsIgnoreCase("title") && description.equalsIgnoreCase("description") && deadline.equalsIgnoreCase("deadline") && amount.equalsIgnoreCase("amount")){
                    try {
                        stmt = conn.createStatement();
                        query = "SELECT ID FROM LEILAO";
                        ResultSet res = stmt.executeQuery(query);
                        while(res.next()){
                            id = res.getInt("ID");
                            if(id>maxid)
                                maxid = id;
                        }
                        res.close();
                        maxid++;
                        stmt = conn.createStatement();
                        query = "SELECT ID FROM Leilao WHERE CLIENTNAME LIKE '" + cliente.getUsername() + "' AND DESCRIPTION LIKE ? AND ITEM LIKE ? AND TITLE LIKE ? AND MAXAMOUNT LIKE ?";
                        PreparedStatement ps=conn.prepareStatement(query);
                        ps.setString(1,dados[7]);
                        ps.setLong(2, longcode);
                        ps.setString(3,dados[5]);
                        ps.setString(4,dados[11].replaceAll("\\s",""));
                        res = ps.executeQuery();
                        if(!res.next()){
                            sp= conn.setSavepoint();
                            query = "INSERT INTO Leilao (ID,CLIENTNAME,MAXAMOUNT,TITLE,DESCRIPTION,ITEM,DEADLINE) "+"VALUES ('"+ maxid+"','"+ cliente.getUsername()+ "',?,?,?,?,?)";
                            ps=conn.prepareStatement(query);
                            ps.setString(1,dados[11].replaceAll("\\s",""));
                            ps.setString(2,dados[5]);
                            ps.setString(3,dados[7]);
                            ps.setLong(4, longcode);
                            ps.setDate(5, sqlDate);
                            ps.executeQuery();
                            conn.commit();
                            System.out.println("AUCTION CREATED SUCCESSFULLY");
                            return true;
                        }
                        else{
                            res.close();
                            System.out.println("AUCTION ALREADY EXISTS");
                        }
               
                        
                } catch (SQLException ex) {
                    Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }
    //Função que retorna uma String com os leilões que existem do artigo com o código fornecido como parâmetro
    public synchronized String search_item(String[] dados){
        try{
            String query;
            long code;
            try{
                code = Long.parseLong(dados[3].replaceAll("\\s",""));
            }
            catch(Exception e){
                return null;
            }
            String respostaaux = "";
            int contador = 0;
            long id;
            String title;
            stmt = conn.createStatement();
            query = "SELECT ID,title "+"FROM Leilao "+"WHERE ITEM LIKE ?";
            PreparedStatement ps=conn.prepareStatement(query);
            ps.setLong(1, code);
            ResultSet res = ps.executeQuery();
            while(res.next()){
                id = res.getLong("id");
                title = res.getString("title");
                respostaaux = respostaaux.concat(", items_" + contador + "_id: " + id + ", items_" + contador + "_code: " + code + ", items_" + contador + "_title: " + title);
                contador++;
            }
            res.close();
            //concatena na string, os leiloes em que o artigo está inserido
            String resposta = String.format("type: search_auction, items_count: " + contador);
            if(contador>0)
                resposta = resposta.concat(respostaaux);
            return resposta;
        }
        catch(SQLException ex){
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return String.format("type: search_auction, items_count: 0 ");

    }
    public boolean logout(Cliente cliente){
        try {
            String query = "UPDATE Cliente "
                    +"SET ativo = ? "
                    +"WHERE NOME LIKE ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1,0);
            ps.setString(2,cliente.getUsername());
            ps.executeQuery();
            conn.commit();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    //vai buscar os utilizadores onlines à base de dados e imprime um string que contém os utilizadores online
    public synchronized String onlineUsers(){
        String query;
        ArrayList<String> online = new ArrayList<>();
        try {
            stmt = conn.createStatement();
            sp = conn.setSavepoint();
            query = "SELECT NOME "
                    +"FROM CLIENTE "
                    +"WHERE ATIVO LIKE ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1,"1");
            ResultSet res = ps.executeQuery();
            while(res.next()){
                online.add(res.getString("NOME"));
            }
            String resposta = String.format("type: online_users, users_count: %d",online.size());
            for(int i=0;i<online.size();i++){
                resposta = resposta.concat(", users_" + i + "_username: " + online.get(i));
            }
            return resposta;
        } catch (SQLException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    //Função que retorna true se o leilão já estiver nas atividades do cliente
    public synchronized boolean search_auction_on_client(int id,Cliente cliente){
            ArrayList<Leilao> lista = cliente.getLeiloesatividade();
        for(int i=0;i<lista.size();i++){
            if(id ==lista.get(i).getId()){
                return true;
            }
        }
        return false;
    }
    //Função que retorna uma String com os detalhes do leilão com o id fornecido como parâmetro
    public synchronized String detail_auction(String[] dados){
   
        int id;
        try{
            id = Integer.parseInt(dados[3].replaceAll("\\s",""));
        }
        catch(NumberFormatException e){
            return null;
        }
         String query,query2,query3,query5,query4;
        boolean exists = false;
        String titulo=new String(),description=new String(),text = new String(),username,username_bid;
        int amount;
        java.sql.Date deadline = null;
        ArrayList<ArrayList<String>> messages = new ArrayList<>();
        ArrayList<ArrayList<String>> bids = new ArrayList<>();
        ArrayList<String>aux;
        ResultSet res;
        try {
            stmt = conn.createStatement();
            query = "SELECT DEADLINE,TITLE,DESCRIPTION "
                +"FROM Leilao "
                +"WHERE ID LIKE ?";
            PreparedStatement ps=conn.prepareStatement(query);
            ps.setInt(1, id);
            res = ps.executeQuery();
            while(res.next()){
                titulo = res.getString("TITLE");
                deadline = res.getDate("DEADLINE");
                description = res.getString("DESCRIPTION");
                exists = true;
        }
        res.close();
       
       if(exists){
           query2 = "SELECT TEXT, CLIENTNAME "
                   +"FROM MESSAGE "
                   +"WHERE LEILAOID LIKE ?";
           ps=conn.prepareStatement(query2);
           ps.setInt(1, id);
           res = ps.executeQuery();
           while(res.next()){
               aux = new ArrayList<>();
               aux.add(res.getString("CLIENTNAME"));
               aux.add(res.getString("TEXT"));
               messages.add(aux);
           }
           res.close();
           query3 = "SELECT AMOUNT,CLIENTNAME "
                   +"FROM BID "
                   +"WHERE LEILAOID LIKE ?";
           ps = conn.prepareStatement(query3);
           ps.setInt(1, id);
           res = ps.executeQuery();
           while(res.next()){
               aux = new ArrayList<>();
               aux.add(res.getString("CLIENTNAME"));
               aux.add(String.valueOf(res.getInt("AMOUNT")));
               bids.add(aux);
           }
           res.close();
            String resposta;
            resposta = String.format("type: detail_auction, title: %s, description: %s, deadline: %s, messages_count: %d",titulo,description,deadline.toString(),messages.size());
            //concatena os detalhes das mensagens à string
            for(int i=0;i<messages.size();i++){
                resposta = resposta.concat(", messages_" + i + "_user: " + messages.get(i).get(0) + ", messages_" + i + "_text: " + messages.get(i).get(1));
            }
            resposta = resposta.concat(", bids_count: " + bids.size());
            //concatena os detalhes das bids à string
            for(int i=0;i<bids.size();i++){
                resposta = resposta.concat(", bids_" + i + "_user: " + bids.get(i).get(0) + ", bids_" + i + "_amount: " + bids.get(i).get(1));
            }
            return resposta;
            }
       else{
           System.out.println("Auction not found");
       }
        } catch (SQLException ex) {
           ex.printStackTrace();
           return null;
        }   
        return null;
    }
    //Função que retorna os leilões em que o cliente teve alguma atividade
   public synchronized String my_auctions(Cliente cliente){
        String query,query2,query3;
        ArrayList <ArrayList<String>> leiloes = new ArrayList<>();
        ArrayList<String> aux;
        try {
            stmt = conn.createStatement();
                    //Leiloes que o cliente criou
            query = "SELECT ID,ITEM,TITLE "
                    +"FROM Leilao "
                    +"WHERE CLIENTNAME LIKE'" + cliente.getUsername()+"'";
            ResultSet res = stmt.executeQuery(query);
            while(res.next()){
                aux = new ArrayList<>();
                aux.add(res.getString("ID"));
                aux.add(res.getString("ITEM"));
                aux.add(res.getString("TITLE"));
                leiloes.add(aux);
            }
            res.close();
            query2 = "SELECT ID "
                    +"FROM bid "
                    +"WHERE CLIENTNAME LIKE'"+cliente.getUsername()+"'";
            res = stmt.executeQuery(query2);
            while(res.next()){
                query3 = "SELECT ID,ITEM,TITLE "
                        +"FROM Leilao "
                        +"WHERE ID LIKE'" + res.getString("ID") + "'";
                ResultSet res2 = stmt.executeQuery(query3);
                while(res2.next()){
                  aux = new ArrayList<>();
                  aux.add(res2.getString("ID"));
                  aux.add(res2.getString("ITEM"));
                  aux.add(res2.getString("TITLE"));
                  leiloes.add(aux);
                }
            }
            res.close();
        } catch (SQLException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        String resposta = String.format("type: my_auctions, items_count: %d",leiloes.size());
        //concate os detalhes dos leilões na string
        for(int i=0;i<leiloes.size();i++){
            resposta = resposta.concat(", items_" + i + "_id: " + leiloes.get(i).get(0) + ", items_" + i + "_code: " + leiloes.get(i).get(1)+ ", items_" + i + "_title: " + leiloes.get(i).get(2));                                
        }
        return String.format(resposta);
    }
    //Função que efetua uma licitação num leilão
    public synchronized boolean bid_on_auction(String[] dados,Cliente cliente){
        Leilao leilao = null;
        int amount,id,amountComp = 1000000;
        try{
            id = Integer.parseInt(dados[3].replaceAll("\\s", ""));
            amount = Integer.parseInt(dados[5].replaceAll("\\s",""));
        }
        catch(Exception e){
            return false;
        }
        
        String query;
        try {
            query = "SELECT deadline,minBidID,maxAmount "+"FROM Leilao "+"WHERE ID LIKE ?";
            PreparedStatement ps=conn.prepareStatement(query);
            ps.setInt(1, id);
            int minBidId;
            ResultSet res = ps.executeQuery();
            if(res.next()){
                java.sql.Date deadline = res.getDate("deadline");
                amountComp = res.getInt("maxAmount");
                minBidId = res.getInt("minBidID");
                LocalDateTime localdate = LocalDateTime.now();
                Instant instant = localdate.toInstant(ZoneOffset.UTC);
                java.util.Date date = (java.util.Date) java.util.Date.from(instant);
                if(deadline.compareTo(date)<0){
                    return false;
                }
                String query2 = "SELECT AMOUNT "
                                +"FROM BID "
                                +"WHERE ID LIKE'" +minBidId + "'";
                ResultSet res2 = stmt.executeQuery(query2);
                if(res2.next()){
                    amountComp = res2.getInt("AMOUNT");
                }
                res2.close();
            }
            res.close();
        } catch (SQLException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        //se o valor da licitação for o mais baixo efetuado
        if(amount<amountComp){
            int idatual,maxid=0;
            try {
                query = "SELECT ID FROM BID";
                ResultSet res = stmt.executeQuery(query);
                while(res.next()){
                    idatual = res.getInt("ID");
                    if(idatual>maxid)
                        maxid=idatual;
                }
                res.close();
                maxid++;
                query = "INSERT INTO BID(AMOUNT, LEILAOID, id, CLIENTNAME) "
                        +"VALUES (?,?,?,'"+cliente.getUsername()+"')";
                PreparedStatement ps=conn.prepareStatement(query);
                ps.setInt(1, amount);
                ps.setInt(2, id);
                ps.setInt(3, maxid);
                ps.executeQuery();
                conn.commit();
                query = "UPDATE LEILAO "
                +"SET MINBIDID = ? "
                +"WHERE ID LIKE ?";
                ps = conn.prepareStatement(query);
                ps.setInt(1, maxid);
                ps.setInt(2,id);
                ps.executeQuery();
                conn.commit();             
                Notify_Bid(id,amount,cliente);
                return true;
            } catch (SQLException ex) {
                Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    //Função que notifica os utilizadores de uma licitação efetuada num leilão
public synchronized void Notify_Bid(int id,int amount, Cliente winner){
        String query,query2;
        ArrayList<String> clientes = new ArrayList<>();
        try {
            stmt = conn.createStatement();
            sp = conn.setSavepoint();
            query = "SELECT CLIENTNAME "
                    +"FROM BID "
                    +"WHERE LEILAOID LIKE ?";
           PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1,String.valueOf(id));
            ResultSet res = ps.executeQuery();
            boolean encontrou;
            while(res.next()){
                String client = res.getString("CLIENTNAME");
                encontrou = false;
                if(!client.equalsIgnoreCase(winner.getUsername())){
                    for(int i=0;i<clientes.size();i++){
                        if(clientes.get(i).equalsIgnoreCase(client)){
                            encontrou = true;
                        }
                    }
                    if(!encontrou)
                        clientes.add(client);
                }
            }
            int idatual,maxid=0;
            res.close();
            stmt = conn.createStatement();
            query = "SELECT ID FROM NOTIFICATION";
            res = stmt.executeQuery(query);
            while(res.next()){
                idatual = res.getInt("ID");
                if(id>maxid)
                    maxid = idatual;
            }
            res.close();
            maxid++;
           query2 = "INSERT INTO NOTIFICATION (ID,CLIENTNAME,TEXT,TYPE,CLIENTENVIOU) "+"VALUES (?,?,?,?,?)";
           for(int i =0;i<clientes.size();i++){
                PreparedStatement ps2 = conn.prepareStatement(query2);
                ps2.setInt(1,maxid);
                ps2.setString(2,clientes.get(i));
                ps2.setInt(3,amount);
                ps2.setString(4,"notification_bid");
                ps2.setString(5,winner.getUsername());
                ps2.executeQuery();
                conn.commit();
           }
           
        } catch (SQLException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //Função que notifica os utilizadores de uma mensagem enviada num leilão
    public synchronized void Notify_Message(int id, Cliente cliente,String text){
        String query,query2;
        ArrayList<String> usernames = new ArrayList<>();
        try {
            stmt = conn.createStatement();
            sp = conn.setSavepoint();
            query = "SELECT CLIENTNAME "
                    +"FROM MESSAGE "
                    +"WHERE LEILAOID LIKE ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1,String.valueOf(id));
            ResultSet res = ps.executeQuery();
            boolean encontrou;
            while(res.next()){
                String client = res.getString("CLIENTNAME");
                encontrou = false;
                if(!client.equalsIgnoreCase(cliente.getUsername())){
                    for(int i=0;i<usernames.size();i++){
                        if(usernames.get(i).equalsIgnoreCase(client)){
                            encontrou = true;
                        }
                    }
                    if(!encontrou)
                        usernames.add(client);
                }               
            }
            int idatual,maxid=0;
            res.close();
            stmt = conn.createStatement();
            query = "SELECT ID FROM NOTIFICATION";
            res = stmt.executeQuery(query);
            while(res.next()){
                idatual = res.getInt("ID");
                if(id>maxid)
                    maxid = idatual;
            }
            res.close();
            maxid++;
           query2 = "INSERT INTO NOTIFICATION (ID,CLIENTNAME,TEXT,TYPE,CLIENTENVIOU) "+"VALUES (?,?,?,?,?)";
           for(int i =0;i<usernames.size();i++){
                PreparedStatement ps2 = conn.prepareStatement(query2);
                ps2.setInt(1,maxid);
                ps2.setString(2,usernames.get(i));
                ps2.setString(3,text);
                ps2.setString(4,"notification_message");
                ps2.setString(5,cliente.getUsername());
                ps2.executeQuery();
               conn.commit();
           }            
        } catch (SQLException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //Retorna true se o cliente estiver na lista de clientes 
    public synchronized boolean search_client(ArrayList<Cliente> lista,Cliente cliente){
        for(int i=0;i<lista.size();i++){
            if(lista.get(i) == cliente){
                return true;
            }
        }
        return false;
    }
    //Insere a mensagem no leilão
    public synchronized boolean setMessage(String[] dados,Cliente cliente){
        int id;
        try{
            id = Integer.parseInt(dados[3].replaceAll("\\s",""));
        }
        catch(NumberFormatException e){
            return false;
        }
        String query,query2;
        int idatual,maxid=0;
        try {
            stmt = conn.createStatement();
            query = "SELECT ID FROM MESSAGE";
            ResultSet res = stmt.executeQuery(query);
            while(res.next()){
                idatual = res.getInt("ID");
                if(idatual>maxid)
                    maxid = idatual;
            }
            res.close();
            maxid++;
            stmt = conn.createStatement();
            sp = conn.setSavepoint();
            query = "SELECT * "
                    +"FROM Leilao "
                    +"WHERE ID LIKE ?";
            PreparedStatement ps=conn.prepareStatement(query);
            ps.setInt(1, id);
            res = ps.executeQuery();
            if(res.next()){
                query2 = "INSERT INTO MESSAGE(ID,CLIENTNAME,TEXT,LEILAOID) "
                    +"VALUES (?,'" + cliente.getUsername() +"',?,?)";
                ps = conn.prepareStatement(query2);
                ps.setInt(1, maxid);
                ps.setString(2, dados[5]);
                ps.setInt(3, id);
                ps.executeQuery();
                conn.commit();
                res.close();
                Notify_Message(id,cliente,dados[5]);
                return true;
            }
            else{
                return false;
            }
           
           
        } catch (SQLException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    //Função que edita dados de um leilão
    public synchronized boolean edit_auction(String[] dados,Cliente cliente){
        int id;
        try{
            id = Integer.parseInt(dados[3].replaceAll("\\s",""));
        }
        catch(NumberFormatException e){
            return false;
        }
        String query;
        try {
            stmt = conn.createStatement();
            sp = conn.setSavepoint();
            query = "SELECT CLIENTNAME "
                    + "FROM LEILAO "
                    + "WHERE ID LIKE ?";
            PreparedStatement ps=conn.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();
            if(res.next()){
                if(!cliente.getUsername().equalsIgnoreCase(res.getString("CLIENTNAME")))
                    return false;
            }
             res.close();      
        for(int i=4;i<dados.length;i+=2){
            if(dados[i+1]!=null){
                switch(dados[i].replaceAll("\\s","")){
                    case "title":
                        query = "UPDATE Leilao "
                                +"SET TITLE = ? "
                                +"WHERE ID LIKE ?";
                        ps = conn.prepareStatement(query);
                        ps.setString(1,dados[i+1]);
                        ps.setInt(2,id);
                        ps.executeQuery();
                        conn.commit();
                        break;
                    case "description":
                        query = "UPDATE Leilao "
                                +"SET DESCRIPTION = ? "
                                +"WHERE ID LIKE ?";
                        ps = conn.prepareStatement(query);
                        ps.setString(1,dados[i+1]);
                        ps.setInt(2,id);
                        ps.executeQuery();
                        conn.commit();
                        break;
                    case "deadline":
                        String[] aux = dados[5].split(" ");
                        if(aux[0].equals("")){
                            aux[0] = aux[1];
                            aux[1] = aux[2].replace("-",":");
                        }
                        else{
                            aux[1] = aux[1].replace("-",":");
                        }
                        SimpleDateFormat dateformat2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        String strdate2 = String.format("%s %s:00",aux[0],aux[1]);
                        java.sql.Date sqlDate;
                        try{
                            java.util.Date newdate = (java.util.Date) dateformat2.parse(strdate2);
                            sqlDate = new java.sql.Date(newdate.getTime());
                        }
                        catch(ParseException es){
                            es.printStackTrace();
                            return false;
                        }
                        query = "UPDATE Leilao "
                                +"SET DEADLINE = ? "
                                +"WHERE ID LIKE ?";
                        ps = conn.prepareStatement(query);
                        ps.setDate(1,sqlDate);
                        ps.setInt(2,id);
                        ps.executeQuery();
                        conn.commit();
                        break;
                    default:
                        return false;
                }
            }           
            else
                return false;
        }
        } catch (SQLException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    //Função que cancela um leilão
    public synchronized boolean cancel_auction(String[] dados){
        String query;
        int id;
        //ArrayList<Leilao> leiloes = bd.getListaleiloes();
        try{
            id = Integer.parseInt(dados[3].replaceAll("\\s",""));
        }
        catch(NumberFormatException e){
            return false;
        }
        LocalDateTime localdate = LocalDateTime.now();
        Instant instant = localdate.toInstant(ZoneOffset.UTC);
        java.util.Date date = (java.util.Date) java.util.Date.from(instant);
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        try {
            stmt = conn.createStatement();
            sp = conn.setSavepoint();
            query = "SELECT * "
                    +"FROM LEILAO "
                    +"WHERE ID LIKE'" + id +"'";
            ResultSet res = stmt.executeQuery(query);
            if(res.next()){
                query = "UPDATE Leilao "
                                +"SET DEADLINE = '" + sqlDate+"' "
                                +"WHERE ID LIKE '" + id + "'";
                                stmt.executeQuery(query);
                                conn.commit();
                                res.close();
                                return true;
            }
            else{
                return false;
            }
           
        } catch (SQLException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    //Função que ban um utilizador
    public synchronized boolean banUser(String[] dados){
        String query,query2,query3,query4;
        String username = dados[3].replaceAll("\\s","");
        try {
            stmt = conn.createStatement();
            sp = conn.setSavepoint();
           
            query = "SELECT nome "
                    +"FROM Cliente "
                    +"WHERE nome LIKE ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1,username);
            ResultSet res = ps.executeQuery();
            if(res.next()){
                //Elimina Utilizador
                query2 = "DELETE FROM cliente "
                    +"WHERE nome LIKE ?";
            PreparedStatement ps2 = conn.prepareStatement(query2);
            ps2.setString(1,username);
            ps2.executeQuery();
            conn.commit();
            return true;
            }
            else{
                System.out.println("Utilizador nao existente");
                return false;
            }
           
        } catch (SQLException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public static void startRmi(){
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DATABASE_URL,USERNAME,PASSWORD);
            stmt = (Statement) conn.createStatement();
            conn.setAutoCommit(false);
            System.out.println("Connected!");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(RmiServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            ConnectionRMI h = new RmiServer();
            LocateRegistry.createRegistry(1099).rebind("RmiServer1", h);
			System.out.println("Server1 RMI ready!");
		} catch (RemoteException re) {
			System.out.println("Exception in RmiServer.main: " + re);
		}    
    
}
    
    
    
    public static void main(String args[]) {
        startRmi();
    }
}
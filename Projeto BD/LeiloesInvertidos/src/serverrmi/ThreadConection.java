/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverrmi;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
/**
 *
 * @author Martins
 */
public class ThreadConection extends Thread implements Serializable{
   private BufferedReader inFromClient;
   private PrintWriter outToClient;
   private Socket clientSocket;
   private int thread_number;
   private ConnectionRMI rmi;
    public ThreadConection(ConnectionRMI rmi,Socket clientSocket, int thread_number) {
        this.thread_number = thread_number;
        this.rmi = rmi;
        try{
            this.clientSocket = clientSocket;
            this.inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //buffer com o que se recebe do cliente
            this.outToClient = new PrintWriter(clientSocket.getOutputStream(), true); //buffer com o que se envia para o cliente
            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }
    @Override
    public void run(){
        ConnectClient();        
    }
    //verifica se o utilizador tem alguma notificação pendente, se tiver, imprime no cliente e apaga as notificações pendentes
    private synchronized void hasNotifications(Cliente cliente) throws RemoteException{
        ArrayList<String> notifications = rmi.listNotifications(cliente);
        if(notifications.size()>0){
            for(int i=0;i<notifications.size();i++){
                outToClient.println(notifications.get(i));
            }      
        }
    }

    private void ConnectClient(){
        String resposta;
        String reader;
        String[] dados;
        Cliente cliente = null;
        ArrayList<Cliente> onlineusers;
        try{
            while(true){
                //percorre este ciclo até o utilizador fazer login
                resposta = "";           
                reader = inFromClient.readLine(); //fica à espera que o cliente lhe envie alguma coisa
                dados = reader.split("[^a-zA-Z0-9'_ -]+");
                if(dados.length>=2){
                    String lineopcao = dados[1].replaceAll("\\s",""); //le a operação que o cliente quer executar (valor do type)
                    if(lineopcao.equalsIgnoreCase("login")){
                        if(dados.length == 6){
                            System.out.println("[" + thread_number + "] Received a valid login request...");
                            if((cliente = rmi.makelogin(dados))!=null){ //operação que pesquisa as credenciais na base de dados (ficheiro) e retorna true se existir
                                resposta = String.format("type: login, ok: true");
                                System.out.println("[" + thread_number + "] Sucessful login!");
                                outToClient.println(resposta);
                                menu(cliente); //redireciona para o menu
                                return;
                            }
                            else{
                                resposta = String.format("type: login, ok: false");
                                System.out.println("[" + thread_number + "] Unsucessful login");
                            }
                        }
                        else{
                            System.out.println("[" + thread_number + "] Received an invalid login request...");
                            resposta = String.format("type: login, ok: false");    
                        }
                    }
                    if(lineopcao.equalsIgnoreCase("register")){
                        if(dados.length == 6){
                            System.out.println("[" + thread_number + "] Received a valid register request...");
                            if(rmi.makeregister(dados)){ //operação que regista uma conta na base de dados (ficheiro) e retorna true se os dados forem válidos
                                resposta = String.format("type: register, ok: true");
                                System.out.println("[" + thread_number + "] Sucessful register!");
                            }
                            else{
                                resposta = String.format("type: register, ok: false");
                                System.out.println("[" + thread_number + "] Unsucessful register!");
                            }
                        }
                    }
                
                }
                outToClient.println(resposta);
            }
            //Ao acontecer uma exception, vai decrementar na carga do server -1, porque o user deixa de estar ativo
        }catch(EOFException e){
            System.out.println("[" + thread_number + "] EOF:" + e);
            int contador;
        }catch(IOException e){
            System.out.println("[" + thread_number + "] IO:" + e);
        }
    }
    //Depois de fazer login, o utilizador é redirecionado para esta funcao
     private void menu(Cliente cliente2){
        Cliente cliente = cliente2;
         
        String resposta = "";
        String auxstring;
        String reader;
        String[] dados;
        Cliente aux;
        String caracteres = ",:";
        ArrayList<Cliente> onlineusers;
        try{
            while(true){
                resposta = "";
                hasNotifications(cliente); //verifica se o user tem notificações pendentes
                reader = inFromClient.readLine(); //fica a espera que o cliente lhe envie alguma coisa
                dados = reader.split("[" + Pattern.quote(caracteres) + "]");
                if(dados.length>=2){
                    String lineopcao = dados[1].replaceAll("\\s","");             
                    if(lineopcao.equalsIgnoreCase("create_auction")){
                        if(dados.length>=12){
                            System.out.println("[" + thread_number + "] Received a valid create_auction request...");
                            if(rmi.create_auction(dados,cliente)){ //funcao que regista um novo leilao e retorna true se os dados forem válidos
                                resposta = String.format("type: create_auction, ok: true");
                                System.out.println("[" + thread_number + "] Auction Created Sucessfully!");
                            }
                            else{
                                resposta = String.format("type: create_auction, ok: false");
                                System.out.println("[" + thread_number + "] Auction not Created Sucessfully!");
                            }
                        }
                    }
                    else if(lineopcao.equalsIgnoreCase("logout")){
                        if(dados.length==2){
                            System.out.println("[" + thread_number + "] Received a valid logout request...");
                            if(rmi.logout(cliente)){ //retorna true se bem sucedido
                                resposta = String.format("type: logout, ok: true");
                                outToClient.println(resposta);
                                ConnectClient();
                                System.out.println("[" + thread_number + "] Logout answered Successfuly");
                                return;
                            }
                            else{
                                resposta = String.format("type: logout, ok: false");
                                System.out.println("["+thread_number+"] Logout not answered Successfuly");
                            }
                        }
                    }
                    else if(lineopcao.equalsIgnoreCase("search_auction")){
                        if(dados.length==4){
                            System.out.println("[" + thread_number + "] Received a valid search_auction request...");
                            if((auxstring = rmi.search_item(dados))!=null){ //retorna uma string com os leiloes no qual o artigo está inserido
                                resposta = auxstring;
                                System.out.println("[" + thread_number + "] Search_Auction answered Successfuly");
                            }
                            else{
                                resposta = String.format("type: search_auction, items_count: 0");
                                System.out.println("["+thread_number+"] Search_Auction not answered Successfuly");
                            }
                        }
                    }
                    else if(lineopcao.equalsIgnoreCase("detail_auction")){
                        if(dados.length==4){
                            System.out.println("[" + thread_number + "] Received a valid detail_auction request...");
                          if((auxstring = rmi.detail_auction(dados))!=null){ //retorna uma string com os dados dos leiloes
                              resposta = auxstring;
                              System.out.println("[" + thread_number + "] Detail_Auction answered Successfuly");
                          }
                          else{
                              resposta = String.format("type: detail_auction, ok: false");
                              System.out.println("["+thread_number+"] Detail_Auction not answered Successfuly");
                          }
                        }
                    }
                    else if(lineopcao.equalsIgnoreCase("my_auctions")){
                        if(dados.length==2){
                            System.out.println("[" + thread_number + "] Received a valid my_auctions request...");
                            resposta = rmi.my_auctions(cliente); //retorna uma string com os leilões em que o utilizador teve alguma atividade                            
                            System.out.println("[" + thread_number + "] my_auctions request answered Successfuly");
                            
                        }
                    }
                    else if(lineopcao.equalsIgnoreCase("bid")){
                        if(dados.length==6){
                            System.out.println("[" + thread_number + "] Received a valid bid request...");
                            if(rmi.bid_on_auction(dados,cliente)){ //efetua uma bid num leilão
                                resposta = String.format("type: bid, ok: true");
                                System.out.println("[" + thread_number + "] Bid made sucessfully!");
                            }
                            else{
                                resposta = String.format("type: bid, ok: false");
                                System.out.println("[" + thread_number + "] Bid not made sucessfully!");
                            }
                        }
                    }
                    else if(lineopcao.equalsIgnoreCase("edit_auction")){
                        if(dados.length>=6){
                            System.out.println("[" + thread_number + "] Received a valid edit_auction request...");
                            if(rmi.edit_auction(dados,cliente)){ //edita dados de um determinado leilão, se o cliente for o criador do leilao
                                resposta = String.format("type: edit_auction, ok: true");
                                System.out.println("[" + thread_number + "] Auction edited sucessfully!");
                            }
                            else{
                                resposta = String.format("type: edit_auction, ok: false");
                                System.out.println("[" + thread_number + "] Auction didn't edit sucessfully!");
                            }
                        }
                    }
                    else if(lineopcao.equalsIgnoreCase("message")){
                        if(dados.length>=6){
                            System.out.println("[" + thread_number + "] Received a valid message request...");
                            if(rmi.setMessage(dados,cliente)){ //insere uma mensagem no leilão
                                resposta = String.format("type: message, ok: true");
                                System.out.println("[" + thread_number + "] Message sent sucessfully!");
                            }
                            else{
                                resposta = String.format("type: message, ok: false");
                                System.out.println("[" + thread_number + "] Message didn't send sucessfully!");
                            }
                        }
                        else{
                            resposta = String.format("type: message, ok: false");
                        }
                    }
                    else if(lineopcao.equalsIgnoreCase("online_users")){
                        if(dados.length==2){
                            System.out.println("[" + thread_number + "] Received a valid online_users request...");
                            if((auxstring = rmi.onlineUsers())!=null){ // retorna os utilizadores online
                                resposta = auxstring;
                                System.out.println("[" + thread_number + "] Online_users answer sent sucessfully!");
                            }
                        }
                        else{
                            resposta = String.format("type: online_users, ok: false");
                        }
                    }
                    if(lineopcao.equalsIgnoreCase("cancel_auction")){
                        if(dados.length ==4){
                            System.out.println("[" + thread_number + "] Received a valid ban request...");
                            if(rmi.cancel_auction(dados)){ //cancela um leilão
                                resposta = String.format("type: cancel_auction, ok: true");
                                System.out.println("[" + thread_number + "] Auction canceled sucessfully!");
                            }
                            else{
                                resposta = String.format("type: cancel_auction, ok: false");
                                System.out.println("[" + thread_number + "] Unable to cancel auction!");
                            }
                        }    
                    }
                    else if(lineopcao.equalsIgnoreCase("ban")){
                        if(dados.length == 4){
                            System.out.println("[" + thread_number + "] Received a valid ban request...");
                            if(rmi.banUser(dados)){ //ban um user e todas as suas contribuições
                                resposta = String.format("type: ban, ok: true");
                                System.out.println("[" + thread_number + "] User banned sucessfully!");
                            }
                            else{
                                resposta = String.format("type: ban, ok: false");
                                System.out.println("[" + thread_number + "] Unable to ban user!");
                            }
                        }
                    }
                    
               }
                outToClient.println(resposta);                
            }
        }catch(EOFException e){
            System.out.println("[" + thread_number + "] EOF:" + e);
            try {
                rmi.logout(cliente);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }catch(IOException e){
            System.out.println("[" + thread_number + "] IO:" + e);
            try {
                rmi.logout(cliente);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        
    }
    
    
   
    
   
    
    
    
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverrmi;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;




/**
 *
 * @author João
 */
public class TCPServer{
    private static ConnectionRMI atualRmi;
   
    public static void main(String args[]) throws IOException {
		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new RMISecurityManager());
                //Conexão do RMI
		try {
                    atualRmi = (ConnectionRMI) Naming.lookup("RmiServer1");
                    System.out.println("TCP connected to RMI");
		} catch (Exception e) {
                    System.out.println("Exception in main: " + e);
		}
                   //Conexão aos Clientes
                    int serverport = 0;
                    try{
                       serverport = Integer.parseInt(args[0]);
                    }
                    catch(NumberFormatException e){
                        e.printStackTrace();
                        return;
                    }
                    int numberthread=0;
                    System.out.println("A Escuta no Porto " + serverport);
                    ServerSocket listenSocket = null;
                    try {
                        listenSocket = new ServerSocket(serverport);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    System.out.println("LISTEN SOCKET="+listenSocket);
                    while(true) {
                        Socket clientSocket = listenSocket.accept(); // procura que algum cliente se conecte no porto.
                        System.out.println("CLIENT_SOCKET (created at accept())="+clientSocket);
                        new ThreadConection(atualRmi,clientSocket,numberthread++);
                    }
    }
}

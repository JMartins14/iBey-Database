/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverrmi;

import java.rmi.Remote;
import java.util.ArrayList;


public interface ConnectionRMI extends Remote{
    public  Cliente makelogin(String[] dados) throws java.rmi.RemoteException;
    public boolean makeregister(String[] dados) throws java.rmi.RemoteException;
    public boolean create_auction(String[] dados,Cliente cliente) throws java.rmi.RemoteException;
    public String search_item(String[] dados)throws java.rmi.RemoteException;
    public boolean search_auction_on_client(int id,Cliente cliente)throws java.rmi.RemoteException;
    public String detail_auction(String[] dados)throws java.rmi.RemoteException;
    public String my_auctions(Cliente cliente)throws java.rmi.RemoteException;
    public boolean bid_on_auction(String[] dados,Cliente cliente)throws java.rmi.RemoteException;
    public void Notify_Bid(int id,int amount, Cliente winner) throws java.rmi.RemoteException;
    public void Notify_Message(int id, Cliente cliente,String text) throws java.rmi.RemoteException;
    public boolean search_client(ArrayList<Cliente> lista,Cliente cliente)throws java.rmi.RemoteException;
    public boolean setMessage(String[] dados,Cliente cliente)throws java.rmi.RemoteException;
    public boolean edit_auction(String[] dados,Cliente cliente)throws java.rmi.RemoteException;
    public boolean banUser(String[] dados) throws java.rmi.RemoteException;
    public boolean cancel_auction(String[] dados) throws java.rmi.RemoteException;
    public  boolean test() throws java.rmi.RemoteException;
    public String onlineUsers() throws java.rmi.RemoteException;
    public boolean logout(Cliente cliente) throws java.rmi.RemoteException;
    public ArrayList<String> listNotifications(Cliente cliente) throws java.rmi.RemoteException;




}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverrmi;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author João
 */
public class Cliente implements Serializable{
    private String username;
    private String password;
    private ArrayList<Leilao> leiloesatividade;
    private ArrayList<String> notifications;
    public Cliente(String username, String password){
        this.username = username;
        this.password = password;
        leiloesatividade = new ArrayList<>();
        notifications = new ArrayList<>();
    }

    public ArrayList<String> getNotifications() {
        return notifications;
    }

    public void setNotifications(ArrayList<String> notifications) {
        this.notifications = notifications;
    }
    
    public ArrayList<Leilao> getLeiloesatividade() {
        return leiloesatividade;
    }

    public void setLeiloesatividade(ArrayList<Leilao> leiloesatividade) {
        this.leiloesatividade = leiloesatividade;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
}

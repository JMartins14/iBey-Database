/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverrmi;

import java.io.Serializable;

/**
 *
 * @author João
 */
public class Bid implements Serializable{
    private int amount;
    private Cliente user;
    public Bid(int amount, Cliente user){
        this.amount = amount;
        this.user  = user;
    }
    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Cliente getUser() {
        return user;
    }

    public void setUser(Cliente user) {
        this.user = user;
    }
    
}

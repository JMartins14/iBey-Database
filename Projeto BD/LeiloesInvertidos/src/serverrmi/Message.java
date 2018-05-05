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
public class Message implements Serializable{
    private String text;
    private Cliente cliente;

    public Message(String text, Cliente cliente) {
        this.text = text;
        this.cliente = cliente;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
        
}

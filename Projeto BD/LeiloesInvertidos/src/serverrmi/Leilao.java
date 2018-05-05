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
public class Leilao implements Serializable{   
    private Item artigo;
    private Data data;
    private ArrayList<Data> datasanteriores;
    private int amount;
    private int id;
    private ArrayList<Bid> listbids;
    private Bid minBid;
    private Cliente owner;
    private ArrayList<Message> mensagens;
    private String title,description;
    private ArrayList<String> titulosanteriores,descricoesanteriores;
    private boolean estado = true;

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
    
    public Leilao(int id,Item artigo,String title,String description, String deadline,String minutedeadline, int amount, Cliente owner) {
        this.artigo = artigo;
        this.id = id;
        this.amount = amount;
        this.data = new Data(deadline,minutedeadline);
        this.owner = owner;
        this.minBid = new Bid(amount,owner);
        this.listbids = new ArrayList<>();
        this.mensagens = new ArrayList<>();
        this.datasanteriores = new ArrayList<>();
        this.titulosanteriores = new ArrayList<>();
        this.descricoesanteriores = new ArrayList<>();
        this.title = title;
        this.description = description;
    }

    public ArrayList<Data> getDatasanteriores() {
        return datasanteriores;
    }

    public void setDatasanteriores(ArrayList<Data> datasanteriores) {
        this.datasanteriores = datasanteriores;
    }

    public ArrayList<String> getTitulosanteriores() {
        return titulosanteriores;
    }

    public void setTitulosanteriores(ArrayList<String> titulosanteriores) {
        this.titulosanteriores = titulosanteriores;
    }

    public ArrayList<String> getDescricoesanteriores() {
        return descricoesanteriores;
    }

    public void setDescricoesanteriores(ArrayList<String> descricoesanteriores) {
        this.descricoesanteriores = descricoesanteriores;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Bid> getListbids() {
        return listbids;
    }

    public void setListbids(ArrayList<Bid> listbids) {
        this.listbids = listbids;
    }

    public ArrayList<Message> getMensagens() {
        return mensagens;
    }

    public void setMensagens(ArrayList<Message> mensagens) {
        this.mensagens = mensagens;
    }
    
    public Bid getMinBid() {
        return minBid;
    }

    public void setMinBid(Bid minBid) {
        this.minBid = minBid;
    }

    public Cliente getOwner() {
        return owner;
    }

    public void setOwner(Cliente owner) {
        this.owner = owner;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Item getArtigo() {
        return artigo;
    }

    public void setArtigo(Item artigo) {
        this.artigo = artigo;
    }
    
}

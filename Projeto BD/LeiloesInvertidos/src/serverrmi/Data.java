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
public class Data implements Serializable{
    private int dia,mes,ano,horas,minutos;
    public Data(int dia, int mes, int ano, int horas, int minutos){
        this.dia = dia;
        this.mes = mes;
        this.ano = ano;
        this.horas = horas;
        this.minutos = minutos;
    }
    public Data(String parte1,String parte2){
        parte1=parte1.replaceAll("\\s","");
        try{
            this.ano = Integer.parseInt(parte1.substring(0,4));
            this.mes = Integer.parseInt(parte1.substring(5,7));
            this.dia = Integer.parseInt(parte1.substring(8,10));
            this.horas = Integer.parseInt(parte1.substring(10,12));
            this.minutos = Integer.parseInt(parte2.replaceAll("\\s",""));
        }
        catch(NumberFormatException e){
            e.printStackTrace();
        }
    }
    public int getAno(){
        return this.ano;
    }
    public int getMes(){
        return this.mes;
    }
    public int getDia(){
        return this.dia;
    }
    public int getHoras(){
        return this.horas;
    }   
    public void setHoras(int horas){
        this.horas = horas;
    }
    public int getMinutos(){
        return this.minutos;
    }
    public String getCodigoDia(){
        return String.format("%4d%2d%2d",ano,mes,dia);
    }
    public String getCodigoHoras(){
        return String.format("%2d%2d",horas,minutos);
    }
    public String getCodigoTotal(){
        return String.format("%4d%2d%2d%2d%2d",ano,mes,dia,horas,minutos);
    }
    public String toStringhoras(){
        String horario = new String();
        if(horas<10){
            horario = horario.concat("0" + horas);
        }
        else{
            horario = horario.concat(String.valueOf(horas));
        }
        if(minutos<10){
            horario = horario.concat(":0" + minutos);
        }
        else
           horario = horario.concat(":" + minutos);
        return horario;
    }
    public String toStringData(){
        String data = new String();
        data = String.format(String.valueOf(ano));
        if(mes<10)
            data = data.concat("-0"+mes);
        else
             data = data.concat("-"+mes);
        if(dia<10)
            data = data.concat("-0"+dia);
        else
            data = data.concat("-" + dia);

        return data;
    }
    public boolean segurancaData(boolean correcao){
        if(this.ano%4==0){
            if(this.mes==2){
                if(this.dia>29){
                    if(correcao==false)
                        return false;
                    else{
                        this.dia=1;
                        this.mes++;
                    }
                }
                return true;
            }       
        }
        if(this.mes==4 || this.mes==6 || this.mes==9 || this.mes==11){
            if(this.dia>30){
                if(correcao==false)
                    return false;
                else{
                    this.dia=1;
                    this.mes++;
                    if(this.mes>12){
                        this.mes=1;
                        this.ano++;
                    }
                }
            }
        }
        else{
            if(this.dia>31){
                if(correcao==false)
                    return false;
                else{
                    this.dia=1;
                    this.mes++;
                    if(this.mes>12){
                        this.mes=1;
                        this.ano++;
                    }
                }
            }
        }
        return true;
    }
}

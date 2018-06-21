package br.ufsm.csi.tcc2.model;

import java.util.Date;

public class Sensor {

    private String id;
    private String uriEndPoint;
    private double valor;
    private Date dataColeta;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUriEndPoint() {
        return uriEndPoint;
    }

    public void setUriEndPoint(String uriEndPoint) {
        this.uriEndPoint = uriEndPoint;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public Date getDataColeta() {
        return dataColeta;
    }

    public void setDataColeta(Date dataColeta) {
        this.dataColeta = dataColeta;
    }
}

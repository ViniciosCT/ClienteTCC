package br.ufsm.csi.tcc2.model;

import java.util.Collection;

public class Tomada {

    private String id;
    private String uriEndPoint;
    private boolean ligada;
    private Atuador atuador;
    private Collection<Sensor> sensores;

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

    public boolean isLigada() {
        return ligada;
    }

    public void setLigada(boolean ligada) {
        this.ligada = ligada;
    }

    public Atuador getAtuador() {
        return atuador;
    }

    public void setAtuador(Atuador atuador) {
        this.atuador = atuador;
    }

    public Collection<Sensor> getSensores() {
        return sensores;
    }

    public void setSensores(Collection<Sensor> sensores) {
        this.sensores = sensores;
    }
}

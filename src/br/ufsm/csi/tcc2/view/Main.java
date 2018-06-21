package br.ufsm.csi.tcc2.view;

import br.ufsm.csi.tcc2.model.Sensor;
import br.ufsm.csi.tcc2.model.Tomada;

import static br.ufsm.csi.tcc2.util.UtilTomada.*;

public class Main {

    public static void main(String ...args) throws InterruptedException {

        Tomada tomada = new Tomada();
        tomada.setUriEndPoint("http://localhost:3030/ontology-outlet-001");
        reconheceAtuador(tomada);
        reconheceSensores(tomada);
        System.out.println(" >>> Criou tomada " + tomada.getUriEndPoint() + " <<< ");

        // Ligar a tomada (chamar metodo no 1º sistema) guarda variavel de estado
        System.out.println(" >>> Ligando Tomada <<<" );
        setEstadoTomada(tomada);

        // A cada 10 segundos
        while (true){

            // Pesquisar dados dos sensores nos seus endpoints
            atualizarSensores(tomada);

            // Testar se estado é "Ligada"
            if(tomada.isLigada()){
                // Verificar se temperatura < 22°C { Desligar tomada }
                double temperatira = 0;
                for(Sensor sensor : tomada.getSensores()){
                    if(sensor.getId().equals("temperature")){
                        temperatira = sensor.getValor();
                    }
                }
                if( temperatira < 22 ){
                    setEstadoTomada(tomada);
                    System.out.println(" >>> Tomada Desligada! -- Temperatura " + temperatira + " <<< ");
                }
            }else{
                // Verificar se temperatura > 22°C { Ligar tomada }
                double temperatira = 0;
                for(Sensor sensor : tomada.getSensores()){
                    if(sensor.getId().equals("temperature")){
                        temperatira = sensor.getValor();
                    }
                }
                if(temperatira > 22){
                    setEstadoTomada(tomada);
                    System.out.println(" >>> Tomada Ligada! -- Temperatura " + temperatira + " <<< ");
                }
            }

            System.out.println(" >>> Esperando para atualizar dados... <<< ");
            Thread.sleep(10000);
        }
    }

}

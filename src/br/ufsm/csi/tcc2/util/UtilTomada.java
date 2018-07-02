package br.ufsm.csi.tcc2.util;

import br.ufsm.csi.tcc2.model.Atuador;
import br.ufsm.csi.tcc2.model.Sensor;
import br.ufsm.csi.tcc2.model.Tomada;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.spatial.pfunction.library.SouthPF;
import org.apache.jena.rdf.model.RDFNode;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class UtilTomada {

    public static boolean setEstadoTomada(Tomada tomada){
        try {
            URL url = new URL( tomada.getAtuador().getUriEndPoint() );
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            System.out.println(" >>> Estabelecendo conexão com " +  tomada.getAtuador().getUriEndPoint() + " <<<" );
            if (con.getResponseCode() != 200) {
                System.out.println(" >>> Erro ao tentar ligar ou desligar tomada <<< ");
                throw new RuntimeException("HTTP error code : "+ con.getResponseCode());
            }else {
                if(tomada.isLigada()){
                    tomada.setLigada(false);
                    System.out.println(" >>> Tomada desligada <<< ");
                }else{
                    tomada.setLigada(true);
                    System.out.println(" >>> Tomada ligada <<< ");
                }
                return true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (RuntimeException ex){
            ex.printStackTrace();
        }
        return false;
    }

    public static void reconheceAtuador(Tomada tomada){
        Atuador atuador = new Atuador();
        String sparql = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX iot-lite: <http://purl.oclc.org/NET/UNIS/fiware/iot-lite#>\n" +
                "\n" +
                "SELECT (str(?urlAtuador) AS ?urlAtuadorString)\n" +
                "WHERE {\n" +
                "  ?instancias rdf:type iot-lite:ActuatingDevice;\n" +
                "              iot-lite:exposedBy ?service.\n" +
                "  ?service iot-lite:endpoint ?urlAtuador.\n" +
                "}";
        String urlAtuador = consultaSPARQLAtuador(sparql, tomada.getUriEndPoint());
        atuador.setUriEndPoint( urlAtuador );
        tomada.setAtuador(atuador);
        System.out.println(" >>> Atuador reconhecido: " + tomada.getAtuador().getUriEndPoint() + " <<< ");
    }

    public static void reconheceSensores(Tomada tomada) {
        System.out.println(" >>> Reconhecendo sensores <<<");
        String sparql = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX iot-lite: <http://purl.oclc.org/NET/UNIS/fiware/iot-lite#>\n" +
                "PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>\n" +
                "\n" +
                "SELECT ?id (str(?urlEndPoint) AS ?urlEndPointString)\n" +
                "       WHERE {\n" +
                "  \t\t\t?instancias rdf:type ssn:SensingDevice;\n" +
                "                 iot-lite:id ?id;\n" +
                "                 iot-lite:exposedBy ?servicos.\n" +
                "  \t\t\t?servicos iot-lite:endpoint ?urlEndPoint.\n" +
                "       }";
        Collection<Sensor> sensoresRecuperados = consultaSPARQLSensores( sparql, tomada.getUriEndPoint());
        tomada.setSensores(sensoresRecuperados);
        System.out.println(" >>> Sensores recuperados: ");
        for(Sensor sensor : tomada.getSensores()){
            System.out.println("\t - ID: " + sensor.getId() + " URI: " + sensor.getUriEndPoint());
        }
        System.out.println(" <<< ");
    }

    public static void consultarSensores(Tomada tomada) {
        System.out.println(" >>> Atualizando sensores <<< ");
        for(Sensor sensor : tomada.getSensores()){
            String sparql = "prefix ontology: <http://www.loa.istc.cnr.it/ontologies/DUL.owl#>\n" +
                    "prefix time: <http://www.w3.org/2006/time#>\n" +
                    "\n" +
                    "SELECT ?valorObservado ?dataColeta\n" +
                    "WHERE {\n" +
                    "  ?recurso ontology:hasDataValue ?valorObservado;\n" +
                    "           time:inXSDDateTime ?dataColeta;\n" +
                    "}\n" +
                    "ORDER BY DESC(?dataColeta)\n" +
                    "LIMIT 1";
            Collection<String> valoresRetornados = consultaSPARQLDados( sparql, sensor.getUriEndPoint() );
            double valorRecuperado = Double.parseDouble( (String) valoresRetornados.toArray()[0] );
            Date dataRecuperada = parseDate( (String) valoresRetornados.toArray()[1] );

            System.out.println(" >>> Sensor: " + sensor.getId() + ": <<< \n" +
                    "\t -- Valor: " + valorRecuperado + " -- Data: " + dataRecuperada);

            sensor.setValor(valorRecuperado);
            sensor.setDataColeta(dataRecuperada);
        }
    }

    private static Date parseDate(String dataStringBruta){
        try {
            dataStringBruta = dataStringBruta.replace("Z", " ");
            String dataString = dataStringBruta.replace("T", "");
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            return new Date(((Date)formatter.parse(dataString)).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Collection<Sensor> consultaSPARQLSensores(String query, String serviceURI){

        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI, query);
        ResultSet results = q.execSelect();

        Collection<Sensor> resultados = new ArrayList<>();
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            // assumes that you have an "?s" and an "?o" in your query
            RDFNode id = soln.get("id");
            RDFNode uri = soln.get("urlEndPointString");
            Sensor sensor = new Sensor();
            sensor.setId( id.toString() );
            sensor.setUriEndPoint( uri.toString() );
            resultados.add(sensor);
        }
        return resultados;
    }

    private static Collection<String> consultaSPARQLDados(String query, String serviceURI){

        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI,
                query);
        ResultSet results = q.execSelect();

        Collection<String> resultados = new ArrayList<>();
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            // assumes that you have an "?s" and an "?o" in your query
            RDFNode valor = soln.get("valorObservado");
            RDFNode data = soln.get("dataColeta");
            resultados.add(valor.toString());
            resultados.add(data.toString());
        }
        return resultados;
    }

    private static String consultaSPARQLAtuador(String query, String serviceURI) {

        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI,
                query);
        ResultSet results = q.execSelect();

        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();

            // assumes that you have an "?s" and an "?o" in your query
            RDFNode urlAtuador = soln.get("urlAtuadorString");
            return urlAtuador.toString();
        }
        return null;
    }

}










//    public static void ligarTomada(Tomada tomada){
//        tomada.setLigada(true); //para testes
//
//        try {
//
//            URL url = new URL( tomada.getAtuador().getUriEndPoint() + "/ligar");
//            HttpURLConnection con = (HttpURLConnection) url.openConnection();
//
//            if (con.getResponseCode() != 200) {
//                throw new RuntimeException("HTTP error code : "+ con.getResponseCode());
//            }else {
//                tomada.setLigada(true);
//            }
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (RuntimeException ex){
//            ex.printStackTrace();
//        }
//    }
//
//    public static void desligaTomada(Tomada tomada){
//        tomada.setLigada(false);//para testes
//
//        try {
//
//            URL url = new URL( tomada.getAtuador().getUriEndPoint() + "/desligar");
//            HttpURLConnection con = (HttpURLConnection) url.openConnection();
//
//            if (con.getResponseCode() != 200) {
//                throw new RuntimeException("HTTP error code : "+ con.getResponseCode());
//            }else {
//                tomada.setLigada(false);
//            }
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }catch (RuntimeException ex){
//            ex.printStackTrace();
//        }
//    }
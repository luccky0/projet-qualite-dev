import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.json.JSONObject;
package examples;

import java.io.*;
import java.net.*;

import bri.Service;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

public class ServiceAnalyseDeFichier implements Service {
	
	private final Socket client;

    private static final List<String> typeAnalyse = List.of("résumé", "Détecter des erreurs", "transformationsJSON", "tout");

    public ServiceAnalyseDeFichier(Socket client) {
        this.client = client;
    }

    @Override
	public void run() {
		try {
            BufferedReader in = new BufferedReader (new InputStreamReader(client.getInputStream ( )));
            PrintWriter out = new PrintWriter (client.getOutputStream ( ), true);
            out.println("=== SERVICE D'ANALYSE DE FICHIER ===##Envoyez le fichiere à analyser :");
            Document doc = lireLefichier(in ,out);
            analyserFichierXML(doc, out);
        }
        catch (IOException e) {
        }
	}
	
	protected void finalize() throws Throwable {
		 client.close(); 
	}

    public static Document lireLefichier (BufferedReader in , PrintWriter out){
       StringBuilder xmlContent = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            xmlContent.append(line).append("\n");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(
                new ByteArrayInputStream(xml.getBytes())
            );
            out.println("✅ Le XML est valide et bien formé !");
            return doc;
        } catch (Exception e) {
            out.println("❌ Le contenu n'est PAS un XML valide.");
        }
    }
    
    public static analyserFichierXML (Document doc, PrintWriter out){
        out.println("quel type d'analyse voulez-vous ?");
        for (String type : typeAnalyse) {
            out.println("Analyse de type : " + type);
        }
        switch (type) {
            case "résumé":
                resumeFichier(doc, out);
                break;
            case "Détecter des erreurs":
               detecterErreurs(doc, out);
                break;
            case "transformationsJSON":
                transformerEnJSON(doc, out);
                break;
            case "tout":
                
                break;
            default:
                out.println("Type d'analyse inconnu.");
                break;
        }
    }

    public static void resumeFichier(Document doc, PrintWriter out) {
        try {
            Element racine = doc.getDocumentElement();
            out.println("La racine du fichier est `" + racine.getTagName() + "`.");

            NodeList enfants = racine.getChildNodes();
            int nbElements = 0;
            StringBuilder nomsBalises = new StringBuilder();

            for (int i = 0; i < enfants.getLength(); i++) {
                Node n = enfants.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    nbElements++;
                    nomsBalises.append("`").append(n.getNodeName()).append("`, ");
                }
            }

            if (nbElements > 0) {
                nomsBalises.setLength(nomsBalises.length() - 2); // retirer la dernière virgule
                out.println("Elle contient " + nbElements + " balises enfants : " + nomsBalises + ".");
            } else {
                out.println("Elle ne contient pas de balises enfants.");
            }

            // Détail de chaque balise
            for (int i = 0; i < enfants.getLength(); i++) {
                Node n = enfants.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) n;
                    String valeur = e.getTextContent().trim();
                    out.print("La balise `" + e.getTagName() + "`");
                    if (!valeur.isEmpty()) {
                        out.println(" a pour valeur `" + valeur + "`.");
                    } else {
                        out.println(" est vide.");
                    }
                }
            }
            
        } catch (Exception e) {
            out.println("❌ Impossible de générer le résumé : erreur interne.");
        }
    }
        // Détecte des erreurs simples dans le document XML (exemple : balises vides)
    public static void detecterErreurs(Document doc, PrintWriter out) {
        try {
            Element racine = doc.getDocumentElement();
            NodeList enfants = racine.getChildNodes();
            boolean erreur = false;
            for (int i = 0; i < enfants.getLength(); i++) {
                Node n = enfants.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) n;
                    String valeur = e.getTextContent().trim();
                    if (valeur.isEmpty()) {
                        out.println("Erreur : la balise '" + e.getTagName() + "' est vide.");
                        erreur = true;
                    }
                }
            }
            if (!erreur) {
                out.println("Aucune erreur détectée : toutes les balises enfants ont une valeur.");
            }
        } catch (Exception e) {
            out.println("❌ Erreur lors de la détection d'erreurs : " + e.getMessage());
        }
    }

    // Transforme le document XML en JSON (simple, racine et enfants directs)
    public static void transformerEnJSON(Document doc, PrintWriter out) {
        try {
            Element racine = doc.getDocumentElement();
            JSONObject json = new JSONObject();
            NodeList enfants = racine.getChildNodes();
            for (int i = 0; i < enfants.getLength(); i++) {
                Node n = enfants.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) n;
                    json.put(e.getTagName(), e.getTextContent().trim());
                }
            }
            out.println("JSON généré : " + json.toString(2));
        } catch (Exception e) {
            out.println("❌ Erreur lors de la transformation en JSON : " + e.getMessage());
        }
    }


}

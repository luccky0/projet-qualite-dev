package examples;

import java.io.*;
import java.net.*;
import java.util.List;

import bri.Service;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;
import org.json.JSONObject;

public class ServiceAnalyseDeFichier implements Service {

    private final Socket client;

    private static final List<String> typeAnalyse = List.of("résumé", "Détecter des erreurs", "transformationsJSON", "tout");

    public ServiceAnalyseDeFichier(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            out.println("=== SERVICE D'ANALYSE DE FICHIER ===");
            out.println("Envoyez le fichier XML à analyser (terminez par une ligne vide) :");

            Document doc = lireLeFichier(in, out);
            if (doc != null) {
                analyserFichierXML(doc, out, in);
            } else {
                out.println("Le contenu reçu n'est pas un XML valide.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        client.close();
    }

    public static Document lireLeFichier(BufferedReader in, PrintWriter out) {
        StringBuilder xmlContent = new StringBuilder();
        String line;

        try {
            // Lecture du contenu XML envoyé par le client
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                xmlContent.append(line).append("\n");
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(
                    new ByteArrayInputStream(xmlContent.toString().getBytes())
            );

            out.println("Le fichier XML est bien formé et valide !");
            return doc;

        } catch (Exception e) {
            out.println("Erreur lors de la lecture du fichier : " + e.getMessage());
            return null;
        }
    }

    public static void analyserFichierXML(Document doc, PrintWriter out, BufferedReader in) throws IOException {
        out.println("Quel type d'analyse voulez-vous effectuer ?");
        for (String type : typeAnalyse) {
            out.println("- " + type);
        }

        String type = in.readLine();

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
                resumeFichier(doc, out);
                detecterErreurs(doc, out);
                transformerEnJSON(doc, out);
                break;
            default:
                out.println("Type d'analyse inconnu.");
                break;
        }
    }

    public static void resumeFichier(Document doc, PrintWriter out) {
        try {
            Element racine = doc.getDocumentElement();
            out.println("La racine du fichier est '" + racine.getTagName() + "'.");

            NodeList enfants = racine.getChildNodes();
            int nbElements = 0;
            StringBuilder nomsBalises = new StringBuilder();

            for (int i = 0; i < enfants.getLength(); i++) {
                Node n = enfants.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    nbElements++;
                    nomsBalises.append(n.getNodeName()).append(", ");
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
                    out.print("La balise '" + e.getTagName() + "'");
                    if (!valeur.isEmpty()) {
                        out.println(" a pour valeur '" + valeur + "'.");
                    } else {
                        out.println(" est vide.");
                    }
                }
            }

        } catch (Exception e) {
            out.println("Impossible de générer le résumé : erreur interne.");
        }
    }

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
            out.println("Erreur lors de la détection d'erreurs : " + e.getMessage());
        }
    }

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

            out.println("JSON généré :");
            out.println(json.toString(2));

        } catch (Exception e) {
            out.println("Erreur lors de la transformation en JSON : " + e.getMessage());
        }
    }
}

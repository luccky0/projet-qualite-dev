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
            out.println("Souhaitez-vous :");
            out.println("1) Envoyer le contenu XML manuellement");
            out.println("2) Fournir un lien FTP vers un fichier XML");
            String choix = in.readLine();

            Document doc = null;

            if ("2".equals(choix)) {
                out.println("Entrez le lien FTP complet (ex: ftp://user:pass@localhost/fichiers/test.xml) :");
                String ftpUrl = in.readLine();
                doc = lireFichierDepuisFTP(ftpUrl, out);
            } else {
                out.println("Envoyez le fichier XML à analyser (terminez par une ligne vide) :");
                doc = lireLeFichier(in, out);
            }

            if (doc != null) {
                analyserFichierXML(doc, out, in);
            } else {
                out.println("Le fichier XML n'a pas pu être lu ou n'est pas valide.");
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

    public static Document lireFichierDepuisFTP(String ftpUrl, PrintWriter out) {
        try {
            URL url = new URL(ftpUrl);
            URLConnection connection = url.openConnection();

            try (InputStream input = connection.getInputStream()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(input);
                out.println("Fichier XML récupéré avec succès depuis le serveur FTP !");
                return doc;
            }

        } catch (Exception e) {
            out.println("Erreur lors de la récupération du fichier FTP : " + e.getMessage());
            return null;
        }
    }

    public static void analyserFichierXML(Document doc, PrintWriter out, BufferedReader in) throws IOException {
        out.println("Quel type d'analyse voulez-vous effectuer ?");
        for (String type : typeAnalyse) {
            out.println("- " + type);
        }

        String type = in.readLine();
        String resultat;

        switch (type) {
            case "résumé":
                resultat = resumeFichier(doc);
                break;
            case "Détecter des erreurs":
                resultat = detecterErreurs(doc);
                break;
            case "transformationsJSON":
                resultat = transformerEnJSON(doc);
                break;
            case "tout":
                StringBuilder sb = new StringBuilder();
                sb.append(resumeFichier(doc)).append("\n\n");
                sb.append(detecterErreurs(doc)).append("\n\n");
                sb.append(transformerEnJSON(doc));
                resultat = sb.toString();
                break;
            default:
                resultat = "Type d'analyse inconnu.";
                break;
        }

        sendEmail(resultat, out, in);
    }

    private static void sendEmail(String resultat, PrintWriter out, BufferedReader in) throws IOException {
        out.println("Veuillez renseigner votre email pour recevoir le résultat de l'analyse :");
        String email = in.readLine();
        out.println("Le résultat sera envoyé à " + email + " (simulation).");
        out.println("----- DÉBUT DU RAPPORT -----");
        out.println(resultat);
        out.println("----- FIN DU RAPPORT -----");
    }

    public static String resumeFichier(Document doc) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);

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
                nomsBalises.setLength(nomsBalises.length() - 2);
                out.println("Elle contient " + nbElements + " balises enfants : " + nomsBalises + ".");
            } else {
                out.println("Elle ne contient pas de balises enfants.");
            }

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
            out.println("Erreur lors du résumé : " + e.getMessage());
        }

        out.flush();
        return sw.toString();
    }

    public static String detecterErreurs(Document doc) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);

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
                out.println("Aucune erreur détectée.");
            }

        } catch (Exception e) {
            out.println("Erreur lors de la détection : " + e.getMessage());
        }

        out.flush();
        return sw.toString();
    }

    public static String transformerEnJSON(Document doc) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);

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
            out.println("Erreur JSON : " + e.getMessage());
        }

        out.flush();
        return sw.toString();
    }
}

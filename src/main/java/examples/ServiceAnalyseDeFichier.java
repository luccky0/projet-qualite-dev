package examples;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import bri.Service;
import javax.naming.directory.*;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
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

            out.println("=== SERVICE D'ANALYSE DE FICHIER ===##" +
                    "1) Envoyer le contenu XML manuellement##" +
                    "2) Fournir un lien FTP vers un fichier XML##" +
                    "Votre choix :"
            );
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

            xmlContent.append(in.readLine());

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
    
    public static String getMX(String email) throws NamingException {
        String domain = email.substring(email.indexOf('@') + 1);

        javax.naming.directory.DirContext ctx = new javax.naming.directory.InitialDirContext();
        javax.naming.directory.Attributes attrs = ctx.getAttributes(domain, new String[]{"MX"});
        javax.naming.directory.Attribute attr = attrs.get("MX");

        if (attr == null) return domain; 

        String bestMX = null;
        int bestPriority = Integer.MAX_VALUE;
        javax.naming.NamingEnumeration<?> en = attr.getAll();
        while (en.hasMore()) {
            String record = (String) en.next();
            String[] parts = record.split("\\s+");
            if (parts.length >= 2) {
                int priority = Integer.parseInt(parts[0]);
                String host = parts[1].endsWith(".") ? parts[1].substring(0, parts[1].length()-1) : parts[1];
                if (priority < bestPriority) {
                    bestPriority = priority;
                    bestMX = host;
                }
            }
        }
        return bestMX;
    }

    
    public static void sendEmail(String resultat, PrintWriter out, BufferedReader in) {
        try {
            out.println("Veuillez renseigner votre email pour recevoir le résultat de l'analyse :");
            String email = in.readLine();

            String mxHost = getMX(email); 

            Properties props = new Properties();
            props.put("mail.smtp.host", mxHost);
            props.put("mail.smtp.port", "25");

            Session session = Session.getInstance(props, null);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("ServiceAnnalyse@defichierXML.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Résultat de l'analyse de votre fichier XML");
            message.setText(resultat);

            Transport.send(message);

            out.println("Email envoyé avec succès à " + email);

        } catch (Exception e) {
            out.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
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
    public static String toStringue() {
        return "Analyse d'un fichier XML";
    }
}

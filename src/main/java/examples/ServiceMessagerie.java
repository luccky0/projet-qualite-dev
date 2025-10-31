package examples;

import bri.Service;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class ServiceMessagerie implements Service {

    private final Socket client;
    private static final Map<String, List<String>> messages = new HashMap<>();
    private String response = "";

    public ServiceMessagerie(Socket socket) {
        this.client = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            while (true) {
                response += "##";
                String menu = response +
                        "=== SERVICE DE MESSAGERIE INTERNE ===##" +
                        "1 - Envoyer un message##" +
                        "2 - Lire mes messages##" +
                        "0 - Quitter##" +
                        "Votre choix :";

                response = "";

                out.println(menu);
                String choix = in.readLine();

                if (choix == null || choix.equals("0")) {
                    break;
                }

                switch (choix.trim()) {
                    case "1":
                        envoyerMessage(in, out);
                        break;
                    case "2":
                        lireMessages(in, out);
                        break;
                    default:
                        response = "Choix invalide##";
                }
            }

            response = "Fin du service messagerie##";
            out.println(response);

        } catch (IOException e) {
            System.err.println("Erreur dans ServiceMessagerie : " + e.getMessage());
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    private void envoyerMessage(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Votre pseudo (expéditeur) :");
        String expediteur = in.readLine();

        out.println("Pseudo du destinataire :");
        String destinataire = in.readLine();

        out.println("Message :");
        String contenu = in.readLine();

        if (expediteur == null || destinataire == null || contenu == null ||
                expediteur.isBlank() || destinataire.isBlank() || contenu.isBlank()) {
            response = "Erreur : champs vides##";
            return;
        }

        synchronized (messages) {
            messages.computeIfAbsent(destinataire, k -> new ArrayList<>())
                    .add("De " + expediteur + " : " + contenu);
        }

        response = "Message envoyé à " + destinataire + "##";
    }

    private void lireMessages(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Entrez votre pseudo :");
        String pseudo = in.readLine();

        if (pseudo == null || pseudo.isBlank()) {
            response = "Pseudo invalide##";
            return;
        }

        List<String> recu;
        synchronized (messages) {
            recu = messages.remove(pseudo);
        }

        if (recu == null || recu.isEmpty()) {
            response = "Aucun message pour " + pseudo + ".##";
        } else {
            StringBuilder sb = new StringBuilder("=== Vos messages ===##");
            for (String msg : recu) {
                sb.append(msg).append("##");
            }
            response = sb.toString();
        }
    }

    public static String toStringue() {
        return "Messagerie interne";
    }

    public void start() {
        new Thread(this).start();
    }
}
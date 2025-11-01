package examples;

import bri.Service;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class ServiceMessagerie implements Service {

    private final Socket client;
    private static final Map<String, List<Message>> messages = new HashMap<>();
    private static final Map<String, String> comptes = new HashMap<>(); // login -> mdp
    private String response = "";

    static {
        comptes.put("Nathan", "1111");
        comptes.put("Luc", "2222");
        comptes.put("Mathias", "3333");
    }

    public ServiceMessagerie(Socket socket) {
        this.client = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            // === Authentification interne ===
            out.println("=== SERVICE DE MESSAGERIE INTERNE ===##Login :");
            String login = in.readLine();

            out.println("Mot de passe :");
            String mdp = in.readLine();

            if (!authentifier(login, mdp)) {
                out.println("Authentification échouée##");
                client.close();
                return;
            }

            response = "Connexion réussie ! Bienvenue " + login + "##";

            // === Menu principal ===
            while (true) {
                response += "##";
                String menu = response +
                        "=== MENU MESSAGERIE ===##" +
                        "1 - Envoyer un message##" +
                        "2 - Lire une conversation##" +
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
                        envoyerMessage(in, out, login);
                        break;
                    case "2":
                        lireConversation(in, out, login);
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

    private boolean authentifier(String login, String mdp) {
        return comptes.containsKey(login) && comptes.get(login).equals(mdp);
    }

    private void envoyerMessage(BufferedReader in, PrintWriter out, String expediteur) throws IOException {
        out.println("Pseudo du destinataire :");
        String destinataire = in.readLine();

        out.println("Message :");
        String contenu = in.readLine();

        if (destinataire == null || contenu == null || destinataire.isBlank() || contenu.isBlank()) {
            response = "Erreur : champs vides##";
            return;
        }

        Message msg = new Message(expediteur, destinataire, contenu, new Date());

        synchronized (messages) {
            messages.computeIfAbsent(expediteur, k -> new ArrayList<>()).add(msg);
            messages.computeIfAbsent(destinataire, k -> new ArrayList<>()).add(msg);
        }

        response = "Message envoyé à " + destinataire + "##";
    }

    private void lireConversation(BufferedReader in, PrintWriter out, String login) throws IOException {
        out.println("Avec qui voulez-vous voir la conversation ?");
        String autre = in.readLine();

        List<Message> msgs;
        synchronized (messages) {
            msgs = messages.getOrDefault(login, new ArrayList<>());
        }

        if (msgs.isEmpty()) {
            response = "Aucun message trouvé.##";
            return;
        }

        StringBuilder sb = new StringBuilder("=== Conversation avec " + autre + " ===##");

        boolean found = false;
        for (Message msg : msgs) {
            if ((msg.expediteur.equals(login) && msg.destinataire.equals(autre)) ||
                    (msg.expediteur.equals(autre) && msg.destinataire.equals(login))) {
                sb.append(msg).append("##");
                found = true;
            }
        }

        if (!found) {
            response = "Aucune conversation trouvée avec " + autre + "##";
        } else {
            response = sb.toString();
        }
    }

    public static String toStringue() {
        return "Messagerie interne";
    }

    public void start() {
        new Thread(this).start();
    }

    private static class Message {
        String expediteur;
        String destinataire;
        String contenu;
        Date date;

        Message(String e, String d, String c, Date date) {
            this.expediteur = e;
            this.destinataire = d;
            this.contenu = c;
            this.date = date;
        }

        @Override
        public String toString() {
            return "[" + date + "] " + expediteur + " → " + destinataire + " : " + contenu;
        }
    }
}

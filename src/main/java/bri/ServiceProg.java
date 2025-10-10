package bri;

import java.io.*;
import java.net.*;
import java.net.URLClassLoader;

class ServiceProg implements Runnable {
    
    private Socket client;
    
    ServiceProg(Socket socket) {
        client = socket;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            
            // Authentification
            out.println("=== CONNEXION PROGRAMMEUR BRi ===##Login :");
            String login = in.readLine();
            out.println("Mot de passe :");
            String motDePasse = in.readLine();
            
            Programmeur prog = Programmeur.authentifier(login, motDePasse);
            if (prog == null) {
                out.println("Authentification échouée");
                client.close();
                return;
            }
            
            out.println("Bienvenue " + login + " !##");
            
            // Menu principal
            while (true) {
                String menu = "=== MENU PROGRAMMEUR ===##" +
                             "1 - Fournir un nouveau service##" +
                             "2 - Mettre à jour un service##" +
                             "3 - Changer l'adresse FTP##" +
                             "4 - Démarrer/Arrêter un service##" +
                             "5 - Désinstaller un service##" +
                             "6 - Voir mes services##" +
                             "0 - Quitter##" +
                             "Votre choix :";
                
                out.println(menu);
                String choix = in.readLine();
                
                if (choix == null || choix.equals("0")) {
                    break;
                }
                
                switch (choix) {
                    case "1":
                        ajouterService(in, out, prog);
                        break;
                    case "2":
                        mettreAJourService(in, out, prog);
                        break;
                    case "3":
                        changerFTP(in, out, prog);
                        break;
                    case "4":
                        toggleService(in, out, prog);
                        break;
                    case "5":
                        supprimerService(in, out, prog);
                        break;
                    case "6":
                        voirServices(out, prog);
                        break;
                    default:
                        out.println("Choix invalide##");
                }
            }
            
            out.println("Au revoir !##");
            client.close();
            
        } catch (Exception e) {
            System.err.println("Erreur dans ServiceProg: " + e.getMessage());
            try { client.close(); } catch (IOException e2) {}
        }
    }
    
    private void ajouterService(BufferedReader in, PrintWriter out, Programmeur prog) throws Exception {
        out.println("Nom de la classe du service (ex: " + prog.getLogin() + ".MonService) :");
        String className = in.readLine();
        
        try {
            URL ftpUrl = new URL(prog.getFtpUrl());
            URLClassLoader urlcl = new URLClassLoader(new URL[]{ftpUrl});
            Class<?> serviceClass = urlcl.loadClass(className);
            
            ServiceRegistry.addService(serviceClass, prog.getLogin());
            out.println("Service ajouté avec succès !##");
        } catch (Exception e) {
            out.println(e);
        }
    }
    
    private void mettreAJourService(BufferedReader in, PrintWriter out, Programmeur prog) throws Exception {
        out.println("Nom du service à mettre à jour :");
        String serviceName = in.readLine();
        out.println("Nouveau nom de classe :");
        String newClassName = in.readLine();
        
        try {
            URL ftpUrl = new URL(prog.getFtpUrl());
            URLClassLoader urlcl = new URLClassLoader(new URL[]{ftpUrl});
            Class<?> newServiceClass = urlcl.loadClass(newClassName);
            
            // Trouver l'ancienne classe
            // Cette implémentation est simplifiée
            out.println("Mise à jour effectuée (implémentation simplifiée)##");
        } catch (Exception e) {
            out.println(e);
        }
    }
    
    private void changerFTP(BufferedReader in, PrintWriter out, Programmeur prog) throws Exception {
        out.println("Nouvelle adresse FTP :");
        String newFtpUrl = in.readLine();
        prog.setFtpUrl(newFtpUrl);
        out.println("Adresse FTP mise à jour##");
    }
    
    private void toggleService(BufferedReader in, PrintWriter out, Programmeur prog) throws Exception {
        out.println("Nom du service à démarrer/arrêter :");
        String serviceName = in.readLine();
        boolean success = ServiceRegistry.toggleService(serviceName, prog.getLogin());
        if (success) {
            out.println("État du service modifié##");
        } else {
            out.println("Service non trouvé##");
        }
    }
    
    private void supprimerService(BufferedReader in, PrintWriter out, Programmeur prog) throws Exception {
        out.println("Nom du service à supprimer :");
        String serviceName = in.readLine();
        boolean success = ServiceRegistry.removeService(serviceName, prog.getLogin());
        if (success) {
            out.println("Service supprimé##");
        } else {
            out.println("Service non trouvé##");
        }
    }
    
    private void voirServices(PrintWriter out, Programmeur prog) {
        out.println(ServiceRegistry.toStringueForProgrammer(prog.getLogin()));
    }

    public void start() {
        (new Thread(this)).start();		
    }
}
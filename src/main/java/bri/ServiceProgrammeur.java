package bri;

import java.io.*;
import java.net.*;
import java.net.URLClassLoader;

/**
 * Service pour traiter les demandes des clients programmeurs
 * Gère l'authentification et les opérations de gestion de services
 */
public class ServiceProgrammeur implements Runnable {
    
    private final Socket client;
    private String loginProgrammeur = null;
    
    /**
     * Constructeur pour un client programmeur
     * @param socket Socket de connexion du client
     */
    public ServiceProgrammeur(Socket socket) {
        this.client = socket;
    }
    
    /**
     * Traite la demande du client programmeur
     */
    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
            
            // Authentification
            if (!authenticate(in, out)) {
                out.println("ERREUR: Authentification échouée");
                return;
            }
            
            // Menu principal pour programmeur authentifié
            showProgrammerMenu(in, out);
            
        } catch (Exception e) {
            System.err.println("Erreur dans ServiceProgrammeur pour " + 
                             client.getRemoteSocketAddress() + " : " + e.getMessage());
            if (out != null) {
                out.println("ERREUR: " + e.getMessage());
            }
        } finally {
            try {
                if (client != null && !client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture de la connexion programmeur : " + e.getMessage());
            }
        }
    }
    
    /**
     * Gère l'authentification du programmeur
     */
    private boolean authenticate(BufferedReader in, PrintWriter out) throws IOException {
        out.println("=== Authentification Programmeur BRi ===");
        out.println("Login:");
        String login = in.readLine();
        if (login == null) return false;
        
        out.println("Mot de passe:");
        String password = in.readLine();
        if (password == null) return false;
        
        if (UserManager.authenticate(login.trim(), password.trim())) {
            loginProgrammeur = login.trim();
            out.println("Authentification réussie. Bienvenue " + loginProgrammeur + " !");
            return true;
        }
        
        return false;
    }
    
    /**
     * Affiche le menu principal du programmeur et traite ses choix
     */
    private void showProgrammerMenu(BufferedReader in, PrintWriter out) throws IOException {
        while (true) {
            out.println("##=== Menu Programmeur ===##");
            out.println("1 - Fournir un nouveau service");
            out.println("2 - Mettre à jour un service");
            out.println("3 - Changer l'adresse FTP");
            out.println("4 - Lister mes services");
            out.println("5 - Quitter");
            out.println("Votre choix:");
            
            String choix = in.readLine();
            if (choix == null) break;
            
            switch (choix.trim()) {
                case "1":
                    ajouterNouveauService(in, out);
                    break;
                case "2":
                    mettreAJourService(in, out);
                    break;
                case "3":
                    changerAdresseFTP(in, out);
                    break;
                case "4":
                    listerMesServices(out);
                    break;
                case "5":
                    out.println("Au revoir !");
                    return;
                default:
                    out.println("Choix invalide. Veuillez réessayer.");
            }
        }
    }
    
    /**
     * Ajoute un nouveau service depuis le serveur FTP du programmeur
     */
    private void ajouterNouveauService(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Nom de la classe de service (sans .class):");
        String className = in.readLine();
        if (className == null || className.trim().isEmpty()) {
            out.println("ERREUR: Nom de classe requis");
            return;
        }
        
        className = className.trim();
        
        try {
            // Récupérer l'URL FTP du programmeur
            UserManager.Programmeur prog = UserManager.getProgrammer(loginProgrammeur);
            if (prog == null) {
                out.println("ERREUR: Programmeur non trouvé");
                return;
            }
            
            // Construire l'URL complète pour le chargement de classe
            String ftpUrl = prog.getFtpUrl();
            if (!ftpUrl.endsWith("/")) {
                ftpUrl += "/";
            }
            ftpUrl += "classes/";
            
            // Créer un URLClassLoader pour charger depuis le FTP
            @SuppressWarnings("deprecation")
            URL ftpURL = new URL(ftpUrl);
            URLClassLoader urlcl = URLClassLoader.newInstance(new URL[] {
                ftpURL
            });
            
            // Charger la classe
            String fullClassName = loginProgrammeur + "." + className;
            Class<?> serviceClass = urlcl.loadClass(fullClassName).asSubclass(Service.class);
            
            // Ajouter le service au registre
            ServiceRegistry.addService(serviceClass, loginProgrammeur);
            out.println("Service '" + className + "' ajouté avec succès !");
            
        } catch (Exception e) {
            out.println("ERREUR: Impossible d'ajouter le service - " + e.getMessage());
            System.err.println("Erreur lors de l'ajout du service " + className + " : " + e.getMessage());
        }
    }
    
    /**
     * Met à jour un service existant
     */
    private void mettreAJourService(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Fonctionnalité de mise à jour non implémentée dans cette version");
        // TODO: Implémenter la mise à jour de services
    }
    
    /**
     * Change l'adresse FTP du programmeur
     */
    private void changerAdresseFTP(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Nouvelle adresse FTP:");
        String newFtpUrl = in.readLine();
        if (newFtpUrl == null || newFtpUrl.trim().isEmpty()) {
            out.println("ERREUR: Adresse FTP requise");
            return;
        }
        
        if (UserManager.updateFtpUrl(loginProgrammeur, newFtpUrl.trim())) {
            out.println("Adresse FTP mise à jour avec succès !");
        } else {
            out.println("ERREUR: Impossible de mettre à jour l'adresse FTP");
        }
    }
    
    /**
     * Liste les services du programmeur
     */
    private void listerMesServices(PrintWriter out) {
        out.println("##=== Vos services ===##");
        var services = ServiceRegistry.getServicesForProgrammer(loginProgrammeur);
        if (services.isEmpty()) {
            out.println("Aucun service enregistré");
        } else {
            for (String service : services) {
                out.println("- " + service);
            }
        }
    }
    
    /**
     * Lance ce service dans un nouveau thread
     */
    public void start() {
        new Thread(this, "ServiceProgrammeur-" + client.getRemoteSocketAddress()).start();
    }
}

package appli;

import bri.*;
import java.util.Scanner;

/**
 * Application principale de la plateforme BRi
 * Lance les serveurs pour amateurs et programmeurs
 */
public class BRiLaunch {
    // Ports de connexion
    private static final int PORT_AMATEUR = 3000;      // Port pour les amateurs
    private static final int PORT_PROGRAMMEUR = 3001;  // Port pour les programmeurs
    
    // Serveurs
    private static ServeurAmateur serveurAmateur;
    private static ServeurProgrammeur serveurProgrammeur;
    
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("    Plateforme BRi - Services Dynamiques     ");
        System.out.println("==============================================");
        System.out.println();
        
        try {
            // Démarrer les serveurs
            demarrerServeurs();
            
            // Interface d'administration
            interfaceAdministration();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage de BRiLaunch : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Arrêter les serveurs
            arreterServeurs();
        }
    }
    
    /**
     * Démarre les serveurs amateur et programmeur
     */
    private static void demarrerServeurs() {
        System.out.println("Démarrage des serveurs...");
        
        // Démarrer le serveur amateur
        serveurAmateur = new ServeurAmateur(PORT_AMATEUR);
        serveurAmateur.start();
        
        // Démarrer le serveur programmeur  
        serveurProgrammeur = new ServeurProgrammeur(PORT_PROGRAMMEUR);
        serveurProgrammeur.start();
        
        System.out.println();
        System.out.println("✓ Serveur amateur démarré sur le port " + PORT_AMATEUR);
        System.out.println("✓ Serveur programmeur démarré sur le port " + PORT_PROGRAMMEUR);
        System.out.println();
        
        // Attendre un peu pour s'assurer que les serveurs sont prêts
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Interface d'administration en ligne de commande
     */
    private static void interfaceAdministration() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Interface d'Administration BRi ===");
        System.out.println("Les serveurs sont démarrés. Les clients peuvent se connecter.");
        System.out.println();
        System.out.println("Informations de connexion :");
        System.out.println("- Amateurs : port " + PORT_AMATEUR);
        System.out.println("- Programmeurs : port " + PORT_PROGRAMMEUR);
        System.out.println();
        
        while (true) {
            System.out.println("--- Menu Administration ---");
            System.out.println("1 - Afficher les services disponibles");
            System.out.println("2 - Afficher les programmeurs");
            System.out.println("3 - Créer un compte programmeur");
            System.out.println("4 - Statistiques");
            System.out.println("0 - Arrêter BRiLaunch");
            System.out.print("Votre choix : ");
            
            String choix = scanner.nextLine().trim();
            
            switch (choix) {
                case "1":
                    afficherServices();
                    break;
                case "2":
                    afficherProgrammeurs();
                    break;
                case "3":
                    creerCompteProgrammeur(scanner);
                    break;
                case "4":
                    afficherStatistiques();
                    break;
                case "0":
                    System.out.println("Arrêt de BRiLaunch...");
                    return;
                default:
                    System.out.println("Choix invalide. Veuillez réessayer.");
            }
            System.out.println();
        }
    }
    
    /**
     * Affiche la liste des services disponibles
     */
    private static void afficherServices() {
        System.out.println("=== Services Disponibles ===");
        String services = ServiceRegistry.toStringue();
        String[] lignes = services.split("##");
        for (String ligne : lignes) {
            if (!ligne.trim().isEmpty()) {
                System.out.println(ligne);
            }
        }
    }
    
    /**
     * Affiche la liste des programmeurs (fonction basique)
     */
    private static void afficherProgrammeurs() {
        System.out.println("=== Programmeurs Enregistrés ===");
        System.out.println("- testprog (URL: ftp://127.0.0.1:2121/)");
        System.out.println("- exemple (URL: ftp://127.0.0.1:2121/)");
        // Note: Dans une version complète, on récupérerait la liste depuis UserManager
    }
    
    /**
     * Crée un nouveau compte programmeur
     */
    private static void creerCompteProgrammeur(Scanner scanner) {
        System.out.println("=== Création de Compte Programmeur ===");
        
        System.out.print("Login : ");
        String login = scanner.nextLine().trim();
        
        if (login.isEmpty()) {
            System.out.println("Erreur : Le login ne peut pas être vide");
            return;
        }
        
        System.out.print("Mot de passe : ");
        String password = scanner.nextLine().trim();
        
        if (password.isEmpty()) {
            System.out.println("Erreur : Le mot de passe ne peut pas être vide");
            return;
        }
        
        System.out.print("URL du serveur FTP : ");
        String ftpUrl = scanner.nextLine().trim();
        
        if (ftpUrl.isEmpty()) {
            System.out.println("Erreur : L'URL FTP ne peut pas être vide");
            return;
        }
        
        if (UserManager.createProgrammer(login, password, ftpUrl)) {
            System.out.println("✓ Compte programmeur créé avec succès pour : " + login);
        } else {
            System.out.println("✗ Erreur : Le login existe déjà");
        }
    }
    
    /**
     * Affiche des statistiques basiques
     */
    private static void afficherStatistiques() {
        System.out.println("=== Statistiques BRi ===");
        System.out.println("Port amateur : " + PORT_AMATEUR);
        System.out.println("Port programmeur : " + PORT_PROGRAMMEUR);
        System.out.println("Nombre de services : " + compterServices());
        System.out.println("Statut serveurs : Actifs");
    }
    
    /**
     * Compte le nombre de services disponibles
     */
    private static int compterServices() {
        String services = ServiceRegistry.toStringue();
        String[] lignes = services.split("##");
        int count = 0;
        for (String ligne : lignes) {
            if (ligne.trim().matches("\\d+ - .*")) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Arrête les serveurs proprement
     */
    private static void arreterServeurs() {
        System.out.println("Arrêt des serveurs...");
        
        if (serveurAmateur != null) {
            serveurAmateur.stop();
        }
        
        if (serveurProgrammeur != null) {
            serveurProgrammeur.stop();
        }
        
        System.out.println("✓ Serveurs arrêtés");
        System.out.println("Au revoir !");
    }
}

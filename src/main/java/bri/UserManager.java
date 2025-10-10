package bri;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Gestionnaire des utilisateurs de la plateforme BRi
 * Gère l'authentification et les informations des programmeurs
 */
public class UserManager {
    
    /**
     * Classe pour représenter un programmeur
     */
    public static class Programmeur {
        private final String login;
        private final String password;
        private String ftpUrl;
        
        public Programmeur(String login, String password, String ftpUrl) {
            this.login = login;
            this.password = password;
            this.ftpUrl = ftpUrl;
        }
        
        public String getLogin() { return login; }
        public String getPassword() { return password; }
        public String getFtpUrl() { return ftpUrl; }
        public void setFtpUrl(String ftpUrl) { this.ftpUrl = ftpUrl; }
    }
    
    // Map thread-safe des programmeurs
    private static final Map<String, Programmeur> programmeurs = new ConcurrentHashMap<>();
    
    static {
        // Ajouter quelques programmeurs de test
        programmeurs.put("testprog", new Programmeur("testprog", "password123", "ftp://127.0.0.1:2121/"));
        programmeurs.put("exemple", new Programmeur("exemple", "motdepasse", "ftp://127.0.0.1:2121/"));
    }
    
    /**
     * Authentifie un programmeur
     * @param login Login du programmeur
     * @param password Mot de passe
     * @return true si l'authentification réussit
     */
    public static boolean authenticate(String login, String password) {
        Programmeur prog = programmeurs.get(login);
        return prog != null && prog.getPassword().equals(password);
    }
    
    /**
     * Crée un nouveau compte programmeur
     * @param login Login unique
     * @param password Mot de passe
     * @param ftpUrl URL du serveur FTP
     * @return true si le compte est créé avec succès
     */
    public static boolean createProgrammer(String login, String password, String ftpUrl) {
        if (programmeurs.containsKey(login)) {
            return false; // Login déjà utilisé
        }
        
        programmeurs.put(login, new Programmeur(login, password, ftpUrl));
        System.out.println("Nouveau programmeur créé : " + login);
        return true;
    }
    
    /**
     * Récupère les informations d'un programmeur
     * @param login Login du programmeur
     * @return Objet Programmeur ou null si non trouvé
     */
    public static Programmeur getProgrammer(String login) {
        return programmeurs.get(login);
    }
    
    /**
     * Met à jour l'URL FTP d'un programmeur
     * @param login Login du programmeur
     * @param newFtpUrl Nouvelle URL FTP
     * @return true si la mise à jour réussit
     */
    public static boolean updateFtpUrl(String login, String newFtpUrl) {
        Programmeur prog = programmeurs.get(login);
        if (prog != null) {
            prog.setFtpUrl(newFtpUrl);
            System.out.println("URL FTP mise à jour pour " + login + " : " + newFtpUrl);
            return true;
        }
        return false;
    }
    
    /**
     * Vérifie si un programmeur existe
     * @param login Login à vérifier
     * @return true si le programmeur existe
     */
    public static boolean exists(String login) {
        return programmeurs.containsKey(login);
    }
}

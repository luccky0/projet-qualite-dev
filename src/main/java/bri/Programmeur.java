package bri;

import java.util.HashMap;
import java.util.Map;

public class Programmeur {
    private String login;
    private String motDePasse;
    private String ftpUrl;
    
    private static Map<String, Programmeur> programmeurs = new HashMap<>();
    
    public Programmeur(String login, String motDePasse, String ftpUrl) {
        this.login = login;
        this.motDePasse = motDePasse;
        this.ftpUrl = ftpUrl;
    }
    
    public static void ajouterProgrammeur(String login, String motDePasse, String ftpUrl) {
        programmeurs.put(login, new Programmeur(login, motDePasse, ftpUrl));
        System.out.println("Programmeur ajouté : " + login);
    }
    
    public static Programmeur authentifier(String login, String motDePasse) {
        Programmeur prog = programmeurs.get(login);
        if (prog != null && prog.motDePasse.equals(motDePasse)) {
            return prog;
        }
        return null;
    }
    
    // Getters
    public String getLogin() { return login; }
    public String getFtpUrl() { return ftpUrl; }
    
    public void setFtpUrl(String ftpUrl) { 
        this.ftpUrl = ftpUrl; 
        System.out.println("URL FTP mise à jour pour " + login + " : " + ftpUrl);
    }
    
    // Ajouter quelques programmeurs par défaut pour les tests
    static {
        ajouterProgrammeur("brette", "123", "ftp://localhost/brette/");
        ajouterProgrammeur("admin", "admin", "ftp://localhost/admin/");
        ajouterProgrammeur("test", "test", "ftp://localhost/test/");
    }
}
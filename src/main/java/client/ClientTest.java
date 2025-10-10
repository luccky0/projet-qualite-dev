package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Client simple pour tester la connexion aux serveurs BRi
 */
public class ClientTest {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Client de Test BRi ===");
        System.out.println("1 - Se connecter comme amateur");
        System.out.println("2 - Se connecter comme programmeur");
        System.out.print("Votre choix : ");
        
        String choix = scanner.nextLine().trim();
        
        switch (choix) {
            case "1":
                connecterCommeAmateur();
                break;
            case "2":
                connecterCommeProgrammeur(scanner);
                break;
            default:
                System.out.println("Choix invalide");
        }
        
        scanner.close();
    }
    
    /**
     * Se connecte au serveur amateur
     */
    private static void connecterCommeAmateur() {
        try (Socket socket = new Socket("localhost", 3000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.println("Connecté au serveur amateur");
            
            // Lire le menu des services
            String line;
            while ((line = in.readLine()) != null) {
                if (line.endsWith(":")) {
                    System.out.println(line);
                    break;
                } else {
                    System.out.println(line.replace("##", "\n"));
                }
            }
            
            // Envoyer le choix
            String choix = scanner.nextLine();
            out.println(choix);
            
            // Lire la réponse du service
            while ((line = in.readLine()) != null) {
                if (line.startsWith("=== Service")) {
                    System.out.println(line);
                    break;
                } else if (line.startsWith("ERREUR")) {
                    System.out.println(line);
                    return;
                }
            }
            
            // Interagir avec le service
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                if (line.endsWith(":")) {
                    String input = scanner.nextLine();
                    out.println(input);
                } else if (line.equals("Service terminé.")) {
                    break;
                }
            }
            
        } catch (IOException e) {
            System.err.println("Erreur de connexion amateur : " + e.getMessage());
        }
    }
    
    /**
     * Se connecte au serveur programmeur
     */
    private static void connecterCommeProgrammeur(Scanner mainScanner) {
        try (Socket socket = new Socket("localhost", 3001);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            System.out.println("Connecté au serveur programmeur");
            
            String line;
            // Authentification
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                if (line.endsWith(":")) {
                    String input = mainScanner.nextLine();
                    out.println(input);
                }
                if (line.contains("Bienvenue")) {
                    break;
                }
                if (line.contains("Authentification échouée")) {
                    return;
                }
            }
            
            // Menu programmeur
            while ((line = in.readLine()) != null) {
                if (line.startsWith("##")) {
                    System.out.println(line.replace("##", ""));
                } else if (line.endsWith(":")) {
                    System.out.println(line);
                    String input = mainScanner.nextLine();
                    out.println(input);
                    if (input.equals("5")) {
                        break;
                    }
                } else {
                    System.out.println(line);
                }
            }
            
        } catch (IOException e) {
            System.err.println("Erreur de connexion programmeur : " + e.getMessage());
        }
    }
}

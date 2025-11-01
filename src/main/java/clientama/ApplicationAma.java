package clientama;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client pour les amateurs de services BRi
 * Se connecte au port PORT_AMA pour utiliser les services disponibles
 */
public class ApplicationAma {
    private final static int PORT_AMA = 3000;
    private final static String HOST = "localhost";

    public static void main(String[] args) {
        Socket s = null;
        try {
            s = new Socket(HOST, PORT_AMA);

            BufferedReader sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter sout = new PrintWriter(s.getOutputStream(), true);
            BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Connecté au serveur BRi Amateurs " + s.getInetAddress() + ":" + s.getPort());

            String line;

            
            line = sin.readLine();
            if (line != null) {
                System.out.println(line.replaceAll("##", "\n"));
                
                
                String choix = clavier.readLine();
                sout.println(choix);

                
                while (true) {
                    
                    line = sin.readLine();
                    if (line == null) {
                        break; 
                    }

                    
                    System.out.println(line.replaceAll("##", "\n"));

                    
                    String reponse = clavier.readLine();
                    sout.println(reponse);

                }
            }

        } catch (IOException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        } finally {
            // Refermer dans tous les cas la socket
            try {
                if (s != null) s.close();
            } catch (IOException e2) {
                System.err.println("Erreur lors de la fermeture : " + e2.getMessage());
            }
        }
    }
}
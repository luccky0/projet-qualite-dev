package bri;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Registre central des services BRi
 * Gère l'ajout, la validation et l'accès aux services
 */
public class ServiceRegistry {
    // Liste thread-safe des classes de services
    private static final List<Class<?>> servicesClasses = new ArrayList<>();
    
    // Map pour associer les services aux programmeurs
    private static final Map<String, List<String>> programmerServices = new ConcurrentHashMap<>();
    
    // Synchronisation pour les opérations sur la liste
    private static final Object lock = new Object();
    
    /**
     * Ajoute une classe de service après contrôle de la norme BRi
     * @param serviceClass La classe du service à ajouter
     * @param programmeurLogin Le login du programmeur propriétaire
     * @throws Exception Si la classe ne respecte pas la norme BRi
     */
    public static void addService(Class<?> serviceClass, String programmeurLogin) throws Exception {
        validateServiceClass(serviceClass);
        validatePackageName(serviceClass, programmeurLogin);
        
        synchronized (lock) {
            servicesClasses.add(serviceClass);
            
            // Associer le service au programmeur
            programmerServices.computeIfAbsent(programmeurLogin, k -> new ArrayList<>())
                             .add(serviceClass.getSimpleName());
        }
        
        System.out.println("Service ajouté : " + serviceClass.getSimpleName() + 
                          " par " + programmeurLogin);
    }
    
    /**
     * Valide qu'une classe de service respecte la norme BRi
     */
    private static void validateServiceClass(Class<?> serviceClass) throws Exception {
        // Vérifier que la classe implémente Service
        if (!Service.class.isAssignableFrom(serviceClass)) {
            throw new Exception("La classe doit implémenter l'interface Service");
        }
        
        // Vérifier que la classe n'est pas abstraite
        if (Modifier.isAbstract(serviceClass.getModifiers())) {
            throw new Exception("La classe ne doit pas être abstraite");
        }
        
        // Vérifier que la classe est publique
        if (!Modifier.isPublic(serviceClass.getModifiers())) {
            throw new Exception("La classe doit être publique");
        }
        
        // Vérifier le constructeur public (Socket)
        try {
            Constructor<?> constructor = serviceClass.getConstructor(Socket.class);
            if (!Modifier.isPublic(constructor.getModifiers())) {
                throw new Exception("Le constructeur (Socket) doit être public");
            }
        } catch (NoSuchMethodException e) {
            throw new Exception("La classe doit avoir un constructeur public (Socket)");
        }
        
        // Vérifier la méthode toStringue()
        try {
            Method toStringueMethod = serviceClass.getMethod("toStringue");
            if (!Modifier.isStatic(toStringueMethod.getModifiers()) || 
                !Modifier.isPublic(toStringueMethod.getModifiers()) ||
                !toStringueMethod.getReturnType().equals(String.class)) {
                throw new Exception("La méthode toStringue() doit être public static String");
            }
        } catch (NoSuchMethodException e) {
            throw new Exception("La classe doit avoir une méthode public static String toStringue()");
        }
    }
    
    /**
     * Valide que le package de la classe correspond au login du programmeur
     */
    private static void validatePackageName(Class<?> serviceClass, String programmeurLogin) throws Exception {
        String packageName = serviceClass.getPackage().getName();
        if (!packageName.equals(programmeurLogin)) {
            throw new Exception("Le service doit être dans un package portant le nom du programmeur: " + programmeurLogin);
        }
    }
    
    /**
     * Récupère la classe de service par son numéro
     * @param numService Numéro du service (1-based)
     * @return La classe du service ou null si non trouvée
     */
    public static Class<?> getServiceClass(int numService) {
        synchronized (lock) {
            if (numService <= 0 || numService > servicesClasses.size()) {
                return null;
            }
            return servicesClasses.get(numService - 1);
        }
    }
    
    /**
     * Génère la liste des activités présentes
     * @return String formatée avec la liste des services
     */
    public static String toStringue() {
        synchronized (lock) {
            StringBuilder result = new StringBuilder("Activités présentes :##");
            for (int i = 0; i < servicesClasses.size(); i++) {
                try {
                    Class<?> serviceClass = servicesClasses.get(i);
                    Method toStringueMethod = serviceClass.getMethod("toStringue");
                    String description = (String) toStringueMethod.invoke(null);
                    result.append(i + 1).append(" - ").append(description).append("##");
                } catch (Exception e) {
                    result.append(i + 1).append(" - ").append(servicesClasses.get(i).getSimpleName()).append("##");
                }
            }
            return result.toString();
        }
    }
    
    /**
     * Récupère les services d'un programmeur
     * @param programmeurLogin Login du programmeur
     * @return Liste des noms de services
     */
    public static List<String> getServicesForProgrammer(String programmeurLogin) {
        return programmerServices.getOrDefault(programmeurLogin, new ArrayList<>());
    }
    
    /**
     * Supprime un service
     * @param serviceClass Classe du service à supprimer
     * @param programmeurLogin Login du programmeur propriétaire
     * @return true si supprimé avec succès
     */
    public static boolean removeService(Class<?> serviceClass, String programmeurLogin) {
        synchronized (lock) {
            boolean removed = servicesClasses.remove(serviceClass);
            if (removed) {
                List<String> services = programmerServices.get(programmeurLogin);
                if (services != null) {
                    services.remove(serviceClass.getSimpleName());
                }
                System.out.println("Service supprimé : " + serviceClass.getSimpleName());
            }
            return removed;
        }
    }
}

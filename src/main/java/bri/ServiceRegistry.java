package bri;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceRegistry {
    private static List<Class<?>> servicesClasses = new ArrayList<>();
    private static Map<Class<?>, String> serviceOwners = new HashMap<>();
    private static Map<Class<?>, Boolean> serviceStates = new HashMap<>();

    // ajoute une classe de service apr�s contr�le de la norme BLTi
    public static void addService(Class<?> serviceClass, String ownerLogin) throws Exception {
        validateServiceClass(serviceClass, ownerLogin);
        
        // Vérifier si le service n'existe pas déjà
        for (Class<?> existingClass : servicesClasses) {
            if (existingClass.getName().equals(serviceClass.getName())) {
                throw new Exception("Service " + serviceClass.getSimpleName() + " existe déjà !");
            }
        }
        
        servicesClasses.add(serviceClass);
        serviceOwners.put(serviceClass, ownerLogin);
        serviceStates.put(serviceClass, true);
        
        System.out.println("Service ajouté : " + serviceClass.getSimpleName() + " par " + ownerLogin);
    }
    
    public static void addService(Class<?> serviceClass) throws Exception {
        addService(serviceClass, "system");
    }
    
    private static void validateServiceClass(Class<?> serviceClass, String ownerLogin) throws Exception {
        // V�rifications de la norme BRi
        if (!Service.class.isAssignableFrom(serviceClass)) {
            throw new Exception("La classe doit impl�menter l'interface Service");
        }
        
        if (Modifier.isAbstract(serviceClass.getModifiers())) {
            throw new Exception("La classe ne doit pas �tre abstraite");
        }
        
        if (!Modifier.isPublic(serviceClass.getModifiers())) {
            throw new Exception("La classe doit �tre publique");
        }
        
        // V�rifier le package (doit correspondre au login du programmeur)
        String packageName = serviceClass.getPackage().getName();
        if (!packageName.equals(ownerLogin) && !ownerLogin.equals("system")) {
            throw new Exception("Le package doit porter le nom du login : " + ownerLogin);
        }
        
        try {
            Constructor<?> constructor = serviceClass.getConstructor(Socket.class);
            if (!Modifier.isPublic(constructor.getModifiers())) {
                throw new Exception("Le constructeur (Socket) doit �tre public");
            }
        } catch (NoSuchMethodException e) {
            throw new Exception("La classe doit avoir un constructeur public (Socket)");
        }
        
        try {
            Method toStringueMethod = serviceClass.getMethod("toStringue");
            if (!Modifier.isStatic(toStringueMethod.getModifiers()) || 
                !Modifier.isPublic(toStringueMethod.getModifiers()) ||
                !toStringueMethod.getReturnType().equals(String.class)) {
                throw new Exception("La m�thode toStringue() doit �tre public static String");
            }
        } catch (NoSuchMethodException e) {
            throw new Exception("La classe doit avoir une m�thode public static String toStringue()");
        }
    }
    
    // renvoie la classe de service (numService -1)	
    public static Class<?> getServiceClass(int numService) {
        if (numService <= 0 || numService > servicesClasses.size()) {
            return null;
        }
        return servicesClasses.get(numService - 1);
    }
    
    // liste les activit�s pr�sentes
    public static String toStringue() {
        String result = "Activit�s pr�sentes :##";
        int compteur = 1;
        for (Class<?> serviceClass : servicesClasses) {
            // Afficher seulement les services d�marr�s
            if (serviceStates.get(serviceClass)) {
                try {
                    Method toStringueMethod = serviceClass.getMethod("toStringue");
                    String description = (String) toStringueMethod.invoke(null);
                    result += compteur + " - " + description + "##";
                    compteur++;
                } catch (Exception e) {
                    result += compteur + " - " + serviceClass.getSimpleName() + "##";
                    compteur++;
                }
            }
        }
        return result;
    }
    
    public static String toStringueForProgrammer(String ownerLogin) {
        String result = "Vos services :##";
        int compteur = 1;
        for (Class<?> serviceClass : servicesClasses) {
            if (serviceOwners.get(serviceClass).equals(ownerLogin)) {
                try {
                    Method toStringueMethod = serviceClass.getMethod("toStringue");
                    String description = (String) toStringueMethod.invoke(null);
                    String etat = serviceStates.get(serviceClass) ? "D�MARR�" : "ARR�T�";
                    result += compteur + " - " + description + " [" + etat + "]##";
                    compteur++;
                } catch (Exception e) {
                    result += compteur + " - " + serviceClass.getSimpleName() + "##";
                    compteur++;
                }
            }
        }
        return result;
    }
    
    public static boolean updateService(Class<?> oldClass, Class<?> newClass, String ownerLogin) throws Exception {
        int index = servicesClasses.indexOf(oldClass);
        if (index >= 0 && serviceOwners.get(oldClass).equals(ownerLogin)) {
            validateServiceClass(newClass, ownerLogin);
            servicesClasses.set(index, newClass);
            serviceOwners.remove(oldClass);
            serviceStates.remove(oldClass);
            serviceOwners.put(newClass, ownerLogin);
            serviceStates.put(newClass, true);
            System.out.println("Service mis � jour : " + newClass.getSimpleName());
            return true;
        }
        return false;
    }
    
    public static boolean removeService(String serviceName, String ownerLogin) {
        for (Class<?> serviceClass : servicesClasses) {
            if (serviceClass.getSimpleName().equals(serviceName) && 
                serviceOwners.get(serviceClass).equals(ownerLogin)) {
                servicesClasses.remove(serviceClass);
                serviceOwners.remove(serviceClass);
                serviceStates.remove(serviceClass);
                System.out.println("Service supprim� : " + serviceName);
                return true;
            }
        }
        return false;
    }
    
    public static boolean toggleService(String serviceName, String ownerLogin) {
        for (Class<?> serviceClass : servicesClasses) {
            if (serviceClass.getSimpleName().equals(serviceName) && 
                serviceOwners.get(serviceClass).equals(ownerLogin)) {
                boolean currentState = serviceStates.get(serviceClass);
                serviceStates.put(serviceClass, !currentState);
                System.out.println("Service " + serviceName + " : " + (!currentState ? "D�MARR�" : "ARR�T�"));
                return true;
            }
        }
        return false;
    }
}

# Projet BRi - Plateforme de Services Dynamiques

## Description

Ce projet implémente une plateforme de services dynamiques inspirée d'OSGi, basée sur l'architecture du TP4. La plateforme BRi permet :

- Aux **programmeurs** de déployer et gérer leurs services dynamiquement
- Aux **amateurs** de consommer ces services facilement
- Le chargement dynamique de services depuis des serveurs FTP
- L'authentification et la gestion des comptes

## Architecture

### Composants principaux

1. **BRiLaunch** (`appli.BRiLaunch`) - Application principale
2. **ServeurAmateur** (`bri.ServeurAmateur`) - Serveur pour les clients amateurs (port 3000)
3. **ServeurProgrammeur** (`bri.ServeurProgrammeur`) - Serveur pour les programmeurs (port 3001)
4. **ServiceRegistry** (`bri.ServiceRegistry`) - Registre central des services
5. **UserManager** (`bri.UserManager`) - Gestion des utilisateurs et authentification

### Norme BRi

Les services doivent respecter la norme BRi :
- Implémenter l'interface `Service` (qui étend `Runnable`)
- Avoir un constructeur public prenant un `Socket` en paramètre
- Avoir une méthode `public static String toStringue()` pour la description
- Être dans un package portant le nom du programmeur
- Être une classe publique et non abstraite

## Utilisation

### Démarrage du serveur

```bash
./run.sh
```

Ou manuellement :
```bash
# Compilation
find src -name "*.java" -exec javac -cp "lib/junit-platform-console-standalone-1.13.0-M3.jar" {} +

# Exécution
java -cp "src/main/java:lib/junit-platform-console-standalone-1.13.0-M3.jar" appli.BRiLaunch
```

### Connexion des clients

#### Client Amateur (port 3000)
- Se connecte pour utiliser les services disponibles
- Choisit un service dans le menu et l'utilise

#### Client Programmeur (port 3001)
- S'authentifie avec login/mot de passe
- Peut ajouter de nouveaux services
- Peut gérer ses services existants
- Peut changer son adresse FTP

### Comptes de test

- **Login**: `testprog`, **Mot de passe**: `password123`
- **Login**: `exemple`, **Mot de passe**: `motdepasse`

### Client de test

Pour tester les connexions :
```bash
java -cp "src/main/java" client.ClientTest
```

## Services d'exemple

### ServiceInversion (package `examples`)
- Inverse le texte fourni par l'utilisateur
- Service de base pour les tests

### ServiceCapitalisation (package `testprog`)
- Met le texte en majuscules
- Exemple de service dans un package programmeur

## Structure du projet

```
src/main/java/
├── appli/
│   └── BRiLaunch.java          # Application principale
├── bri/
│   ├── Service.java            # Interface de base
│   ├── ServiceRegistry.java    # Registre des services
│   ├── UserManager.java        # Gestion utilisateurs
│   ├── ServeurAmateur.java     # Serveur amateurs
│   ├── ServiceAmateur.java     # Traitement clients amateurs
│   ├── ServeurProgrammeur.java # Serveur programmeurs
│   └── ServiceProgrammeur.java # Traitement clients programmeurs
├── examples/
│   └── ServiceInversion.java   # Service d'exemple
├── testprog/
│   └── ServiceCapitalisation.java # Service de test
└── client/
    └── ClientTest.java         # Client de test
```

## Fonctionnalités implémentées

✅ **Base du serveur BRi**
- Serveurs TCP séparés pour amateurs et programmeurs
- Gestion multi-thread des connexions
- Interface d'administration en ligne de commande

✅ **Gestion des services**
- Registre centralisé thread-safe
- Validation de la norme BRi
- Chargement dynamique depuis FTP

✅ **Authentification**
- Système de login/mot de passe pour les programmeurs
- Gestion des comptes utilisateurs

✅ **Services pour amateurs**
- Menu des services disponibles
- Exécution des services choisis

✅ **Services pour programmeurs**
- Ajout de nouveaux services
- Gestion des services existants
- Modification des paramètres FTP

## Fonctionnalités à implémenter (extensions)

⏳ **Extensions possibles**
- Mise à jour de services existants
- Démarrage/arrêt de services individuels
- Désinstallation de services
- Support des bibliothèques .jar
- Services avec ressources partagées (messagerie)
- Services avec échange de fichiers

## Tests

Le projet peut être testé avec :
1. Le serveur FTP d'Apache fourni dans le TP4
2. Les services d'exemple fournis
3. Le client de test inclus

## Conformité au sujet

Ce projet respecte les spécifications du sujet :
- ✅ Serveurs séparés pour amateurs et programmeurs
- ✅ Authentification des programmeurs
- ✅ Chargement dynamique depuis FTP
- ✅ Norme BRi respectée
- ✅ Services de base (inversion de texte)
- ✅ Gestion des packages par programmeur

## Notes de développement

Le projet utilise l'architecture du TP4 comme base et l'étend pour supporter :
- Deux ports de connexion distincts
- Authentification et gestion d'utilisateurs
- Interface d'administration
- Meilleure gestion des erreurs et de la concurrence

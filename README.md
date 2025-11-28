README — Projet JavaFX SGEB

------------------------------------------------------------
Projet JavaFX — SGEB
Ce projet universitaire consiste à développer une application desktop en JavaFX permettant la gestion d’une bibliothèque.

Le projet a été réalisé en Java 19 avec JavaFX 21.
------------------------------------------------------------
PRÉREQUIS
------------------------------------------------------------
Pour exécuter l’application :

1. Java JDK 19 ou 17 (recommandé)
   Vérification :
   java -version

2. JavaFX SDK 21.0.5
   Le dossier lib/javafx-sdk-21.0.5 est déjà inclus dans le projet.

------------------------------------------------------------
BASE DE DONNÉES
------------------------------------------------------------
L’application utilise une base SQLite fournie avec le projet :

Fichier : sgbd.db  
Emplacement : racine du projet

➡️ Aucun serveur n’est requis — la base est entièrement intégrée.  
➡️ Les tables, données de test et structures nécessaires y sont déjà présentes.

L’application se connecte automatiquement à ce fichier lors du lancement.

------------------------------------------------------------
STRUCTURE DU PROJET
------------------------------------------------------------
projet/
 ├── lib/
 │    └── javafx-sdk-21.0.5/
 ├── target/
 │    ├── sgeb-javafx-1.0-SNAPSHOT.jar
 ├── sgbd.db          
 ├── src/
 ├── pom.xml
 └── README.txt

------------------------------------------------------------
EXÉCUTION DE L’APPLICATION
------------------------------------------------------------
- Se placer dans le dossier racine du projet 

COMMANDES :

--- Windows ---
java --module-path "lib\javafx-sdk-21.0.5\lib" --add-modules javafx.controls,javafx.fxml -jar target\sgeb-javafx-1.0-SNAPSHOT.jar

--- macOS / Linux ---
java --module-path "lib/javafx-sdk-21.0.5/lib" --add-modules javafx.controls,javafx.fxml -jar target/sgeb-javafx-1.0-SNAPSHOT.jar

------------------------------------------------------------
COMPILATION AVEC MAVEN
------------------------------------------------------------
Pour recompiler le projet :

mvn clean package

Le fichier exécutable sera généré dans :
target/sgeb-javafx-1.0-SNAPSHOT.jar

------------------------------------------------------------
NOTES IMPORTANTES
------------------------------------------------------------
- JavaFX n'est plus intégré dans Java depuis la version 11, d’où la nécessité du module-path.
- La base SQLite (sgbd.db) ne nécessite aucune installation.

------------------------------------------------------------
AUTEURS
------------------------------------------------------------
Manel Boumahdi / Hocine Mohammed Walid 
Année 2025-2026

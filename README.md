# Mail-Catcher

## Objectif

Fournir un projet compilable et exécutable, capable de capturer des mails depuis diverses sources et les sauvegarder dans un répertoire local.

## Sépcifications requises

* Utilisation de Java 8
* Utilisation de MAVEN comme format du projet
* Utilisation de l’API standard Java Mail
* Les protocoles supportés seront POP3 / IMAP /Microsoft EWS
* Chaques paramètres devra être présent dans un fichier unique application.properties


Le programme devra lister les mail contenus dans la boite avec un ou plusieurs filtre suivant :

* Filtre sur l’email de l’émetteur du mail ( filtre requis au minimum )
* Filtre sur la présence de pièces jointes
* Filtre sur un mot présent dans le sujet du mail



Ensuite en utilisant les mails filtrés, il faut pour chaques mails :

* Créer un répertoire avec l’email émetteur
* Générer un numéro incrémental pour chaques mails
* Avec ce numéro sauvegarder dans le répertoire de l’émetteur :
    * Le mail au format EML ( `<numéro>.eml` )
    * Le sujet du mail et le corps dans un fichier TXT ( `<numéro>.txt`)
    * Pièce jointe si présente (`<numéro>_<nom pièce  jointe>.<extension pièce jointe>`)
    * Générer un fichier JSON de résumé dans ce même répertoire au format :

```json
[
    {
        "id" : "<numéro>"
        "subject" : "<sujet du mail>"
    },{
        "id" : "<numéro>"
        "subject" : "<sujet du mail>"
    },{
        ...
    }
]
```



## Comment l'utiliser

L'application à été conçu afin de pouvoir choisir son protocole de communication avec le serveur mail. Trois choix sont possibles : 
* IMAP
* POP3
* Microsoft Exchange EWS

Pour faire ce choix il suffit de modifier le fichier de paramétrage se trouvant dans "src/main/java/resources/application.properties".

### Détails du paramétrage
#### Configuration générale
Property | Valeur | Description
------------ | ------------- | -------------
mail.debug.enable | true/false | (Optionnel) Pour activer le mode DEBUG. Désactivé par défaut.
mail.output.path | String  | Le nom du répertoire où seront enregistrés les mails.

#### Connfiguration mail
Property | Valeur | Description
------------ | ------------- | -------------
mail.store.protocol | imap/pop3/ews | Protocole à utiliser pour la récupération des mails. Par défaut, l'application choisi IMAP
mail.host | URL | L'URL du serveur de mail.
mail.user | String | Le nom d'utilisateur pour se connecter au serveur mail.
mail.ews.domain | String | Pour le protocole Exchange EWS, le nom du domaine de l'utilisateur.
mail.ews.email | Email | Pour le protocole Exchange EWS, l'email de l'utilisateur.
mail.password | String | Le mot de passe de connexion au serveur mail pour l'utilisateur.

#### Configuration des filtres
Property | Valeur | Description
------------ | ------------- | -------------
mail.filter.from | Email | L'email de l'expéditeur.
mail.filter.has.pj | true/false | (Optionnel) Avec une pièce jointe ou non.
mail.filter.subject | String | (Optionnel) Terme ou phrase à rechercher dans l'objet des mails.

### Execution
Un fois la configuration souhaité a été mise en place, lancer une compilation Maven :
```
mvn clean package
```

La compilation génère un jar executable et portable. Il se trouvera dans le dossier target du projet. Il ne reste plus qu'à lancer mail-catcher :
```
java -jar mail-catcher-1.0-SNAPSHOT.jar
```

Un fichier de log sera créé dans le répertoire d'execution de l'application.
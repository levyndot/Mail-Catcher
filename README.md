# Mail Catcher

## Objectif

Une application Java capable de capturer des mails depuis diverses sources et les sauvegarder dans un répertoire local.

## Sépicifications

* Java 8
* MAVEN 3.3.3
* API standard Java Mail
* Protocoles supportés : POP3 / IMAP /Microsoft EWS

Mail Catcher récupère les mails contenus dans une boite mail avec un ou plusieurs filtre suivant :

* email de l’émetteur du mail (requis)
* présence de pièces jointes
* mot présent dans le sujet du mail

Puis pour chaques mails :

* Création d'un répertoire avec l’email émetteur
* Génération d'un numéro incrémental pour chaques mails et avec ce numéro, sauvegarde dans le répertoire de l’émetteur :
    * Le mail au format EML ( `<numéro>.eml` )
    * Le sujet du mail et le corps dans un fichier TXT ( `<numéro>.txt`)
    * Pièce(s) jointe(s) si présente(nt) (`<numéro>_<nom pièce  jointe>.<extension pièce jointe>`)
    * Génération d'un fichier JSON de résumé dans le répertoire de l’émetteur au format :

```json
[
    {
        "id" : "<numéro>"
        "subject" : "<sujet du mail>"
    },{
        "id" : "<numéro>"
        "subject" : "<sujet du mail>"
    },
    ...
]
```



## Comment l'utiliser

Il faut avant tout paramétrer l'application afin qu'elle puisse se connecter au serveur avec les bons crédentials et le protocole souhaité. Ces informations doivent être renseigné dans le fichier "src/main/java/resources/application.properties".

### Détails du paramétrage
#### Configuration générale
Configuration générique, transverse a toute l'application.

Property | Valeur | Description
------------ | ------------- | -------------
mail.debug.enable | true/false | (Optionnel) Pour activer le mode DEBUG. Par défaut : false.
mail.output.path | String  | Le nom du répertoire où seront enregistrés les mails.

#### Connfiguration mail
Configuration du protocole de communication, et connection au serveur mail.

Property | Valeur | Description
------------ | ------------- | -------------
mail.store.protocol | imap/pop3/ews | Protocole à utiliser. Par défaut : IMAP.
mail.host | URL | L'URL du serveur mail.
mail.user | String | Le nom d'utilisateur du compte mail.
mail.ews.domain | String | Pour le protocole Exchange EWS, le nom du domaine de l'utilisateur.
mail.ews.email | Email | Pour le protocole Exchange EWS, l'email de l'utilisateur.
mail.password | String | Le mot de passe de l'utilisateur.

#### Configuration des filtres
Filtres applicables.

Property | Valeur | Description
------------ | ------------- | -------------
mail.filter.from | String (xxx@xxx.xxx) | L'email de l'expéditeur.
mail.filter.has.pj | true/false | (Optionnel) Avec une pièce jointe ou non.
mail.filter.subject | String | (Optionnel) Terme ou phrase à rechercher dans l'objet des mails.

### Execution
Un fois que Mail-Catcher est correctement paramétré, lancer une compilation :
```
mvn clean package
```

Ce qui donnera naissance à un jar executable et portable dans le dossier `target` du projet. Il ne reste plus qu'à lancer Mail Catcher :
```
java -jar mail-catcher-1.0-SNAPSHOT.jar
```

Un fichier de log sera créé dans le répertoire d'execution de l'application.

# Workshop Quarkus

Le but de ce workshop est de vous présenter comment boostraper une application sous Quarkus et la déployer sur Kubernetes

Il est très fortement inspiré d'un workshop disponible sur le site officiel https://quarkus.io/quarkus-workshops/super-heroes/

## Pourquoi Quarkus

Quarkus répond au besoin de pouvoir exécuter des applications Java dans le cloud, notamment en utilisant le mode serverless des différents clouds  (e.g AWS Lambda ). Jusqu'à présent, cela était impossible du fait du temps de démarrage d'une application (plusieurs secondes, voir dizaines de secondes). Un autre objectif est également de réduire l'empreinte mémoire des applications (qui peuvent faire plus d'une dizaine de Mo pour un simple "Hello World").

On peut voir Quarkus comme un intermédiaire entre GraalVM et les différents frameworks Java.

### Sa philosophie

L'idée, c'est  de transférer le plus possible de la charge qui est traditionnellement effectuée lors du démarrage de l'application, vers l'étape de build de l'application. Cette étape sera donc plus longue mais ne sera faite qu'une seule fois.

Quarkus permet également d'avoir de la programmation impérative et de la programmation réactive dans la même stack.

### GraalVM

#### Rapide présentation

GraalVM est une machine virtuelle qui permet d'exécuter différents langages dans un même code (ceux supportés par la JVM et la LLVM, JS, Python, Ruby, R). Du coup, on peut se retrouver avec du code mixant du JS dans du Java :

```java
import org.graalvm.polyglot.*;

class Polyglot {
    public static void main(String[] args) {
        Context polyglot = Context.create();
        Value array = polyglot.eval("js", "[1,2,42,4]");
        int result = array.getArrayElement(2).asInt();
        System.out.println(result);
    }
}
```

Une autre caractéristique intéressante est que GraalVM permet de compiler des applications en natif (en utilisant la compilation Ahead-Of-Time). Cela permet d'avoir d'avoir des images plus légères et plus rapides à démarrer. L'exécutable contient SubstrateVM qui contient tous les éléments nécessaires au runtime. L'utilitaire de GraalVM *native-image* effectue une analyse statique du code afin d'enlever le maximum de code inutile dans le binaire final.

#### Pricing

GraalVM possède deux licences : la version communautaire et la version entreprise. La version entreprise apporte notamment des améliorations en terme de performance et de sécurité (le détail sur la page de [téléchargement](https://www.graalvm.org/downloads/)) Le [prix](https://shop.oracle.com/apex/f?p=dstore:4:9944669784388:::RIR:IR_ROWFILTER:graalvm) de la version entreprise varie selon le nombre de licences achetées, de 130 à 194€. Quarkus fonctionne avec les deux versions.

## Démarrer un nouveau projet

### Pré requis

- Un JDK 8 ou 11+ (GraalVM pour faire de la compilation native, c'est le choix fait pour ce workshop, version 20.0.0.r8-grl )  Changer facilement de SDK entre les projets => https://sdkman.io/

- Maven 3.6.2+

- Docker

L'ensemble des commandes de ce workshop ont été testées sur Linux (Ubuntu 19.10). Cela devrait marcher sur Mac.

### But de notre application

Nous allons créer une appli web permettant de suivre les bilans psychomoteurs réalisés par une psychomotricienne. Cela permet de suivre les progrès réalisés par les patients en fonction de leurs pathologies. Nous appellerons cette application *Zykomot*.

### Générer un Hello World

Tout d'abord ouvrir un terminal et se rendre dans le répertoire où vous voulez initialiser le projet.

```shell
mvn io.quarkus:quarkus-maven-plugin:1.2.1.Final:create \
    -DprojectGroupId=com.zenika \
    -DprojectArtifactId=zykomot \
    -DclassName="org.zenika.zykomot.PatientResource" \
    -Dpath="/patient"
```

  Quarkus vient avec un plugin Maven qui va nous générer toute la structure dont nous avons besoin.

Il nous faut juste renseigner : 

- le Group ID
- l'Artifact ID
- le nom de la classe qui sera générée
- le chemin pour y accéder

Voici l'arborescence créée :

```shell
└── zykomot
    ├── mvnw
    ├── mvnw.cmd
    ├── pom.xml
    ├── README.md
    └── src
        ├── main
        │   ├── docker
        │   │   ├── Dockerfile.jvm
        │   │   └── Dockerfile.native
        │   ├── java
        │   │   └── org
        │   │       └── zenika
        │   │           └── zykomot
        │   │               └── PatientResource.java
        │   └── resources
        │       ├── application.properties
        │       └── META-INF
        │           └── resources
        │               └── index.html
        └── test
            └── java
                └── org
                    └── zenika
                        └── zykomot
                            ├── NativepatientResourceIT.java
                            └── patientResourceTest.java

```

Plusieurs choses intéressantes : 

- un utilitaire *mvnw* qui va nous permettre de lancer des commandes "Quarkus" sans avoir à passer toutes les options à GraalVM.
- un pom.xml avec pas mal de dépendances déjà pré choisies (on reviendra dessus après)
- Deux Dockerfile : un pour compiler à destination d'une JVM, un pour compiler du code natif
- Notre classe, contenant un "Hello world"
- Des tests

### Bon, on la voit cette appli ?

Première étape : compiler notre code. Pour cela, se rendre dans le répertoire contenant le fichier pom.xml et lancer la commande

`./mvnw compile quarkus:dev`

Voici le résultat attendu :
```shell
Listening for transport dt_socket at address: 5005
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2020-03-19 15:18:22,147 INFO  [io.quarkus] (main) zykomot 1.0-SNAPSHOT (powered by Quarkus 1.3.0.Final) started in 0.704s. Listening on: http://0.0.0.0:8080
2020-03-19 15:18:22,150 INFO  [io.quarkus] (main) Profile dev activated. Live Coding activated.
2020-03-19 15:18:22,150 INFO  [io.quarkus] (main) Installed features: [cdi, resteasy]
```

À noter le goal "dev" qui permet de faire du rechargement à chaud. Chaque fois que l'API va être appelée, Quarkus va redémarrer l'application si le code a été changé. Comme dans notre cas, le démarrage a duré moins d'une seconde, cela n'est pas dérangeant.

> *mvnw* est un wrapper qui permet de s'assurer que Maven est bien dans une certaine version

Si vous coupez et relancez la commande, le temps de compilation devrait diminuer un petit peu.

Maintenant que l'application tourne, allons sur http://localhost:8080/

![image-20200319152530319](/home/jym/.config/Typora/typora-user-images/image-20200319152530319.png)

Cette page récapitule certaines informations, notamment comment s'en débarrasser.

Pour voir notre code métier, il faut se rendre sur http://localhost:8080/patient

Là nous voyons "hello" s'afficher. Pour comprendre d'où il sort, il faut ouvrir la classe *PatientResource.java*. On peut voir qu'elle expose un endpoint sur */patient* qui retourne *hello*.

```java
package org.zenika.zykomot;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/patient")
public class PatientResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }
}
```

Par défaut, Quarkus vient avec une extension *quarkus-resteasy* qui permet d'exposer des services REST. Nous reviendrons sur le système des extensions un peu plus tard. Quarkus étant porté en partie par Red Hat, on retrouve pas mal de projet Red Hat dans l'écosystème Quarkus.

### Accès à une base de données

Nous allons maintenant rajouté une base de données PostgreSQL afin de pouvoir stocker nos informations sur nos différents patients.

Lancer la base de données : 

```shell
docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 \
    --name postgres-zykomot -e POSTGRES_USER=zykomot \
    -e POSTGRES_PASSWORD=zykomot -e POSTGRES_DB=zykomot-bdd \
    -p 5432:5432 postgres:10.5
```

Pour faire le lien entre notre application et la base de donnée, nous avons besoin d'installer des extensions.

```shell
./mvnw quarkus:add-extension -Dextensions="jdbc-postgresql,hibernate-orm-panache,hibernate-validator,resteasy-jsonb"
```

Résultat, le pom a été modifié : 

```shell
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-hibernate-orm-panache</artifactId>
</dependency>
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-hibernate-validator</artifactId>
</dependency>
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-resteasy-jsonb</artifactId>
</dependency>
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-jdbc-postgresql</artifactId>
</dependency>
```

Des extensions ? Mais pourquoi donc ne pas utiliser mes dépendances habituelles ? Et bien tout simplement car pour optimiser le temps de démarrage de l'application, Quarkus a besoin d'optimiser certaines choses avec les frameworks utilisés. Parfois l'équipe a pu pousser des patchs sur les branches upstream des frameworks, parfois non. Et c'est donc des versions patchées qui sont utilisées.

La liste des extensions utilisées est disponible sur le site : https://quarkus.io/extensions/ . On peut également les lister grâce à `mvn quarkus:list-extensions`

Maintenant que notre pom est à jour, nous pouvons déclarer notre entité :

```java
package org.zenika.zykomot;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;

@Entity
public class Patient extends PanacheEntity {
    public String name;
    public String firstName;
}
```

Et oui, c'est tout. La magie vient de `PanacheEntity` qui fait une bonne partie du boulot pour nous.

Un peu de configuration à rajouter dans le fichier `application.properties` :

```shell
# database configuration

quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=zykomot
quarkus.datasource.password=zykomot

quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/zykomot-bdd
quarkus.datasource.jdbc.min-size=2
quarkus.datasource.jdbc.max-size=8

quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.log.sql=true
quarkus.hibernate-orm.sql-load-script=import.sql
```

Dans le fichier `import.sql`, j'ai juste rajouté des patients pour avoir des données :

```sql
INSERT INTO patient (id, name, firstName) VALUES (nextval('hibernate_sequence'), 'Michu','Robert');
INSERT INTO patient (id, name, firstName) VALUES (nextval('hibernate_sequence'), 'Dupont','Jeanne');
```

Et enfin, modifions notre controlleur pour pouvoir récupérer l'ensemble des patients :

```java
package org.zenika.zykomot;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/patient")
@Produces("application/json")
@Consumes("application/json")
public class PatientResource {

    @GET
    public List<Patient> getAll(){
        return Patient.listAll();
    }
}
```

Dans ce fichier, on précise qu'on attend et renvoie du json. La magie de Panache nous permet aussi de renvoyer facilement l'ensemble des lignes de la table.

### Insérer une donnée en base

Maintenant, essayons d'enregistrer des informations dans notre application. Pour effectuer les requêtes POST, je vais utiliser cURL, le choix des outils est libre.

Dans le fichier *PatientResource.java*, nous allons rajouter un endpoint POST

```java
@POST
@Transactional
public Response create(Patient patient){
    Patient.persist(patient);
    return Response.status(Response.Status.CREATED).entity(patient).build();
}
```

Grâce à Panache, il suffit de faire appel à la méthode *persist* pour enregistrer notre donnée.

Testons avec : 

``` shell
 curl -X POST -d  '{ "name":"Ducobu", "firstName":"Lino"}'  -H "Content-Type: application/json" http://localhost:8080/patient -v
```

Une réponse 201 doit être reçue.

On peut également se rendre sur http://localhost:8080/patient pour vérifier que notre patient a bien été ajouté.

### Les tests

Pour lancer les tests, soit depuis votre IDE favori, soit en lançant la commande `./mvnw test` . 

Normalement, si vous avez le même code que moi, ça plante \o/ Et c'est normal, vu que le code a été changé. Ce qui est intéressant de noter, c'est que pour les tests, Quarkus a lancé l'application. Et vu que dans mon cas, cela a pris environ 2 secondes, cela ne s'est pas vu. Quarkus ne va pas lancer l'application pour chaque test, donc si le premier test est un peu long, ce ne sera pas le cas pour les prochains. À noter l'annotation `@QuarkusTest` qui permet de définir un test Quarkus.

Rajoutons un test pour notre nouveau endpoint, dans le fichier *PatientResourceTest*

```java
@Test
void testCreateEndpoint() {
    	given()
            .body("{\"firstName\":\"Raoul\",\"name\":\"Ferdinand\"}")
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .when()
            .post("/patient")
            .then()
            .statusCode(CREATED.getStatusCode());
}
```

 Même chose, l'application est démarrée pour ce test et les données sont insérées dans notre base (on peut aller vérifier une fois les tests exécutés)

Quarkus utilise la notion de profiles. Il est donc possible de surcharger la configuration pour utiliser par exemple une base *in memory* pour l'exécution des tests. Pour cela, il suffit de rajouter une propriété dans *application.properties* et de la préfixer avec le nom du profile. Les profiles par défaut étant dev, test et prod.

> Il est également possible d'effectuer des tests sur la version native de l'application en utilisant l'annotation  `@NativeImageTest` (uniquement des tests en mode boite noire)

## Déployer notre application

Maintenant que nous avons une application minimale qui fonctionne, regardons comment la packager et la déployer.

### Packager notre application

#### En mode JVM

Pour cela, on utilise (sans grand suspense, je vous l'accorde) la commande `./mvnw package`. Cela va nous permettre de produire deux jars dans */target* :

- zykomot-1.0-SNAPSHOT.jar
- zykomot-1.0-SNAPSHOT-runner.jar

Le premier jar est le jar généré par Maven, de manière classique.

Le deuxième jar est un jar exécutable. Il ne contient cependant pas toutes les dépendances qui se trouvent dans *target/lib* (donc si vous déplacez ce jar, il faudra aussi déplacer le répertoire *lib* en même temps). On peut vérifier cela en lançant la commande

`java -jar zykomot-1.0-SNAPSHOT-runner.jar`

Ensuite, il nous faut construire l'image Docker avec

`docker build -f src/main/docker/Dockerfile.jvm -t quarkus/zykomot-jvm .`

À noter qu'il y a deux Dockerfile dans le répertoire *src/main/docker*. Le premier pour construire un container qui exécute l'application en mode JVM. Le deuxième permet de construire un container qui exécute l'application en mode natif, sans JVM.

Si je lance `docker images`, je vois bien mon image *quarkus/zykomot-jvm* qui vient d'être créée.

> À noter que le temps de build peut être assez long et consommateur de CPU. En effet, Quarkus doit décider quelles classes garder et lesquelles supprimer. Ce qu'on appelle la compilation AOT (Ahead Of Time)

#### En mode natif

Faisons maintenant la même chose pour l'image native. Afin de créer un exécutable natif, il faut utiliser le profil *native*.
`mvn package -Pnative`

De la même manière, il faut ensuite construire l'image Docker

`docker build -f src/main/docker/Dockerfile.native -t quarkus/zykomot .`

Quand on compare la taille des deux images générées, on peut voir une différence significative entre les deux (300MB vs 218MB)

### Création d'un cluster Kubernetes local

Pour cela, nous allons utiliser k3d, une distribution légère de Kubernetes.

Pour installer k3d, vous pouvez lancer la commande

`$ wget -q -O - https://raw.githubusercontent.com/rancher/k3d/master/install.sh | bash` 

ou bien suivre les instructions sur le site officiel https://k3s.io/ (alors oui le site c'est k3**S** et le github k3**D**, mystère à résoudre, je suis parti sur k3d comme c'est le nom de la commande )

On peut vérifier que l'installation s'est bien déroulée, `k3d -v` doit nous renvoyer la version de k3d.

Il faut ensuite créer un cluster avec `k3d create`.

> Si le port 6443 est déjà occupé, vous pouvez changer de port avec `k3d create -a 0.0.0.0:6550` 

Puis faire en sorte que kubectl pointe vers ce cluster :

`export KUBECONFIG=$(k3d get-kubeconfig)`.

Vérifions maintenant que le cluster est bien démarré :

`kubectl get pods --all-namespaces` 

> Si vous voulez redémarrer un cluster, vous pouvez commencer par lister l'ensemble des clusters de la machine `k3d list` puis démarrer le cluster avec `k3d start nom-du-cluster` 
>
> Il faut également relancer l'export de la variable d'environnement `export KUBECONFIG=$(k3d get-kubeconfig)`

```
kubectl create deployment kubernetes-zykomot --image=quarkus/zykomot:latest
```

On peut vérifier que le déploiement s'est bien déroulé avec `kubectl get deployments` ou bien `kubectl get pods`.

Il reste une dernière étape, qui est d'ouvrir le port *5432* afin que notre application puisse accéder à la base de données.

## Contraintes

Avant de se lancer dans un projet Quarkus, il faut d'abord jeter un coup d’œil aux différentes restrictions.

### L'intégration de frameworks

Quarkus supporte déjà une liste assez importante de frameworks sous forme d'extensions (https://quarkus.io/extensions/), cependant, il n'est pas dit que le framework que vous souhaitez utiliser soit compatible avec la manière de compiler de SubstrateVM.

### La réflexion

La réflexion au runtime n'est pas possible avec la compilation Ahead Of Time car SubstrateVM ne peut pas anticiper quelles classes charger. Il est possible de contourner cela en rajoutant dans un fichier json la liste des classes qu'on souhaite

### Le chargement dynamique de classe

Non supporté (pareil, contrainte liée au type de compilation)

### Autres

Le détail exposé dans cette page https://github.com/oracle/graal/blob/master/substratevm/LIMITATIONS.md
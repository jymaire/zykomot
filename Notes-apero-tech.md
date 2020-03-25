# Notes pour Apéro tech

## Pré requis

- un JDK 8 ou 11
- Maven 3.6.2 ou plus
- Idéalement GraalVM pour la partie compilation native

Installer native image pour pouvoir générer l'image native (si pas fait en avance, un message dans Maven demandera de l'installer en indiquant la commande)

```
gu install native-image
```

## Commandes

Bootstrap appli

```shell
mvn io.quarkus:quarkus-maven-plugin:1.3.0.Final:create \
    -DprojectGroupId=com.zenika \
    -DprojectArtifactId=quarkus-apero \
    -DclassName="com.zenika.quarkus.GreetingResource" \
    -Dpath="/hello"
```

ou bien https://code.quarkus.io/

Compiler

```shell
mvn compile quarkus:dev
```

Tester

```
mvn test
```

URL appli

http://localhost:8080/hello

URL aide

http://localhost:8080/

Installer des dépendances

```shell
mvn quarkus:list-extensions
```

```
mvn quarkus:add-extension -Dextensions="jdbc-postgresql,hibernate-orm-panache,hibernate-validator,resteasy-jsonb"
```

Packager en natif

```
mvn package -Pnative
```

Lancer l'image native

```
cd target
./quarkus-apero-1.0-SNAPSHOT-runner
```

Package en mode JVM

```
mvn package
```

Lancer le jar

```
cd target
java -jar quarkus-apero-1.0-SNAPSHOT.jar 
```





## Liens utiles

https://quarkus.io/

https://code.quarkus.io/

https://quarkus.io/extensions/

https://quarkus.io/guides/building-native-image

https://quarkus.io/quarkus-workshops/super-heroes/

https://www.graalvm.org/


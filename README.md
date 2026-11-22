# value-object

Small, immutable, self-validating value objects for Java (25+).

```xml
<dependency>
    <groupId>io.joshuasalcedo</groupId>
    <artifactId>value-object</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Build

```sh
mvn verify      # compile, sources and Javadoc jars
mvn install     # into ~/.m2
```

## Release

Push a tag `vX.Y.Z` (or run the **Release** workflow by hand with a version). The workflow sets the
version, signs every artifact and publishes to Maven Central.

Repository secrets it needs:

| Secret | What |
|---|---|
| `MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD` | a Central Portal user token |
| `SIGNING_KEY` | an ASCII-armoured private PGP key |
| `SIGNING_KEY_PASSWORD` | its passphrase |

## License

Apache License 2.0.

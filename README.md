# Glass - Example
A basic shard that demonstrates **optional implementations**, and **interfaces**.

# Basic Information
A lot of stuff will be more complex than other mod loaders, but it should be make easier in the future. Also keep in mind that glass is in design more complex, so there will always be a little more complex.

**impl directory(s):** Used for optional implementations (in short, sections of code that are only loaded / ran if their dependencies have already been loaded). In this example they are used for applying the shard to multiple versions.

# Setup
Instructions should be relatively clear, but don't be afraid to ask because there is very likely to be something missing.

## Downloading
Download / Clone the github repository to get the contents locally.

## Running

### For IntelliJ IDEA Users:

Run

`./gradlew genRunConfiguration -Pconfiguration=idea,{client/server},{version}`

and a run configuration should be created (you will have to go into run configurations and select it)

For example, to generate a run configuration for a 1.8.9 client:

`./gradlew genRunConfiguration -Pconfiguration=idea,client,1.8.9`

### Everyone Else:
Kiln does not yet support automatically creating run configurations for any other IDEs, so you will have to create one manually.

- **Classpath:** {project-name}
- **JVM Arguments:** see [jvm arguments](#jvm-arguments)
- **Main Class:** com.github.glassmc.loader.client.GlassClientMain
- **Program Arguments:** see [program arguments](#program-arguments)
- **Working Directory:** run

#### JVM Arguments
To get the proper jvm arguments, run

`./gradlew getRunConfiguration -Pconfiguration={ide},{client/server},{version}`

For example, to get the correct arguments for running a 1.8.9 client

`./gradlew getRunConfiguration -Pconfiguration=client,1.8.9`

You will see a long string printed into the terminal, copy that and add it to your jvm arguments.

#### Program Arguments
Most versions will work with supplying

`--accessToken 0 --version {version}`

(for offline mode)

but some versions (at least 1.7.10) require also adding

`--userProperties {}`
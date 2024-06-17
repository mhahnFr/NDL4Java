# Welcome to *NDL4Java*!
The **N**ative **D**ark mode **L**istener **for Java** is a wrapper library for the [NDL project][1]. It enables Java
applications to query whether the user interface of the operating system uses a dark theme.

## Usage
Use this library by integrating this library as dependency in your Java based application.

Specify the path to the native NDL library when launching the JVM: `-Djava.library.path=<path/to/NDL>`

To enable warning-free access to the restricted JVM methods that are required, specify additionally:
`--enable-native-access=ALL-UNNAMED`

Make sure you have installed a Java Development Kit in version 22 or higher to use this library.

### Installation
The following sections show how to use NDL4Java as dependency using the build system Gradle.

#### GitHub Packages
To use NDL4Java as dependency from GitHub Packages, follow the instructions [here][3].

Add the following to your Gradle build script:
```groovy
repositories {
    maven {
        url = uri('https://maven.pkg.github.com/mhahnfr/ndl4java')
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.token") ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation 'mhahnfr:ndl4java:0.2'
}
```

#### Release
The [releases][4] of this library can also be used as dependency in your Gradle build script:
```groovy
repositories {
    ivy {
        url 'https://github.com/'
        patternLayout {
            artifact '/[organisation]/[module]/releases/download/v[revision]/[module]-[revision].jar'
        }
        metadataSources { artifact() }
    }
}

dependencies {
    implementation 'mhahnfr:ndl4java:0.2'
}
```

#### Build from source
To build a Java archive file (`.jar`) that can be included in other projects, simply execute the `jar` task:
```shell
./gradlew jar
```
Running the above command successfully generates the file `build/libs/NDL4Java-<version>.jar`.

### API
NDL4Java exposes the following classes:
- **NDL**: The wrapped functions from the [NDL project][1].
- **DarkModeCallback**: An interface for registering objects as dark mode listeners.
- **NDLException**: The exception class of this library.

The class **NDL** exposes the following static methods:

| Signature                                      | Description                                      |
|------------------------------------------------|--------------------------------------------------|
| `boolean queryDarkMode()`                      | Queries whether the system UI uses a dark theme. |
| `void registerCallback(DarkModeCallback cb)`   | Registers the given callback.                    |
| `void deregisterCallback(DarkModeCallback cb)` | Unregisters the given callback.                  |

Since NDL needs access to the native application, it is recommended to not call `queryDarkMode()` before the
`java.awt.EventQueue` has launched.

> [!WARNING]
> If the method is called before the JVM has initialized the native application, the JVM might crash.

Dark mode callbacks can be registered and unregistered anywhere at any time.

> [!NOTE]
> On macOS, make sure to set the Java property `apple.awt.application.appearance` to `system`, e.g.
> `System.setProperty("apple.awt.application.appearance", "system");` for incorporating system theme changes into your
> Java application.
> 
> Make sure to set this property before the `java.awt.EventQueue` has launched for it to have an effect.

### Example
```java
// Main.java

import mhahnFr.NDL.NDL;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        System.setProperty("apple.awt.application.appearance", "system"); // Needed on macOS

        NDL.registerCallback(() -> System.out.println("Callback: Dark: " + NDL.queryDarkMode()));

        EventQueue.invokeLater(() -> {
            final var frame = new JFrame("NDL4Java: Example");
            final var label = new JLabel("Dark: " + NDL.queryDarkMode());
            NDL.registerCallback(() -> label.setText("Dark: " + NDL.queryDarkMode()));
            frame.getContentPane().add(label);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
```
Compile as follows:
```shell
javac -cp <path/to/NDL4Java>/NDL4Java-0.2.jar Main.java
```
And run as follows:
```shell
java -cp .:<path/to/NDL4Java>/NDL4Java-0.2.jar -Djava.library.path=<path/to/NDL> --enable-native-access=ALL-UNNAMED Main
```


## Final notes
This repository is licensed under the terms of the GNU LGPL version 3 or later.

© Copyright 2024 [mhahnFr][2]

[1]: https://github.com/mhahnFr/NDL
[2]: https://github.com/mhahnFr
[3]: https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package
[4]: https://github.com/mhahnFr/NDL4Java/releases/latest
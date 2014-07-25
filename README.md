KraftRPG
========

Implementation of the KraftRPG-API, essentially, an RPG plugin built for Bukkit.

Supports the following server mods:
* CraftBukkit [http://bukkit.org](http://bukkit.org)
* Spigot [http://spigotmc.org](http://spigotmc.org)
* Tweakkit [https://afterkraft.com](https://afterkraft.com)

Website: [https://afterkraft.com/forum/forums/kraftrpg/](https://afterkraft.com/forum/forums/kraftrpg/)
Bugs/Suggestions: [http://git.afterkraft.com/afterkraft/kraftrpg/issues](http://git.afterkraft.com/afterkraft/kraftrpg/issues)

Compilation
-----------

We use maven to handle our dependencies and compatibility modules.

* Install [Maven 3](http://maven.apache.org/download.html)
* Check out this repo and: `mvn clean install`

Importing to Eclipse
--------------------

Due to m2e and Eclipse's flat project structure requirement, KraftRPG does not easily import into Eclipse.

To avoid headaches, simply create a new Eclipse project from KraftRPG's sources and note the following:

* Install [Maven 3](http://maven.apache.org/download.html)
* In command line [GitBash for Windows](http://msysgit.github.io) or Terminal for Mac/Linux, navigate to the project directory and type `mvn clean package`

Maven will have downloaded the dependencies for KraftRPG and you can manually add these dependencies into the Eclipse Project Classpath.


Importing to IntelliJ
---------------------
Checkout from repo and import existing module by selecting the pom.xml.

IntelliJ easily handles the KraftRPG maven project structure, except the compatibility modules, easily can be fixed by hovering over the imports and 'Add xxx to classpath' will be available.


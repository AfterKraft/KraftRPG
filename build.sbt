import sbt.Keys._

name := "KraftRPG"

version := "1.0"

organization := "com.afterkraft"

scalaVersion := "2.11.6"

libraryDependencies += "org.spongepowered" % "spongeapi" % "2.1-SNAPSHOT"

libraryDependencies += "com.afterkraft.kraftrpg" % "KraftRPG-API" % "0.0.5-SNAPSHOT"

resolvers += "SpongePowered" at "http://repo.spongepowered.org/maven"

resolvers += "AfterKraft" at "http://nexus.afterkraft.com/content/groups/public/"

resolvers += Resolver.sonatypeRepo("snapshots")

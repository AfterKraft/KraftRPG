/*
 * Copyright 2014 Gabriel Harris-Rouquette
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterkraft.kraftrpg.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public enum RPGPluginVersion {

    DEFAULT(0, 0, "1.7.9", "1.0.0", "1.0.0"),

    RPG_1_0_0_R1(0, 0, "1.7.9", "1.0.0", "1.0.0");

    public static final RPGPluginVersion CURRENT = RPGPluginVersion.RPG_1_0_0_R1;

    private final int revision;

    private final long timestamp;

    private final String minecraftVersion;

    private final String loaderVersion;

    private final Set<String> supportedVersions = new HashSet<String>();

    private RPGPluginVersion(int revision, long timestamp, String minecraftVersion, String loaderVersion, String... supportedVersions) {
        this.revision = revision;
        this.timestamp = timestamp;
        this.minecraftVersion = minecraftVersion;
        this.loaderVersion = loaderVersion;

        Collections.addAll(this.supportedVersions, supportedVersions);
    }

    public static RPGPluginVersion getVersionFromRevision(int revision) {
        for (RPGPluginVersion version : RPGPluginVersion.values()) {
            if (version.getLoaderRevision() == revision) {
                return version;
            }
        }

        return RPGPluginVersion.DEFAULT;
    }

    public int getLoaderRevision() {
        return this.revision;
    }

    public static int getRevisionFromVersion(String versionString) {
        for (RPGPluginVersion version : RPGPluginVersion.values()) {
            if (version.getLoaderVersion().equals(versionString)) {
                return version.getLoaderRevision();
            }
        }

        return RPGPluginVersion.DEFAULT.getLoaderRevision();
    }

    public String getLoaderVersion() {
        return this.loaderVersion;
    }

    public long getReleaseTimestamp() {
        return this.timestamp;
    }

    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }

    public boolean isVersionSupported(String version) {
        return this.supportedVersions.contains(version);
    }

    @Override
    public String toString() {
        return this == RPGPluginVersion.DEFAULT ? "Unknown" : this.loaderVersion;
    }
}

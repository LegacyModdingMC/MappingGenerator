# MappingGenerator

This Gradle plugin is an addon for [RetroFuturaGradle](https://github.com/GTNewHorizons/RetroFuturaGradle) that generates extra method parameter names by layering multiple sources.

## How good is it?

With the default list of sources:

```
Parameter coverage: 1884 -> 7675 / 11903 (64.47954297235991%)
```

## Usage

First, add the plugin:

```gradle
plugins {
    id 'io.github.legacymoddingmc.mappinggenerator' version '0.1'
}
```

Since we're using jitpack you also have to merge this into `settings.gradle`, sorry

```gradle
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if(requested.id.toString() == "io.github.legacymoddingmc.mappinggenerator")
                useModule("com.github.legacymoddingmc:mappinggenerator:" + requested.version)
        }
    }
    repositories {
        maven{
            url = "https://jitpack.io"
        }
    }
}
```

People using [GTNH's ExampleMod](https://github.com/GTNewHorizons/ExampleMod1.7.10) may be disgruntled by the idea of editing `build.gradle`. To those people, I recommend merging [extras/auto-patch-examplemod.gradle](extras/auto-patch-examplemod.gradle) into `addon.gradle`. It will automatically add the plugin declaration line after every run of `updateBuildScript`.

You can also `apply` it:
```gradle
ext.mappingGeneratorVersion = "0.1"
apply from: 'https://raw.githubusercontent.com/LegacyModdingMC/MappingGenerator/master/extras/auto-patch-examplemod.gradle'
```

### Configuration

The plugin will layer names from sources defined in `mappingGenerator.sources`. The default list can be found [here](src/main/java/io/github/legacymoddingmc/mappinggenerator/DefaultSources.java).

Sources are applied in sequential order, meaning names originating from later sources overwrite names originating from earlier ones. Additionally, MCP names will never be overwritten - MappingGenerator is purely additive.

The source list can be configured using the `mappingGenerator.sources` property, which returns a list of source specs (`List<List<String>>`). To add sources before and after the defaults, you can use this example:

```gradle
mappingGenerator.sources =
        [
            ["some", "low", "priority", "source"],
        ] + mappingGenerator.sources.get() + [
            ["some", "high", "priority", "source"],
        ]
```

## Mapping sources

### MCP

Extracts names from MCP mappings.

**Specification:** `"mcp", "<game_version>", "<mapping_version>", "<kind>"`

* `kind` can be one of the following
    * `methodComments`: Extract parameter names from the comments in `methods.csv`
    * `parameters`: Extract parameter names from `parameters.csv`

#### Examples
* `"mcp", "1.7.10", "stable_12", "methodComments"`
* `"mcp", "1.8.9", "stable_22", "parameters"`

### Yarn

Extracts names from Legacy Yarn mappings.

**Specification:** `"yarn", "<mapping_version>"`

* `mapping_version` can be set to `<game_version>+latest` to automatically use the latest one.

#### Examples
* `"yarn", "1.7.10+latest"`
* `"yarn", "1.8.9+build.458"`

### CSV

Extracts names from a custom `parameters.csv`. Only the first two columns are parsed, the `side` column can be left out.

**Specification:** `"csv", "<url>"`

#### Examples
* `"csv", "https://github.com/makamys/MCModdingMisc/blob/6d3292c76cfa4f54089443e1e761df0ce2de586a/test.csv"`

## License

The plugin is licensed under the [MIT License](LICENSE).

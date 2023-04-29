```gradle
mappingGenerator.sources =
        [
            ["some", "low", "priority", "source"],
        ] + mappingGenerator.sources.get() + [
            ["some", "high", "priority", "source"],
        ]
```

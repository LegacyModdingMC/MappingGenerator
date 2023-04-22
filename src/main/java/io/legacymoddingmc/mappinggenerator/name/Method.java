package io.legacymoddingmc.mappinggenerator.name;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Method implements Name {
    private String klass;
    private String method;
    private String desc;
}

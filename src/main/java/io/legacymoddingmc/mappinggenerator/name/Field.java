package io.legacymoddingmc.mappinggenerator.name;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.annotation.Nullable;

@Data
@AllArgsConstructor
public class Field {
    private String klass;
    private String field;
    @Nullable
    private String desc;
}

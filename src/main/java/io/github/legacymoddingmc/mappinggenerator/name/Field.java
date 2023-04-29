package io.github.legacymoddingmc.mappinggenerator.name;

import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.lang3.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.annotation.Nullable;

@Data
@AllArgsConstructor
public class Field implements Name {
    private String klass;
    private String field;
    @Nullable
    private String desc;

    @Override
    public String toString() {
        return StringUtils.defaultString(klass, "?") + " "
                + StringUtils.defaultString(field, "?") + " "
                + StringUtils.defaultString(desc, "?");
    }
}

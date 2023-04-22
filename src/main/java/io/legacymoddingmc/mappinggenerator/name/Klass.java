package io.legacymoddingmc.mappinggenerator.name;

import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.lang3.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * :]
 */
@Data
@AllArgsConstructor
public class Klass implements Name {
    private String klass;

    @Override
    public String toString() {
        return StringUtils.defaultString(klass, "?");
    }
}

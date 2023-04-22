package io.legacymoddingmc.mappinggenerator.name;

import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.lang3.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Method implements Name {
    private String klass;
    private String method;
    private String desc;

    @Override
    public String toString() {
        return StringUtils.defaultString(klass, "?") + " "
                + StringUtils.defaultString(method, "?") + " "
                + StringUtils.defaultString(desc, "?");
    }
}

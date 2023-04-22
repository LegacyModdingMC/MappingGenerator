package io.legacymoddingmc.mappinggenerator.name;

import com.gtnewhorizons.retrofuturagradle.shadow.org.apache.commons.lang3.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Parameter implements Name {
    private String klass;
    private String method;
    private String desc;
    int index;
    private String parameter;

    public Parameter(Method method, int index, String parameter) {
        this.klass = method.getKlass();
        this.method = method.getMethod();
        this.desc = method.getDesc();
        this.index = index;
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return StringUtils.defaultString(klass, "?") + " "
                + StringUtils.defaultString(method, "?") + " "
                + StringUtils.defaultString(desc, "?") + " "
                + "#" + index + " "
                + StringUtils.defaultString(parameter, "?");
    }
}

package io.legacymoddingmc.mappinggenerator.name;

import com.gtnewhorizons.retrofuturagradle.shadow.com.google.common.base.Preconditions;

public class NameHelper {
    public static Name create(Class<?> klass, String... params) {
        if(klass == Klass.class) {
            Preconditions.checkArgument(params.length == 1);
            return new Klass(params[0]);
        } else if(klass == Field.class) {
            Preconditions.checkArgument(params.length == 3);
            return new Field(params[0], params[1], params[2]);
        } else if(klass == Method.class) {
            Preconditions.checkArgument(params.length == 3);
            return new Method(params[0], params[1], params[2]);
        } else if(klass == Parameter.class) {
            Preconditions.checkArgument(params.length == 5);
            return new Parameter(params[0], params[1], params[2], Integer.parseInt(params[3]), params[4]);
        } else {
            throw new IllegalStateException("" + klass);
        }
    }
}

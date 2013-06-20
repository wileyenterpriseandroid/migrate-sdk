package com.migrate.tools;

public class MalformedSchemaDeclarationException extends Exception {
    public MalformedSchemaDeclarationException(String mesg) {
        super(mesg);
    }

    public MalformedSchemaDeclarationException(String mesg, Throwable cause) {
        super(mesg, cause);
    }
}

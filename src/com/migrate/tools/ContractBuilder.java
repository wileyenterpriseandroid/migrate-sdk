package com.migrate.tools;

public class ContractBuilder {
    private static final String CONTRACT = "Contract";
    private static final String COLUMNS = "Columns";
    private static final String INDENT = "    ";

    private StringBuilder contractBuilder;
    private static String LS = System.getProperty("line.separator");
    private String contractClassName;
    private String packageName;

    public ContractBuilder() {
        contractBuilder = new StringBuilder();
    }

    public void start(Class cl, String version) {
        String qualifiedName = cl.getName();
        int lastDotIndex = qualifiedName.lastIndexOf(".");
        int nameStart = (lastDotIndex < 0 ? 0 : lastDotIndex + 1);
        String unqualifiedName = qualifiedName.substring(nameStart);
        String upperUnqualifiedName = qualifiedName.substring(nameStart).toUpperCase();
        packageName = qualifiedName.substring(0, (lastDotIndex > 0 ? lastDotIndex : 0));

        contractBuilder
                .append("/* Generated Source Code - Do not Edit! */")
                .append(LS)
                .append("package ").append(packageName).append(";")
                .append(LS)
                .append(LS)
                .append("import android.net.Uri;")
                .append(LS)
                .append("import android.provider.BaseColumns;")
                .append(LS)
                .append("import net.migrate.api.Webdata;")
                .append(LS)
//                .append("import ").append(qualifiedName).append(';')
//                .append(LS)
                .append(LS);

        contractClassName = unqualifiedName + CONTRACT;
        String columnsName = unqualifiedName + COLUMNS;
        contractBuilder
                .append("public final class ").append(contractClassName).append(" {")
                .append(LS)
                .append(INDENT).append("private ").append(contractClassName).append("() {}")
                .append(LS)
                .append(LS)
                .append(INDENT).append("public static final String SCHEMA_ID = ").append(unqualifiedName).append(".class.getName();")
                .append(LS)
                .append(LS)
                .append(INDENT).append("public static final Uri SCHEMA_").append(upperUnqualifiedName).append("_URI = Webdata.Schema.schemaUri(SCHEMA_ID);")
                .append(LS)
                .append(INDENT).append("public static final Uri OBJECT_").append(upperUnqualifiedName).append("_URI = Webdata.Object.objectUri(SCHEMA_ID);")
                .append(LS)
                .append(LS)
                .append(INDENT).append("public static final class ").append(columnsName).append(" implements BaseColumns {")
                .append(LS)
                .append(INDENT).append(INDENT).append("private ").append(columnsName).append("() {}")
                .append(LS)
                .append(LS);
    }

    public void addProperty(String propertyName) {
        String constantName = camel2Constant(propertyName);
        contractBuilder
                .append(INDENT).append(INDENT).append("public static final String ").append(constantName).append(" = \"").append(propertyName).append("\";").append(LS);
    }

    public void end() {
        contractBuilder
                .append(INDENT).append('}')
                .append(LS)
                .append('}')
                .append(LS);
    }

    public String build() {
        return contractBuilder.toString();
    }

    public static final String camel2Constant(String name) {
        char[] nameChars = name.toCharArray();
        int wordStart = 0;
        StringBuilder underscoresBuilder = new StringBuilder();
        // just leave cases like nameE alone
        for (int i = 0; i < (nameChars.length - 1); i++) {
            int next = i + 1;
            if (Character.isLowerCase(nameChars[i]) && Character.isUpperCase(nameChars[next])) {
                String word = new String(nameChars, wordStart, (next-wordStart)).toUpperCase();
                underscoresBuilder.append(word).append('_');
                wordStart = next;
            }
        }
        // add the last
        String lastWord = new String(nameChars, wordStart, nameChars.length - wordStart).toUpperCase();
        underscoresBuilder.append(lastWord);
        return underscoresBuilder.toString();
    }

    public String getContractClassName() {
        return contractClassName;
    }

    public String getPackageName() {
        return packageName;
    }
}

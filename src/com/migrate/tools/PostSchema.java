package com.migrate.tools;

import com.migrate.webdata.model.PersistentSchema;
import com.migrate.webdata.model.PropertyIndex;
import net.migrate.api.WebData;
import net.migrate.api.annotations.WebDataSchema;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Takes a jar and a class, then generates json-schema, and puts it in a WebData
 * service instance.  The class should implement APISource and return an array
 * containing the APIs for which this tool will put schema.
 */
public class PostSchema {
    protected static org.apache.log4j.Logger log = Logger.getLogger(PostSchema.class);
    protected static RestTemplate restTemplate = new RestTemplate();

    private static final String CP = "cp";
    private static final String DEST = "d";
    private static final String MIGRATE_URL = "migrateurl";
    private static final String API_CLASSES = "api";
    private static final String OUTPUT_DIR = "api";

    // TODO: constants that should live somewhere else
    private static final String TYPE = "type";
    private static final String OBJECT = "object";
    private static final String SCHEMA_ID = "id";
    private static final String SCHEMA_NAMESPACE = "__schema";

    public static final String GET = "get";

    public static final String STRING = "string";
    public static final String NUMBER = "number";
    public static final String INTEGER = "integer";

    public static final String UNIQUE = "unique";
    public static final String INDEX = "index";

    private static String SCHEMA_TYPE = "/schema/{schemaId}";

    public static void main(String[] args) throws Exception {
        Map<String, String> argMap = parseArgs(args);

        String cp = argMap.get(CP);
        String destDirectory = argMap.get(DEST);
        String migrateURL = argMap.get(MIGRATE_URL);
        String apis = argMap.get(API_CLASSES);

        if ((cp == null) || (apis == null)) {
            usage();
            // does not happen
            return;
        }

        if ((destDirectory == null) && (migrateURL == null)) {
            usage();
            // does not happen
            return;
        }

        String[] classes = cp.split(String.valueOf(File.pathSeparatorChar));
        ArrayList<URL> jarUrls = new ArrayList<URL>();
        for (String elt: classes) {
            URL jarURL = new File(elt).toURL();
            jarUrls.add(jarURL);
        }

        URL[] urls = jarUrls.toArray(new URL[] {});

        URLClassLoader apiLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());

        postSchemasAndBuildContracts(apis, apiLoader, migrateURL, destDirectory);
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> argMap = new HashMap<String, String>();

        Iterator<String> argIter = Arrays.asList(args).iterator();
        while (argIter.hasNext()) {
            String arg = argIter.next().toLowerCase();
            if (arg.startsWith("-")) {
                if (argIter.hasNext()) {
                    String value = argIter.next();
                    // strip the '-'
                    argMap.put(arg.substring(1), value);
                } else {
                    usage();
                }
            }
        }
        return argMap;
    }
    private static void postSchemasAndBuildContracts(String api, URLClassLoader apiLoader,
                                                     String migrateURL, String destDirectory)
            throws ClassNotFoundException, IOException, MalformedSchemaDeclarationException
    {
        String[] classElts = api.split(",");
        for (String clName : classElts) {
            clName = clName.trim();
            if (!"".equals(clName.trim())) {
                Class cl = Class.forName(clName, true, apiLoader);
                PersistentSchema schema = readSchemaAndWriteContractAPI(cl, destDirectory);
                if (migrateURL != null) {
                    postSchema(schema, cl, migrateURL);
                }
            }
        }
    }

    private static PersistentSchema readSchemaAndWriteContractAPI(Class apiClass, String destDirectory)
            throws IOException, MalformedSchemaDeclarationException
    {
        Map<String, Object> jsonSchema = new HashMap<String, Object>();
        jsonSchema.put(TYPE, OBJECT);
        String schemaClassName = apiClass.getName();
        //jsonSchema.put(SCHEMA_ID, schemaClassName);

        ContractBuilder contractBuilder = new ContractBuilder();
        String version = readVersion(apiClass, contractBuilder);
        version = "2";

        PersistentSchema persistentSchema = new PersistentSchema();
        persistentSchema.setWd_id(schemaClassName);
        persistentSchema.setWd_version(Long.valueOf(version));
        persistentSchema.setWd_updateTime(System.currentTimeMillis());
        persistentSchema.setWd_classname(PersistentSchema.class.getName());
        persistentSchema.setWd_namespace(SCHEMA_NAMESPACE);

        Map<String, Object> properties = getProperties(apiClass, contractBuilder);

        // These fields are required for the basic operation of WebData synchronization
        addProperty(properties, WebData.Object.WD_DATA_ID, "string", true); // this is a data uuid
        addProperty(properties, WebData.Schema.WD_VERSION, "integer", true);
        addProperty(properties, WebData.Object.WD_DELETED, "integer", true);
        addProperty(properties, WebData.Schema.WD_UPDATETIME, "long", true);
        addProperty(properties, WebData.Schema.WD_NAMESPACE, "string");
        addProperty(properties, WebData.Schema.WD_CLASSNAME, "string");

        jsonSchema.put(WebData.Schema.JS_PROPERTIES, properties);

        persistentSchema.setJsonSchema(jsonSchema);

        contractBuilder.end();

        if ((destDirectory != null) && (!"".equals(destDirectory))) {
            writeContractSource(destDirectory, contractBuilder);
        }

        // do this with annotations
        List<PropertyIndex> indexList = creatIndexList();
        persistentSchema.setIndexList(indexList);
//        System.out.println(new String(JsonHelper.writeValueAsByte(persistentSchema)));
        return persistentSchema;
    }

    private static void addProperty(Map<String, Object> schema, String property, String type) {
        addProperty(schema, property, type, false );
    }

    private static void addProperty(Map<String, Object> schema, String property, String type, boolean required) {
        Map<String, Object> typeMap = new HashMap<String, Object>();
        typeMap.put("type", type);
        if (required ) {
            typeMap.put("required", true);
        }
        schema.put(property, typeMap);
    }

    private static void writeContractSource(String destDirectory, ContractBuilder contractBuilder) throws IOException {

        File destFile = new File(destDirectory);
        assertDir(destFile);

        String packageName = contractBuilder.getPackageName();
        String packageDirs = packageName.replace('.', File.separatorChar);
        File finalPackageDir = new File(destFile, packageDirs);
        if (!finalPackageDir.exists() && !finalPackageDir.mkdirs()) {
            throw new IOException("Unable to create directories for contract class.");
        } else if (!finalPackageDir.isDirectory()) {
            throw new IOException("Package dir exists, but is not a directory: " + finalPackageDir);
        }
        FileOutputStream fout = new FileOutputStream(finalPackageDir +
                File.separator + contractBuilder.getContractClassName() + ".java");
//        System.out.println(new String(contractBuilder.build().getBytes()));
        fout.write(contractBuilder.build().getBytes());
        fout.flush();
        fout.close();
    }

    private static void assertDir(File dir) throws IOException {
        if (!dir.exists()) {
            throw new IOException("No such file: " + dir);
        } else if (!dir.isDirectory()) {
            throw new IOException("File is not a directory: " + dir);
        }
    }

    private static String readVersion(Class apiClass, ContractBuilder apiBuilder)
            throws MalformedSchemaDeclarationException
    {
        WebDataSchema schemaAnnotation = (WebDataSchema)
                apiClass.getAnnotation(WebDataSchema.class);
        if (schemaAnnotation == null) {
            throw new MalformedSchemaDeclarationException("Missing annotation: " +
                    WebDataSchema.class.getName());
        }
        String version = schemaAnnotation.version();
        if (version == null) {
            throw new MalformedSchemaDeclarationException("Missing schema version");
        }

        apiBuilder.start(apiClass, version);

        return version;
    }

    private static List<PropertyIndex> creatIndexList() {
        return null;
    }

    public static Map<String, Object> getProperties(Class apiClass, ContractBuilder apiBuilder) {

        Map<String, Object> properties = new HashMap<String, Object>();
        Method[] methods = apiClass.getDeclaredMethods();
        for (Method m : methods) {
            String mName = m.getName();
            String propertyName = propertyName(mName);

            if (propertyName != null) {

                Map<String, String> typeMap = new HashMap<String, String>();

                Class returnType = m.getReturnType();

                if (String.class.isAssignableFrom(returnType)) {
                    typeMap.put(TYPE, "string");
                } else if (int.class.isAssignableFrom(returnType)) {
                    typeMap.put(TYPE, "integer");
                } else if (Integer.class.isAssignableFrom(returnType)) {
                    typeMap.put(TYPE, "integer");
                } else if (long.class.isAssignableFrom(returnType)) {
                    typeMap.put(TYPE, "integer");
                } else if (Long.class.isAssignableFrom(returnType)) {
                    typeMap.put(TYPE, "integer");
                } else if (float.class.isAssignableFrom(returnType)) {
                    typeMap.put(TYPE, "number");
                } else if (Float.class.isAssignableFrom(returnType)) {
                    typeMap.put(TYPE, "number");
                } else if (double.class.isAssignableFrom(returnType)) {
                    typeMap.put(TYPE, "number");
                } else if (Double.class.isAssignableFrom(returnType)) {
                    typeMap.put(TYPE, "number");
                } else {
                    throw new IllegalArgumentException("Unsupported property type: " +
                            propertyName + ": " + returnType.getName());
                }

//                Unique unique = m.getAnnotation(Unique.class);
//                if (unique != null) {
//                    typeMap.put(UNIQUE, "true");
//                }
//
//                Index index = m.getAnnotation(Index.class);
//                if (unique != null) {
//                    String indexType = index.type();
//                    typeMap.put(INDEX, indexType);
//                }

                properties.put(propertyName, typeMap);

                apiBuilder.addProperty(propertyName);
            }
        }
        return properties;
    }

    private static String propertyName(String mName) {
        if (mName.startsWith(GET)) {
            String name = mName.substring(GET.length());
            return String.valueOf(name.charAt(0)).toLowerCase() + name.substring(1);
        }
        return null;
    }

    private static void postSchema(PersistentSchema persistentSchema, Class apiClass, String dest)
            throws IOException, MalformedSchemaDeclarationException
    {
        HttpHeaders header = new HttpHeaders();
        header.add("content-type", "application/json");

        // TODO: the slash must be present or the schema name is truncated to only the package...
        String migrateURL = dest + SCHEMA_TYPE + "/";
//        System.out.println(" migrateURL: " + migrateURL);
        HttpEntity<PersistentSchema> requestEntity =
                new HttpEntity<PersistentSchema>(persistentSchema, header);

        String schemaId = apiClass.getName();

        ResponseEntity<String> response = restTemplate.exchange(migrateURL, HttpMethod.POST,
                requestEntity, String.class, schemaId);
        log.info(response.getBody());
    }

    private static void usage() {
        System.err.println("");
        System.exit(1);
    }
}

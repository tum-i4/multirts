package edu.tum.sse.multirts.ext;

/**
 * Configuration for MultiRTS Maven Extension.
 */
public final class ExtensionConfiguration {
    public static final String POM_XML = "pom.xml";
    public static final String TRIGGER_ALL_MODULES = "*";
    public static final String PARAMETER_TRANSITIVE_MODULES = "multirts.transitiveModules";
    public static final String PARAMETER_UPSTREAM_MODULES = "multirts.upstreamModules";
    public static final String PARAMETER_DOWNSTREAM_MODULES = "multirts.downstreamModules";
    public static final String PARAMETER_FILTER_EXECUTION = "multirts.filterExecution";
    public static final String PARAMETER_OUTPUT_FILE = "multirts.moduleOutput";
    public static final String FILE_PATH_DELIMITER = ",";
}

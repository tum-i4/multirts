package edu.tum.sse.multirts.ext;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static edu.tum.sse.multirts.ext.ExtensionConfiguration.*;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "Selects reactor modules from files.")
public class MultiRTSLifecycleParticipant extends AbstractMavenLifecycleParticipant {

    private static final Logger logger = Logger.getLogger(MultiRTSLifecycleParticipant.class.getName());

    private static List<Path> getValidFilePathsFromString(String filePathString) {
        return Arrays.stream(filePathString.split(FILE_PATH_DELIMITER)).map(filePath -> Paths.get(filePath).toAbsolutePath()).filter(Files::exists).collect(Collectors.toList());
    }

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        Properties userProperties = session.getRequest().getUserProperties();
        ModuleSelector moduleSelector = new ModuleSelector(session);

        try {
            if (userProperties.containsKey(PARAMETER_TRANSITIVE_MODULES)) {
                List<Path> filePaths = getValidFilePathsFromString(userProperties.getProperty(PARAMETER_TRANSITIVE_MODULES));
                List<Path> modulePaths = getModulePathsFromFiles(filePaths, session);
                moduleSelector.selectTransitiveProjects(modulePaths);
            }
            if (userProperties.containsKey(PARAMETER_UPSTREAM_MODULES)) {
                List<Path> filePaths = getValidFilePathsFromString(userProperties.getProperty(PARAMETER_UPSTREAM_MODULES));
                List<Path> modulePaths = getModulePathsFromFiles(filePaths, session);
                moduleSelector.selectUpstreamProjects(modulePaths);
            }
            if (userProperties.containsKey(PARAMETER_DOWNSTREAM_MODULES)) {
                List<Path> filePaths = getValidFilePathsFromString(userProperties.getProperty(PARAMETER_DOWNSTREAM_MODULES));
                List<Path> modulePaths = getModulePathsFromFiles(filePaths, session);
                moduleSelector.selectDownstreamModules(modulePaths);
            }
            logger.info(String.format("Selected module(s) (%d) after MultiRTS module selection:", moduleSelector.getSelectedProjects().size()));
            printSelectedModules(moduleSelector.getSelectedProjects());
            if (userProperties.containsKey(PARAMETER_OUTPUT_FILE)) {
                Path outputFile = Paths.get(userProperties.getProperty(PARAMETER_OUTPUT_FILE)).toAbsolutePath();
                writeSelectedModulesToOutput(moduleSelector.getSelectedProjects(), outputFile);
            }
            if (userProperties.containsKey(PARAMETER_FILTER_EXECUTION)) {
                session.setProjects(new ArrayList<>(moduleSelector.getSelectedProjects()));
            }
        } catch (Exception runtimeException) {
            logger.warning("Found pattern to select all modules, continuing without selection.");
        }
    }

    private void writeSelectedModulesToOutput(Set<MavenProject> selectedProjects, Path outputFile) {
        try {
            String projectList = selectedProjects.stream()
                    .map(project -> project.getBasedir().getAbsolutePath())
                    .sorted()
                    .collect(Collectors.joining("\n"));
            Files.write(outputFile,
                    projectList.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }


    private List<Path> getModulePathsFromFiles(List<Path> paths, MavenSession session) {
        return paths.stream().map((Path path) -> getModulePathsFromFile(path, session)).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<Path> getModulePathsFromFile(Path path, MavenSession session) throws AllModulesSelectionPatternFoundException {
        List<Path> paths = new ArrayList<>();
        try {
            BufferedReader br = Files.newBufferedReader(path);
            paths = br.lines().filter(line -> !line.equals("")).map(line -> {
                line = line.trim();
                if (line.equals(TRIGGER_ALL_MODULES)) {
                    throw new AllModulesSelectionPatternFoundException();
                }
                if (line.endsWith(POM_XML)) {
                    line = line.replace(POM_XML, "");
                }
                Path modulePath = Paths.get(line).toAbsolutePath();
                // In case the module path does not exist, we try the relative path to the session's root.
                if (!Files.exists(modulePath)) {
                    modulePath = Paths.get(session.getTopLevelProject().getBasedir().getAbsolutePath(), line);
                }
                return modulePath;
            }).collect(Collectors.toList());
        } catch (IOException exception) {
            logger.warning("Could not find any Maven modules in provided file at " + path);
            exception.printStackTrace();
        }
        return paths;
    }


    private void printSelectedModules(Set<MavenProject> selectedProjects) {
        System.out.println("--------------");
        System.out.println(selectedProjects.stream().map(MavenProject::getName).sorted().collect(Collectors.joining("\n")));
        System.out.println("--------------");
    }

    static class AllModulesSelectionPatternFoundException extends RuntimeException {

        private static final String errorMessage = "Found trigger for selecting all modules: " + TRIGGER_ALL_MODULES;

        public AllModulesSelectionPatternFoundException() {
            super(errorMessage);
        }
    }
}


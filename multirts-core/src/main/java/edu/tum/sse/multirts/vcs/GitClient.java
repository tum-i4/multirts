import java.io.BufferedReader;
import java.io.InputStreamReader;
    private Git gitRepo;
    private List<String> runProcessAndReturnOutput(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        process.waitFor();
        if (process.exitValue() != 0) {
            throw new RuntimeException("Failed to execute command " + command);
        }
        return lines;
    }

                "git -P -C %s show %s",
            List<String> lines = runProcessAndReturnOutput(command);
            content = lines.stream().collect(Collectors.joining(System.lineSeparator()));
                "git -P -C %s diff %s %s",
            List<String> lines = runProcessAndReturnOutput(command);
            diffItems = parseDiffOutput(lines);
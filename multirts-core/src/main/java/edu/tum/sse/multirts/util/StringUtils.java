package edu.tum.sse.multirts.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtils {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static List<String> collectAllLines(final InputStream inputStream) {
        return collectAllLines(inputStream, DEFAULT_CHARSET);
    }

    public static List<String> collectAllLines(final InputStream inputStream, Charset charset) {
        return new BufferedReader(
                new InputStreamReader(inputStream, charset))
                .lines().collect(Collectors.toList());
    }
}

package main.java.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

public final class Config {

    public static final Config CONFIG;

    static {
        // Set up mapper
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Config.class, new ConfigDeserializer());
        mapper.registerModule(module);
        // Load default
        Config temp;
        try {
            temp = mapper.readValue(Config.class.getClassLoader().getResource("config.json"), Config.class);
        } catch (IOException e) {
            // Can't run without charPossibilities
            temp = null;
            System.exit(1);
        }
        CONFIG = temp;
    }

    public final int length;
    public final int logLevel;
    public final String configuration;
    private final Map<String, JsonNode> options;

    private Config(int length, int logLevel, String configuration, Map<String, JsonNode> options) {
        this.length = length;
        this.logLevel = logLevel;
        this.configuration = configuration;
        this.options = options;
    }

    public <T> T getOption(String name, Function<JsonNode, T> function) {
        return function.apply(options.get(name));
    }

    private static class ConfigDeserializer extends StdDeserializer<Config> {

        protected ConfigDeserializer() {
            super((JavaType) null);
        }

        @Override
        public Config deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode root = jsonParser.getCodec().readTree(jsonParser);
            // Outer fields
            int length = root.get("length").asInt();
            int logLevel = root.get("log_level").asInt();
            String configuration = root.get("configuration").asText();
            // Configuration-specific options
            Iterator<Map.Entry<String, JsonNode>> optionsIterator = root.get("options").get(configuration).fields();
            Map<String, JsonNode> options = new HashMap<>();
            optionsIterator.forEachRemaining(e -> options.put(e.getKey(), e.getValue()));
            // Create new config object
            return new Config(length, logLevel, configuration, options);
        }
    }
}

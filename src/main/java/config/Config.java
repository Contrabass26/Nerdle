package main.java.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

public class Config {

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
    public final int filterThreshold;

    private Config(int length, int logLevel, int filterThreshold) {
        this.length = length;
        this.logLevel = logLevel;
        this.filterThreshold = filterThreshold;
    }

    private static class ConfigDeserializer extends StdDeserializer<Config> {

        protected ConfigDeserializer() {
            super((JavaType) null);
        }

        @Override
        public Config deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode root = jsonParser.getCodec().readTree(jsonParser);
            int length = root.get("length").asInt();
            int logLevel = root.get("logLevel").asInt();
            int filterThreshold = root.get("filterThreshold").asInt();
            return new Config(length, logLevel, filterThreshold);
        }
    }
}

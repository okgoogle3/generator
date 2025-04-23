package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.*;

public class DeviceJsonAggregator {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        Path inputDir = Paths.get("/home/thingsboard/Desktop/tb-asset-tracking-vc/device/");
        Path outputFile = inputDir.resolve("devices.json");

        ArrayNode combined = mapper.createArrayNode();

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(inputDir, "*.json")) {
            for (Path jsonFile : dirStream) {
                // Skip the output file if it already exists in the folder
                if (jsonFile.getFileName().toString().equals(outputFile.getFileName().toString())) {
                    continue;
                }
                JsonNode source = mapper.readTree(jsonFile.toFile());
                JsonNode converted = convertSourceToTarget(source);
                combined.add(converted);
            }
        }

        // Write out the aggregated array
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(outputFile.toFile(), combined);

        System.out.println("Wrote " + combined.size() + " devices to " + outputFile);
    }

    /**
     * Converts a single source JSON into your target structure.
     * Adjust the mappings below to match your result template.
     */
    private static JsonNode convertSourceToTarget(JsonNode source) {
        ObjectNode target = mapper.createObjectNode();

        // Example mappings – replace with your actual template fields:
        target.put("name", source.path("entity").path("name").asText());    // TODO: adjust if your name comes from a different path
        target.put("label", source.path("entity").path("label").asText(null));
        target.put("type", source.path("entity").path("type").asText());
        // e.g. if your template has a "group" field:
        // target.put("group", source.path("some").path("path").asText());

        // Copy attributes and relations wholesale if the structure matches:
        // Refactored attributes flattening
        JsonNode rawAttrs = source.path("attributes");
        if (rawAttrs.isObject()) {
            ObjectNode attrsNode = mapper.createObjectNode();
            rawAttrs.fields().forEachRemaining(scopeEntry -> {
                JsonNode arr = scopeEntry.getValue();
                if (arr.isArray()) {
                    for (JsonNode attr : arr) {
                        String key = attr.path("key").asText();
                        // Pick the first non-null value in priority order
                        if (!attr.path("strValue").isNull()) {
                            attrsNode.put(key, attr.path("strValue").asText());
                        } else if (!attr.path("booleanValue").isNull()) {
                            attrsNode.put(key, attr.path("booleanValue").asBoolean());
                        } else if (!attr.path("doubleValue").isNull()) {
                            attrsNode.put(key, attr.path("doubleValue").asDouble());
                        } else if (!attr.path("longValue").isNull()) {
                            attrsNode.put(key, attr.path("longValue").asLong());
                        } else if (!attr.path("jsonValue").isNull() && !attr.path("jsonValue").asText().isEmpty()) {
                            try {
                                JsonNode parsed = mapper.readTree(attr.path("jsonValue").asText());
                                attrsNode.set(key, parsed);
                            } catch (IOException e) {
                                // Fallback to raw string if parsing fails
                                attrsNode.put(key, attr.path("jsonValue").asText());
                            }
                        }
                    }
                }
            });
            target.set("attributes", attrsNode);
        }
        JsonNode rels = source.path("relations");
        if (!rels.isMissingNode()) {
            target.set("relations", rels);
        }

        // If you need a custom JSON ID field:
        // String jsonId = source.path("entity").path("id").path("id").asText(null);
        // if (jsonId != null) {
        //     target.put("jsonId", jsonId);
        // }

        // TODO: map any other fields your template requires

        return target;
    }
}

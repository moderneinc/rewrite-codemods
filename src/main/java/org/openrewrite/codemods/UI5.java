/*
 * Copyright 2025 the original author or authors.
 * <p>
 * Licensed under the Moderne Source Available License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://docs.moderne.io/licensing/moderne-source-available-license
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.codemods;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.text.PlainText;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openrewrite.Tree.randomId;

@Value
@EqualsAndHashCode(callSuper = false)
public class UI5 extends NodeBasedRecipe {

    private static final String UI5_DIR = Putout.class.getName() + ".UI5_DIR";

    transient UI5Messages messages = new UI5Messages(this);

    String displayName = "Lint UI5 projects with UI5 linter";

    String description = "Runs the [UI5 Linter](https://github.com/SAP/ui5-linter), a static code analysis tool for UI5 projects. It checks JavaScript, TypeScript, XML, JSON, and other files in your project and reports findings.";

    @Override
    public Accumulator getInitialValue(ExecutionContext ctx) {
        Path path = RecipeResources.from(getClass()).extractResources("config", UI5_DIR, ctx);
        ctx.putMessage(UI5_DIR, path);
        return super.getInitialValue(ctx);
    }

    @Override
    protected List<String> getNpmCommand(Accumulator acc, ExecutionContext ctx) {
        List<String> command = new ArrayList<>();
        command.add("sh");
        command.add("-c");
        command.add("node ${nodeModules}/@ui5/linter/bin/ui5lint.js --fix --format=json || true"); // ignore exit code
        return command;
    }

    @Override
    protected void processOutput(Path output, Accumulator acc, ExecutionContext ctx) {
        /**
         * The output of the UI5 linter looks like:
         * Array<{
         *   filePath: string;
         *   messages: Array<{
         *     ruleId: string;
         *     severity: 1 | 2
         *     line: number;
         *     column: number;
         *     message: string;
         *   }>;
         *   errorCount: number;
         *   warningCount: number;
         *   fatalErrorCount: number;
         * }>
         */
        try {
            Map<Path, JsonNode> results = new HashMap<>();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode resultsArray = objectMapper.readTree(output.toFile());

            for (JsonNode resultNode : resultsArray) {
                int errorCount = resultNode.get("errorCount").intValue();
                int warningCount = resultNode.get("warningCount").intValue();

                if (errorCount > 0 || warningCount > 0) {
                    Path filePath = Paths.get(resultNode.get("filePath").asText());
                    results.put(filePath, resultNode);
                }
            }
            acc.putData("results", results);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected SourceFile createAfter(SourceFile before, Accumulator acc, ExecutionContext ctx) {
        Map<Path, JsonNode> results = acc.getData("results");
        if (results == null) {
            return super.createAfter(before, acc, ctx);
        }

        JsonNode resultNode = results.get(before.getSourcePath());
        if (resultNode == null) {
            return super.createAfter(before, acc, ctx);
        }

        String content = acc.content(before);
        List<PlainText.Snippet> snippets = new ArrayList<>();
        SourcePosition currentPosition = new SourcePosition(content, 1, 1, 0);
        PlainText.Snippet currentSnippet = new PlainText.Snippet(randomId(), Markers.EMPTY, "");
        ArrayNode messagesNode = (ArrayNode) resultNode.get("messages");
        JsonNode previousMessage = null;

        for (int i = 0; i < messagesNode.size(); i++) {
            JsonNode message = messagesNode.get(i);
            int line = message.get("line").asInt();
            int column = message.get("column").asInt();
            SourcePosition nextPosition = currentPosition.scanForwardTo(line, column);
            if (nextPosition.offset > currentPosition.offset) {
                if (previousMessage != null) {
                    SourcePosition endPosition = currentPosition.scanForwardTo(previousMessage.get("endLine").intValue(), previousMessage.get("endColumn").intValue());
                    if (endPosition.offset < nextPosition.offset) {
                        snippets.add(currentSnippet.withText(content.substring(currentPosition.offset, endPosition.offset)));
                        currentSnippet = new PlainText.Snippet(randomId(), Markers.EMPTY, "");
                        currentPosition = endPosition;
                    }
                }
                snippets.add(currentSnippet.withText(content.substring(currentPosition.offset, nextPosition.offset)));
                currentSnippet = new PlainText.Snippet(randomId(), Markers.EMPTY, "");
            }

            int severity = message.get("severity").asInt();
            String messageText = message.get("message").asText();
            String ruleId = message.get("ruleId").asText();
            Marker marker = new SearchResult(randomId(), messageText);
            messages.insertRow(
                    ctx, new UI5Messages.Row(
                            before.getSourcePath().toString(),
                            ruleId,
                            UI5Messages.Severity.of(severity),
                            line,
                            column,
                            messageText
                    )
            );
            currentSnippet = currentSnippet.withMarkers(currentSnippet.getMarkers().add(marker));
            currentPosition = nextPosition;
            previousMessage = message.has("endLine") ? message : null;
        }

        if (previousMessage != null) {
            SourcePosition endPosition = currentPosition.scanForwardTo(previousMessage.get("endLine").intValue(), previousMessage.get("endColumn").intValue());
            if (endPosition.offset < content.length()) {
                snippets.add(currentSnippet.withText(content.substring(currentPosition.offset, endPosition.offset)));
                currentSnippet = new PlainText.Snippet(randomId(), Markers.EMPTY, "");
                currentPosition = endPosition;
            }
        }
        snippets.add(currentSnippet.withText(content.substring(currentPosition.offset)));

        return new PlainText(
                before.getId(),
                before.getSourcePath(),
                before.getMarkers(),
                before.getCharset() != null ? before.getCharset().name() : null,
                before.isCharsetBomMarked(),
                before.getFileAttributes(),
                null,
                "",
                snippets
        );
    }

    @Value
    private static class SourcePosition {
        String text;
        int line;
        int column;
        int offset;

        private SourcePosition scanForwardTo(int line, int column) {
            if (line == this.line && column == this.column) {
                return this;
            }
            int currentLine = this.line;
            int currentColumn = this.column;
            int currentOffset = this.offset;
            while (currentLine < line || (currentLine == line && currentColumn < column)) {
                if (text.charAt(currentOffset) == '\n') {
                    currentLine++;
                    currentColumn = 1;
                    currentOffset++;
                } else {
                    currentColumn++;
                    currentOffset++;
                }
            }
            return new SourcePosition(text, line, column, currentOffset);
        }
    }
}

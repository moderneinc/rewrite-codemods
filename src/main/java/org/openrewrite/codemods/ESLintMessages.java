/*
 * Moderne Proprietary. Only for use by Moderne customers under the terms of a commercial contract.
 */
package org.openrewrite.codemods;

import lombok.Value;
import org.openrewrite.Column;
import org.openrewrite.DataTable;
import org.openrewrite.Recipe;

public class ESLintMessages extends DataTable<ESLintMessages.Row> {

    public ESLintMessages(Recipe recipe) {
        super(recipe,
                "ESLint messages",
                "Errors and warnings as reported by ESLint.");
    }

    @Value
    public static class Row {
        @Column(displayName = "Source Path", description = "The source path of the file.")
        String sourcePath;

        @Column(displayName = "Rule ID", description = "ESLint Rule ID.")
        String ruleId;

        @Column(displayName = "Severity", description = "Either `Warning` or `Error`.")
        Severity severity;

        @Column(displayName = "Fatal", description = "Is this a fatal error (like a parse error).")
        boolean fatal;

        @Column(displayName = "Message", description = "The message created by the rule.")
        String message;

        @Column(displayName = "Line", description = "Line in source file this message pertains to.")
        int line;

        @Column(displayName = "Column", description = "Column in source file this message pertains to.")
        int column;
    }

    public enum Severity {
        Warning,
        Error;

        public static Severity of(int severity) {
            return severity == 1 ? Warning : Error;
        }
    }
}

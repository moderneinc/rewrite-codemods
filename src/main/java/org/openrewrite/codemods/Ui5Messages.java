/*
 * Copyright 2025 the original author or authors.
 *
 * Moderne Proprietary. Only for use by Moderne customers under the terms of a commercial contract.
 */
package org.openrewrite.codemods;

import lombok.Value;
import org.openrewrite.Column;
import org.openrewrite.DataTable;
import org.openrewrite.Recipe;

public class Ui5Messages extends DataTable<Ui5Messages.Row> {

    public Ui5Messages(Recipe recipe) {
        super(recipe,
                "Ui5 messages",
                "Errors and warnings as reported by Ui5.");
    }

    @Value
    public static class Row {
        @Column(displayName = "File Path", description = "The source path of the file.")
        String sourcePath;

        @Column(displayName = "Rule ID", description = "UI5 rule ID.")
        String ruleId;

        @Column(displayName = "Severity", description = "Either `Warning` or `Error`.")
        Severity severity;

        @Column(displayName = "Line", description = "Line in source file this message pertains to.")
        int line;

        @Column(displayName = "Column", description = "Column in source file this message pertains to.")
        int column;

        @Column(displayName = "Message", description = "The message created by the rule.")
        String message;
    }

    public enum Severity {
        Warning,
        Error;

        public static Severity of(int severity) {
            return severity == 1 ? Warning : Error;
        }
    }
}

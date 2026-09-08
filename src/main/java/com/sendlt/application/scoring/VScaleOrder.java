package com.sendlt.application.scoring;

import com.sendlt.application.exception.BusinessValidationException;

public final class VScaleOrder {

    private VScaleOrder() {}

    public static int ordinal(String grade) {
        if (grade == null || grade.isBlank()) {
            return 0;
        }
        String normalized = grade.trim().toUpperCase();
        if (!normalized.startsWith("V")) {
            throw new BusinessValidationException("Unknown V-Scale grade: " + grade);
        }
        try {
            return Integer.parseInt(normalized.substring(1)) + 1;
        } catch (NumberFormatException ex) {
            throw new BusinessValidationException("Unknown V-Scale grade: " + grade);
        }
    }

    public static boolean isInRange(String grade, String min, String max) {
        if (grade == null || grade.isBlank()) {
            return false;
        }
        int value = ordinal(grade);
        if (min != null && !min.isBlank() && value < ordinal(min)) {
            return false;
        }
        if (max != null && !max.isBlank() && value > ordinal(max)) {
            return false;
        }
        return true;
    }
}

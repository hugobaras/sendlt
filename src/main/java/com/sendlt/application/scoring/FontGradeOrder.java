package com.sendlt.application.scoring;

import com.sendlt.application.exception.BusinessValidationException;
import java.util.List;

public final class FontGradeOrder {

    private static final List<String> ORDERED_GRADES = List.of(
            "4", "4+", "5a", "5a+", "5b", "5b+", "5c", "5c+",
            "6a", "6a+", "6b", "6b+", "6c", "6c+",
            "7a", "7a+", "7b", "7b+", "7c", "7c+",
            "8a", "8a+", "8b", "8b+", "8c", "8c+",
            "9a");

    private FontGradeOrder() {}

    public static int ordinal(String grade) {
        if (grade == null || grade.isBlank()) {
            return 0;
        }
        String normalized = grade.trim().toLowerCase();
        int index = ORDERED_GRADES.indexOf(normalized);
        if (index < 0) {
            throw new BusinessValidationException("Unknown Font grade: " + grade);
        }
        return index + 1;
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

    public static String fromOrdinal(int ordinal) {
        if (ordinal < 1 || ordinal > ORDERED_GRADES.size()) {
            return "UNKNOWN";
        }
        return ORDERED_GRADES.get(ordinal - 1);
    }
}

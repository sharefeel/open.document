package net.youngrok.snippet.withoutlombok;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class FinalAuto {
    public void example() {
        final Map<String, Map<Integer, Map<String, Map<Long, List<LocalDateTime>>>>> bareValue = complexReturnMethod();
        for (Map.Entry<String, Map<Integer, Map<String, Map<Long, List<LocalDateTime>>>>> entry1 : bareValue.entrySet()) {
            for (Map.Entry<Integer, Map<String, Map<Long, List<LocalDateTime>>>> entry2 : entry1.getValue().entrySet()) {
                System.out.println(entry2);
            }
        }
    }

    private Map<String, Map<Integer, Map<String, Map<Long, List<LocalDateTime>>>>> complexReturnMethod() {
        return new HashMap<>();
    }
}

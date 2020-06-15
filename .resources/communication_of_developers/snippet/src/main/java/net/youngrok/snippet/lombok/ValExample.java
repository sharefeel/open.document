package net.youngrok.snippet.lombok;

import lombok.val;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ValExample {
    public void valExample() {
        val lombokValue = complexReturnMethod();
        for (val entry1 : lombokValue.entrySet()) {
            for (val entry2 : entry1.getValue().entrySet()) {
                System.out.println(entry2);
            }
        }

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

package net.youngrok.snippet.withlombok;

import lombok.val;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class FinalAuto {
    public void valExample() {
        val lombokValue = complexReturnMethod();
        for (val entry1 : lombokValue.entrySet()) {
            for (val entry2 : entry1.getValue().entrySet()) {
                System.out.println(entry2);
            }
        }
    }

    private Map<String, Map<Integer, Map<String, Map<Long, List<LocalDateTime>>>>> complexReturnMethod() {
        return new HashMap<>();
    }
}

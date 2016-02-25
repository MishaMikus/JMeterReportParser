package epam;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserErrorEntry {
    public Set<String> threadNameSet = new HashSet<>();
    public Map<String,Long> errorCountByTCMap = new HashMap<>();

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("UserErrorEntry{");
        sb.append("errorCountByTCMap=").append(errorCountByTCMap);
        sb.append(", threadNameSet=").append(threadNameSet);
        sb.append('}');
        return sb.toString();
    }
}

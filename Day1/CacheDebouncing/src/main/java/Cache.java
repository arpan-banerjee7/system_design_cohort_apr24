import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class Cache {
    private final Map<String, String> cache;

    public Optional<String> get(String key) {
        return Optional.ofNullable(cache.get(key));
    }

    public void put(String key, String value) {
        cache.put(key, value);
    }

}

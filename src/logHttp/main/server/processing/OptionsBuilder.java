package server.processing;

import java.time.LocalDateTime;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import common.ELevel;
import common.Options;
import server.interfaces.IOptionsBuilder;
import server.models.EQueryOption;
import utils.Err;
import utils.ErrorType;
import utils.Ok;
import utils.Result;

public class OptionsBuilder implements IOptionsBuilder {
    public Result<Options> build(String query) {
        var allOptionsR = parseQuery(query);
        if (allOptionsR instanceof Err<?> err) return new Err<>(err.what(), ErrorType.REAL_ERROR);
        var allOptions = ((Ok<Map<EQueryOption, String>>)allOptionsR).value();

        Optional<LocalDateTime> since = Optional.empty();
        Optional<LocalDateTime> before = Optional.empty();
        Optional<String> service = Optional.empty();
        Optional<ELevel> level = Optional.empty();

        for (EQueryOption option : allOptions.keySet()) {
            switch (option) {
                case SINCE:
                    since = Optional.of(LocalDateTime.parse(allOptions.get(option)));
                    break;
                
                case BEFORE:
                    before = Optional.of(LocalDateTime.parse(allOptions.get(option)));
                    break;
                
                case SERVICE:
                    service = Optional.of(allOptions.get(option));

                case LEVEL:
                   level = ELevel.fromString(allOptions.get(option)); 
            }
        }

        return new Ok<Options>(new Options(since, before, service, level));
    }

    private Result<Map<EQueryOption, String>> parseQuery(String query) {
        Map<EQueryOption, String> result = new HashMap<>();
        List<String> pairs = Arrays.stream(query.split("&"))
            .filter((s) -> !s.isEmpty())
            .toList();
        
        for (String pair : pairs) {
            List<String> keyValue = Arrays.stream(pair.split("="))
                .filter((s) -> !s.isEmpty())
                .toList();

            if (keyValue.size() != 2) 
                return new Err<>("Broken option in query: " + pair, ErrorType.REAL_ERROR);
            
            Result<EQueryOption> keyR = EQueryOption.fromString(keyValue.get(0));
            if (keyR instanceof Err<?> err) return new Err<>(err.what(), ErrorType.REAL_ERROR);
            EQueryOption key = ((Ok<EQueryOption>)keyR).value();

            String value = keyValue.get(1);
            if (!key.check(value)) return new Err<>(
                String.format("Value: %s not compatible with type: %s", value, key),
                ErrorType.REAL_ERROR
            );
            if (result.containsKey(key)) 
                return new Err<>("duplicating parameter: " + key, ErrorType.REAL_ERROR);

            result.put(key, value);
        }

        return new Ok<>(result);
    }
}

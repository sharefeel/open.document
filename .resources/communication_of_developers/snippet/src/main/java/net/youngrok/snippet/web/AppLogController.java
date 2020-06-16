package net.youngrok.snippet.web;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import net.youngrok.snippet.database.AppLogEntity;
import net.youngrok.snippet.database.AppLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class AppLogController {
    private final AppLogRepository repository;

    @ApiOperation("Add new log API")
    @PostMapping(value = "/applog", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> newLog(@RequestBody AppLog appLog) {
        repository.saveAndFlush(AppLogEntity.newEntity(appLog));
        return ResponseEntity.ok("OK");
    }

    @ApiOperation("Log count api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "API-KEY", value = "api key", paramType = "header", required = true, example = "valid_api_key"),
            @ApiImplicitParam(name = "market", value = "조회할 market", allowableValues = "all, appstore, playstore", required = true, example = "appstore")
    })
    @GetMapping(value = "/applog/count/{market}")
    public ResponseEntity<LogCount> countLog(@PathVariable("market") String market,
                                             @RequestHeader("API-KEY") String apiKey) {
        if (!apiKey.equals("valid_api_key")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LogCount().setMarket(market).setCount(-1));
        }

        long count;
        if (market.equalsIgnoreCase("all")) {
            count = repository.count();
        } else {
            count = repository.countByMarket(market);
        }
        return ResponseEntity.ok(new LogCount().setMarket(market).setCount(count));
    }
}

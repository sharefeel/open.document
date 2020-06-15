package net.youngrok.snippet.database;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppLogRepository extends JpaRepository<AppLogEntity, Long> {
    long countByMarket(String market);
}

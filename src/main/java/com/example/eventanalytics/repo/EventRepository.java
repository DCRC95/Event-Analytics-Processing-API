package com.example.eventanalytics.repo;

import com.example.eventanalytics.domain.entity.EventEntity;
import com.example.eventanalytics.repo.projection.DailyTotalProjection;
import com.example.eventanalytics.repo.projection.EntityCountProjection;
import com.example.eventanalytics.repo.projection.TypeCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {

  // 1) totalEvents in range
  @Query(value = """
      select count(*)
      from events
      where user_id = :userId
        and occurred_at >= :fromInclusive
        and occurred_at < :toExclusive
      """, nativeQuery = true)
  long countInRange(
      @Param("userId") UUID userId,
      @Param("fromInclusive") Instant fromInclusive,
      @Param("toExclusive") Instant toExclusive
  );

  // 2) countsByType in range
  @Query(value = """
      select type as type, count(*) as count
      from events
      where user_id = :userId
        and occurred_at >= :fromInclusive
        and occurred_at < :toExclusive
      group by type
      order by count desc, type asc
      """, nativeQuery = true)
  List<TypeCountProjection> countByTypeInRange(
      @Param("userId") UUID userId,
      @Param("fromInclusive") Instant fromInclusive,
      @Param("toExclusive") Instant toExclusive
  );

  // 3) dailyTotals in range (UTC day buckets)
  @Query(value = """
      select (occurred_at at time zone 'UTC')::date as date, count(*) as count
      from events
      where user_id = :userId
        and occurred_at >= :fromInclusive
        and occurred_at < :toExclusive
      group by date
      order by date asc
      """, nativeQuery = true)
  List<DailyTotalProjection> dailyTotalsInRange(
      @Param("userId") UUID userId,
      @Param("fromInclusive") Instant fromInclusive,
      @Param("toExclusive") Instant toExclusive
  );

  // 4) topEntities overall in range (top-K)
  @Query(value = """
      select entity_type as entityType, entity_id as entityId, count(*) as count
      from events
      where user_id = :userId
        and occurred_at >= :fromInclusive
        and occurred_at < :toExclusive
      group by entity_type, entity_id
      order by count desc, entity_type asc, entity_id asc
      limit :limit
      """, nativeQuery = true)
  List<EntityCountProjection> topEntitiesInRange(
      @Param("userId") UUID userId,
      @Param("fromInclusive") Instant fromInclusive,
      @Param("toExclusive") Instant toExclusive,
      @Param("limit") int limit
  );

  // 5) topEntities by type in range (endpoint B)
  @Query(value = """
      select entity_type as entityType, entity_id as entityId, count(*) as count
      from events
      where user_id = :userId
        and type = :type
        and occurred_at >= :fromInclusive
        and occurred_at < :toExclusive
      group by entity_type, entity_id
      order by count desc, entity_type asc, entity_id asc
      limit :limit
      """, nativeQuery = true)
  List<EntityCountProjection> topEntitiesByTypeInRange(
      @Param("userId") UUID userId,
      @Param("type") String type,
      @Param("fromInclusive") Instant fromInclusive,
      @Param("toExclusive") Instant toExclusive,
      @Param("limit") int limit
  );
}
/*We are using native SQL here because: 
-date bucketing and limit are trivial in Postgres
-It avoid JPQL quirks and keep the query plan predictable

-Using JPQL with functions that behave differently per DB
-It avoids returning huge datasets and sorting/grouping in Java
 */

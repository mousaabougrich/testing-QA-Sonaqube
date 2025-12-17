package com.wallet.biochain.repositories;

import com.wallet.biochain.entities.Node;
import com.wallet.biochain.enums.NodeStatus;
import com.wallet.biochain.enums.NodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface NodeRepository extends JpaRepository<Node, Long> {

    Optional<Node> findByNodeId(String nodeId);

    Optional<Node> findByIpAddressAndPort(String ipAddress, Integer port);

    List<Node> findByStatus(NodeStatus status);

    List<Node> findByNodeType(NodeType nodeType);

    List<Node> findByIsTrusted(Boolean isTrusted);

    @Query("SELECT n FROM Node n WHERE n.status = 'ACTIVE' AND n.lastSeen > :since")
    List<Node> findActiveNodesSince(@Param("since") LocalDateTime since);

    @Query("SELECT n FROM Node n WHERE n.status = 'ACTIVE' ORDER BY n.blockHeight DESC")
    List<Node> findActiveNodesOrderByBlockHeight();

    @Query("SELECT n FROM Node n WHERE n.status = 'ACTIVE' ORDER BY n.latencyMs ASC")
    List<Node> findActiveNodesOrderByLatency();

    @Query("SELECT n FROM Node n WHERE n.blockHeight >= :minHeight")
    List<Node> findNodesByMinHeight(@Param("minHeight") Integer minHeight);

    @Query("SELECT COUNT(n) FROM Node n WHERE n.status = :status")
    Long countByStatus(@Param("status") NodeStatus status);

    @Query("SELECT AVG(n.blockHeight) FROM Node n WHERE n.status = 'ACTIVE'")
    Double getAverageBlockHeight();

    @Query("SELECT AVG(n.latencyMs) FROM Node n WHERE n.status = 'ACTIVE'")
    Double getAverageLatency();

    @Query("SELECT n FROM Node n WHERE n.lastSeen < :threshold")
    List<Node> findStaleNodes(@Param("threshold") LocalDateTime threshold);

    boolean existsByNodeId(String nodeId);

    boolean existsByIpAddressAndPort(String ipAddress, Integer port);
}
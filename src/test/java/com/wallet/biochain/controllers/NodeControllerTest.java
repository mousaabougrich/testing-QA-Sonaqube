package com.wallet.biochain.controllers;

import com.wallet.biochain.dto.NodeConnectionRequestDTO;
import com.wallet.biochain.dto.NodeInfoDTO;
import com.wallet.biochain.dto.PeerListDTO;
import com.wallet.biochain.enums.NodeStatus;
import com.wallet.biochain.enums.NodeType;
import com.wallet.biochain.services.NodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class NodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NodeService nodeService;

    @Test
    void registerNode_success() throws Exception {
        NodeInfoDTO dto = new NodeInfoDTO(1L, "node1", "127.0.0.1", 8080, 
                NodeStatus.ACTIVE, NodeType.FULL_NODE, null, 0, "v1", 0L, false, 0, 0);
        
        when(nodeService.registerNode(any())).thenReturn(dto);

        mockMvc.perform(post("/api/nodes/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nodeId\":\"node1\",\"ipAddress\":\"127.0.0.1\",\"port\":8080,\"nodeType\":\"FULL_NODE\",\"version\":\"v1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nodeId").value("node1"));

        verify(nodeService).registerNode(any());
    }

    @Test
    void registerNode_failure() throws Exception {
        when(nodeService.registerNode(any())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/nodes/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nodeId\":\"node1\",\"ipAddress\":\"127.0.0.1\",\"port\":8080,\"nodeType\":\"FULL_NODE\",\"version\":\"v1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNodeById_found() throws Exception {
        NodeInfoDTO dto = new NodeInfoDTO(1L, "node1", "127.0.0.1", 8080, 
                NodeStatus.ACTIVE, NodeType.FULL_NODE, null, 0, "v1", 0L, false, 0, 0);
        
        when(nodeService.getNodeById(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/nodes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeId").value("node1"));
    }

    @Test
    void getNodeById_notFound() throws Exception {
        when(nodeService.getNodeById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/nodes/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getNodeByNodeId_found() throws Exception {
        NodeInfoDTO dto = new NodeInfoDTO(1L, "node1", "127.0.0.1", 8080, 
                NodeStatus.ACTIVE, NodeType.FULL_NODE, null, 0, "v1", 0L, false, 0, 0);
        
        when(nodeService.getNodeByNodeId("node1")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/nodes/node-id/node1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeId").value("node1"));
    }

    @Test
    void getAllNodes_success() throws Exception {
        NodeInfoDTO dto1 = new NodeInfoDTO(1L, "node1", "127.0.0.1", 8080, 
                NodeStatus.ACTIVE, NodeType.FULL_NODE, null, 0, "v1", 0L, false, 0, 0);
        NodeInfoDTO dto2 = new NodeInfoDTO(2L, "node2", "127.0.0.2", 8081, 
                NodeStatus.ACTIVE, NodeType.FULL_NODE, null, 0, "v1", 0L, false, 0, 0);
        
        when(nodeService.getAllNodes()).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/nodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getActiveNodes_success() throws Exception {
        NodeInfoDTO dto = new NodeInfoDTO(1L, "node1", "127.0.0.1", 8080, 
                NodeStatus.ACTIVE, NodeType.FULL_NODE, null, 0, "v1", 0L, false, 0, 0);
        
        when(nodeService.getActiveNodes()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/nodes/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getNodesByStatus_success() throws Exception {
        NodeInfoDTO dto = new NodeInfoDTO(1L, "node1", "127.0.0.1", 8080, 
                NodeStatus.ACTIVE, NodeType.FULL_NODE, null, 0, "v1", 0L, false, 0, 0);
        
        when(nodeService.getNodesByStatus(NodeStatus.ACTIVE)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/nodes/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateNodeStatus_success() throws Exception {
        doNothing().when(nodeService).updateNodeStatus("node1", NodeStatus.ACTIVE);

        mockMvc.perform(put("/api/nodes/node1/status")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk());

        verify(nodeService).updateNodeStatus("node1", NodeStatus.ACTIVE);
    }

    @Test
    void updateNodeStatus_notFound() throws Exception {
        doThrow(new IllegalArgumentException("Not found"))
                .when(nodeService).updateNodeStatus("node1", NodeStatus.ACTIVE);

        mockMvc.perform(put("/api/nodes/node1/status")
                .param("status", "ACTIVE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBlockHeight_success() throws Exception {
        doNothing().when(nodeService).updateBlockHeight("node1", 100);

        mockMvc.perform(put("/api/nodes/node1/block-height")
                .param("blockHeight", "100"))
                .andExpect(status().isOk());

        verify(nodeService).updateBlockHeight("node1", 100);
    }

    @Test
    void connectToPeer_success() throws Exception {
        doNothing().when(nodeService).connectToPeer("node1", "node2");

        mockMvc.perform(post("/api/nodes/node1/connect/node2"))
                .andExpect(status().isOk());

        verify(nodeService).connectToPeer("node1", "node2");
    }

    @Test
    void disconnectFromPeer_success() throws Exception {
        doNothing().when(nodeService).disconnectFromPeer("node1", "node2");

        mockMvc.perform(post("/api/nodes/node1/disconnect/node2"))
                .andExpect(status().isOk());

        verify(nodeService).disconnectFromPeer("node1", "node2");
    }

    @Test
    void getPeerList_success() throws Exception {
        PeerListDTO dto = new PeerListDTO(List.of(), 0, 0);
        
        when(nodeService.getPeerList("node1")).thenReturn(dto);

        mockMvc.perform(get("/api/nodes/node1/peers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPeers").value(0));
    }

    @Test
    void pingNode_success() throws Exception {
        when(nodeService.pingNode("node1")).thenReturn(true);

        mockMvc.perform(get("/api/nodes/node1/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void getNodeLatency_success() throws Exception {
        when(nodeService.getNodeLatency("node1")).thenReturn(100L);

        mockMvc.perform(get("/api/nodes/node1/latency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(100));
    }

    @Test
    void trustNode_success() throws Exception {
        doNothing().when(nodeService).trustNode("node1");

        mockMvc.perform(post("/api/nodes/node1/trust"))
                .andExpect(status().isOk());

        verify(nodeService).trustNode("node1");
    }

    @Test
    void untrustNode_success() throws Exception {
        doNothing().when(nodeService).untrustNode("node1");

        mockMvc.perform(post("/api/nodes/node1/untrust"))
                .andExpect(status().isOk());

        verify(nodeService).untrustNode("node1");
    }

    // Commented out - /sync endpoint doesn't exist in NodeController
    // @Test
    // void synchronizeWithPeer_success() throws Exception {
    //     doNothing().when(nodeService).synchronizeWithPeer("node1", "node2");

    //     mockMvc.perform(post("/api/nodes/node1/sync")
    //             .param("peerNodeId", "node2"))
    //             .andExpect(status().isOk());

    //     verify(nodeService).synchronizeWithPeer("node1", "node2");
    // }
}

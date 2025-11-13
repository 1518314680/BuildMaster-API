package com.buildmaster.repository;

import com.buildmaster.model.ComponentKnowledge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 配件知识库Repository
 */
@Repository
public interface ComponentKnowledgeRepository extends MongoRepository<ComponentKnowledge, String> {

    /**
     * 根据配件ID查找知识
     */
    List<ComponentKnowledge> findByComponentId(Long componentId);

    /**
     * 根据配件类型查找知识
     */
    List<ComponentKnowledge> findByComponentType(String componentType);

    /**
     * 根据标签查找知识
     */
    List<ComponentKnowledge> findByTagsContaining(String tag);

    /**
     * 查找未向量化的知识
     */
    List<ComponentKnowledge> findByVectorizedFalse();

    /**
     * 根据向量ID查找知识
     */
    ComponentKnowledge findByVectorId(String vectorId);
}


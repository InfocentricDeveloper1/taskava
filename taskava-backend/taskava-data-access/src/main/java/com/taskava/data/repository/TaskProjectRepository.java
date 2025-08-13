package com.taskava.data.repository;

import com.taskava.data.entity.TaskProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskProjectRepository extends JpaRepository<TaskProject, UUID> {
    
    Optional<TaskProject> findByTaskIdAndProjectId(UUID taskId, UUID projectId);
    
    List<TaskProject> findByTaskId(UUID taskId);
    
    List<TaskProject> findByProjectId(UUID projectId);
    
    List<TaskProject> findByProjectIdAndSectionId(UUID projectId, UUID sectionId);
    
    @Query("SELECT tp FROM TaskProject tp WHERE tp.project.id = :projectId AND tp.section IS NULL")
    List<TaskProject> findByProjectIdWithNoSection(@Param("projectId") UUID projectId);
    
    @Modifying
    @Query("DELETE FROM TaskProject tp WHERE tp.task.id = :taskId AND tp.project.id = :projectId")
    void deleteByTaskIdAndProjectId(@Param("taskId") UUID taskId, @Param("projectId") UUID projectId);
    
    @Query("SELECT COUNT(tp) > 0 FROM TaskProject tp WHERE tp.task.id = :taskId AND tp.project.id = :projectId")
    boolean existsByTaskIdAndProjectId(@Param("taskId") UUID taskId, @Param("projectId") UUID projectId);
    
    @Query("SELECT MAX(tp.position) FROM TaskProject tp WHERE tp.project.id = :projectId AND tp.section.id = :sectionId")
    Optional<Integer> findMaxPositionInSection(@Param("projectId") UUID projectId, @Param("sectionId") UUID sectionId);
    
    @Query("SELECT MAX(tp.position) FROM TaskProject tp WHERE tp.project.id = :projectId AND tp.section IS NULL")
    Optional<Integer> findMaxPositionInProjectWithNoSection(@Param("projectId") UUID projectId);
    
    @Modifying
    @Query("""
        UPDATE TaskProject tp 
        SET tp.position = tp.position + 1 
        WHERE tp.project.id = :projectId 
        AND tp.section.id = :sectionId 
        AND tp.position >= :position
    """)
    void incrementPositionsInSection(@Param("projectId") UUID projectId, 
                                    @Param("sectionId") UUID sectionId, 
                                    @Param("position") Integer position);
    
    @Modifying
    @Query("""
        UPDATE TaskProject tp 
        SET tp.position = tp.position - 1 
        WHERE tp.project.id = :projectId 
        AND tp.section.id = :sectionId 
        AND tp.position > :position
    """)
    void decrementPositionsInSection(@Param("projectId") UUID projectId, 
                                    @Param("sectionId") UUID sectionId, 
                                    @Param("position") Integer position);
    
    @Query("SELECT tp.project.id FROM TaskProject tp WHERE tp.task.id = :taskId")
    List<UUID> findProjectIdsByTaskId(@Param("taskId") UUID taskId);
}
package com.buildmaster.repository;

import com.buildmaster.model.BuildConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildConfigRepository extends JpaRepository<BuildConfig, Long> {
    
    List<BuildConfig> findByUserId(Long userId);
    
    List<BuildConfig> findByIsPublicTrue();
    
    @Query("SELECT bc FROM BuildConfig bc WHERE bc.userId = :userId OR bc.isPublic = true")
    List<BuildConfig> findByUserIdOrPublic(@Param("userId") Long userId);
    
    @Query("SELECT bc FROM BuildConfig bc WHERE bc.name LIKE %:keyword% AND (bc.userId = :userId OR bc.isPublic = true)")
    List<BuildConfig> searchByKeyword(@Param("keyword") String keyword, @Param("userId") Long userId);
}

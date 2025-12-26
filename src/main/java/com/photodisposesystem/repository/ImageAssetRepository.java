package com.photodisposesystem.repository;

import com.photodisposesystem.model.ImageAsset;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageAssetRepository extends JpaRepository<ImageAsset, Long> {
    List<ImageAsset> findByUserId(Long userId);
}

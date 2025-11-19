package gov.lby.cityissuetracker.repository;

import gov.lby.cityissuetracker.entity.IssueImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IssueImageRepository extends JpaRepository<IssueImage, UUID> {

    List<IssueImage> findByIssueIdOrderByCreatedAtAsc(UUID issueId);

    int countByIssueId(UUID issueId);

    void deleteByIssueId(UUID issueId);
}

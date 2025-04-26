/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.repo;

import fi.asteriski.nakitin.entity.OrganizationEntity;
import fi.asteriski.nakitin.repo.projection.IdAndNameProjection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {
    Optional<OrganizationEntity> findByName(@NonNull String name);

    List<IdAndNameProjection> readAllByName(@NonNull String name);

    @NativeQuery(
            value =
                    """
        select id, name
        from organizations where id in (select organization_id from organizationadmins where user_id = :userId)
        order by name asc
        """)
    List<IdAndNameProjection> fetchUsersOrganizations(@Param("userId") UUID userId);

    @NativeQuery(
            value =
                    """
        select id, name
            from organizations
            order by name asc
        """)
    List<IdAndNameProjection> readAll();

    @Query("SELECT o FROM OrganizationEntity o WHERE o.id = :id")
    @EntityGraph(value = "graph_organizations_users")
    Optional<OrganizationEntity> findByIdWithUsers(@Param("id") UUID id);

    @EntityGraph(value = "graph_organizations_users")
    @NonNull
    Page<OrganizationEntity> findAll(@NonNull Pageable pageable);
}

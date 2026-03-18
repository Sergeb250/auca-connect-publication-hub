package auca.ac.rw.connect.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Single ordered publication author record.
 */
@Entity
@Table(name = "publication_authors", indexes = {
        @Index(name = "idx_publication_authors_publication_id", columnList = "publication_id"),
        @Index(name = "idx_publication_authors_user_id", columnList = "user_id"),
        @Index(name = "idx_publication_authors_author_order", columnList = "author_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PublicationAuthor extends BaseEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "institution")
    private String institution;

    @Column(name = "role", nullable = false)
    private String role;

    @Builder.Default
    @Column(name = "author_order", nullable = false)
    private int authorOrder = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id", nullable = false)
    @JsonBackReference("publication-authors")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Publication publication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;
}

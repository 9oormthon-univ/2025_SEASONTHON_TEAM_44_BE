package groom._55.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stamp extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private int useStamp;

    @Column
    private int totalStamp;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToOne
    private Store store;
}

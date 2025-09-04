package goorm._44.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Noti extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)   // enum 이름 그대로 저장
    @Column
    private NotiTarget target;     // ALL / BASIC / CERTIFIED

    @Column
    private Integer targetCount;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @OneToMany(mappedBy = "noti")
    private List<NotiRead> notiRead = new ArrayList<>();

    @ManyToOne
    private Store store;
}

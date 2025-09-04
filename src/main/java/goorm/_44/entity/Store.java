package goorm._44.entity;

import goorm._44.dto.request.StoreCreateRequest;
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
public class Store extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String phone;

    @Column
    private String address;

    @Column
    private String detailAddress;

    @Column
    private String introduction;

    @Column(name = "image_key", length = 512)
    private String imageKey;

    @Column
    private Integer open;

    @Column
    private Integer close;

    @Column
    private int yesterdayNewRegular;

    @Column
    private int yesterdayRevisitRegular;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Stamp> stamp = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StampLog> log = new ArrayList<>();
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Noti>  noti = new ArrayList<>();

    public static Store from(StoreCreateRequest req, User user) {
        return Store.builder()
                .name(req.name())
                .imageKey(req.imageKey())
                .introduction(req.introduction())
                .phone(req.phone())
                .address(req.address())
                .detailAddress(req.detailAddress())
                .open(req.open())
                .close(req.close())
                .user(user)
                .build();
    }
}
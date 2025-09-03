package groom._55.entity;

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
    private String category;

    @Column
    private String address;

    @Column
    private String detailAddress;

    @Column
    private String introduction;

    @Column(name = "image_key", length = 512)
    private String imageKey;

    @Column
    private String MondayOpen;
    @Column
    private String TuesdayOpen;
    @Column
    private String WednesdayOpen;
    @Column
    private String ThursdayOpen;
    @Column
    private String FridayOpen;
    @Column
    private String SaturdayOpen;
    @Column
    private String SundayOpen;

    @Column
    private String MondayClose;
    @Column
    private String TuesdayClose;
    @Column
    private String WednesdayClose;
    @Column
    private String ThursdayClose;
    @Column
    private String FridayClose;
    @Column
    private String SaturdayClose;
    @Column
    private String SundayClose;

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

}

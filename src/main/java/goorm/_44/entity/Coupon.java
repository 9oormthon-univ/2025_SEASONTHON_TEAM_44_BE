package goorm._44.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coupon",
        uniqueConstraints = @UniqueConstraint(name = "uk_coupon_store", columnNames = "store_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false, length = 255)
    private String benefit;

    @Builder.Default
    @Column(nullable = false)
    private int requiredStamp = 10;

    public Coupon(Store store, String name, String benefit, Integer requiredStamp) {
        this.store = store;
        this.name = (name == null || name.isBlank()) ? "10회 인증 쿠폰" : name;
        this.benefit = (benefit == null) ? "" : benefit;
        if (requiredStamp != null && requiredStamp > 0) this.requiredStamp = requiredStamp;
    }

    public static Coupon createDefault(Store store) {
        return new Coupon(store, "10회 인증 쿠폰", "", 10);
    }

    public void update(String name, String benefit, Integer requiredStamp) {
        if (name != null && !name.isBlank()) this.name = name;
        if (benefit != null) this.benefit = benefit;
        if (requiredStamp != null && requiredStamp > 0) this.requiredStamp = requiredStamp;
    }
}

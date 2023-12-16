package com.giza.center_reservation.entities;

import com.giza.center_reservation.enumeration.PackageType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "package")
@NoArgsConstructor
public class PackageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    private PackageType type;

    @ManyToOne
    private Center center;

    @Column(name = "center_id", insertable = false, updatable = false)
    private int centerId;

    public PackageEntity(PackageType type, Center center) {
        this.type = type;
        this.center = center;
    }
}

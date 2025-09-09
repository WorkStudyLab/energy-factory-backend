package com.energyfactory.energy_factory.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT NOT NULL COMMENT '태크 ID'")
    private Long id;

    @Column(name = "name", unique = true, nullable = false, columnDefinition = "VARCHAR(255) NOT NULL COMMENT '태그명(고단백,다이어트,저지방 등등)'")
    private String name;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL)
    private List<ProductTag> productTags = new ArrayList<>();
}
